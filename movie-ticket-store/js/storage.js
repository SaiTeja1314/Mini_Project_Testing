/**
 * Local Storage helpers and seed data for Movie Ticket Store
 * Keys: users, currentUser, movies, cart_{email}, orders_{email}
 */

(function (global) {
  'use strict';

  var STORAGE_KEYS = {
    USERS: 'mt_users',
    CURRENT_USER: 'mt_currentUser',
    MOVIES: 'mt_movies',
    CART: 'mt_cart_',
    ORDERS: 'mt_orders_'
  };

  var MOVIES_SEED = [
    {
      id: '1',
      title: 'Inception',
      price: 12.99,
      rating: '8.8',
      genre: 'Sci-Fi, Thriller',
      description: 'A thief who steals corporate secrets through dream-sharing technology is offered a chance to have his criminal record erased in exchange for planting an idea in a CEO\'s mind.',
      image: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=400&h=600&fit=crop'
    },
    {
      id: '2',
      title: 'The Dark Knight',
      price: 11.99,
      rating: '9.0',
      genre: 'Action, Crime, Drama',
      description: 'When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests to fight injustice.',
      image: 'https://images.unsplash.com/photo-1509347528160-9a9e33742cdb?w=400&h=600&fit=crop'
    },
    {
      id: '3',
      title: 'Interstellar',
      price: 13.99,
      rating: '8.6',
      genre: 'Sci-Fi, Adventure, Drama',
      description: 'A team of explorers travel through a wormhole in space in an attempt to ensure humanity\'s survival.',
      image: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=400&h=600&fit=crop'
    },
    {
      id: '4',
      title: 'Avatar',
      price: 14.99,
      rating: '7.8',
      genre: 'Sci-Fi, Adventure, Fantasy',
      description: 'A paraplegic Marine dispatched to the moon Pandora on a unique mission becomes torn between following his orders and protecting the world he feels is his home.',
      image: 'https://images.unsplash.com/photo-1478720568477-152d9b164e26?w=400&h=600&fit=crop'
    },
    {
      id: '5',
      title: 'Dune',
      price: 15.99,
      rating: '8.0',
      genre: 'Sci-Fi, Adventure',
      description: 'A noble family becomes embroiled in a war for control over the galaxy\'s most valuable asset while its heir becomes troubled by visions of a dark future.',
      image: 'https://images.unsplash.com/photo-1517604931442-7e0c8ed3843f?w=400&h=600&fit=crop'
    },
    {
      id: '6',
      title: 'Top Gun: Maverick',
      price: 12.49,
      rating: '8.3',
      genre: 'Action, Drama',
      description: 'After thirty years, Maverick is still pushing the envelope as a top naval aviator, but must confront ghosts of his past when he leads a mission requiring the ultimate sacrifice.',
      image: 'https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=400&h=600&fit=crop'
    },
    {
      id: '7',
      title: 'Spider-Man: No Way Home',
      price: 13.49,
      rating: '8.7',
      genre: 'Action, Adventure, Sci-Fi',
      description: 'With Spider-Man\'s identity now revealed, Peter asks Doctor Strange for help. When a spell goes wrong, dangerous foes from other worlds start to appear.',
      image: 'https://images.unsplash.com/photo-1509347528160-9a9e33742cdb?w=400&h=600&fit=crop'
    },
    {
      id: '8',
      title: 'Everything Everywhere All at Once',
      price: 11.99,
      rating: '8.9',
      genre: 'Sci-Fi, Comedy, Drama',
      description: 'An aging Chinese immigrant is swept up in an insane adventure where she alone can save existence by exploring other universes connecting with the lives she could have led.',
      image: 'https://images.unsplash.com/photo-1485846234645-a62644f84728?w=400&h=600&fit=crop'
    }
  ];

  function get(key) {
    try {
      var raw = localStorage.getItem(key);
      return raw ? JSON.parse(raw) : null;
    } catch (e) {
      return null;
    }
  }

  function set(key, value) {
    try {
      localStorage.setItem(key, JSON.stringify(value));
      return true;
    } catch (e) {
      return false;
    }
  }

  function getUsers() {
    return get(STORAGE_KEYS.USERS) || [];
  }

  function saveUser(email, password) {
    var users = getUsers();
    if (users.some(function (u) { return u.email === email; })) return false;
    users.push({ email: email, password: password });
    return set(STORAGE_KEYS.USERS, users);
  }

  function findUser(email, password) {
    var users = getUsers();
    return users.find(function (u) { return u.email === email && u.password === password; }) || null;
  }

  function getCurrentUser() {
    return get(STORAGE_KEYS.CURRENT_USER);
  }

  function setCurrentUser(email) {
    return set(STORAGE_KEYS.CURRENT_USER, email);
  }

  function logout() {
    try {
      localStorage.removeItem(STORAGE_KEYS.CURRENT_USER);
      return true;
    } catch (e) {
      return false;
    }
  }

  function getMovies() {
    var stored = get(STORAGE_KEYS.MOVIES);
    if (stored && stored.length) return stored;
    set(STORAGE_KEYS.MOVIES, MOVIES_SEED);
    return MOVIES_SEED;
  }

  function getMovieById(id) {
    var movies = getMovies();
    return movies.find(function (m) { return m.id === id; }) || null;
  }

  function getCartKey(email) {
    return STORAGE_KEYS.CART + (email || getCurrentUser() || '');
  }

  function getCart(email) {
    return get(getCartKey(email)) || [];
  }

  function saveCart(email, items) {
    return set(getCartKey(email), items);
  }

  function addToCart(email, movieId, title, price, image, qty) {
    qty = qty || 1;
    var cart = getCart(email);
    var i = cart.findIndex(function (item) { return item.movieId === movieId; });
    if (i >= 0) {
      cart[i].quantity += qty;
    } else {
      cart.push({ movieId: movieId, title: title, price: price, image: image, quantity: qty });
    }
    return saveCart(email, cart);
  }

  function updateCartItemQuantity(email, movieId, delta) {
    var cart = getCart(email);
    var i = cart.findIndex(function (item) { return item.movieId === movieId; });
    if (i < 0) return false;
    cart[i].quantity += delta;
    if (cart[i].quantity <= 0) {
      cart.splice(i, 1);
    }
    return saveCart(email, cart);
  }

  function removeFromCart(email, movieId) {
    var cart = getCart(email).filter(function (item) { return item.movieId !== movieId; });
    return saveCart(email, cart);
  }

  function clearCart(email) {
    return saveCart(email, []);
  }

  function getOrders(email) {
    return get(STORAGE_KEYS.ORDERS + (email || getCurrentUser() || '')) || [];
  }

  function addOrder(email, items, total) {
    var orders = getOrders(email);
    var order = {
      id: 'ORD-' + Date.now(),
      date: new Date().toISOString(),
      items: items.map(function (item) {
        return { title: item.title, price: item.price, quantity: item.quantity };
      }),
      total: total
    };
    orders.unshift(order);
    return set(STORAGE_KEYS.ORDERS + email, orders);
  }

  global.MTStorage = {
    get: get,
    set: set,
    getUsers: getUsers,
    saveUser: saveUser,
    findUser: findUser,
    getCurrentUser: getCurrentUser,
    setCurrentUser: setCurrentUser,
    logout: logout,
    getMovies: getMovies,
    getMovieById: getMovieById,
    getCart: getCart,
    saveCart: saveCart,
    addToCart: addToCart,
    updateCartItemQuantity: updateCartItemQuantity,
    removeFromCart: removeFromCart,
    clearCart: clearCart,
    getOrders: getOrders,
    addOrder: addOrder
  };
})(typeof window !== 'undefined' ? window : this);
