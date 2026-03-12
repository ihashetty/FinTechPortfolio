package com.niveshtrack.portfolio.controller;

import com.niveshtrack.portfolio.dto.request.CreateTransactionRequest;
import com.niveshtrack.portfolio.dto.request.UpdateTransactionRequest;
import com.niveshtrack.portfolio.dto.response.TransactionDTO;
import com.niveshtrack.portfolio.security.UserDetailsServiceImpl;
import com.niveshtrack.portfolio.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD endpoints for stock transactions.
 * All endpoints require a valid JWT Bearer token.
 */
@RestController
@RequestMapping("/api/transactions")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Transactions", description = "BUY/SELL transaction management APIs")
public class TransactionController extends BaseController {

    private final TransactionService transactionService;

    public TransactionController(UserDetailsServiceImpl userDetailsService,
                                 TransactionService transactionService) {
        super(userDetailsService);
        this.transactionService = transactionService;
    }

    @GetMapping
    @Operation(summary = "Get all transactions",
               description = "Returns all transactions for the authenticated user. Optionally filter by symbol.")
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(
            @RequestParam(required = false) String symbol) {
        return ResponseEntity.ok(transactionService.getAllTransactions(getCurrentUserId(), symbol));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(getCurrentUserId(), id));
    }

    @PostMapping
    @Operation(summary = "Create a new transaction",
               description = "Records a BUY or SELL transaction. SELL validation ensures you hold sufficient quantity.")
    public ResponseEntity<TransactionDTO> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        TransactionDTO created = transactionService.createTransaction(getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction",
               description = "Partially updates an existing transaction. Only non-null fields are applied.")
    public ResponseEntity<TransactionDTO> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        return ResponseEntity.ok(transactionService.updateTransaction(getCurrentUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(getCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
