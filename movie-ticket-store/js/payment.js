(function () {
  'use strict';

  if (typeof MTStorage === 'undefined') return;

  var user = MTStorage.getCurrentUser();

  var cart = MTStorage.getCart();
  if (cart.length === 0) {
    window.location.replace('cart.html');
    return;
  }

  var summaryEl = document.getElementById('order-summary');
  var form = document.getElementById('payment-form');
  var payErr = document.getElementById('payment-err');
  var cardNumber = document.getElementById('card-number');
  var cardExpiry = document.getElementById('card-expiry');
  var cardCvv = document.getElementById('card-cvv');

  function formatPrice(n) {
    return Number(n).toFixed(2);
  }

  var total = cart.reduce(function (sum, item) {
    return sum + item.price * item.quantity;
  }, 0);

  if (summaryEl) {
    summaryEl.innerHTML =
      '<div class="cart-total-row"><span>Items total</span><span>$' + formatPrice(total) + '</span></div>' +
      '<div class="cart-total-row grand">Amount to pay <span>$' + formatPrice(total) + '</span></div>';
  }

  function showErr(el, msg) {
    if (!el) return;
    el.textContent = msg || '';
    el.hidden = !msg;
  }

  function formatCardNumber(val) {
    var v = val.replace(/\D/g, '').slice(0, 16);
    var parts = [];
    for (var i = 0; i < v.length; i += 4) parts.push(v.slice(i, i + 4));
    return parts.join(' ');
  }

  function formatExpiry(val) {
    var v = val.replace(/\D/g, '');
    if (v.length >= 2) return v.slice(0, 2) + '/' + v.slice(2, 4);
    return v;
  }

  if (cardNumber) {
    cardNumber.addEventListener('input', function () {
      this.value = formatCardNumber(this.value);
    });
  }
  if (cardExpiry) {
    cardExpiry.addEventListener('input', function () {
      this.value = formatExpiry(this.value);
    });
  }
  if (cardCvv) {
    cardCvv.addEventListener('input', function () {
      this.value = this.value.replace(/\D/g, '').slice(0, 4);
    });
  }

  function validateCardNumber(val) {
    var digits = val.replace(/\s/g, '');
    return /^\d{13,19}$/.test(digits);
  }

  function validateExpiry(val) {
    var m = val.match(/^(\d{2})\/(\d{2})$/);
    if (!m) return false;
    var month = parseInt(m[1], 10);
    var year = parseInt(m[2], 10) + 2000;
    if (month < 1 || month > 12) return false;
    var now = new Date();
    if (year < now.getFullYear()) return false;
    if (year === now.getFullYear() && month < now.getMonth() + 1) return false;
    return true;
  }

  function validateCvv(val) {
    return /^\d{3,4}$/.test(val);
  }

  if (form) {
    form.addEventListener('submit', function (e) {
      e.preventDefault();
      showErr(payErr, '');

      var name = (document.getElementById('card-name') || {}).value.trim();
      var num = (cardNumber && cardNumber.value) || '';
      var expiry = (cardExpiry && cardExpiry.value) || '';
      var cvv = (cardCvv && cardCvv.value) || '';

      if (!name) {
        showErr(payErr, 'Please enter the name on card.');
        return;
      }
      if (!validateCardNumber(num)) {
        showErr(payErr, 'Please enter a valid card number (13–19 digits).');
        return;
      }
      if (!validateExpiry(expiry)) {
        showErr(payErr, 'Please enter a valid expiry date (MM/YY).');
        return;
      }
      if (!validateCvv(cvv)) {
        showErr(payErr, 'Please enter a valid CVV (3 or 4 digits).');
        return;
      }

      var btn = document.getElementById('pay-btn');
      if (btn) {
        btn.disabled = true;
        btn.textContent = 'Processing…';
      }

      MTStorage.addOrder(user, cart, total);
      MTStorage.clearCart(user);

      window.location.href = 'orders.html?success=1';
    });
  }
})();
