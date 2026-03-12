package com.niveshtrack.portfolio.repository;

import com.niveshtrack.portfolio.entity.AssetType;
import com.niveshtrack.portfolio.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for persisted holdings.
 */
@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    List<Holding> findByUserId(Long userId);

    Optional<Holding> findByUserIdAndAssetTypeAndSymbol(Long userId, AssetType assetType, String symbol);

    List<Holding> findByUserIdAndAssetType(Long userId, AssetType assetType);

    void deleteByUserIdAndAssetTypeAndSymbol(Long userId, AssetType assetType, String symbol);
}
