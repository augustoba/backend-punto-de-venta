package com.pdv.purchasing;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/** API de compras a proveedores. */
@RestController
@RequestMapping("/api/purchases")
public class PurchasingController {
    private final PurchasingService service;
    public PurchasingController(PurchasingService s) { this.service = s; }

    public record SaveIn(Long supplierId, List<PurchasingService.LineIn> lines, String name) {}
    public record ReceiveIn(boolean paid, Long accountId) {}

    @GetMapping public List<Purchase> list() { return service.list(); }
    @GetMapping("/{id}") public Purchase get(@PathVariable Long id) { return service.get(id); }
    @PostMapping public Purchase create(@RequestBody SaveIn in) { return service.save(null, in.supplierId(), in.lines(), in.name(), "sistema"); }
    @PutMapping("/{id}") public Purchase update(@PathVariable Long id, @RequestBody SaveIn in) { return service.save(id, in.supplierId(), in.lines(), in.name(), "sistema"); }
    @PostMapping("/{id}/order") public Purchase order(@PathVariable Long id) { return service.markOrdered(id); }
    @PostMapping("/{id}/receive") public Purchase receive(@PathVariable Long id, @RequestBody ReceiveIn in) { return service.receive(id, in.paid(), in.accountId(), "sistema"); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.delete(id); }
}
