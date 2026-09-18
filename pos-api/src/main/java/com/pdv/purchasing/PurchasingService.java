package com.pdv.purchasing;

import com.pdv.catalog.CatalogService;
import com.pdv.catalog.Product;
import com.pdv.common.BusinessException;
import com.pdv.common.NotFoundException;
import com.pdv.parties.PartyService;
import com.pdv.purchasing.Purchase.Status;
import com.pdv.treasury.TreasuryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Compras (ver referencia-envi/ANALISIS_facturas_compras.md): el pedido es una intención de compra;
 * al RECIBIR sube el stock, se actualiza el costo del producto y, si no está pago, queda deuda con el proveedor.
 */
@Service
@Transactional
public class PurchasingService {
    private final PurchaseRepository purchases;
    private final CatalogService catalog;
    private final PartyService parties;
    private final TreasuryService treasury;

    public PurchasingService(PurchaseRepository p, CatalogService c, PartyService pa, TreasuryService t) {
        this.purchases = p; this.catalog = c; this.parties = pa; this.treasury = t;
    }

    public record LineIn(Long productId, int qty, BigDecimal cost) {}

    public Purchase get(Long id) { return purchases.findById(id).orElseThrow(() -> new NotFoundException("Pedido", id)); }
    public List<Purchase> list() { return purchases.findAllByOrderByCreatedAtDescIdDesc(); }

    /** Crea (id == null) o edita un borrador/pedido. Guarda el costo anterior de cada producto para mostrar la variación. */
    public Purchase save(Long id, Long supplierId, List<LineIn> lines, String name, String user) {
        parties.supplier(supplierId);
        if (lines == null || lines.isEmpty()) throw new BusinessException("El pedido no tiene productos");
        Purchase p = id == null ? new Purchase() : get(id);
        if (p.getStatus() == Status.RECIBIDO) throw new BusinessException("Un pedido recibido no se puede editar");
        p.setSupplierId(supplierId); p.setName(name); if (id == null) p.setUsername(user);
        p.getLines().clear();
        BigDecimal total = BigDecimal.ZERO;
        for (LineIn l : lines) {
            if (l.qty() <= 0) throw new BusinessException("La cantidad debe ser mayor a cero");
            Product prod = catalog.get(l.productId());
            Purchase.Line line = new Purchase.Line();
            line.productId = prod.getId(); line.name = prod.getName(); line.qty = l.qty(); line.cost = l.cost(); line.prevCost = prod.getCost();
            p.getLines().add(line);
            total = total.add(l.cost().multiply(BigDecimal.valueOf(l.qty())));
        }
        p.setTotal(total);
        return purchases.save(p);
    }

    public Purchase markOrdered(Long id) {
        Purchase p = get(id);
        if (p.getStatus() != Status.BORRADOR) throw new BusinessException("Solo un borrador se puede marcar como enviado");
        p.setStatus(Status.PEDIDO);
        return p;
    }

    /** Recepción. paid + accountId: egreso de esa cuenta; si no está pago, deuda con el proveedor. */
    public Purchase receive(Long id, boolean paid, Long accountId, String user) {
        Purchase p = get(id);
        if (p.getStatus() == Status.RECIBIDO) throw new BusinessException("El pedido ya fue recibido");
        if (paid && accountId == null) throw new BusinessException("Elegí la cuenta desde la que se pagó");
        String label = "Compra P-" + p.getNumber();
        for (Purchase.Line l : p.getLines()) {
            Product prod = catalog.get(l.productId);
            catalog.move(prod, l.qty, CatalogService.R_PURCHASE, label, user);
            if (prod.getCost().compareTo(l.cost) != 0) catalog.setCost(prod, l.cost, user);
            l.received = l.qty;
        }
        p.setStatus(Status.RECIBIDO); p.setReceivedAt(Instant.now()); p.setPaid(paid);
        if (paid) treasury.addMovement(accountId, p.getTotal().negate(), PartyService.CAT_SUPPLIERS, PartyService.SUB_PURCHASES,
                label + " · " + parties.supplier(p.getSupplierId()).getName(), null, "compra", "o" + p.getId(), user);
        else parties.addPurchaseDebt(p.getSupplierId(), p.getTotal(), "o" + p.getId(), label, user);
        return p;
    }

    public void delete(Long id) {
        Purchase p = get(id);
        if (p.getStatus() == Status.RECIBIDO) throw new BusinessException("Un pedido recibido no se puede eliminar");
        purchases.delete(p);
    }
}
