import type { NextRequest } from "next/server";
import { requireAuth } from "@/lib/auth/session";
import { ApiError, assertUuid, handle, ok, readJson } from "@/lib/http";
import { changeRole } from "@/lib/services/users";
import { validateRoleChange } from "@/lib/validation/auth";

export async function PATCH(request: NextRequest, ctx: RouteContext<"/api/users/[id]">) {
  return handle(async () => {
    const { db, user } = await requireAuth("users:manage");
    const { id } = await ctx.params;
    assertUuid(id);
    const result = validateRoleChange(await readJson(request));
    if (!result.valid) {
      throw new ApiError(400, "VALIDATION_ERROR", "Invalid role", result.errors);
    }
    // Prevents the last admin from locking everyone out by demoting themself.
    if (id === user.id) {
      throw new ApiError(400, "VALIDATION_ERROR", "You cannot change your own role", {
        role: "You cannot change your own role",
      });
    }
    return ok(await changeRole(db, id, result.value.role));
  });
}
