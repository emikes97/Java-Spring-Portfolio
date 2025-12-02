# Portfolio E-Shop (Java • Spring Boot • PostgreSQL)

A production-style **e-commerce backend simulation** designed as a portfolio project to practice **architecture**, **concurrency/async workflows**.

### Features
- **Customer Lifecycle** – account creation, addresses, payment methods.
- **Cart & Wishlist** – add, remove, and manage items.
- **Orders** – create, update, and manage orders with itemized details.
- **Mock Payments** – simulate payment flows and tokenization.
- **Centralized Auditing** – consistent domain events and logging across services.
- **Mock Emails** – outbox pattern for account/order notifications.
- **Python Toolkit** – simulates multiple concurrent users placing orders.
- **Postman Collection** – all endpoints with test cases ready for import.

---

## 🗺️ Roadmap
- [ ] Retry/scheduler for failed tokenizations & emails
      
---

## ⚙️ Tech Stack

- **Language**: Java 17  
- **Framework**: Spring Boot 3.x (Web, Validation, Data JPA), Lombok  
- **DB**: PostgreSQL (UUID, CITEXT, enums, JSONB)  
- **Build**: Maven  
- **Docs**: Postman collection
- **Optional**: Python 3.1
  
---

## 🚀 Quick Start

### 1) Prereqs
- JDK 17  
- PostgreSQL 16+  
- Maven 3.9+

---

## ✨ Highlights

- **Clean, layered design**: Controllers → DTO/Mappers → Services → Repos (JPA/Hibernate).
- **Domain-driven model**: `Customer`, `Cart`, `CartItem`, `Wishlist`, `Order`, `OrderItem`, `Transaction`,  
  `PaymentMethod`, `CustomerAddress`, `EmailSent`, `Auditing`, `Category`, `Product`, `ProductCategory`.
- **Async, event-driven flows**:
  - Account created → email dispatch (status tracked in DB).
  - Order paid → audit trail + email.
  - **Payment Method tokenization**: create returns `201` with `tokenStatus=PENDING`; async listener fetches provider token and updates to `ACTIVE`/`FAILED` after commit.
- **Strong Postgres usage**: UUID PKs, `CITEXT`, enums for `order_status`, `token_status`, auditing status, JSONB where useful.
- **Automated Orders Toolkit**: seeders + scripts to spin up realistic orders for demos and load testing.
- **Auditing & Idempotency**: central audit util + event logs.
- **Postman collection** for quick smoke testing.

---

**Key patterns**
- Single source of truth for domain lookups (centralized `DomainLookupService`).
- Whitelisted sorting/filters to keep repos safe from user input.
- Mappers decoupled from services; DTOs at the edge only.
- Transaction boundaries at service layer; out-of-txn external calls via AFTER_COMMIT listeners.

### Core Relationships (Complete)

**Primary Domain**
- `Customer` ⇄ `Cart` — **1:1**
- `Customer` ⇄ `CustomerAddress` — **1:N** (exactly one `is_default = true`)
- `Customer` ⇄ `CustomerPaymentMethod` — **1:N** (async tokenization lifecycle)
- `Customer` ⇄ `Order` — **1:N**
- `Customer` ⇄ `Wishlist` — **1:N**
- `Wishlist` ⇄ `WishlistItem` — **1:N**
- `WishlistItem` ⇄ `Product` — **N:1**
- `Cart` ⇄ `CartItem` — **1:N**
- `CartItem` ⇄ `Product` — **N:1**
- `Order` ⇄ `OrderItem` — **1:N**
- `OrderItem` ⇄ `Product` — **N:1**
- `Product` ⇄ `Category` — **M:N** via `ProductCategory`
- `Order` ⇄ `Transaction` — **1:N** (payments/refunds per order)

**Comms / Observability**
- `EmailSent` ⇄ `Customer` — **N:1**
- `EmailSent` ⇄ `Order` — **N:1 (nullable)**
- `EmailSent` ⇄ `Transaction` — **N:1 (nullable)**
- `AuditLog` (centralized auditing of domain events; references target via ids + type)

## 📖 Core Flows (Selected)

### 1) Account Creation → Welcome Email
1. Client calls `POST /customers` with registration data.
2. `CustomerController` → `CustomerService` creates the new customer in DB.
3. An entry is inserted in `emails_sent` with status = `QUEUED`, type = `ACCOUNT_CREATED`.
4. After transaction commit, an `EmailEventRequest` is published.
5. `EmailClaimService` picks the queued email asynchronously.
6. Email is composed (`EmailComposer`) and marked `SENT` (or `FAILED` if sending fails).

---

### 2) Add Payment Method → Async Tokenization
1. Client calls `POST /customers/{id}/payment-methods`.
2. `CustomerPaymentMethodController` → `CustomerPaymentMethodService` inserts record with `token_status = PENDING`.
3. Response is returned immediately with `201 Created` and `tokenStatus: PENDING`.
4. After commit, `PaymentMethodCreatedEvent` is published.
5. Async service (`service/async/external`) calls the mock provider.
6. DB is updated with:
   - `token_status = ACTIVE` + provider token (on success), OR
   - `token_status = FAILED` (on error).

---

### 3) Cart → Order → Payment → Audit + Email
1. Client adds products to cart via `POST /carts/{cid}/items`.
2. `CartService` persists `CartItem` entries linked to `Cart`.
3. Client calls `POST /orders` to create order from cart.
4. `OrderService` persists `Order` and `OrderItem` entities.
5. Client calls `POST /orders/{id}/pay` to simulate payment.
6. `TransactionService` inserts a `Transaction` record with `status = SUCCESSFUL` or `FAILED`.
7. After commit:
   - `CentralAudit` logs the payment in the `auditing` table.
   - An `EmailEventRequest` is queued in `emails_sent` (e.g., `ORDER_CONFIRMATION`, `PAYMENT_CONFIRMATION`).
   - Email async worker picks it up and marks as `SENT`.

---

### 4) Wishlist Management
1. Client calls `POST /customers/{id}/wishlist` to create a wishlist (idempotent).
2. `WishlistService` ensures the wishlist exists for that customer.
3. Client calls `POST /wishlist/{id}/items` with a product ID.
4. `WishlistService` adds a `WishlistItem` row linked to the product.
5. Client calls `GET /wishlist/{id}/items`.
6. `WishlistService` fetches all items, joins with `Product`, and returns product details.

---

### 5) Email Outbox Worker
1. Business services enqueue rows into `emails_sent` with `status = QUEUED`.
2. After commit, `EmailEventRequest` ensures async worker wakes up.
3. `EmailClaimService` locks rows (status → `SENDING`).
4. `EmailComposer` builds message from templates.
5. Status is updated to:
   - `SENT` on success, OR
   - `FAILED` on error (optional retries in roadmap).
  
---

## Project Structure
```
Portfolio_Eshop/
└── core/
    └── src/main/java/commerce/eshop/core/
        ├── config/                            # App configs (executors, async, branding, etc.)
        │   ├── AsyncConfig.java
        │   ├── AsyncExecutorConfig.java
        │   ├── BackgroundConfig.java
        │   ├── BrandConfig.java
        │   ├── Conf.java
        │   └── RestTemplateConfig.java
        │
        ├── model/                             # Entities (DB schema mirror)
        │   └── entity/
        │       ├── Auditing.java
        │       ├── Cart.java
        │       ├── CartItem.java
        │       ├── Category.java
        │       ├── Customer.java
        │       ├── CustomerAddress.java
        │       ├── CustomerPaymentMethod.java
        │       ├── EmailsSent.java
        │       ├── Order.java
        │       ├── OrderItem.java
        │       ├── Product.java
        │       ├── ProductCategory.java
        │       ├── Transaction.java
        │       ├── Wishlist.java
        │       └── WishlistItem.java
        │
        ├── repository/                        # Spring Data JPA repos (+ impls)
        │   ├── AuditingRepo.java
        │   ├── CartItemRepo.java
        │   ├── CartRepo.java
        │   ├── CategoryRepo.java
        │   ├── CustomerAddrRepo.java
        │   ├── CustomerPaymentMethodRepo.java
        │   ├── CustomerRepo.java
        │   ├── EmailsSentRepo.java
        │   ├── OrderItemRepo.java
        │   ├── OrderRepo.java
        │   ├── ProductCategoryRepo.java
        │   ├── ProductRepo.java
        │   ├── TransactionRepo.java
        │   ├── WishlistItemRepo.java
        │   ├── WishlistRepo.java
        │   └── Impl/                          # Custom impls
        │       └── DbLockRepository.java
        │
        ├── application/                       # Application layer (use-cases, orchestration, infra)
        │   ├── async/
        │   │   ├── external/                  # External provider clients (payment/email)
        │   │   │   ├── contracts/
        │   │   │   │   ├── EmailSender.java
        │   │   │   │   └── PaymentProviderClient.java
        │   │   │   └── impl/
        │   │   │       ├── MockEmailSender.java
        │   │   │       ├── PaymentProviderClientImpl.java
        │   │   │       └── (other mock/provider impls)
        │   │   ├── internal/                  # Internal async tasks
        │   │   │   └── EmailOutboxProcessor.java
        │   │   └── schedule/                  # Scheduled jobs, retries, pollers
        │   │       └── PaymentStatusPoller.java
        │   │
        │   ├── email/                         # Email composition + claim service
        │   │   ├── constants/
        │   │   ├── enums/
        │   │   ├── properties/
        │   │   └── templating/
        │   │       ├── EmailClaimService.java
        │   │       └── EmailComposer.java
        │   │
        │   ├── events/                        # Domain/application events
        │   │   ├── auditing_events/
        │   │   │   └── EmailEventRequest.java
        │   │   ├── customer/
        │   │   │   ├── CustomerRegisteredEvent.java
        │   │   │   ├── CustomerUpdatedInfoEvent.java
        │   │   │   └── CustomerSuccessfulOrFailedUpdatePasswordEvent.java
        │   │   ├── email/
        │   │   │   └── EmailSentOrFailedEvent.java
        │   │   ├── order/
        │   │   │   ├── PlacedOrderEvent.java
        │   │   │   └── CancelledOrderEvent.java
        │   │   └── payments/
        │   │       ├── PaymentExecutionRequestEvent.java
        │   │       ├── PaymentMethodCreatedEvent.java
        │   │       └── PaymentSucceededOrFailed.java
        │   │
        │   ├── infrastructure/                # Cross-cutting infra (audit, lookup, client impls)
        │   │   ├── audit/
        │   │   │   └── CentralAudit.java
        │   │   ├── domain/
        │   │   │   └── DomainLookupService.java
        │   │   └── impl/
        │   │       ├── EmailSenderOutboxAdapter.java
        │   │       ├── PaymentProviderOutboxAdapter.java
        │   │       └── (other infra adapter impls)
        │   │
        │   ├── cart/
        │   │   ├── commands/
        │   │   │   ├── AddToCart.java
        │   │   │   ├── ClearCart.java
        │   │   │   └── RemoveFromCart.java
        │   │   ├── factory/
        │   │   │   └── CartItemFactory.java
        │   │   ├── queries/
        │   │   │   └── CartQueries.java
        │   │   ├── validation/
        │   │   │   └── AuditedCartValidation.java
        │   │   └── writer/
        │   │       └── CartWriter.java
        │   │
        │   ├── category/
        │   │   ├── commands/
        │   │   │   ├── AddCategory.java
        │   │   │   └── RemoveCategory.java
        │   │   ├── factory/
        │   │   │   └── CategoryFactory.java
        │   │   ├── queries/
        │   │   │   └── CategoryQueries.java
        │   │   ├── validation/
        │   │   │   └── AuditedCategoryValidation.java
        │   │   └── writer/
        │   │       └── CategoryWriter.java
        │   │
        │   ├── customer/
        │   │   ├── commands/
        │   │   │   ├── CustomerRegistration.java
        │   │   │   ├── CustomerServiceActions.java
        │   │   │   └── (other customer commands)
        │   │   ├── factory/
        │   │   │   └── CustomerFactory.java
        │   │   ├── queries/
        │   │   │   └── CustomerQueries.java
        │   │   ├── validation/
        │   │   │   └── AuditedCustomerValidation.java
        │   │   ├── writer/
        │   │   │   └── CustomerWriter.java
        │   │   └── addons/                    # Addresses, payment methods, wishlist, etc.
        │   │       ├── address/
        │   │       │   ├── commands/
        │   │       │   ├── factory/
        │   │       │   ├── queries/
        │   │       │   ├── validation/
        │   │       │   └── writer/
        │   │       ├── payments/
        │   │       │   ├── commands/
        │   │       │   ├── factory/
        │   │       │   ├── listener/
        │   │       │   ├── validation/
        │   │       │   └── writer/
        │   │       └── wishlist/
        │   │           ├── commands/
        │   │           ├── factory/
        │   │           ├── queries/
        │   │           └── writer/
        │   │
        │   ├── order/
        │   │   ├── commands/
        │   │   │   ├── CancelOrder.java
        │   │   │   └── PlaceOrder.java
        │   │   ├── factory/
        │   │   │   ├── DefaultAddressFactory.java
        │   │   │   └── OrderFactory.java
        │   │   ├── listener/
        │   │   │   └── OrderApplicationEventListener.java
        │   │   ├── orchestrator/
        │   │   │   └── OrderPlacementExecutor.java
        │   │   ├── queries/
        │   │   │   └── OrderQueries.java
        │   │   ├── validation/
        │   │   │   └── AuditedOrderValidation.java
        │   │   └── writer/
        │   │       ├── OrderCartWriter.java
        │   │       └── OrderWriter.java
        │   │
        │   ├── product/
        │   │   ├── commands/
        │   │   │   ├── AddProduct.java
        │   │   │   └── RemoveProduct.java
        │   │   ├── factory/
        │   │   │   └── ProductFactory.java
        │   │   ├── queries/
        │   │   │   └── ProductQueries.java
        │   │   ├── validation/
        │   │   │   └── AuditedProductValidation.java
        │   │   └── writer/
        │   │       └── ProductWriter.java
        │   │
        │   ├── transaction/
        │   │   ├── commands/
        │   │   │   └── PayOrder.java
        │   │   ├── factory/
        │   │   │   └── TransactionFactory.java
        │   │   ├── validation/
        │   │   │   └── AuditedTransactionValidation.java
        │   │   └── writer/
        │   │       └── TransactionWriter.java
        │   │
        │   └── util/                          # Cross-cutting app-level utilities
        │       ├── constants/
        │       │   └── EndpointsNameMethods.java
        │       ├── enums/
        │       │   ├── AuditingStatus.java
        │       │   ├── OrderStatus.java
        │       │   ├── PaymentStatus.java
        │       │   ├── TokenStatus.java
        │       │   └── TransactionStatus.java
        │       └── sort/
        │           └── SortSanitizer.java
        │
        └── web/                               # Web/API layer
            ├── controller/
            │   ├── CartController.java
            │   ├── CategoryController.java
            │   ├── CustomerAddressController.java
            │   ├── CustomerController.java
            │   ├── CustomerPaymentMethodController.java
            │   ├── OrderController.java
            │   ├── ProductController.java
            │   ├── TransactionController.java
            │   └── WishController.java
            │
            ├── dto/
            │   ├── requests/
            │   │   ├── Cart/
            │   │   ├── Category/
            │   │   ├── Customer/
            │   │   ├── CustomerAddr/
            │   │   ├── CustomerPaymentMethodRequests/
            │   │   ├── Order/
            │   │   ├── Products/
            │   │   ├── Transactions/
            │   │   └── Wishlist/
            │   └── response/
            │       ├── Cart/
            │       ├── Category/
            │       ├── Customer/
            │       ├── CustomerAddr/
            │       ├── Order/
            │       ├── PaymentMethod/
            │       ├── Product/
            │       ├── Providers/
            │       ├── Transactions/
            │       └── Wishlist/
            │
            ├── errorHandler/
            │   └── GlobalApiErrorHandler.java
            │
            └── mapper/
                ├── CartServiceMapper.java
                ├── CategoryServiceMapper.java
                ├── CustomerAddressServiceMapper.java
                ├── CustomerPaymentMethodServiceMapper.java
                ├── CustomerServiceMapper.java
                ├── OrderServiceMapper.java
                ├── ProductServiceMapper.java
                ├── TransactionServiceMapper.java
                └── WishlistServiceMapper.java
```
