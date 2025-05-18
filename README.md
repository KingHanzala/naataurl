# naataurl

A Spring Boot-based URL shortener backend with JWT authentication and Redis caching.

## Table of Contents
- [Overview](#overview)
- [Setup & Requirements](#setup--requirements)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
  - [Auth APIs](#auth-apis-auth)
  - [URL Shortener APIs](#url-shortener-apis)
- [Authentication](#authentication)
- [CORS & Frontend Integration](#cors--frontend-integration)
- [Docker/Redis](#dockerredis)

---

## Overview
This project provides a backend for a URL shortener service. It supports user registration, login, JWT-based authentication, and URL shortening. Redis is used for caching, and PostgreSQL is the default database.

## Setup & Requirements
- Java 17+
- Maven
- Redis (see Docker section)
- PostgreSQL (configure in `application.properties`)

## Running the Application
1. **Clone the repository**
2. **Start Redis (optional, for caching):**
   ```sh
   docker-compose up -d
   ```
3. **Build and run the Spring Boot app:**
   ```sh
   mvn spring-boot:run
   ```

## API Endpoints

### Auth APIs (`/auth`)

#### `POST /auth/login`
- **Request:**
  ```json
  { "email": "user@example.com", "password": "yourpassword" }
  ```
- **Response:**
  ```json
  { "token": "<JWT>", "user": { ... } }
  ```
- **Description:** Logs in a user and returns a JWT token.

#### `POST /auth/register`
- **Request:**
  ```json
  { "username": "yourname", "email": "user@example.com", "password": "yourpassword" }
  ```
- **Response:**
  ```json
  { "token": "<JWT>", "user": { ... } }
  ```
- **Description:** Registers a new user and returns a JWT token.

#### `POST /auth/logout`
- **Description:** Logs out the current user (stateless, but clears security context).

---

### URL Shortener APIs

#### `POST /api/create-url`
- **Headers:**
  - `Authorization: Bearer <JWT>`
- **Request:**
  ```json
  { "longUrl": "https://example.com/very/long/url" }
  ```
- **Response:**
  ```json
  "shortUrl"
  ```
- **Description:** Creates a short URL for the authenticated user.

#### `GET /{shortUrl}`
- **Description:** Redirects to the original URL. No authentication required.

---

## Authentication
- All `/api/*` endpoints require a valid JWT token in the `Authorization` header.
- Auth endpoints (`/auth/*`) are public except for logout.

## CORS & Frontend Integration
- CORS is enabled for `http://localhost:3000` by default (see `SecurityConfig.java`).
- Update the allowed origins in `SecurityConfig` if your frontend runs elsewhere.

## Docker/Redis
- Redis is required for caching. Start it with:
  ```sh
  docker-compose up -d
  ```
- Default Redis port: `6379`

---

## Notes
- Update your database and Redis connection settings in `application.properties` as needed.
- For OAuth2 login, see additional configuration in the backend (not covered here).

---

## Contact & Contributing
Feel free to open issues or PRs for improvements!
