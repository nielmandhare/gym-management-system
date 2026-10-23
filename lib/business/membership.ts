// Pure business rules for memberships. No I/O, so each function is
// independently testable. Dates are plain "YYYY-MM-DD" strings.

export const MEMBERSHIP_PLANS = ["Monthly", "Quarterly", "Yearly"] as const;
export type MembershipPlan = (typeof MEMBERSHIP_PLANS)[number];
export type MembershipStatus = "Active" | "Expired";

// Demonstration prices only (INR); not real market prices.
export const PLAN_DETAILS: Record<
  MembershipPlan,
  { months: number; priceInr: number; summary: string }
> = {
  Monthly: { months: 1, priceInr: 1000, summary: "Full gym access for 1 month" },
  Quarterly: { months: 3, priceInr: 2700, summary: "Full gym access for 3 months" },
  Yearly: { months: 12, priceInr: 9600, summary: "Full gym access for 12 months" },
};

export function isValidPlan(plan: unknown): plan is MembershipPlan {
  return (
    typeof plan === "string" &&
    (MEMBERSHIP_PLANS as readonly string[]).includes(plan)
  );
}

/** True only for a real calendar date written as YYYY-MM-DD. */
export function isValidDateString(value: unknown): value is string {
  if (typeof value !== "string" || !/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/.test(value)) {
    return false;
  }
  const [y, m, d] = value.split("-").map(Number);
  const date = new Date(Date.UTC(y, m - 1, d));
  return (
    date.getUTCFullYear() === y &&
    date.getUTCMonth() === m - 1 &&
    date.getUTCDate() === d
  );
}

/** Returns an error message, or null when the date range is acceptable. */
export function validateMembershipDates(
  startDate: unknown,
  endDate: unknown,
): string | null {
  if (!isValidDateString(startDate)) return "Start date must be a valid date (YYYY-MM-DD)";
  if (!isValidDateString(endDate)) return "End date must be a valid date (YYYY-MM-DD)";
  if (endDate < startDate) return "End date must not be earlier than start date";
  return null;
}

/**
 * Business rule: a membership is Active while the current date is on or
 * before its end date (the end date itself is still Active) and Expired
 * from the day after. Throws RangeError for an invalid date range.
 */
export function calculateMembershipStatus(
  startDate: string,
  endDate: string,
  currentDate: string,
): MembershipStatus {
  const rangeError = validateMembershipDates(startDate, endDate);
  if (rangeError) throw new RangeError(rangeError);
  if (!isValidDateString(currentDate)) {
    throw new RangeError("Current date must be a valid date (YYYY-MM-DD)");
  }
  return endDate >= currentDate ? "Active" : "Expired";
}

/** Adds whole months to a YYYY-MM-DD date, clamping to the month's last day. */
export function addMonths(date: string, months: number): string {
  if (!isValidDateString(date)) throw new RangeError("Invalid date");
  const [y, m, d] = date.split("-").map(Number);
  const target = new Date(Date.UTC(y, m - 1 + months, 1));
  const lastDay = new Date(
    Date.UTC(target.getUTCFullYear(), target.getUTCMonth() + 1, 0),
  ).getUTCDate();
  target.setUTCDate(Math.min(d, lastDay));
  return target.toISOString().slice(0, 10);
}

/** Local calendar date of the given instant as YYYY-MM-DD. */
export function todayISO(now: Date = new Date()): string {
  const mm = String(now.getMonth() + 1).padStart(2, "0");
  const dd = String(now.getDate()).padStart(2, "0");
  return `${now.getFullYear()}-${mm}-${dd}`;
}
