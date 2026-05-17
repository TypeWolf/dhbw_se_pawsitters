# Security Concept - Pawsitters

This document outlines the security measures and architectural decisions taken to protect user data and ensure the integrity of the Pawsitters platform.

## 1. Authentication & Authorization

### 1.1 User Authentication
- **Mechanism:** Users authenticate using their email and a password.
- **Current State:** Password hashing is implemented using **BCrypt** via Spring Security's `BCryptPasswordEncoder`. 
- **Session Management:** Currently, the system relies on client-side state in `localStorage`. The backend is stateless but does not yet issue tokens.
- **Planned Improvements:** Implementation of **JWT (JSON Web Tokens)** or secure HttpOnly cookies for robust session management.

### 1.2 Role-Based Access Control (RBAC)
The system defines three primary roles:
- `PET_OWNER`: Can manage pets and post sitting requests.
- `SITTER`: Can browse and accept sitting requests.
- `ADMIN`: Has full read access to all system data and can manage user roles.

Users can hold multiple roles simultaneously (e.g., a user can be both an owner and a sitter).

### 1.3 Authorization & Business Logic Guards
Security is enforced at the service layer through explicit checks:
- **Ownership:** Users can only modify or delete pets they own.
- **Request Lifecycle:**
    - Only the **owner** of a sitting request can cancel it or confirm its completion.
    - A user **cannot accept their own** sitting request.
    - Requests can only be accepted if they are in the `PENDING` state.
- **Admin Protection:** Administrative endpoints (under `/api/admin/**`) require a `requesterId` query parameter which is verified against the database to ensure the user has the `ADMIN` role (implemented in `AdminService.requireAdmin`).

## 2. Data Protection

### 2.1 Sensitive Information
- **Passwords:** Never stored in plain text; only as BCrypt hashes.
- **Personal Data:** Minimal collection (Name, Email, Phone) to reduce the impact of potential data breaches.

### 2.2 Database Security
- **H2 Isolation:** The in-memory database used for development is not exposed to external networks.
- **SQL Injection Prevention:** Utilization of **Spring Data JPA** and the **Unit of Work pattern** ensures that all database interactions use prepared statements, effectively neutralizing SQL injection risks.

## 3. Financial Integrity (Escrow Logic)

Pawsitters implements a mock escrow system to ensure financial security during transactions:
- **Funds Holding:** When a request is created, the offered amount is immediately deducted from the owner's wallet and marked as `HELD` in the `Payments` table.
- **Atomic Transactions:** Wallet updates and sitting request status changes are performed within `@Transactional` methods to ensure consistency.
- **Release/Refund Logic:** Funds are only released to the sitter upon owner confirmation or refunded to the owner upon cancellation, preventing unauthorized fund transfers.

## 4. Communication & Input Security

### 4.1 CORS (Cross-Origin Resource Sharing)
- **Current State:** The backend uses `@CrossOrigin("*")` to facilitate development with a decoupled frontend.
- **Production Goal:** Restrict CORS to explicitly trusted origins (e.g., the production frontend domain).

### 4.2 Error Handling
- **Information Leakage:** A `GlobalExceptionHandler` maps internal exceptions to generic or descriptive HTTP error codes (e.g., `400 Bad Request`), preventing the leakage of internal stack traces or database structures to the client.

### 4.3 Input Validation
- **Frontend:** Live validation for password complexity (12+ characters, mixed case, numbers, specials).
- **Backend:** Service-layer validation for business rules and data constraints.

---
*Last Updated: 2026-05-17*
