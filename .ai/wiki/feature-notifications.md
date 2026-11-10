# 🔔 Фіча "Сповіщення" (:feature:notifications)

## 📌 Огляд фічі

Push-повідомлення (нагадування про податкові дедлайни), отримані пристроєм, кешуються в DataStore і доступні користувачеві на окремому екрані. На головному екрані показується іконка сповіщень із міткою для непрочитаних.

---

## 🏗️ Структура

- **`core:domen`**
  - `PushMessage` (`id`, `title`, `body`, `receivedAt` у мс, `isRead`) — серіалізована модель.
  - `PreferenceKey.PushMessagesKey` — JSON-список повідомлень у DataStore. Не входить до `getSavedKeys()`, тому очищається при виході з акаунта.
  - `PushMessagesUseCase` — `messages` / `unreadCount` (Flow), `add`, `markAsRead`, `markAllAsRead`, `delete`, `clear`. Нові повідомлення зверху, дублікати за `id` ігноруються, зберігається не більше 100. Зміни захищені `Mutex`.
- **`feature:notifications`**
  - `NotificationsScreen` — список карток (заголовок, текст, дата й час), непрочитані підсвічені з крапкою; дії: видалити одне, "позначити всі прочитаними", "очистити всі" (через `ConfirmBottomSheet`). Усі повідомлення позначаються прочитаними при виході з екрана.
  - `NotificationsViewModel`, `model/NotificationsUiState.kt`, `di/Module.kt` (`featureNotificationsModule()`, реєструється в `initKoin`).
- **`composeApp`**: маршрут `AppScreen.Notifications` (`notifications`), відкривається з `HomeScreen` (`onOpenNotifications`).

---

## 📥 Отримання повідомлень (Android)

`TaxMessagingService.onMessageReceived` бере `title`/`body` з блоку `notification` або з `data`, зберігає повідомлення через `PushMessagesUseCase.add` і показує системне сповіщення (за наявності дозволу `POST_NOTIFICATIONS`).

> Якщо повідомлення містить блок `notification`, у фоні його показує система й `onMessageReceived` не викликається, тому воно **не кешується**. Щоб кешувати повідомлення й у фоні, скрипт `reminders/send.js` надсилає data-only повідомлення (`data: { title, body }`) для Android; для iOS залишається `aps.alert`. Кешу на iOS немає.

> **Сума неоплачених податків.** `MainViewModel` стежить за доходами й через `PushNotificationUseCase.syncUnpaidTax()` записує в `users/{uid}/devices/{token}` поле `unpaidTax` (сума `unpaidAmount` розрахованих записів). `send.js` додає до тексту нагадування «Неоплачено: N ₴», якщо `unpaidTax > 0`.

---

## 🧪 Тестування

```shell
cd reminders && npm install
export FIREBASE_SERVICE_ACCOUNT="$(cat /шлях/до/service-account.json)"
TEST_DATE=2026-11-06 node send.js
```

`TEST_DATE` (або `FORCE=true`) обходить перевірку "вже надсилали сьогодні". Скрипт шле всім зареєстрованим пристроям, що підпадають під правило. Також можна запустити workflow `tax-reminders` вручну (`workflow_dispatch`).
