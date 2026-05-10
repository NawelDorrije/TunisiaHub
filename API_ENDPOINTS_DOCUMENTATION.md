# TunisiaHub Backend REST API Documentation

## Overview
Complete catalog of all REST API endpoints available in the TunisiaHub backend. This document details all GET endpoints that retrieve data, along with their paths, return types, and role-based access restrictions.

**API Base URL:** `http://localhost:8089`

---

## Authentication & Authorization

### Roles
- **CLIENT** - Regular users who book/purchase services
- **OWNER** - Business owners managing shops/accommodations
- **ADMIN** - System administrators with full access
- **GUEST** - Unauthenticated users (limited public access)

### Authentication Mechanism
- **Method:** JWT (JSON Web Token)
- **Location:** `Authorization: Bearer <token>`
- **Token Contains:** email, role, expiration time
- **User Registration:** `/api/auth/register` - can register as CLIENT or OWNER (ADMIN role cannot be self-assigned)

### CORS Configuration
- **Allowed Origin:** `http://localhost:4200` (Angular dev server)
- **Allowed Methods:** GET, POST, PUT, PATCH, DELETE, OPTIONS
- **Credentials:** Allowed

---

## GET Endpoints by Module

### 1. AUTHENTICATION MODULE (`/api/auth`)
**Public Access** - No authentication required

| Endpoint | Method | Returns | Role Restriction |
|----------|--------|---------|------------------|
| `/api/auth/register` | POST | AuthResponse (userId, token, role, email, nom, prenom) | Public |
| `/api/auth/login` | POST | AuthResponse (userId, token, role, email, nom, prenom) | Public |

**AuthResponse Structure:**
```json
{
  "id": Long,
  "token": String (JWT),
  "role": String (CLIENT|OWNER|ADMIN),
  "email": String,
  "nom": String,
  "prenom": String
}
```

---

### 2. SOUVENIR SHOPS MODULE (`/api/souvenir-shops/*`)

#### 2.1 Shops Endpoints (`/api/souvenir-shops/shops`)

| Endpoint | Method | Returns | Role Restriction |
|----------|--------|---------|------------------|
| `/api/souvenir-shops/shops` | GET | List\<Shop\> | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/shops/{id}` | GET | Shop | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/shops/owner/{ownerId}` | GET | List\<Shop\> | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/shops/{id}/products` | GET | List\<Product\> | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/shops/{id}/orders` | GET | List\<Order\> | OWNER, ADMIN |
| `/api/souvenir-shops/shops/{id}/reviews` | GET | List\<Review\> | CLIENT, OWNER, ADMIN |

**Shop Data Structure:**
- id: Long
- name: String
- description: String
- location: String
- ownerId: Long
- categories: List
- products: List (reference)
- orders: List (reference)
- reviews: List (reference)

#### 2.2 Products Endpoints (`/api/souvenir-shops/products`)

| Endpoint | Method | Returns | Role Restriction |
|----------|--------|---------|------------------|
| `/api/souvenir-shops/products` | GET | List\<Product\> | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/products/{id}` | GET | Product | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/products/shop/{shopId}` | GET | List\<Product\> | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/products/{id}/reviews` | GET | List\<Review\> | CLIENT, OWNER, ADMIN |

**Product Data Structure:**
- id: Long
- name: String
- description: String
- price: BigDecimal
- image: String (URL)
- shopId: Long
- category: String
- stock: Integer
- rating: Double
- reviews: List (reference)

#### 2.3 Orders Endpoints (`/api/souvenir-shops/orders`)

| Endpoint | Method | Returns | Role Restriction |
|----------|--------|---------|------------------|
| `/api/souvenir-shops/orders` | GET | List\<Order\> | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/orders/{id}` | GET | Order | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/orders/me` | GET | List\<Order\> | Authenticated Users |
| `/api/souvenir-shops/orders/user/{userId}` | GET | List\<Order\> | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/orders/shop/{shopId}` | GET | List\<Order\> | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/orders/{id}/items` | GET | List\<OrderItem\> | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/orders/{id}/payments` | GET | List\<Payment\> | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/orders/issues` | GET | List\<String\> | OWNER, ADMIN |

**Order Data Structure:**
- id: Long
- userId: Long
- shopId: Long
- orderDate: Date
- totalAmount: BigDecimal
- status: String (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
- items: List\<OrderItem\>
- payments: List\<Payment\>

**Order Issues Detection API:**
- Returns detected problems with orders (stock issues, pricing inconsistencies, etc.)

#### 2.4 Order Items Endpoints (`/api/souvenir-shops/order-items`)

| Endpoint | Method | Returns | Role Restriction |
|----------|--------|---------|------------------|
| `/api/souvenir-shops/order-items` | GET | List\<OrderItem\> | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/order-items/{id}` | GET | OrderItem | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/order-items/order/{orderId}` | GET | List\<OrderItem\> | CLIENT, OWNER, ADMIN |

**OrderItem Data Structure:**
- id: Long
- orderId: Long
- productId: Long
- quantity: Integer
- pricePerItem: BigDecimal
- totalPrice: BigDecimal

#### 2.5 Reviews Endpoints (`/api/souvenir-shops/reviews`)
**Dual Path:** `/api/souvenir-shops/reviews` and `/api/souvenir-shops/souvenir-shops/reviews`

| Endpoint | Method | Returns | Role Restriction |
|----------|--------|---------|------------------|
| `/api/souvenir-shops/reviews/shop/{shopId}` | GET | List\<Review\> | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/reviews/product/{productId}` | GET | List\<Review\> | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/reviews/shop/{shopId}/with-eligibility` | GET | ReviewEligibilityResponse | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/reviews/product/{productId}/with-eligibility` | GET | ReviewEligibilityResponse | CLIENT, OWNER, ADMIN |

**Review Data Structure:**
- id: Long
- userId: Long
- shopId: Long (for shop reviews) OR productId: Long (for product reviews)
- rating: Integer (1-5)
- comment: String
- createdDate: Date
- modifiedDate: Date

**ReviewEligibilityResponse Structure:**
- reviews: List\<Review\>
- eligibleForReview: Boolean (whether current user can leave a review)
- eligibilityReason: String (explanation if not eligible)

#### 2.6 Payments Endpoints (`/api/souvenir-shops/payments`)

| Endpoint | Method | Returns | Role Restriction |
|----------|--------|---------|------------------|
| `/api/souvenir-shops/payments` | GET | List\<Payment\> | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/payments/{id}` | GET | Payment | CLIENT, OWNER, ADMIN |
| `/api/souvenir-shops/payments/order/{orderId}` | GET | List\<Payment\> | CLIENT, OWNER, ADMIN |

**Payment Data Structure:**
- id: Long
- orderId: Long
- amount: BigDecimal
- paymentMethod: String (CREDIT_CARD, DEBIT_CARD, PAYPAL, etc.)
- status: String (PENDING, COMPLETED, FAILED, REFUNDED)
- transactionDate: Date
- reference: String (transaction reference)

---

### 3. ACCOMMODATIONS MODULE (`/api/accommodations`)

#### 3.1 Accommodations Endpoints

| Endpoint | Method | Returns | Role Restriction | Notes |
|----------|--------|---------|------------------|-------|
| `/api/accommodations/getAll` | GET | List\<Accommodation\> | **PUBLIC** | No auth required |
| `/api/accommodations/get/{id}` | GET | Accommodation | **PUBLIC** | No auth required |

**Accommodation Data Structure:**
- id: Long
- title: String
- description: String
- price: Double
- capacite: Integer (capacity)
- location: String
- type: String (HOTEL, VILLA, APARTEMENT, MAISON, etc.)
- image: String (URL)
- amenities: List\<String\>
- reviews: List (reference)

---

### 4. ACCOMMODATION REVIEWS MODULE (`/api/reviews`)

#### 4.1 Accommodation Reviews Endpoints

| Endpoint | Method | Returns | Role Restriction | Notes |
|----------|--------|---------|------------------|-------|
| `/api/reviews/getAll` | GET | List\<AccommodationReview\> | **PUBLIC** | No auth required |
| `/api/reviews/get/{id}` | GET | AccommodationReview | **PUBLIC** | No auth required |
| `/api/reviews/accommodation/{accommodationId}` | GET | List\<AccommodationReview\> | **PUBLIC** | No auth required |

**AccommodationReview Data Structure:**
- id: Long
- accommodationId: Long
- userId: Long
- rating: Integer (1-5)
- comment: String
- reviewDate: Date
- lastModified: Date

---

### 5. CAMPINGS MODULE (`/api/campings`)

| Endpoint | Method | Returns | Role Restriction |
|----------|--------|---------|------------------|
| `/api/campings` | GET | List\<Camping\> | Authenticated (Default) |
| `/api/campings/{id}` | GET | Camping | Authenticated (Default) |

**Camping Data Structure:**
- id: Long
- name: String
- description: String
- location: String
- price: Double
- amenities: List\<String\>
- contactInfo: String
- spots: List (reference)

---

### 6. CAMPING SPOTS MODULE (`/api/spots`)

| Endpoint | Method | Returns | Role Restriction |
|----------|--------|---------|------------------|
| `/api/spots` | GET | List\<Spot\> | Authenticated (Default) |
| `/api/spots/{id}` | GET | Spot | Authenticated (Default) |

**Spot Data Structure:**
- id: Long
- campingId: Long
- spotNumber: String
- capacity: Integer
- amenities: List\<String\>
- available: Boolean
- price: Double

---

### 7. RESERVATIONS MODULE (`/api/reservations`)

| Endpoint | Method | Returns | Role Restriction |
|----------|--------|---------|------------------|
| `/api/reservations` | GET | List\<Reservation\> | Authenticated (Default) |
| `/api/reservations/{id}` | GET | Reservation | Authenticated (Default) |

**Reservation Data Structure:**
- id: Long
- userId: Long
- resourceId: Long
- resourceType: String (CAMPING, ACCOMMODATION, etc.)
- startDate: Date
- endDate: Date
- status: String (PENDING, CONFIRMED, CANCELLED)
- totalPrice: Double

---

### 8. COMPLAINTS MODULE (`/api/complaints`)

| Endpoint | Method | Returns | Role Restriction |
|----------|--------|---------|------------------|
| `/api/complaints` | GET | List\<ComplaintView\> | Authenticated (Default) |

**ComplaintView Data Structure:**
- id: Long
- description: String
- date: Date
- reportedByUserId: String
- reservationId: Long (nullable)
- status: String (OPEN, CLOSED, RESOLVED, IN_REVIEW)

---

### 9. USERS MODULE (`/api/users`)

| Endpoint | Method | Returns | Role Restriction |
|----------|--------|---------|------------------|
| `/api/users` | GET | List\<User\> | **ADMIN ONLY** |
| `/api/users/{id}` | GET | User | **ADMIN ONLY** |

**User Data Structure:**
- id: Long
- nom: String (last name)
- prenom: String (first name)
- email: String (unique)
- motDePasse: String (hashed password)
- role: RoleUser (CLIENT, OWNER, ADMIN)
- createdDate: Date
- lastLogin: Date

---

## Image Service (`/api/images`)

### Image Upload Endpoints (All POST, requires authentication)

| Endpoint | Method | Request | Response | Role |
|----------|--------|---------|----------|------|
| `/api/images/shop` | POST | MultipartFile (file) | String (URL) | Authenticated |
| `/api/images/product` | POST | MultipartFile (file) | String (URL) | Authenticated |
| `/api/images/shop/describe` | POST | file, name?, category?, city? | ImageUploadAnalysisResponse | Authenticated |
| `/api/images/product/describe` | POST | file, name?, shopName?, price? | ImageUploadAnalysisResponse | Authenticated |

**ImageUploadAnalysisResponse Structure:**
- url: String (Cloudinary image URL)
- suggestedDescription: String (AI-generated description)

**Features:**
- Images stored on Cloudinary
- AI-powered image description generation
- Automatic categorization based on context

---

## Role-Based Access Control Summary

### PUBLIC Endpoints (No Authentication Required)
```
GET /api/auth/**  (register/login)
GET /api/accommodations/getAll
GET /api/accommodations/get/{id}
GET /api/reviews/getAll
GET /api/reviews/get/{id}
GET /api/reviews/accommodation/{accommodationId}
```

### CLIENT Role Permissions
- ✅ Create/update/delete own reviews
- ✅ Create orders (POST souvenir-shops/orders)
- ✅ Delete own orders (DELETE souvenir-shops/orders/{id})
- ✅ View own orders (/me endpoint)
- ✅ View all reviews (read-only)
- ✅ View products and shops
- ✅ Upload images for personal use
- ✅ Create payments
- ✅ Update own payments

### OWNER Role Permissions
- **All CLIENT permissions PLUS:**
- ✅ Create/edit/delete own shops (POST, PUT, DELETE /shops)
- ✅ Create/edit/delete own products (POST, PUT, DELETE /products)
- ✅ View orders for own shops (`/api/souvenir-shops/shops/{id}/orders`)
- ✅ Update order status
- ✅ Detect order issues (`/api/souvenir-shops/orders/issues`)
- ✅ View all reviews and manage own reviews
- ✅ Full review eligibility check

### ADMIN Role Permissions
- ✅ **Full access to all endpoints**
- ✅ Manage all users (`/api/users/**`)
- ✅ Manage accommodations (create/edit/delete)
- ✅ Manage all shops as if owner
- ✅ Manage all orders
- ✅ Manage order items (create/edit/delete)
- ✅ Delete payments
- ✅ Update complaint status
- ✅ View all system data

---

## Security Headers & Configuration

### Enabled
- **CORS**: Configured for Angular dev server (localhost:4200)
- **CSRF Protection**: ❌ Disabled for API (stateless JWT)
- **Session Management**: Stateless (JWT-based)

### Headers Exposed
- All headers are exposed (`*`)
- Credentials passed with requests

---

## Request/Response Formats

### Standard Response Structure
- **Success (2xx):** Returns entity or list of entities directly (or wrapped in ResponseEntity)
- **Error (4xx/5xx):** Returns ApiErrorResponse

### ApiErrorResponse Structure
```json
{
  "timestamp": Date,
  "status": Integer (HTTP status),
  "error": String (error message),
  "message": String (detailed error message),
  "path": String (request path)
}
```

---

## Error Handling

| Error Type | HTTP Status | Trigger |
|-----------|------------|---------|
| Invalid Credentials | 401 | Wrong password or invalid token |
| Insufficient Permissions | 403 | User role doesn't allow action |
| Not Found | 404 | Resource ID doesn't exist |
| Bad Request | 400 | Invalid input parameters |
| Validation Error | 400 | Failed bean validation |
| File Upload Exceeded | 413 | File size exceeds limit |
| Internal Server Error | 500 | Unhandled exception |

---

## Notable Features

### 1. Order Issue Detection (`/api/souvenir-shops/orders/issues`)
- Validates order consistency
- Checks stock availability across orders
- Identifies pricing inconsistencies
- Returns list of detected issues as strings

### 2. AI Image Description Service
- Analyzes uploaded product/shop images
- Generates contextual descriptions
- Takes into account:
  - Image content
  - Product/shop metadata (name, category, price)
  - Context (city, shop name)

### 3. Review Eligibility Checking (`/with-eligibility` endpoints)
- Determines if user can leave review
- Provides reason if not eligible
- Considers:
  - Purchase history
  - Review cooldown periods
  - Rating requirements

### 4. Multi-modal Review System
- Shop reviews from customers
- Product reviews from buyers
- Accommodation reviews from guests
- Tracks rating and comments

---

## Data Flow Examples

### Shopping Flow
1. **Browse:** GET `/api/souvenir-shops/shops` → see all shops
2. **Filter:** GET `/api/souvenir-shops/products/shop/{shopId}` → see shop products
3. **Details:** GET `/api/souvenir-shops/reviews/product/{productId}` → see reviews
4. **Order:** POST `/api/souvenir-shops/orders` → create order
5. **Track:** GET `/api/souvenir-shops/orders/me` → view own orders
6. **Payment:** POST `/api/souvenir-shops/payments` → pay for order
7. **Review:** POST `/api/souvenir-shops/reviews/product/{productId}` → leave review

### Shop Management Flow (OWNER)
1. **Auth:** POST `/api/auth/login` → get JWT token
2. **Create Shop:** POST `/api/souvenir-shops/shops` → register shop
3. **Add Products:** POST `/api/souvenir-shops/products` → list products
4. **View Orders:** GET `/api/souvenir-shops/shops/{id}/orders` → order list
5. **Check Status:** GET `/api/souvenir-shops/orders/{id}` → order details
6. **Update:** PUT `/api/souvenir-shops/orders/{id}/status` → fulfill order
7. **Diagnose:** GET `/api/souvenir-shops/orders/issues` → check problems

---

## Testing with Postman

Import the provided Postman collection: `/backend/postman/SouvenirsShops.postman_collection.json`

This collection includes pre-configured requests for all major endpoints with:
- Example request bodies
- Environment variables for base URL and token
- Authorization headers setup

