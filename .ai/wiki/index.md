# Вікі проекту "Мій Асистент" (My Assistant)

Ласкаво просимо до технічної вікі-документації проекту **My Assistant** — кросплатформеного додатку для Android та iOS, розробленого на Kotlin Multiplatform (KMP) та Compose Multiplatform.

Додаток призначений для фізичних осіб-підприємців (ФОП) в Україні для обліку доходів та розрахунку податків (єдиний податок, військовий збір, ЄСВ) за курсом НБУ, а також ведення обліку автомобілів та їх сервісного обслуговування.

---

## 📚 Навігація по вікі

1. [🏛️ Архітектура та стек технологій](architecture.md) — детальний опис шарів додатку, модульної структури, DI (Koin), DataStore, мережі Ktor, Firebase та обробки помилок.
2. [💰 Фіча "Податки" (:feature:taxes)](feature-taxes.md) — облік та кешування доходів, розрахунок неоподаткованих доходів, історія податків з підрахунком суми до сплати.
3. [📊 Фіча "Головна" (:feature:home)](feature-home.md) — зведений дашборд із картками неоподаткованих доходів, неоплачених податків та автомобілів.
4. [🚗 Фіча "Автообслуговування" (:feature:auto)](feature-auto.md) — облік автомобілів та історії технічного обслуговування з додаванням фотографій.
5. [👤 Фічі "Акаунт" та "Вхід" (:feature:account & :feature:login)](feature-account-login.md) — аутентифікація через Firebase REST API, збереження нікнейму, аватара, налаштувань ФОП групи та валюти.

---

## 🛠️ Стек технологій

- **Мова**: Kotlin 2.4 (Kotlin Multiplatform)
- **UI Фреймворк**: Compose Multiplatform 1.9 (Material 3)
- **Навігація**: Navigation Compose
- **Впровадження залежностей (DI)**: Koin 4 (`viewModelOf`, DSL)
- **Асинхронність**: Kotlin Coroutines & StateFlow / Flow
- **Мережа**: Ktor Client (OkHttp engine для Android, Darwin engine для iOS)
- **Локальне збереження (Cache)**: AndroidX DataStore Preferences + kotlinx.serialization
- **Серверна частина / База даних**: Firebase (через Ktor REST API):
  - Firebase Auth Identity Toolkit (email/password sign-in, sign-up, token refresh)
  - Cloud Firestore REST API (збереження розрахунків, профілю з аватаром/нікнеймом, авто та записів обслуговування)
- **Сервіс сповіщень**: Firebase Cloud Messaging (FCM, налаштовано через GitHub Actions для нагадувань про податкові дедлайни)
- **Локалізація**: Українська (`values-uk`) та Англійська (`values`) через Compose Resources (`Res`).

---

## 🚀 Як зібрати проект

### Android
```shell
./gradlew :composeApp:assembleDebug
```
Згенерований APK буде доступний у `composeApp/build/outputs/apk/debug/`.

### iOS
```shell
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```
Для запуску на iOS відкрийте `iosApp/iosApp.xcworkspace` в Xcode після виконання `pod install`.
