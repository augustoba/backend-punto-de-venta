package com.pdv.parties;

import com.pdv.parties.PartyEntry.Party;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/** API de clientes y proveedores con su cuenta corriente. */
@RestController
@RequestMapping("/api")
public class PartyController {
    private final PartyService service;
    public PartyController(PartyService s) { this.service = s; }

    public record CustomerOut(Long id, String name, String lastName, String phone, String email, String dni, String cuit, String notes, BigDecimal balance) {}
    public record SupplierOut(Long id, String name, String cuit, String trade, String contact, String phone, String email, String address, String city, BigDecimal markup, String notes, BigDecimal balance) {}
    public record MovementIn(@NotBlank String kind, BigDecimal amount, Long accountId, String comment) {}
    public record Totals(BigDecimal receivable, BigDecimal payable) {}

    private CustomerOut out(Customer c) { return new CustomerOut(c.getId(), c.getName(), c.getLastName(), c.getPhone(), c.getEmail(), c.getDni(), c.getCuit(), c.getNotes(), service.balance(Party.CUSTOMER, c.getId())); }
    private SupplierOut out(Supplier s) { return new SupplierOut(s.getId(), s.getName(), s.getCuit(), s.getTrade(), s.getContact(), s.getPhone(), s.getEmail(), s.getAddress(), s.getCity(), s.getMarkup(), s.getNotes(), service.balance(Party.SUPPLIER, s.getId())); }

    @GetMapping("/customers") public List<CustomerOut> customers() { return service.customers().stream().map(this::out).toList(); }
    @PostMapping("/customers") public CustomerOut createCustomer(@RequestBody Customer c) { return out(service.saveCustomer(null, c)); }
    @PutMapping("/customers/{id}") public CustomerOut updateCustomer(@PathVariable Long id, @RequestBody Customer c) { return out(service.saveCustomer(id, c)); }
    @GetMapping("/customers/{id}/ledger") public List<PartyService.Row> customerLedger(@PathVariable Long id) { return service.ledger(Party.CUSTOMER, id); }
    @PostMapping("/customers/{id}/movements")
    public CustomerOut customerMovement(@PathVariable Long id, @RequestBody MovementIn in) { service.customerMovement(id, in.kind(), in.amount(), in.accountId(), in.comment(), com.pdv.auth.CurrentUser.name()); return out(service.customer(id)); }

    @GetMapping("/suppliers") public List<SupplierOut> suppliers() { return service.suppliers().stream().map(this::out).toList(); }
    @PostMapping("/suppliers") public SupplierOut createSupplier(@RequestBody Supplier s) { return out(service.saveSupplier(null, s)); }
    @PutMapping("/suppliers/{id}") public SupplierOut updateSupplier(@PathVariable Long id, @RequestBody Supplier s) { return out(service.saveSupplier(id, s)); }
    @GetMapping("/suppliers/{id}/ledger") public List<PartyService.Row> supplierLedger(@PathVariable Long id) { return service.ledger(Party.SUPPLIER, id); }
    @PostMapping("/suppliers/{id}/movements")
    public SupplierOut supplierMovement(@PathVariable Long id, @RequestBody MovementIn in) { service.supplierMovement(id, in.kind(), in.amount(), in.accountId(), in.comment(), com.pdv.auth.CurrentUser.name()); return out(service.supplier(id)); }

    @GetMapping("/accounts-receivable-payable") public Totals totals() { return new Totals(service.totalReceivable(), service.totalPayable()); }
}
