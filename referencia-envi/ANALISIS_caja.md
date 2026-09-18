# Caja: apertura, cierre, arqueo y movimientos (probado con datos reales)

Se activa en Ajustes > Ventas y caja > **Arqueo de caja**. Al activarlo aparece "Alerta de diferencia": umbral en $ (ej. 2000); diferencias menores no se marcan como error. Con el arqueo APAGADO se puede vender sin abrir caja.

- Menú de usuario (avatar): Mi cuenta, Caja registradora, Suscripción, Apariencia (claro/oscuro), Cerrar sesión.
- **Caja cerrada** => la caja registradora bloquea la venta: "La caja está cerrada. Abrí la caja registradora para empezar a vender. Al abrirla vas a declarar el efectivo inicial, y al cerrarla vas a poder comparar lo contado contra lo registrado." + botón *Abrir caja registradora*.
- **Apertura de caja** (modal): Caja (cuenta de tipo caja), "Valor cierre anterior" (solo lectura = saldo real del último cierre), "Nuevo saldo inicial" (editable, por defecto el cierre anterior), Notas de apertura (opcional). Si el nuevo saldo difiere, avisa: "Se agregará movimiento 'Diferencia de apertura de caja' +$X" (categoría "Ajustes y diferencias de caja > Diferencia al abrir caja").
- Panel en la caja abierta: "Caja abierta: Caja", botón **Cerrar caja**, link **Otras operaciones de caja** (Registrar ingreso / Registrar egreso / Movimiento entre cuentas).
- **Nuevo movimiento (ingreso/egreso)**: banner de tipo, Cuenta (fija = la caja), Importe, Fecha del movimiento (datetime, default ahora), texto en vivo "Nuevo total en la cuenta Caja: $X" (permite saldo negativo), Categoría + **Subcategoría (obligatoria; sin ella "Aceptar" no hace nada)**, Descripción.
- **Cierre de caja** (modal): Saldo inicial + Total ingresos − Total egresos, "Mostrar detalle de movimientos de caja", **Saldo de cierre esperado** (calculado por el sistema, solo lectura), **Saldo de cierre real** (lo carga el empleado; por defecto = esperado) con badge en vivo "Sobrante de $X" (verde) o faltante, Notas de cierre (opcional). Al confirmar la caja queda cerrada y hay que reabrirla.
- **Cierres de caja** (/cash-register-manager): filtro, refrescar, **Diferencias por empleado** (modal: mes/año, por empleado cantidad de diferencias, total y promedio por cierres), **Configurar alerta de diferencia** (lleva a Ajustes). Tabla por turno: caja + vendedor + fecha + "Ver movimientos" (modal saldo inicial/ingresos/egresos/lista), apertura (hora, saldo), cierre (hora, saldo, diferencia +/−) o "Caja sin cerrar", comentarios (agregar notas) y switch **Verificado** Sí/No para que el dueño confirme el cierre.

## Ajustes > Mi negocio
Link de acceso (subdominio), sucursales: logo/nombre editable, **Color del negocio** (5 swatches: define el degradé de fondo), "Contacto y ubicación".

## Modelo de datos sugerido para caja
- `cash_session` (id, caja/cuenta_id, employee_id, opened_at, opening_balance, previous_close, opening_diff, closed_at, expected_close, real_close, close_diff, notes_open, notes_close, verified).
- Cada movimiento de caja = `account_movement` (cuenta, importe +/-, fecha, categoría, subcategoría, descripción, usuario, session_id).
- Ventas en efectivo con arqueo activo => ingreso en la caja abierta; esperado = saldo inicial + ingresos − egresos.
