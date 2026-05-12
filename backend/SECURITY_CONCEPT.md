# Security Concept - Pawsitters Project

## 1. Introduction
This document outlines the security architecture and measures implemented in the Pawsitters project. The goal is to ensure Confidentiality, Integrity, and Availability (CIA) of user data and system resources.

## 2. Identity and Access Management (IAM)

### 2.1 Authentication
The system uses a robust authentication mechanism to verify user identities.
*   **Password Policy (NIST SP 800-63B):** Enforces a minimum length of 12 characters and multi-class complexity (uppercase, lowercase, digits, special characters) to maximize entropy.
*   **Credential Storage:** Passwords are never stored in plaintext. The system uses **BCrypt (via Spring Security's BCryptPasswordEncoder)** with a high cost factor to protect against rainbow table and brute-force attacks.
*   **Backend Validation:** All security rules are enforced at the service layer to prevent API-based bypasses.

### 2.2 Authorization
The system employs **Role-Based Access Control (RBAC)**:
*   **USER / PET_OWNER / SITTER:** Standard roles for application features.
*   **ADMIN:** Reserved for system maintenance and administrative actions.
*   **Principle of Least Privilege:** Users are granted only the permissions necessary for their specific roles.

## 3. Application Security

### 3.1 Input Validation
*   **Client-Side:** Real-time feedback in the React frontend (e.g., `usePasswordValidation` hook) improves UX and provides a first line of defense.
*   **Server-Side:** Strict validation in `AppUserService` ensures that malformed or malicious data is rejected regardless of the entry point.

### 3.2 Protection Against Common Vulnerabilities (OWASP Top 10)
*   **Injection:** Use of Hibernate/JPA and parameterized queries prevents SQL Injection.
*   **Broken Access Control:** Spring Security filters ensure that unauthorized requests are blocked before reaching business logic.
*   **Cross-Site Scripting (XSS):** React's default escaping mechanism prevents script injection in the UI.
*   **CSRF:** Protection is handled via Spring Security (enabled in production; currently disabled for development/API testing).

## 4. Data Protection

### 4.1 Encryption in Transit
All communication between the frontend and backend must occur over **TLS 1.2+ (HTTPS)** to prevent eavesdropping and Man-in-the-Middle (MITM) attacks.

### 4.2 Data Privacy (GDPR Compliance)
*   **Data Minimization:** Only necessary user data (email, name, pet info) is collected.
*   **Right to Access/Delete:** Users can view and manage their profiles through the `UserController`.

## 5. Security Maintenance
*   **Dependency Management:** Regular updates of Maven dependencies (Spring Boot, Hibernate) to patch known vulnerabilities (CVEs).
*   **Logging:** Auditable logging of security-critical events (login failures, registration) for forensic analysis.

---
**Version:** 1.0  
**Compliance:** NIST SP 800-63B, OWASP Top 10  
**Lead Developer:** Senior Full-Stack Developer (Security Focus)
