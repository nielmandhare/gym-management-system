import { requireAuth } from "@/lib/auth/session";
import { calculateMembershipStatus, todayISO } from "@/lib/business/membership";
import { fromDbError, handle, ok } from "@/lib/http";

/**
 * "My details" for the logged-in user, matched to the gym records by email:
 * a customer gets their membership, a trainer gets their trainer record.
 * Row Level Security also limits what each role can read.
 */
export async function GET() {
  return handle(async () => {
    const { db, user } = await requireAuth("me:read");
    const base = { user, member: null as unknown, trainer: null as unknown };

    if (user.role === "customer") {
      const { data, error } = await db.from("members").select("*").eq("email", user.email.toLowerCase()).maybeSingle();
      if (error) throw fromDbError(error, "member");
      if (data) {
        base.member = {
          ...data,
          status: calculateMembershipStatus(data.start_date, data.end_date, todayISO()),
        };
      }
    } else if (user.role === "trainer") {
      const { data, error } = await db.from("trainers").select("*").eq("email", user.email.toLowerCase()).maybeSingle();
      if (error) throw fromDbError(error, "trainer");
      base.trainer = data;
    }
    return ok(base);
  });
}
