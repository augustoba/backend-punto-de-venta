package com.pdv.finance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChequeRepository extends JpaRepository<Cheque, Long> {
    List<Cheque> findByKindOrderByDueAsc(Cheque.Kind kind);
}
