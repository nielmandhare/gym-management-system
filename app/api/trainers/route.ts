import type { NextRequest } from "next/server";
import { requireAuth } from "@/lib/auth/session";
import { ApiError, handle, ok, readJson } from "@/lib/http";
import { createTrainer, listTrainers } from "@/lib/services/trainers";
import { validateTrainerInput } from "@/lib/validation/trainer";

export async function GET() {
  return handle(async () => {
    const { db } = await requireAuth("trainers:read");
    return ok(await listTrainers(db));
  });
}

export async function POST(request: NextRequest) {
  return handle(async () => {
    const { db } = await requireAuth("trainers:write");
    const result = validateTrainerInput(await readJson(request));
    if (!result.valid) {
      throw new ApiError(400, "VALIDATION_ERROR", "Invalid trainer data", result.errors);
    }
    return ok(await createTrainer(db, result.value), 201);
  });
}
