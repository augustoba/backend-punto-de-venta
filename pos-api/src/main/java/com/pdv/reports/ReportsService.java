package com.pdv.reports;

import com.pdv.sales.Sale;
import com.pdv.sales.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

/** Reportes de ventas (ver referencia-envi/ANALISIS.md, pestañas Ventas y Productos). Usan el costo congelado de cada línea. */
@Service
@Transactional(readOnly = true)
public class ReportsService {
    private final SaleRepository sales;
    public ReportsService(SaleRepository s) { this.sales = s; }

    public record ByMethod(Sale.Method method, long count, BigDecimal total) {}
    public record ProductRow(Long productId, String name, long qty, BigDecimal revenue, BigDecimal cost, BigDecimal profit, BigDecimal marginPct) {}

    private List<Sale> between(Instant from, Instant to, boolean onlyPaid) {
        return sales.findAll().stream()
                .filter(s -> (from == null || !s.getOccurredAt().isBefore(from)) && (to == null || s.getOccurredAt().isBefore(to)) && (!onlyPaid || s.isPaid()))
                .toList();
    }

    public List<ByMethod> salesByMethod(Instant from, Instant to, boolean onlyPaid) {
        Map<Sale.Method, List<Sale>> g = new EnumMap<>(Sale.Method.class);
        between(from, to, onlyPaid).forEach(s -> g.computeIfAbsent(s.getMethod(), k -> new ArrayList<>()).add(s));
        return g.entrySet().stream()
                .map(e -> new ByMethod(e.getKey(), e.getValue().size(), e.getValue().stream().map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add)))
                .toList();
    }

    /** Ranking por facturación (con el descuento global prorrateado) y ganancia sobre el costo congelado. */
    public List<ProductRow> productRanking(Instant from, Instant to) {
        Map<Long, long[]> qty = new HashMap<>();
        Map<Long, BigDecimal[]> money = new HashMap<>();
        Map<Long, String> names = new HashMap<>();
        for (Sale s : between(from, to, false)) {
            BigDecimal factor = BigDecimal.ONE.subtract(s.getDiscountPct().movePointLeft(2));
            for (Sale.Line l : s.getLines()) {
                names.put(l.productId, l.name);
                qty.computeIfAbsent(l.productId, k -> new long[1])[0] += l.qty;
                BigDecimal[] m = money.computeIfAbsent(l.productId, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                m[0] = m[0].add(l.price.subtract(l.discountUnit).multiply(BigDecimal.valueOf(l.qty)).multiply(factor));
                m[1] = m[1].add(l.cost.multiply(BigDecimal.valueOf(l.qty)));
            }
        }
        return money.entrySet().stream().map(e -> {
            BigDecimal rev = e.getValue()[0].setScale(2, RoundingMode.HALF_UP), cost = e.getValue()[1].setScale(2, RoundingMode.HALF_UP);
            BigDecimal profit = rev.subtract(cost);
            BigDecimal pct = rev.signum() == 0 ? BigDecimal.ZERO : profit.multiply(BigDecimal.valueOf(100)).divide(rev, 2, RoundingMode.HALF_UP);
            return new ProductRow(e.getKey(), names.get(e.getKey()), qty.get(e.getKey())[0], rev, cost, profit, pct);
        }).sorted(Comparator.comparing(ProductRow::revenue).reversed()).toList();
    }
}
