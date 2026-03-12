package com.niveshtrack.portfolio.repository;

import com.niveshtrack.portfolio.entity.SipInstruction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SIP instructions.
 */
@Repository
public interface SipInstructionRepository extends JpaRepository<SipInstruction, Long> {

    List<SipInstruction> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Finds all active SIPs that are due for execution (next_execution_date <= today). */
    List<SipInstruction> findByActiveTrueAndNextExecutionDateLessThanEqual(LocalDate date);

    Optional<SipInstruction> findByIdAndUserId(Long id, Long userId);
}
