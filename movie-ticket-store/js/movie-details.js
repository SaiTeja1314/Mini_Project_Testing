(function () {
  'use strict';

  if (typeof MTStorage === 'undefined') return;

  var user = MTStorage.getCurrentUser();

  var params = new URLSearchParams(window.location.search);
  var id = params.get('id');
  var content = document.getElementById('detail-content');
  var notFound = document.getElementById('detail-not-found');
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

  function showToast(msg) {
    var el = document.getElementById('toast');
    if (!el) return;
    el.textContent = msg;
    el.classList.add('show');
    setTimeout(function () { el.classList.remove('show'); }, 3000);
  }

  function updateCartCount() {
    var cart = MTStorage.getCart();
    var total = cart.reduce(function (sum, item) { return sum + item.quantity; }, 0);
    if (navCartCount) {
      navCartCount.textContent = total;
      navCartCount.hidden = total === 0;
    }
  }

  function addToCart() {
    var movie = MTStorage.getMovieById(id);
    if (!movie) return;
    MTStorage.addToCart(user, movie.id, movie.title, movie.price, movie.image, 1);
    updateCartCount();
    showToast('Added to cart!');
  }

  if (!id) {
    if (content) content.style.display = 'none';
    if (notFound) notFound.style.display = 'block';
    return;
  }

  var movie = MTStorage.getMovieById(id);
  if (!movie) {
    if (content) content.style.display = 'none';
    if (notFound) notFound.style.display = 'block';
    return;
  }

  if (content) {
    content.innerHTML =
      '<div class="detail-layout">' +
        '<div class="detail-poster">' +
          '<img src="' + escapeHtml(movie.image) + '" alt="" width="320" height="480" loading="eager" decoding="async">' +
        '</div>' +
        '<div class="detail-info">' +
          '<h1>' + escapeHtml(movie.title) + '</h1>' +
          '<div class="detail-meta">' +
            (movie.genre ? '<span>Genre: ' + escapeHtml(movie.genre) + '</span>' : '') +
            (movie.rating ? '<span>Rating: ★ ' + escapeHtml(movie.rating) + '</span>' : '') +
          '</div>' +
          '<p class="detail-price">$' + formatPrice(movie.price) + '</p>' +
          '<p class="detail-desc">' + escapeHtml(movie.description || '') + '</p>' +
          '<div class="detail-actions">' +
            '<button type="button" class="btn btn-primary" id="btn-add-cart">Add to Cart</button>' +
            '<a href="home.html" class="btn btn-secondary">Back to Home</a>' +
          '</div>' +
        '</div>' +
      '</div>';
  }

  var btn = document.getElementById('btn-add-cart');
  if (btn) btn.addEventListener('click', addToCart);
  updateCartCount();
})();
