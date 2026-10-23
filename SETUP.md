# Setup and Chrome testing guide

This guide prepares the Gym Management System on a Windows PC for local testing and a classroom demonstration. Run commands from the project folder in PowerShell. Use Google Chrome for all browser automation described here.

## Requirements

- Windows 10 or 11, with current Google Chrome and Visual Studio Code installed.
- Node.js 20.9 or newer (Node 22 LTS is recommended), Python 3.11, and Git.
- Internet access to install packages and reach Supabase.
- A dedicated Supabase project for testing. Do not point tests at production data.

Check the installed tools:

```powershell
node --version
npm.cmd --version
py -3.11 --version
git --version
```

## Get the project and install dependencies

Clone the repository or copy its project folder to the PC. Open PowerShell in the folder containing `package.json` (the `gymmanagement` folder), then run:

```powershell
npm.cmd ci
py -3.11 -m venv .venv
.\.venv\Scripts\python.exe -m pip install --upgrade pip
.\.venv\Scripts\python.exe -m pip install -r requirements-test.txt
```

Open the same folder in VS Code:

```powershell
code .
```

If VS Code asks for a Python interpreter, choose `.venv\Scripts\python.exe`.

## Configure a test database and demo accounts

1. Create a new Supabase project for this demo. Keep its URL, anon/public key, and test database separate from any production project.
2. In that project's SQL Editor, run the complete contents of `supabase/schema.sql`.
3. In Supabase Authentication settings, turn off email confirmation for the test project so newly created demo accounts can log in.
4. Copy `.env.example` to `.env.local` and fill in the values below. Keep `.env.local` private and out of Git. Never use a Supabase service-role key.

```powershell
Copy-Item .env.example .env.local
notepad .env.local
```

Set `NEXT_PUBLIC_SUPABASE_URL` and `NEXT_PUBLIC_SUPABASE_ANON_KEY` using the test project's API settings. Set `TEST_ADMIN_EMAIL` / `TEST_ADMIN_PASSWORD`, `TEST_TRAINER_EMAIL` / `TEST_TRAINER_PASSWORD`, and `TEST_CUSTOMER_EMAIL` / `TEST_CUSTOMER_PASSWORD` to three test accounts you control. Use strong, unique passwords. Keep `TEST_BASE_URL=http://localhost:3000`.

Start the app in one PowerShell terminal:

```powershell
npm.cmd run dev:local
```

Wait for the ready message, then in a second terminal create the demo accounts:

```powershell
.\.venv\Scripts\python.exe scripts/create_test_users.py
```

The script reads account emails/passwords from `.env.local`; it does not print passwords. It creates missing accounts and prints SQL statements to promote the admin and trainer profiles. Copy those printed `UPDATE public.profiles ...` statements into the **same test project's** SQL Editor and run them. The customer stays a customer. If an account already exists, the script reports that; confirm its role in the test database rather than creating duplicates. If email confirmation was enabled during registration, disable it, remove only the unconfirmed test account, and repeat registration.

Open `http://localhost:3000` in Chrome and sign in with the demo account to confirm the app is ready. The three accounts support the admin, trainer, and customer browser groups.

## Run the Chrome WebDriver suite

Keep the development server running. In another terminal, from the project root, run the full native Chrome suite:

```powershell
npm.cmd run test:webdriver
```

This suite drives the visible Google Chrome browser and runs 30 functional cases across authentication, dashboard and plans, member workflows, trainers, user controls, and trainer/customer permissions. It reuses a session within each role group instead of signing in before every case. Selenium Manager selects or downloads a matching ChromeDriver when needed; keep Chrome installed and allow Selenium Manager network access on a fresh machine.

For a focused check, pass selected case IDs:

```powershell
.\.venv\Scripts\python.exe scripts/run_webdriver.py --browser chrome --match WD07
.\.venv\Scripts\python.exe scripts/run_webdriver.py --browser chrome --match WD02,WD14,WD25
```

The complete run saves its real console output, structured per-case results, cleanup status, and screenshots in a timestamped directory under `test-results/webdriver/chrome/`. Read that run's `results.json` and `results.txt` before reporting an outcome. A focused run is not the full-suite result. Do not count skipped cases as passes.

Other useful checks:

```powershell
npm.cmd run lint
npm.cmd run typecheck
```

HTTP integration tests also write test records to Supabase. Run them only after checking that `.env.local` points to your dedicated test project and the local app is running. The optional suite is guarded by `RUN_INTEGRATION_TESTS=1`:

```powershell
$env:RUN_INTEGRATION_TESTS = "1"
.\.venv\Scripts\python.exe -m unittest discover -s tests -p "test_integration_*.py" -v
Remove-Item Env:RUN_INTEGRATION_TESTS
```

The tests create uniquely marked records and remove records they own. After interrupted runs, inspect the reported cleanup status and test database before manually removing anything. Never delete unrelated records.

## Classroom demonstration

Start the local server with `npm.cmd run dev:local`, open `http://localhost:3000` in Chrome, and sign in with the demo admin account. A concise walkthrough is:

1. Show the dashboard totals, recent members, and the membership plans and prices.
2. Open Members, create a test member, view its details, edit a value, reload to show persistence, then delete it through the confirmation dialog.
3. Show member-form validation by trying a blank form or malformed email, then cancel without saving.
4. Create and delete a demo trainer through the Trainers page.
5. Sign in as the trainer to show the read-only member view and denied management pages; sign in as the customer to show only the linked membership and denied management access.
6. Run `npm.cmd run test:webdriver` if time permits. Show the final `results.txt` and a few screenshots from the same completed timestamped run.

Use test-only records and accounts. Avoid showing `.env.local`, passwords, Supabase keys, browser developer tools with session data, or private crash diagnostics on the projector. Clean up any extra demo records that you create.

## Browser safety and the earlier restart problem

**Use Chrome for this project on the affected Windows PC. Do not launch Firefox or run any Firefox Selenium command.** The repository has Firefox guardrails; keep them in place.

Earlier in this project, I ran Selenium with Firefox while diagnosing browser automation on this PC. Windows then had two blue-screen restarts. The saved dump review found a Selenium-launched Firefox child in the process tree at the failing Windows registry operation during the second crash. That evidence does not prove Firefox was the sole root cause, but it is enough to keep Firefox blocked on this machine. Windows 11 Pro 22H2 on that PC also remains unsupported and its update scan offered no updates. Chrome completed the project's 30-case run successfully; this does not repair Windows or establish that Firefox is safe on another PC.

Install and use Chrome, and run the commands in this guide with `--browser chrome`. Do not disable antivirus, browser sandboxing, or other security protections to make Firefox run. If a different PC restarts or shows a blue screen, stop browser tests and preserve its crash details before continuing.

## Project reference files

- `README.md`: application overview, roles, architecture, and general commands.
- `HANDOFF.md`: current verified results, detailed continuation context, evidence paths, and crash history.
- `docs/webdriver-completion-plan.md`: approved coverage plan and case distribution.
- `docs/local-testing-setup.md`: VS Code and local testing notes.
- `test-results/webdriver/chrome/latest-full.json`: pointer to the last completed full native WebDriver run on the machine that generated it. On a fresh PC, run the suite to produce local evidence.
- `Gym_Management_System_STQA_Final_Report.docx` in `D:\stqa` on the original PC: completed report from the recorded run. The project folder itself may not include that file when copied elsewhere.
