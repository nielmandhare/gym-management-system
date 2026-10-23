-- Gym Management System schema (run in Supabase Dashboard > SQL Editor).
-- Safe to re-run: uses IF NOT EXISTS / DROP POLICY IF EXISTS.

create extension if not exists pgcrypto;

-- ---------------------------------------------------------------- members
create table if not exists public.members (
  id              uuid primary key default gen_random_uuid(),
  full_name       text not null,
  email           text not null,
  phone           text not null,
  membership_plan text not null,
  start_date      date not null,
  end_date        date not null,
  created_at      timestamptz not null default now(),

  constraint members_full_name_not_blank check (length(btrim(full_name)) > 0),
  constraint members_email_not_blank     check (length(btrim(email)) > 0),
  constraint members_phone_ten_digits    check (phone ~ '^[0-9]{10}$'),
  constraint members_plan_allowed        check (membership_plan in ('Monthly', 'Quarterly', 'Yearly')),
  constraint members_dates_ordered       check (end_date >= start_date)
);

-- Case-insensitive unique email.
create unique index if not exists members_email_lower_key
  on public.members (lower(email));
-- Supports name search / ordering.
create index if not exists members_full_name_lower_idx
  on public.members (lower(full_name));
create index if not exists members_created_at_idx
  on public.members (created_at desc);

-- --------------------------------------------------------------- trainers
create table if not exists public.trainers (
  id              uuid primary key default gen_random_uuid(),
  full_name       text not null,
  email           text not null,
  phone           text not null,
  specialization  text not null,
  created_at      timestamptz not null default now(),

  constraint trainers_full_name_not_blank      check (length(btrim(full_name)) > 0),
  constraint trainers_email_not_blank          check (length(btrim(email)) > 0),
  constraint trainers_phone_ten_digits         check (phone ~ '^[0-9]{10}$'),
  constraint trainers_specialization_not_blank check (length(btrim(specialization)) > 0)
);

create unique index if not exists trainers_email_lower_key
  on public.trainers (lower(email));

-- ---------------------------------------------------------------- profiles
-- One row per Supabase Auth user. The role lives here, NOT in user metadata,
-- because metadata can be set by the user at sign-up.
create table if not exists public.profiles (
  id         uuid primary key references auth.users (id) on delete cascade,
  email      text not null,
  full_name  text,
  role       text not null default 'customer',
  created_at timestamptz not null default now(),
  constraint profiles_role_allowed check (role in ('admin', 'trainer', 'customer'))
);

-- Every new auth user becomes a 'customer'. Only an admin can change a role.
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  insert into public.profiles (id, email, full_name, role)
  values (new.id, lower(new.email), new.raw_user_meta_data ->> 'full_name', 'customer');
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- Accounts created before this trigger existed get a profile too.
insert into public.profiles (id, email, full_name, role)
select id, lower(email), raw_user_meta_data ->> 'full_name', 'customer'
from auth.users
on conflict (id) do nothing;

-- Role of the calling user (security definer avoids RLS recursion on profiles).
create or replace function public.current_user_role()
returns text
language sql
stable
security definer
set search_path = ''
as $$
  select role from public.profiles where id = auth.uid()
$$;

-- ------------------------------------------------- Row Level Security (RLS)
-- ACCESS STRATEGY:
--   * Users sign in with Supabase Auth (email + password).
--   * The Next.js API sends each request to Supabase with THAT user's access
--     token, so the policies below apply to the logged-in user. The anon role
--     has NO access to these tables.
--   * admin    : full access to members, trainers and profiles
--   * trainer  : read members and trainers
--   * customer : read only their own member row (matched by email)
--   * Only the public anon key is used; no service-role key exists anywhere.
alter table public.members  enable row level security;
alter table public.trainers enable row level security;
alter table public.profiles enable row level security;

-- Remove the policies of the earlier demo version (open to anon).
drop policy if exists "demo_anon_all_members"  on public.members;
drop policy if exists "demo_anon_all_trainers" on public.trainers;

drop policy if exists "members_admin_all" on public.members;
create policy "members_admin_all" on public.members
  for all to authenticated
  using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');

drop policy if exists "members_trainer_read" on public.members;
create policy "members_trainer_read" on public.members
  for select to authenticated
  using (public.current_user_role() = 'trainer');

drop policy if exists "members_customer_read_own" on public.members;
create policy "members_customer_read_own" on public.members
  for select to authenticated
  using (
    public.current_user_role() = 'customer'
    and lower(email) = lower(auth.jwt() ->> 'email')
  );

drop policy if exists "trainers_admin_all" on public.trainers;
create policy "trainers_admin_all" on public.trainers
  for all to authenticated
  using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');

drop policy if exists "trainers_trainer_read" on public.trainers;
create policy "trainers_trainer_read" on public.trainers
  for select to authenticated
  using (public.current_user_role() = 'trainer');

drop policy if exists "profiles_read_own_or_admin" on public.profiles;
create policy "profiles_read_own_or_admin" on public.profiles
  for select to authenticated
  using (id = auth.uid() or public.current_user_role() = 'admin');

drop policy if exists "profiles_admin_update" on public.profiles;
create policy "profiles_admin_update" on public.profiles
  for update to authenticated
  using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');

-- Make sure anon cannot touch the data tables at all.
revoke all on public.members, public.trainers, public.profiles from anon;

-- ------------------------------------------------------- first admin account
-- 1. Register normally in the app (Register page), then
-- 2. run this once in the SQL Editor with your own email:
--
--    update public.profiles set role = 'admin' where email = 'you@example.com';
--
-- Also: Supabase Dashboard > Authentication > Providers > Email > turn OFF
-- "Confirm email" for this demo, otherwise new accounts cannot log in until
-- they click an email link.
