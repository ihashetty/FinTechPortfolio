package com.niveshtrack.portfolio.repository;

import com.niveshtrack.portfolio.entity.AssetPriceHistory;
import com.niveshtrack.portfolio.entity.AssetType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for asset price history records.
 */
@Repository
public interface AssetPriceHistoryRepository extends JpaRepository<AssetPriceHistory, Long> {

    List<AssetPriceHistory> findBySymbolAndRecordedAtBetweenOrderByRecordedAtAsc(
            String symbol, LocalDateTime from, LocalDateTime to);

    List<AssetPriceHistory> findByAssetTypeAndSymbolOrderByRecordedAtDesc(
            AssetType assetType, String symbol, Pageable pageable);

    List<AssetPriceHistory> findBySymbolOrderByRecordedAtDesc(String symbol, Pageable pageable);
}
