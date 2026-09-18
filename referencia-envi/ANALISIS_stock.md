# Stock / Productos (/products) — completo

## Listado
- KPIs: Productos (N), Stock crítico (N), Estado del stock (barra: "Stock ideal 29%" verde / "Sin stock 71%" rojo).
- Toolbar: buscar, **Filtrar** (panel: Stock [todos/ideal/crítico/sin stock], Orden [más nuevos…], Proveedor, Categoría (con ×), Eliminados [activos/eliminados], Archivado [activos/archivados]), refrescar, selector de **lista de precios** (Principal/Mayorista, cambia la columna Precio), importar/exportar, imprimir (etiquetas), **Nuevo producto ▾** (Nuevo combo · Carga masiva).
- Columnas: checkbox, imagen ("Sin imagen disponible"), producto (expandible ›), código de barra, categoría (badge azul), proveedor, precio (oferta con original tachado), inventario (badge verde/rojo con número; **negativo permitido**, ej. −1; click en el badge → **edición en línea** "Editar stock" con ✓), menú ⋮.
- Menú ⋮: Editar, Ver historial de precios, Ver historial de stock, Archivar, Eliminar.
- **Selección múltiple** → barra "N seleccionado ×" con acciones masivas: Estado, Categoría, Proveedor, Vencimiento, **Precio** (modal "Actualización masiva de precios": aviso "La remarcación se aplica únicamente a los precios de la lista de precios principal"; ¿Qué querés actualizar? [Precio de lista | Ofertas]; Tipo de ajuste [Por porcentaje | Por monto fijo]; Porcentaje de ajuste (valor negativo para bajar); check "Redondear para eliminar centavos"; "Se actualizará el precio de N producto"), Exportar, Eliminar.
- **Carga masiva** (modal): archivo (Excel/CSV) con columnas A Nombre · B Código de barra · C Stock · D Precio · E Oferta · F Costo · G Utilidad; check "La primera fila es el encabezado (se ignora)".
- **Combo** (`/products/new?type=combo`): Nombre, Código, Proveedor (opcional), Categoría, "Productos del combo" (modal buscador "Producto para el combo" con toggle Con stock/Todos + "Crear nuevo producto"; líneas con cantidad −/+ y borrar), Precio (autocompleta con la suma de componentes) y Oferta, Opciones avanzadas. **El inventario del combo se deriva de sus componentes** (badge con ⌄).
- **Producto completo** (`/products/new`): imagen, nombre, código (con botón de escáner), proveedor (opcional), categoría, costo, precio, oferta, stock, alerta stock bajo, stock ideal, opciones avanzadas (IVA 21%).
- Detalle/edición: ver ANALISIS.md (costo, margen %, precio, oferta, ganancia/unidad, markup, stock por sucursal, historial).

## Historial de stock (/stock-history) — libro mayor de movimientos
Filtrar + "Buscar por producto". Columnas: fecha (con avatar del responsable), stock anterior, movimiento (▲ +N verde / ▼ −N rojo), stock resultante, producto, **detalles**: tipo de origen + referencia. Tipos observados:
| Tipo | Ejemplo |
|---|---|
| Creación de producto | 0 → +10 = 10 |
| Venta en caja | "Venta #2": 10 → −1 = 9 (también permitió 0 → −1 = −1) |
| Compra recibida | "Compra P-1725": 0 → +1 = 1 |
| Actualización manual | edición en línea 9 → +6 = 15 |
(Otros esperables: transferencia entre depósitos, ajuste masivo, devolución/anulación.)
Observación: la venta creada por "Convertir presupuesto a venta" no generó movimiento de stock en el demo.

## Otros historiales
- **Historial de precios** (/pricing-history): "Seguí cómo cambiaron los precios de tus productos" (vacío hasta que hay cambios de precio/costo).
- **Ajustes de stock** (/stock-adjustments): "ajustes masivos de stock: quién los hizo, cuándo y qué tocaron"; columnas fecha, tipo, alcance, productos afectados, entradas, salidas.

## Lógica a replicar
1. Todo cambio de stock escribe una fila `stock_movement(product_id, branch_id, prev, delta, resulting, reason_type, reason_ref, user_id, created_at)`.
2. Venta sin stock permitida (negativo) salvo configuración; filtro "Con stock" solo afecta la búsqueda de la caja.
3. Precio de lista por `price_list` (Principal, Mayorista); oferta sobre la lista principal.
4. Combo = producto con `components[]`; stock = min(stock componente / cantidad).
5. Categorías con subcategorías (compartidas con tesorería).
