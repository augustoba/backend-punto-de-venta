package com.pdv.catalog;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/price-lists")
public class PriceListController {
    private final PriceListService service;
    public PriceListController(PriceListService s) { this.service = s; }

    public record ListIn(String name, BigDecimal percent) {}

    @GetMapping public List<PriceList> list() { return service.list(); }
    @PostMapping public PriceList create(@RequestBody ListIn in) { return service.create(in.name(), in.percent()); }
    @PutMapping("/{id}") public PriceList update(@PathVariable Long id, @RequestBody ListIn in) { return service.update(id, in.name(), in.percent()); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.delete(id); }
}
