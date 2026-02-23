# Movie Ticket Store

A complete movie ticket shopping website built with **HTML**, **CSS**, and **vanilla JavaScript**. Uses **Local Storage** for auth, cart, and orders—no backend required.

## Features

- **Login / Sign up** – Email & password stored in Local Storage; validation and redirect to Home when authenticated.
- **Home** – Movie grid with poster, title, price, rating; search by name; nav (Home, Cart, Orders, Logout).
- **Movie details** – Full description, genre, rating, price; Add to Cart (quantity increases for duplicates).
- **Cart** – List with quantity controls, remove item, per-item total, grand total, **Checkout** button.
- **Payment** – After Checkout: collect card name, number, expiry, CVV, optional billing address; validate and “process” (saves order, clears cart).
- **Orders** – Purchase history with Order ID, date, items, and total; persisted in Local Storage.

## UI/UX

- Dark theme, card-based layout, sticky nav.
- Responsive (mobile and desktop), hover effects.
- Accessible (labels, ARIA where needed).

## How to run

1. Open the project folder in a terminal.
2. Serve the site with a local server (required for correct behavior and for Lighthouse):

   **Node (npx):**
   ```bash
   npx serve .
   ```
   Then open the URL shown (e.g. `http://localhost:3000`).

   **Python 3:**
   ```bash
   python -m http.server 8080
   ```
   Then open `http://localhost:8080`.

3. Open `index.html` (or the root URL) to start at the Login page.

## Lighthouse performance

- Scripts use `defer`; images use `loading="lazy"` and `decoding="async"` where appropriate.
- Run Lighthouse from Chrome DevTools (F12 → Lighthouse) against the URL of your local server (e.g. `http://localhost:3000`) for accurate Performance scoring.

## File structure

```
movie-ticket-store/
├── index.html          # Login / Sign up
├── home.html           # Movie list
├── movie-details.html  # Single movie + Add to Cart
├── cart.html           # Cart + Checkout button
├── payment.html        # Payment form → place order
├── orders.html         # Order history
├── css/
│   └── style.css       # Global styles
├── js/
│   ├── storage.js      # Local Storage + seed movies
│   ├── auth.js         # Login/signup (index only)
│   ├── nav.js          # Logout (all pages with nav)
│   ├── home.js         # Home grid + search
│   ├── movie-details.js
│   ├── cart.js
│   ├── payment.js
│   └── orders.js
└── README.md
```

## Test account

After opening the app, use **Sign Up** to create an account (e.g. `test@test.com` / `password123`), then **Login**. No backend—everything is stored in the browser’s Local Storage.
