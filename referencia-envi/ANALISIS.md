# Analisis de referencia: Envi (maxkiosco.myenvi.website)

Relevado el 2026-09-18 desde un negocio demo "maxkiosco" (kiosco). Objetivo: replicar vistas y logica de un POS.
Las vistas HTML replicadas estan en `vistas/` y comparten `tokens.css`.

> Nota de diseno: las capturas oscuras iniciales venian de la extension Dark Reader. **El tema real es CLARO**
> (fondo `#f3f1ed` con degrade calido segun "color del negocio", tarjetas de vidrio translucidas, boton principal `#21213b`, fuente Montserrat).

## Estructura de navegacion (sidebar)
| Grupo | Item | Ruta |
|---|---|---|
| Analisis | Reportes | /reports (?tab=treasury/stock/products/customers/profitability) |
| Analisis | Historial > Historial de precios | /pricing-history |
| | Historial de stock | /stock-history |
| | Ajustes de stock | /stock-adjustments |
| | Cierres de caja | /cash-register-manager |
| Ventas | Ventas | /sales (detalle: ?saleId=ID) |
| | Presupuestos | /budgets |
| | Facturas | /invoices |
| Productos y servicios | Stock | /products (detalle /products/ID?edit=1) |
| | Compras y pedidos | /purchases |
| | Transferencias de stock | /stock-transfers |
| Cuentas corrientes | Cuentas y saldos | /accounting |
| | Clientes | /customers (detalle /customers/physical/ID) |
| | Proveedores | /suppliers |
| Usuarios | Empleados | /employees |
| | Horas trabajadas | /employees-schedules |
| Configuracion | Ajustes | /settings (?tab=cash-register/products/finances) |
| | Listas y catalogos > Categorias | /categories |
| (pantalla completa) | Caja registradora | /cash-register |

## Patron de pantalla (se repite en todo el sistema)
1. Topbar: logo del negocio + nombre, campana, usuario.
2. Titulo (28px/800) + subtitulo.
3. Fila de tarjetas KPI (resumen del dia/mes + barra de distribucion con porcentajes).
4. Barra de herramientas: buscar, Filtrar (panel colapsable), refrescar, exportar, boton principal.
5. Tabla con selector de columnas (icono +), menu de 3 puntos por fila, paginacion "Mostrando 1-20 de N" (server-side).
6. Detalles/altas en modales (URL con ?saleId=, ?edit=1).

## Ventas (/sales)
- KPIs de hoy: cantidad de ventas, monto vendido, medios de pago (barra con %: Cuenta corriente/Efectivo/Transferencia...).
- Filtros: periodo, rango de fechas, cliente, vendedor, medio de pago, factura (con/sin).
- Columnas: fecha+hora, vendedor (avatar+nombre), cliente (avatar; "Sin definir" para consumidor final), total, medio de pago (badge de color), detalle ("N prod. - Ver mas"). Opcionales: ID, Origen.
- Acciones por fila: Ver detalle, Ver comprobante, Generar factura, Eliminar.
- Detalle (modal): total con estado (ej. "Entregado"), cliente, vendedor, fecha, medio de pago, lineas (precio, cantidad, descuento, subtotal, eliminar linea), "+ Agregar producto", subtotal, descuento (+), total, "Agregar notas".
- Botones: refrescar, exportar, Nueva venta (abre /cash-register).

## Caja registradora (/cash-register)
Pantalla completa, sin sidebar, 2 columnas.
- Izquierda: buscador por nombre o codigo (con boton de escaner), selector de lista de precios (Principal / Mayorista), boton Guardar venta (deshabilitado con carrito vacio), link Promociones (modal: "No tienes promociones vigentes").
- Buscar: resultados en tabla (producto, categoria, precio, boton +). Filtro "Con stock" (activo por defecto; desmarcado deja vender sin stock). Precio de oferta con el de lista tachado.
- Carrito: cantidad con -/+, producto+categoria, precio, subtotal, menu de 3 puntos (Agregar descuento / Eliminar), TOTAL grande.
- Descuento por linea: modal "Gestionar descuento del producto": precio original - descuento ($ o %) = precio final (los 3 campos sincronizados).
- "+ Crear producto": modal Nuevo producto con carga **Rapida** (nombre + precio) o **Completa** (imagen, nombre, codigo, proveedor opcional, categoria, costo, precio, oferta, stock, alerta stock bajo, nivel de stock ideal, opciones avanzadas: IVA 21%). El producto creado se agrega solo al carrito.
- Atajos de teclado: Alt+F buscar, Esc limpiar busqueda, Alt+S guardar venta, Enter aceptar, Alt+Q producto rapido, Alt+N producto completo, Alt+M promociones; con producto seleccionado: Alt+"+" suma, Alt+"-" resta, Alt+X elimina.

### Modal de cobro "Guardar venta"
- Cliente y vendedor: Cliente (default **Consumidor final**, "* Nuevo cliente *", lista de clientes, buscable, con X para limpiar); Vendedor (default usuario logueado).
- Pago: selector de medio (Tarjeta de Debito/Credito, Transferencia, Efectivo) + switch **"Esta pago"** (default ON).
- **Logica de cuenta corriente**: si se elige un cliente y se apaga "Esta pago", el medio pasa a "Cuenta corriente" y aparece el aviso "Se registrara en la cuenta corriente de este cliente" (la venta queda como deuda del cliente).
- "Aplicar descuento": modal "Gestionar descuentos" con **porcentaje** global.
- Switch "Emitir factura": si no hay emisor configurado avisa "No tenes ningun emisor de facturas configurado. Configuralo en Facturacion".
- Switch "Imprimir comprobante". Campo Notas.
- Pie: TOTAL A COBRAR, boton atajos (teclado), Cancelar, Guardar.
- Tras guardar: el carrito se reinicia y la venta aparece en /sales con su medio de pago.

## Stock / Productos (/products)
- KPIs: Productos, Stock critico, Estado del stock (barra: sin stock/ok).
- Toolbar: buscar, filtrar, refrescar, selector de lista de precios (Principal/Mayorista), exportar/importar, imprimir, "Nuevo producto" con menu (Nuevo combo, Carga masiva).
- Columnas: checkbox, imagen, producto (expandible), codigo de barra, categoria (badge), proveedor, precio (oferta con original tachado), inventario (badge rojo si 0).
- Menu de fila: Editar, Ver historial de precios, Ver historial de stock, Archivar, Eliminar.
- Detalle/edicion de producto: cabecera con precio, costo, margen %, stock total; bloque datos (nombre, codigo, categoria, proveedor); "Costo y precios" (costo, margen de utilidad %, precio de venta, precio de oferta, ganancia por unidad, markup sobre costo, ganancia por stock actual); "Stock" por sucursal + total, alerta de stock bajo, stock ideal, boton Reponer; "Historial" (fecha, tipo, detalle, sucursal, responsable, precio, stock anterior, movimiento, stock resultante).

(Continua en secciones siguientes a medida que se releva.)
