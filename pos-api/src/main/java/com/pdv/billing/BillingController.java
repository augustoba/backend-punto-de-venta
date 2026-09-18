package com.pdv.billing;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** API de presupuestos y facturas. */
@RestController
@RequestMapping("/api")
public class BillingController {
    private final BillingService service;
    public BillingController(BillingService s) { this.service = s; }

    public record BudgetIn(Long customerId, LocalDate expires, List<BillingService.BudgetLineIn> lines, String notes) {}
    public record StatusIn(Budget.Status status) {}
    public record InvoiceIn(Long customerId, String items, BigDecimal total, BigDecimal ivaRate) {}

    @GetMapping("/budgets") public List<Budget> budgets() { return service.budgets(); }
    @PostMapping("/budgets") public Budget create(@RequestBody BudgetIn in) { return service.saveBudget(null, in.customerId(), in.expires(), in.lines(), in.notes(), "sistema"); }
    @PutMapping("/budgets/{id}") public Budget update(@PathVariable Long id, @RequestBody BudgetIn in) { return service.saveBudget(id, in.customerId(), in.expires(), in.lines(), in.notes(), "sistema"); }
    @PutMapping("/budgets/{id}/status") public Budget status(@PathVariable Long id, @RequestBody StatusIn in) { return service.setStatus(id, in.status()); }
    @DeleteMapping("/budgets/{id}") public void delete(@PathVariable Long id) { service.deleteBudget(id); }

    @GetMapping("/invoices") public List<Invoice> invoices() { return service.invoices(); }
    @PostMapping("/invoices") public Invoice issue(@RequestBody InvoiceIn in) { return service.issue(in.customerId(), in.items(), in.total(), in.ivaRate()); }
}
