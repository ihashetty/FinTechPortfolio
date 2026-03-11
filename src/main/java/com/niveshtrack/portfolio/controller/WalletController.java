package com.niveshtrack.portfolio.controller;

import com.niveshtrack.portfolio.dto.request.WalletDepositRequest;
import com.niveshtrack.portfolio.dto.request.WalletWithdrawRequest;
import com.niveshtrack.portfolio.dto.response.LedgerEntryDTO;
import com.niveshtrack.portfolio.dto.response.WalletBalanceDTO;
import com.niveshtrack.portfolio.security.UserDetailsServiceImpl;
import com.niveshtrack.portfolio.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Wallet management endpoints — deposit, withdraw, balance, ledger.
 */
@RestController
@RequestMapping("/api/wallet")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Wallet", description = "Virtual wallet / cash balance management")
public class WalletController extends BaseController {

    private final WalletService walletService;

    public WalletController(UserDetailsServiceImpl userDetailsService,
                            WalletService walletService) {
        super(userDetailsService);
        this.walletService = walletService;
    }

    @GetMapping("/balance")
    @Operation(summary = "Get wallet balance",
               description = "Returns the current cash balance for the authenticated user.")
    public ResponseEntity<BigDecimal> getBalance() {
        return ResponseEntity.ok(walletService.getBalance(getCurrentUserId()));
    }

    @GetMapping
    @Operation(summary = "Get wallet details",
               description = "Returns balance along with recent ledger entries.")
    public ResponseEntity<WalletBalanceDTO> getWalletDetails() {
        return ResponseEntity.ok(walletService.getWalletDetails(getCurrentUserId()));
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit funds",
               description = "Adds funds to the virtual wallet.")
    public ResponseEntity<BigDecimal> deposit(@Valid @RequestBody WalletDepositRequest request) {
        walletService.deposit(getCurrentUserId(), request.getAmount());
        return ResponseEntity.ok(walletService.getBalance(getCurrentUserId()));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds",
               description = "Withdraws funds from the virtual wallet. Fails if insufficient balance.")
    public ResponseEntity<BigDecimal> withdraw(@Valid @RequestBody WalletWithdrawRequest request) {
        walletService.withdraw(getCurrentUserId(), request.getAmount());
        return ResponseEntity.ok(walletService.getBalance(getCurrentUserId()));
    }

    @GetMapping("/ledger")
    @Operation(summary = "Get ledger history",
               description = "Returns the full transaction ledger for the authenticated user.")
    public ResponseEntity<List<LedgerEntryDTO>> getLedger() {
        return ResponseEntity.ok(walletService.getWalletDetails(getCurrentUserId()).getRecentEntries());
    }
}
