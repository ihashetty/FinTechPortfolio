package com.niveshtrack.portfolio.service;

import com.niveshtrack.portfolio.dto.response.LedgerEntryDTO;
import com.niveshtrack.portfolio.dto.response.WalletBalanceDTO;
import com.niveshtrack.portfolio.entity.AccountLedger;
import com.niveshtrack.portfolio.entity.LedgerType;
import com.niveshtrack.portfolio.entity.User;
import com.niveshtrack.portfolio.exception.ResourceNotFoundException;
import com.niveshtrack.portfolio.exception.ValidationException;
import com.niveshtrack.portfolio.repository.AccountLedgerRepository;
import com.niveshtrack.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wallet service managing user cash balance via the account_ledger table.
 *
 * <p>Balance is always derived as SUM(amount) from ledger entries.
 * Positive amounts = credits, negative amounts = debits.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final AccountLedgerRepository ledgerRepository;
    private final UserRepository userRepository;

    // ===== Public API =====

    /**
     * Returns the current wallet balance for a user.
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId) {
        return ledgerRepository.getBalance(userId);
    }

    /**
     * Returns wallet balance with recent ledger entries.
     */
    @Transactional(readOnly = true)
    public WalletBalanceDTO getWalletDetails(Long userId) {
        BigDecimal balance = ledgerRepository.getBalance(userId);
        List<AccountLedger> entries = ledgerRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<LedgerEntryDTO> entryDTOs = entries.stream()
                .limit(50) // last 50 entries
                .map(this::toLedgerDTO)
                .collect(Collectors.toList());

        return WalletBalanceDTO.builder()
                .balance(balance)
                .recentEntries(entryDTOs)
                .build();
    }

    /**
     * Deposits cash into the wallet.
     *
     * @return the new balance after deposit
     */
    @Transactional
    public BigDecimal deposit(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Deposit amount must be positive.");
        }

        User user = findUser(userId);

        AccountLedger entry = AccountLedger.builder()
                .user(user)
                .type(LedgerType.DEPOSIT)
                .amount(amount)
                .referenceId(null)
                .build();

        ledgerRepository.save(entry);
        BigDecimal newBalance = ledgerRepository.getBalance(userId);
        log.info("Wallet deposit: userId={}, amount={}, newBalance={}", userId, amount, newBalance);
        return newBalance;
    }

    /**
     * Withdraws cash from the wallet.
     *
     * @return the new balance after withdrawal
     * @throws ValidationException if insufficient balance
     */
    @Transactional
    public BigDecimal withdraw(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Withdrawal amount must be positive.");
        }

        BigDecimal currentBalance = ledgerRepository.getBalance(userId);
        if (currentBalance.compareTo(amount) < 0) {
            throw new ValidationException(
                    String.format("Insufficient balance. Available: ₹%s, Requested: ₹%s",
                            currentBalance.toPlainString(), amount.toPlainString()));
        }

        User user = findUser(userId);

        AccountLedger entry = AccountLedger.builder()
                .user(user)
                .type(LedgerType.WITHDRAW)
                .amount(amount.negate()) // negative for debit
                .referenceId(null)
                .build();

        ledgerRepository.save(entry);
        BigDecimal newBalance = ledgerRepository.getBalance(userId);
        log.info("Wallet withdrawal: userId={}, amount={}, newBalance={}", userId, amount, newBalance);
        return newBalance;
    }

    // ===== Internal Methods (called by TransactionService / MutualFundService) =====

    /**
     * Debits the wallet for a BUY transaction.
     */
    @Transactional
    public void debitForBuy(Long userId, BigDecimal amount, Long transactionId) {
        User user = findUser(userId);
        AccountLedger entry = AccountLedger.builder()
                .user(user)
                .type(LedgerType.BUY)
                .amount(amount.negate()) // negative for debit
                .referenceId(transactionId)
                .build();
        ledgerRepository.save(entry);
        log.debug("Wallet debit (BUY): userId={}, amount={}, txnId={}", userId, amount, transactionId);
    }

    /**
     * Credits the wallet for a SELL transaction.
     */
    @Transactional
    public void creditForSell(Long userId, BigDecimal amount, Long transactionId) {
        User user = findUser(userId);
        AccountLedger entry = AccountLedger.builder()
                .user(user)
                .type(LedgerType.SELL)
                .amount(amount) // positive for credit
                .referenceId(transactionId)
                .build();
        ledgerRepository.save(entry);
        log.debug("Wallet credit (SELL): userId={}, amount={}, txnId={}", userId, amount, transactionId);
    }

    /**
     * Debits brokerage fee from the wallet.
     */
    @Transactional
    public void debitBrokerage(Long userId, BigDecimal brokerage, Long transactionId) {
        if (brokerage == null || brokerage.compareTo(BigDecimal.ZERO) <= 0) {
            return; // No brokerage to debit
        }
        User user = findUser(userId);
        AccountLedger entry = AccountLedger.builder()
                .user(user)
                .type(LedgerType.BROKERAGE)
                .amount(brokerage.negate()) // negative for debit
                .referenceId(transactionId)
                .build();
        ledgerRepository.save(entry);
        log.debug("Wallet debit (BROKERAGE): userId={}, brokerage={}, txnId={}", userId, brokerage, transactionId);
    }

    /**
     * Debits the wallet for a SIP execution.
     */
    @Transactional
    public void debitForSip(Long userId, BigDecimal amount, Long transactionId) {
        User user = findUser(userId);
        AccountLedger entry = AccountLedger.builder()
                .user(user)
                .type(LedgerType.SIP)
                .amount(amount.negate()) // negative for debit
                .referenceId(transactionId)
                .build();
        ledgerRepository.save(entry);
        log.debug("Wallet debit (SIP): userId={}, amount={}, txnId={}", userId, amount, transactionId);
    }

    /**
     * Validates that the user has sufficient balance for a purchase.
     *
     * @throws ValidationException if balance is insufficient
     */
    @Transactional(readOnly = true)
    public void validateBalance(Long userId, BigDecimal requiredAmount) {
        BigDecimal balance = ledgerRepository.getBalance(userId);
        if (balance.compareTo(requiredAmount) < 0) {
            throw new ValidationException(
                    String.format("Insufficient wallet balance. Available: ₹%s, Required: ₹%s",
                            balance.toPlainString(), requiredAmount.toPlainString()));
        }
    }

    // ===== Helpers =====

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private LedgerEntryDTO toLedgerDTO(AccountLedger entry) {
        return LedgerEntryDTO.builder()
                .id(entry.getId())
                .type(entry.getType().name())
                .amount(entry.getAmount())
                .referenceId(entry.getReferenceId())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
