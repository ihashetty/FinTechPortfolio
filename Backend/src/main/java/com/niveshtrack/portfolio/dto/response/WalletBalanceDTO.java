package com.niveshtrack.portfolio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for wallet balance and recent ledger history.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalanceDTO {

    private BigDecimal balance;
    private List<LedgerEntryDTO> recentEntries;
}
