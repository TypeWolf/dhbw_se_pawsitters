# Security Concept - Pawsitters

This document outlines the security measures and architectural decisions taken to protect user data and ensure the integrity of the Pawsitters platform.

## 1. Authentication & Authorization

### 1.1 User Authentication
- **Mechanism:** Users authenticate using their email and a password.
- **Current State:** Basic password matching is implemented in the Service layer.
- **Planned Improvements:** Implementation of Spring Security with BCrypt password hashing and JWT (JSON Web Tokens) for stateless session management.

### 1.2 Authorization Rules
- **Access Control:** Only authenticated users can access pet management and sitting requests.
- **Ownership:** Users can only create requests for pets they own.
- **Prevention of Self-Acceptance:** Logic is implemented to prevent users from accepting their own sitting requests.
- **Deletion Rights:** Only the requester (owner) has the authority to delete a sitting request.

## 2. Data Protection

### 2.1 Sensitive Information
- **Passwords:** Currently stored in plain text for prototype purposes. *Must be hashed before production.*
- **Personal Data:** Minimal personal data is collected (Name, Email, Phone).

### 2.2 Database Security
- **Isolation:** The application uses an H2 in-memory database for development, which is isolated from external access.
- **JPA/Hibernate:** Using JPA helps prevent SQL Injection by using prepared statements automatically.

## 3. Communication Security

### 3.1 Cross-Origin Resource Sharing (CORS)
- **Current State:** Permissive (`@CrossOrigin("*")`) for development ease.
- **Planned Improvements:** Restricting CORS origins to specific trusted frontend domains in production.

### 3.2 TLS/SSL
- All communication between the frontend and backend should be encrypted via HTTPS in a production environment.

## 4. Input Validation
- Basic validation is performed on the frontend (HTML5 required fields).
- Backend services perform logical checks (e.g., checking for existing emails during registration).

---
*Last Updated: 2026-05-08*
