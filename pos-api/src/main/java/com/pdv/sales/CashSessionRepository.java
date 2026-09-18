package com.pdv.sales;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CashSessionRepository extends JpaRepository<CashSession, Long> {
    Optional<CashSession> findFirstByClosedAtIsNull();
    Optional<CashSession> findFirstByAccountIdAndClosedAtIsNotNullOrderByClosedAtDescIdDesc(Long accountId);
    List<CashSession> findAllByOrderByOpenedAtDescIdDesc();
}
