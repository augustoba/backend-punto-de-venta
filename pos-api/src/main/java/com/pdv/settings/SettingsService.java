package com.pdv.settings;

import com.pdv.common.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SettingsService {
    private final SettingsRepositoryHolder repo;

    public SettingsService(SettingsRepositoryHolder repo) { this.repo = repo; }

    /** Devuelve los ajustes; los crea con los valores por defecto la primera vez. */
    public Settings get() { return repo.findById(1L).orElseGet(() -> repo.save(new Settings())); }

    public Settings update(Settings in) {
        Settings s = get();
        s.setBusinessName(in.getBusinessName()); s.setArqueo(in.isArqueo()); s.setAlertDiff(in.getAlertDiff());
        s.setCreateProductFromCash(in.isCreateProductFromCash()); s.setHideStockFilter(in.isHideStockFilter());
        s.setCumulativeDiscounts(in.isCumulativeDiscounts()); s.setSellerCommission(in.getSellerCommission());
        s.setTransferDiscount(in.getTransferDiscount()); s.setDefaultIva(in.getDefaultIva()); s.setMarkup(in.getMarkup());
        s.setHideOutOfStock(in.isHideOutOfStock());
        s.setLogo(validLogo(in.getLogo()));
        s.setBusinessColor(validColor(in.getBusinessColor())); s.setAddress(in.getAddress()); s.setCity(in.getCity()); s.setPhone(in.getPhone()); s.setContactEmail(in.getContactEmail());
        s.setReceiptAction(oneOf(in.getReceiptAction(), "preguntar", "nada", "imprimir", "preguntar"));
        s.setReceiptFormat(oneOf(in.getReceiptFormat(), "ticket80", "a4", "ticket80", "ticket58"));
        s.setReceiptQuality(oneOf(in.getReceiptQuality(), "normal", "normal", "baja"));
        s.setExchangeTicket(in.isExchangeTicket()); s.setProductImages(in.isProductImages()); s.setServices(in.isServices()); s.setWeightSales(in.isWeightSales());
        s.setCashShipping(in.isCashShipping()); s.setBankReconciliation(in.isBankReconciliation()); s.setMultiCurrency(in.isMultiCurrency());
        return s;
    }

    /** Sólo imágenes en data URL y de tamaño acotado (el front la reduce antes de subirla). */
    static String validLogo(String logo) {
        if (logo == null || logo.isBlank()) return "";
        if (!logo.startsWith("data:image/png;base64,") && !logo.startsWith("data:image/jpeg;base64,") && !logo.startsWith("data:image/webp;base64,"))
            throw new BusinessException("El logo debe ser una imagen PNG, JPG o WebP");
        if (logo.length() > 600_000) throw new BusinessException("El logo es demasiado pesado (máximo ~450 KB)");
        return logo;
    }

    /** Color del negocio: #RRGGBB; si viene vacío o inválido se usa el naranja de siempre. */
    static String validColor(String c) { return c != null && c.matches("#[0-9a-fA-F]{6}") ? c : "#eeb37a"; }

    /** Una de las opciones permitidas; si no, la que se indica como valor por defecto. */
    static String oneOf(String v, String def, String... opciones) { for (String o : opciones) if (o.equals(v)) return v; return def; }
}
