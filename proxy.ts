import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { createClient } from "@supabase/supabase-js";
import { ACCESS_COOKIE, REFRESH_COOKIE, REFRESH_MAX_AGE, cookieOptions } from "@/lib/auth/session-cookies";

const PUBLIC_PAGES = ["/login", "/register"];

/**
 * Runs before every page and API request (except auth endpoints and assets).
 *  1. If the short-lived access cookie expired but a refresh cookie exists,
 *     get a new session from Supabase and retry the same URL.
 *  2. Pages other than /login and /register need a session cookie,
 *     otherwise the visitor is sent to /login.
 * This is only a convenience redirect. Real protection is in the API
 * (requireAuth) and in Row Level Security, which never trust this check.
 */
export async function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const access = request.cookies.get(ACCESS_COOKIE)?.value;
  const refresh = request.cookies.get(REFRESH_COOKIE)?.value;
  const isApi = pathname.startsWith("/api/");
  const secure = request.nextUrl.protocol === "https:";

  if (!access && refresh) {
    const url = process.env.NEXT_PUBLIC_SUPABASE_URL;
    const key = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY;
    if (url && key) {
      const supabase = createClient(url, key, { auth: { persistSession: false, autoRefreshToken: false } });
      const { data, error } = await supabase.auth.refreshSession({ refresh_token: refresh });
      if (!error && data.session) {
        const retry = NextResponse.redirect(request.nextUrl, 307);
        retry.cookies.set(ACCESS_COOKIE, data.session.access_token, cookieOptions(secure, data.session.expires_in));
        retry.cookies.set(REFRESH_COOKIE, data.session.refresh_token, cookieOptions(secure, REFRESH_MAX_AGE));
        return retry;
      }
    }
    // Refresh failed: drop the dead cookie and continue as logged out.
    const res = isApi || PUBLIC_PAGES.includes(pathname)
      ? NextResponse.next()
      : NextResponse.redirect(new URL("/login", request.url));
    res.cookies.delete(REFRESH_COOKIE);
    return res;
  }

  const loggedIn = Boolean(access);
  if (!isApi) {
    if (!loggedIn && !PUBLIC_PAGES.includes(pathname)) {
      return NextResponse.redirect(new URL("/login", request.url));
    }
    if (loggedIn && PUBLIC_PAGES.includes(pathname)) {
      return NextResponse.redirect(new URL("/", request.url));
    }
  }
  return NextResponse.next();
}

export const config = {
  // Skip static assets, the PWA files (manifest, service worker, icons: the browser
  // fetches them without cookies) and the auth endpoints (login must work when logged out).
  matcher: ["/((?!_next/static|_next/image|favicon.ico|manifest.webmanifest|sw.js|offline.html|icons/|icon.png|apple-icon.png|api/auth/).*)"],
};
