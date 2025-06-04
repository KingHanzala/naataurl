# naataurl

A Spring Boot-based URL shortener backend with JWT authentication, OAuth2 support, and Redis caching

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Setup & Requirements](#setup--requirements)
- [Running the Application](#running-the-application)
  - [Local Development](#local-development)
  - [Docker Deployment](#docker-deployment)
- [API Endpoints](#api-endpoints)
  - [Auth APIs](#auth-apis-auth)
  - [OAuth2 APIs](#oauth2-apis)
  - [URL Shortener APIs](#url-shortener-apis)
  - [User APIs](#user-apis)
- [Authentication](#authentication)
- [CORS & Frontend Integration](#cors--frontend-integration)
- [Environment Variables](#environment-variables)

---

## Overview
This project provides a backend for a URL shortener service. It supports user registration, login, JWT-based authentication, OAuth2 integration, and URL shortening with Redis caching for improved performance.

## Features
- User authentication with JWT
- OAuth2 integration (Google & GitHub)
- URL shortening with custom aliases
- Redis caching for improved performance
- Email verification
- Password reset functionality
- Safe URL checking with Google Safe Browsing API
- Docker support for easy deployment

## Setup & Requirements
- Java 17+
- Maven
- PostgreSQL
- Redis
- SendGrid API Key for email functionality
- Docker & Docker Compose (for containerized deployment)

## Running the Application

### Local Development
1. **Clone the repository**
2. **Set up environment variables:**
   ```sh
   export SENDGRID_API_KEY=your_sendgrid_api_key
   export MAIL_FROM=your_verified_sender_email
   export JWT_SECRET=your_jwt_secret
   export GOOGLE_CLIENT_ID=your_google_client_id
   export GOOGLE_CLIENT_SECRET=your_google_client_secret
   export GITHUB_CLIENT_ID=your_github_client_id
   export GITHUB_CLIENT_SECRET=your_github_client_secret
   export GOOGLE_SAFEBROWSING_API=your_safebrowsing_api_key
   ```
3. **Clean and install** 
   ```sh
   mvn clean install
   ```
4. **Build and run the Spring Boot app:**
   ```sh
   mvn spring-boot:run
   ```

### Docker Deployment
1. **Create a `.env` file with all required environment variables**
2. **Build and run using Docker Compose:**
   ```sh
   docker-compose up --build
   ```

## Environment Variables
The following environment variables are required for the application:

| Variable | Description | Required |
|----------|-------------|----------|
| SENDGRID_API_KEY | API key for SendGrid email service | Yes |
| MAIL_FROM | Verified sender email address | Yes |
| JWT_SECRET | Secret key for JWT token generation | Yes |
| GOOGLE_CLIENT_ID | Google OAuth2 client ID | Yes |
| GOOGLE_CLIENT_SECRET | Google OAuth2 client secret | Yes |
| GITHUB_CLIENT_ID | GitHub OAuth2 client ID | Yes |
| GITHUB_CLIENT_SECRET | GitHub OAuth2 client secret | Yes |
| GOOGLE_SAFEBROWSING_API | Google Safe Browsing API key | Yes |
| SPRING_DATASOURCE_URL | PostgreSQL database URL | Yes |
| SPRING_DATASOURCE_USERNAME | Database username | Yes |
| SPRING_DATASOURCE_PASSWORD | Database password | Yes |
| REDIS_HOST | Redis host address | Yes |
| REDIS_PORT | Redis port | Yes |
| REDIS_PASSWORD | Redis password | Yes |
| FRONTEND_URL | Frontend application URL | Yes |
| SHORT_FRONTEND_URL | Shortened frontend URL | Yes |

## API Endpoints

### Auth APIs (`/auth`)

#### `POST /auth/login`
- **Request:**
  ```json
  { "email": "user@example.com", "password": "yourpassword" }
  ```
- **Response:**
  ```json
  { 
    "token": "<JWT>", 
    "user": { 
      "userId": 123,
      "userName": "example",
      "userEmail": "user@example.com"
    } 
  }
  ```
- **Error Responses:**
  ```json
  {
    "status": 400,
    "message": "User is not registered"
  }
  ```
  ```json
  {
    "status": 401,
    "message": "User is not verified."
  }
  ```
- **Description:** Logs in a user and returns a JWT token.

#### `POST /auth/register`
- **Request:**
  ```json
  { "username": "yourname", "email": "user@example.com", "password": "yourpassword" }
  ```
- **Response:**
  ```json
  { "message": "SignUp successful! Please verify your email before logging in." }
  ```
- **Error Responses:**
  ```json
  {
    "status": 400,
    "message": "User already signed up. Please Login or verify your email if not done already."
  }
  ```
  ```json
  {
    "status": 503,
    "message": "Signup Failed! Please try again."
  }
  ```
- **Description:** Registers a new user and sends a verification email.

#### `POST /auth/logout`
- **Response:**
  ```json
  "Logged out successfully"
  ```
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
- **Error Responses:**
  ```json
  {
    "status": 400,
    "message": "Invalid Request. Please provide a valid password"
  }
  ```
  ```json
  {
    "status": 401,
    "message": "Invalid Authentication"
  }
  ```
- **Description:** Allows authenticated users to reset their password.

#### `POST /auth/forgot-password`
- **Request:**
  ```json
  { 
    "userEmail": "user@example.com", 
    "password": "newpassword",
    "confirmationToken": "token"
  }
  ```
- **Response:**
  ```json
  "Password reset successful"
  ```
- **Error Responses:**
  ```json
  {
    "status": 400,
    "message": "Invalid Request. Please provide a valid email address"
  }
  ```
  ```json
  {
    "status": 400,
    "message": "User not found!"
  }
  ```
  ```json
  {
    "status": 400,
    "message": "Invalid operation"
  }
  ```
- **Description:** Allows users to reset their password using their email address and confirmation token.

#### `GET /auth/get-reset-token`
- **Request:**
  ```json
  { "email": "user@example.com" }
  ```
- **Response:**
  ```json
  { "alreadySent": true/false }
  ```
- **Error Responses:**
  ```json
  {
    "status": 400,
    "message": "Invalid Request. Please provide a valid email address"
  }
  ```
  ```json
  {
    "status": 400,
    "message": "User not found!"
  }
  ```
  ```json
  {
    "status": 400,
    "message": "User logged in via OAuth2. Trying using the third party application to login."
  }
  ```
  ```json
  {
    "status": 503,
    "message": "Password Reset Failed! Please try again."
  }
  ```
- **Description:** Generates or retrieves a password reset token for a user and sends a reset email.

#### `GET /auth/validate-reset-token`
- **Request:**
  ```json
  { "confirmationToken": "<token>" }
  ```
- **Response:**
  ```json
  { 
    "email": "user@example.com", 
    "tokenExpired": true/false, 
    "confirmationToken": "<token>" 
  }
  ```
- **Error Responses:**
  ```json
  {
    "status": 400,
    "message": "Invalid Request."
  }
  ```
  ```json
  {
    "status": 400,
    "message": "User not found!"
  }
  ```
  ```json
  {
    "status": 400,
    "message": "User logged in via OAuth2. Trying using the third party application to login."
  }
  ```
- **Description:** Validates a password reset token and returns token status.

#### `GET /auth/validate-token`
- **Request:**
  ```json
  { "confirmationToken": "<token>" }
  ```
- **Response:**
  ```json
  { 
    "email": "user@example.com", 
    "tokenExpired": true/false, 
    "confirmationToken": "<token>" 
  }
  ```
- **Error Responses:**
  ```json
  {
    "status": 400,
    "message": "Invalid Request."
  }
  ```
  ```json
  {
    "status": 400,
    "message": "User not found!"
  }
  ```
  ```json
  {
    "status": 400,
    "message": "User logged in via OAuth2. Trying using the third party application to login."
  }
  ```
  ```json
  {
    "status": 400,
    "message": "User is already verified"
  }
  ```
- **Description:** Validates a user verification token and returns token status.

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
- **Error Responses:**
  ```json
  {
    "status": 401,
    "message": "Invalid Authentication"
  }
  ```
- **Description:** Creates a short URL for the authenticated user.

#### `GET /{shortUrl}`
- **Description:** Redirects to the original URL. No authentication required.
- **Error Responses:**
  ```json
  {
    "status": 404,
    "message": "Short URL not found"
  }
  ```

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
- **Error Responses:**
  ```json
  {
    "status": 401,
    "message": "Invalid Authentication"
  }
  ```
  ```json
  {
    "status": 404,
    "message": "User not found"
  }
  ```
- **Description:** Returns the user's dashboard information including their profile, created URLs, and available credits.

---

## Authentication
- All `/api/*` endpoints require a valid JWT token in the `Authorization` header.
- Auth endpoints (`/auth/*`) are public except for logout.
- OAuth2 endpoints (`/oauth2/authorization/*`) are provided by Spring Security and are used for social login.

## CORS & Frontend Integration
- CORS is enabled for `http://localhost:3000` by default (see `SecurityConfig.java`).
- Update the allowed origins in `SecurityConfig` if your frontend runs elsewhere.

## Email Configuration
- The application uses SendGrid for sending emails
- Configure the following in `application.properties`:
  ```properties
  spring.mail.host=smtp.sendgrid.net
  spring.mail.port=465
  spring.mail.username=apikey
  spring.mail.password=${SENDGRID_API_KEY}
  spring.mail.properties.mail.smtp.from=Cryptoutils <hello@cryptoutils.xyz>
  ```
- Set the `SENDGRID_API_KEY` environment variable with your SendGrid API key
- Ensure the sender email is verified in your SendGrid account

## Notes
- Update your database connection settings in `application.properties` as needed.
- For OAuth2 login, see additional configuration in the backend (not covered here).
- Make sure to verify your sender email address in SendGrid before sending emails.

---

## Contact & Contributing
Feel free to open issues or PRs for improvements!