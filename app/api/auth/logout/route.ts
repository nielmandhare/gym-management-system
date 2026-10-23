import { clearSessionCookies } from "@/lib/auth/session";
import { handle, ok } from "@/lib/http";

export async function POST() {
  return handle(async () => {
    await clearSessionCookies();
    return ok({ logged_out: true });
  });
}
