import { createClient } from "@supabase/supabase-js";
import type { SupabaseClient } from "@supabase/supabase-js";
import type { Role } from "@/lib/auth/permissions";
import { ApiError, fromDbError } from "@/lib/http";
import type { CreateUserInput } from "@/lib/validation/auth";

export type Profile = {
  id: string;
  email: string;
  full_name: string | null;
  role: Role;
  created_at: string;
};

/** Throws a safe ApiError for Supabase Auth failures. */
export function fromAuthError(error: { code?: string; status?: number; message?: string }): ApiError {
  if (error.code === "user_already_exists" || /already registered/i.test(error.message ?? "")) {
    return new ApiError(409, "DUPLICATE_EMAIL", "An account with this email already exists", {
      email: "This email is already registered",
    });
  }
  if (error.code === "weak_password") {
    return new ApiError(400, "VALIDATION_ERROR", "Password is too weak", {
      password: "Choose a stronger password",
    });
  }
  if (error.code === "over_email_send_rate_limit" || error.status === 429) {
    return new ApiError(429, "RATE_LIMITED", "Too many attempts. Please wait and try again");
  }
  console.error("Auth error:", error.code, error.message);
  return new ApiError(500, "INTERNAL_ERROR", "Something went wrong on the server");
}

/** A client with no stored session, used only to call auth.signUp. */
export function anonAuthClient(): SupabaseClient {
  return createClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL as string,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY as string,
    { auth: { persistSession: false, autoRefreshToken: false } },
  );
}

export async function listProfiles(db: SupabaseClient): Promise<Profile[]> {
  const { data, error } = await db.from("profiles").select("*").order("created_at", { ascending: false });
  if (error) throw fromDbError(error, "user");
  return data as Profile[];
}

export async function changeRole(db: SupabaseClient, id: string, role: Role): Promise<Profile> {
  const { data, error } = await db.from("profiles").update({ role }).eq("id", id).select().maybeSingle();
  if (error) throw fromDbError(error, "user");
  if (!data) throw new ApiError(404, "NOT_FOUND", "User not found");
  return data as Profile;
}

/**
 * Admin creates an account. Supabase Auth creates it (always as 'customer' via
 * the database trigger); the admin's own token is then used to set the role.
 */
export async function createUser(db: SupabaseClient, input: CreateUserInput): Promise<Profile> {
  const { data, error } = await anonAuthClient().auth.signUp({
    email: input.email,
    password: input.password,
    options: { data: { full_name: input.full_name } },
  });
  if (error) throw fromAuthError(error);
  // Supabase hides duplicates when "Confirm email" is on: identities is empty.
  if (!data.user || (data.user.identities?.length ?? 1) === 0) {
    throw new ApiError(409, "DUPLICATE_EMAIL", "An account with this email already exists", {
      email: "This email is already registered",
    });
  }
  return changeRole(db, data.user.id, input.role);
}
