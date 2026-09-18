package com.pdv.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PriceListRepository extends JpaRepository<PriceList, Long> {
    Optional<PriceList> findFirstByMainTrue();
}
