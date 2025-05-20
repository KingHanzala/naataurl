# naataurl

A Spring Boot-based URL shortener backend with JWT authentication

## Table of Contents
- [Overview](#overview)
- [Setup & Requirements](#setup--requirements)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
  - [Auth APIs](#auth-apis-auth)
  - [OAuth2 APIs](#oauth2-apis)
  - [URL Shortener APIs](#url-shortener-apis)
  - [User APIs](#user-apis)
- [Authentication](#authentication)
- [CORS & Frontend Integration](#cors--frontend-integration)

---

## Overview
This project provides a backend for a URL shortener service. It supports user registration, login, JWT-based authentication, and URL shortening. PostgreSQL is the default database.

## Setup & Requirements
- Java 17+
- Maven
- PostgreSQL (configure in `application.properties`)

## Running the Application
1. **Clone the repository**
2. **Clean and install** 
```sh
  mvn clean install
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
- **Error Codes:**
  - `400 Bad Request`: Invalid email or password.
  - `401 Unauthorized`: User is not registered or not verified.
- **Description:** Logs in a user and returns a JWT token.

#### `POST /auth/register`
- **Request:**
  ```json
  { "username": "yourname", "email": "user@example.com", "password": "yourpassword" }
  ```
- **Response:**
  ```json
  { "confirmationToken": "<token>" }
  ```
- **Error Codes:**
  - `400 Bad Request`: Email already exists.
- **Description:** Registers a new user and returns a confirmation token for email verification.

#### `POST /auth/logout`
- **Description:** Logs out the current user (stateless, but clears security context).

#### `POST /auth/reset-password`
- **Headers:**
  - `Authorization: Bearer <JWT>`
- **Request:**
  ```json
  { "password": "newpassword" }
  ```
- **Response:**
  ```json
  "Password reset successful"
  ```
- **Error Codes:**
  - `400 Bad Request`: Invalid request or password.
  - `401 Unauthorized`: Invalid authentication.
- **Description:** Allows authenticated users to reset their password.

#### `POST /auth/forgot-password`
- **Request:**
  ```json
  { "userEmail": "user@example.com", "password": "newpassword" }
  ```
- **Response:**
  ```json
  "Password reset successful"
  ```
- **Error Codes:**
  - `400 Bad Request`: Invalid email or password, User not found.
- **Description:** Allows users to reset their password using their email address.

#### `GET /auth/get-reset-token`
- **Request:**
  ```json
  { "email": "user@example.com" }
  ```
- **Response:**
  ```json
  { "confirmationToken": "<token>", "isExistingToken": true/false }
  ```
- **Error Codes:**
  - `400 Bad Request`: Invalid email, User not found, User logged in via OAuth2.
- **Description:** Generates or retrieves a password reset token for a user.

#### `GET /auth/validate-reset-token`
- **Request:**
  ```json
  { "confirmationToken": "<token>" }
  ```
- **Response:**
  ```json
  { "email": "user@example.com", "isValid": true/false, "newToken": "<token>" }
  ```
- **Error Codes:**
  - `400 Bad Request`: Invalid token, User not found, User logged in via OAuth2.
- **Description:** Validates a password reset token and optionally generates a new one.

#### `GET /auth/validate-token`
- **Request:**
  ```json
  { "confirmationToken": "<token>" }
  ```
- **Response:**
  ```json
  { "email": "user@example.com", "isValid": true/false, "newToken": "<token>" }
  ```
- **Error Codes:**
  - `400 Bad Request`: Invalid token, User not found, User logged in via OAuth2, User already verified.
- **Description:** Validates a user verification token and optionally generates a new one.

---

### OAuth2 APIs (Provided by Spring Security)

#### `GET /oauth2/authorization/google`
- **Description:** Initiates the OAuth2 login flow for Google. Redirects the user to Google's consent screen.

#### `GET /oauth2/authorization/github`
- **Description:** Initiates the OAuth2 login flow for GitHub. Redirects the user to GitHub's consent screen.

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
  {"shortUrl": "<shortUrl>"}
  ```
- **Error Codes:**
  - `401 Unauthorized`: Invalid authentication.
  - `404 Not Found`: Short URL not found.
- **Description:** Creates a short URL for the authenticated user.

#### `GET /{shortUrl}`
- **Description:** Redirects to the original URL. No authentication required.
- **Error Codes:**
  - `404 Not Found`: Short URL not found.

### User APIs

#### `GET /api/dashboard`
- **Headers:**
  - `Authorization: Bearer <JWT>`
- **Response:**
  ```json
  {
    "userResponse": {
      "userId": 123,
      "userName": "example",
      "email": "user@example.com"
    },
    "urlsMappedList": [
      {
        "originalUrl": "https://example.com/long/url",
        "shortUrl": "abc123",
        "urlClicks": 42,
        "createdDtm": "2024-03-19T12:34:56",
        "userId": 123
      }
    ],
    "availableCredits": 5
  }
  ```
- **Error Codes:**
  - `401 Unauthorized`: Invalid authentication.
  - `404 Not Found`: User not found.
- **Description:** Returns the user's dashboard information including their profile, created URLs, and available credits.

---

## Authentication
- All `/api/*` endpoints require a valid JWT token in the `Authorization` header.
- Auth endpoints (`/auth/*`) are public except for logout.
- OAuth2 endpoints (`/oauth2/authorization/*`) are provided by Spring Security and are used for social login.

## CORS & Frontend Integration
- CORS is enabled for `http://localhost:3000` by default (see `SecurityConfig.java`).
- Update the allowed origins in `SecurityConfig` if your frontend runs elsewhere.

## Notes
- Update your database connection settings in `application.properties` as needed.
- For OAuth2 login, see additional configuration in the backend (not covered here).

---

## Contact & Contributing
Feel free to open issues or PRs for improvements!