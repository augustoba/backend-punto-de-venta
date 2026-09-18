# Ventario vs. nuestro POS — análisis comparativo

Fuente: `https://www.ventario.com.ar` (cuenta de prueba «maxkiosco», plan PRO con 7 días de prueba), recorrida el 2026-09-18.
**Límites del relevamiento:** la cuenta está vacía (sin productos ni ventas), así que se ven estructura, textos y controles pero no tablas con datos; y algunas funciones son del plan **FULL** (marca, unidad Kg, notas de crédito/facturación ARCA, resumen fiscal, valorización, pérdidas).
No se copian marca, textos comerciales ni planes: sólo ideas de producto.

## 1. Mapa de Ventario
| Grupo | Pantallas |
|---|---|
| Inicio | **Primeros pasos** (asistente con checklist), **Dashboard** |
| Productos | Inventario (Productos / Organización), **Promociones y Combos**, Compras (Órdenes de compra), **Control de Stock**, **Auditoría de Stock** |
| Caja y Tesorería | Control de Caja, Cuentas Corrientes («fiados»), Tesorería |
| Tienda Online | Configuración, Catálogo Online, Pedidos Recibidos, Cupones |
| Otros | Clientes, Mi Equipo, Proveedores, Gastos, Historial de Ventas, Reportes (11), Resumen Fiscal, Presupuestos, MercadoLibre, Configuración (centro de ajustes) |
| Aparte | **Punto de Venta** (pantalla completa `/overlay`, con recorrido guiado de 9 pasos) |

## 2. Lo que hacen mejor o distinto (aplicable a nuestro sistema)

### Primeros pasos y guía
- Pantalla **Primeros pasos**: «¿Por dónde querés empezar?», barra «0 de 2 esenciales», tarjetas grandes con ilustración (Agregá tu primer producto / Configurá tu Punto de Venta) y tarjetas secundarias (Hacé tu primera venta, Activá tu tienda online, Completá los datos del negocio).
- **Píldora «Configuración 0 %»** fija en el encabezado, con barra de avance y acceso directo.
- **Recorrido guiado del POS** (9 pasos, «Siguiente / Atrás / Entendido»).
- Estados vacíos que **guían a la acción** («Agregá tu primer producto» con botones Crear / Importar desde Excel).
- Botón de **ayuda flotante** (círculo verde) siempre visible.
- *Lo pidió el usuario copiar más adelante.*

### Dashboard (nosotros no tenemos)
Tarjetas de color (azul / verde / naranja / gris) con: Total productos, Ventas de la semana, **Vendido hoy** (ingresado, cobros de cuenta corriente, operaciones, facturado), Alertas de stock. Además: análisis financiero diario (barras o línea), **resumen financiero** (ganancia bruta = ingresos − costos, egresos operativos, ganancia neta y margen, tendencia vs. período anterior), gastos del mes, **Top 5 días**, **ventas por hora** (hoy / 7 días), top productos, **alertas de rentabilidad** (márgenes bajos), saldo de cuentas corrientes, control de caja del turno, deuda con proveedores, stock bajo, últimos movimientos de inventario.

### Punto de Venta
- Indicador siempre visible **«SCANNER ON»** (estado del lector) y modo **«Rápida» (F1)**.
- **Atajos de teclado en la barra**: F1 rápida, F4 cierre de caja, F6 historial, F7 gastos, F8 confirmar venta.
- Barra de **botones de color con ícono** para ir a Caja, Proveedores, Gastos, Movimientos, Cierre, Historial y Dashboard sin salir del POS.
- **Chips de categoría** + grilla de productos con foto (además del buscador «Enter para agregar»).
- Botones **Descuento** y **Recargo** en el carrito (nosotros sólo tenemos descuento).
- Aviso «Sin caja activa · Abrir caja» dentro del panel.

### Productos
- Formulario en panel lateral con secciones: Datos, Organización (categoría, **subcategoría**, **marca**, proveedor), Tienda online, En el local, **Precios y rentabilidad** (costo, margen %, precio final, **unidad Uni / Kg / Lt**, categoría de IVA, **precio mayorista**), **hasta 3 imágenes** (se comprimen), **variantes** (talles, colores, tamaños), Inventario (cantidad, **stock mínimo**, **códigos alternativos**).
- **Importar productos desde Excel** y pestaña «Organización».

### Stock
- **Control de Stock**: registrar **Ingreso / Egreso** con motivo (**Baja por pérdida o daño, Consumo interno, Ajuste manual**), proveedor y nota; el historial guarda quién lo hizo.
- **Auditoría de Stock**: asistente de 4 pasos (Iniciar → Descargar planilla → Importar → **Revisar y aplicar**): compara el inventario físico contra el sistema y detecta diferencias; se puede contar en la app o en Excel, todo o una parte.
- **Promociones y Combos**: reglas (3×2, combo a precio fijo), vigencias y estado; KPIs (activas ahora, configuradas, aplicaciones, ganancia registrada).

### Compras
Compras **y** Órdenes de compra por separado, «Factura rápida», **borradores compartidos con el equipo**, selector de mes/año, filtros Todas / Pagadas / Con deuda, KPIs (gastado, compras, deuda del mes, órdenes abiertas).

### Caja y Tesorería
- **Control de Caja**: cajas abiertas en tiempo real («efectivo esperado por puesto»), resumen del período (ventas, gastos, cobros de cuenta corriente, movimientos manuales, balance), «todos los cierres están conciliados», **Analizar turnos**, filtro por vendedor e «incluir anulados».
- **Tesorería**: «plata del negocio» consolidada, **fondo operativo** (efectivo) y **fondo virtual** (digital), **cheques en cartera**, **«mi plata personal»** (retiros del dueño, separada), configuración de motivos y **PIN de administrador**, filtros por tipo / categoría / origen / medio.
- **Cuentas Corrientes**: estados Con deuda / A favor / Sin deuda, y **cuotas vencidas**.

### Clientes, proveedores, gastos, equipo
- Clientes con **categorías** (Mayorista, Distribuidor, Franquicia), filtros por **origen** (Online, POS, Cuenta corriente, Presupuestos) y «clientes estrella»; el alta **actualiza sin duplicar** si el teléfono o el email ya existen.
- Proveedores: estados Con deuda / Al día / **A favor**, ordenar por mayor deuda, **pagos a proveedor** por período.
- **Gastos** como pantalla propia: fijos vs. variables, categorías configurables.
- Mi Equipo: límite de vendedores por plan y «Rendimiento avanzado» (KPIs por vendedor, ranking, objetivos; anunciado como próximamente).

### Historial y reportes
- **Historial** con pestañas Resumen / Ventas / Pedidos / Presupuestos / **Devoluciones**, período Mes / Semana / Día / Rango, ganancia bruta y margen, desglose de pagos, ticket promedio.
- **11 reportes**: rentabilidad, rentabilidad por categoría, **rotación de inventario (clasificación ABC)**, rendimiento de turnos, **Reposición — ¿qué reponer?** (cruza lo vendido con el stock e informa cuánto reponer y la inversión estimada), stock actual, stock bajo, movimientos, valorización, **pérdidas**, **devoluciones**.
- **Devoluciones / cambios** como concepto propio (nosotros no tenemos).
- **Resumen fiscal**: Libro IVA Ventas / Compras, desglose por alícuota, exportación XLSX/CSV.

### Presupuestos
PDF profesional con datos y logo, envío por **WhatsApp**, **seña**, convertir en venta con un clic, seguimiento (Pendientes / Historial).

### Tienda online (canal de venta)
Configuración (enlace propio, horarios, **retiro en local / delivery propio**, control de stock: bloquear o permitir sin stock, WhatsApp de pedidos, redes, ubicación con mapa, logo, portada, color de acento, modo de visualización), **catálogo público** (publicar productos, destacados, ordenar categorías, **catálogo en PDF**), **pedidos en tablero** (Por confirmar → En cola → Preparación → Para entregar) y **cupones**. Además integración con **MercadoLibre** y un programa de **referidos**.

### Configuración
Centro de ajustes por tarjetas (Datos del negocio, Referidos, Punto de venta, Facturación ARCA, Contraseña), **perfil con % de avance** (dirección, CUIT, logo pendientes), **horarios de atención** (un turno / dos turnos / 24 h / variable), CUIT.

### Visual
Más colorido: tarjetas de resumen con degradado, íconos consistentes, píldoras de estado, selector de negocio arriba del menú con botón de contraer, campana con punto de aviso, botón de ayuda flotante, aviso de prueba con avance.

## 3. Lo que nosotros tenemos y no vimos en Ventario
Arqueo con diferencias por empleado y umbral de alerta; **cheques a cobrar/pagar**; **centros de costos** con 4 criterios de reparto y rentabilidad por categoría con costo fijo; **listas de precios** (Principal / Mayorista); **depósitos y transferencias** entre depósitos; cuenta corriente con **saldo corrido** de clientes y proveedores; árbol de categorías con emoji/color compartido con tesorería; movimientos de dinero con libro por cuenta; historial de precios; lector de mano **y** cámara para códigos de barras; usuarios con roles y token; API propia.

## 4. Qué nos falta (priorizado)

### Prioridad 1 — mayor valor, viable
1. **Dashboard** (tarjetas, análisis financiero, top días / productos, ventas por hora, alertas).
2. **Primeros pasos** + píldora de progreso + **recorrido guiado del POS** + estados vacíos que guían.
3. **POS**: chips de categoría y grilla de productos con foto, **Recargo**, atajos F1/F4/F6/F7/F8 visibles, indicador de escáner («SCANNER ON»), accesos rápidos (gastos, movimientos, cierre) sin salir de la caja.
4. **Devoluciones / cambios** (con reintegro de stock y de dinero o crédito) y **notas de crédito** de prueba.
5. **Control de stock con motivos** (baja, consumo interno, ajuste manual) y reporte de **pérdidas**.
6. **Auditoría de stock** (conteo físico vs. sistema, planilla, revisar y aplicar).
7. Reportes: **Reposición (qué reponer)**, **Rotación ABC**, Stock bajo, Valorización.

### Prioridad 2
8. **Motor de promociones** (3×2, combo precio fijo, vigencias, métricas).
9. **Libro IVA Ventas / Compras** y exportación CSV/XLSX (ya guardamos neto/IVA).
10. Producto: **subcategoría, marca, stock mínimo visible, códigos de barras alternativos, hasta 3 imágenes, variantes** (talle / color), **importar desde Excel**.
11. Cuentas corrientes con **vencimientos** y saldo **a favor**; clientes con **categorías y origen**; alta sin duplicados por teléfono/email.
12. Presupuestos: **PDF con logo, WhatsApp, seña**.
13. **Gastos** como pantalla (fijos / variables); **Control de Caja en tiempo real** y resumen del período; **PIN de administrador**; «plata personal» del dueño.
14. Órdenes de compra separadas de las compras; borradores compartidos.

### Prioridad 3 / integraciones
15. **Canal de tienda online** (catálogo público, pedidos en tablero, cupones): coincide con el plan de **integrar más adelante con el ecommerce «Estilos Pequeños»**; conviene diseñarlo como integración y no duplicarlo.
16. MercadoLibre, referidos, horarios de atención y perfil con % de avance.
17. Ya anotados como cambios posibles: venta por peso (unidad Kg/Lt), múltiples monedas, lotes, ARCA.

## 5. Cómo seguir
Elegir del apartado 4 lo que se quiera hacer (sugerencia: empezar por el Dashboard y Primeros pasos, que es lo más visible, y luego POS y devoluciones). Si se quiere, se puede replicar cualquier pantalla de Ventario como HTML de referencia antes de construirla (como se hizo con Envi en `referencia-envi/vistas`).
