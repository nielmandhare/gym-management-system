# STQA project handoff

Last updated: 2026-10-23, Asia/Calcutta. Read this before running anything.

For installing on another Windows PC and reproducing the Chrome demo, start
with [SETUP.md](SETUP.md). This handoff records machine-specific history and
verified results; the setup guide gives fresh-machine commands.

The current Next.js project is published on the `stqa-nextjs-chrome-transfer`
branch of `nielmandhare/gym-management-system`; `main` remains the earlier
Java/web project. Clone the transfer branch on a new PC, then follow `SETUP.md`.

## Completed outcome

The user-approved 30-case native Selenium WebDriver suite is implemented and
PASSED ALL 30 in visible Chrome: 0 failures, errors or skips; 380.028 seconds.
All 17 owned fixture cleanup entries are clean. Only four successful UI logins
were needed: admin twice (including the dedicated logout test), trainer once,
customer once. The user specifically rejected login-heavy test inflation.

The final college report is complete and visually verified: 47 pages, updated
contents/page numbers, 30 case rows with actual results, eight genuine final-run
browser screenshots, defects/retests, limitations, conclusion and supplied team
details. Section 16 starts on page 37. The teammate's earlier work is retained
as historical results with separate counts. The supplied original is unchanged.
No college upload, email or other submission has been made.

## Deliverables and evidence

- Final report: `docs/submission/Gym_Management_System_STQA_Final_Report.docx`
- Supplied original: `D:\stqa\Gym_Management_System_STQA_Report_Selenium_IDE.docx`
- Project: `D:\stqa\gymmanagement`
- Approved plan: `docs/webdriver-completion-plan.md`
- Final exact evidence: `test-results/webdriver/chrome/20261023-011232-full/results.json`
  and `results.txt`; 34 genuine PNGs are in the same run folder.
- `test-results/webdriver/chrome/latest-full.json` points to that completed run.
  Focused attempts have a separate latest pointer and never replace it.
- Separate Python discovery evidence: `test-results/full-test-suite.txt`,
  2026-10-23 00:39 IST: 103 entries, 98 passes and 5 browser skips, no failures.
  The passes are 64 unit, 12 integration and 22 black-box API cases. Those five
  preliminary browser cases were subsequently replaced by the 30-case suite;
  future discovery counts will differ. Skips are never passes.
- Lint and TypeScript checks passed after the functional changes.
- No current production-build result is claimed from the historical report.
- Report QA manifest: `D:\stqa\report-work\final-qa.json` includes output SHA256,
  unchanged-original check, 47-page coverage and 30 passing case-row checks.
- Internal QA: `D:\stqa\report-work\final-v4.pdf` and `final-pages-v4` PNGs.
  Deliver DOCX only unless the user also requests PDF.

## Run locally

Use PowerShell from `D:\stqa\gymmanagement`. Dependencies and credentials are
already configured. Do not create new accounts or reapply database setup merely
to rerun the suite. Check server health before starting another server.

```powershell
npm.cmd run dev:local
# In another terminal, with the app ready:
npm.cmd run test:webdriver
# Focused member create/view/edit/reload/delete pilot:
.\.venv\Scripts\python.exe scripts/run_webdriver.py --browser chrome --match WD07
# Example focused selection:
.\.venv\Scripts\python.exe scripts/run_webdriver.py --browser chrome --match WD02,WD14,WD25
```

App: `http://localhost:3000`, bound to 127.0.0.1. HTTP 200 verified again during
final report work around 08:00 IST. The clean dev server was started hidden at
00:58 (launcher PID 13540); inspect current processes rather than trusting an old
PID. Logs: `test-results/dev-server-clean.stdout.log` and `.stderr.log`.
The VS Code project/interpreter/tasks are configured. Its Chrome task now says
30 functional cases. Use `npm.cmd` to avoid PowerShell npm.ps1 forwarding issues.

Installed: Node 22.23.2, Python 3.11.15 in `.venv`, Selenium 4.51.0,
Chrome 134.0.6998.89, ChromeDriver 134.0.6998.165 cached under the user's Selenium
cache. Browser requirements: `requirements-test.txt`. Credentials stay in
`.env.local`; never print passwords or copy them into a report or handoff.
The legacy IDE file also contains test credentials; do not publish it casually.

## Native suite design and coverage

- `tests/selenium/test_webdriver.py`: authentication 3; dashboard/navigation/plans
  3; members 14; trainers 4; users 2; trainer permissions 2; customer permissions 2.
- Member cases cover UI creation, stored reload values, required/email/phone/date
  validation, case-insensitive duplicates, name/email/no-result searches, exact
  detail identity, edited phone/plan persistence, cancel and confirmed deletion.
- Trainer cases create, validate and delete through the UI. Users cases verify
  own-role protection and create-user validation without leaving a permanent
  authentication account. Trainer/customer cases check restricted direct URLs,
  read-only controls and membership isolation.
- `tests/selenium/browser_helpers.py`: one browser session/login per functional
  role group, fresh visible-element lookups and stale-tolerant explicit waits.
  Authentication cases are isolated. No repeated login before every case.
- API helpers prepare fixtures, clean up exact owned records and check selected
  persistence outcomes; the feature under test is driven through Chrome.
  Ownership is registered by ID or exact unique email before creating via UI.
  Existing records and account roles are preserved.
- Text input uses Selenium send_keys. Segmented date inputs use an ISO value
  through their native DOM setter plus input/change events; actual form submit
  performs validation. This detail is disclosed in the report.
- `scripts/run_webdriver.py`: Chrome default, Firefox hold, --match IDs,
  timestamped run folders, authentic flushed stdout/stderr.
- `scripts/execute_webdriver_suite.py`: unittest evidence result with expected,
  actual, status, timing, screenshots, versions, login counts and cleanup.
  Final JSON is written only when execution completes.
- Plain discovery skips the browser suite unless RUN_SELENIUM_TESTS=1.

## Repairs and preserved attempts

- Repaired three zero-filled interrupted-write files: package.json,
  .vscode/tasks.json, docs/local-testing-setup.md. Damaged backups preserved.
- Auth submit waits for hydration; PWA install button uses a hydration guard.
- Next development indicator moved bottom-right after intercepting Logout.
- API test timeout 20 to 60 seconds accommodates cold compilation; cleanup
  errors retain owned IDs. Full separate Python rerun passed.
- Tailwind excludes test-results through @source not in app/globals.css.
- Word export inside the app caused a Turbopack CSS panic on a locked temporary
  non-source file. Document work moved OUTSIDE the app. Prior generated .next
  was moved to `D:\stqa\report-work\next-cache-before-clean-restart`; a clean
  dev restart restored hydration. Do not render reports inside the app folder.
- Pilot `20261023-005606-focused` failed class setup: zero cases executed.
- The 00:58 WD07 pilot created/edited correctly but waited for retained hidden
  phone inputs to disappear. Helpers now select visible controls. Pilot
  `20261023-010133-focused` PASSED the entire lifecycle (51.109 seconds).
- First full run `20261023-010237-full`: 30 executed, 27 pass, 2 failures, 1 error;
  owned cleanup clean. WD02 expected the wrong invalid-login wording; WD14 read
  a transient search loading state; WD25 compared CSS capitalization literally.
  Corrected the expectations/waits while retaining all functional assertions.
- Focused `20261023-011103-focused`: WD02/WD14/WD25 all PASS, 50.619 seconds.
- Final `20261023-011232-full`: 30/30 PASS. Preserve earlier attempts honestly;
  do not add repeated runs to the final 30-case count. No further rerun is needed
  unless code/environment changes or a new concern justifies it.

## Windows crash restriction

Two genuine BSODs occurred October 22 around 23:31 and 23:38. Both are registry
rename kernel crashes, 0x3B / C0000005. Offline Microsoft DbgEng analysis of the
larger dump identifies a Selenium-launched Firefox child in the Geckodriver
process tree at the second crash's failing NtRenameKey call. The first dump
cannot establish the triggering program with the same precision.

Windows 11 Pro 22H2 build 22621.4317 remains unsupported and its update scan
offered no software updates. The underlying OS issue is NOT repaired. Firefox
must remain blocked. The user explicitly authorized Chrome as the workaround.
Do not disable antivirus, browser sandboxing or security protections. Execution
policy was not changed. Last boot remains 2026-10-22 23:38:18, checked again
around 08:00 IST on October 23: no additional restart was observed.

Firefox wrappers honor ignored `selenium/local-browser-status.json`. Preserve
that hold. Private crash dumps, downloaded symbols and analysis tools are under
ignored `test-results/crash-diagnostics`; never upload them. See
`docs/recovery-status.md`. Earlier source scan found no reboot/registry code.

## Legacy Selenium IDE scope

`selenium/gym-management.side` currently has 100 cases, including 16 intentional
failure demonstrations. Its recent full CLI run is INCOMPLETE. The historical
supplied report describes an earlier 67-case stage. Do not label the current
100-case suite complete, claim 84/100 or treat empty JSON files as results.

`scripts/run_selenium.mjs` defaults to Chrome, guards Firefox, uses one worker
and the aggregate filter, resolves a shared loopback ChromeDriver service.
Three regexpi text expectations were adapted to exact displayed strings and
form-readiness waits added. Selenium IDE's editable wait still rejects stale
hydrating nodes. All legacy runners were stopped. User approved completing the
native functional suite instead of spending more time on repeated IDE logins.

## Report authoring and continuation

Team: Niel Mandhare BC238; Swanand Mukhavar BC240; Shruti Kokil BC230.
Guide: Prof. Jagruti Wagh. College: Marathwada Mitra Mandal's College of Engineering.

Builder: `D:\stqa\report-work\build_final_report.py`. It reads latest-full.json,
requires 30 actual passes and clean cleanup, preserves historical results,
fills cover/team, adds a real TOC and Section 16 with exact evidence. Original
SHA256: 6008de59e220a16f98a8a0bdd3d7c1e793f7007d5b6beaaf8f180904ae7bf872.

The documents skill was read and announced; its artifact-operation marker ran
successfully EXACTLY ONCE (edit, one DOCX). Do NOT run the marker again for this
request. The canonical render_docx.py was attempted, but bundled LibreOffice
was unavailable. Isolated hidden Word 2013 COM export worked instead. Renderer:
`scripts/render_report_word.ps1 -Finalize` updates fields/TOC, repaginates,
saves the final copy, exports a PDF and closes its own instance. Invoke via an
inline scriptblock if execution policy blocks .ps1; do not change system policy.

Use the authoritative bundled document runtime:
`C:\Users\owner\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe`.
Available: python-docx, pypdf, PIL, pdf2image; fitz is absent. Node is under the
same dependency root at node/bin/node.exe. Poppler is at
native/poppler/Library/bin/pdftoppm.exe (pdfinfo.exe also available).
Do not use desktop LibreOffice. Render outside the app; inspect every page
again after any DOCX change. `audit_final_report.py` checks source preservation,
placeholders, 30 passing case rows, 13 images and visual-review coverage.
Final changed page PNGs were reviewed directly; unchanged page bodies were
verified pixel-identical to already reviewed versions. Two nearly empty pages
were removed without removing historical result data. Final total: 47 pages.

## Repository and task boundaries

Read AGENTS.md and installed Next.js docs before app edits. The working tree
contains extensive pre-existing user changes: never reset/revert them. Do not
spawn agents absent explicit authorization. No commit, portal upload or sending
the report to someone was requested. The requested implementation, execution,
report and handoff work is complete.

Next.js feedback instructions were fetched once. A single anonymized review
draft for the meaningful locked-temp-file Turbopack detour is prepared in the
final reporting pass. It is a review form only: never submit it for the user.
No further feedback drafts or duplicate form tabs should be created for this
request after that final pass.
