# Cuentas corrientes: Clientes y Proveedores (probado)

## Clientes (/customers)
- KPIs: Clientes (N), **Saldo a cobrar** (suma de deudas), Estado de las cuentas (barra: Al día % / Saldo a cobrar %).
- Toolbar: buscar, Filtrar, refrescar, **Registrar movimiento** (global, elige cliente), **Nuevo cliente**.
- Columnas: cliente (avatar con inicial), contacto (email/teléfono), cuenta corriente ($, rojo si debe).
- **Nuevo cliente** (`/customers/form`): título dinámico con avatar (inicial del nombre), acciones "Carga masiva" / Cancelar / Crear cliente. Campos: Nombre (obligatorio), Apellido, Género, Fecha de nacimiento, Teléfono, Email, DNI, CUIT (todos opcionales), Tipo de cliente ("Tienda física"; existe también online), check "Email marketing – Recibe ofertas y novedades", Notas. Sin nombre no deja crear.
- **Detalle del cliente** (`/customers/physical/ID`): "Cliente desde 18/09/26 · 2 datos cargados", chips (Tienda física · **Al día** verde / **Con deuda** rojo), botón Editar y menú ⋮. KPIs: Saldo de cuenta (rojo si debe), Compras del año, Última compra. "Datos del cliente". **Historial del cliente**: buscar, filtro tipo ("Todo"), Registrar movimiento; tabla fecha, concepto, movimiento asociado, tipo (chip **Pago** verde / **Aumento de deuda** rojo), medio de pago, vendedor, importe (pagos en azul con −), **saldo corrido**; pie "Saldo final $X". Toast verde "Éxito · Movimiento registrado".
- **Registrar movimiento** (modal): tarjeta cliente + saldo actual; Fecha; **Tipo de movimiento**: *Recibir un pago del cliente* · *Devolver dinero al cliente* · *Aumentar deuda*; Monto; **Medio de pago = cuenta de tesorería** (Caja, Banco Nación, Banco; solo en pagos/devoluciones, se oculta en "Aumentar deuda"); Comentario ("Número de comprobante, observaciones"); **"Saldo tras el movimiento"** en vivo. Botones Cancelar / Confirmar movimiento.
- Regla: saldo > 0 = el cliente debe (rojo, "Con deuda"); las ventas con "Está pago" apagado generan una fila de deuda automáticamente; el pago con medio de pago genera un ingreso en la cuenta elegida.

## Proveedores (/suppliers)
- KPIs: Proveedores activos, Saldo adeudado, Estado de las cuentas corrientes (barra: Al día / Saldo a pagar).
- Columnas: fecha, proveedor, rubro, contacto (nombre + teléfono), cuenta corriente. Botones: Filtrar, Registrar movimiento, **Crear proveedor**.
- **Ficha/Nuevo proveedor** (`/suppliers/form`): Razón social (obligatoria), CUIT, Rubro, Contacto, Teléfono, Email, Dirección, Ciudad, **Porcentaje de remarcación** (aviso: "La remarcación se reflejará únicamente en precios de la lista de precios principal" → precio sugerido = costo × (1+%)), Notas. Botones Cancelar / Guardar cambios.
- **Detalle del proveedor** (`/suppliers/ID`): avatar iniciales, "alta el 18/09/26 a las 14:25 h", chip **Al día** / **Saldo a pagar**; KPIs Saldo actual, Compras 2026 (acumulado del año), Última compra; "Datos del proveedor" (Remarcación 40%); **Movimientos de cuenta corriente** (filtro "Todos los tipos", Registrar movimiento; tabla fecha, tipo, comprobante, detalle, monto, saldo; pie "Saldo final").
- **Registrar movimiento (proveedor)**: Tipo de operación: *Pago a proveedor* · *Compra* · *Nota de crédito* · *Ajuste de saldo*; Monto; Comentario; "Saldo tras el movimiento". Compra suma deuda; pago y nota de crédito la restan. (Observado: una "Compra" manual quedó registrada con chip "Ajuste".)
- Las recepciones de pedidos (Compras) sin marcar "está pago" suman deuda acá.

## Modelo sugerido
`account_party(type customer|supplier)`, `party_ledger(party_id, date, kind[payment|debt_increase|refund|purchase|credit_note|adjustment|sale], amount, running_balance, payment_account_id, ref_type, ref_id, user_id, note)`; saldo = suma del ledger.

# Transferencias de stock (/stock-transfers)
- Listado con estado, productos, fecha, "Ver detalles" y botón **Nueva transferencia** → `/stock-transfers/transfer-form`: "Enviá stock de un depósito a otro." Dos paneles **ORIGEN** y **DESTINO** (con ícono ⇄ para invertir) y regla "El origen y el destino no pueden ser la misma sucursal".
- Requiere más de un depósito/sucursal: en el demo (1 sola sucursal, "Depósitos" en Ajustes > Productos y stock = "Pedir activación") el formulario no avanza. Estado "En tránsito" existe en Reportes > Stock ("En depósitos" vs "En tránsito").
