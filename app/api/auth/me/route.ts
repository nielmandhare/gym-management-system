import { requireAuth } from "@/lib/auth/session";
import { handle, ok } from "@/lib/http";

export async function GET() {
  return handle(async () => {
    const { user } = await requireAuth("me:read");
    return ok(user);
  });
}
