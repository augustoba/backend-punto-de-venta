package com.pdv.staff;

import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/** Fichaje: entrada y salida de un empleado en un día. La salida debe ser posterior a la entrada. */
@Entity
@Table(name = "pos_shift")
public class Shift {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long employeeId;
    @Column(name = "work_day", nullable = false) private LocalDate day;
    @Column(nullable = false) private LocalTime clockIn;
    @Column(nullable = false) private LocalTime clockOut;
    @Column(length = 300) private String notes = "";

    protected Shift() {}
    public Shift(Long employeeId, LocalDate day, LocalTime in, LocalTime out, String notes) {
        this.employeeId = employeeId; this.day = day; this.clockIn = in; this.clockOut = out; this.notes = notes == null ? "" : notes;
    }
    public Long getId() { return id; }
    public Long getEmployeeId() { return employeeId; }
    public LocalDate getDay() { return day; }
    public LocalTime getClockIn() { return clockIn; }
    public LocalTime getClockOut() { return clockOut; }
    public String getNotes() { return notes; }
    /** Horas trabajadas, con dos decimales. */
    public double getHours() { return Math.round(Duration.between(clockIn, clockOut).toMinutes() / 60.0 * 100) / 100.0; }
}
