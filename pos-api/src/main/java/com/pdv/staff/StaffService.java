package com.pdv.staff;

import com.pdv.common.BusinessException;
import com.pdv.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Empleados y horas trabajadas. */
@Service
@Transactional
public class StaffService {
    private final EmployeeRepository employees;
    private final ShiftRepository shifts;

    public StaffService(EmployeeRepository e, ShiftRepository s) { this.employees = e; this.shifts = s; }

    public List<Employee> employees() { return employees.findAll(); }
    public Employee employee(Long id) { return employees.findById(id).orElseThrow(() -> new NotFoundException("Empleado", id)); }

    public Employee save(Long id, Employee in) {
        if (in.getName() == null || in.getName().isBlank()) throw new BusinessException("El empleado necesita un nombre");
        Employee e = id == null ? new Employee() : employee(id);
        e.setName(in.getName().trim()); e.setLastName(in.getLastName()); e.setEmail(in.getEmail()); e.setRole(in.getRole()); e.setHired(in.getHired());
        return employees.save(e);
    }

    public Shift clock(Long employeeId, LocalDate day, LocalTime in, LocalTime out, String notes) {
        employee(employeeId);
        if (in == null || out == null) throw new BusinessException("Cargá la entrada y la salida");
        if (!out.isAfter(in)) throw new BusinessException("La salida debe ser posterior a la entrada");
        return shifts.save(new Shift(employeeId, day == null ? LocalDate.now() : day, in, out, notes));
    }

    public List<Shift> shifts() { return shifts.findAllByOrderByDayDescClockInDesc(); }
    public double hoursOf(Long employeeId) { return shifts.findByEmployeeId(employeeId).stream().mapToDouble(Shift::getHours).sum(); }
}
