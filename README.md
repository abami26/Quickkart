# QuickCart (Java / Spring Boot) — Local Delivery & E-Commerce Platform

Class project for MMS4 NIIT (Java Track). Same QuickCart app as before, rebuilt
with a Java backend using Spring Boot instead of Node.js.

## What's inside

- **Frontend:** plain HTML, CSS, and JavaScript (`src/main/resources/static`)
- **Backend:** Java + Spring Boot (`src/main/java/com/niit/quickcart`)
- **Database:** H2 — a real embedded SQL database that saves to a file on disk
  (`./data/quickcart.mv.db`), not just JSON. Passwords are hashed with BCrypt.

## Before you run it — install Java and Maven

You need two things installed:

1. **Java JDK 17 or newer** — download from https://adoptium.net (choose the
   "Temurin" JDK for your OS). After installing, close and reopen your
   terminal, then check it worked:
   ```
   java -version
   ```
2. **Maven** — download from https://maven.apache.org/download.cgi (get the
   "Binary zip archive"), unzip it somewhere, then add its `bin` folder to
   your PATH (same idea as adding Node.js to PATH). Check it worked:
   ```
   mvn -version
   ```

## How to run it

1. Open a terminal inside this project folder (the one with `pom.xml` in it).
2. Run:
   ```
   mvn spring-boot:run
   ```
   The first run will take a few minutes — Maven downloads all the Spring
   Boot libraries the project depends on. This needs an internet connection.
3. Once you see `QuickCart running at http://localhost:8080`, open that link
   in your browser.

> **Note on how this was built:** I wrote every file for this by hand, but
> the sandbox I work in can't reach Maven's package repository, so I wasn't
> able to run `mvn spring-boot:run` myself to confirm it compiles cleanly —
> unlike the Node.js version, which I did fully test. I checked the code
> carefully, but if you hit an error the first time you run it, copy the
> error message to me and I'll fix it immediately.

## Pages

- `index.html` — shop, product grid, cart drawer, checkout
- `stores.html` — filter partner stores by covered Lagos area
- `help.html` — delivery coverage, location, payment, and order FAQs
- `signup.html` / `login.html` — create an account / sign in
- `orders.html` — see your past orders (only visible when logged in)
- `merchant-signup.html` / `merchant-login.html` — create or access a store application
- `merchant-dashboard.html` — view KYC review status and update store profile and coverage areas

The landing page includes an embedded Google Maps area search. The optional
“Use my location” action needs browser location permission and a secure context
(HTTPS or localhost); users can always choose their area manually. The map
embed and external Google Maps link require an internet connection.

The visitor counter uses an opt-in first-party consent cookie and an HttpOnly
random browser ID. It stores only that ID and its first-seen timestamp, not IP
addresses, account details, or delivery locations. The count represents unique
consenting browsers, not verified people. Cookie settings are available in the
homepage footer.

Checkout currently uses a simulated payment confirmation for demonstration
only. It does not connect to a payment provider, collect payment details, or
charge money; orders record `paymentStatus: demo_only`.

Merchant onboarding is also a demo: it stores business contact/profile fields,
hashes merchant passwords, and records KYC consent and a `pending_review`
status. It does not collect identity documents, bank details, or ID numbers.
Merchant submissions remain private and are not added to the shopper catalog;
there is no admin review/approval workflow or merchant inventory management
yet. Real KYC should use a secure, access-controlled review process before
publishing stores.

Order history shows a simulated delivery timeline for demo orders. Milestones
advance based on order creation time and refresh while a demo order is active.
The route is illustrative only: there is no rider GPS, live map position, or
real delivery status feed.

## API endpoints

| Method | Route              | Auth required | Description               |
|--------|---------------------|:--------------:|----------------------------|
| GET    | `/api/products`     | No             | List all products          |
| GET    | `/api/stores?city=<city>` | No        | List shops and products serving a Lagos area |
| POST   | `/api/visits`       | No             | Count a consenting unique browser |
| POST   | `/api/signup`       | No             | Create an account          |
| POST   | `/api/login`        | No             | Log in, get a token        |
| POST   | `/api/merchant/signup` | No          | Create a Lagos merchant application |
| POST   | `/api/merchant/login`  | No          | Log in to a merchant account |
| GET    | `/api/merchant/me`  | Merchant       | Read merchant profile and KYC status |
| PUT    | `/api/merchant/me`  | Merchant       | Update store profile and service areas |
| GET    | `/api/me`           | Yes            | Get the logged-in user     |
| POST   | `/api/orders`       | Yes            | Place an order using the demo-only payment flow |
| GET    | `/api/orders/mine`  | Yes            | List the user's own orders |

Auth uses a JSON Web Token (JWT), same as the Node.js version — the token is
stored in the browser's `localStorage` after login and sent as
`Authorization: Bearer <token>` on every request that needs it.

The homepage asks for a Lagos area before showing nearby shops and products.
The current demo coverage is seeded for Agege, Alimosho, Ikeja, Kosofe, Ajah,
Lekki, Victoria Island, Somolu, Surulere, and Yaba; other listed areas show an
unavailable message until a shop is added for them.

## Peeking at the database

Spring Boot ships with a web-based database viewer. While the app is running,
open:
```
http://localhost:8080/h2-console
```
and use this JDBC URL to connect (username `sa`, no password):
```
jdbc:h2:file:./data/quickcart
```
You'll see the `USERS`, `ORDERS`, and `SITE_VISITORS` tables — good for showing your lecturer
that data is really being saved.

## Project structure

```
quickcart-java/
├── pom.xml                          # Maven config — lists all dependencies
├── src/main/java/com/niit/quickcart/
│   ├── QuickcartApplication.java    # entry point (main method)
│   ├── model/                       # User, Order, Product
│   ├── repository/                  # database access (Spring Data JPA)
│   ├── controller/                  # REST API endpoints
│   ├── dto/                         # request/response shapes
│   ├── security/                    # JWT creation & validation
│   ├── data/                        # static product catalog
│   └── exception/                   # turns errors into JSON responses
├── src/main/resources/
│   ├── application.properties       # server port, database settings
│   └── static/                      # the frontend (HTML/CSS/JS)
└── data/                            # created automatically — the H2 database file
```

## Notes for your presentation

- Passwords are hashed with BCrypt before being saved — never stored in plain
  text.
- Orders can only be placed by a logged-in user; every protected endpoint
  checks the JWT token first (`JwtUtil.requireUserId`) before doing anything.
- This uses a real relational database (H2) via Spring Data JPA, so you can
  show actual SQL tables in the H2 console — a step up from the Node.js
  version's JSON files.
- Swapping H2 for MySQL later is a one-line change in `application.properties`
  plus adding the MySQL driver dependency — the rest of the code (entities,
  repositories, controllers) stays the same.

## Ideas if you want to extend it further

- Connect real order updates and rider GPS from a store/rider service
- An admin endpoint/page to see all orders across users
- Product search
- Switch H2 for MySQL/PostgreSQL for a "production-style" database
