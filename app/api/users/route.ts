import type { NextRequest } from "next/server";
import { requireAuth } from "@/lib/auth/session";
import { ApiError, handle, ok, readJson } from "@/lib/http";
import { createUser, listProfiles } from "@/lib/services/users";
import { validateCreateUser } from "@/lib/validation/auth";

export async function GET() {
  return handle(async () => {
    const { db } = await requireAuth("users:manage");
    return ok(await listProfiles(db));
  });
}

export async function POST(request: NextRequest) {
  return handle(async () => {
    const { db } = await requireAuth("users:manage");
    const result = validateCreateUser(await readJson(request));
    if (!result.valid) {
      throw new ApiError(400, "VALIDATION_ERROR", "Invalid user data", result.errors);
    }
    return ok(await createUser(db, result.value), 201);
  });
}
