# WebDriver testing and report completion plan

## Objective

Implement 30 meaningful Selenium WebDriver tests in visible Chrome and add
their actual execution evidence to the supplied STQA Word report. The current
five smoke tests are preliminary coverage, not the finished WebDriver section.
Repeated login setup and simple label checks will not inflate the test count.

## Test coverage

| Area | Cases | Workflows and assertions |
|---|---:|---|
| Authentication | 3 | Empty credentials, invalid credentials, logout revokes access |
| Dashboard and navigation | 3 | Dashboard reflects known test data; sidebar navigation; plan names, duration and prices |
| Members | 14 | Valid creation; persistence after reload; required fields; invalid email; invalid phone; reversed dates; case-insensitive duplicate email; name search; email search; no matches; details; edit and reload; cancel deletion; confirm deletion |
| Trainers | 4 | Valid creation; required name and specialization; invalid contact details; confirmed deletion |
| Users | 2 | User list and protected own-role control; create-user required-field validation |
| Trainer permissions | 2 | Members are readable without write controls; restricted direct URLs are denied |
| Customer permissions | 2 | Only the customer's membership is shown; restricted direct URLs are denied |
| Total | 30 | Browser-driven functional, negative, persistence and access-control checks |

## Implementation order

1. Stabilize browser execution. Complete a small representative run that includes
   creating, editing and deleting a member before expanding the suite. Keep
   report rendering outside the app folder to avoid locked Word temporary files.
2. Create reusable page helpers for forms, navigation, tables and confirmation
   dialogs. Wait for hydrated forms and loaded data using fresh element lookups;
   handle stale elements through explicit waits instead of fixed sleeps.
3. Organize tests by role. Log in once for each role's group and reuse its
   browser session. Login validation and logout cases use a separate session
   so they cannot disrupt functional tests. Use one browser worker.
4. Give each test independent, uniquely named data. Record every created member
   and trainer ID and clean up only those records. API calls may prepare or
   clean fixtures; the feature under test must be exercised through Chrome.
5. Run all 30 tests, investigate unexpected failures, fix relevant defects, and
   rerun affected tests before the final complete regression run. Keep failed
   execution evidence and distinguish it from the final retest.
6. Save per-case expected/actual results, screenshots, console output, browser
   and driver versions, execution time, and cleanup status. Do not insert
   artificial failing assertions into this WebDriver suite.
7. Complete the report cover, explain WebDriver architecture and setup, add
   the 30-case table, genuine screenshots, results, defects and retests, limits,
   and conclusion. Preserve the teammate's earlier work as historical evidence;
   do not add overlapping or repeated runs to the final test count.
8. Render the completed DOCX using a separate hidden Word instance, update the
   contents and page numbers, inspect every page, and deliver the final Word
   report. Keep the supplied original unchanged.

## Existing evidence and scope

- Five preliminary Chrome smoke tests passed.
- The latest Python discovery run has 98 unit/API passes and five intentional
  browser skips; the browser tests were run separately.
- Lint and TypeScript checks passed after the current changes.
- The legacy 100-case IDE run is incomplete. It has 16 deliberate failed
  assertions and runner/setup problems, so it is not the finished WebDriver
  result and must not be represented as a passing complete run.
- Firefox remains blocked because the saved Windows dump identifies a
  Selenium-launched Firefox process at the kernel crash trigger. Chrome is
  the authorized workaround; the Windows defect is not repaired.

## Submission details

- Niel Mandhare, BC238
- Swanand Mukhavar, BC240
- Shruti Kokil, BC230
- Guide: Prof. Jagruti Wagh
- Marathwada Mitra Mandal's College of Engineering

Submission means preparing the completed report locally. Uploading it to a
college portal or sending it to someone requires the user's explicit request.
