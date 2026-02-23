(function () {
  'use strict';

  if (typeof MTStorage === 'undefined') return;

  var user = MTStorage.getCurrentUser();

  var cartEmpty = document.getElementById('cart-empty');
  var cartContent = document.getElementById('cart-content');
  var cartTotalBox = document.getElementById('cart-total-box');
  var navCartCount = document.getElementById('nav-cart-count');

  function escapeHtml(s) {
    if (!s) return '';
    var div = document.createElement('div');
    div.textContent = s;
    return div.innerHTML;
  }

  function formatPrice(n) {
    return Number(n).toFixed(2);
  }

  function updateCartCount() {
    var cart = MTStorage.getCart();
    var total = cart.reduce(function (sum, item) { return sum + item.quantity; }, 0);
    if (navCartCount) {
      navCartCount.textContent = total;
      navCartCount.hidden = total === 0;
    }
  }

  function render() {
    var cart = MTStorage.getCart();
    if (cart.length === 0) {
      if (cartEmpty) cartEmpty.style.display = 'block';
      if (cartContent) cartContent.style.display = 'none';
      if (cartTotalBox) cartTotalBox.style.display = 'none';
      updateCartCount();
      return;
    }

    if (cartEmpty) cartEmpty.style.display = 'none';
    if (cartContent) cartContent.style.display = 'block';
    if (cartTotalBox) cartTotalBox.style.display = 'block';

    var subtotal = 0;
    var rows = cart.map(function (item) {
      var lineTotal = item.price * item.quantity;
      subtotal += lineTotal;
      return '<tr data-movie-id="' + escapeHtml(item.movieId) + '">' +
        '<td class="cart-item-name">' + escapeHtml(item.title) + '</td>' +
        '<td>$' + formatPrice(item.price) + '</td>' +
        '<td class="cart-qty">' +
          '<button type="button" class="cart-qty-minus" aria-label="Decrease quantity">−</button>' +
          '<span>' + item.quantity + '</span>' +
          '<button type="button" class="cart-qty-plus" aria-label="Increase quantity">+</button>' +
        '</td>' +
        '<td>$' + formatPrice(lineTotal) + '</td>' +
        '<td><button type="button" class="btn btn-danger btn-remove" aria-label="Remove item">Remove</button></td>' +
      '</tr>';
    }).join('');

    cartContent.innerHTML =
      '<table class="cart-table">' +
        '<thead><tr><th>Movie</th><th>Price</th><th>Quantity</th><th>Total</th><th></th></tr></thead>' +
        '<tbody>' + rows + '</tbody>' +
      '</table>';

    cartTotalBox.innerHTML =
      '<div class="cart-total-row">Subtotal <span id="cart-subtotal">$' + formatPrice(subtotal) + '</span></div>' +
      '<div class="cart-total-row grand">Grand Total <span id="cart-grand">$' + formatPrice(subtotal) + '</span></div>' +
      '<a href="payment.html" class="btn btn-primary">Checkout</a>';

    cartContent.querySelectorAll('.cart-qty-plus').forEach(function (btn) {
      btn.addEventListener('click', function () {
        var row = btn.closest('tr');
        var movieId = row && row.getAttribute('data-movie-id');
        if (movieId) {
          MTStorage.updateCartItemQuantity(user, movieId, 1);
          render();
        }
      });
    });
    cartContent.querySelectorAll('.cart-qty-minus').forEach(function (btn) {
      btn.addEventListener('click', function () {
        var row = btn.closest('tr');
        var movieId = row && row.getAttribute('data-movie-id');
        if (movieId) {
          MTStorage.updateCartItemQuantity(user, movieId, -1);
          render();
        }
      });
    });
    cartContent.querySelectorAll('.btn-remove').forEach(function (btn) {
      btn.addEventListener('click', function () {
        var row = btn.closest('tr');
        var movieId = row && row.getAttribute('data-movie-id');
        if (movieId) {
          MTStorage.removeFromCart(user, movieId);
          render();
        }
      });
    });

    updateCartCount();
  }

  render();
})();
