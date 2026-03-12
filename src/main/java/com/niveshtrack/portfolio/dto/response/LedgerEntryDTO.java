package com.niveshtrack.portfolio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for a single ledger entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntryDTO {

    private Long id;
    private String type;
    private BigDecimal amount;
    private Long referenceId;
    private LocalDateTime createdAt;
}
