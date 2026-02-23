(function () {
  'use strict';

  if (typeof MTStorage === 'undefined') return;

  var grid = document.getElementById('movie-grid');
  var searchInput = document.getElementById('search-movies');
  var navCartCount = document.getElementById('nav-cart-count');

  function updateCartCount() {
    var cart = MTStorage.getCart();
    var total = cart.reduce(function (sum, item) { return sum + item.quantity; }, 0);
    if (navCartCount) {
      navCartCount.textContent = total;
      navCartCount.hidden = total === 0;
    }
  }

  function renderMovies(movies) {
    if (!grid) return;
    grid.innerHTML = '';
    movies.forEach(function (m) {
      var card = document.createElement('article');
      card.className = 'movie-card';
      card.setAttribute('role', 'listitem');
      card.innerHTML =
        '<img class="movie-card-poster" src="' + escapeHtml(m.image) + '" alt="" loading="lazy" decoding="async" width="400" height="600">' +
        '<div class="movie-card-body">' +
          '<h2 class="movie-card-title">' + escapeHtml(m.title) + '</h2>' +
          '<div class="movie-card-meta">' +
            '<span class="movie-card-price">$' + formatPrice(m.price) + '</span>' +
            (m.rating ? '<span class="movie-card-rating">★ ' + escapeHtml(m.rating) + '</span>' : '') +
          '</div>' +
          '<a href="movie-details.html?id=' + encodeURIComponent(m.id) + '" class="btn btn-primary">View Details</a>' +
        '</div>';
      grid.appendChild(card);
    });
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

  function filterAndRender() {
    var query = (searchInput && searchInput.value) ? searchInput.value.trim().toLowerCase() : '';
    var all = MTStorage.getMovies();
    var list = query
      ? all.filter(function (m) { return m.title.toLowerCase().indexOf(query) !== -1; })
      : all;
    renderMovies(list);
  }

  if (searchInput) {
    searchInput.addEventListener('input', filterAndRender);
    searchInput.addEventListener('search', filterAndRender);
  }

  filterAndRender();
  updateCartCount();
})();
