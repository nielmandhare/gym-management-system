# Local development and Chrome testing

## Plan and setup

1. Verify crash evidence and block Firefox on this PC.
2. Use the existing Node dependencies, Python `.venv`, Chrome and VS Code.
3. Start the app, prove the member lifecycle pilot, then run the 30-case native
   WebDriver suite with one browser per role group.
4. Save actual results and screenshots, and check lint and TypeScript.

Open this folder in VS Code. Credentials stay in `.env.local`; never publish it.
Start the app using Terminal > Run Task, or:

```powershell
npm.cmd run dev:local
```

Open http://localhost:3000. In another terminal:

```powershell
npm.cmd run test:webdriver
npm.cmd run test:selenium
```

Both commands use visible Chrome. Selenium Manager obtains the matching
ChromeDriver automatically. Tests use temporary browser profiles. VS Code tasks
provide the same commands. For a fresh environment:

```powershell
npm.cmd ci
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements-test.txt
```

## Browser tests and saved evidence

The native Python suite has 30 cases covering authentication, dashboard data,
navigation, plans, member creation/validation/search/details/edit/deletion,
trainer creation/validation/deletion, user controls and role permissions. It
reads test accounts from `.env.local`, reuses one login per functional role
group, and saves actual output, per-case JSON and screenshots in timestamped
folders under `test-results/webdriver/chrome/`. `latest-full.json` points to
the most recent completed full run; inspect its status before claiming a pass.
Optional settings: `CHROME_BINARY` and
`SELENIUM_HEADLESS=1`. Normal unittest discovery skips browser tests unless
`RUN_SELENIUM_TESTS=1`; the wrapper sets this flag.

The existing Selenium IDE file has 100 cases, including 16 labelled
`INTENTIONAL FAIL`. Three unsupported regex text expectations were adapted to
literal display text, and readiness waits were added. The runner selects
`All 100 tests` once, uses one worker, and saves JSON results and failure
screenshots in `test-results/selenium/chrome/`. A nonzero exit is expected for
the deliberate failures; inspect the report for any additional failures.
Its recent full run is incomplete and must not be counted as a passing
100-case run. The native WebDriver suite is the current submission work.

For a focused native retest (comma-separated IDs are accepted):

```powershell
.\.venv\Scripts\python.exe scripts/run_webdriver.py --browser chrome --match WD07
.\.venv\Scripts\python.exe scripts/run_webdriver.py --browser chrome --match WD02,WD14,WD25
```

API and IDE CRUD tests write to the configured Supabase project; use a test
database. Logs, screenshots and the IDE file can contain account details.
Keep them private. Latest run details are in the saved reports; interrupted
or skipped runs do not establish a pass.

## Firefox hold and Windows status

The two 2026-10-22 crashes share a Windows registry-rename kernel stack. The
larger dump identifies a Selenium-launched Firefox child as the initiating
process for the second crash. Windows 11 Pro 22H2 build 22621.4317 is out of
support; its update scan offered no software updates. Chrome is an authorized
workaround and does not repair the Windows defect.

`selenium/local-browser-status.json` blocks the project's Firefox wrappers and
native suite before browser creation. Keep this local hold in place until the
OS issue is addressed. Do not run the IDE runner directly with Firefox.
See [recovery evidence](recovery-status.md). Damaged project files were restored;
the originals remain under `test-results/recovery/`.

References: [Chrome WebDriver](https://www.selenium.dev/documentation/webdriver/browsers/chrome/)
and [Selenium IDE runner](https://www.selenium.dev/selenium-ide/docs/en/introduction/command-line-runner).
