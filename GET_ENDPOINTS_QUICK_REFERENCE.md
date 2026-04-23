# TunisiaHub REST API - GET Endpoints Quick Reference

## All Available GET Endpoints

| # | Endpoint | Returns | Role Restriction | Public? |
|----|----------|---------|------------------|---------|
| **AUTHENTICATION** |
| 1 | POST `/api/auth/register` | AuthResponse | PUBLIC | ✅ Yes |
| 2 | POST `/api/auth/login` | AuthResponse | PUBLIC | ✅ Yes |
| **SOUVENIR SHOPS - SHOPS** |
| 3 | GET `/api/souvenir-shops/shops` | List\<Shop\> | CLIENT, OWNER, ADMIN | ❌ |
| 4 | GET `/api/souvenir-shops/shops/{id}` | Shop | CLIENT, OWNER, ADMIN | ❌ |
| 5 | GET `/api/souvenir-shops/shops/owner/{ownerId}` | List\<Shop\> | CLIENT, OWNER, ADMIN | ❌ |
| 6 | GET `/api/souvenir-shops/shops/{id}/products` | List\<Product\> | CLIENT, OWNER, ADMIN | ❌ |
| 7 | GET `/api/souvenir-shops/shops/{id}/orders` | List\<Order\> | OWNER, ADMIN | ❌ |
| 8 | GET `/api/souvenir-shops/shops/{id}/reviews` | List\<Review\> | CLIENT, OWNER, ADMIN | ❌ |
| **SOUVENIR SHOPS - PRODUCTS** |
| 9 | GET `/api/souvenir-shops/products` | List\<Product\> | CLIENT, OWNER, ADMIN | ❌ |
| 10 | GET `/api/souvenir-shops/products/{id}` | Product | CLIENT, OWNER, ADMIN | ❌ |
| 11 | GET `/api/souvenir-shops/products/shop/{shopId}` | List\<Product\> | CLIENT, OWNER, ADMIN | ❌ |
| 12 | GET `/api/souvenir-shops/products/{id}/reviews` | List\<Review\> | CLIENT, OWNER, ADMIN | ❌ |
| **SOUVENIR SHOPS - ORDERS** |
| 13 | GET `/api/souvenir-shops/orders` | List\<Order\> | CLIENT, OWNER, ADMIN | ❌ |
| 14 | GET `/api/souvenir-shops/orders/{id}` | Order | CLIENT, OWNER, ADMIN | ❌ |
| 15 | GET `/api/souvenir-shops/orders/me` | List\<Order\> | Authenticated | ❌ |
| 16 | GET `/api/souvenir-shops/orders/user/{userId}` | List\<Order\> | CLIENT, OWNER, ADMIN | ❌ |
| 17 | GET `/api/souvenir-shops/orders/shop/{shopId}` | List\<Order\> | CLIENT, OWNER, ADMIN | ❌ |
| 18 | GET `/api/souvenir-shops/orders/{id}/items` | List\<OrderItem\> | CLIENT, OWNER, ADMIN | ❌ |
| 19 | GET `/api/souvenir-shops/orders/{id}/payments` | List\<Payment\> | CLIENT, OWNER, ADMIN | ❌ |
| 20 | GET `/api/souvenir-shops/orders/issues` | List\<String\> | OWNER, ADMIN | ❌ |
| **SOUVENIR SHOPS - ORDER ITEMS** |
| 21 | GET `/api/souvenir-shops/order-items` | List\<OrderItem\> | CLIENT, OWNER, ADMIN | ❌ |
| 22 | GET `/api/souvenir-shops/order-items/{id}` | OrderItem | CLIENT, OWNER, ADMIN | ❌ |
| 23 | GET `/api/souvenir-shops/order-items/order/{orderId}` | List\<OrderItem\> | CLIENT, OWNER, ADMIN | ❌ |
| **SOUVENIR SHOPS - REVIEWS** |
| 24 | GET `/api/souvenir-shops/reviews/shop/{shopId}` | List\<Review\> | CLIENT, OWNER, ADMIN | ❌ |
| 25 | GET `/api/souvenir-shops/reviews/product/{productId}` | List\<Review\> | CLIENT, OWNER, ADMIN | ❌ |
| 26 | GET `/api/souvenir-shops/reviews/shop/{shopId}/with-eligibility` | ReviewEligibilityResponse | CLIENT, OWNER, ADMIN | ❌ |
| 27 | GET `/api/souvenir-shops/reviews/product/{productId}/with-eligibility` | ReviewEligibilityResponse | CLIENT, OWNER, ADMIN | ❌ |
| **SOUVENIR SHOPS - PAYMENTS** |
| 28 | GET `/api/souvenir-shops/payments` | List\<Payment\> | CLIENT, OWNER, ADMIN | ❌ |
| 29 | GET `/api/souvenir-shops/payments/{id}` | Payment | CLIENT, OWNER, ADMIN | ❌ |
| 30 | GET `/api/souvenir-shops/payments/order/{orderId}` | List\<Payment\> | CLIENT, OWNER, ADMIN | ❌ |
| **ACCOMMODATIONS** |
| 31 | GET `/api/accommodations/getAll` | List\<Accommodation\> | PUBLIC | ✅ Yes |
| 32 | GET `/api/accommodations/get/{id}` | Accommodation | PUBLIC | ✅ Yes |
| **ACCOMMODATION REVIEWS** |
| 33 | GET `/api/reviews/getAll` | List\<AccommodationReview\> | PUBLIC | ✅ Yes |
| 34 | GET `/api/reviews/get/{id}` | AccommodationReview | PUBLIC | ✅ Yes |
| 35 | GET `/api/reviews/accommodation/{accommodationId}` | List\<AccommodationReview\> | PUBLIC | ✅ Yes |
| **CAMPINGS** |
| 36 | GET `/api/campings` | List\<Camping\> | Authenticated | ❌ |
| 37 | GET `/api/campings/{id}` | Camping | Authenticated | ❌ |
| **CAMPING SPOTS** |
| 38 | GET `/api/spots` | List\<Spot\> | Authenticated | ❌ |
| 39 | GET `/api/spots/{id}` | Spot | Authenticated | ❌ |
| **RESERVATIONS** |
| 40 | GET `/api/reservations` | List\<Reservation\> | Authenticated | ❌ |
| 41 | GET `/api/reservations/{id}` | Reservation | Authenticated | ❌ |
| **COMPLAINTS** |
| 42 | GET `/api/complaints` | List\<ComplaintView\> | Authenticated | ❌ |
| **USERS** |
| 43 | GET `/api/users` | List\<User\> | ADMIN | ❌ |
| 44 | GET `/api/users/{id}` | User | ADMIN | ❌ |

---

## Summary Statistics

### Endpoints by Category
| Module | GET Endpoints | Public | Authenticated | Admin-Only |
|--------|---------------|--------|---------------|-----------|
| Authentication | 2 (POST) | 2 | 0 | 0 |
| Souvenir Shops | 25 | 0 | 25 | 2 special |
| Accommodations | 5 | 5 | 0 | 0 |
| Campings | 4 | 0 | 4 | 0 |
| Reservations | 2 | 0 | 2 | 0 |
| Complaints | 1 | 0 | 1 | 0 |
| Users | 2 | 0 | 0 | 2 |
| **TOTAL** | **41** | **7** | **32** | **2** |

### By Role Access
| Role | Accessible Endpoints |
|------|----------------------|
| **PUBLIC (Guest)** | 7 endpoints (Auth + Accommodations + Reviews) |
| **CLIENT** | 32 endpoint (All authenticated minus admin/owner specific) |
| **OWNER** | 35 endpoints (All CLIENT + order mgmt + product mgmt) |
| **ADMIN** | 44 endpoints (Full access) |

### Data Retrieval Categories

#### 🛍️ E-Commerce Data (Souvenir Shops)
- 8 endpoints for shops & products browsing
- 7 endpoints for orders (creation, tracking, status)
- 3 endpoints for related items
- 4 endpoints for reviews with eligibility checks
- 3 endpoints for payments

**Total:** 25 GET endpoints (shops/products/orders), with write operations available via POST/PUT/PATCH

#### 🏨 Accommodations Data
- 2 public GET endpoints (list all, get by ID)
- No role-based restrictions
- Public listing possible without authentication

#### 🏕️ Camping Data
- 2 GET endpoints (campings)
- 2 GET endpoints (spots)
- Requires authentication

#### 📋 Administrative Data
- 2 endpoints for user management (ADMIN only)
- 1 endpoint for system-wide complaints

---

## Response Field Reference

### Core Business Objects

#### Shop
```
- id: Long
- name: String
- description: String
- location: String
- ownerId: Long
- rating: Double
- verified: Boolean
```

#### Product
```
- id: Long
- shopId: Long
- name: String
- description: String
- price: BigDecimal
- image: String (URL)
- category: String
- stock: Integer
- rating: Double
```

#### Order
```
- id: Long
- userId: Long
- shopId: Long
- orderDate: Date
- totalAmount: BigDecimal
- status: PENDING|CONFIRMED|SHIPPED|DELIVERED|CANCELLED
- items: List<OrderItem>
```

#### OrderItem
```
- id: Long
- orderId: Long
- productId: Long
- quantity: Integer
- pricePerItem: BigDecimal
- totalPrice: BigDecimal
```

#### Review (Product/Shop)
```
- id: Long
- userId: Long
- targetId: Long (shopId or productId)
- rating: Integer (1-5)
- comment: String
- createdDate: Date
```

#### Payment
```
- id: Long
- orderId: Long
- amount: BigDecimal
- method: String (CREDIT_CARD|PAYPAL|etc)
- status: PENDING|COMPLETED|FAILED|REFUNDED
- transactionDate: Date
```

#### User
```
- id: Long
- email: String
- nom: String
- prenom: String
- role: CLIENT|OWNER|ADMIN
- createdDate: Date
```

---

## Authentication Header Example

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZSI6IkNMSUVOVCIsImlhdCI6MTcwMzAwMDAwMCwiZXhwIjoxNzAzMDAzNjAwfQ...
```

**Token Payload:**
- `sub` (subject): User email
- `role`: User role (CLIENT, OWNER, or ADMIN)
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp

---

## Key Role-Based Restrictions

| Operation | PUBLIC | CLIENT | OWNER | ADMIN |
|-----------|--------|--------|-------|-------|
| Browse shops/products | ❌ | ✅ | ✅ | ✅ |
| Create shop | ❌ | ❌ | ✅ | ✅ |
| Manage own shop | ❌ | ❌ | ✅ | ✅ |
| Create order | ❌ | ✅ | ✅ | ✅ |
| View own orders | ❌ | ✅ | ✅ | ✅ |
| View all orders | ❌ | ❌ | ✅ | ✅ |
| Update order status | ❌ | ❌ | ✅ (own shop) | ✅ |
| Leave review | ❌ | ✅ | ✅ | ✅ |
| Browse public accommodations | ✅ | ✅ | ✅ | ✅ |
| Create accommodation | ❌ | ❌ | ❌ | ✅ |
| Manage users | ❌ | ❌ | ❌ | ✅ |

