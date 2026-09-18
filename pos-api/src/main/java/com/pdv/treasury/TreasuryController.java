package com.pdv.treasury;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** API de tesorería: cuentas, libro de movimientos, transferencias y categorías. */
@RestController
@RequestMapping("/api")
public class TreasuryController {
    private final TreasuryService service;
    private final CategoryRepository categories;

    public TreasuryController(TreasuryService s, CategoryRepository c) { this.service = s; this.categories = c; }

    public record AccountOut(Long id, String name, Account.Type type, String color, String notes, BigDecimal balance) {}
    public record AccountIn(@NotBlank String name, @NotNull Account.Type type, String color, String notes, BigDecimal initial) {}
    public record MovementIn(@NotNull Long accountId, @NotNull BigDecimal amount, @NotBlank String category, String subcategory, String description, Instant at) {}
    public record TransferIn(@NotNull Long fromId, @NotNull Long toId, @NotNull BigDecimal amount, String description) {}
    public record CategoryIn(@NotBlank String name, String emoji, String color, List<String> subcategories) {}

    private AccountOut out(Account a) { return new AccountOut(a.getId(), a.getName(), a.getType(), a.getColor(), a.getNotes(), service.balance(a.getId())); }

    @GetMapping("/accounts") public List<AccountOut> accounts() { return service.accounts().stream().map(this::out).toList(); }

    @PostMapping("/accounts")
    public AccountOut createAccount(@RequestBody AccountIn in) {
        return out(service.createAccount(in.name(), in.type(), in.color(), in.notes(), in.initial(), "sistema"));
    }

    @PutMapping("/accounts/{id}")
    public AccountOut updateAccount(@PathVariable Long id, @RequestBody AccountIn in) {
        return out(service.updateAccount(id, in.name(), in.type(), in.color(), in.notes()));
    }

    @PutMapping("/categories/{id}")
    public Category updateCategory(@PathVariable Long id, @RequestBody CategoryIn in) {
        return service.updateCategory(id, in.name(), in.emoji(), in.color(), in.subcategories());
    }

    @GetMapping("/movements")
    public List<Movement> movements(@RequestParam(required = false) Long accountId) { return service.movements(accountId); }

    @PostMapping("/movements")
    public Movement addMovement(@RequestBody MovementIn in) {
        return service.addMovement(in.accountId(), in.amount(), in.category(), in.subcategory(), in.description(), in.at(), "manual", "", "sistema");
    }

    @PostMapping("/transfers")
    public List<Movement> transfer(@RequestBody TransferIn in) { return service.transfer(in.fromId(), in.toId(), in.amount(), in.description(), "sistema"); }

    @GetMapping("/categories") public List<Category> categories() { return categories.findAll(); }

    @PostMapping("/categories")
    public Category createCategory(@RequestBody CategoryIn in) {
        return categories.save(new Category(in.name(), in.emoji() == null ? "" : in.emoji(), in.color() == null ? "#888888" : in.color(), in.subcategories()));
    }

    @DeleteMapping("/categories/{id}") public void deleteCategory(@PathVariable Long id) { categories.deleteById(id); }
}
