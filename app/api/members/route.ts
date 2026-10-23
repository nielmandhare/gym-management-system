import type { NextRequest } from "next/server";
import { requireAuth } from "@/lib/auth/session";
import { ApiError, handle, ok, readJson } from "@/lib/http";
import { createMember, listMembers } from "@/lib/services/members";
import { validateMemberInput } from "@/lib/validation/member";

export async function GET(request: NextRequest) {
  return handle(async () => {
    const { db } = await requireAuth("members:read");
    const q = request.nextUrl.searchParams.get("q") ?? "";
    return ok(await listMembers(db, q));
  });
}

export async function POST(request: NextRequest) {
  return handle(async () => {
    const { db } = await requireAuth("members:write");
    const result = validateMemberInput(await readJson(request));
    if (!result.valid) {
      throw new ApiError(400, "VALIDATION_ERROR", "Invalid member data", result.errors);
    }
    return ok(await createMember(db, result.value), 201);
  });
}
