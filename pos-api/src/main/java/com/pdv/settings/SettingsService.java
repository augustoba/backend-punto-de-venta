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
}
