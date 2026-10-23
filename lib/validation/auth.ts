import { isValidRole } from "@/lib/auth/permissions";
import type { Role } from "@/lib/auth/permissions";
import { isValidEmail, isValidName } from "@/lib/validation/member";
import type { FieldErrors, ValidationResult } from "@/lib/validation/member";

export const MIN_PASSWORD_LENGTH = 8;
export const MAX_PASSWORD_LENGTH = 72;

export function isValidPassword(password: unknown): boolean {
  return (
    typeof password === "string" &&
    password.length >= MIN_PASSWORD_LENGTH &&
    password.length <= MAX_PASSWORD_LENGTH
  );
}

function asObject(input: unknown): Record<string, unknown> | null {
  return typeof input === "object" && input !== null && !Array.isArray(input)
    ? (input as Record<string, unknown>)
    : null;
}

const trim = (v: unknown) => (typeof v === "string" ? v.trim() : v);

export type LoginInput = { email: string; password: string };

/** Login only checks presence/format; password rules apply at registration. */
export function validateLogin(input: unknown): ValidationResult<LoginInput> {
  const obj = asObject(input);
  if (!obj) return { valid: false, errors: { _form: "Request body must be a JSON object" } };
  const errors: FieldErrors = {};
  if (!isValidEmail(trim(obj.email))) errors.email = "Enter a valid email address";
  if (typeof obj.password !== "string" || obj.password.length === 0) {
    errors.password = "Password is required";
  }
  if (Object.keys(errors).length > 0) return { valid: false, errors };
  return {
    valid: true,
    value: { email: (obj.email as string).trim().toLowerCase(), password: obj.password as string },
  };
}

export type RegisterInput = LoginInput & { full_name: string };

export function validateRegister(input: unknown): ValidationResult<RegisterInput> {
  const obj = asObject(input);
  if (!obj) return { valid: false, errors: { _form: "Request body must be a JSON object" } };
  const errors: FieldErrors = {};
  if (!isValidName(obj.full_name)) errors.full_name = "Full name is required (max 100 characters)";
  if (!isValidEmail(trim(obj.email))) errors.email = "Enter a valid email address";
  if (!isValidPassword(obj.password)) {
    errors.password = `Password must be ${MIN_PASSWORD_LENGTH} to ${MAX_PASSWORD_LENGTH} characters`;
  }
  if (Object.keys(errors).length > 0) return { valid: false, errors };
  return {
    valid: true,
    value: {
      full_name: (obj.full_name as string).trim(),
      email: (obj.email as string).trim().toLowerCase(),
      password: obj.password as string,
    },
  };
}

export type CreateUserInput = RegisterInput & { role: Role };

/** Admin creating an account with a chosen role. */
export function validateCreateUser(input: unknown): ValidationResult<CreateUserInput> {
  const base = validateRegister(input);
  const role = asObject(input)?.role;
  if (!base.valid) {
    return {
      valid: false,
      errors: isValidRole(role) ? base.errors : { ...base.errors, role: "Role must be admin, trainer or customer" },
    };
  }
  if (!isValidRole(role)) {
    return { valid: false, errors: { role: "Role must be admin, trainer or customer" } };
  }
  return { valid: true, value: { ...base.value, role } };
}

export function validateRoleChange(input: unknown): ValidationResult<{ role: Role }> {
  const role = asObject(input)?.role;
  if (!isValidRole(role)) {
    return { valid: false, errors: { role: "Role must be admin, trainer or customer" } };
  }
  return { valid: true, value: { role } };
}
