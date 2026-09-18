package com.pdv.catalog;

import com.pdv.common.BusinessException;
import com.pdv.common.NotFoundException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Listas de precios. «Principal» (0 %) siempre existe; las demás ajustan el precio base en un porcentaje. */
@Service
@Transactional
public class PriceListService implements ApplicationRunner {
    private final PriceListRepository lists;
    public PriceListService(PriceListRepository l) { this.lists = l; }

    @Override
    public void run(ApplicationArguments args) { main(); }

    PriceList main() { return lists.findFirstByMainTrue().orElseGet(() -> lists.save(new PriceList("Principal", BigDecimal.ZERO, true))); }

    public List<PriceList> list() { main(); return lists.findAll(); }
    public PriceList get(Long id) { return lists.findById(id).orElseThrow(() -> new NotFoundException("Lista de precios", id)); }

    public PriceList create(String name, BigDecimal percent) {
        check(name, percent);
        if (lists.findAll().stream().anyMatch(l -> l.getName().equalsIgnoreCase(name.trim()))) throw new BusinessException("Ya existe una lista con ese nombre");
        return lists.save(new PriceList(name.trim(), percent, false));
    }

    public PriceList update(Long id, String name, BigDecimal percent) {
        PriceList l = get(id);
        if (l.isMain()) throw new BusinessException("La lista Principal no se puede modificar");
        check(name, percent);
        l.setName(name.trim()); l.setPercent(percent);
        return l;
    }

    public void delete(Long id) {
        if (get(id).isMain()) throw new BusinessException("La lista Principal no se puede eliminar");
        lists.deleteById(id);
    }

    /** Precio de un producto en una lista; con la Principal (o sin lista) es el precio base. */
    public BigDecimal priceIn(Product p, Long listId) {
        if (listId == null) return p.getPrice();
        PriceList l = get(listId);
        if (l.isMain()) return p.getPrice();
        return p.getPrice().multiply(BigDecimal.ONE.add(l.getPercent().movePointLeft(2))).setScale(2, RoundingMode.HALF_UP);
    }

    private void check(String name, BigDecimal percent) {
        if (name == null || name.isBlank()) throw new BusinessException("La lista necesita un nombre");
        if (percent == null) throw new BusinessException("Indicá el porcentaje de ajuste");
        if (percent.compareTo(new BigDecimal("-100")) <= 0) throw new BusinessException("El descuento no puede ser del 100 % o más");
    }
}
