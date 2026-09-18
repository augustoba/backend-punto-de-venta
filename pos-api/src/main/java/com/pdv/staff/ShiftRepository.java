package com.pdv.staff;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findAllByOrderByDayDescClockInDesc();
    List<Shift> findByEmployeeId(Long employeeId);
}
