package com.pdv.sales;

import com.pdv.settings.Settings;
import com.pdv.settings.SettingsService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** API de ventas, caja (arqueo) y ajustes. */
@RestController
@RequestMapping("/api")
public class SalesController {
    private final SalesService service;
    private final SettingsService settings;
    public SalesController(SalesService s, SettingsService st) { this.service = s; this.settings = st; }

    public record OpenIn(Long accountId, BigDecimal balance, String notes) {}
    public record CloseIn(BigDecimal real, String notes) {}
    public record VerifyIn(boolean verified) {}
    public record NoteIn(String note) {}

    @GetMapping("/sales") public List<Sale> sales() { return service.list(); }
    @GetMapping("/sales/{id}") public Sale sale(@PathVariable Long id) { return service.get(id); }
    @PostMapping("/sales") public Sale register(@RequestBody SalesService.SaleIn in) { return service.register(in, "sistema"); }
    @DeleteMapping("/sales/{id}") public void cancel(@PathVariable Long id) { service.cancel(id, "sistema"); }

    @GetMapping("/cash/status")
    public Map<String, Object> status() {
        var open = service.openSession();
        return Map.of("canSell", service.canSell(), "open", open.isPresent(), "session", open.orElse(null) == null ? Map.of() : open.get(), "expected", service.expectedClose());
    }
    @GetMapping("/cash/previous-close") public BigDecimal previous(@RequestParam Long accountId) { return service.previousClose(accountId); }
    @PostMapping("/cash/open") public CashSession open(@RequestBody OpenIn in) { return service.open(in.accountId(), in.balance(), in.notes(), "sistema"); }
    @PostMapping("/cash/close") public CashSession close(@RequestBody CloseIn in) { return service.close(in.real(), in.notes(), "sistema"); }
    @GetMapping("/cash/sessions") public List<CashSession> sessions() { return service.sessions(); }
    @PutMapping("/cash/sessions/{id}/verified") public CashSession verify(@PathVariable Long id, @RequestBody VerifyIn in) { return service.verify(id, in.verified()); }
    @PutMapping("/cash/sessions/{id}/note") public CashSession note(@PathVariable Long id, @RequestBody NoteIn in) { return service.note(id, in.note()); }

    @GetMapping("/settings") public Settings settings() { return settings.get(); }
    @PutMapping("/settings") public Settings update(@RequestBody Settings in) { return settings.update(in); }
}
