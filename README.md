# Gym Management System (STQA Mini Project)

For the current testing/report work and recovery context, read
[STQA handoff](HANDOFF.md) and the
[WebDriver completion plan](docs/webdriver-completion-plan.md).
For a fresh Windows installation, Chrome-only browser setup, Supabase test
accounts, and a classroom walkthrough, follow [SETUP.md](SETUP.md).

A small Next.js + Supabase CRUD application for managing gym members and trainers, built to demonstrate software testing with Python PyUnit (`unittest`): unit, white-box, black-box, integration and regression testing.

Pages: Login, Register, Dashboard, Members (search, add, view, edit, delete), Trainers (list, add, delete), Users (admin), My Membership / My Profile, Plans.

## Roles

| Role | Can do | Pages |
|---|---|---|
| **admin** | Everything: manage members, trainers and user accounts/roles | Dashboard, Members, Trainers, Users, My Profile, Plans |
| **trainer** | View dashboard, members and own trainer record (read-only) | Dashboard, Members (view only), My Profile, Plans |
| **customer** | View only their own membership | My Membership, Plans |

Accounts are matched to gym records **by email**: a customer sees the member record with the same email as their login, a trainer sees the trainer record with the same email.

## 1. Install dependencies

Requirements: Node.js 20+, Python 3, and Chrome for browser tests. API/unit tests use the Python standard library; browser tests need `requirements-test.txt`.

```bash
npm install
```

## 2. Configure Supabase

1. Create a Supabase project. Use a **separate project for testing**: the integration and black-box tests write to it.
2. Open **SQL Editor**, paste the contents of `supabase/schema.sql`, and run it. It creates the `members` and `trainers` tables, constraints, indexes and Row Level Security policies. It can be re-run safely.
3. In **Authentication > Providers > Email**, turn **off "Confirm email"** (otherwise new accounts cannot log in until they click an email link).
4. Copy `.env.example` to `.env.local` and fill in **Project Settings > API**:

```text
NEXT_PUBLIC_SUPABASE_URL=https://your-project-ref.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=your-anon-public-key
```

Never commit `.env.local` (it is git-ignored) and never use a service-role key in this project.

### Demo logins live in `.env.local`

`.env.local` also holds the demo accounts (`TEST_ADMIN_EMAIL` / `TEST_ADMIN_PASSWORD`, and the trainer and customer equivalents). The Python tests read them from there. With the app running and the schema applied, create all three accounts with:

```bash
python scripts/create_test_users.py
```

then run the printed `update public.profiles set role = ...` lines once in the SQL Editor.

### Create the first admin (manual alternative)

1. Run the app (`npm run dev`), open `/register` and create an account. Every self-registered account is a **customer**.
2. In the Supabase SQL Editor run (with your own email):

```sql
update public.profiles set role = 'admin' where email = 'you@example.com';
```

3. Log out and log in again. From the **Users** page the admin can create trainer/admin/customer accounts and change roles.

### Security design

- **Login:** Supabase Auth (email + password). Passwords are handled by Supabase, never stored by this app.
- **Session:** the access and refresh tokens are kept in `httpOnly` cookies (not readable by page scripts). `proxy.ts` refreshes an expired access token and redirects visitors without a session to `/login`.
- **Three layers of access control:** (1) the proxy redirect and the UI hide pages by role (convenience only); (2) every API route calls `requireAuth(action)` and returns `401` (not logged in) or `403` (role not allowed); (3) Row Level Security in the database applies per user, because the API sends the *logged-in user's* token to Supabase. The `anon` role has no table access.
- **Roles** live in `public.profiles`, assigned `customer` by a database trigger. Users cannot choose their own role; only an admin can change it, and an admin cannot change their own role.
- Only the public anon key is used. There is no service-role key.
- Limitations: no password reset or email verification flow, no rate limiting beyond Supabase's own, and cookie-based sessions rely on `SameSite=Lax` for CSRF protection. This is suitable for a college demo, not a production system.

## 3. Run the app

```bash
npm run dev      # http://localhost:3000
```

Checks:

```bash
npm run lint
npm run typecheck
npm run build
```

### Install as an app (PWA)

The app is an installable Progressive Web App (manifest, icons, service worker, offline page). Browsers only offer installation on **HTTPS or `localhost`**, so it works on `http://localhost:3000` and on any HTTPS deployment.

- **Chrome / Edge (desktop, Android):** an **Install app** button appears in the sidebar and on the login page, and the browser also shows an install icon in the address bar. Click either one.
- **iPhone / iPad (Safari):** the button explains the steps: Share, then "Add to Home Screen".
- Once installed, the button disappears. To check the setup, open DevTools > Application > Manifest / Service Workers.
- The service worker caches only the offline page and the icons. Pages and `/api` responses are never cached, because they hold role-specific user data.
- Regenerate the icons (needs `pip install pillow`) with `python scripts/make_icons.py`.

## 4. Run the tests

All commands run from the repository root.

### Chrome Selenium setup (Windows / VS Code)

See [local setup and testing plan](docs/local-testing-setup.md) for installation,
VS Code tasks, and saved results. With the app running:

```powershell
.\.venv\Scripts\python.exe -m pip install -r requirements-test.txt
npm run test:webdriver  # 30 native Python WebDriver functional tests
npm run test:selenium   # existing 100-case Selenium IDE suite, visible Chrome
```

The existing IDE suite includes 16 deliberately failing demonstrations labelled
`INTENTIONAL FAIL`. Their failures are expected and are not passing tests.
Firefox is locally blocked after the confirmed Windows kernel crash trigger;
see [recovery status](docs/recovery-status.md). Chrome is the default workaround.

### Unit tests (no server or database needed)

```bash
python -m unittest discover -s tests/unit -v
python -m unittest discover -s tests -p "test_unit_*.py" -v
```

### Integration and black-box tests (need a running server + test database)

These are **skipped** unless you opt in. In one terminal run `npm run dev` (with `.env.local` pointing to your test project). In another terminal:

PowerShell:

```powershell
$env:RUN_INTEGRATION_TESTS = "1"
python -m unittest discover -s tests -p "test_integration_*.py" -v
python -m unittest discover -s tests -p "test_blackbox_*.py" -v
```

Bash:

```bash
RUN_INTEGRATION_TESTS=1 python -m unittest discover -s tests -p "test_integration_*.py" -v
RUN_INTEGRATION_TESTS=1 python -m unittest discover -s tests -p "test_blackbox_*.py" -v
```

The API is login-protected, so the HTTP tests need an **admin test account** (create it as described above, in your test project):

```powershell
$env:TEST_ADMIN_EMAIL = "admin@gymtest.example"
$env:TEST_ADMIN_PASSWORD = "your-test-password"
# optional: enables the role-restriction tests (otherwise those tests are skipped)
$env:TEST_TRAINER_EMAIL = "trainer@gymtest.example";   $env:TEST_TRAINER_PASSWORD = "..."
$env:TEST_CUSTOMER_EMAIL = "customer@gymtest.example"; $env:TEST_CUSTOMER_PASSWORD = "..."
```

Passwords are read from the environment only; they are never written to files or printed.

Optional variables: `TEST_BASE_URL` (default `http://localhost:3000`; non-local URLs also need `ALLOW_REMOTE_TEST_SERVER=1`).

The tests create members with `@gymtest.example` emails and delete only the records they created. If cleanup fails the test fails and prints the IDs to remove by hand.

### Whole suite

```bash
python -m unittest discover -s tests -p "test_*.py" -v
```

### What each kind of test does

- **Unit / white-box** (`tests/unit/`): test a *Python reference implementation* of the documented rules (`tests/reference_rules.py`). PyUnit does **not** import or run the TypeScript code.
- **Integration and black-box** (`tests/integration/`, `tests/blackbox/`): send real HTTP requests to the running Next.js API, so these are what exercise the actual application.
- A skipped suite is reported as skipped / Not Run, never as passed.

## 5. Save test output to files

```bash
python scripts/run_tests.py unit          # test-results/unit-tests.txt
python scripts/run_tests.py integration   # test-results/integration-tests.txt
python scripts/run_tests.py blackbox      # test-results/blackbox-tests.txt
python scripts/run_tests.py all           # test-results/full-test-suite.txt (one combined run)
```

Each file has a header (date, command, exit code, counts) followed by the real, unmodified console output. `test-results/summary.json` keeps the latest result per suite. The runner exits non-zero if a suite fails. The black-box run also writes `test-results/blackbox-cases.json` (input / expected / actual / status per case).

## 6. Capture screenshots

Run each command in the VS Code terminal and take a real screenshot. Save to `screenshots/`:

| File | What to capture |
|---|---|
| `01-dashboard.png` | Dashboard page with data |
| `02-members.png` | Members list |
| `03-add-member-validation.png` | Add Member form showing validation messages |
| `04-unit-tests.png` | Unit test command output |
| `05-blackbox-tests.png` | Black-box test command output |
| `06-integration-tests.png` | Integration test command output |
| `07-supabase-records.png` | Table Editor showing members |

## 7. Generate the report with Pandoc

Fill in the placeholders at the top of `miniprojectreport.md`, add your screenshots, then:

```bash
pandoc miniprojectreport.md -o miniprojectreport.docx --toc
pandoc miniprojectreport.md -o miniprojectreport.pdf --toc
```

DOCX needs only Pandoc. Direct PDF output needs a PDF engine such as LaTeX (MiKTeX or TeX Live); if you do not have one, create the DOCX and use Word's "Save as PDF". Pandoc does not run Python or touch the database.

## 8. Project layout

```text
app/                 pages and API routes (auth, users, me, members, trainers)
proxy.ts             session refresh and login redirect
components/          layout, dashboard, members, trainers, ui
lib/auth/            roles/permissions (pure) and session handling
lib/business/        pure membership rules
lib/validation/      pure input validation
lib/services/        Supabase data access
lib/supabase/        server-side Supabase client
supabase/schema.sql  database schema, constraints, RLS
tests/               unit, integration, blackbox + shared helpers
scripts/run_tests.py runner that saves results
docs/test-plan.md    test plan
miniprojectreport.md project report
```

## Business rules

- Phone: exactly 10 digits. Email: valid format, unique (case-insensitive). Name: not blank.
- Plans: Monthly (1 month), Quarterly (3 months), Yearly (12 months); prices on the Plans page are demonstration values.
- Membership status is **calculated** from the end date, never stored: `Active` if `end_date >= today`, otherwise `Expired`. A membership whose end date is today is still Active.

## API

| Method | Path | Success | Errors |
|---|---|---|---|
| POST | `/api/auth/login`, `/api/auth/register`, `/api/auth/logout` | 200 / 201 / 200 | 400, 401, 403, 409 |
| GET | `/api/auth/me`, `/api/me` | 200 | 401 |
| GET / POST | `/api/users` (admin) | 200 / 201 | 400, 401, 403, 409 |
| PATCH | `/api/users/:id` (admin) | 200 | 400, 401, 403, 404 |
| GET | `/api/members?q=` (admin, trainer) | 200 | 401, 403, 500 |
| POST | `/api/members` (admin) | 201 | 400, 401, 403, 409 |
| GET (admin, trainer); PATCH, DELETE (admin) | `/api/members/:id` | 200 | 400, 401, 403, 404, 409 |
| GET / POST | `/api/trainers` (read: admin, trainer; write: admin) | 200 / 201 | 400, 401, 403, 409 |
| GET / PATCH / DELETE | `/api/trainers/:id` | 200 | 400, 401, 403, 404, 409 |

Responses: `{ "data": ... }` on success, `{ "error": { "code", "message", "fields?" } }` on failure.
