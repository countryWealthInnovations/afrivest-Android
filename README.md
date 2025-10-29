# AfriVest Android App

Investment-First Remittance Platform for the Ugandan Diaspora

## Overview

AfriVest is a fintech remittance platform that transforms money transfers into investment opportunities. The platform integrates real estate, insurance, gold, and crypto investments alongside traditional remittance services, targeting Ugandan diaspora communities in the US, UK, UAE, EU, and Canada.

**Backend API:** `https://afrivest.co/api/`  
**Status:** Foundation Complete - Ready for UI Development  
**Date:** January 2025

## Features

### Core Functionality
- Multi-currency wallet management (UGX, USD, EUR, GBP)
- Secure user authentication with OTP verification
- Real-time transaction tracking and history
- Biometric authentication for secure access
- Push notifications for transaction updates
- Offline data caching with Room database

### Financial Services
- **Deposits:** Card payment, Mobile Money (MTN, Airtel), Bank transfer
- **Withdrawals:** Bank account, Mobile Money
- **P2P Transfers:** Send money to other AfriVest users
- **Bill Payments:** Utilities, airtime/data, TV subscriptions

### Investment Opportunities
- Real Estate marketplace
- Insurance products
- Gold trading
- Cryptocurrency trading

### Security & Compliance
- KYC verification system
- Encrypted local storage (AndroidX Security Crypto)
- Secure token management
- HTTPS-only communication

## Tech Stack

### Core
- **Language:** Kotlin 2.0.21
- **Build System:** Gradle 8.11.1 with Kotlin DSL
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Architecture:** MVVM with Clean Architecture

### Libraries

**UI Framework:**
- Material Design 1.11.0
- ConstraintLayout 2.1.4
- ViewBinding
- Lottie Animations 6.3.0
- SwipeRefreshLayout 1.1.0
- ViewPager2 1.0.0

**Architecture Components:**
- Lifecycle (ViewModel, LiveData) 2.7.0
- Navigation Component 2.7.6
- WorkManager 2.9.0

**Dependency Injection:**
- Hilt 2.50

**Networking:**
- Retrofit 2.9.0
- OkHttp 4.12.0
- Gson 2.10.1

**Database:**
- Room 2.6.1
- DataStore Preferences 1.0.0

**Security:**
- AndroidX Security Crypto 1.1.0-alpha06
- Biometric 1.1.0

**Image Loading:**
- Glide 4.16.0

**Firebase:**
- Firebase BOM 32.7.0
- Firebase Analytics
- Firebase Messaging
- Firebase Crashlytics

**Async Processing:**
- Kotlin Coroutines 1.7.3

**Development Tools:**
- Timber 5.0.1 (Logging)
- LeakCanary 2.13 (Memory leak detection)

**Testing:**
- JUnit 4.13.2
- Mockito 5.8.0
- Espresso 3.6.1
- Coroutines Test 1.7.3

## Project Structure

```
AfriVest/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/afrivest/app/
│   │   │   │   ├── AfriVestApplication.kt         # Application class
│   │   │   │   │
│   │   │   │   ├── data/                          # Data Layer
│   │   │   │   │   ├── api/                       # Network/API
│   │   │   │   │   │   ├── ApiService.kt          # 40+ API endpoints
│   │   │   │   │   │   ├── ApiClient.kt           # Retrofit client
│   │   │   │   │   │   ├── ApiResponse.kt         # Response wrapper
│   │   │   │   │   │   └── interceptors/
│   │   │   │   │   │       ├── AuthInterceptor.kt
│   │   │   │   │   │       └── ErrorInterceptor.kt
│   │   │   │   │   │
│   │   │   │   │   ├── model/                     # Data models
│   │   │   │   │   │   ├── User.kt
│   │   │   │   │   │   ├── Wallet.kt
│   │   │   │   │   │   ├── Transaction.kt
│   │   │   │   │   │   ├── Dashboard.kt
│   │   │   │   │   │   ├── ApiError.kt
│   │   │   │   │   │   └── Resource.kt
│   │   │   │   │   │
│   │   │   │   │   ├── repository/                # Repository pattern
│   │   │   │   │   │   ├── AuthRepository.kt
│   │   │   │   │   │   ├── WalletRepository.kt
│   │   │   │   │   │   ├── TransactionRepository.kt
│   │   │   │   │   │   ├── ProfileRepository.kt
│   │   │   │   │   │   └── TransferRepository.kt
│   │   │   │   │   │
│   │   │   │   │   └── local/                     # Local storage
│   │   │   │   │       ├── SecurePreferences.kt
│   │   │   │   │       ├── PreferencesManager.kt
│   │   │   │   │       └── AppDatabase.kt
│   │   │   │   │
│   │   │   │   ├── ui/                            # Presentation Layer
│   │   │   │   │   ├── splash/
│   │   │   │   │   ├── auth/
│   │   │   │   │   ├── main/
│   │   │   │   │   ├── dashboard/
│   │   │   │   │   ├── wallet/
│   │   │   │   │   ├── transaction/
│   │   │   │   │   ├── transfer/
│   │   │   │   │   └── profile/
│   │   │   │   │
│   │   │   │   ├── utils/                         # Utilities
│   │   │   │   │   ├── Constants.kt
│   │   │   │   │   ├── Extensions.kt
│   │   │   │   │   ├── Validators.kt
│   │   │   │   │   ├── Formatters.kt
│   │   │   │   │   ├── NetworkMonitor.kt
│   │   │   │   │   ├── DateUtils.kt
│   │   │   │   │   └── CurrencyUtils.kt
│   │   │   │   │
│   │   │   │   ├── di/                            # Dependency Injection
│   │   │   │   │   ├── AppModule.kt
│   │   │   │   │   ├── NetworkModule.kt
│   │   │   │   │   └── RepositoryModule.kt
│   │   │   │   │
│   │   │   │   └── services/                      # Background services
│   │   │   │       ├── FirebaseMessagingService.kt
│   │   │   │       └── SyncService.kt
│   │   │   │
│   │   │   ├── res/                               # Resources
│   │   │   │   ├── layout/                        # XML layouts
│   │   │   │   ├── drawable/                      # Vector drawables
│   │   │   │   ├── values/                        # Strings, colors, themes
│   │   │   │   ├── values-night/                  # Dark mode
│   │   │   │   ├── menu/                          # Menus
│   │   │   │   ├── navigation/                    # Navigation graphs
│   │   │   │   ├── anim/                          # Animations
│   │   │   │   └── mipmap-xxxhdpi/                # App icons
│   │   │   │
│   │   │   ├── AndroidManifest.xml
│   │   │   └── google-services.json               # Firebase config
│   │   │
│   │   ├── test/                                  # Unit tests
│   │   └── androidTest/                           # Instrumentation tests
│   │
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── .gitignore
│
├── gradle/
├── build.gradle.kts                               # Project-level config
├── settings.gradle.kts
├── gradle.properties
├── local.properties                               # Git ignored
└── README.md
```

## Requirements

- Android Studio Ladybug | 2024.2.1 or newer
- JDK 11 or higher
- Android SDK 34
- Gradle 8.11.1

## Setup Instructions

### 1. Clone Repository

```bash
git clone https://github.com/countryWealthInnovations/afrivest-Android.git
cd afrivest-Android
```

### 2. Configure Local Environment

Create `local.properties` in the project root:

```properties
sdk.dir=/path/to/your/Android/sdk
```

### 3. Firebase Setup

1. Download `google-services.json` from Firebase Console
2. Place it in the `app/` directory
3. Ensure the package name matches: `com.afrivest.app`

### 4. Open in Android Studio

1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to the cloned directory
4. Wait for Gradle sync to complete

### 5. Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

Or use Android Studio's Run button (Shift+F10)

## API Integration

### Authentication Endpoints
- `POST /auth/register` - User registration
- `POST /auth/login` - User login
- `POST /auth/logout` - User logout
- `GET /auth/me` - Get current user
- `POST /auth/verify-otp` - Verify OTP
- `POST /auth/resend-otp` - Resend OTP
- `POST /auth/forgot-password` - Password recovery
- `POST /auth/reset-password` - Reset password

### Wallet Endpoints
- `GET /wallets` - Get all user wallets
- `GET /wallets/{currency}` - Get specific wallet
- `GET /wallets/{currency}/transactions` - Wallet transactions

### Transaction Endpoints
- `POST /deposits/card` - Card deposit
- `POST /deposits/mobile-money` - Mobile money deposit
- `POST /deposits/bank-transfer` - Bank transfer deposit
- `POST /withdrawals/bank` - Bank withdrawal
- `POST /withdrawals/mobile-money` - Mobile money withdrawal
- `POST /transfers/p2p` - P2P transfer
- `POST /transfers/insurance` - Insurance purchase
- `POST /transfers/investment` - Investment
- `POST /transfers/bill-payment` - Bill payment
- `POST /transfers/gold` - Gold purchase
- `POST /transfers/crypto` - Crypto purchase
- `GET /transactions` - Get all transactions
- `GET /transactions/{id}` - Get transaction detail

### Other Endpoints
- `GET /forex/rates` - Exchange rates
- `GET /forex/convert` - Currency conversion
- `GET /dashboard` - Dashboard data
- `GET /profile` - User profile
- `PUT /profile` - Update profile
- `POST /profile/avatar` - Upload avatar

## Design System

### Brand Colors

```xml
<!-- Primary Gold -->
<color name="primary_gold">#EFBF04</color>

<!-- Dark Backgrounds -->
<color name="dark_bg_1">#2F2F2F</color>
<color name="dark_bg_2">#333231</color>

<!-- Button Gradient: #FFEAAF to #C58D30 -->
```

### Typography
- Primary Font: Roboto
- Financial data emphasis on readability
- Clear hierarchy for amounts and labels

## Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

### Test Coverage
- Repository layer tests
- ViewModel tests
- UI tests for critical flows

## Security Considerations

- All API tokens stored in encrypted SharedPreferences
- Biometric authentication for sensitive operations
- Certificate pinning for API communication
- Input validation on all user inputs
- ProGuard rules for code obfuscation in release builds

## Development Workflow

1. Create feature branch from `main`
2. Implement feature following MVVM pattern
3. Write unit tests for business logic
4. Test on physical device
5. Create pull request with description
6. Code review and merge

## Build Variants

- **Debug:** Development build with logging enabled
- **Release:** Production build with ProGuard and minification

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is proprietary and confidential. Unauthorized access, copying, or distribution is prohibited.

## Team

**Country Wealth Innovations**  
Building financial inclusion solutions for Africa

## Support

For technical support or bug reports:
- Email: support@countrywealth.ug
- Website: https://afrivest.co

---

**Current Status:** Foundation complete - Ready for UI implementation  
**Next Phase:** Screen development following the structure outlined above