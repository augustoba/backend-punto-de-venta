package com.pdv.returns;

import com.pdv.auth.CurrentUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReturnsController {
    private final ReturnsService service;
    public ReturnsController(ReturnsService s) { this.service = s; }

    public record ReturnIn(Long saleId, List<ReturnsService.LineIn> lines, String reason, SaleReturn.Refund refund) {}

    @GetMapping("/returns") public List<SaleReturn> list() { return service.list(); }
    @GetMapping("/sales/{id}/returnable") public List<ReturnsService.Returnable> returnable(@PathVariable Long id) { return service.returnable(id); }
    @PostMapping("/returns") public SaleReturn register(@RequestBody ReturnIn in) { return service.register(in.saleId(), in.lines(), in.reason(), in.refund(), CurrentUser.name()); }
}
