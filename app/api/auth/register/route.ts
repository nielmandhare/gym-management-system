import type { NextRequest } from "next/server";
import { isSecureRequest, loadUser, setSessionCookies } from "@/lib/auth/session";
import { ApiError, handle, ok, readJson } from "@/lib/http";
import { getSupabase } from "@/lib/supabase/server";
import { anonAuthClient, fromAuthError } from "@/lib/services/users";
import { validateRegister } from "@/lib/validation/auth";

/** Public self-registration. The new account is always a 'customer'. */
export async function POST(request: NextRequest) {
  return handle(async () => {
    const result = validateRegister(await readJson(request));
    if (!result.valid) {
      throw new ApiError(400, "VALIDATION_ERROR", "Invalid registration data", result.errors);
    }
    const { email, password, full_name } = result.value;
    const { data, error } = await anonAuthClient().auth.signUp({
      email,
      password,
      options: { data: { full_name } },
    });
    if (error) throw fromAuthError(error);
    if (!data.user || (data.user.identities?.length ?? 1) === 0) {
      throw new ApiError(409, "DUPLICATE_EMAIL", "An account with this email already exists", {
        email: "This email is already registered",
      });
    }
    // With "Confirm email" off Supabase returns a session: log the user in.
    if (data.session) {
      const user = await loadUser(getSupabase(data.session.access_token), data.session.access_token);
      await setSessionCookies(data.session, isSecureRequest(request));
      return ok({ user, needs_confirmation: false }, 201);
    }
    return ok({ user: null, needs_confirmation: true }, 201);
  });
}
