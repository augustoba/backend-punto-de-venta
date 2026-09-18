package com.pdv.sales;

import com.pdv.billing.BillingService;
import com.pdv.catalog.CatalogService;
import com.pdv.catalog.PriceListService;
import com.pdv.catalog.Product;
import com.pdv.common.BusinessException;
import com.pdv.common.NotFoundException;
import com.pdv.parties.PartyService;
import com.pdv.sales.Sale.Method;
import com.pdv.settings.SettingsService;
import com.pdv.treasury.Account;
import com.pdv.treasury.TreasuryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Ventas y caja (ver referencia-envi/ANALISIS.md, ANALISIS_caja.md y ANALISIS_presupuestos_descuentos.md).
 * Una venta baja stock (también de combos), deja un asiento en la cuenta o una deuda del cliente, y puede llevar
 * descuento por línea, global o automático por transferencia.
 */
@Service
@Transactional
public class SalesService {
    private final SaleRepository sales;
    private final CashSessionRepository sessions;
    private final CatalogService catalog;
    private final TreasuryService treasury;
    private final PartyService parties;
    private final SettingsService settings;
    private final BillingService billing;
    private final PriceListService priceLists;

    public SalesService(SaleRepository s, CashSessionRepository c, CatalogService cat, TreasuryService t, PartyService p, SettingsService st, BillingService b, PriceListService pl) {
        this.sales = s; this.sessions = c; this.catalog = cat; this.treasury = t; this.parties = p; this.settings = st; this.billing = b; this.priceLists = pl;
    }

    public record LineIn(Long productId, int qty, BigDecimal price, BigDecimal discountUnit) {}
    public record SaleIn(Long customerId, List<LineIn> lines, BigDecimal discountPct, Method method, boolean paid, String notes,
                         boolean invoice, Long budgetId, Boolean autoDiscount, Long priceListId) {
        /** Sin lista de precios (la Principal). */
        public SaleIn(Long customerId, List<LineIn> lines, BigDecimal discountPct, Method method, boolean paid, String notes, boolean invoice, Long budgetId, Boolean autoDiscount) {
            this(customerId, lines, discountPct, method, paid, notes, invoice, budgetId, autoDiscount, null);
        }
    }

    private static BigDecimal r2(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }

    public boolean canSell() { return !settings.get().isArqueo() || sessions.findFirstByClosedAtIsNull().isPresent(); }

    public Sale register(SaleIn in, String user) {
        if (in.lines() == null || in.lines().isEmpty()) throw new BusinessException("La venta no tiene productos");
        if (!canSell()) throw new BusinessException("La caja está cerrada: abrila para vender");

        Sale sale = new Sale();
        sale.setSeller(user); sale.setCustomerId(in.customerId()); sale.setNotes(in.notes()); sale.setInvoiced(in.invoice()); sale.setBudgetId(in.budgetId()); sale.setPriceListId(in.priceListId());
        BigDecimal subtotal = BigDecimal.ZERO;
        for (LineIn l : in.lines()) {
            if (l.qty() <= 0) throw new BusinessException("La cantidad debe ser mayor a cero");
            Product p = catalog.get(l.productId());
            BigDecimal base = l.price() != null ? l.price()
                    : in.priceListId() != null ? priceLists.priceIn(p, in.priceListId())   // en una lista se ignora la oferta
                    : (p.getOffer().signum() > 0 ? p.getOffer() : p.getPrice());
            BigDecimal disc = l.discountUnit() == null ? BigDecimal.ZERO : l.discountUnit();
            Sale.Line line = new Sale.Line();
            line.productId = p.getId(); line.name = p.getName(); line.qty = l.qty(); line.price = r2(base); line.discountUnit = r2(disc);
            line.cost = p.isCombo() ? comboCost(p) : p.getCost(); line.categoryId = p.getCategoryId();
            sale.getLines().add(line);
            subtotal = subtotal.add(line.price.subtract(line.discountUnit).multiply(BigDecimal.valueOf(l.qty())));
        }
        subtotal = r2(subtotal);

        BigDecimal pct = in.discountPct() == null ? BigDecimal.ZERO : in.discountPct();
        BigDecimal auto = settings.get().getTransferDiscount();
        if (!Boolean.FALSE.equals(in.autoDiscount()) && in.method() == Method.TRANSFERENCIA && auto.signum() > 0
                && (pct.signum() == 0 || !settings.get().isCumulativeDiscounts())) pct = pct.max(auto);
        BigDecimal discountAmount = r2(subtotal.multiply(pct).movePointLeft(2));
        BigDecimal total = r2(subtotal.subtract(discountAmount));
        sale.setSubtotal(subtotal); sale.setDiscountPct(pct); sale.setDiscountAmount(discountAmount); sale.setTotal(total);

        boolean onAccount = in.method() == Method.CUENTA_CORRIENTE || !in.paid();
        if (onAccount && in.customerId() == null) throw new BusinessException("Para dejar la venta a cuenta corriente elegí un cliente");
        sale.setMethod(onAccount ? Method.CUENTA_CORRIENTE : in.method()); sale.setPaid(!onAccount);
        sale = sales.save(sale);
        String ref = "v" + sale.getId(), label = "Venta #" + sale.getId();

        for (Sale.Line l : sale.getLines()) catalog.consume(l.productId, l.qty, CatalogService.R_SALE, label, user);
        if (onAccount) {
            parties.addSaleDebt(in.customerId(), total, ref, label, user);
        } else {
            Account target = sale.getMethod() == Method.EFECTIVO
                    ? sessions.findFirstByClosedAtIsNull().map(s -> treasury.account(s.getAccountId())).orElseGet(() -> treasury.firstAccount(Account.Type.CAJA))
                    : treasury.firstAccount(Account.Type.BANCO);
            sale.setAccountId(target.getId());
            treasury.addMovement(target.getId(), total, "Ventas", "Ventas del local", label, null, "venta", ref, user);
        }
        if (in.invoice()) billing.issue(in.customerId(), sale.getLines().stream().map(l -> l.name).collect(java.util.stream.Collectors.joining(", ")), total, null);
        if (in.budgetId() != null) billing.markSold(in.budgetId());
        return sale;
    }

    private BigDecimal comboCost(Product combo) {
        return combo.getCombo().stream().map(c -> catalog.get(c.productId).getCost().multiply(BigDecimal.valueOf(c.qty))).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Sale get(Long id) { return sales.findById(id).orElseThrow(() -> new NotFoundException("Venta", id)); }
    public List<Sale> list() { return sales.findAllByOrderByOccurredAtDescIdDesc(); }

    /** Anular: devuelve el stock y quita los asientos de dinero y de cuenta corriente. */
    public void cancel(Long id, String user) {
        Sale s = get(id);
        for (Sale.Line l : s.getLines()) restore(l.productId, l.qty, "Anulación venta #" + s.getId(), user);
        treasury.removeBySource("venta", "v" + s.getId());
        parties.removeByRef("v" + s.getId());
        sales.delete(s);
    }

    private void restore(Long productId, int qty, String ref, String user) {
        Product p = catalog.get(productId);
        if (p.isCombo()) p.getCombo().forEach(c -> restore(c.productId, c.qty * qty, ref, user));
        else catalog.move(p, qty, CatalogService.R_MANUAL, ref, user);
    }

    // ---------- caja: apertura, cierre, arqueo ----------
    public java.util.Optional<CashSession> openSession() { return sessions.findFirstByClosedAtIsNull(); }
    public List<CashSession> sessions() { return sessions.findAllByOrderByOpenedAtDescIdDesc(); }

    public BigDecimal previousClose(Long accountId) {
        return sessions.findFirstByAccountIdAndClosedAtIsNotNullOrderByClosedAtDescIdDesc(accountId).map(CashSession::getReal).orElse(BigDecimal.ZERO);
    }

    /** Apertura: si el saldo declarado difiere del cierre anterior, se registra «Diferencia al abrir caja». */
    public CashSession open(Long accountId, BigDecimal newBalance, String notes, String user) {
        if (sessions.findFirstByClosedAtIsNull().isPresent()) throw new BusinessException("Ya hay una caja abierta");
        treasury.account(accountId);
        BigDecimal prev = previousClose(accountId), diff = newBalance.subtract(prev);
        if (diff.signum() != 0) treasury.addMovement(accountId, diff, TreasuryService.CAT_ADJUST, "Diferencia al abrir caja", "Diferencia de apertura de caja", null, "apertura", "", user);
        return sessions.save(new CashSession(accountId, user, newBalance, prev, notes));
    }

    /** Saldo esperado al cerrar = saldo de la cuenta según el libro. */
    public BigDecimal expectedClose() {
        return sessions.findFirstByClosedAtIsNull().map(s -> treasury.balance(s.getAccountId())).orElse(BigDecimal.ZERO);
    }

    public CashSession close(BigDecimal real, String notes, String user) {
        CashSession s = sessions.findFirstByClosedAtIsNull().orElseThrow(() -> new BusinessException("No hay una caja abierta"));
        BigDecimal expected = treasury.balance(s.getAccountId()), diff = real.subtract(expected);
        if (diff.signum() != 0) treasury.addMovement(s.getAccountId(), diff, TreasuryService.CAT_ADJUST, "Diferencia al cerrar caja", "Diferencia de cierre de caja", null, "cierre", String.valueOf(s.getId()), user);
        s.close(expected, real, notes);
        return s;
    }

    public CashSession verify(Long id, boolean verified) {
        CashSession s = sessions.findById(id).orElseThrow(() -> new NotFoundException("Turno de caja", id));
        s.setVerified(verified); return s;
    }
    public CashSession note(Long id, String note) {
        CashSession s = sessions.findById(id).orElseThrow(() -> new NotFoundException("Turno de caja", id));
        s.setNote(note); return s;
    }
}
