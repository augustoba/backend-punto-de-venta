package com.pdv.purchasing;

import com.pdv.catalog.CatalogService;
import com.pdv.catalog.CatalogService.ProductData;
import com.pdv.catalog.Product;
import com.pdv.common.BusinessException;
import com.pdv.parties.PartyEntry.Party;
import com.pdv.parties.PartyService;
import com.pdv.parties.Supplier;
import com.pdv.purchasing.PurchasingService.LineIn;
import com.pdv.treasury.Account;
import com.pdv.treasury.TreasuryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PurchasingServiceTest {
    @Autowired PurchasingService service;
    @Autowired CatalogService catalog;
    @Autowired PartyService parties;
    @Autowired TreasuryService treasury;

    private BigDecimal bd(String s) { return new BigDecimal(s); }
    private Product prod() { return catalog.create(new ProductData("P", "", null, null, bd("500"), bd("1000"), BigDecimal.ZERO, 0, 0, bd("21"), null), 0, "t"); }
    private Supplier prov() { Supplier s = new Supplier(); s.setName("Prov"); return parties.saveSupplier(null, s); }

    @Test void recibirSinPagarSubeStockActualizaCostoYDejaDeuda() {
        Product p = prod(); Supplier s = prov();
        Purchase o = service.save(null, s.getId(), List.of(new LineIn(p.getId(), 5, bd("600"))), "", "t");
        assertEquals(Purchase.Status.BORRADOR, o.getStatus());
        assertEquals(0, bd("500").compareTo(o.getLines().get(0).prevCost));
        service.markOrdered(o.getId());
        service.receive(o.getId(), false, null, "t");
        assertEquals(5, catalog.get(p.getId()).getStock());
        assertEquals(0, bd("600").compareTo(catalog.get(p.getId()).getCost()));
        assertEquals(0, bd("3000").compareTo(parties.balance(Party.SUPPLIER, s.getId())));
    }

    @Test void recibirPagoEgresaDeLaCuentaYNoDejaDeuda() {
        Product p = prod(); Supplier s = prov();
        Account caja = treasury.createAccount("Caja compras", Account.Type.CAJA, null, null, bd("1000"), "t");
        Purchase o = service.save(null, s.getId(), List.of(new LineIn(p.getId(), 2, bd("100"))), "", "t");
        service.receive(o.getId(), true, caja.getId(), "t");
        assertEquals(0, bd("800").compareTo(treasury.balance(caja.getId())));
        assertEquals(0, BigDecimal.ZERO.compareTo(parties.balance(Party.SUPPLIER, s.getId())));
    }

    @Test void unPedidoRecibidoNoSeEditaNiSeElimina() {
        Product p = prod(); Supplier s = prov();
        Purchase o = service.save(null, s.getId(), List.of(new LineIn(p.getId(), 1, bd("10"))), "", "t");
        service.receive(o.getId(), false, null, "t");
        assertThrows(BusinessException.class, () -> service.save(o.getId(), s.getId(), List.of(new LineIn(p.getId(), 1, bd("10"))), "", "t"));
        assertThrows(BusinessException.class, () -> service.delete(o.getId()));
        assertThrows(BusinessException.class, () -> service.receive(o.getId(), false, null, "t"));
    }

    @Test void pagadoExigeCuenta() {
        Product p = prod(); Supplier s = prov();
        Purchase o = service.save(null, s.getId(), List.of(new LineIn(p.getId(), 1, bd("10"))), "", "t");
        assertThrows(BusinessException.class, () -> service.receive(o.getId(), true, null, "t"));
    }
}
