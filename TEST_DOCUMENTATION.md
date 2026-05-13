# Test Documentation - Pawsitters

## Overview
This document outlines the unit test coverage for the Pawsitters backend services. The tests are implemented using Spring Boot's testing support (`@SpringBootTest`) with an in-memory H2 database. All tests are `@Transactional` to ensure a clean state between executions.

## Service Test Coverage

### 1. AppUserService
Tests user registration, login, and retrieval.
- **Happy Path:**
    - `testRegisterHappyPath`: Successful registration with hashed password.
    - `testLoginHappyPath`: Successful login with correct credentials and role assignment.
    - `testLoginFirstUserIsAdmin`: The first registered user automatically receives the ADMIN role upon login.
    - `testGetUserByEmail`: Successfully retrieve a user by their email.
    - `testGetAllUsers`: Retrieve all registered users.
- **Bad Path:**
    - `testRegisterDuplicateEmail`: Fails when trying to register an email that already exists.
    - `testRegisterInvalidPasswords`: Fails for passwords that are too short, lack uppercase/lowercase/numbers, or lack special characters.
    - `testLoginUserNotFound`: Fails when trying to login with a non-existent email.
    - `testLoginIncorrectPassword`: Fails when the password does not match the stored hash.

### 2. PetService
Tests pet management.
- **Happy Path:**
    - `testCreatePet`: Successfully create and save a new pet.
    - `testGetAllPets`: Retrieve all pets in the system.
    - `testGetPetById`: Retrieve a specific pet by its ID.

### 3. SittingRequestService
Tests the lifecycle of a sitting request.
- **Happy Path:**
    - `testCreateRequest`: Successfully create a request with status PENDING.
    - `testAcceptRequest`: Sitter accepts a PENDING request, status changes to ACCEPTED.
    - `testCancelRequest`: Requester cancels a PENDING request, status changes to CANCELLED.
    - `testCompleteRequest`: Requester confirms completion of an ACCEPTED request, status changes to COMPLETED.
- **Bad Path:**
    - `testAcceptOwnRequestThrows`: Prevents users from accepting their own requests.
    - `testCancelByOtherThrows`: Only the requester can cancel their request.
    - `testCompleteUnacceptedRequestThrows`: Requests must be ACCEPTED before they can be COMPLETED.

### 4. WalletService
Tests financial operations and balance management.
- **Happy Path:**
    - `testGetOrCreateNewWallet`: Automatically creates a wallet with a signup bonus for new users.
    - `testDebitDrawsFromCreditFirst`: Debiting funds uses owner credit before touching sitter earnings.
    - `testDebitDrawsFromEarningsIfCreditInsufficient`: Correctly splits debit between credit and earnings.
    - `testWithdrawEarnings`: Allows sitters to withdraw their earnings, zeroing out the balance.
- **Bad Path:**
    - `testDebitInsufficientFundsThrows`: Prevents operations if the total balance is too low.

### 5. PaymentService
Tests payment holding, release, and refund logic.
- **Happy Path:**
    - `testHoldPayment`: Correctly creates a HELD payment record and debits the wallet.
    - `testReleasePayment`: Releases held funds to the sitter's earnings.
    - `testRefundPayment`: Restores funds to the payer's wallet.
    - `testPaymentHistory`: Retrieves a history of all financial transactions for a user.

### 6. AdminService
Tests administrative role management.
- **Happy Path:**
    - `testRequireAdminHappyPath`: Confirms that admin-only checks pass for authorized users.
    - `testSetRolesHappyPath`: Admin can successfully update roles for any user.
- **Bad Path:**
    - `testRequireAdminThrowsForNonAdmin`: Blocks access for non-admin users.
    - `testSetRolesByNonAdminThrows`: Prevents non-admins from modifying roles.

## Technical Details
- **Test Framework:** JUnit 5, Spring Boot Test
- **Database:** H2 (In-Memory)
- **Total Tests:** 37
- **Database Cleanup:** A generic `deleteAll` method in `UnitOfWork` is used in `@BeforeEach` to clear tables in the correct order (respecting foreign key constraints).

## End-to-End (E2E) Test Coverage

### Overview
End-to-End tests are implemented using **Playwright** to verify the integration between the frontend and backend, ensuring that user flows work correctly in a real browser environment.

### Test Scenarios
The E2E suite (`e2e-tests/tests/pawsitters.spec.ts`) covers the following critical user journeys:

1. **Authentication**
   - **User Registration:** Verifies that a new user can sign up, is redirected to the dashboard, and sees the authenticated state.
   - **User Login:** Verifies that an existing user can log in with valid credentials.
   - **User Logout:** Ensures the session is cleared and the user is redirected to the landing page.

2. **Pet Management**
   - **Add New Pet:** Logs in a user, navigates to the pets page, and adds a new pet through the form, verifying its appearance in the list.

3. **Sitting Requests**
   - **Create Request:** Validates the flow of creating a new sitting request, including pet selection and date input, ensuring the request appears in "My Requests".

### Technical Details
- **Framework:** Playwright
- **Configuration:** `e2e-tests/playwright.config.ts`
- **Base URL:** `http://localhost:3000` (default frontend port)
- **Automatic Setup:** Tests include logic to ensure a pet exists before attempting to create a sitting request.
