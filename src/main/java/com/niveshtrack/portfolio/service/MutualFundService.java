package com.niveshtrack.portfolio.service;

import com.niveshtrack.portfolio.dto.request.BuyMutualFundRequest;
import com.niveshtrack.portfolio.dto.request.CreateSipRequest;
import com.niveshtrack.portfolio.dto.request.SellMutualFundRequest;
import com.niveshtrack.portfolio.dto.response.HoldingDTO;
import com.niveshtrack.portfolio.dto.response.MutualFundDTO;
import com.niveshtrack.portfolio.dto.response.SipInstructionDTO;
import com.niveshtrack.portfolio.dto.response.TransactionDTO;
import com.niveshtrack.portfolio.entity.*;
import com.niveshtrack.portfolio.exception.ResourceNotFoundException;
import com.niveshtrack.portfolio.exception.ValidationException;
import com.niveshtrack.portfolio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for mutual fund operations: buy, sell, SIP management.
 *
 * <p>Key differences from stock transactions:
 * <ul>
 *   <li>Quantity represents units (fractional)</li>
 *   <li>Price represents NAV</li>
 *   <li>No brokerage for MF transactions</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MutualFundService {

    private final MutualFundRepository mutualFundRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final HoldingService holdingService;
    private final AssetPriceHistoryRepository priceHistoryRepository;
    private final SipInstructionRepository sipInstructionRepository;

    // ===== Fund Catalog =====

    /**
     * Returns all available mutual fund schemes.
     */
    @Transactional(readOnly = true)
    public List<MutualFundDTO> getAllFunds() {
        return mutualFundRepository.findAll().stream()
                .map(this::toMutualFundDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns mutual fund holdings for a user.
     */
    @Transactional(readOnly = true)
    public List<HoldingDTO> getMutualFundHoldings(Long userId) {
        return holdingService.getUserHoldingsByAssetType(userId, AssetType.MF);
    }

    // ===== Buy =====

    /**
     * One-time mutual fund purchase.
     *
     * <p>Flow:
     * <ol>
     *   <li>Validate fund exists and get current NAV</li>
     *   <li>Calculate units = amount / NAV</li>
     *   <li>Validate wallet balance</li>
     *   <li>Create transaction</li>
     *   <li>Debit wallet</li>
     *   <li>Update holding</li>
     * </ol>
     */
    @Transactional
    public TransactionDTO buyOneTime(Long userId, BuyMutualFundRequest request) {
        MutualFund fund = mutualFundRepository.findBySymbol(request.getSymbol().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mutual Fund", "symbol", request.getSymbol()));

        BigDecimal nav = fund.getNav();
        if (nav == null || nav.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("NAV not available for fund: " + fund.getSymbol());
        }

        BigDecimal amount = request.getAmount();
        BigDecimal units = amount.divide(nav, 4, RoundingMode.HALF_UP);

        // Validate wallet balance
        walletService.validateBalance(userId, amount);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Create transaction (no brokerage for MF)
        Transaction transaction = Transaction.builder()
                .user(user)
                .assetType(AssetType.MF)
                .stockSymbol(fund.getSymbol())
                .stockName(fund.getName())
                .type(TransactionType.BUY)
                .quantity(units)
                .price(nav)
                .transactionDate(LocalDate.now())
                .brokerage(BigDecimal.ZERO)
                .notes("Mutual fund purchase")
                .build();

        Transaction saved = transactionRepository.save(transaction);

        // Debit wallet
        walletService.debitForBuy(userId, amount, saved.getId());

        // Update holding
        holdingService.updateHoldingAfterBuy(userId, AssetType.MF, fund.getSymbol(), units, nav);

        log.info("MF buy: userId={}, fund={}, amount={}, units={}, nav={}",
                userId, fund.getSymbol(), amount, units, nav);

        return toTransactionDTO(saved);
    }

    // ===== Sell =====

    /**
     * Sells mutual fund units.
     */
    @Transactional
    public TransactionDTO sellUnits(Long userId, SellMutualFundRequest request) {
        MutualFund fund = mutualFundRepository.findBySymbol(request.getSymbol().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mutual Fund", "symbol", request.getSymbol()));

        BigDecimal nav = fund.getNav();
        BigDecimal units = request.getUnits();

        // Validate holding
        BigDecimal heldUnits = holdingService.getHeldQuantity(userId, AssetType.MF, fund.getSymbol());
        if (units.compareTo(heldUnits) > 0) {
            throw new ValidationException(
                    String.format("Cannot sell %s units of %s — only %s units held.",
                            units.toPlainString(), fund.getSymbol(), heldUnits.toPlainString()));
        }

        BigDecimal proceeds = units.multiply(nav).setScale(2, RoundingMode.HALF_UP);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Create transaction
        Transaction transaction = Transaction.builder()
                .user(user)
                .assetType(AssetType.MF)
                .stockSymbol(fund.getSymbol())
                .stockName(fund.getName())
                .type(TransactionType.SELL)
                .quantity(units)
                .price(nav)
                .transactionDate(LocalDate.now())
                .brokerage(BigDecimal.ZERO)
                .notes("Mutual fund redemption")
                .build();

        Transaction saved = transactionRepository.save(transaction);

        // Credit wallet
        walletService.creditForSell(userId, proceeds, saved.getId());

        // Update holding
        holdingService.updateHoldingAfterSell(userId, AssetType.MF, fund.getSymbol(), units);

        log.info("MF sell: userId={}, fund={}, units={}, nav={}, proceeds={}",
                userId, fund.getSymbol(), units, nav, proceeds);

        return toTransactionDTO(saved);
    }

    // ===== SIP Management =====

    /**
     * Creates a new SIP instruction.
     */
    @Transactional
    public SipInstructionDTO createSIP(Long userId, CreateSipRequest request) {
        MutualFund fund = mutualFundRepository.findBySymbol(request.getSymbol().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mutual Fund", "symbol", request.getSymbol()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String frequency = (request.getFrequency() != null && !request.getFrequency().isBlank())
                ? request.getFrequency().toUpperCase()
                : "MONTHLY";

        // Next execution = 1st of next month
        LocalDate nextExecution = LocalDate.now().plusMonths(1).withDayOfMonth(1);

        SipInstruction sip = SipInstruction.builder()
                .user(user)
                .symbol(fund.getSymbol())
                .amount(request.getAmount())
                .frequency(frequency)
                .nextExecutionDate(nextExecution)
                .active(true)
                .build();

        SipInstruction saved = sipInstructionRepository.save(sip);

        log.info("SIP created: userId={}, fund={}, amount={}, frequency={}, nextExec={}",
                userId, fund.getSymbol(), request.getAmount(), frequency, nextExecution);

        return toSipDTO(saved, fund.getName());
    }

    /**
     * Cancels (deactivates) a SIP instruction.
     */
    @Transactional
    public void cancelSIP(Long userId, Long sipId) {
        SipInstruction sip = sipInstructionRepository.findByIdAndUserId(sipId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("SIP", "id", sipId));

        sip.setActive(false);
        sipInstructionRepository.save(sip);
        log.info("SIP cancelled: sipId={}, userId={}", sipId, userId);
    }

    /**
     * Returns all SIP instructions for a user.
     */
    @Transactional(readOnly = true)
    public List<SipInstructionDTO> getUserSIPs(Long userId) {
        return sipInstructionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(sip -> {
                    String fundName = mutualFundRepository.findBySymbol(sip.getSymbol())
                            .map(MutualFund::getName)
                            .orElse(sip.getSymbol());
                    return toSipDTO(sip, fundName);
                })
                .collect(Collectors.toList());
    }

    /**
     * Executes a single SIP instruction (called by scheduler).
     *
     * @return true if executed successfully, false if skipped
     */
    @Transactional
    public boolean executeSIP(SipInstruction sip) {
        try {
            MutualFund fund = mutualFundRepository.findBySymbol(sip.getSymbol()).orElse(null);
            if (fund == null || fund.getNav() == null) {
                log.warn("SIP execution skipped — fund not found: {}", sip.getSymbol());
                return false;
            }

            BigDecimal nav = fund.getNav();
            BigDecimal amount = sip.getAmount();
            BigDecimal units = amount.divide(nav, 4, RoundingMode.HALF_UP);

            // Check wallet balance
            BigDecimal balance = walletService.getBalance(sip.getUser().getId());
            if (balance.compareTo(amount) < 0) {
                log.warn("SIP execution skipped — insufficient balance: userId={}, fund={}, needed={}, available={}",
                        sip.getUser().getId(), sip.getSymbol(), amount, balance);
                return false;
            }

            // Create transaction
            Transaction transaction = Transaction.builder()
                    .user(sip.getUser())
                    .assetType(AssetType.MF)
                    .stockSymbol(fund.getSymbol())
                    .stockName(fund.getName())
                    .type(TransactionType.BUY)
                    .quantity(units)
                    .price(nav)
                    .transactionDate(LocalDate.now())
                    .brokerage(BigDecimal.ZERO)
                    .notes("SIP auto-execution")
                    .build();

            Transaction saved = transactionRepository.save(transaction);

            // Debit wallet
            walletService.debitForSip(sip.getUser().getId(), amount, saved.getId());

            // Update holding
            holdingService.updateHoldingAfterBuy(
                    sip.getUser().getId(), AssetType.MF, fund.getSymbol(), units, nav);

            // Advance next execution date
            sip.setNextExecutionDate(computeNextExecutionDate(sip));
            sipInstructionRepository.save(sip);

            log.info("SIP executed: sipId={}, userId={}, fund={}, amount={}, units={}",
                    sip.getId(), sip.getUser().getId(), sip.getSymbol(), amount, units);
            return true;

        } catch (Exception e) {
            log.error("SIP execution failed: sipId={}, error={}", sip.getId(), e.getMessage(), e);
            return false;
        }
    }

    // ===== Helpers =====

    private LocalDate computeNextExecutionDate(SipInstruction sip) {
        // For MONTHLY: advance by 1 month
        return sip.getNextExecutionDate().plusMonths(1);
    }

    private MutualFundDTO toMutualFundDTO(MutualFund fund) {
        return MutualFundDTO.builder()
                .id(fund.getId())
                .symbol(fund.getSymbol())
                .name(fund.getName())
                .category(fund.getCategory())
                .nav(fund.getNav())
                .lastUpdated(fund.getLastUpdated())
                .build();
    }

    private TransactionDTO toTransactionDTO(Transaction t) {
        BigDecimal total = t.getPrice()
                .multiply(t.getQuantity())
                .add(t.getBrokerage() != null ? t.getBrokerage() : BigDecimal.ZERO);

        return TransactionDTO.builder()
                .id(t.getId())
                .assetType(t.getAssetType().name())
                .stockSymbol(t.getStockSymbol())
                .stockName(t.getStockName())
                .type(t.getType())
                .quantity(t.getQuantity())
                .price(t.getPrice())
                .transactionDate(t.getTransactionDate())
                .brokerage(t.getBrokerage())
                .totalAmount(total)
                .notes(t.getNotes())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private SipInstructionDTO toSipDTO(SipInstruction sip, String fundName) {
        return SipInstructionDTO.builder()
                .id(sip.getId())
                .symbol(sip.getSymbol())
                .fundName(fundName)
                .amount(sip.getAmount())
                .frequency(sip.getFrequency())
                .nextExecutionDate(sip.getNextExecutionDate())
                .active(sip.getActive())
                .createdAt(sip.getCreatedAt())
                .build();
    }
}
