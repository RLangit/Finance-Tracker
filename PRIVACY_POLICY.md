# Privacy Policy for Finance Tracker

**Last Updated: October 2023**

RLangit ("we," "us," or "our") operates the Finance Tracker mobile application (the "App"). We respect your privacy and are committed to protecting the personal information you share with us.

## 1. Information Collection and Use

### 1.1 Personal Data
The App uses **Firebase Authentication** and **Google Sign-In** to manage your account. When you sign in, we may collect:
*   Your Email Address

### 1.2 Financial Data
The App is designed to help you track finances. Financial records (income, expenses, savings goals) are stored:
*   **Locally:** On your device using an encrypted Room database.
*   **Cloud (Optional):** If synced, your data is stored in **Firebase Firestore**, tied to your unique user ID. This data is not shared with third parties.

### 1.3 Notification Listener Service
The App includes an optional feature to sync transactions from notifications (e.g., Dana, Wondr).
*   **Permissions:** This requires the `BIND_NOTIFICATION_LISTENER_SERVICE` permission.
*   **Usage:** The App only reads notifications from specific financial apps you authorize. It extracts transaction amounts and descriptions locally to update your records.
*   **Privacy:** This data is processed on-device and is never sent to our servers except for your personal cloud backup if enabled.

## 2. Permissions Used
*   `INTERNET`: Required for Firebase Sync and Authentication.
*   `POST_NOTIFICATIONS`: Required to show alerts and maintain service status.
*   `BIND_NOTIFICATION_LISTENER_SERVICE`: Required for the auto-sync feature.

## 3. Data Security
We implement industry-standard security measures, including Firebase security rules, to ensure your cloud data is only accessible by you.

## 4. Third-Party Services
We use the following third-party services:
*   **Firebase (Google):** For auth, database, and analytics.
*   **Google Play Services:** For app functionality and security.

## 5. Your Rights
You can delete your account and all associated data at any time by contacting us or using the delete option within the app settings.

## 6. Contact Us
If you have any questions about this Privacy Policy, please contact us at:
raditya.langit08@gmail.com
