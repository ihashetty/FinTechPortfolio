package com.niveshtrack.portfolio.repository;

import com.niveshtrack.portfolio.entity.MutualFund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for mutual fund master data.
 */
@Repository
public interface MutualFundRepository extends JpaRepository<MutualFund, Long> {

    Optional<MutualFund> findBySymbol(String symbol);

    List<MutualFund> findByCategory(String category);
}
