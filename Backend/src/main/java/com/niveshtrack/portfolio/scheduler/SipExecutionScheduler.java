package com.niveshtrack.portfolio.scheduler;

import com.niveshtrack.portfolio.entity.SipInstruction;
import com.niveshtrack.portfolio.repository.SipInstructionRepository;
import com.niveshtrack.portfolio.service.MutualFundService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Executes due SIP installments every 30 seconds (demo mode).
 *
 * <p>Finds all active SIP instructions whose {@code nextExecutionDate ≤ today}
 * and invokes the MutualFundService to execute each one.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SipExecutionScheduler {

    private final SipInstructionRepository sipInstructionRepository;
    private final MutualFundService mutualFundService;

    /**
     * On startup, reset all active SIPs' nextExecutionDate to today so they execute immediately (demo mode).
     */
    @PostConstruct
    public void resetSipDatesToToday() {
        List<SipInstruction> allActive = sipInstructionRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive()))
                .filter(s -> s.getNextExecutionDate() != null && s.getNextExecutionDate().isAfter(LocalDate.now()))
                .toList();
        for (SipInstruction sip : allActive) {
            sip.setNextExecutionDate(LocalDate.now());
            sipInstructionRepository.save(sip);
        }
        if (!allActive.isEmpty()) {
            log.info("Reset {} active SIPs' nextExecutionDate to today for demo mode", allActive.size());
        }
    }

    /**
     * SIP execution every 30 seconds (demo/testing).
     */
    @Scheduled(fixedRate = 30_000)
    public void executeDueSips() {
        LocalDate today = LocalDate.now();
        List<SipInstruction> dueSips =
                sipInstructionRepository.findByActiveTrueAndNextExecutionDateLessThanEqual(today);

        if (dueSips.isEmpty()) {
            log.debug("No SIP installments due today ({})", today);
            return;
        }

        log.info("Executing {} SIP installments due on or before {}", dueSips.size(), today);

        int success = 0;
        int failed = 0;

        for (SipInstruction sip : dueSips) {
            try {
                boolean executed = mutualFundService.executeSIP(sip);
                if (executed) {
                    success++;
                    log.debug("SIP #{} executed: {} ₹{}", sip.getId(), sip.getSymbol(), sip.getAmount());
                } else {
                    failed++;
                    log.debug("SIP #{} skipped: {} ₹{}", sip.getId(), sip.getSymbol(), sip.getAmount());
                }
            } catch (Exception e) {
                failed++;
                log.error("SIP #{} failed for {} : {}", sip.getId(), sip.getSymbol(), e.getMessage());
            }
        }

        log.info("SIP execution complete: {} success, {} failed", success, failed);
    }
}
