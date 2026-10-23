import {
  isValidDateString,
  isValidPlan,
  validateMembershipDates,
  type MembershipPlan,
} from "@/lib/business/membership";

export type MemberInput = {
  full_name: string;
  email: string;
  phone: string;
  membership_plan: MembershipPlan;
  start_date: string;
  end_date: string;
};

export type FieldErrors = Record<string, string>;

export type ValidationResult<T> =
  | { valid: true; value: T }
  | { valid: false; errors: FieldErrors };

const MEMBER_FIELDS = [
  "full_name",
  "email",
  "phone",
  "membership_plan",
  "start_date",
  "end_date",
] as const;

/** Exactly 10 ASCII digits (academic-demo rule). */
export function isValidPhone(phone: unknown): boolean {
  return typeof phone === "string" && /^[0-9]{10}$/.test(phone);
}

export function isValidEmail(email: unknown): boolean {
  return (
    typeof email === "string" &&
    email.length <= 254 &&
    /^[^\s@]+@[^\s@.]+(\.[^\s@.]+)+$/.test(email)
  );
}

export function isValidName(name: unknown): boolean {
  return typeof name === "string" && name.trim().length > 0 && name.length <= 100;
}

function trimmed(value: unknown): unknown {
  return typeof value === "string" ? value.trim() : value;
}

function fieldError(field: string, value: unknown): string | null {
  switch (field) {
    case "full_name":
      return isValidName(value) ? null : "Full name is required (max 100 characters)";
    case "email":
      return isValidEmail(trimmed(value)) ? null : "Enter a valid email address";
    case "phone":
      return isValidPhone(trimmed(value))
        ? null
        : "Phone number must contain exactly 10 digits";
    case "membership_plan":
      return isValidPlan(value) ? null : "Plan must be Monthly, Quarterly or Yearly";
    default:
      return null;
  }
}

function normalise(field: string, value: unknown): unknown {
  if (typeof value !== "string") return value;
  if (field === "email") return value.trim().toLowerCase();
  if (field === "full_name" || field === "phone") return value.trim();
  return value;
}

function asObject(input: unknown): Record<string, unknown> | null {
  return typeof input === "object" && input !== null && !Array.isArray(input)
    ? (input as Record<string, unknown>)
    : null;
}

/** Validates a complete member (all fields required). */
export function validateMemberInput(input: unknown): ValidationResult<MemberInput> {
  const obj = asObject(input);
  if (!obj) return { valid: false, errors: { _form: "Request body must be a JSON object" } };

  const errors: FieldErrors = {};
  for (const field of MEMBER_FIELDS) {
    const err = fieldError(field, obj[field]);
    if (err) errors[field] = err;
  }
  // Each date is checked on its own; the ordering rule applies only when
  // both are valid, so a bad date is never reported twice.
  if (!isValidDateString(obj.start_date)) {
    errors.start_date = "Start date must be a valid date (YYYY-MM-DD)";
  }
  if (!isValidDateString(obj.end_date)) {
    errors.end_date = "End date must be a valid date (YYYY-MM-DD)";
  }
  if (!errors.start_date && !errors.end_date) {
    const orderError = validateMembershipDates(obj.start_date, obj.end_date);
    if (orderError) errors.end_date = orderError;
  }
  if (Object.keys(errors).length > 0) return { valid: false, errors };

  const value = Object.fromEntries(
    MEMBER_FIELDS.map((f) => [f, normalise(f, obj[f])]),
  ) as MemberInput;
  return { valid: true, value };
}

export function isValidMemberInput(input: unknown): boolean {
  return validateMemberInput(input).valid;
}

/**
 * Validates a partial update. Only supplied known fields are checked;
 * the date-range rule is applied by the caller on the merged record.
 */
export function validateMemberPatch(
  input: unknown,
): ValidationResult<Partial<MemberInput>> {
  const obj = asObject(input);
  if (!obj) return { valid: false, errors: { _form: "Request body must be a JSON object" } };

  const errors: FieldErrors = {};
  const value: Record<string, unknown> = {};
  for (const field of MEMBER_FIELDS) {
    if (!(field in obj)) continue;
    let err: string | null;
    if (field === "start_date" || field === "end_date") {
      // Format check only; ordering is checked against the stored record.
      err = isValidDateString(obj[field])
        ? null
        : `${field === "start_date" ? "Start" : "End"} date must be a valid date (YYYY-MM-DD)`;
    } else {
      err = fieldError(field, obj[field]);
    }
    if (err) errors[field] = err;
    else value[field] = normalise(field, obj[field]);
  }
  if (Object.keys(errors).length === 0 && Object.keys(value).length === 0) {
    errors._form = "Provide at least one field to update";
  }
  if (Object.keys(errors).length > 0) return { valid: false, errors };
  return { valid: true, value: value as Partial<MemberInput> };
}
