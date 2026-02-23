(function () {
  'use strict';

  if (typeof MTStorage === 'undefined') return;

  var user = MTStorage.getCurrentUser();

  var listEl = document.getElementById('orders-list');
  var emptyEl = document.getElementById('orders-empty');

  var params = new URLSearchParams(window.location.search);
  if (params.get('success') === '1') {
    var toast = document.getElementById('toast');
    if (toast) {
      toast.textContent = 'Order placed successfully!';
      toast.classList.add('show');
      setTimeout(function () { toast.classList.remove('show'); }, 4000);
    }
    window.history.replaceState({}, '', 'orders.html');
  }

  function escapeHtml(s) {
    if (!s) return '';
    var div = document.createElement('div');
    div.textContent = s;
    return div.innerHTML;
  }

  function formatPrice(n) {
    return Number(n).toFixed(2);
  }

  function formatDate(iso) {
    try {
      var d = new Date(iso);
      return d.toLocaleString();
    } catch (e) {
      return iso;
    }
  }

  var orders = MTStorage.getOrders();
  if (orders.length === 0) {
    if (listEl) listEl.innerHTML = '';
    if (emptyEl) emptyEl.style.display = 'block';
    return;
  }

  if (emptyEl) emptyEl.style.display = 'none';
  if (listEl) {
    listEl.innerHTML = orders.map(function (order) {
      var itemsHtml = order.items.map(function (item) {
        return '<li>' + escapeHtml(item.title) + ' × ' + item.quantity + ' — $' + formatPrice(item.price * item.quantity) + '</li>';
      }).join('');
      return '<article class="order-card">' +
        '<div class="order-header">' +
          '<span class="order-id">' + escapeHtml(order.id) + '</span>' +
          '<span class="order-date">' + escapeHtml(formatDate(order.date)) + '</span>' +
        '</div>' +
        '<ul class="order-items">' + itemsHtml + '</ul>' +
        '<div class="order-total">Total: $' + formatPrice(order.total) + '</div>' +
      '</article>';
    }).join('');
  }

  var navCartCount = document.getElementById('nav-cart-count');
  if (navCartCount) {
    var cart = MTStorage.getCart();
    var total = cart.reduce(function (sum, item) { return sum + item.quantity; }, 0);
    navCartCount.textContent = total;
    navCartCount.hidden = total === 0;
  }
})();
