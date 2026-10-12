# My Taxes

Kotlin Multiplatform (Android + iOS) app that calculates the Ukrainian single tax and military levy for FOP groups 1, 2, 3 (5% / 3% VAT) on
income received in a foreign currency. The user enters the income date, amount and currency; the app requests the
official National Bank of Ukraine (NBU) exchange rate for that date and shows the tax in UAH. The UI is available in English and Ukrainian (selectable in the app). Signed-in users keep
their calculation history in Firebase Cloud Firestore.

## Setup

1. Create a Firebase project, enable **Authentication → Email/Password** and **Firestore Database**.
2. Put the Web API key and project id into `composeApp/src/commonMain/kotlin/com/gmail/vanyadubik/mytaxes/di/FirebaseSettings.kt`.
3. Publish the Firestore rules from [`firestore.rules`](./firestore.rules).
4. iOS: run `pod install` in `iosApp` (the Pods project is regenerated), then open `iosApp.xcworkspace`.

## Build

```shell
./gradlew :composeApp:assembleDebug          # Android
./gradlew :core:domen:testDebugUnitTest      # unit tests
```

See [CLAUDE.md](./CLAUDE.md) for the architecture description.
