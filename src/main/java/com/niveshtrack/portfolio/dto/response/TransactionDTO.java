package com.niveshtrack.portfolio.dto.response;

import com.niveshtrack.portfolio.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for a single transaction record.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    private Long id;
    private String assetType;
    private String stockSymbol;
    private String stockName;
    private TransactionType type;
    private BigDecimal quantity;
    private BigDecimal price;
    private LocalDate transactionDate;
    private BigDecimal brokerage;
    private BigDecimal totalAmount;   // price * quantity + brokerage
    private String notes;
    private LocalDateTime createdAt;
}
