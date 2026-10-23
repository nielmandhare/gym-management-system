import type { NextRequest } from "next/server";
import { requireAuth } from "@/lib/auth/session";
import { ApiError, assertUuid, handle, ok, readJson } from "@/lib/http";
import { deleteTrainer, getTrainer, updateTrainer } from "@/lib/services/trainers";
import { validateTrainerInput } from "@/lib/validation/trainer";

export async function GET(_req: NextRequest, ctx: RouteContext<"/api/trainers/[id]">) {
  return handle(async () => {
    const { db } = await requireAuth("trainers:read");
    const { id } = await ctx.params;
    assertUuid(id);
    return ok(await getTrainer(db, id));
  });
}

// Trainers are small, so an update replaces all editable fields.
export async function PATCH(request: NextRequest, ctx: RouteContext<"/api/trainers/[id]">) {
  return handle(async () => {
    const { db } = await requireAuth("trainers:write");
    const { id } = await ctx.params;
    assertUuid(id);
    const result = validateTrainerInput(await readJson(request));
    if (!result.valid) {
      throw new ApiError(400, "VALIDATION_ERROR", "Invalid trainer data", result.errors);
    }
    return ok(await updateTrainer(db, id, result.value));
  });
}

export async function DELETE(_req: NextRequest, ctx: RouteContext<"/api/trainers/[id]">) {
  return handle(async () => {
    const { db } = await requireAuth("trainers:write");
    const { id } = await ctx.params;
    assertUuid(id);
    await deleteTrainer(db, id);
    return ok({ id });
  });
}
