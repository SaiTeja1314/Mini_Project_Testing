(function () {
  'use strict';

  if (typeof MTStorage === 'undefined') return;

  var formLogin = document.getElementById('form-login');
  var formSignup = document.getElementById('form-signup');
  var tabLogin = document.querySelector('[data-tab="login"]');
  var tabSignup = document.querySelector('[data-tab="signup"]');
  var loginErr = document.getElementById('login-err');
  var signupErr = document.getElementById('signup-err');
  var signupSuccess = document.getElementById('signup-success');

  function showErr(el, msg) {
    if (!el) return;
    el.textContent = msg || '';
    el.hidden = !msg;
  }

  function setActiveTab(tab) {
    var isLogin = tab === 'login';
    tabLogin.classList.toggle('active', isLogin);
    tabSignup.classList.toggle('active', !isLogin);
    tabLogin.setAttribute('aria-selected', isLogin ? 'true' : 'false');
    tabSignup.setAttribute('aria-selected', !isLogin ? 'true' : 'false');
    formLogin.classList.toggle('active', isLogin);
    formSignup.classList.toggle('active', !isLogin);
    showErr(loginErr, '');
    showErr(signupErr, '');
    showErr(signupSuccess, '');
  }

  if (tabLogin) tabLogin.addEventListener('click', function () { setActiveTab('login'); });
  if (tabSignup) tabSignup.addEventListener('click', function () { setActiveTab('signup'); });

  if (formLogin) {
    formLogin.addEventListener('submit', function (e) {
      e.preventDefault();
      showErr(loginErr, '');
      var email = (document.getElementById('login-email') || {}).value.trim();
      var password = (document.getElementById('login-password') || {}).value;
      if (!email) {
        showErr(loginErr, 'Please enter your email.');
        return;
      }
      if (!password) {
        showErr(loginErr, 'Please enter your password.');
        return;
      }
      var user = MTStorage.findUser(email, password);
      if (!user) {
        showErr(loginErr, 'Invalid email or password.');
        return;
      }
      MTStorage.setCurrentUser(email);
      window.location.href = 'home.html';
    });
  }

  if (formSignup) {
    formSignup.addEventListener('submit', function (e) {
      e.preventDefault();
      showErr(signupErr, '');
      showErr(signupSuccess, '');
      var email = (document.getElementById('signup-email') || {}).value.trim();
      var password = (document.getElementById('signup-password') || {}).value;
      if (!email) {
        showErr(signupErr, 'Please enter your email.');
        return;
      }
      if (!password) {
        showErr(signupErr, 'Please enter a password (min 6 characters).');
        return;
      }
      if (password.length < 6) {
        showErr(signupErr, 'Password must be at least 6 characters.');
        return;
      }
      if (!MTStorage.saveUser(email, password)) {
        showErr(signupErr, 'An account with this email already exists.');
        return;
      }
      showErr(signupSuccess, 'Account created. You can log in now.');
    });
  }

  if (MTStorage.getCurrentUser()) {
    window.location.replace('home.html');
  }
})();
