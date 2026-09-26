# Customer Loyalty Points System

A Java Spring Boot customer loyalty and e-commerce web application built for demo and learning purposes. It includes customer authentication, product browsing, cart and checkout flow, loyalty point accrual, reward redemption, membership tiers, notifications, and an admin dashboard.

## Tech Stack

- Java 21
- Spring Boot 3.3.4
- Spring Web
- Spring Data JPA
- Hibernate
- H2 Database (default local development database)
- MySQL connector support for production or custom environments
- Maven
- Static HTML/CSS/JS frontend served from `src/main/resources/static`

## Features

- Customer sign up and login
- Product catalog and product detail pages
- Shopping cart and checkout flow
- Loyalty point earning on purchases
- Rewards and redemption flow
- Membership tiers and loyalty statistics
- Notifications and messaging
- Admin overview and management screens
- Seeded demo users for quick local testing

## Demo Accounts

Customer:
- Email: `customer@loyalty.com`
- Password: `customer123`

Admin:
- Email: `admin@loyalty.com`
- Password: `admin123`

## Run Locally

From the project root:

```bash
./mvnw.cmd spring-boot:run
```

Then open:

- http://localhost:8080
- http://localhost:8080/login.html
- http://localhost:8080/admin-login.html

## Database Configuration

The project is configured to use an in-memory H2 database by default so it starts cleanly in local development environments without requiring a MySQL install.

Default configuration in `src/main/resources/application.properties`:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:h2:mem:loyalty_db;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:sa}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}
```

If you want to use MySQL instead, set environment variables before running the app:

```bash
set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/loyalty_db
set SPRING_DATASOURCE_USERNAME=root
set SPRING_DATASOURCE_PASSWORD=your_password
./mvnw.cmd spring-boot:run
```

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
├── .gitignore
└── target/
```

## Notes

- This is a UI-focused demo application with backend logic preserved.
- Frontend pages are served from `src/main/resources/static`.
- Build artifacts and generated files are intentionally ignored by Git.

## License

This project is intended for academic/demo use and is not published as a production-ready commercial deployment.
