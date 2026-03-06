# 🛒 E-Commerce REST API

> A production-ready RESTful API for e-commerce platform built with Spring Boot — featuring JWT authentication, product management, shopping cart, and order processing.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat-square&logo=postgresql)
![Maven](https://img.shields.io/badge/Maven-3.x-red?style=flat-square&logo=apachemaven)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?style=flat-square&logo=docker)
![License](https://img.shields.io/badge/License-MIT-brightgreen?style=flat-square)

---

## 📖 Table of Contents
- [About](#about)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Contact](#contact)

---

## 📌 About

A fully functional backend API for an e-commerce platform, built to demonstrate clean REST API design with Spring Boot. This project covers the complete shopping flow — from user registration and authentication, product browsing, cart management, to order processing.

Designed with a layered architecture (Controller → Service → Repository) and secured with JWT-based authentication. All endpoints are documented via Swagger UI for easy exploration and testing.

---

## ✨ Features

- ✅ **JWT Authentication** — Secure register & login with BCrypt password hashing
- ✅ **Role-based Access Control** — USER and ADMIN roles with endpoint-level protection
- ✅ **Product Management** — Full CRUD with category filtering, search, and pagination
- ✅ **Shopping Cart** — Add, update quantity, remove items, and clear cart
- ✅ **Order Processing** — Checkout from cart, order history, and status tracking
- ✅ **Stock Management** — Automatic stock deduction and validation on checkout
- ✅ **Global Exception Handling** — Consistent error response format across all endpoints
- ✅ **Input Validation** — Request validation with descriptive error messages
- ✅ **API Documentation** — Interactive Swagger UI / OpenAPI 3
- ✅ **Docker Support** — Run the entire stack with a single command

---

## 🛠️ Tech Stack

| Layer | Technology                     |
|---|--------------------------------|
| Language | Java 17                        |
| Framework | Spring Boot 4.x                |
| Security | Spring Security + JWT (jjwt)   |
| Database | PostgreSQL 15                  |
| ORM | Spring Data JPA / Hibernate    |
| Build Tool | Maven                          |
| Documentation | Springdoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 + Mockito              |
| Containerization | Docker + Docker Compose        |

---

## 🚀 Getting Started

### Prerequisites

Make sure the following are installed:
- Java 17+
- Maven 3.8+
- PostgreSQL 15+
- Docker & Docker Compose (optional)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/ecommerce-spring-api.git
cd ecommerce-spring-api
```

2. **Create the database**
```sql
CREATE DATABASE ecommerce_db;
```

3. **Configure environment**

Copy the example env file and fill in your values:
```bash
cp .env.example .env
```

Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce_db
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
jwt.secret=your_jwt_secret_key_min_256bit
jwt.expiration=86400000
```

4. **Run the application**
```bash
mvn spring-boot:run
```

Application runs at: `http://localhost:8080`

Swagger UI available at: `http://localhost:8080/swagger-ui.html`

### Running with Docker

```bash
docker-compose up --build
```

This will spin up both the Spring Boot app and PostgreSQL automatically.

---

## 📚 API Documentation

Full interactive documentation is available at `http://localhost:8080/swagger-ui.html` after running the application.

### Base URL
```
http://localhost:8080/api
```

### Authentication

All protected endpoints require a Bearer token in the Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

### Endpoints Overview

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/auth/register` | Register new user | ❌ |
| POST | `/auth/login` | Login and get JWT token | ❌ |
| GET | `/products` | Get all products (with pagination) | ❌ |
| GET | `/products/{id}` | Get product by ID | ❌ |
| GET | `/categories` | Get all categories | ❌ |
| POST | `/products` | Create new product | ✅ ADMIN |
| PUT | `/products/{id}` | Update product | ✅ ADMIN |
| DELETE | `/products/{id}` | Delete product | ✅ ADMIN |
| GET | `/cart` | Get current user's cart | ✅ USER |
| POST | `/cart/items` | Add item to cart | ✅ USER |
| PUT | `/cart/items/{id}` | Update item quantity | ✅ USER |
| DELETE | `/cart/items/{id}` | Remove item from cart | ✅ USER |
| POST | `/orders/checkout` | Checkout from cart | ✅ USER |
| GET | `/orders` | Get order history | ✅ USER |
| GET | `/orders/{id}` | Get order detail | ✅ USER |
| GET | `/admin/orders` | Get all orders | ✅ ADMIN |
| PUT | `/admin/orders/{id}/status` | Update order status | ✅ ADMIN |

### Example Request & Response

**POST /api/auth/register**
```json
// Request
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securepassword123"
}

// Response 201 Created
{
  "message": "User registered successfully",
  "userId": 1
}
```

**POST /api/auth/login**
```json
// Request
{
  "email": "john@example.com",
  "password": "securepassword123"
}

// Response 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "expiresIn": 86400000
}
```

**GET /api/products?page=0&size=10&category=electronics**
```json
// Response 200 OK
{
  "content": [
    {
      "id": 1,
      "name": "Wireless Headphones",
      "description": "High quality wireless headphones",
      "price": 299000,
      "stock": 50,
      "category": "Electronics"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0
}
```

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/commerce/demo/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   └── SwaggerConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── ProductController.java
│   │   │   ├── CategoryController.java
│   │   │   ├── CartController.java
│   │   │   ├── OrderController.java
│   │   │   └── AdminController.java
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── Product.java
│   │   │   ├── Category.java
│   │   │   ├── Cart.java
│   │   │   ├── CartItem.java
│   │   │   ├── Order.java
│   │   │   └── OrderItem.java
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── ProductRepository.java
│   │   │   └── OrderRepository.java
│   │   ├── security/
│   │   │   ├── JwtUtil.java
│   │   │   ├── JwtAuthFilter.java
│   │   │   └── UserDetailsServiceImpl.java
│   │   └── service/
│   │       ├── AuthService.java
│   │       ├── ProductService.java
│   │       ├── CartService.java
│   │       └── OrderService.java
│   └── resources/
│       └── application.properties
├── test/
│   └── java/
│       └── (unit & integration tests)
├── Dockerfile
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run with coverage report
mvn verify
```

---

## 📬 Contact

**[Your Name]** — Java Backend Developer

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue?style=flat-square&logo=linkedin)](https://linkedin.com/in/yourusername)
[![GitHub](https://img.shields.io/badge/GitHub-Follow-black?style=flat-square&logo=github)](https://github.com/yourusername)
[![Email](https://img.shields.io/badge/Email-Contact-red?style=flat-square&logo=gmail)](mailto:your@email.com)

---

> ⭐ If you find this project useful, please consider giving it a **star**!