package com.pdv.treasury;

import com.pdv.common.BusinessException;
import com.pdv.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Reglas de tesorería (réplica de Envi, ver referencia-envi/ANALISIS_tesoreria.md):
 * el saldo de una cuenta es la suma de sus movimientos; toda operación de dinero deja una fila.
 */
@Service
@Transactional
public class TreasuryService {
    public static final String CAT_ADJUST = "Ajustes y diferencias de caja";
    public static final String SUB_TRANSFER = "Transferencia entre cuentas propias";

    private final AccountRepository accounts;
    private final MovementRepository movements;
    private final CategoryRepository categories;

    public TreasuryService(AccountRepository a, MovementRepository m, CategoryRepository c) {
        this.accounts = a; this.movements = m; this.categories = c;
    }

    public Account createAccount(String name, Account.Type type, String color, String notes, BigDecimal initial, String user) {
        if (name == null || name.isBlank()) throw new BusinessException("La cuenta necesita un nombre");
        Account acc = accounts.save(new Account(name.trim(), type, color, notes));
        if (initial != null && initial.signum() != 0) {
            movements.save(new Movement(acc.getId(), initial, Instant.now(), "Saldo inicial", "", "Saldo inicial", "saldo", "", user));
        }
        return acc;
    }

    public Account account(Long id) { return accounts.findById(id).orElseThrow(() -> new NotFoundException("Cuenta", id)); }
    public List<Account> accounts() { return accounts.findAll(); }
    public BigDecimal balance(Long accountId) { return movements.balance(accountId); }

    /** Ingreso (amount > 0) o egreso (amount < 0). Si la categoría tiene subcategorías, la subcategoría es obligatoria. */
    public Movement addMovement(Long accountId, BigDecimal amount, String category, String subcategory, String description,
                                Instant at, String sourceType, String sourceId, String user) {
        account(accountId);
        if (amount == null || amount.signum() == 0) throw new BusinessException("El importe no puede ser cero");
        if (category == null || category.isBlank()) throw new BusinessException("Elegí una categoría");
        categories.findByName(category).ifPresent(c -> {
            if (!c.getSubcategories().isEmpty() && (subcategory == null || subcategory.isBlank()))
                throw new BusinessException("Elegí una subcategoría");
        });
        return movements.save(new Movement(accountId, amount, at == null ? Instant.now() : at, category, subcategory,
                description, sourceType == null ? "manual" : sourceType, sourceId, user));
    }

    /** Movimiento interno entre cuentas propias: dos filas (−origen, +destino) que no cambian el total. */
    public List<Movement> transfer(Long fromId, Long toId, BigDecimal amount, String description, String user) {
        if (fromId.equals(toId)) throw new BusinessException("El origen y el destino no pueden ser la misma cuenta");
        if (amount == null || amount.signum() <= 0) throw new BusinessException("El importe debe ser mayor a cero");
        Account from = account(fromId), to = account(toId);
        Instant now = Instant.now();
        Movement in = movements.save(new Movement(toId, amount, now, CAT_ADJUST, SUB_TRANSFER,
                blank(description) ? "← Transferido desde " + from.getName() : description, "transferencia", "", user));
        Movement out = movements.save(new Movement(fromId, amount.negate(), now, CAT_ADJUST, SUB_TRANSFER,
                blank(description) ? "→ Transferido a " + to.getName() : description, "transferencia", "", user));
        return List.of(out, in);
    }

    public List<Movement> movements(Long accountId) {
        return accountId == null ? movements.findAllByOrderByOccurredAtDescIdDesc()
                : movements.findByAccountIdOrderByOccurredAtDescIdDesc(accountId);
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }
}
