package com.pdv.reports;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {
    private final ReportsService service;
    public ReportsController(ReportsService s) { this.service = s; }

    @GetMapping("/sales-by-method")
    public List<ReportsService.ByMethod> byMethod(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                                  @RequestParam(defaultValue = "false") boolean onlyPaid) { return service.salesByMethod(from, to, onlyPaid); }

    @GetMapping("/product-ranking")
    public List<ReportsService.ProductRow> ranking(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) { return service.productRanking(from, to); }
}
