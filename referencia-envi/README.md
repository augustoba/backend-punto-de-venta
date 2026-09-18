# Referencia Envi (maxkiosco.myenvi.website)

Relevamiento hecho el 2026-09-18 sobre un demo "maxkiosco" (acceso gratuito de 3 días). Sirve de guía de **vistas y lógica** para un POS.

## Cómo abrirlo
- Abrí cualquier archivo de `vistas/*.html` en el navegador (comparten `tokens.css` y `vistas/layout.js`, que arma el sidebar).
- Tema real de Envi: **claro** (fondo `#f3f1ed` + degradé cálido por "color del negocio", tarjetas de vidrio, botón principal `#21213b`, Montserrat). Las capturas oscuras del inicio eran de la extensión Dark Reader.

## Documentos
| Archivo | Contenido |
|---|---|
| `ANALISIS.md` | Navegación completa, patrón de pantalla, Ventas, Caja registradora, modal de cobro, Stock/producto |
| `ANALISIS_caja.md` | Apertura, cierre, arqueo, movimientos de caja, cierres de caja |
| `ANALISIS_presupuestos_descuentos.md` | Presupuestos y los 4 niveles de descuento (incl. automático por medio de pago) |
| `ANALISIS_facturas_compras.md` | Facturas de prueba y flujo completo de compras a proveedor |
| `ANALISIS_stock.md` | Listado, masivos, combos, carga masiva, historial de stock (libro mayor) |
| `ANALISIS_cuentas_corrientes.md` | Clientes, proveedores, transferencias de stock |
| `ANALISIS_tesoreria.md` | Cuentas, movimientos, cheques, centros de costos, categorías |

## Vistas HTML (`vistas/`)
01 Ventas · 02 Caja registradora · 03 Cobro · 04 Reportes (6 pestañas) · 05 Stock (+combo, masivos) · 06 Compras + Transferencias · 07 Cuentas y saldos (+cheques, centros de costos) · 08 Clientes y proveedores · 09 Empleados/horas/Mi cuenta/Suscripción · 10 Ajustes (4 pestañas) · 11 Categorías · 12 Cierres de caja (+apertura/cierre) · 13 Historiales · 14 Presupuestos y facturas.
Son réplicas estructurales (datos reales del demo), no copias pixel-perfect.

## Lo que NO se pudo ver / probar
- **Crear empleado**: el plan alcanzó su límite (solo se vio el formulario). Roles/permisos no visibles (solo "Es admin").
- **Emitir factura**: sin emisor configurado el botón no genera (validación de Emisor).
- **Transferencia de stock**: requiere 2+ depósitos (Ajustes > Depósitos = "Pedir activación").
- **Horas trabajadas**: el campo de hora del navegador no permitió cargar una jornada válida; se documentó la validación (salida > entrada).
- No se descargaron PDF/Excel ni se subieron archivos (carga masiva).
- Ajustes: no se abrieron todos los desplegables (formato de comprobante, "al cerrar una venta").
- Presupuesto convertido a venta: no generó movimiento de stock en el demo (posible bug/regla del demo).

## Datos de prueba que quedaron creados en el demo
Ventas #1 (Coca-Cola, efectivo) · #2 (Producto de prueba a cuenta corriente, Clara López) · #3 (Presupuesto→venta por transferencia, $900 con 10% automático) · Producto de prueba (stock 15) · Combo de prueba · Presupuesto (convertido) · Pedido P-1725 recibido (Pure Taste) · Cliente De Prueba (saldo $300) · Proveedor de prueba (saldo a pagar $1.000, remarcación 40%) · Cuenta de prueba · Transferencia Caja→Cuenta de prueba $1.000 · Egreso de caja $300 · Apertura/cierre de caja con diferencias · Cheque #02100 · Centro de costos "Gastos fijos del local" + costo fijo Alquiler $40.000 · Categoría de prueba. "Arqueo de caja" quedó otra vez **apagado**.

## Lógica clave (resumen ejecutivo)
1. Toda operación de dinero es una fila de un **libro de movimientos** por cuenta (caja/banco); el saldo es la suma.
2. Toda operación de stock es una fila de un **libro de stock** (venta, compra recibida, actualización manual, creación); se permite stock negativo.
3. **Cuenta corriente** por cliente/proveedor con saldo corrido; venta con "Está pago" apagado = deuda; recepción de compra sin pagar = deuda con proveedor; cobros/pagos eligen la cuenta destino.
4. Precios **con IVA incluido**; el neto y el IVA se calculan por división (1000 → 826,45 + 173,55).
5. Descuentos: por línea (monto/%/precio final), global (%), automático por medio de pago (10% transferencia), promociones.
6. **Arqueo de caja** opcional: abrir con saldo declarado (difiere del cierre anterior ⇒ asiento de diferencia), operar, cerrar comparando esperado vs. real (diferencia ⇒ asiento), y verificación por el dueño.
7. Compras en 3 estados: Borrador → Pedido → Recibido (con recepción parcial); el costo del catálogo y el stock cambian **solo** al recibir.
8. Presupuesto → "Convertir a venta" reutiliza el modal de cobro.
9. Rentabilidad = ingresos − costo variable − costo fijo asignado (centros de costos con 4 criterios de reparto).
10. Planes/límites: cantidad de empleados, cupo de facturas, funciones "Pedir activación" (depósitos, lotes).

## Comparación rápida con este backend (`src/main/java/com/saasweb`)
Ya existen: órdenes POS/WEB, turnos (`shift`), descuentos/cupones, ARCA, notas de crédito, cambios, proveedores, gastos y presupuestos de gastos, planes. Faltan o conviene revisar: listas de precios (Principal/Mayorista), descuento por línea con 3 modos, descuento automático por medio de pago, cuentas (caja/banco) + libro de movimientos + cheques, cuenta corriente de clientes con saldo corrido, pedidos de compra en 3 estados con recepción, libro de movimientos de stock con tipos, combos, centros de costos + rentabilidad por categoría, verificación de cierres de caja, historial de precios.
