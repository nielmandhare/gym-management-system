import type { NextRequest } from "next/server";
import { requireAuth } from "@/lib/auth/session";
import { ApiError, assertUuid, handle, ok, readJson } from "@/lib/http";
import { deleteMember, getMember, updateMember } from "@/lib/services/members";
import { validateMemberPatch } from "@/lib/validation/member";

export async function GET(_req: NextRequest, ctx: RouteContext<"/api/members/[id]">) {
  return handle(async () => {
    const { db } = await requireAuth("members:read");
    const { id } = await ctx.params;
    assertUuid(id);
    return ok(await getMember(db, id));
  });
}

export async function PATCH(request: NextRequest, ctx: RouteContext<"/api/members/[id]">) {
  return handle(async () => {
    const { db } = await requireAuth("members:write");
    const { id } = await ctx.params;
    assertUuid(id);
    const result = validateMemberPatch(await readJson(request));
    if (!result.valid) {
      throw new ApiError(400, "VALIDATION_ERROR", "Invalid member data", result.errors);
    }
    return ok(await updateMember(db, id, result.value));
  });
}

export async function DELETE(_req: NextRequest, ctx: RouteContext<"/api/members/[id]">) {
  return handle(async () => {
    const { db } = await requireAuth("members:write");
    const { id } = await ctx.params;
    assertUuid(id);
    await deleteMember(db, id);
    return ok({ id });
  });
}
