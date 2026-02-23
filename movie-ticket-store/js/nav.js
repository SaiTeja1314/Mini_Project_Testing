(function () {
  'use strict';
  var logoutEl = document.getElementById('nav-logout');
  if (logoutEl && typeof MTStorage !== 'undefined') {
    logoutEl.addEventListener('click', function (e) {
      e.preventDefault();
      MTStorage.logout();
      window.location.href = 'index.html';
    });
  }
})();
