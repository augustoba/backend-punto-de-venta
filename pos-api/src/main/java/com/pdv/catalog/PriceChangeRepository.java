package com.pdv.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceChangeRepository extends JpaRepository<PriceChange, Long> {
    List<PriceChange> findAllByOrderByOccurredAtDescIdDesc();
}
