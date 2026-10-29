// Sends the tax reminders due today to all registered devices through FCM.
// Runs from GitHub Actions (.github/workflows/tax-reminders.yml), so no Cloud Functions / Blaze plan is needed.
const { initializeApp, cert } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const { remindersFor, monthlyRemindersFor, isLastDayOfMonth, messageFor } = require("./deadlines");

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

// Start of the given day in [timeZone] as epoch millis (the offset is taken near that instant)
function startOfDay({ y, m, d }, timeZone) {
  const utc = Date.UTC(y, m - 1, d);
  const local = Date.parse(`${new Date(utc).toLocaleString("en-US", { timeZone, hour12: false })} UTC`);
  return utc - (local - utc);
}

async function main() {
  initializeApp({ credential: cert(JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT)) });
  const db = getFirestore();
  const today = nowIn(TIME_ZONE);
  const dateKey = `${today.y}-${today.m}-${today.d}`;
  const force = process.env.FORCE === "true" || Boolean(process.env.TEST_DATE);

  // The workflow is scheduled twice (summer / winter time) and GitHub May delay runs: send once per day
  const marker = db.doc("meta/reminders");
  if (!force) {
    if (today.hour < SEND_FROM_HOUR) return console.log(`Too early in ${TIME_ZONE} (${today.hour}h), skipping`);
    if ((await marker.get()).get("lastSent") === dateKey) return console.log("Already sent today, skipping");
  }

  const devices = await db.collectionGroup("devices").get();
  const messages = [];
  const owners = [];
  // Calculating the taxes counts as paying them: skip the deadlines the user has already calculated
  const lastCalculation = new Map();
  const lastCalculationAt = async (uid) => {
    if (!lastCalculation.has(uid)) {
      const latest = await db.collection(`users/${uid}/calculations`).orderBy("createdAt", "desc").limit(1).get();
      lastCalculation.set(uid, latest.empty ? 0 : Number(latest.docs[0].get("createdAt")) || 0);
    }
    return lastCalculation.get(uid);
  };

  for (const doc of devices.docs) {
    const { group, language, taxReminderMode } = doc.data();
    const mode = taxReminderMode || "as_needed";
    let due = [];
    let isMonthly = false;

    if (mode === "monthly") {
      if (!isLastDayOfMonth(today)) continue;
      due = monthlyRemindersFor(today, group);
      isMonthly = true;
    } else {
      due = remindersFor(today, group);
    }

    if (due.length === 0) continue;
    const lastAt = await lastCalculationAt(doc.ref.parent.parent.id);
    const reminders = due.filter((r) => lastAt < startOfDay(r.paidSince, TIME_ZONE));
    if (reminders.length === 0) continue;
    const { title, body } = messageFor(reminders, language, isMonthly);
    messages.push({
      token: doc.id,
      notification: { title, body },
      // High priority wakes a device in Doze mode
      android: { priority: "high", notification: { channelId: CHANNEL_ID } },
      apns: { headers: { "apns-priority": "10" }, payload: { aps: { sound: "default" } } },
    });
    owners.push(doc.ref);
  }

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
