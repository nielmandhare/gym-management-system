// Cookie constants shared by the proxy and the API (no next/headers import,
// so it is safe to use from proxy.ts).
export const ACCESS_COOKIE = "gym_at";
export const REFRESH_COOKIE = "gym_rt";
export const REFRESH_MAX_AGE = 60 * 60 * 24 * 30;

export function cookieOptions(secure: boolean, maxAge: number) {
  return { httpOnly: true, sameSite: "lax" as const, secure, path: "/", maxAge };
}
