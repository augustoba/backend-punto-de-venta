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
| B9 | Login y usuarios (`com.pdv.auth`) | — | ✅ |
| B10 | Depósitos y transferencias de stock (`com.pdv.warehouses`) | — | ✅ |
| B11 | Listas de precios (`PriceList`, `priceListId` en la venta) | — | ✅ |
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
- **Nota MySQL** (2026-09-18): las pruebas corren en H2 y no detectan palabras reservadas de MySQL. Al probar contra MySQL real falló `pos_cash_session` por la columna `real` (ahora `real_amount`); `day` ya se había renombrado a `work_day`. Al agregar columnas conviene evitar `real`, `day`, `order`, `group`, `key`, `rank`, `condition`.
- **B9** (2026-09-18): `com.pdv.auth`: `AppUser` (ADMIN/VENDEDOR, activo), contraseñas con PBKDF2-HMAC-SHA256 y sal (`PasswordHasher`), token firmado HMAC-SHA256 sin estado (`TokenService`, 12 h), `AuthFilter` que exige `Authorization: Bearer` en `/api/**` (menos `/api/auth/login` y OPTIONS) y `CurrentUser` que reemplaza al "sistema" en asientos, ventas, movimientos, etc. Al primer arranque crea el usuario `admin` con la contraseña `ADMIN_PASSWORD` (por defecto `admin`: **cambiala**). Endpoints: `POST /api/auth/login`, `PUT /api/auth/password`, `GET/POST /api/users`, `PUT /api/users/{id}/active` (los tres últimos sólo admin; no deja desactivar al único admin). Variables: `AUTH_ENABLED` (true), `AUTH_SECRET` (cambiar en producción), `ADMIN_PASSWORD`. 7 pruebas (49 en total); en las pruebas la autenticación va apagada y el filtro se verificó por HTTP contra la API levantada.
- **B10** (2026-09-18): `com.pdv.warehouses`: depósitos (el «Principal» se crea solo) y transferencias entre ellos. El stock total del producto no se toca: el principal es «total − lo que hay en los otros», así ventas y compras siguen descontando del principal sin cambios; los demás depósitos guardan su cantidad en `pos_stock_level`. Valida origen≠destino, cantidades positivas, productos repetidos, combos (su stock sale de los componentes) y que alcance el stock en el origen. Endpoints: `GET/POST /api/warehouses`, `PUT /api/warehouses/{id}`, `GET /api/warehouses/stock`, `GET/POST /api/stock-transfers`. 6 pruebas (55 en total).
- **B11** (2026-09-18): listas de precios en `com.pdv.catalog`: «Principal» (0 %, siempre existe, no se edita ni se borra) y las que se creen con un ajuste porcentual sobre el precio base (Mayorista −15, Recargo +10). `SaleIn` acepta `priceListId`: en una lista se usa `precio × (1+%)` e ignora la oferta; un precio explícito de la línea gana sobre la lista; la venta guarda la lista usada. Endpoints: `GET/POST /api/price-lists`, `PUT/DELETE /api/price-lists/{id}`. 5 pruebas (60 en total).
- **B12** (2026-09-18): logo del negocio en Ajustes (`logo`, data URL PNG/JPG/WebP de hasta ~450 KB; se valida en `SettingsService`). 2 pruebas (62 en total).
- **B13** (2026-09-18): las facturas guardan quién las emitió (`username`) y numeran `000001-NNNNNN` como Envi.
- **B14** (2026-09-18): ajustes nuevos para igualar Envi: color del negocio, contacto y ubicación (dirección, ciudad, teléfono, email), comprobantes e impresión (`receiptAction` nada/imprimir/preguntar, `receiptFormat` a4/ticket80/ticket58, `receiptQuality`, `exchangeTicket`) e interruptores de catálogo y finanzas (`productImages`, `services`, `weightSales`, `cashShipping`, `bankReconciliation`, `multiCurrency`). Las opciones inválidas vuelven al valor por defecto. 2 pruebas (64 en total).
- **B15** (2026-09-18): foto de producto (`image`, data URL PNG/JPG/WebP de hasta ~180 KB; en la actualización `null` conserva la foto y `""` la quita). 1 prueba (65 en total).
