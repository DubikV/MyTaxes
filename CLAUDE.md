# CLAUDE.md

Guidance for AI assistants working in this repository.

## Project

**My Assistant (My Taxes)** — Kotlin Multiplatform + Compose Multiplatform app (Android, iOS).
The app assists Ukrainian sole proprietors (FOP / ФОП) with:
1. Income tracking & tax calculation (NBU exchange rate auto-fetching, single tax, military levy, ESV calculation).
2. Car management & maintenance/service records tracking (odometer reading, service descriptions, attached photos).
3. Profile settings (nickname, avatar photo, FOP group, base currency, tax reminder notifications).

Package root: `com.gmail.vanyadubik.myassistant`. Android applicationId: `com.gmail.vanyadubik.myassistant` (debug suffix `.debug`).

## Tech stack

Kotlin 2.4 · Compose Multiplatform 1.9 (Material3) · Navigation Compose · Koin (DI, `viewModelOf`) ·
Ktor client (OkHttp / Darwin) · kotlinx.serialization · kotlinx.datetime · AndroidX DataStore (preferences) ·
Gradle version catalog (`gradle/libs.versions.toml`, type-safe project accessors `projects.core.domen`).

Firebase is used **through REST APIs with Ktor** (no native Firebase SDKs, no google-services.json):
- Auth: Identity Toolkit (`accounts:signInWithPassword`, `accounts:signUp`) and Secure Token (`/token` refresh).
- Firestore: `users/{uid}/profile/main` (nickname, Base64 avatar, fopGroup, currency, reminder mode, cars) and `users/{uid}/auto_records/{id}`, authorized with the user's ID token.
- Push reminders (FCM): `PushNotificationUseCase` stores the device token + FOP group + language in `users/{uid}/devices/{token}` (removed on logout).
- Config (Web API key, project id): `composeApp/.../di/FirebaseSettings.kt`.

## Module layout and dependency rules

```
composeApp            app entry points, Koin start, navigation, MainViewModel, splash
feature:login         LoginScreen + LoginViewModel (sign in / sign up with email, password, nickname)
feature:account       AccountScreen + AccountViewModel (nickname, avatar, FOP group, currency, cars, logout, delete account)
feature:home          HomeScreen + HomeViewModel (overview cards for untaxed incomes, unpaid taxes, cars)
feature:taxes         TaxesScreen + TaxesHistoryScreen (income tracking & tax calculation)
feature:auto          AutoScreen + AutoViewModel (car management & service records with photos)
core:domen            pure Kotlin domain: models, repository interfaces, use cases (no Ktor/Compose)
core:network          Ktor client, DTOs, repository implementations (NBU, Firebase Auth, Firestore)
core:data-store       DataStore implementation of DataStoreRepository (expect/actual per platform)
core:common           platform helpers (logger, PlatformConfig, AppInfo, date utils) + `commonPlatformModule`
core:common-ui        theme (animated light/dark switch), shared components, dialogs, image pickers/previews, formatting helpers
core:resource         compose resources (strings, drawables) and colors; `Res` class in `com.gmail.vanyadubik.myassistant.resource`
```

Dependencies flow downwards: `composeApp → feature:* → core:common-ui / core:network / core:data-store → core:domen → core:common`. Features never depend on each other; the app module wires them together. Domain code must not depend on network/UI. Repository **interfaces live in `core:domen`**, implementations in `core:network` / `core:data-store`.

## Conventions

- Each module has `commonMain`, `androidMain`, `iosMain`; platform differences use `expect/actual`.
- Each module exposes a Koin module function in `di/Module.kt` (`domenModule()`, `networkModule(...)`, `featureLoginModule()` …). Register new modules in `composeApp/.../di/KoinModules.kt` (`initKoin`).
- UI state: ViewModel exposes `StateFlow<UiState>` (immutable data class); screens use `collectAsState()` and `koinViewModel()`. Screens take navigation callbacks as parameters.
- **Model and Screen File Structure**: UI state models MUST be extracted into a separate file under `model/` (e.g. `featureaccount/model/AccountUiState.kt`). If a feature contains 2+ screens, group screen files into subdirectories under `screens/` (e.g. `screens/taxes/` and `screens/history/`).
- **Error Handling & Retry Rules**:
  - **Data Load Failures**: Render `ErrorContainerCard` (error icon `ErrorOutline`, title "Failed to load data" / "Не вдалося завантажити дані", error message, and "Retry" / "Повторити" button with `Refresh` icon that re-triggers data loading).
  - **Action / Submission Failures**: Render `ConfirmBottomSheet` with title "Oops, something went wrong" / "Упс, щось пішло не так", error message, confirm button "Retry" / "Повторити" (re-executes failed action via `retryLastAction`), and cancel button.
- **Confirmation Dialogs**: All confirmation dialogs for destructive actions (deleting records, history items, account) MUST be implemented using `ConfirmBottomSheet` from `core:common-ui`.
- **Photo Picker & Image Preview**:
  - `rememberPhotoPickerLauncher`: Presents a `ModalBottomSheet` on Android offering choice between Camera (`ActivityResultContracts.TakePicturePreview()`) and Gallery (`ActivityResultContracts.GetContent()`). Photos are resized (800px max) and compressed to JPEG Base64.
  - `PhotoPreviewDialog`: Full-screen dialog with dark background, pinch-to-zoom (1x–5x), pan dragging, double-tap zoom toggle, single tap or close button to dismiss.
  - `AppAsyncImage`: Supports `onClick` for opening preview, and `onDelete` badge protruding outside the top-right image edge.
- **Navigation**: Defined in `composeApp/.../navigation/AppNavigation.kt`. Bottom `NavigationBar` switches between 4 tabs: **Home**, **Taxes**, **Auto**, and **Account** (or "Sign in" for guests).
- Localization: English (`values/`) and Ukrainian (`values-uk/`) in `core:resource`; add every new string to both files. Strings go to `strings.xml`, accessed with `stringResource(Res.string.x)`.
- Money is shown with `Double.formatNumber()` (`core:common-ui/utils/Format.kt`), dates with `LocalDate.formatDate()`.

## Commands

```shell
./gradlew :composeApp:assembleDebug                     # build Android debug APK
./gradlew :composeApp:compileKotlinIosSimulatorArm64    # verify iOS compilation
./gradlew :core:domen:testDebugUnitTest                 # domain unit tests
```

iOS: open `iosApp/iosApp.xcworkspace` after `pod install`; the Xcode project embeds the `ComposeApp` framework.
