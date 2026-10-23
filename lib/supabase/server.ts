import { createClient, type SupabaseClient } from "@supabase/supabase-js";

function config() {
  const url = process.env.NEXT_PUBLIC_SUPABASE_URL;
  const key = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY;
  if (!url || !key) {
    throw new Error(
      "Missing NEXT_PUBLIC_SUPABASE_URL or NEXT_PUBLIC_SUPABASE_ANON_KEY (see .env.example)",
    );
  }
  return { url, key };
}

/**
 * Server-side Supabase client. With an access token, requests run AS that
 * user, so the Row Level Security policies in supabase/schema.sql apply.
 * Without one, requests run as the anonymous role (which has no table access).
 * A service-role key is never used or exposed.
 */
export function getSupabase(accessToken?: string): SupabaseClient {
  const { url, key } = config();
  return createClient(url, key, {
    auth: { persistSession: false, autoRefreshToken: false },
    global: accessToken ? { headers: { Authorization: `Bearer ${accessToken}` } } : undefined,
  });
}
