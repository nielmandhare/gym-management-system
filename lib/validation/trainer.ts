import { isValidEmail, isValidName, isValidPhone } from "@/lib/validation/member";
import type { FieldErrors, ValidationResult } from "@/lib/validation/member";

export type TrainerInput = {
  full_name: string;
  email: string;
  phone: string;
  specialization: string;
};

export function validateTrainerInput(input: unknown): ValidationResult<TrainerInput> {
  if (typeof input !== "object" || input === null || Array.isArray(input)) {
    return { valid: false, errors: { _form: "Request body must be a JSON object" } };
  }
  const obj = input as Record<string, unknown>;
  const str = (v: unknown) => (typeof v === "string" ? v.trim() : v);

  const errors: FieldErrors = {};
  if (!isValidName(obj.full_name)) errors.full_name = "Full name is required (max 100 characters)";
  if (!isValidEmail(str(obj.email))) errors.email = "Enter a valid email address";
  if (!isValidPhone(str(obj.phone))) errors.phone = "Phone number must contain exactly 10 digits";
  if (!isValidName(obj.specialization)) errors.specialization = "Specialization is required";
  if (Object.keys(errors).length > 0) return { valid: false, errors };

  return {
    valid: true,
    value: {
      full_name: (obj.full_name as string).trim(),
      email: (obj.email as string).trim().toLowerCase(),
      phone: (obj.phone as string).trim(),
      specialization: (obj.specialization as string).trim(),
    },
  };
}
