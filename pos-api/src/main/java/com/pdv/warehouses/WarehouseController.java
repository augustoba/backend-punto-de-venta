package com.pdv.warehouses;

import com.pdv.auth.CurrentUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WarehouseController {
    private final WarehouseService service;
    public WarehouseController(WarehouseService s) { this.service = s; }

    public record NameIn(String name) {}
    public record TransferIn(Long fromId, Long toId, List<WarehouseService.LineIn> lines, String notes) {}

    @GetMapping("/warehouses") public List<Warehouse> list() { return service.list(); }
    @PostMapping("/warehouses") public Warehouse create(@RequestBody NameIn in) { return service.create(in.name()); }
    @PutMapping("/warehouses/{id}") public Warehouse rename(@PathVariable Long id, @RequestBody NameIn in) { return service.rename(id, in.name()); }
    @GetMapping("/warehouses/stock") public List<WarehouseService.StockRow> stock() { return service.stock(); }
    @GetMapping("/stock-transfers") public List<StockTransfer> transfers() { return service.transfers(); }
    @PostMapping("/stock-transfers")
    public StockTransfer transfer(@RequestBody TransferIn in) { return service.transfer(in.fromId(), in.toId(), in.lines(), in.notes(), CurrentUser.name()); }
}
