package com.pdv.finance;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** API de cheques y centros de costos. */
@RestController
@RequestMapping("/api")
public class FinanceController {
    private final FinanceService service;
    public FinanceController(FinanceService s) { this.service = s; }

    public record ChequeIn(Cheque.Kind kind, BigDecimal amount, LocalDate due, Long customerId, String description) {}
    public record CollectedIn(boolean collected) {}
    public record CenterIn(String name, CostCenter.Allocation allocation) {}
    public record CostIn(String name, BigDecimal monthly, String notes) {}

    @GetMapping("/cheques") public List<Cheque> cheques(@RequestParam Cheque.Kind kind) { return service.cheques(kind); }
    @GetMapping("/cheques/pending") public Map<String, BigDecimal> pending() { return Map.of("cobrar", service.pending(Cheque.Kind.COBRAR), "pagar", service.pending(Cheque.Kind.PAGAR)); }
    @PostMapping("/cheques") public Cheque create(@RequestBody ChequeIn in) { return service.saveCheque(null, in.kind(), in.amount(), in.due(), in.customerId(), in.description()); }
    @PutMapping("/cheques/{id}") public Cheque update(@PathVariable Long id, @RequestBody ChequeIn in) { return service.saveCheque(id, in.kind(), in.amount(), in.due(), in.customerId(), in.description()); }
    @PutMapping("/cheques/{id}/collected") public Cheque collected(@PathVariable Long id, @RequestBody CollectedIn in) { return service.setCollected(id, in.collected()); }
    @DeleteMapping("/cheques/{id}") public void deleteCheque(@PathVariable Long id) { service.deleteCheque(id); }

    @GetMapping("/cost-centers") public List<CostCenter> centers() { return service.centers(); }
    @PostMapping("/cost-centers") public CostCenter createCenter(@RequestBody CenterIn in) { return service.createCenter(in.name(), in.allocation()); }
    @DeleteMapping("/cost-centers/{id}") public void deleteCenter(@PathVariable Long id) { service.deleteCenter(id); }
    @PostMapping("/cost-centers/{id}/costs") public CostCenter addCost(@PathVariable Long id, @RequestBody CostIn in) { return service.addFixedCost(id, in.name(), in.monthly(), in.notes()); }
    @DeleteMapping("/cost-centers/{id}/costs/{index}") public CostCenter removeCost(@PathVariable Long id, @PathVariable int index) { return service.removeFixedCost(id, index); }
}
