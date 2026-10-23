import { unstable_rethrow } from "next/navigation";
import type { FieldErrors } from "@/lib/validation/member";

/** An error that is safe to show to API clients. */
export class ApiError extends Error {
  constructor(
    public status: number,
    public code: string,
    message: string,
    public fields?: FieldErrors,
  ) {
    super(message);
  }
}

const UUID_RE =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function assertUuid(id: string): void {
  if (!UUID_RE.test(id)) {
    throw new ApiError(400, "INVALID_ID", "ID must be a valid UUID");
  }
}

export async function readJson(request: Request): Promise<unknown> {
  try {
    return await request.json();
  } catch {
    throw new ApiError(400, "INVALID_JSON", "Request body must be valid JSON");
  }
}

export function ok(data: unknown, status = 200): Response {
  return Response.json({ data }, { status });
}

/** Runs a handler and converts any thrown error into a safe JSON response. */
export async function handle(fn: () => Promise<Response>): Promise<Response> {
  try {
    return await fn();
  } catch (err) {
    if (err instanceof ApiError) {
      return Response.json(
        { error: { code: err.code, message: err.message, fields: err.fields } },
        { status: err.status },
      );
    }
    unstable_rethrow(err); // let Next.js internal signals (e.g. prerender abort) through
    console.error("Unexpected API error:", err);
    return Response.json(
      { error: { code: "INTERNAL_ERROR", message: "Something went wrong on the server" } },
      { status: 500 },
    );
  }
}

/** Maps a Supabase/Postgres error to a safe ApiError (never leaks details). */
export function fromDbError(error: { code?: string; message?: string }, entity: string): ApiError {
  if (error.code === "23505") {
    return new ApiError(409, "DUPLICATE_EMAIL", `A ${entity} with this email already exists`, {
      email: "This email is already registered",
    });
  }
  if (error.code === "23514" || error.code === "22007" || error.code === "22008") {
    return new ApiError(400, "VALIDATION_ERROR", `The ${entity} data violates a database rule`);
  }
  console.error(`Database error (${entity}):`, error.code, error.message);
  return new ApiError(500, "INTERNAL_ERROR", "Something went wrong on the server");
}
