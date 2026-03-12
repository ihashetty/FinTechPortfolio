package com.niveshtrack.portfolio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for a SIP instruction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SipInstructionDTO {

    private Long id;
    private String symbol;
    private String fundName;
    private BigDecimal amount;
    private String frequency;
    private LocalDate nextExecutionDate;
    private Boolean active;
    private LocalDateTime createdAt;
}
