# pos-api — API del punto de venta

Backend **nuevo y limpio** del punto de venta (proyecto individual; sin relación con el ecommerce). Réplica de la lógica de Envi.
Referencia: `../referencia-envi/` (README, `ANALISIS_*.md`). El front: `../../frontend-punto-de-venta` (Angular 19).

> Nota histórica: la carpeta `backend-punto-de-venta` todavía contiene el backend heredado del ecommerce (Estilos Pequeños) en su raíz (`src/`, `pom.xml`). No se usa. Recuperable y borrable cuando se decida (etiqueta git `base-ecommerce-antes-de-recrear`). Esta API vive aparte en `pos-api/` y usa tablas con prefijo `pos_` en la base `puntoventa`.

## Correr
```
cd pos-api
./mvnw spring-boot:run        # http://localhost:8081  (MySQL local, DB puntoventa; se crea sola)
./mvnw test                   # pruebas con H2 en memoria
```
Variables: `PORT`, `DB_USER`, `DB_PASSWORD`, `CORS_ORIGINS`.

## Reglas de diseño
- **Saldo = suma de movimientos.** Nunca se guarda un saldo; cada operación de dinero deja una fila en `pos_movement`.
- Errores de negocio → HTTP 400 `{"error": "..."}`; no encontrado → 404.
- Dinero en `BigDecimal(14,2)`; fechas en UTC (`Instant`).
- Sin datos de ejemplo: el arranque solo siembra las categorías estándar y una Caja y un Banco vacíos.

## Partes (back)
| # | Módulo | Referencia | Estado |
|---|---|---|---|
| B1 | Esqueleto + **tesorería**: cuentas, libro de movimientos, transferencias, categorías | `ANALISIS_tesoreria.md` | ✅ |
| B2 | Catálogo: productos, combos, libro de stock, historial de precios | `ANALISIS_stock.md` | ✅ |
| B3 | Clientes y proveedores + cuenta corriente (libro con saldo corrido) | `ANALISIS_cuentas_corrientes.md` | ✅ |
| B4 | Ventas + caja (apertura/cierre/arqueo) + descuentos + ajustes | `ANALISIS.md`, `ANALISIS_caja.md`, `ANALISIS_presupuestos_descuentos.md` | ✅ |
| B5 | Compras (borrador → pedido → recibido) y recepción | `ANALISIS_facturas_compras.md` | ✅ |
| B6 | Presupuestos, facturas, cheques, centros de costos | idem | ✅ |
| B7 | Empleados/horas, ajustes, reportes | `ANALISIS_*.md` | ✅ |
| F11 | Conectar el front a la API (hoy el front guarda en `localStorage`) | — | ⬜ |

## Endpoints (B1)
| Método | Ruta | Qué |
|---|---|---|
| GET/POST | `/api/accounts` | Cuentas con saldo; alta con saldo inicial opcional |
| GET/POST | `/api/movements[?accountId=]` | Libro de movimientos; ingreso/egreso (subcategoría obligatoria si la categoría tiene) |
| POST | `/api/transfers` | Movimiento entre cuentas: 2 filas, neutro en el total |
| GET/POST/DELETE | `/api/categories` | Categorías y subcategorías |

## Bitácora
- **B1** (2026-09-18): proyecto Spring Boot 3.3.5 / Java 21; entidades `Account`, `Movement`, `Category`; `TreasuryService` con las reglas; controlador; seeder de categorías; 5 pruebas (`TreasuryServiceTest`) en verde con H2.
- **B2** (2026-09-18): paquete `com.pdv.catalog`: `Product` (combo con `ComboItem`, stock puede ser negativo), `StockMove` (libro de stock), `PriceChange`, `CatalogService` (`move` es el único punto que cambia stock; `consume` baja componentes de un combo; `setStock`, `bulkPrice`, archivar/eliminar), `CatalogController` (`/api/products`, `/api/stock-moves`, `/api/price-changes`). 6 pruebas. Nota: los repositorios de Spring Data deben ser interfaces de primer nivel (las anidadas no se registran).
- **B3** (2026-09-18): `com.pdv.parties`: `Customer`, `Supplier`, `PartyEntry` (libro de cuenta corriente; saldo = suma), `PartyService` (`addSaleDebt`/`addPurchaseDebt` para ventas y compras; `customerMovement` pago/devolución/deuda y `supplierMovement` pago/compra/nota de crédito/ajuste, donde pago y devolución impactan en el libro de tesorería), `PartyController` (`/api/customers`, `/api/suppliers`, `/ledger`, `/movements`, `/api/accounts-receivable-payable`). 6 pruebas.
- **B4** (2026-09-18): `com.pdv.sales` (`Sale` con líneas que congelan precio/descuento/costo, `CashSession`, `SalesService`, `SalesController`) y `com.pdv.settings` (`Settings` de una sola fila, gobierna arqueo y descuento automático por transferencia). Reglas: baja de stock (combos descuentan componentes), deuda si no está paga (exige cliente), asiento en la caja abierta o el banco, descuento automático por transferencia, anulación (devuelve stock y quita asientos), apertura con «Diferencia al abrir caja», cierre con esperado (= saldo del libro) vs real y «Diferencia al cerrar caja», verificación del turno. Endpoints: `/api/sales`, `/api/cash/*`, `/api/settings`. 10 pruebas.
- **B5** (2026-09-18): `com.pdv.purchasing`: `Purchase` (BORRADOR/PEDIDO/RECIBIDO, líneas con costo anterior para mostrar variación), `PurchasingService` (`save`, `markOrdered`, `receive`: sube stock, actualiza costo con rastro en el historial de precios, egreso de la cuenta si está pago o deuda con el proveedor; un pedido recibido no se edita ni elimina), `/api/purchases`. 4 pruebas.
- **B6** (2026-09-18): `com.pdv.billing` (`Budget` ACTIVO/VENDIDO/RECHAZADO/ARCHIVADO con detección de vencido, `Invoice` Factura C de prueba con neto/IVA, `BillingService`) y `com.pdv.finance` (`Cheque` a cobrar/pagar con numeración automática y total pendiente, `CostCenter` con costos fijos y 4 criterios de reparto, `FinanceService`). `SalesService` ahora emite la factura si se pide y marca el presupuesto como VENDIDO al convertirlo. Endpoints: `/api/budgets`, `/api/invoices`, `/api/cheques`, `/api/cost-centers`. 6 pruebas.
- **B7** (2026-09-18): `com.pdv.staff` (`Employee` ADMIN/VENDEDOR, `Shift` fichaje con horas calculadas, valida salida posterior a entrada) y `com.pdv.reports` (ventas por medio de pago y ranking de productos con ganancia sobre el costo congelado de cada línea). Ajustes ya estaban en `/api/settings` (B4). Endpoints: `/api/employees`, `/api/shifts`, `/api/reports/sales-by-method`, `/api/reports/product-ranking`. La columna del día se llama `work_day` porque `day` es reservada en H2. 4 pruebas (41 en total).
- **B8** (2026-09-18): `PUT /api/accounts/{id}` y `PUT /api/categories/{id}` (editar cuenta y categoría con sus subcategorías), que el front necesita para operar contra la API. 1 prueba (42 en total).
