import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:3000';

test.describe('Pawsitters E2E Tests', () => {
  
  test.beforeEach(async ({ page }) => {
    // Basic logging for unexpected errors
    page.on('console', msg => {
      if (msg.type() === 'error') console.log(`PAGE ERROR: "${msg.text()}"`);
    });
  });

  test('should register a new user', async ({ page }) => {
    await page.goto(`${BASE_URL}/signup.html`);
    
    const randomEmail = `user${Math.floor(Math.random() * 10000)}@example.com`;
    
    await page.fill('#firstName', 'Test');
    await page.fill('#lastName', 'User');
    await page.fill('#email', randomEmail);
    await page.fill('#password', 'SecureP@ss123!');
    
    await page.click('button[type="submit"]');
    
    // Should be redirected to dashboard
    await expect(page).toHaveURL(/.*dashboard(\.html)?/);
    await expect(page.locator('#site-header')).toContainText('Log out', { ignoreCase: true });
  });

  test('should login with existing user', async ({ page }) => {
    await page.goto(`${BASE_URL}/login.html`);
    
    await page.fill('#email', 'alice@example.com');
    await page.fill('#password', 'SecureP@ss123!');
    
    await page.click('button[type="submit"]');
    
    await expect(page).toHaveURL(/.*dashboard(\.html)?/);
    await expect(page.locator('#site-header')).toContainText('Log out', { ignoreCase: true });
  });

  test('should add a new pet', async ({ page }) => {
    // Login first
    await page.goto(`${BASE_URL}/login.html`);
    await page.fill('#email', 'alice@example.com');
    await page.fill('#password', 'SecureP@ss123!');
    await page.click('button[type="submit"]');
    
    // Navigate to Pets
    await page.click('nav a[href="pets.html"]');
    await expect(page).toHaveURL(/.*pets(\.html)?/);
    
    // Click toggle to show form
    await page.click('#toggleForm');
    
    const uniquePetName = `Rex-${Math.floor(Math.random() * 10000)}`;
    
    // Add pet
    await page.fill('#petName', uniquePetName);
    await page.fill('#petSpecies', 'Dog');
    await page.fill('#petBreed', 'German Shepherd');
    await page.fill('#petAge', '4');
    
    await page.click('button[type="submit"]');
    
    // Verify pet is in the list
    await expect(page.locator('.pet-card').filter({ hasText: uniquePetName }).first()).toBeVisible();
  });

  test('should create a sitting request', async ({ page }) => {
    // Login
    await page.goto(`${BASE_URL}/login.html`);
    await page.fill('#email', 'alice@example.com');
    await page.fill('#password', 'SecureP@ss123!');
    await page.click('button[type="submit"]');
    
    // Navigate to Request New
    await page.click('nav a[href="my-requests.html"]');
    await page.click('a[href="request-new.html"]');
    
    // Select the first available pet
    // Make sure we wait for options to be populated
    await page.waitForSelector('#petId option:not([value=""])', { timeout: 10000 }).catch(() => {});
    
    const options = page.locator('#petId option');
    const count = await options.count();
    
    if (count <= 1) {
        await page.goto(`${BASE_URL}/pets.html`);
        await page.click('#toggleForm');
        await page.fill('#petName', 'Buddy');
        await page.fill('#petSpecies', 'Dog');
        await page.fill('#petBreed', 'Golden');
        await page.fill('#petAge', '2');
        await page.click('button[type="submit"]');
        await page.goto(`${BASE_URL}/request-new.html`);
        await page.waitForSelector('#petId option:not([value=""])');
    }

    const petOption = page.locator('#petId option').nth(1);
    const petLabel = await petOption.textContent();
    const petName = petLabel?.split(' ')[0] || 'Buddy';
    
    await page.selectOption('#petId', { index: 1 });
    
    const formatDate = (date: Date) => {
        const pad = (n: number) => n.toString().padStart(2, '0');
        return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
    };

    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowStr = formatDate(tomorrow);
    
    await page.fill('#startTime', tomorrowStr);
    
    const tomorrowEnd = new Date(tomorrow);
    tomorrowEnd.setHours(tomorrowEnd.getHours() + 2);
    const tomorrowEndStr = formatDate(tomorrowEnd);
    
    await page.fill('#endTime', tomorrowEndStr);
    
    // Select handover type (required)
    await page.selectOption('#handoverType', { index: 1 });
    
    await page.fill('#handoverLocation', 'Central Park');
    // Set price to 0 to avoid "Insufficient funds"
    await page.fill('#priceOffered', '0');
    
    await page.click('button[type="submit"]');
    
    // Should be redirected to my-requests
    await expect(page).toHaveURL(/.*my-requests(\.html)?/);
    await expect(page.locator('.req-card').filter({ hasText: petName }).first()).toBeVisible();
  });

  test('should logout successfully', async ({ page }) => {
    await page.goto(`${BASE_URL}/login.html`);
    await page.fill('#email', 'alice@example.com');
    await page.fill('#password', 'SecureP@ss123!');
    await page.click('button[type="submit"]');
    
    await page.click('#logoutBtn');
    
    await expect(page).toHaveURL(new RegExp(`${BASE_URL}/(index(\\.html)?)?$`));
    await expect(page.locator('nav')).toContainText('Log in');
  });

  test('should rate a sitter after completing a request', async ({ page }) => {
    // 1. Login as Alice (owner)
    await page.goto(`${BASE_URL}/login.html`);
    await page.fill('#email', 'alice@example.com');
    await page.fill('#password', 'SecureP@ss123!');
    await page.click('button[type="submit"]');

    // 2. Navigate to My Requests
    await page.click('nav a[href="my-requests.html"]');

    // 3. Find an ACCEPTED request to complete, or use the already completed one for rating
    // In our DataInitializer, we have a COMPLETED request for Bob.
    // Let's try to find a "Rate your sitter" button.
    const rateButton = page.locator('button[data-rate]').first();
    
    // If no completed request exists yet, we might need to complete one, 
    // but DataInitializer provides one.
    await expect(rateButton).toBeVisible();
    await rateButton.click();

    // 4. Fill the rating modal
    await expect(page.locator('#rating-modal')).toBeVisible();
    
    // Click on the 4th star (value 4)
    await page.click('.star-rating-input span[data-value="4"]');
    await page.fill('#rating-comment', 'Bob was great, but 5 mins late.');
    
    await page.click('#rating-form button[type="submit"]');

    // 5. Verify success message
    await expect(page.locator('.alert-success')).toBeVisible();
    
    // 6. Verify rating is displayed (optional, depends on how quickly average updates)
    // The average rating should now be visible in the request card
    await expect(page.locator('.star-rating-display')).toBeVisible();
  });
});
