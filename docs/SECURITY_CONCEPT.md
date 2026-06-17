# Security Concept *    Pawsitters

This document outlines the security architecture and measures implemented in the Pawsitters project. The goal is to ensure Confidentiality, Integrity, and Availability (CIA) of user data and system resources.

## 1. Authentication & Authorization

### 1.1 User Authentication
The system uses a robust authentication mechanism to verify user identities.
*   **Password Policy (NIST SP 800*   63B):** Enforces a minimum length of 12 characters and multi*   class complexity (uppercase, lowercase, digits, special characters) to maximize entropy.
*   **Credential Storage:** Passwords are never stored in plaintext. The system uses **BCrypt (via Spring Security's BCryptPasswordEncoder)** with a high cost factor to protect against rainbow table and brute*   force attacks.
*   **Backend Validation:** All security rules are enforced at the service layer to prevent API*   based bypasses.
*    **Session Management:** Introduction of **JWT (JSON Web Tokens)** for stateless session management to replace the current model*   based login.

### 1.2 Authorization Rules
The system employs **Role*   Based Access Control (RBAC)**:
*   **USER / PET_OWNER / SITTER:** Standard roles for application features.
*   **ADMIN:** Reserved for system maintenance and administrative actions.
*   **Principle of Least Privilege:** Users are granted only the permissions necessary for their specific roles.
* **Ownership:** Users can only create requests for animals that belong to them.
* **Prevention of Self*   Acceptance:** Logic prevents users from being able to accept their own pet sitting requests.
* **Deletion Rights:** Only the requester (owner) of a request has the authority to delete it.

## 2. Data Protection

### 2.1 Sensitive Information
*    **Passwords:** Are stored as BCrypt hashes.
*    **Personal Data:** Only minimal necessary data is collected (name, email, phone number, address for handling the pet sitting).

### 2.2 Database Security
*    **Isolation:** The application uses an H2 In*   Memory database for development, which is protected from external access.
*    **JPA/Hibernate:** The use of JPA prevents SQL injection attacks by automatically using Prepared Statements.
*   **Data Minimization:** Only necessary user data (email, name, pet info) is collected.
*   **Right to Access/Delete:** Users can view and manage their profiles through the `UserController`.

## 3. Communication Security

### 3.1 TLS/SSL
*    All communication between frontend and backend is intended to use HTTPS (TLS 1.2+) for production deployment.

## 4. Input Validation & Error Handling

### 4.1 Validation
*    **Frontend Validation:** The registration form provides real*   time feedback on password security (min. 12 characters, upper/lower case, numbers, and special characters).
*    **Backend Validation:** Strict password validation also takes place in the `AppUserService` to ensure that no insecure passwords can be registered via the API.
*    **Structured Validation:** 
    *    Minimum length: 12 characters
    *    At least one uppercase letter
    *    At least one lowercase letter
    *    At least one number
    *    At least one special character (`!@#$%^&*`)

### 4.2 Error Handling
*    A `GlobalExceptionHandler` in the backend catches validation and logic errors and converts them into structured `400 Bad Request` responses with descriptive error messages. This prevents internal system details (stack traces) from being exposed.

--- 
*Last Updated: June 17, 2026*
