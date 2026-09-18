// Arma sidebar + topbar alrededor de <main id="content"> (réplica de la navegación de Envi).
(function () {
  const nav = [
    ['Análisis', [['Reportes', '04-reportes.html'], ['Historial de precios', '13-historiales.html'], ['Historial de stock', '13-historiales.html'], ['Ajustes de stock', '13-historiales.html'], ['Cierres de caja', '12-cierres-caja.html']]],
    ['Ventas', [['Ventas', '01-ventas.html'], ['Presupuestos', '14-presupuestos-facturas.html'], ['Facturas', '14-presupuestos-facturas.html']]],
    ['Productos y servicios', [['Stock', '05-stock.html'], ['Compras y pedidos', '06-compras-transferencias.html'], ['Transferencias de stock', '06-compras-transferencias.html']]],
    ['Cuentas corrientes', [['Cuentas y saldos', '07-cuentas-saldos.html'], ['Clientes', '08-clientes-proveedores.html'], ['Proveedores', '08-clientes-proveedores.html']]],
    ['Usuarios', [['Empleados', '09-empleados.html'], ['Horas trabajadas', '09-empleados.html']]],
    ['Configuración', [['Ajustes', '10-ajustes.html'], ['Categorías', '11-categorias.html']]],
  ];
  const active = document.currentScript.dataset.active || '';
  const main = document.getElementById('content');
  const side = nav.map(([g, items]) => '<h6>' + g + '</h6>' + items.map(([l, h]) => '<a href="' + h + '"' + (l === active ? ' class="on"' : '') + '>' + l + '</a>').join('')).join('');
  const app = document.createElement('div');
  app.className = 'app';
  app.innerHTML = '<aside class="sidebar"><div class="logo">envi</div>' + side + '</aside><div class="main"><div class="topbar"><b>maxkiosco</b><span>🔔 &nbsp; juan</span></div></div>';
  main.parentNode.insertBefore(app, main);
  app.querySelector('.main').appendChild(main);
})();
