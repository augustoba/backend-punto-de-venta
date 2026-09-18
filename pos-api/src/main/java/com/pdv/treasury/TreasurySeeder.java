package com.pdv.treasury;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Datos de base (NO de ejemplo): las categorías estándar, que usan los asientos automáticos, y una Caja y un Banco vacíos. */
@Component
public class TreasurySeeder implements ApplicationRunner {
    private final CategoryRepository categories;
    private final AccountRepository accounts;

    public TreasurySeeder(CategoryRepository c, AccountRepository a) { this.categories = c; this.accounts = a; }

    @Override
    public void run(ApplicationArguments args) {
        if (categories.count() == 0) {
            add("Ventas", "🛒", "#a267ac", "Ventas del local");
            add("Transportes", "🚗", "#f4a340", "Combustible vehículos", "Mantenimiento de vehículos", "Servicios de flete y envíos");
            add("Servicios Administrativos y Financieros", "💳", "#1f5fb0", "Comisiones de tarjetas", "Honorarios contables", "Servicios y gastos bancarios");
            add("Servicios", "📡", "#2cbfa5", "Agua", "Electricidad", "Gas", "Internet y telefonía", "Software y licencias");
            add("Seguros", "💰", "#e5476b", "Seguro local", "Seguro empleados", "Seguro otros");
            add("Saldo inicial", "🤝", "#8fc24a");
            add("Proveedores", "🚚", "#ef7c3a", "Compra a proveedores");
            add("Personal", "👤", "#5aa9e6", "Capacitaciones", "Comisiones de ventas");
            add("Otros gastos del local", "💡", "#f2c531", "Insumos de limpieza", "Mantenimiento del local", "Servicios de limpieza");
            add("Marketing y publicidad", "📰", "#d970c0", "Publicidad Online", "Publicidad Offline");
            add("Impuestos", "💸", "#666666", "IVA", "Ingresos brutos", "Monotributo");
            add("Empleados", "👥", "#1f8fa8", "Sueldos empleados");
            add("Cuentas corrientes", "🤝", "#6fc2a5", "Cuenta corriente clientes", "Cuenta corriente proveedores");
            add("Consumibles", "📦", "#8a8f3c", "Bolsas y Packaging", "Papelería y librería");
            add("Alquileres", "🏢", "#aaaaaa", "Alquiler del local");
            add("Ajustes y diferencias de caja", "💰", "#e0921a", "Diferencia al abrir caja", "Diferencia al cerrar caja", "Diferencias de caja", "Transferencia entre cuentas propias");
        }
        if (accounts.count() == 0) {
            accounts.save(new Account("Caja", Account.Type.CAJA, "#8fc24a", ""));
            accounts.save(new Account("Banco", Account.Type.BANCO, "#5aa9e6", ""));
        }
    }

    private void add(String name, String emoji, String color, String... subs) { categories.save(new Category(name, emoji, color, List.of(subs))); }
}
