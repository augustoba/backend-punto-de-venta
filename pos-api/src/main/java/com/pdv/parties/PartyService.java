package com.pdv.parties;

import com.pdv.common.BusinessException;
import com.pdv.common.NotFoundException;
import com.pdv.parties.PartyEntry.Party;
import com.pdv.treasury.TreasuryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Clientes, proveedores y su cuenta corriente (ver referencia-envi/ANALISIS_cuentas_corrientes.md).
 * Saldo > 0 = el cliente nos debe / nosotros le debemos al proveedor. Los pagos y devoluciones mueven dinero de una cuenta.
 */
@Service
@Transactional
public class PartyService {
    public static final String CAT_CC = "Cuentas corrientes";
    public static final String SUB_CC_CUSTOMERS = "Cuenta corriente clientes";
    public static final String CAT_SUPPLIERS = "Proveedores";
    public static final String SUB_PURCHASES = "Compra a proveedores";

    private final CustomerRepository customers;
    private final SupplierRepository suppliers;
    private final PartyEntryRepository entries;
    private final TreasuryService treasury;

    public PartyService(CustomerRepository c, SupplierRepository s, PartyEntryRepository e, TreasuryService t) {
        this.customers = c; this.suppliers = s; this.entries = e; this.treasury = t;
    }

    public Customer customer(Long id) { return customers.findById(id).orElseThrow(() -> new NotFoundException("Cliente", id)); }
    public Supplier supplier(Long id) { return suppliers.findById(id).orElseThrow(() -> new NotFoundException("Proveedor", id)); }
    public List<Customer> customers() { return customers.findAll(); }
    public List<Supplier> suppliers() { return suppliers.findAll(); }

    public Customer saveCustomer(Long id, Customer in) {
        if (in.getName() == null || in.getName().isBlank()) throw new BusinessException("El cliente necesita un nombre");
        Customer c = id == null ? new Customer() : customer(id);
        c.setName(in.getName().trim()); c.setLastName(in.getLastName()); c.setPhone(in.getPhone()); c.setEmail(in.getEmail());
        c.setDni(in.getDni()); c.setCuit(in.getCuit()); c.setNotes(in.getNotes());
        return customers.save(c);
    }

    public Supplier saveSupplier(Long id, Supplier in) {
        if (in.getName() == null || in.getName().isBlank()) throw new BusinessException("El proveedor necesita una razón social");
        Supplier s = id == null ? new Supplier() : supplier(id);
        s.setName(in.getName().trim()); s.setCuit(in.getCuit()); s.setTrade(in.getTrade()); s.setContact(in.getContact());
        s.setPhone(in.getPhone()); s.setEmail(in.getEmail()); s.setAddress(in.getAddress()); s.setCity(in.getCity());
        s.setMarkup(in.getMarkup()); s.setNotes(in.getNotes());
        return suppliers.save(s);
    }

    public BigDecimal balance(Party party, Long id) { return entries.balance(party, id); }

    private BigDecimal sumPositive(Party p) {
        return entries.balances(p).stream().map(r -> (BigDecimal) r[1]).filter(b -> b.signum() > 0).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public BigDecimal totalReceivable() { return sumPositive(Party.CUSTOMER); }
    public BigDecimal totalPayable() { return sumPositive(Party.SUPPLIER); }

    public record Row(PartyEntry entry, BigDecimal balance) {}

    /** Libro con saldo corrido, del más viejo al más nuevo. */
    public List<Row> ledger(Party party, Long id) {
        BigDecimal acc = BigDecimal.ZERO;
        List<Row> out = new ArrayList<>();
        for (PartyEntry e : entries.findByPartyAndPartyIdOrderByOccurredAtAscIdAsc(party, id)) { acc = acc.add(e.getDelta()); out.add(new Row(e, acc)); }
        return out;
    }

    /** Venta a cuenta corriente: aumenta la deuda del cliente. */
    public PartyEntry addSaleDebt(Long customerId, BigDecimal total, String ref, String comment, String user) {
        customer(customerId);
        return entries.save(new PartyEntry(Party.CUSTOMER, customerId, "venta", total, null, comment, ref, user));
    }

    /** Recepción de una compra sin pagar: aumenta la deuda con el proveedor. */
    public PartyEntry addPurchaseDebt(Long supplierId, BigDecimal total, String ref, String comment, String user) {
        supplier(supplierId);
        return entries.save(new PartyEntry(Party.SUPPLIER, supplierId, "compra", total, null, comment, ref, user));
    }

    /** kind: pago | devolucion | deuda. Pago y devolución mueven dinero de la cuenta elegida. */
    public PartyEntry customerMovement(Long customerId, String kind, BigDecimal amount, Long accountId, String comment, String user) {
        Customer c = customer(customerId);
        if (amount == null || amount.signum() <= 0) throw new BusinessException("El monto debe ser mayor a cero");
        boolean money = kind.equals("pago") || kind.equals("devolucion");
        if (!List.of("pago", "devolucion", "deuda").contains(kind)) throw new BusinessException("Tipo de movimiento inválido");
        if (money && accountId == null) throw new BusinessException("Elegí el medio de pago");
        BigDecimal delta = kind.equals("pago") ? amount.negate() : amount;
        PartyEntry e = entries.save(new PartyEntry(Party.CUSTOMER, customerId, kind, delta, money ? accountId : null, comment, "", user));
        if (money) {
            String who = (c.getName() + " " + c.getLastName()).trim();
            treasury.addMovement(accountId, kind.equals("pago") ? amount : amount.negate(), CAT_CC, SUB_CC_CUSTOMERS,
                    (kind.equals("pago") ? "Cobro de " : "Devolución a ") + who, null, "cc-cliente", String.valueOf(customerId), user);
        }
        return e;
    }

    /** kind: pago | compra | nota_credito | ajuste. Solo el pago mueve dinero. */
    public PartyEntry supplierMovement(Long supplierId, String kind, BigDecimal amount, Long accountId, String comment, String user) {
        Supplier s = supplier(supplierId);
        if (amount == null || amount.signum() <= 0) throw new BusinessException("El monto debe ser mayor a cero");
        if (!List.of("pago", "compra", "nota_credito", "ajuste").contains(kind)) throw new BusinessException("Tipo de movimiento inválido");
        if (kind.equals("pago") && accountId == null) throw new BusinessException("Elegí la cuenta desde la que se paga");
        BigDecimal delta = (kind.equals("compra") || kind.equals("ajuste")) ? amount : amount.negate();
        PartyEntry e = entries.save(new PartyEntry(Party.SUPPLIER, supplierId, kind, delta, kind.equals("pago") ? accountId : null, comment, "", user));
        if (kind.equals("pago")) treasury.addMovement(accountId, amount.negate(), CAT_SUPPLIERS, SUB_PURCHASES, "Pago a " + s.getName(), null, "cc-proveedor", String.valueOf(supplierId), user);
        return e;
    }
}
