import type { NextRequest } from "next/server";
import { isSecureRequest, loadUser, setSessionCookies } from "@/lib/auth/session";
import { ApiError, handle, ok, readJson } from "@/lib/http";
import { getSupabase } from "@/lib/supabase/server";
import { validateLogin } from "@/lib/validation/auth";

export async function POST(request: NextRequest) {
  return handle(async () => {
    const result = validateLogin(await readJson(request));
    if (!result.valid) {
      throw new ApiError(400, "VALIDATION_ERROR", "Invalid login data", result.errors);
    }
    const { data, error } = await getSupabase().auth.signInWithPassword(result.value);
    if (error || !data.session) {
      if (error?.code === "email_not_confirmed") {
        throw new ApiError(403, "EMAIL_NOT_CONFIRMED", "Please confirm your email before logging in");
      }
      if (error && error.status && error.status >= 500) {
        console.error("Login error:", error.code, error.message);
        throw new ApiError(500, "INTERNAL_ERROR", "Something went wrong on the server");
      }
      // Same message for unknown email and wrong password (no account probing).
      throw new ApiError(401, "INVALID_CREDENTIALS", "Invalid email or password");
    }
    const user = await loadUser(getSupabase(data.session.access_token), data.session.access_token);
    if (!user) {
      throw new ApiError(403, "NO_PROFILE", "Account profile not found. Run supabase/schema.sql");
    }
    await setSessionCookies(data.session, isSecureRequest(request));
    return ok(user);
  });
}
