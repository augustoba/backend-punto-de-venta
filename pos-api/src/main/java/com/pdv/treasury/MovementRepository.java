package com.pdv.treasury;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface MovementRepository extends JpaRepository<Movement, Long> {
    @Query("select coalesce(sum(m.amount), 0) from Movement m where m.accountId = :id")
    BigDecimal balance(@Param("id") Long accountId);

    List<Movement> findByAccountIdOrderByOccurredAtDescIdDesc(Long accountId);
    List<Movement> findAllByOrderByOccurredAtDescIdDesc();
}
