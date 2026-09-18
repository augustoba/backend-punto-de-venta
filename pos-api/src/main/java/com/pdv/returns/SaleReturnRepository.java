package com.pdv.returns;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleReturnRepository extends JpaRepository<SaleReturn, Long> {
    List<SaleReturn> findBySaleId(Long saleId);
    boolean existsBySaleId(Long saleId);
    List<SaleReturn> findAllByOrderByIdDesc();
}
