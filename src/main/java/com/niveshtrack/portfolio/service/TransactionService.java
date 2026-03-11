package com.niveshtrack.portfolio.service;

import com.niveshtrack.portfolio.dto.request.CreateTransactionRequest;
import com.niveshtrack.portfolio.dto.request.UpdateTransactionRequest;
import com.niveshtrack.portfolio.dto.response.TransactionDTO;
import com.niveshtrack.portfolio.entity.*;
import com.niveshtrack.portfolio.exception.ResourceNotFoundException;
import com.niveshtrack.portfolio.exception.ValidationException;
import com.niveshtrack.portfolio.repository.AssetPriceHistoryRepository;
import com.niveshtrack.portfolio.repository.StockRepository;
import com.niveshtrack.portfolio.repository.TransactionRepository;
import com.niveshtrack.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CRUD operations for stock/MF transactions with wallet integration.
 *
 * <p>BUY flow: validate wallet balance → save transaction → debit wallet → update holding.
 * <p>SELL flow: validate holding quantity → save transaction → credit wallet → update holding.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final HoldingsService holdingsService;
    private final WalletService walletService;
    private final HoldingService holdingService;
    private final AssetPriceHistoryRepository priceHistoryRepository;

    // ===== Read =====

    /**
     * Returns all transactions for a user, optionally filtered by stock symbol.
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getAllTransactions(Long userId, String symbol) {
        List<Transaction> transactions;
        if (StringUtils.hasText(symbol)) {
            transactions = transactionRepository.findByUserIdAndStockSymbol(userId, symbol.toUpperCase());
        } else {
            transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
        }
        return transactions.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Returns a single transaction by ID, verifying the caller owns it.
     */
    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction", "id", transactionId));
        return toDTO(transaction);
    }

    // ===== Create =====

    /**
     * Creates a new BUY or SELL transaction with full wallet + holdings integration.
     *
     * <p>For STOCK BUY:
     * <ol>
     *   <li>Calculate total cost (price × quantity + brokerage)</li>
     *   <li>Validate wallet balance</li>
     *   <li>Save transaction</li>
     *   <li>Debit wallet (cost + brokerage)</li>
     *   <li>Update holdings (weighted average)</li>
     *   <li>Record price history</li>
     * </ol>
     *
     * <p>For STOCK SELL:
     * <ol>
     *   <li>Validate holding quantity</li>
     *   <li>Save transaction</li>
     *   <li>Credit wallet (proceeds - brokerage)</li>
     *   <li>Update holdings</li>
     * </ol>
     */
    @Transactional
    public TransactionDTO createTransaction(Long userId, CreateTransactionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        AssetType assetType = request.getAssetType() != null ? request.getAssetType() : AssetType.STOCK;
        BigDecimal quantity = request.getQuantity();
        BigDecimal brokerage = request.getBrokerage() != null ? request.getBrokerage() : BigDecimal.ZERO;

        // For MF transactions, route to MutualFundService instead
        if (assetType == AssetType.MF) {
            throw new ValidationException("Use /api/mutual-funds/buy or /api/mutual-funds/sell for mutual fund transactions.");
        }

        // ===== Validate stock exists in master data =====
        String symbol = request.getStockSymbol().toUpperCase();
        Stock stock = stockRepository.findById(symbol)
                .orElseThrow(() -> new ValidationException(
                        String.format("Stock symbol '%s' not found. Please select a valid stock.", symbol)));

        // Auto-fill stock name from master data if not provided
        if (!StringUtils.hasText(request.getStockName())) {
            request.setStockName(stock.getName());
        }

        // ===== Resolve price: MARKET vs LIMIT =====
        boolean isMarketOrder = request.getOrderType() == null
                || request.getOrderType().isBlank()
                || "MARKET".equalsIgnoreCase(request.getOrderType());

        BigDecimal price;
        if (isMarketOrder) {
            // Market order: use current price from stocks table
            price = stock.getCurrentPrice();
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException(
                        String.format("Market price unavailable for %s. Please use a LIMIT order with a specific price.", symbol));
            }
            request.setPrice(price); // Set on request so buildTransaction picks it up
            log.info("Market order: resolved price for {} = {}", symbol, price);
        } else {
            // Limit order: price must be provided by the user
            price = request.getPrice();
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Price is required for LIMIT orders and must be positive.");
            }
        }

        if (request.getType() == TransactionType.BUY) {
            // === BUY Flow ===
            BigDecimal totalCost = price.multiply(quantity).add(brokerage);

            // Validate wallet balance
            walletService.validateBalance(userId, totalCost);

            // Save transaction
            Transaction transaction = buildTransaction(user, assetType, request);
            Transaction saved = transactionRepository.save(transaction);

            // Debit wallet: cost + brokerage
            walletService.debitForBuy(userId, price.multiply(quantity), saved.getId());
            walletService.debitBrokerage(userId, brokerage, saved.getId());

            // Update holding (weighted average)
            holdingService.updateHoldingAfterBuy(userId, assetType, 
                    request.getStockSymbol().toUpperCase(), quantity, price);

            // Record price history
            recordPriceHistory(assetType, request.getStockSymbol().toUpperCase(), price, "TRANSACTION");

            // Evict legacy holdings cache
            holdingsService.evictHoldingsCache(userId);

            log.info("Stock BUY: userId={}, symbol={}, qty={}, price={}, total={}",
                    userId, saved.getStockSymbol(), quantity, price, totalCost);
            return toDTO(saved);

        } else {
            // === SELL Flow ===

            // Validate holding quantity
            BigDecimal heldQty = holdingService.getHeldQuantity(userId, assetType, symbol);
            if (quantity.compareTo(heldQty) > 0) {
                throw new ValidationException(
                        String.format("Cannot sell %s shares of %s — only %s held.",
                                quantity.toPlainString(), symbol, heldQty.toPlainString()));
            }

            // Save transaction
            Transaction transaction = buildTransaction(user, assetType, request);
            Transaction saved = transactionRepository.save(transaction);

            // Credit wallet: proceeds
            BigDecimal proceeds = price.multiply(quantity);
            walletService.creditForSell(userId, proceeds, saved.getId());
            walletService.debitBrokerage(userId, brokerage, saved.getId());

            // Update holding
            holdingService.updateHoldingAfterSell(userId, assetType, symbol, quantity);

            // Evict legacy holdings cache
            holdingsService.evictHoldingsCache(userId);

            log.info("Stock SELL: userId={}, symbol={}, qty={}, price={}, proceeds={}",
                    userId, symbol, quantity, price, proceeds);
            return toDTO(saved);
        }
    }

    // ===== Update =====

    /**
     * Updates an existing transaction (partial update — only non-null fields applied).
     * Note: Update does NOT reverse/re-apply wallet or holding changes.
     * This is kept for backward compatibility with legacy transactions.
     */
    @Transactional
    public TransactionDTO updateTransaction(Long userId, Long transactionId,
                                            UpdateTransactionRequest request) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction", "id", transactionId));

        if (request.getStockSymbol() != null) {
            transaction.setStockSymbol(request.getStockSymbol().toUpperCase());
        }
        if (request.getStockName() != null) {
            transaction.setStockName(request.getStockName());
        }
        if (request.getType() != null) {
            transaction.setType(request.getType());
        }
        if (request.getQuantity() != null) {
            transaction.setQuantity(request.getQuantity());
        }
        if (request.getPrice() != null) {
            transaction.setPrice(request.getPrice());
        }
        if (request.getTransactionDate() != null) {
            transaction.setTransactionDate(request.getTransactionDate());
        }
        if (request.getBrokerage() != null) {
            transaction.setBrokerage(request.getBrokerage());
        }
        if (request.getNotes() != null) {
            transaction.setNotes(request.getNotes());
        }
        if (request.getAssetType() != null) {
            transaction.setAssetType(request.getAssetType());
        }

        Transaction updated = transactionRepository.save(transaction);
        holdingsService.evictHoldingsCache(userId);

        log.info("Transaction updated: id={}, userId={}", transactionId, userId);
        return toDTO(updated);
    }

    // ===== Delete =====

    /**
     * Deletes a transaction, verifying ownership first.
     * Note: Delete does NOT reverse wallet or holding changes.
     * This is kept for backward compatibility with legacy transactions.
     */
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction", "id", transactionId));

        transactionRepository.delete(transaction);
        holdingsService.evictHoldingsCache(userId);

        log.info("Transaction deleted: id={}, userId={}", transactionId, userId);
    }

    // ===== Helpers =====

    private Transaction buildTransaction(User user, AssetType assetType, CreateTransactionRequest request) {
        return Transaction.builder()
                .user(user)
                .assetType(assetType)
                .stockSymbol(request.getStockSymbol().toUpperCase())
                .stockName(request.getStockName())
                .type(request.getType())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .transactionDate(request.getTransactionDate())
                .brokerage(request.getBrokerage() != null ? request.getBrokerage() : BigDecimal.ZERO)
                .notes(request.getNotes())
                .build();
    }

    private void recordPriceHistory(AssetType assetType, String symbol, BigDecimal price, String source) {
        try {
            AssetPriceHistory history = AssetPriceHistory.builder()
                    .assetType(assetType)
                    .symbol(symbol)
                    .price(price)
                    .recordedAt(LocalDateTime.now())
                    .source(source)
                    .build();
            priceHistoryRepository.save(history);
        } catch (Exception e) {
            log.warn("Failed to record price history: {}", e.getMessage());
        }
    }

    private TransactionDTO toDTO(Transaction t) {
        BigDecimal total = t.getPrice()
                .multiply(t.getQuantity())
                .add(t.getBrokerage() != null ? t.getBrokerage() : BigDecimal.ZERO);

        return TransactionDTO.builder()
                .id(t.getId())
                .assetType(t.getAssetType() != null ? t.getAssetType().name() : "STOCK")
                .stockSymbol(t.getStockSymbol())
                .stockName(t.getStockName())
                .type(t.getType())
                .quantity(t.getQuantity())
                .price(t.getPrice())
                .transactionDate(t.getTransactionDate())
                .brokerage(t.getBrokerage())
                .totalAmount(total.setScale(2, RoundingMode.HALF_UP))
                .notes(t.getNotes())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
