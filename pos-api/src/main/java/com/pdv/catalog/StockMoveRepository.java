package com.pdv.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMoveRepository extends JpaRepository<StockMove, Long> {
    List<StockMove> findAllByOrderByOccurredAtDescIdDesc();
    List<StockMove> findByProductIdOrderByOccurredAtDescIdDesc(Long productId);
}
