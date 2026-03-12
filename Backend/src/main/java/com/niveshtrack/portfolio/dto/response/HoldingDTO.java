package com.niveshtrack.portfolio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents one stock holding in the user's current portfolio.
 *
 * <p>Calculated in real-time from all transactions using weighted-average cost method.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingDTO {

    private String assetType;
    private String symbol;
    private String name;
    private String sector;

    /** Net quantity currently held (BUY qty - SELL qty). Decimal for MF units. */
    private BigDecimal quantity;

    /** Weighted average buy price across all BUY transactions */
    private BigDecimal avgBuyPrice;

    /** Total amount invested: avgBuyPrice * quantity */
    private BigDecimal investedAmount;

    /** Current market price (from stocks table) */
    private BigDecimal currentPrice;

    /** Current total value: currentPrice * quantity */
    private BigDecimal totalValue;

    /** P&L: totalValue - investedAmount */
    private BigDecimal gainLoss;

    /** Return %: (gainLoss / investedAmount) * 100 */
    private BigDecimal returnPercent;

    /** Placeholder for real-time day change (requires intraday price feed) */
    private BigDecimal dayChange;

    /** Weight of this holding in the total portfolio (%) */
    private BigDecimal weightPercent;
}
