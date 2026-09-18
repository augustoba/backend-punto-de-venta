package com.pdv.warehouses;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockLevelRepository extends JpaRepository<StockLevel, Long> {
    List<StockLevel> findByProductId(Long productId);
    Optional<StockLevel> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
}
