package com.pdv.staff;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** API de empleados y horas trabajadas. */
@RestController
@RequestMapping("/api")
public class StaffController {
    private final StaffService service;
    public StaffController(StaffService s) { this.service = s; }

    public record ShiftIn(Long employeeId, LocalDate day, LocalTime clockIn, LocalTime clockOut, String notes) {}
    public record EmployeeOut(Employee employee, double hours) {}

    @GetMapping("/employees") public List<EmployeeOut> employees() { return service.employees().stream().map(e -> new EmployeeOut(e, service.hoursOf(e.getId()))).toList(); }
    @PostMapping("/employees") public Employee create(@RequestBody Employee e) { return service.save(null, e); }
    @PutMapping("/employees/{id}") public Employee update(@PathVariable Long id, @RequestBody Employee e) { return service.save(id, e); }
    @GetMapping("/shifts") public List<Shift> shifts() { return service.shifts(); }
    @PostMapping("/shifts") public Shift clock(@RequestBody ShiftIn in) { return service.clock(in.employeeId(), in.day(), in.clockIn(), in.clockOut(), in.notes()); }
}
