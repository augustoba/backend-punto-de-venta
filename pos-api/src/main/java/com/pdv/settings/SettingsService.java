package com.pdv.settings;

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
        return s;
    }
}
