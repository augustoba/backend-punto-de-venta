# Presupuestos (/budgets) y descuentos (probado)

## Presupuestos
- KPIs del mes: Presupuestos activos, Presupuestos vencidos, Monto total presupuestado (barra por estado: Activos / Vendidos ...).
- Columnas: fecha+hora, creado por, cliente, total, detalle ("N prod. Ver más"), vencimiento, estado (badge "Activo").
- **Nuevo presupuesto** (modal): Cliente (selector con "* Nuevo cliente *"), Fecha de expiración (default hoy + 30 días), Lista de precios (Principal/Mayorista), Productos (botón punteado "+ Agregar producto" → modal buscador con "N producto encontrado", nombre, badge de stock y precio, link "+ Crear nuevo producto"), tabla de líneas (precio, cantidad −/+, subtotal, borrar), Notas ("Condiciones de entrega, validez de precios, forma de pago"), resumen **Subtotal + IVA (21%) = Total**. Pie: "Se guarda como borrador hasta que lo envíes." / Cancelar / Crear presupuesto.
- **Los precios ya incluyen IVA**: producto $1.000 → Subtotal $826,45 + IVA 21% $173,55 = Total $1.000.
- Menú de fila: Editar, Imprimir, Duplicar, **Convertir a venta**, Rechazar presupuesto, Archivar, Eliminar.
- **Convertir a venta** abre el mismo modal "Guardar venta" con el cliente cargado; al guardar, el presupuesto pasa a estado "Vendido" (sale del listado de activos; la barra del KPI muestra "Vendidos 100%").

## Descuentos (3 niveles + automático)
1. Por línea (caja): monto $, % o precio final, sincronizados.
2. Global de la venta (modal Guardar venta > "Aplicar descuento"): porcentaje. "Descuentos acumulativos" (Ajustes) define si los de línea se suman al global.
3. **Descuento automático por medio de pago**: al elegir *Transferencia* aparece "Descuento automático aplicado: 10%" con link "Eliminar descuento". La venta se guarda con el 10% aplicado ($1.000 → $900). (Efectivo y tarjeta no lo aplican.) Es configurable por el negocio (ver Promociones/Listas).
4. Promociones vigentes (botón en caja): modal con la lista; en el demo "No tienes promociones vigentes".

## Ventas: efectos colaterales verificados
- Vender un producto descuenta stock (10 → 9).
- Venta a cliente con "Está pago" apagado => medio "Cuenta corriente" (deuda del cliente).
- KPIs de hoy se recalculan (ventas, monto, % por medio de pago).
