# Customer Loyalty Points System

A Java Spring Boot customer loyalty and e-commerce application with authentication, product browsing, cart and checkout, loyalty points, rewards, membership tiers, notifications, and an admin dashboard.

## Tech Stack

- Java 21
- Spring Boot 3.3.4
- Spring Web and Spring Data JPA
- Hibernate
- MySQL
- Maven
- Static HTML/CSS/JavaScript frontend served from `src/main/resources/static`

## Features

- Customer registration and login
- Product catalog, cart, and checkout
- Loyalty point earning and reward redemption
- Membership tiers and notifications
- Customer and product reviews
- Admin dashboard and management screens
- Seeded demo users for local testing

## Demo Accounts

Customer:
- Email: `customer@loyalty.com`
- Password: `customer123`

Admin:
- Email: `admin@loyalty.com`
- Password: `admin123`

## Run Locally

The application uses the local MySQL server by default and connects to the `loyalty_db` schema. Set your MySQL password in PowerShell before starting the app:

```powershell
$env:SPRING_DATASOURCE_PASSWORD = 'your-mysql-password'
.\mvnw.cmd spring-boot:run
```

Then open:

- http://localhost:8080
- http://localhost:8080/login.html
- http://localhost:8080/admin-login.html

## Database

The datasource defaults to `jdbc:mysql://localhost:3306/loyalty_db` with username `root`. The password is read from `SPRING_DATASOURCE_PASSWORD` and is not stored in the project. Hibernate creates or updates the application tables at startup.

In MySQL Workbench, connect to your local MySQL server, refresh the schemas, and open `loyalty_db`. Useful tables include `users`, `orders`, `order_items`, `points_transactions`, `cart`, `cart_items`, `rewards`, `redeemed_rewards`, `notifications`, and `reviews`.

The database files are managed by MySQL Server and are not stored in this project folder.

## Project Structure

```text
.
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── pom.xml
├── mvnw.cmd
├── loyalty_db.sql
├── README.md
└── .gitignore
```

## Notes

- Frontend pages are served from `src/main/resources/static`.
- Build artifacts and generated files are ignored by Git.
- This project is intended for academic/demo use and is not published as a production-ready commercial deployment.

## License

This project is intended for academic/demo use.
