# Finance Tracker - Smart Wallet Manager 🚀

A modern, secure, and feature-rich Android application built with Jetpack Compose to help you track your finances effortlessly.

## ✨ Features

- **📊 Comprehensive Dashboard**: Get a quick overview of your balance, income, and expenses.
- **☁️ Cloud Sync & Backup**: Securely backup your data to Firebase using your Google Account.
- **🔄 Auto-Sync Integration**: Automatic tracking for popular wallets (Dana, Wondr) via notification listening.
- **🛡️ Secure Authentication**: Robust login and registration system with Firebase Auth, including email enumeration protection.
- **📂 Category Management**: Fully customizable categories for income, expenses, and payment methods.
- **📈 Insightful Reports**: Visual representation of your financial trends and history.
- **🎯 Savings Goals**: Set and track your savings progress to reach your financial milestones.
- **🎨 Modern UI/UX**: Clean, dark-themed interface inspired by GoPay and modern banking apps.

## 🛠 Tech Stack

- **UI**: Jetpack Compose (100% Kotlin)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (Local Persistence)
- **Backend**: Firebase (Authentication, Firestore for Cloud Backup)
- **Networking**: Retrofit & OkHttp
- **Dependency Injection**: Manual/Clean Architecture principles
- **Asynchronous**: Kotlin Coroutines & Flow

## 🚀 Getting Started

### Prerequisites

- [Android Studio Koala](https://developer.android.com/studio) or newer.
- Android Device or Emulator (API 24+).

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/RLangit/Finance-Tracker.git
   ```

2. **Setup Environment Variables**:
   Create a `.env` file in the root directory (refer to `.env.example`) and add your Gemini or other necessary API keys:
   ```env
   GEMINI_API_KEY=your_api_key_here
   ```

3. **Firebase Setup**:
   - Create a project on [Firebase Console](https://console.firebase.google.com/).
   - Add an Android App with the package name `com.kai.financetracker`.
   - Download `google-services.json` and place it in the `app/` directory.

4. **Build and Run**:
   Open the project in Android Studio and click the **Run** button.

## 📸 Screenshots

| Dashboard | Profile & Backup | History |
|-----------|------------------|---------|
| ![Dashboard](https://via.placeholder.com/200x400?text=Dashboard) | ![Profile](https://via.placeholder.com/200x400?text=Profile) | ![History](https://via.placeholder.com/200x400?text=History) |

## 🛡 Security & Privacy

This app implements:
- **Email Enumeration Protection**: Prevents account discovery during password resets.
- **Secure Data Storage**: Local data is managed via Room, and cloud data is tied to Firebase UID.
- **Password Validation**: Enforces strong password policies for user accounts.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
Built with ❤️ by [RLangit](https://github.com/RLangit)
