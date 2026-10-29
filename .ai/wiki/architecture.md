# 🏛️ Архітектура та стек технологій

## 1. Загальні принципи архітектури

Проект побудовано за принципами Clean Architecture з чітким розділенням відповідальності та принципом спрямування залежностей **згори донизу**:

```
composeApp  --->  feature:*  --->  core:common-ui / core:network / core:data-store  --->  core:domen  --->  core:common
```

- **`composeApp`**: Точка входу в додаток (Android Application / iOS Framework), ініціалізація Koin DI (`initKoin`), головне вікно та верхньорівнева навігація (`AppNavigation`).
- **`feature:*`**: Фічні модулі (`login`, `taxes`, `account`, `home`, `auto`). Фічі **не залежать одна від одної**. Кожна фіча містить свій UI (Compose UI) та ViewModel.
- **`core:domen`**: Чистий Kotlin-модуль без залежностей від Compose чи Ktor. Містить доменні моделі (`TaxCalculation`, `TaxIncomeItem`, `FopGroup`, `Currency`, `AutoServiceRecord`, `Car`, `AccountProfile`), інтерфейси репозиторіїв та бізнес-логіку у вигляді use-case'ів (`CalculateTaxUseCase`, `TaxesUseCase`, `AutoServiceUseCase`, `AccountUseCase` тощо).
- **`core:network`**: Реалізація репозиторіїв для роботи з зовнішніми мережевими API (офіційні курси валют НБУ, Firebase Auth REST API, Cloud Firestore REST API).
- **`core:data-store`**: Кросплатформена реалізація локального сховища Preferences на базі AndroidX DataStore (Expect/Actual для Android та iOS).
- **`core:common-ui`**: Спільна дизайн-система Material 3 (анімована тема, загальні компоненти `AppButton`, `DateField`, `TextFieldWithLabel`, `AppAsyncImage`, `PhotoPreviewDialog`, `rememberPhotoPickerLauncher`, діалоги `ConfirmBottomSheet`, `ErrorContainerCard` та хелпери форматування).
- **`core:resource`**: Ресурси додатка (рядки локалізації `values` / `values-uk`, іконки, кольори).

---

## 2. Управління станом (UI State Management) та обробка помилок

1. **ViewModel**: Кожна фіча використовує `androidx.lifecycle.ViewModel`, оголошений у `commonMain`.
2. **State Flow**: ViewModel надає незмінний `StateFlow<UiState>` (`UiState` є immutable `data class`).
3. **Compose Integration**: Екрани підписуються на стан за допомогою `val state by viewModel.state.collectAsState()`.
4. **DI Injection**: Екрани отримують ViewModel через Koin: `viewModel: TaxesViewModel = koinViewModel()`.
5. **Обробка помилок завантаження даних (Load Errors)**:
   При звої завантаження даних на екрані відображається компонент `ErrorContainerCard` з червоним забарвленням `errorContainer`, заголовком **«Не вдалося завантажити дані»**, деталями помилки та кнопкою **«Повторити»** (`Icons.Default.Refresh`), яка повторно запускає завантаження.
6. **Обробка помилок відправки/дій (Action Errors)**:
   При збої відправки даних або виконання дії (збереження доходу, розрахунок податків, додавання авто, створення запису про обслуговування, оновлення акаунту) відображається шторка `ConfirmBottomSheet` із заголовком **«Упс, щось пішло не так»**, деталями помилки та кнопкою **«Повторити»**, яка повторно викликає невідправлену дію (`retryLastAction`).
7. **Діалоги підтвердження (Confirmation Dialogs)**:
   Всі деструктивні та підтверджувальні дії (видалення запису, видалення акаунту, підтвердження сплати) реалізуються через компонент `ConfirmBottomSheet` з модуля `core:common-ui`.
8. **Структура моделей та екранів у фічах**:
   - Всі моделі UI-стану (`UiState`) виносяться в окремі файли у папці `model/`.
   - Якщо фіча містить декілька екранів, файли екранів групуються по підпапках усередині директорії `screens/` (наприклад, `screens/taxes/` та `screens/history/`).

---

## 3. Робота з фотографіями та медіа

1. **Фото Пікер (`rememberPhotoPickerLauncher`)**:
   Надає можливість вибору джерела фотографії у вигляді нижньої шторки `ModalBottomSheet` із варіантами **«Зробити фото»** (системна камера `ActivityResultContracts.TakePicturePreview()`) та **«Обрати з галереї»** (`ActivityResultContracts.GetContent()`).
2. **Повноекранний перегляд фотографій (`PhotoPreviewDialog`)**:
   Дозволяє переглядати будь-яку світлину при тапі з підтримкою жестів зуму (pinch-to-zoom від 1x до 5x, панорамування при збільшенні, подвійний тап для перемикання масштабу 1x/2.5x).
3. **Відображення фотографій (`AppAsyncImage`)**:
   Підтримує обробку тапу (`onClick`) та кнопку видалення (`onDelete`) з виступаючим круглим бейджем на правому верхньому куті зображення.

---

## 4. Локальне сховище та кешування (DataStore)

Клас `DataStoreRepositoryImpl` працює з `DataStore<Preferences>` для збереження:
- Налаштувань теми (`AppTheme`: System, Dark, Light)
- Метрики мови додатку (`AppLanguage`: Ukrainian, English)
- Збереженої групи ФОП (`FopGroup`: Group 1, Group 2, Group 3 (5%), Group 3 (3%))
- Сесії користувача (Auth ID Token, Refresh Token, User ID, User Email)
- **Кешу доходів та податків (`TaxIncomesKey`)**: JSON-масив об'єктів `TaxIncomeItem`, який серіалізується та десеріалізується за допомогою `kotlinx.serialization.json.Json`.

---

## 5. Мережевий шар та REST Firebase

Додаток **не використовує нативні Firebase SDK** (за винятком Android FCM SDK в `composeApp`), а працює з Firebase безпосередньо через **Ktor HTTP REST Client**:

1. **Курси валют НБУ**:
   - Ендпоінт: `https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?valcode={currency}&date={yyyyMMdd}&json`
   - Автоматично запитує курс НБУ на конкретну дату надходження доходу.
2. **Firebase Auth REST API**:
   - `accounts:signInWithPassword` — вхід за паролем.
   - `accounts:signUp` — реєстрація нового акаунту.
   - `accounts:delete` — видалення акаунту користувача.
3. **Cloud Firestore REST API**:
   - Документи користувача знаходяться за шляхом `users/{uid}/...` і захищені ID Token авторизацією.
   - `users/{uid}/profile/main` — профіль користувача (нікрам, аватар Base64, група ФОП, валюта, налаштування сповіщень та список авто).
   - `users/{uid}/auto_records/{id}` — серіалізовані сервісні записи автообслуговування з фотографіями.

---

## 6. Впровадження залежностей (Koin)

Ініціалізація Koin відбувається у `composeApp` за допомогою функції `initKoin()`:

```kotlin
startKoin {
    modules(
        listOf(
            appScopeModule(),
            domenModule(),
            dataStoreModule(),
            networkModule(firebaseConfig, enableNetworkLogs),
            featureLoginModule(),
            featureTaxesModule(),
            featureAccountModule(),
            featureHomeModule(),
            featureAutoModule(),
            platformModule(),
            commonUiModule(),
            commonModule()
        )
    )
}
```

Кожен модуль надає власний метод `di/Module.kt` (наприклад, `featureTaxesModule()`), що робить структуру модульною та легко розширюваною.
