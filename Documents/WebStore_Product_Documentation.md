# WebStore E-Commerce Platform
## Product Documentation

**Version:** 1.0  
**Date:** January 2024  
**Document Type:** Product Documentation

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Introduction](#introduction)
3. [System Architecture](#system-architecture)
4. [Technology Stack](#technology-stack)
5. [Features & Functionality](#features--functionality)
6. [API Documentation](#api-documentation)
7. [Authentication & Authorization](#authentication--authorization)
8. [Installation & Setup](#installation--setup)
9. [Configuration Guide](#configuration-guide)
10. [Data Models](#data-models)
11. [Error Handling](#error-handling)
12. [Troubleshooting](#troubleshooting)
13. [Appendix](#appendix)

---

## Executive Summary

**WebStore** is a comprehensive multi-vendor e-commerce platform designed to facilitate product catalog management, seller administration, and customer interactions. The platform provides a robust RESTful API backend with role-based access control, supporting both administrative and seller user types.

### Key Highlights

- **Multi-vendor Support:** Manage multiple sellers with individual product catalogs
- **Role-Based Access Control:** Separate authentication flows for Admin and Seller users
- **Google OAuth Integration:** Secure authentication using Google OAuth 2.0
- **WhatsApp Business API Integration:** Enable customer interactions via WhatsApp
- **RESTful API:** Comprehensive API for all platform operations
- **Database Migrations:** Version-controlled database schema using Flyway

### Target Audience

- **Administrators:** Full platform management capabilities
- **Sellers:** Product and inventory management
- **Developers:** API integration and customization
- **End Customers:** Product browsing and purchasing (via WhatsApp)

---

## Introduction

### Purpose

WebStore is an enterprise-grade e-commerce platform that enables businesses to manage product catalogs, sellers, and customer interactions through a unified API. The platform supports multi-vendor operations with granular access control and modern authentication mechanisms.

### Scope

This documentation covers:

- System architecture and design
- API endpoints and usage
- Authentication and authorization mechanisms
- Installation and configuration
- Data models and relationships
- Integration guides

### Document Conventions

- **Code blocks:** Used for code examples, API requests, and configuration
- **Bold text:** Important terms and concepts
- **Italic text:** Emphasis and notes
- **Tables:** Structured information presentation

---

## System Architecture

### High-Level Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────────┐
│   Frontend      │    │   Spring Boot    │    │    PostgreSQL       │
│   Application   │◄──►│   REST API       │◄──►│    Database         │
│                 │    │                  │    │                     │
│ • Admin Panel   │    │ • Controllers    │    │ • Products          │
│ • Seller Portal │    │ • Services       │    │ • Sellers           │
│ • Customer App  │    │ • Security       │    │ • Categories        │
└─────────────────┘    └──────────────────┘    └─────────────────────┘
         │                       │
         │                       │
    ┌────▼────┐             ┌────▼────┐
    │ Google  │             │WhatsApp │
    │  OAuth  │             │Business │
    │         │             │   API   │
    └─────────┘             └─────────┘
```

### Component Overview

1. **Presentation Layer:** Frontend applications (Admin Dashboard, Seller Portal)
2. **API Layer:** Spring Boot REST Controllers
3. **Business Logic Layer:** Service implementations
4. **Data Access Layer:** JPA Repositories
5. **Database Layer:** PostgreSQL with Flyway migrations
6. **Security Layer:** JWT authentication and authorization
7. **Integration Layer:** Google OAuth, WhatsApp Business API

---

## Technology Stack

### Backend Technologies

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 3.4.4 |
| **Database** | PostgreSQL | Latest |
| **ORM** | Hibernate/JPA | Included in Spring Boot |
| **Security** | Spring Security | Included in Spring Boot |
| **JWT** | JJWT | 0.12.3 |
| **Migration** | Flyway | 10.0.0 |
| **Validation** | Jakarta Validation | 3.0.2 |
| **HTTP Client** | OkHttp | 4.11.0 |

### Development Tools

- **Build Tool:** Gradle
- **Testing:** JUnit 5, Mockito
- **Code Quality:** Checkstyle, PMD
- **Documentation:** Markdown, Postman Collections

---

## Features & Functionality

### 1. Authentication & Authorization

#### Google OAuth Authentication
- Secure login using Google OAuth 2.0
- Support for both Admin and Seller user types
- JWT token-based session management
- Token expiration: 24 hours (configurable)

#### Role-Based Access Control
- **Admin Role:** Full system access
  - Manage catalogues, categories, sellers
  - View all products across all sellers
  - System configuration and management

- **Seller Role:** Limited access
  - Manage own products only
  - View own product inventory
  - Update own product information
  - Cannot create/modify catalogues or categories

### 2. Product Management

#### Core Features
- **CRUD Operations:** Create, Read, Update, Delete products
- **Product Search:** Search products by name or description
- **Pagination:** Efficient data retrieval with pagination support
- **Multi-Currency Pricing:** Support for multiple currencies per product
- **Stock Management:** Track product inventory
- **Image Support:** Product image URLs
- **Category Association:** Products linked to categories within catalogues

#### Product Attributes
- Product name (unique)
- Product description
- Catalogue-Category mapping
- Seller association
- Image URL
- Stock quantity
- Multiple price points (different currencies)

### 3. Seller Management

#### Features
- Seller registration and profile management
- Status management (Active/Inactive)
- Joining date tracking
- Email-based unique identification
- Seller-product relationship management

### 4. Catalogue Management

#### Features
- Create and manage product catalogues
- Catalogue-category mapping
- Catalogue search functionality
- Admin-only access for catalogue operations

### 5. Category Management

#### Features
- Category creation and management
- Category description
- Unique category names
- Admin-only access for category operations

### 6. Currency Management

#### Features
- Multi-currency support
- Currency code, name, and symbol
- Currency-based product pricing

### 7. WhatsApp Business API Integration

#### Features
- Interactive product browsing via WhatsApp
- Category and product navigation
- Product details and pricing information
- Order management through WhatsApp
- Webhook-based message handling

---

## API Documentation

### Base URL

```
http://localhost:8080/api
```

### Authentication

All protected endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer <jwt_token>
```

---

### Authentication Endpoints

#### POST /api/auth/google

Authenticate user using Google OAuth token.

**Request Body:**
```json
{
  "googleToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyMzQ1NiIsInR5cCI6IkpXVCJ9...",
  "userType": "ADMIN"  // or "SELLER"
}
```

**Success Response (200 OK) - Admin:**
```json
{
  "jwtToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "sellerId": null,
  "userId": 1,
  "email": "admin@webstore.com",
  "name": "Admin User",
  "role": "ADMIN"
}
```

**Success Response (200 OK) - Seller:**
```json
{
  "jwtToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "sellerId": 1,
  "userId": null,
  "email": "seller@example.com",
  "name": "John Doe",
  "role": "SELLER"
}
```

**Error Responses:**
- `401 Unauthorized` - Invalid Google token
- `403 Forbidden` - User not authorized or seller inactive
- `404 Not Found` - User/Seller not found

---

### Product Endpoints

#### GET /api/products

Get all products with optional pagination.

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: all)

**Response (200 OK):**
```json
[
  {
    "productId": 1,
    "productName": "Smartphone X",
    "productDescription": "Latest smartphone model",
    "catalogueId": 1,
    "categoryId": 1,
    "sellerId": 1,
    "imageUrl": "https://example.com/image.jpg",
    "stock": 50,
    "productPrices": [
      {
        "priceId": 1,
        "price": 999.99,
        "currencyCode": "USD"
      }
    ]
  }
]
```

#### GET /api/products/{id}

Get product by ID.

**Path Parameters:**
- `id`: Product ID

**Response (200 OK):**
```json
{
  "productId": 1,
  "productName": "Smartphone X",
  "productDescription": "Latest smartphone model",
  "catalogueId": 1,
  "categoryId": 1,
  "sellerId": 1,
  "imageUrl": "https://example.com/image.jpg",
  "stock": 50,
  "productPrices": [...]
}
```

#### POST /api/products

Create a new product.

**Request Body:**
```json
{
  "productName": "New Product",
  "productDescription": "Product description",
  "catalogueId": 1,
  "categoryId": 1,
  "sellerId": 1,
  "imageUrl": "https://example.com/image.jpg",
  "stock": 100
}
```

**Response (201 Created):**
```json
{
  "productId": 2,
  "productName": "New Product",
  ...
}
```

**Note:** Sellers can only create products for themselves. The `sellerId` is automatically set to the authenticated seller's ID.

#### PUT /api/products/{id}

Update an existing product.

**Path Parameters:**
- `id`: Product ID

**Request Body:** Same as POST

**Response (200 OK):** Updated product object

**Note:** Sellers can only update their own products.

#### DELETE /api/products/{id}

Delete a product.

**Path Parameters:**
- `id`: Product ID

**Response (204 No Content)**

**Note:** Sellers can only delete their own products.

#### GET /api/products/search

Search products by name or description.

**Query Parameters:**
- `searchTerm` (optional): Search keyword

**Response (200 OK):** Array of matching products

---

### Seller Endpoints

#### GET /api/sellers

Get all sellers with optional pagination.

**Query Parameters:**
- `page` (optional): Page number
- `size` (optional): Page size

**Response (200 OK):**
```json
[
  {
    "sellerId": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "status": "ACTIVE",
    "role": "SELLER",
    "joiningDate": "2024-01-15"
  }
]
```

#### GET /api/sellers/{id}

Get seller by ID.

**Response (200 OK):** Seller object

#### POST /api/sellers

Create a new seller (Admin only).

**Request Body:**
```json
{
  "name": "Jane Smith",
  "email": "jane@example.com",
  "status": "ACTIVE",
  "joiningDate": "2024-01-20"
}
```

**Response (201 Created):** Created seller object

#### PUT /api/sellers/{id}

Update seller information (Admin only).

**Response (200 OK):** Updated seller object

#### DELETE /api/sellers/{id}

Delete a seller (Admin only).

**Response (204 No Content)**

---

### Category Endpoints

#### GET /api/categories

Get all categories with optional pagination.

**Response (200 OK):** Array of category objects

#### GET /api/categories/{id}

Get category by ID.

**Response (200 OK):** Category object

#### GET /api/categories/search

Search categories by name.

**Query Parameters:**
- `searchTerm` (optional): Search keyword

**Response (200 OK):** Array of matching categories

#### POST /api/categories

Create a new category (Admin only).

**Request Body:**
```json
{
  "categoryName": "Electronics",
  "categoryDescription": "Electronic devices"
}
```

**Response (201 Created):** Created category object

#### PUT /api/categories/{id}

Update category (Admin only).

**Response (200 OK):** Updated category object

#### DELETE /api/categories/{id}

Delete category (Admin only).

**Response (204 No Content)**

---

### Catalogue Endpoints

#### GET /api/catalogues

Get all catalogues with optional pagination.

**Response (200 OK):** Array of catalogue objects

#### GET /api/catalogues/search

Search catalogues by name.

**Query Parameters:**
- `name`: Catalogue name to search

**Response (200 OK):** Array of matching catalogues

#### POST /api/catalogues

Create a new catalogue (Admin only).

**Request Body:**
```json
{
  "catalogueName": "Summer Collection",
  "catalogueDescription": "Products for summer season"
}
```

**Response (200 OK):** Created catalogue object

#### PUT /api/catalogues/{id}

Update catalogue (Admin only).

**Response (200 OK):** Updated catalogue object

#### DELETE /api/catalogues/{id}

Delete catalogue (Admin only).

**Response (204 No Content)**

---

### Currency Endpoints

#### GET /api/currencies

Get all currencies with pagination.

**Query Parameters:**
- `page` (default: 0): Page number
- `size` (default: 5): Page size

**Response (200 OK):** Array of currency objects

#### GET /api/currencies/{id}

Get currency by ID.

**Response (200 OK):** Currency object

#### POST /api/currencies

Create a new currency.

**Request Body:**
```json
{
  "currencyCode": "USD",
  "currencyName": "US Dollar",
  "currencySymbol": "$"
}
```

**Response (201 Created):** Created currency object

#### PUT /api/currencies/{id}

Update currency.

**Response (200 OK):** Updated currency object

---

### Catalogue-Category Mapping Endpoints

#### POST /api/catalogue-categories

Create a mapping between catalogue and category.

**Request Body:**
```json
{
  "catalogueId": 1,
  "categoryId": 1
}
```

**Response (200 OK):** Success message

#### GET /api/catalogue-categories

Get all catalogue-category mappings.

**Response (200 OK):** Array of mapping objects

---

## Authentication & Authorization

### Google OAuth Flow

1. **Frontend initiates Google OAuth:**
   - User clicks "Sign in with Google"
   - Google OAuth popup appears
   - User authenticates with Google
   - Google returns ID token

2. **Backend verification:**
   - Frontend sends Google ID token to `/api/auth/google`
   - Backend verifies token with Google
   - Backend extracts user email from token

3. **User lookup:**
   - For Admin: Lookup in `users` table
   - For Seller: Lookup in `sellers` table

4. **JWT token generation:**
   - Backend generates JWT token with user information
   - Token includes: userId/sellerId, email, role
   - Token expires in 24 hours

5. **Token usage:**
   - Frontend stores token
   - Include token in Authorization header for all API calls

### JWT Token Structure

**Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload (Admin):**
```json
{
  "userId": 1,
  "email": "admin@webstore.com",
  "role": "ADMIN",
  "sub": "admin@webstore.com",
  "iat": 1705896000,
  "exp": 1705982400
}
```

**Payload (Seller):**
```json
{
  "sellerId": 1,
  "email": "seller@example.com",
  "role": "SELLER",
  "sub": "seller@example.com",
  "iat": 1705896000,
  "exp": 1705982400
}
```

### Role-Based Permissions

| Operation | Admin | Seller |
|-----------|-------|--------|
| Create Catalogue | ✅ | ❌ |
| Update Catalogue | ✅ | ❌ |
| Delete Catalogue | ✅ | ❌ |
| Create Category | ✅ | ❌ |
| Update Category | ✅ | ❌ |
| Delete Category | ✅ | ❌ |
| Create Product | ✅ | ✅ (own only) |
| Update Product | ✅ | ✅ (own only) |
| Delete Product | ✅ | ✅ (own only) |
| View All Products | ✅ | ❌ (own only) |
| Manage Sellers | ✅ | ❌ |

---

## Installation & Setup

### Prerequisites

1. **Java Development Kit (JDK)**
   - Version: 21 or higher
   - Download: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)

2. **PostgreSQL Database**
   - Version: 12 or higher
   - Download: [PostgreSQL](https://www.postgresql.org/download/)

3. **Google OAuth Credentials**
   - Google Cloud Console project
   - OAuth 2.0 Client ID
   - Authorized redirect URIs configured

4. **Build Tool**
   - Gradle (included via wrapper)

### Database Setup

1. **Create PostgreSQL Database:**
```sql
CREATE DATABASE web_store;
```

2. **Configure Database Connection:**
   - Update `application-local.properties` with your database credentials

3. **Run Migrations:**
   - Flyway will automatically run migrations on application startup
   - Migrations are located in `src/main/resources/database/versions/`

### Application Setup

1. **Clone/Download the project**

2. **Configure Application Properties:**
   - Edit `src/main/resources/application-local.properties`
   - Set database connection details
   - Configure JWT secret
   - Set Google OAuth client ID

3. **Build the Application:**
```bash
./gradlew build
```

4. **Run the Application:**
```bash
./gradlew bootRun
```

Or using Java directly:
```bash
java -jar build/libs/WebStore-1.0-SNAPSHOT.jar
```

5. **Verify Installation:**
   - Application should start on `http://localhost:8080`
   - Check logs for successful startup
   - Verify database migrations completed

---

## Configuration Guide

### Database Configuration

```properties
# Database Connection
spring.datasource.url=jdbc:postgresql://localhost:5432/web_store
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Flyway Configuration

```properties
# Flyway Database Migration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:database/versions
spring.flyway.url=${spring.datasource.url}
spring.flyway.user=${spring.datasource.username}
spring.flyway.password=${spring.datasource.password}
spring.flyway.validate-on-migrate=false
```

### JWT Configuration

```properties
# JWT Token Configuration
jwt.secret=${JWT_SECRET:your-256-bit-secret-key-here-make-it-long-and-secure-for-production-use-change-this-in-production}
jwt.expiration=${JWT_EXPIRATION:86400000}  # 24 hours in milliseconds
```

**Important:** 
- Generate a strong random secret for production
- Use environment variables for secrets
- Minimum 256 bits (32 characters) recommended

### Google OAuth Configuration

```properties
# Google OAuth Configuration
google.oauth.client-id=${GOOGLE_CLIENT_ID:your-google-client-id-here}
```

**Setup Steps:**
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Enable Google+ API
4. Create OAuth 2.0 Client ID
5. Add authorized redirect URIs
6. Copy Client ID to configuration

### WhatsApp Configuration (Optional)

```properties
# WhatsApp Business API Configuration
whatsapp.webhook.verify-token=${WEBHOOK_VERIFY_TOKEN:your-verify-token}
whatsapp.api.access-token=${ACCESS_API_TOKEN:your-access-token}
whatsapp.api.version=v23.0
whatsapp.api.phone-number-id=your-phone-number-id
whatsapp.api.base-url=https://your-ngrok-url.ngrok.io
whatsapp.api.graph-url=https://graph.facebook.com
```

### Server Configuration

```properties
# Server Configuration
server.port=8080
server.address=0.0.0.0
spring.main.allow-bean-definition-overriding=true
```

---

## Data Models

### Product Entity

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| `productId` | Integer | Primary key | Auto-generated |
| `productName` | String(50) | Product name | Unique, Not null |
| `productDescription` | String(100) | Product description | Optional |
| `catalogueCategoryId` | Integer | Catalogue-Category mapping | Foreign key, Not null |
| `sellerId` | Integer | Associated seller | Foreign key, Not null |
| `imageUrl` | String(500) | Product image URL | Optional |
| `stock` | Integer | Stock quantity | Default: 0 |

**Relationships:**
- Many-to-One with `CatalogueCategory`
- Many-to-One with `Seller`
- One-to-Many with `ProductPrice`

### Seller Entity

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| `sellerId` | Integer | Primary key | Auto-generated |
| `name` | String(100) | Seller name | Not null |
| `email` | String(100) | Seller email | Unique, Not null |
| `status` | Enum | Seller status | ACTIVE/INACTIVE, Default: ACTIVE |
| `role` | Enum | Seller role | SELLER (only), Default: SELLER |
| `joiningDate` | LocalDate | Joining date | Not null |

**Enums:**
- `SellerStatus`: ACTIVE, INACTIVE
- `SellerRole`: SELLER

### User Entity (Admin)

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| `userId` | Integer | Primary key | Auto-generated |
| `username` | String(50) | Username | Unique, Not null |
| `email` | String(100) | User email | Unique, Not null |
| `fullName` | String(50) | Full name | Optional |
| `role` | String(20) | User role | ADMIN, Not null |

### Category Entity

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| `categoryId` | Integer | Primary key | Auto-generated |
| `categoryName` | String(50) | Category name | Unique, Not null |
| `categoryDescription` | String(100) | Category description | Optional |

### Catalogue Entity

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| `catalogueId` | Integer | Primary key | Auto-generated |
| `catalogueName` | String(100) | Catalogue name | Unique, Not null |
| `catalogueDescription` | String(255) | Catalogue description | Optional |

### CatalogueCategory Entity (Mapping)

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| `catalogueCategoryId` | Integer | Primary key | Auto-generated |
| `catalogueId` | Integer | Catalogue reference | Foreign key, Not null |
| `categoryId` | Integer | Category reference | Foreign key, Not null |

### ProductPrice Entity

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| `priceId` | Integer | Primary key | Auto-generated |
| `productId` | Integer | Product reference | Foreign key, Not null |
| `price` | BigDecimal | Price value | Not null |
| `currencyId` | Integer | Currency reference | Foreign key, Not null |

### Currency Entity

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| `currencyId` | Integer | Primary key | Auto-generated |
| `currencyCode` | String(5) | Currency code (USD, EUR) | Unique, Not null |
| `currencyName` | String(50) | Currency name | Not null |
| `currencySymbol` | String(5) | Currency symbol ($, €) | Not null |

---

## Error Handling

### Error Response Format

All error responses follow a consistent format:

```json
{
  "timestamp": "2024-01-20T10:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with ID: 123",
  "path": "/api/products/123"
}
```

### HTTP Status Codes

| Status Code | Meaning | Common Scenarios |
|-------------|---------|------------------|
| `200 OK` | Success | Successful GET, PUT requests |
| `201 Created` | Resource created | Successful POST requests |
| `204 No Content` | Success, no content | Successful DELETE requests |
| `400 Bad Request` | Invalid request | Validation errors, malformed JSON |
| `401 Unauthorized` | Authentication required | Missing/invalid JWT token |
| `403 Forbidden` | Access denied | Insufficient permissions |
| `404 Not Found` | Resource not found | Invalid ID, resource doesn't exist |
| `500 Internal Server Error` | Server error | Unexpected exceptions |

### Common Error Scenarios

#### Authentication Errors

**401 Unauthorized - Invalid Token:**
```json
{
  "timestamp": "2024-01-20T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired JWT token",
  "path": "/api/products"
}
```

**403 Forbidden - Insufficient Permissions:**
```json
{
  "timestamp": "2024-01-20T10:30:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Sellers cannot create catalogues",
  "path": "/api/catalogues"
}
```

#### Validation Errors

**400 Bad Request - Validation Failed:**
```json
{
  "timestamp": "2024-01-20T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/products",
  "errors": [
    {
      "field": "productName",
      "message": "Product name is required"
    }
  ]
}
```

#### Resource Not Found

**404 Not Found:**
```json
{
  "timestamp": "2024-01-20T10:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with ID: 999",
  "path": "/api/products/999"
}
```

---

## Troubleshooting

### Common Issues and Solutions

#### 1. Database Connection Issues

**Problem:** Application fails to connect to database

**Solutions:**
- Verify PostgreSQL is running
- Check database credentials in `application-local.properties`
- Ensure database `web_store` exists
- Verify network connectivity to database server

#### 2. Migration Failures

**Problem:** Flyway migrations fail on startup

**Solutions:**
- Check migration files in `src/main/resources/database/versions/`
- Verify SQL syntax is correct
- Check for conflicting migration versions
- Review Flyway logs for specific errors

#### 3. Authentication Failures

**Problem:** Google OAuth login fails

**Solutions:**
- Verify Google OAuth client ID is correct
- Check authorized redirect URIs in Google Cloud Console
- Ensure Google+ API is enabled
- Verify Google token is valid and not expired

#### 4. JWT Token Issues

**Problem:** Token validation fails

**Solutions:**
- Verify JWT secret matches in configuration
- Check token expiration (default: 24 hours)
- Ensure token is included in Authorization header
- Verify token format: `Bearer <token>`

#### 5. Permission Denied Errors

**Problem:** 403 Forbidden errors for valid operations

**Solutions:**
- Verify user role in JWT token
- Check if operation requires Admin role
- Ensure seller is trying to access only their own resources
- Verify user status is ACTIVE

#### 6. Port Already in Use

**Problem:** Application fails to start - port 8080 in use

**Solutions:**
- Change port in `application-local.properties`: `server.port=8081`
- Stop other applications using port 8080
- Use `netstat` or `lsof` to find process using port

### Logging

Application logs are available in:
- Console output
- Log files (if configured)

Enable debug logging by setting:
```properties
logging.level.com.webstore=DEBUG
```

### Health Checks

Check application health:
```bash
curl http://localhost:8080/actuator/health
```

(If Spring Boot Actuator is configured)

---

## Appendix

### A. API Endpoint Summary

| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| POST | `/api/auth/google` | Google OAuth login | No | - |
| GET | `/api/products` | Get all products | Yes | Admin/Seller |
| GET | `/api/products/{id}` | Get product by ID | Yes | Admin/Seller |
| POST | `/api/products` | Create product | Yes | Admin/Seller |
| PUT | `/api/products/{id}` | Update product | Yes | Admin/Seller |
| DELETE | `/api/products/{id}` | Delete product | Yes | Admin/Seller |
| GET | `/api/products/search` | Search products | Yes | Admin/Seller |
| GET | `/api/sellers` | Get all sellers | Yes | Admin |
| GET | `/api/sellers/{id}` | Get seller by ID | Yes | Admin |
| POST | `/api/sellers` | Create seller | Yes | Admin |
| PUT | `/api/sellers/{id}` | Update seller | Yes | Admin |
| DELETE | `/api/sellers/{id}` | Delete seller | Yes | Admin |
| GET | `/api/categories` | Get all categories | Yes | Admin/Seller |
| POST | `/api/categories` | Create category | Yes | Admin |
| PUT | `/api/categories/{id}` | Update category | Yes | Admin |
| DELETE | `/api/categories/{id}` | Delete category | Yes | Admin |
| GET | `/api/catalogues` | Get all catalogues | Yes | Admin/Seller |
| POST | `/api/catalogues` | Create catalogue | Yes | Admin |
| PUT | `/api/catalogues/{id}` | Update catalogue | Yes | Admin |
| DELETE | `/api/catalogues/{id}` | Delete catalogue | Yes | Admin |
| GET | `/api/currencies` | Get all currencies | Yes | Admin/Seller |
| POST | `/api/currencies` | Create currency | Yes | Admin/Seller |

### B. Environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `JWT_SECRET` | JWT signing secret | Yes (Production) | - |
| `JWT_EXPIRATION` | Token expiration (ms) | No | 86400000 |
| `GOOGLE_CLIENT_ID` | Google OAuth client ID | Yes | - |
| `DB_URL` | Database URL | Yes | - |
| `DB_USERNAME` | Database username | Yes | - |
| `DB_PASSWORD` | Database password | Yes | - |

### C. Postman Collection

A Postman collection is available at:
```
Documents/Postman/SpringBoot.postman_collection.json
```

**Import Instructions:**
1. Open Postman
2. Click "Import"
3. Select the JSON file
4. Configure environment variables
5. Start testing APIs

### D. Database Schema Diagram

Refer to: `Documents/Product_Catalog_ERD.png`

### E. Glossary

- **JWT:** JSON Web Token - A compact, URL-safe token format
- **OAuth:** Open Authorization - Authorization framework
- **REST:** Representational State Transfer - Architectural style for APIs
- **CRUD:** Create, Read, Update, Delete - Basic data operations
- **Flyway:** Database migration tool
- **JPA:** Java Persistence API - Java specification for ORM

### F. References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [JWT.io](https://jwt.io/) - JWT token decoder and validator
- [Google OAuth 2.0](https://developers.google.com/identity/protocols/oauth2)
- [WhatsApp Business API](https://developers.facebook.com/docs/whatsapp)

### G. Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | January 2024 | Initial release |
| | | - Core product management |
| | | - Seller management |
| | | - Google OAuth authentication |
| | | - JWT token-based authorization |
| | | - Role-based access control |
| | | - WhatsApp Business API integration |

### H. Support and Contact

For technical support or questions:
- Review this documentation
- Check application logs
- Refer to API endpoint documentation
- Contact development team

---

## Document Information

**Document Version:** 1.0  
**Last Updated:** January 2024  
**Author:** WebStore Development Team  
**Status:** Current

---

*This document is subject to change. Please refer to the latest version for the most up-to-date information.*

