import { ApiError, fromDbError } from "@/lib/http";
import type { SupabaseClient } from "@supabase/supabase-js";
import type { TrainerInput } from "@/lib/validation/trainer";

export type Trainer = TrainerInput & { id: string; created_at: string };

const notFound = () => new ApiError(404, "NOT_FOUND", "Trainer not found");

export async function listTrainers(db: SupabaseClient): Promise<Trainer[]> {
  const { data, error } = await db
    .from("trainers")
    .select("*")
    .order("created_at", { ascending: false });
  if (error) throw fromDbError(error, "trainer");
  return data as Trainer[];
}

export async function getTrainer(db: SupabaseClient, id: string): Promise<Trainer> {
  const { data, error } = await db.from("trainers").select("*").eq("id", id).maybeSingle();
  if (error) throw fromDbError(error, "trainer");
  if (!data) throw notFound();
  return data as Trainer;
}

export async function createTrainer(db: SupabaseClient, input: TrainerInput): Promise<Trainer> {
  const { data, error } = await db.from("trainers").insert(input).select().single();
  if (error) throw fromDbError(error, "trainer");
  return data as Trainer;
}

export async function updateTrainer(db: SupabaseClient, id: string, input: TrainerInput): Promise<Trainer> {
  const { data, error } = await db
    .from("trainers")
    .update(input)
    .eq("id", id)
    .select()
    .maybeSingle();
  if (error) throw fromDbError(error, "trainer");
  if (!data) throw notFound();
  return data as Trainer;
}

export async function deleteTrainer(db: SupabaseClient, id: string): Promise<void> {
  const { data, error } = await db.from("trainers").delete().eq("id", id).select("id");
  if (error) throw fromDbError(error, "trainer");
  if (!data || data.length === 0) throw notFound();
}
