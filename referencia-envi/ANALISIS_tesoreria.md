# Cuentas y saldos / Tesorería (/accounting) — probado

## Pantalla
- KPIs (5 tarjetas): **Cajas** ($ suma de cuentas tipo caja), **Bancos** ($ tipo banco), **Cuenta corriente** (A cobrar clientes / A pagar proveedores, cada uno con ›), **Cheques** (A cobrar / A pagar, con ›), **Distribución del saldo** (total + barra Cajas % / Bancos %).
- "CUENTAS · N ACTIVAS": tarjetas por cuenta (nombre, saldo, "Ver movimientos", "Editar"; color propio) + botón **Nueva cuenta**.
- Toolbar del libro: buscar, Filtrar, refrescar, **Centros de costos**, **Crear movimiento ▾** (Registrar ingreso · Registrar egreso · Movimiento entre cuentas · Importar desde Excel).
- Libro de movimientos: fecha+hora, valor (+ verde / − rojo), **categoría** (chip con emoji), cuenta, descripción (con ícono para "Agregar descripción"), creación (usuario). Selector de columnas.

## Asientos AUTOMÁTICOS observados (todos van al libro)
| Origen | Categoría | Cuenta | Detalle |
|---|---|---|---|
| Cobro a cliente (cuenta corriente) | Cuenta corriente clientes | la elegida en "medio de pago" | "Cobro de Cliente De Prueba" +$200 |
| Apertura de caja con saldo distinto | Diferencia al abrir caja | Caja | +$5.200 |
| Cierre de caja con sobrante/faltante | Diferencia al cerrar caja | Caja | +$100 |
| Egreso manual desde la caja | Otros gastos del local › Insumos de limpieza | Caja | −$300 |
| Transferencia entre cuentas | Transferencia entre cuentas propias | origen (−) y destino (+) | "→ Transferido a Cuenta de prueba"; dos filas, total neutro |
Regla: **cada movimiento de dinero (ventas cobradas, pagos, gastos, cierres) es una fila del libro** y el saldo de cada cuenta = suma de sus filas.

## Nueva cuenta (modal "Crear cuenta")
Saldo nuevo (inicial), Nombre de la cuenta, Tipo de cuenta (**Caja** | **Banco**), Color (#hex), Notas internas ("Quién la usa, en qué sucursal, restricciones…"). Al crearla aparece como tarjeta.

## Crear movimiento
- **Ingreso / Egreso** (modal "Nuevo movimiento"): banner de tipo, Cuenta, Importe, Fecha (datetime), texto en vivo "Nuevo total en la cuenta X", Categoría + Subcategoría (obligatoria), Descripción.
- **Movimiento entre cuentas**: Cuenta origen, Importe, Cuenta destino (excluye el origen), Descripción opcional; chips en vivo "Nuevo valor: $4.000,00" bajo cada cuenta. Permite saldo negativo.
- Importar desde Excel.

## Cheques (modal desde el KPI)
- Pestañas **A pagar / A cobrar**, buscador, chip "Total a cobrar $", botones refrescar / **Resumen** / **Nuevo cheque**.
- Tabla: vencimiento, valor, **Cobrado** (switch + chip Sí/No), número (autonumerado #02100), cliente del local, editar/borrar.
- **Nuevo cheque**: toggle A pagar / A cobrar, Valor, Vencimiento (**obligatorio**, datepicker en español lunes→domingo), Descripción, Cliente (Sin definir).
- **Resumen de cheques**: selector de año, "Balance del año", cuadros "Cheques recibidos – pendiente de cobro" y "Cheques entregados – pendiente de pago" con filas por mes (a cobrar, total, mes, botón Ver).

## Centros de costos (`/accounting/cost-centers`) — alimenta Reportes > Rentabilidad
- Vacío: "Agrupá tus costos fijos (alquiler, sueldos, luz) y elegí cómo se reparten entre tus servicios y categorías de productos para conocer la rentabilidad real de cada uno."
- **Nuevo centro de costos**: Nombre + criterio de reparto: *Partes iguales* · *Por horas vendidas* (default) · *Por facturación* · *Porcentajes manuales*.
- Cada centro muestra nombre (editar/borrar), descripción del criterio ("Por facturación — el total se reparte proporcional a lo que facturó cada servicio o categoría en el mes"), **Total mensual**, botón **+ Nuevo costo fijo** y tabla (nombre, notas, monto mensual, acciones).
- **Nuevo costo fijo**: Nombre, Monto mensual, Categoría contable (selector), Notas. Ej.: Alquiler del local $40.000.

## Categorías (/categories)
Árbol categoría → subcategorías con emoji, compartido por tesorería y catálogo ("Organizá tus movimientos con categorías y subcategorías"). Sembrado del sistema: Ventas › Ventas online; Transportes; Servicios Administrativos y Financieros; Servicios (Agua, Electricidad, Gas, Internet, Software); Seguros; Saldo inicial; Proveedores › Compra a proveedores; Personal; Otros gastos del local; Marketing y publicidad; Impuestos (IVA, Ingresos brutos, Monotributo…); Empleados › Sueldos; Cuentas corrientes (clientes/proveedores); Consumibles; Alquileres; Ajustes y diferencias de caja (Diferencia al abrir/cerrar caja, Diferencias de caja, Transferencia entre cuentas propias).

## Modelo sugerido
`account(id, name, type[cash|bank], color, notes, active)`; `account_movement(id, account_id, amount±, occurred_at, category_id, subcategory_id, description, source_type, source_id, user_id)`; `cheque(id, kind[receivable|payable], amount, due_date, number, collected, customer_id, description)`; `cost_center(id, name, allocation[equal|hours|revenue|manual])`, `fixed_cost(cost_center_id, name, monthly_amount, category_id, notes)`.
