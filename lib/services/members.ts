import { calculateMembershipStatus, todayISO, validateMembershipDates } from "@/lib/business/membership";
import type { MembershipStatus } from "@/lib/business/membership";
import { ApiError, fromDbError } from "@/lib/http";
import type { SupabaseClient } from "@supabase/supabase-js";
import type { MemberInput } from "@/lib/validation/member";

export type MemberRow = MemberInput & { id: string; created_at: string };
export type Member = MemberRow & { status: MembershipStatus };

const notFound = () => new ApiError(404, "NOT_FOUND", "Member not found");

function withStatus(row: MemberRow): Member {
  return {
    ...row,
    status: calculateMembershipStatus(row.start_date, row.end_date, todayISO()),
  };
}

/** Strip characters that have meaning in PostgREST filter syntax. */
function sanitiseSearch(q: string): string {
  return q.replace(/[,()%*\\"]/g, " ").trim().slice(0, 100);
}

export async function listMembers(db: SupabaseClient, search = ""): Promise<Member[]> {
  let query = db.from("members").select("*").order("created_at", { ascending: false });
  const s = sanitiseSearch(search);
  if (s) query = query.or(`full_name.ilike.%${s}%,email.ilike.%${s}%`);
  const { data, error } = await query;
  if (error) throw fromDbError(error, "member");
  return (data as MemberRow[]).map(withStatus);
}

export async function getMember(db: SupabaseClient, id: string): Promise<Member> {
  const { data, error } = await db.from("members").select("*").eq("id", id).maybeSingle();
  if (error) throw fromDbError(error, "member");
  if (!data) throw notFound();
  return withStatus(data as MemberRow);
}

export async function createMember(db: SupabaseClient, input: MemberInput): Promise<Member> {
  const { data, error } = await db.from("members").insert(input).select().single();
  if (error) throw fromDbError(error, "member");
  return withStatus(data as MemberRow);
}

export async function updateMember(db: SupabaseClient, id: string, patch: Partial<MemberInput>): Promise<Member> {
  if (patch.start_date || patch.end_date) {
    // The date-range rule must hold for the merged record.
    const existing = await getMember(db, id);
    const dateError = validateMembershipDates(
      patch.start_date ?? existing.start_date,
      patch.end_date ?? existing.end_date,
    );
    if (dateError) {
      throw new ApiError(400, "VALIDATION_ERROR", "Invalid member data", {
        end_date: dateError,
      });
    }
  }
  const { data, error } = await db
    .from("members")
    .update(patch)
    .eq("id", id)
    .select()
    .maybeSingle();
  if (error) throw fromDbError(error, "member");
  if (!data) throw notFound();
  return withStatus(data as MemberRow);
}

export async function deleteMember(db: SupabaseClient, id: string): Promise<void> {
  const { data, error } = await db.from("members").delete().eq("id", id).select("id");
  if (error) throw fromDbError(error, "member");
  if (!data || data.length === 0) throw notFound();
}
