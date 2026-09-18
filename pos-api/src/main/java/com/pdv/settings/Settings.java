package com.pdv.settings;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Ajustes del negocio (una sola fila). Gobiernan el comportamiento: arqueo de caja, descuento automático, etc. */
@Entity
@Table(name = "pos_settings")
public class Settings {
    @Id private Long id = 1L;
    @Column(length = 160) private String businessName = "Mi negocio";
    private boolean arqueo = false;
    @Column(precision = 14, scale = 2) private BigDecimal alertDiff = new BigDecimal("2000");
    private boolean createProductFromCash = true;
    private boolean hideStockFilter = false;
    private boolean cumulativeDiscounts = false;
    @Column(precision = 5, scale = 2) private BigDecimal sellerCommission = new BigDecimal("10");
    /** % de descuento automático al cobrar por transferencia (0 = desactivado). */
    @Column(precision = 5, scale = 2) private BigDecimal transferDiscount = BigDecimal.ZERO;
    @Column(precision = 5, scale = 2) private BigDecimal defaultIva = new BigDecimal("21");
    @Column(precision = 5, scale = 2) private BigDecimal markup = new BigDecimal("40");
    private boolean hideOutOfStock = false;

    public Long getId() { return id; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String v) { businessName = v; }
    public boolean isArqueo() { return arqueo; }
    public void setArqueo(boolean v) { arqueo = v; }
    public BigDecimal getAlertDiff() { return alertDiff; }
    public void setAlertDiff(BigDecimal v) { alertDiff = v; }
    public boolean isCreateProductFromCash() { return createProductFromCash; }
    public void setCreateProductFromCash(boolean v) { createProductFromCash = v; }
    public boolean isHideStockFilter() { return hideStockFilter; }
    public void setHideStockFilter(boolean v) { hideStockFilter = v; }
    public boolean isCumulativeDiscounts() { return cumulativeDiscounts; }
    public void setCumulativeDiscounts(boolean v) { cumulativeDiscounts = v; }
    public BigDecimal getSellerCommission() { return sellerCommission; }
    public void setSellerCommission(BigDecimal v) { sellerCommission = v; }
    public BigDecimal getTransferDiscount() { return transferDiscount; }
    public void setTransferDiscount(BigDecimal v) { transferDiscount = v; }
    public BigDecimal getDefaultIva() { return defaultIva; }
    public void setDefaultIva(BigDecimal v) { defaultIva = v; }
    public BigDecimal getMarkup() { return markup; }
    public void setMarkup(BigDecimal v) { markup = v; }
    public boolean isHideOutOfStock() { return hideOutOfStock; }
    public void setHideOutOfStock(boolean v) { hideOutOfStock = v; }
}
