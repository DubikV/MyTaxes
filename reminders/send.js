// Sends the tax reminders due today to all registered devices through FCM.
// Runs from GitHub Actions (.github/workflows/tax-reminders.yml), so no Cloud Functions / Blaze plan is needed.
const { initializeApp, cert } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const { remindersFor, messageFor } = require("./deadlines");

const TIME_ZONE = "Europe/Kyiv";
const SEND_FROM_HOUR = 9;
const CHANNEL_ID = "tax_reminders"; // created by the Android app
const STALE_TOKEN_ERRORS = [
  "messaging/registration-token-not-registered",
  "messaging/invalid-registration-token",
];

function nowIn(timeZone) {
  // TEST_DATE (YYYY-MM-DD) pretends that today is that day, to try the reminders without waiting for a deadline
  const now = process.env.TEST_DATE ? new Date(`${process.env.TEST_DATE}T09:30:00+03:00`) : new Date();
  const [y, m, d] = now.toLocaleDateString("en-CA", { timeZone }).split("-").map(Number);
  const hour = Number(now.toLocaleString("en-GB", { timeZone, hour: "2-digit", hour12: false }));
  return { y, m, d, hour };
}

async function main() {
  initializeApp({ credential: cert(JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT)) });
  const db = getFirestore();
  const today = nowIn(TIME_ZONE);
  const dateKey = `${today.y}-${today.m}-${today.d}`;
  const force = process.env.FORCE === "true" || Boolean(process.env.TEST_DATE);

  // The workflow is scheduled twice (summer / winter time) and GitHub may delay runs: send once per day
  const marker = db.doc("meta/reminders");
  if (!force) {
    if (today.hour < SEND_FROM_HOUR) return console.log(`Too early in ${TIME_ZONE} (${today.hour}h), skipping`);
    if ((await marker.get()).get("lastSent") === dateKey) return console.log("Already sent today, skipping");
  }

  const devices = await db.collectionGroup("devices").get();
  const messages = [];
  const owners = [];
  devices.forEach((doc) => {
    const { group, language } = doc.data();
    const reminders = remindersFor(today, group);
    if (reminders.length === 0) return;
    const { title, body } = messageFor(reminders, language);
    messages.push({
      token: doc.id,
      notification: { title, body },
      // High priority wakes a device in Doze mode
      android: { priority: "high", notification: { channelId: CHANNEL_ID } },
      apns: { headers: { "apns-priority": "10" }, payload: { aps: { sound: "default" } } },
    });
    owners.push(doc.ref);
  });

  const messaging = getMessaging();
  for (let i = 0; i < messages.length; i += 500) {
    const response = await messaging.sendEach(messages.slice(i, i + 500));
    // Drop tokens of uninstalled apps
    await Promise.all(
      response.responses.map((r, index) =>
        !r.success && STALE_TOKEN_ERRORS.includes(r.error?.code) ? owners[i + index].delete() : null,
      ),
    );
  }
  if (!force) await marker.set({ lastSent: dateKey });
  console.log(`Sent ${messages.length} reminders for ${dateKey}`);
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
