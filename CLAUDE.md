# CLAUDE.md

Guidance for AI assistants working in this repository.

## Project

**My Taxes** — Kotlin Multiplatform + Compose Multiplatform app (Android, iOS).
The user enters one or several incomes (each with date, amount and currency; e.g. all receipts of a month) and presses
**Calculate**. The app requests the official NBU rate for each income date, converts the incomes to UAH, sums them and shows the taxes for the selected FOP group (`FopGroup`: groups 1 and 2 —
fixed monthly single tax + military levy; group 3 — 5% or 3% (VAT payer) single tax + 1% military levy).
Authentication is optional: guests use the calculator freely; users who sign in with Firebase Authentication
(email/password, opened from the calculator top bar) get every calculation stored in Cloud Firestore and shown
on the History screen.

Package root: `com.gmail.vanyadubik.mytaxes`. Android applicationId: `com.gmail.vanyadubik.mytaxes` (debug suffix `.debug`).

## Tech stack

Kotlin 2.4 · Compose Multiplatform 1.9 (Material3) · Navigation Compose · Koin (DI, `viewModelOf`) ·
Ktor client (OkHttp / Darwin) · kotlinx.serialization · kotlinx.datetime · AndroidX DataStore (preferences) ·
Gradle version catalog (`gradle/libs.versions.toml`, type-safe project accessors `projects.core.domen`).

Firebase is used **through REST APIs with Ktor** (no native Firebase SDKs, no google-services.json):
- Auth: Identity Toolkit (`accounts:signInWithPassword`, `accounts:signUp`) and Secure Token (`/token` refresh).
- Firestore: `users/{uid}/calculations/{id}`, `users/{uid}/expenses/{id}` and `users/{uid}/profile/main` (`fopGroup`, saved from the Account screen), authorized with the user's ID token (rules in `firestore.rules`).
- Push reminders (FCM): `PushNotificationUseCase` stores the device token + FOP group + language in
  `users/{uid}/devices/{token}` (removed on logout); the GitHub Actions workflow `.github/workflows/tax-reminders.yml`
  runs `reminders/send.js` daily (~09:00 Kyiv, once per day via the `meta/reminders` marker) and sends the deadline
  reminders from `reminders/deadlines.js` through FCM (no Blaze plan needed; secret `FIREBASE_SERVICE_ACCOUNT` = service
  account JSON). Android only: `FirebaseApp` is initialised by hand in `TaxApp` (`firebaseAndroidOptions`, no
  google-services plugin). A deadline is skipped for a user who already calculated the taxes since the end of the paid
  period (a calculation counts as a payment): `send.js` compares the latest `users/{uid}/calculations` `createdAt` with
  `paidSince` of each reminder from `deadlines.js` (1st of the deadline month for the monthly tax / ESV, 1st of the previous
  month for the quarterly declaration and tax); iOS has a stub `PushTokenProvider` (no token). Deploy rules: `firebase deploy --only firestore:rules`.
- Config (Web API key, project id): `composeApp/.../di/FirebaseSettings.kt`.

## Module layout and dependency rules

```
composeApp            app entry points, Koin start, navigation, MainViewModel, splash
feature:login         LoginScreen + LoginViewModel (sign in / sign up)
feature:calculator    CalculatorScreen + CalculatorViewModel (input -> NBU rate -> tax -> save to history)
feature:account      AccountScreen + AccountViewModel (email, FOP group saved in Firestore - tap to save / tap again to remove, logout, delete account)
feature:home          HomeScreen + HomeViewModel (chart of income / taxes / expenses per month, expenses input)
feature:history       HistoryScreen + HistoryViewModel (list / delete saved calculations)
core:domen            pure Kotlin domain: models, repository interfaces, use cases (no Ktor/Compose)
core:network          Ktor client, DTOs, repository implementations (NBU, Firebase Auth, Firestore)
core:data-store       DataStore implementation of DataStoreRepository (expect/actual per platform)
core:common           platform helpers (logger, PlatformConfig, AppInfo, date utils) + `commonPlatformModule`
core:common-ui        theme (animated light/dark switch), shared components, dialogs, formatting helpers
core:resource         compose resources (strings, drawables) and colors; `Res` class in `com.gmail.vanyadubik.mytaxes.resource`
```

Dependencies flow downwards: `composeApp → feature:* → core:common-ui / core:network / core:data-store → core:domen →
core:common`. Features never depend on each other; the app module wires them together. Domain code must not
depend on network/UI. Repository **interfaces live in `core:domen`**, implementations in `core:network` / `core:data-store`.

## Conventions

- Each module has `commonMain`, `androidMain`, `iosMain`; platform differences use `expect/actual`
  (e.g. `HttpEngineFactory`, `PlatformDataStoreFactory`, `PlatformConfig`, `commonPlatformModule`).
- Each module exposes a Koin module function in `di/Module.kt` (`domenModule()`, `networkModule(...)`,
  `featureLoginModule()` …). Register new modules in `composeApp/.../di/KoinModules.kt` (`initKoin`).
- UI state: ViewModel exposes `StateFlow<UiState>` (immutable data class); screens use `collectAsState()` and
  `koinViewModel()`. Screens take navigation callbacks as parameters, they do not know the NavController.
- Use cases (`core:domen/usecase`) hold the business logic; ViewModels call use cases, never repositories.
- Network calls return `ResultState<T>` (`Success` / `Error` / `Loading` / `Inactive`) via `apiCall { }` in
  `core/network/.../ApiWrapper.kt`; errors carry a user-presentable `message`.
- Persisted preferences are declared in `PreferenceKey` and accessed via `DataStoreUseCase`.
  `logout()` clears all keys except `PreferenceKey.getSavedKeys()` (theme).
- Navigation is defined in `composeApp/.../navigation/AppNavigation.kt` (string routes in `AppScreen`); the start
  destination is the home screen. A bottom `NavigationBar` switches between home, calculator and history (history only
  while signed in) and holds the last item: "Account" (account icon, opens `AccountScreen`) when signed in, "Sign in" for guests; the login screen is opened on demand (bar hidden) and closes
  itself after sign in (`MainViewModel.isLoggedIn`). Tabs keep their ViewModels, so data screens reload on `ON_RESUME`.
  Account screen: FOP group chips saved to / removed from Firestore (`AccountUseCase.saveGroup`), log out button and an
  outlined "Delete account" button; deletion is confirmed in a `ModalBottomSheet` (bottom sheets act as confirmation
  dialogs, shared `ConfirmBottomSheet` in `core:common-ui`; deleting a history record asks the same way) and wipes calculations, expenses, profile, device docs and the Firebase account (`accounts:delete`).
  After sign in `MainViewModel` applies the saved group (`AccountUseCase.loadSavedGroup`); when a group is saved (`AccountUseCase.savedGroup`) the calculator hides the group chips and only shows the group with a
  "change it in the Account tab" hint; otherwise it shows the chips plus a small hint to save the group in the account.
- Dashboard: `MonthlyStatsUseCase` groups calculations (incomes by income date, tax by the first income's month) and
  `Expense`s (UAH, stored per user, signed in only) by month for the last 6 months.
- Localization: English (`values/`) and Ukrainian (`values-uk/`) in `core/resource`; add every new string to both files.
  The language is chosen in-app (`AppLanguage`, stored in `PreferenceKey.Language`, default = system). `LanguageHandler`
  (`core:common-ui`) + `applyAppLanguage` (expect/actual: `Locale.setDefault` on Android, `AppleLanguages` on iOS) switch the
  resources locale; `App.kt` wraps the navigation in `key(language)` to re-read strings. The language, theme and FOP group
  survive logout.
- Strings go to `core/resource/src/commonMain/composeResources/values/strings.xml`, accessed with
  `stringResource(Res.string.x)`. Do not hardcode user-visible text in composables.
- Money is shown with `Double.formatNumber()` (`core:common-ui/utils/Format.kt`), dates with `LocalDate.formatDate()`.
- Tax logic: `CalculateTaxUseCase` (UAH income = Σ amount × NBU rate of each income date; each tax is rounded to
  2 decimals). Currencies are listed in `Currency`; `UAH` skips the NBU request. NBU endpoint:
  `https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?valcode=USD&date=yyyyMMdd&json`
  (empty array = no rate for that date).

## Commands

```shell
./gradlew :composeApp:assembleDebug                     # build Android debug APK
./gradlew :composeApp:compileKotlinIosSimulatorArm64    # verify iOS compilation
./gradlew :core:domen:testDebugUnitTest                 # domain unit tests
```

iOS: open `iosApp/iosApp.xcworkspace` after `pod install`; the Xcode project embeds the `ComposeApp` framework.

## Adding things

- **New screen/feature**: create `feature/<name>` (copy `feature/history/build.gradle.kts`, change namespace and
  framework baseName), add it to `settings.gradle.kts` and `composeApp/build.gradle.kts`, register its Koin module
  and a route in `AppNavigation`.
- **New API**: DTO in `core/network/dto`, interface in `core/domen/repository`, implementation in
  `core/network/repository`, bind it in `networkModule`, expose via a use case in `core/domen/usecase`.
- **Tax rule changes** (rates, fixed amounts, income limits per year): edit `FopGroup` / `CalculateTaxUseCase` and update
  `core/domen/src/commonTest/.../CalculateTaxUseCaseTest.kt`.

## Things to keep in mind

- Never commit real keystores/passwords; release signing is not configured in this repo.
- The Firebase Web API key is public by design; security comes from Firestore rules — keep `firestore.rules` in sync
  with the data layout.
- Gradle: AGP 9 with `android.builtInKotlin=false` / `android.newDsl=false` (see `gradle.properties`).
