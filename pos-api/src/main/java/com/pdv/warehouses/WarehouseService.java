package com.pdv.warehouses;

import com.pdv.catalog.CatalogService;
import com.pdv.catalog.Product;
import com.pdv.common.BusinessException;
import com.pdv.common.NotFoundException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Depósitos y transferencias. El stock total del producto (libro de stock) no se toca: el depósito principal
 * es «total − lo que hay en los otros», así ventas y compras siguen descontando del principal sin cambios.
 */
@Service
@Transactional
public class WarehouseService implements ApplicationRunner {
    public record LineIn(Long productId, int qty) {}
    public record StockRow(Long productId, String name, int total, Map<Long, Integer> byWarehouse) {}

    private final WarehouseRepository warehouses;
    private final StockLevelRepository levels;
    private final StockTransferRepository transfers;
    private final CatalogService catalog;

    public WarehouseService(WarehouseRepository w, StockLevelRepository l, StockTransferRepository t, CatalogService c) {
        this.warehouses = w; this.levels = l; this.transfers = t; this.catalog = c;
    }

    @Override
    public void run(ApplicationArguments args) { ensureMain(); }

    Warehouse ensureMain() { return warehouses.findFirstByMainTrue().orElseGet(() -> warehouses.save(new Warehouse("Principal", true))); }

    public List<Warehouse> list() { ensureMain(); return warehouses.findAll(); }
    public Warehouse get(Long id) { return warehouses.findById(id).orElseThrow(() -> new NotFoundException("Depósito", id)); }

    public Warehouse create(String name) {
        if (name == null || name.isBlank()) throw new BusinessException("El depósito necesita un nombre");
        if (warehouses.findAll().stream().anyMatch(w -> w.getName().equalsIgnoreCase(name.trim()))) throw new BusinessException("Ya existe un depósito con ese nombre");
        return warehouses.save(new Warehouse(name.trim(), false));
    }

    public Warehouse rename(Long id, String name) {
        if (name == null || name.isBlank()) throw new BusinessException("El depósito necesita un nombre");
        Warehouse w = get(id); w.setName(name.trim()); return w;
    }

    /** Lo que hay de un producto en un depósito (el principal puede quedar negativo, como el stock general). */
    public int available(Product p, Warehouse w) {
        if (!w.isMain()) return levels.findByProductIdAndWarehouseId(p.getId(), w.getId()).map(StockLevel::getQty).orElse(0);
        return p.getStock() - levels.findByProductId(p.getId()).stream().mapToInt(StockLevel::getQty).sum();
    }

    public List<StockRow> stock() {
        List<Warehouse> ws = list();
        List<StockRow> rows = new ArrayList<>();
        for (Product p : catalog.list(false)) {
            if (p.isCombo()) continue;
            Map<Long, Integer> by = new LinkedHashMap<>();
            ws.forEach(w -> by.put(w.getId(), available(p, w)));
            rows.add(new StockRow(p.getId(), p.getName(), p.getStock(), by));
        }
        return rows;
    }

    public StockTransfer transfer(Long fromId, Long toId, List<LineIn> in, String notes, String user) {
        if (fromId == null || toId == null) throw new BusinessException("Elegí el depósito de origen y el de destino");
        if (fromId.equals(toId)) throw new BusinessException("El origen y el destino deben ser distintos");
        if (in == null || in.isEmpty()) throw new BusinessException("Agregá al menos un producto");
        Warehouse from = get(fromId), to = get(toId);
        Set<Long> seen = new HashSet<>();
        List<StockTransfer.Line> lines = new ArrayList<>();
        for (LineIn l : in) {
            if (l.qty() <= 0) throw new BusinessException("La cantidad debe ser mayor a cero");
            if (!seen.add(l.productId())) throw new BusinessException("Un producto está repetido en la transferencia");
            Product p = catalog.get(l.productId());
            if (p.isCombo()) throw new BusinessException("«" + p.getName() + "» es un combo: su stock sale de los componentes");
            int have = available(p, from);
            if (have < l.qty()) throw new BusinessException("No alcanza el stock de «" + p.getName() + "» en " + from.getName() + " (hay " + have + ")");
            lines.add(new StockTransfer.Line(p.getId(), p.getName(), l.qty()));
        }
        for (StockTransfer.Line l : lines) {
            if (!from.isMain()) shift(l.productId, from.getId(), -l.qty);
            if (!to.isMain()) shift(l.productId, to.getId(), l.qty);
        }
        return transfers.save(new StockTransfer(fromId, toId, user, notes, lines));
    }

    private void shift(Long productId, Long warehouseId, int delta) {
        StockLevel s = levels.findByProductIdAndWarehouseId(productId, warehouseId).orElseGet(() -> new StockLevel(productId, warehouseId, 0));
        s.setQty(s.getQty() + delta);
        levels.save(s);
    }

    public List<StockTransfer> transfers() { return transfers.findAllByOrderByIdDesc(); }
}
