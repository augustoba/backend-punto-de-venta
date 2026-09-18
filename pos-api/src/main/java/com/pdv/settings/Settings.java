package com.pdv.settings;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Ajustes del negocio (una sola fila). Gobiernan el comportamiento: arqueo de caja, descuento automático, etc. */
@Entity
@Table(name = "pos_settings")
public class Settings {
    @Id private Long id = 1L;
    // --- Mi negocio (sucursal) ---
    @Column(length = 9) private String businessColor = "#eeb37a";
    @Column(length = 200) private String address = "";
    @Column(length = 80) private String city = "";
    @Column(length = 40) private String phone = "";
    @Column(length = 160) private String contactEmail = "";
    // --- Comprobantes e impresión ---
    @Column(length = 12) private String receiptAction = "preguntar";   // nada | imprimir | preguntar
    @Column(length = 12) private String receiptFormat = "ticket80";    // a4 | ticket80 | ticket58
    @Column(length = 8) private String receiptQuality = "normal";      // normal | baja
    private boolean exchangeTicket;
    // --- Catálogo y finanzas (interruptores) ---
    private boolean productImages;
    private boolean services;
    private boolean weightSales;
    private boolean cashShipping;
    private boolean bankReconciliation;
    private boolean multiCurrency;
    @Column(length = 160) private String businessName = "Mi negocio";
    /** Logo del negocio como data URL (data:image/...;base64,...). Vacío = sin logo. */
    @Column(length = 700000) private String logo = "";
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
    public String getLogo() { return logo; }
    public void setLogo(String v) { logo = v == null ? "" : v; }
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

    public String getBusinessColor() { return businessColor; }
    public void setBusinessColor(String v) { businessColor = v == null ? "" : v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { address = v == null ? "" : v; }
    public String getCity() { return city; }
    public void setCity(String v) { city = v == null ? "" : v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { phone = v == null ? "" : v; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String v) { contactEmail = v == null ? "" : v; }
    public String getReceiptAction() { return receiptAction; }
    public void setReceiptAction(String v) { receiptAction = v == null ? "" : v; }
    public String getReceiptFormat() { return receiptFormat; }
    public void setReceiptFormat(String v) { receiptFormat = v == null ? "" : v; }
    public String getReceiptQuality() { return receiptQuality; }
    public void setReceiptQuality(String v) { receiptQuality = v == null ? "" : v; }
    public boolean isExchangeTicket() { return exchangeTicket; }
    public void setExchangeTicket(boolean v) { exchangeTicket = v; }
    public boolean isProductImages() { return productImages; }
    public void setProductImages(boolean v) { productImages = v; }
    public boolean isServices() { return services; }
    public void setServices(boolean v) { services = v; }
    public boolean isWeightSales() { return weightSales; }
    public void setWeightSales(boolean v) { weightSales = v; }
    public boolean isCashShipping() { return cashShipping; }
    public void setCashShipping(boolean v) { cashShipping = v; }
    public boolean isBankReconciliation() { return bankReconciliation; }
    public void setBankReconciliation(boolean v) { bankReconciliation = v; }
    public boolean isMultiCurrency() { return multiCurrency; }
    public void setMultiCurrency(boolean v) { multiCurrency = v; }
}
