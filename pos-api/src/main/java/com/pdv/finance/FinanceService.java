package com.pdv.finance;

import com.pdv.common.BusinessException;
import com.pdv.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Cheques y centros de costos (ver referencia-envi/ANALISIS_tesoreria.md). */
@Service
@Transactional
public class FinanceService {
    private final ChequeRepository cheques;
    private final CostCenterRepository centers;

    public FinanceService(ChequeRepository c, CostCenterRepository cc) { this.cheques = c; this.centers = cc; }

    public List<Cheque> cheques(Cheque.Kind kind) { return cheques.findByKindOrderByDueAsc(kind); }

    public Cheque saveCheque(Long id, Cheque.Kind kind, BigDecimal amount, java.time.LocalDate due, Long customerId, String description) {
        if (amount == null || amount.signum() <= 0) throw new BusinessException("El valor del cheque debe ser mayor a cero");
        if (due == null) throw new BusinessException("El vencimiento es obligatorio");
        Cheque c = id == null ? new Cheque() : cheque(id);
        c.setKind(kind); c.setAmount(amount); c.setDue(due); c.setCustomerId(customerId); c.setDescription(description);
        c = cheques.save(c);
        if (id == null) c.setNumber("#" + String.format("%05d", 2000 + c.getId()));   // numeración automática
        return c;
    }
    public Cheque cheque(Long id) { return cheques.findById(id).orElseThrow(() -> new NotFoundException("Cheque", id)); }
    public Cheque setCollected(Long id, boolean collected) { Cheque c = cheque(id); c.setCollected(collected); return c; }
    public void deleteCheque(Long id) { cheques.delete(cheque(id)); }

    /** Total pendiente (no cobrado / no pagado) por tipo. */
    public BigDecimal pending(Cheque.Kind kind) {
        return cheques.findByKindOrderByDueAsc(kind).stream().filter(c -> !c.isCollected()).map(Cheque::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<CostCenter> centers() { return centers.findAll(); }
    public CostCenter createCenter(String name, CostCenter.Allocation allocation) {
        if (name == null || name.isBlank()) throw new BusinessException("El centro de costos necesita un nombre");
        CostCenter c = new CostCenter(); c.setName(name.trim()); c.setAllocation(allocation == null ? CostCenter.Allocation.HORAS : allocation);
        return centers.save(c);
    }
    public CostCenter center(Long id) { return centers.findById(id).orElseThrow(() -> new NotFoundException("Centro de costos", id)); }
    public void deleteCenter(Long id) { centers.delete(center(id)); }
    public CostCenter addFixedCost(Long centerId, String name, BigDecimal monthly, String notes) {
        if (name == null || name.isBlank() || monthly == null || monthly.signum() <= 0) throw new BusinessException("El costo fijo necesita nombre y un monto mensual mayor a cero");
        CostCenter c = center(centerId);
        CostCenter.FixedCost f = new CostCenter.FixedCost(); f.name = name.trim(); f.monthly = monthly; f.notes = notes == null ? "" : notes;
        c.getCosts().add(f);
        return c;
    }
    public CostCenter removeFixedCost(Long centerId, int index) { CostCenter c = center(centerId); c.getCosts().remove(index); return c; }
}
