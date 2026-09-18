package com.pdv.returns;

import com.pdv.catalog.CatalogService;
import com.pdv.common.BusinessException;
import com.pdv.parties.PartyService;
import com.pdv.sales.CashSession;
import com.pdv.sales.CashSessionRepository;
import com.pdv.sales.Sale;
import com.pdv.sales.SalesService;
import com.pdv.treasury.Account;
import com.pdv.treasury.TreasuryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

/**
 * Devoluciones y cambios. Devuelve stock (o lo descarta como merma), devuelve el dinero (o lo acredita en la cuenta
 * corriente del cliente) y emite una nota de crédito de prueba. No se puede devolver más de lo vendido.
 */
@Service
@Transactional
public class ReturnsService {
    public record LineIn(Long productId, int qty, boolean restock) {}
    public record Returnable(Long productId, String name, int sold, int returned, int remaining, BigDecimal unitPrice) {}

    private final SaleReturnRepository returns;
    private final SalesService sales;
    private final CatalogService catalog;
    private final TreasuryService treasury;
    private final PartyService parties;
    private final CashSessionRepository sessions;

    public ReturnsService(SaleReturnRepository r, SalesService s, CatalogService c, TreasuryService t, PartyService p, CashSessionRepository cs) {
        this.returns = r; this.sales = s; this.catalog = c; this.treasury = t; this.parties = p; this.sessions = cs;
    }

    private static BigDecimal r2(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }

    public List<SaleReturn> list() { return returns.findAllByOrderByIdDesc(); }

    /** Precio efectivo por unidad: el de la línea menos su descuento, con el descuento/recargo global de la venta aplicado. */
    private BigDecimal unitPrice(Sale sale, Sale.Line l) {
        BigDecimal factor = BigDecimal.ONE.subtract(sale.getDiscountPct().movePointLeft(2));
        return r2(l.price.subtract(l.discountUnit).multiply(factor));
    }

    private Map<Long, Integer> alreadyReturned(Long saleId) {
        Map<Long, Integer> m = new HashMap<>();
        returns.findBySaleId(saleId).forEach(r -> r.getLines().forEach(l -> m.merge(l.productId, l.qty, Integer::sum)));
        return m;
    }

    public List<Returnable> returnable(Long saleId) {
        Sale sale = sales.get(saleId);
        Map<Long, Integer> done = alreadyReturned(saleId);
        return sale.getLines().stream().map(l -> {
            int back = done.getOrDefault(l.productId, 0);
            return new Returnable(l.productId, l.name, l.qty, back, l.qty - back, unitPrice(sale, l));
        }).toList();
    }

    public SaleReturn register(Long saleId, List<LineIn> in, String reason, SaleReturn.Refund refund, String user) {
        if (in == null || in.isEmpty()) throw new BusinessException("Elegí al menos un producto para devolver");
        if (refund == null) throw new BusinessException("Elegí cómo se devuelve el dinero");
        Sale sale = sales.get(saleId);
        if (refund == SaleReturn.Refund.CUENTA_CORRIENTE && sale.getCustomerId() == null)
            throw new BusinessException("Para acreditar en cuenta corriente la venta tiene que tener un cliente");

        Map<Long, Integer> done = alreadyReturned(saleId);
        SaleReturn ret = new SaleReturn(saleId, user, reason, refund, sale.getCustomerId());
        BigDecimal total = BigDecimal.ZERO;
        Set<Long> seen = new HashSet<>();
        for (LineIn l : in) {
            if (l.qty() <= 0) throw new BusinessException("La cantidad a devolver debe ser mayor a cero");
            if (!seen.add(l.productId())) throw new BusinessException("Un producto está repetido en la devolución");
            Sale.Line sold = sale.getLines().stream().filter(x -> x.productId.equals(l.productId())).findFirst()
                    .orElseThrow(() -> new BusinessException("Ese producto no estaba en la venta"));
            int remaining = sold.qty - done.getOrDefault(l.productId(), 0);
            if (l.qty() > remaining) throw new BusinessException("De «" + sold.name + "» sólo se puede devolver " + remaining + " (se vendieron " + sold.qty + ")");
            SaleReturn.Line line = new SaleReturn.Line();
            line.productId = sold.productId; line.name = sold.name; line.qty = l.qty(); line.restock = l.restock();
            line.unitPrice = unitPrice(sale, sold); line.unitCost = sold.cost;
            ret.getLines().add(line);
            total = total.add(line.unitPrice.multiply(BigDecimal.valueOf(l.qty())));
        }
        ret.setTotal(r2(total));
        ret.setCreditNote(String.format("NC-000001-%06d", returns.count() + 1));
        ret = returns.save(ret);
        String ref = "Devolución " + ret.getCreditNote();

        for (SaleReturn.Line l : ret.getLines()) if (l.restock) catalog.consume(l.productId, -l.qty, "Devolución", ref, user);

        switch (refund) {
            case CUENTA_CORRIENTE -> parties.addCredit(sale.getCustomerId(), ret.getTotal(), "dev" + ret.getId(), "Nota de crédito " + ret.getCreditNote() + " (venta #" + sale.getNumber() + ")", user);
            case CAMBIO -> { /* sin movimiento de dinero */ }
            default -> {
                Long account = accountFor(refund);
                ret.setAccountId(account);
                treasury.addMovement(account, ret.getTotal().negate(), "Ventas", "Ventas del local", "Devolución venta #" + sale.getNumber() + " · " + ret.getCreditNote(),
                        Instant.now(), "devolucion", String.valueOf(ret.getId()), user);
            }
        }
        return ret;
    }

    /** Efectivo sale de la caja abierta (o la primera caja); transferencia y tarjeta, del primer banco. */
    private Long accountFor(SaleReturn.Refund refund) {
        if (refund == SaleReturn.Refund.EFECTIVO) return sessions.findFirstByClosedAtIsNull().map(CashSession::getAccountId).orElseGet(() -> treasury.firstAccount(Account.Type.CAJA).getId());
        return treasury.firstAccount(Account.Type.BANCO).getId();
    }
}
