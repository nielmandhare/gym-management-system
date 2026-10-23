import { cookies } from "next/headers";
import { connection } from "next/server";
import type { SupabaseClient } from "@supabase/supabase-js";
import { can } from "@/lib/auth/permissions";
import type { Action, Role } from "@/lib/auth/permissions";
import { ACCESS_COOKIE, REFRESH_COOKIE, REFRESH_MAX_AGE, cookieOptions } from "@/lib/auth/session-cookies";
import { ApiError } from "@/lib/http";
import { getSupabase } from "@/lib/supabase/server";

export type AuthUser = { id: string; email: string; full_name: string | null; role: Role };

export const isSecureRequest = (request: Request) =>
  new URL(request.url).protocol === "https:" ||
  request.headers.get("x-forwarded-proto") === "https";

/** Stores the Supabase session in httpOnly cookies (invisible to page scripts). */
export async function setSessionCookies(
  session: { access_token: string; refresh_token: string; expires_in: number },
  secure: boolean,
) {
  const jar = await cookies();
  jar.set(ACCESS_COOKIE, session.access_token, cookieOptions(secure, session.expires_in));
  jar.set(REFRESH_COOKIE, session.refresh_token, cookieOptions(secure, REFRESH_MAX_AGE));
}

export async function clearSessionCookies() {
  const jar = await cookies();
  jar.delete(ACCESS_COOKIE);
  jar.delete(REFRESH_COOKIE);
}

/** Loads the profile (and so the role) of the user behind a token. */
export async function loadUser(
  db: SupabaseClient,
  token: string,
): Promise<AuthUser | null> {
  const { data, error } = await db.auth.getUser(token);
  if (error || !data.user) return null;
  const { data: profile } = await db
    .from("profiles")
    .select("full_name, role")
    .eq("id", data.user.id)
    .maybeSingle();
  if (!profile) return null;
  return {
    id: data.user.id,
    email: data.user.email ?? "",
    full_name: profile.full_name,
    role: profile.role as Role,
  };
}

/**
 * Every protected API route calls this first. Returns a Supabase client that
 * acts as the logged-in user, or throws 401 (not logged in) / 403 (role not
 * allowed to perform the action).
 */
export async function requireAuth(action: Action): Promise<{ db: SupabaseClient; user: AuthUser }> {
  await connection(); // always evaluated at request time, never prerendered
  const token = (await cookies()).get(ACCESS_COOKIE)?.value;
  if (!token) throw new ApiError(401, "UNAUTHENTICATED", "Please log in to continue");
  const db = getSupabase(token);
  const user = await loadUser(db, token);
  if (!user) throw new ApiError(401, "UNAUTHENTICATED", "Your session has expired. Please log in again");
  if (!can(user.role, action)) {
    throw new ApiError(403, "FORBIDDEN", "You do not have permission to do this");
  }
  return { db, user };
}
