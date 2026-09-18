package com.pdv.billing;

import com.pdv.common.BusinessException;
import com.pdv.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/** Presupuestos y facturas de prueba (ver referencia-envi/ANALISIS_presupuestos_descuentos.md y ANALISIS_facturas_compras.md). */
@Service
@Transactional
public class BillingService {
    private final BudgetRepository budgets;
    private final InvoiceRepository invoices;

    public BillingService(BudgetRepository b, InvoiceRepository i) { this.budgets = b; this.invoices = i; }

    public record BudgetLineIn(Long productId, String name, int qty, BigDecimal price) {}

    public Budget budget(Long id) { return budgets.findById(id).orElseThrow(() -> new NotFoundException("Presupuesto", id)); }
    public List<Budget> budgets() { return budgets.findAllByOrderByCreatedAtDescIdDesc(); }

    public Budget saveBudget(Long id, Long customerId, LocalDate expires, List<BudgetLineIn> lines, String notes, String user) {
        if (lines == null || lines.isEmpty()) throw new BusinessException("El presupuesto no tiene productos");
        Budget b = id == null ? new Budget() : budget(id);
        b.setCustomerId(customerId); b.setExpires(expires == null ? LocalDate.now().plusDays(30) : expires); b.setNotes(notes);
        if (id == null) b.setUsername(user);
        b.getLines().clear();
        BigDecimal total = BigDecimal.ZERO;
        for (BudgetLineIn l : lines) {
            Budget.Line line = new Budget.Line();
            line.productId = l.productId(); line.name = l.name(); line.qty = l.qty(); line.price = l.price();
            b.getLines().add(line);
            total = total.add(l.price().multiply(BigDecimal.valueOf(l.qty())));
        }
        b.setTotal(total);
        return budgets.save(b);
    }

    public Budget setStatus(Long id, Budget.Status status) { Budget b = budget(id); b.setStatus(status); return b; }
    public void deleteBudget(Long id) { budgets.delete(budget(id)); }
    /** Lo llama la venta que convierte el presupuesto. */
    public void markSold(Long id) { setStatus(id, Budget.Status.VENDIDO); }

    public List<Invoice> invoices() { return invoices.findAllByOrderByOccurredAtDescIdDesc(); }

    /** Factura C de prueba: el total incluye IVA (neto = total / (1 + alícuota)). */
    public Invoice issue(Long customerId, String items, BigDecimal total, BigDecimal ivaRate) {
        if (total == null || total.signum() <= 0) throw new BusinessException("El total de la factura debe ser mayor a cero");
        BigDecimal rate = ivaRate == null ? new BigDecimal("21") : ivaRate;
        BigDecimal net = total.divide(BigDecimal.ONE.add(rate.movePointLeft(2)), 2, RoundingMode.HALF_UP);
        String number = String.format("0001-%06d", invoices.count() + 1);
        return invoices.save(new Invoice(number, customerId, total, net, total.subtract(net), items));
    }
}
