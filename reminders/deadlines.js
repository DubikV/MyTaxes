// Tax deadlines of Ukrainian FOP. A deadline that falls on a weekend moves to the next Monday; public
// holidays are not known here, the reminder 3 days before the deadline leaves a margin.
// Verify the rules against the current Tax Code before each year.

const REMINDER_OFFSETS = [3, 0]; // days before the deadline

// Each rule: which FOP groups it concerns, on which days of which months, and the message key.
const RULES = [
  // Groups 1-2: fixed single tax and military levy are paid monthly by the 20th
  { groups: ["group1", "group2"], months: "all", day: 20, kind: "monthlyTax" },
  // Group 3: quarterly declaration (40 days after the quarter) and single tax + military levy (50 days)
  { groups: ["group3_5", "group3_3"], months: [2, 5, 8, 11], day: null, kind: "declaration" },
  { groups: ["group3_5", "group3_3"], months: [2, 5, 8, 11], day: null, kind: "quarterTax" },
  // ESV "for yourself" is quarterly for every group: by the 20th of the month after the quarter
  { groups: ["group1", "group2", "group3_5", "group3_3"], months: [1, 4, 7, 10], day: 20, kind: "esv" },
];

// Group 3 deadlines differ by month: declaration on the 9th/10th, payment on the 19th/20th
const DECLARATION_DAY = { 2: 9, 5: 10, 8: 9, 11: 9 };
const PAYMENT_DAY = { 2: 19, 5: 20, 8: 19, 11: 19 };

function ruleDay(rule, month) {
  if (rule.kind === "declaration") return DECLARATION_DAY[month];
  if (rule.kind === "quarterTax") return PAYMENT_DAY[month];
  return rule.day;
}

// A calculation made since this day counts as the payment of the deadline: after the end of the paid period
// (the month itself for the monthly tax, the quarter that ended in the previous month for the others).
function paidSince(rule, year, month) {
  if (rule.kind === "monthlyTax" || rule.kind === "esv") return { y: year, m: month, d: 1 };
  const index = year * 12 + (month - 1) - 1;
  return { y: Math.floor(index / 12), m: (index % 12) + 1, d: 1 };
}

function moveOffWeekend({ y, m, d }) {
  const weekday = new Date(Date.UTC(y, m - 1, d)).getUTCDay(); // 0 = Sunday, 6 = Saturday
  const shifted = new Date(Date.UTC(y, m - 1, d + (weekday === 6 ? 2 : weekday === 0 ? 1 : 0)));
  return { y: shifted.getUTCFullYear(), m: shifted.getUTCMonth() + 1, d: shifted.getUTCDate() };
}

function daysBetween(from, to) {
  return Math.round((Date.UTC(to.y, to.m - 1, to.d) - Date.UTC(from.y, from.m - 1, from.d)) / 86400000);
}

function isLastDayOfMonth({ y, m, d }) {
  const nextDay = new Date(Date.UTC(y, m - 1, d + 1));
  return nextDay.getUTCMonth() !== m - 1;
}

/**
 * Returns the reminders due on [today] ({y, m, d}) for [group]:
 * [{ kind, daysLeft, paidSince }], daysLeft is one of REMINDER_OFFSETS, paidSince is {y, m, d}.
 */
function remindersFor(today, group) {
  const result = [];
  for (const rule of RULES) {
    if (!rule.groups.includes(group)) continue;
    // The deadline can be in this month or (for the offset) in the next one
    for (const shift of [0, 1]) {
      const index = today.y * 12 + (today.m - 1) + shift;
      const year = Math.floor(index / 12);
      const month = (index % 12) + 1;
      if (rule.months !== "all" && !rule.months.includes(month)) continue;
      const deadline = moveOffWeekend({ y: year, m: month, d: ruleDay(rule, month) });
      const daysLeft = daysBetween(today, deadline);
      if (REMINDER_OFFSETS.includes(daysLeft)) result.push({ kind: rule.kind, daysLeft, paidSince: paidSince(rule, year, month) });
    }
  }
  return result;
}

function monthlyRemindersFor(today, group) {
  const result = [];
  for (const rule of RULES) {
    if (!rule.groups.includes(group)) continue;
    const month = today.m;
    const year = today.y;
    if (rule.months !== "all" && !rule.months.includes(month)) continue;
    result.push({ kind: rule.kind, daysLeft: 0, paidSince: paidSince(rule, year, month) });
  }
  if (result.length === 0) {
    result.push({ kind: "monthlyTax", daysLeft: 0, paidSince: { y: today.y, m: today.m, d: 1 } });
  }
  return result;
}

const TEXTS = {
  uk: {
    title: { 0: "Сьогодні строк сплати податків", 3: "Через 3 дні строк сплати податків" },
    monthlyTitle: "Нагадування про сплату податків",
    monthlyTax: "Єдиний податок і військовий збір ФОП 1-2 групи",
    declaration: "Подання декларації платника єдиного податку",
    quarterTax: "Сплата єдиного податку і військового збору за квартал",
    esv: "Сплата ЄСВ",
  },
  en: {
    title: { 0: "Tax deadline is today", 3: "Tax deadline in 3 days" },
    monthlyTitle: "Tax payment reminder",
    monthlyTax: "Single tax and military levy (FOP groups 1-2)",
    declaration: "Single tax declaration",
    quarterTax: "Quarterly single tax and military levy payment",
    esv: "Single social contribution (ESV) payment",
  },
};

function messageFor(reminders, language, isMonthly = false) {
  const t = TEXTS[language] || TEXTS.en;
  if (isMonthly) {
    const body = reminders.map((r) => t[r.kind] || t.monthlyTax).join("; ");
    return { title: t.monthlyTitle, body };
  }
  const daysLeft = Math.min(...reminders.map((r) => r.daysLeft));
  return { title: t.title[daysLeft], body: reminders.map((r) => t[r.kind]).join("; ") };
}

module.exports = { remindersFor, monthlyRemindersFor, isLastDayOfMonth, messageFor };
