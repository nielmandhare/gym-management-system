# Test Plan: Gym Management System

## 1. Project Overview

The Gym Management System is a small web application for a single gym administrator. It manages members and trainers (create, view, edit, delete), shows membership plans, and shows a dashboard of counts derived from the database.

| Layer | Technology |
|---|---|
| Frontend and API | Next.js (App Router), TypeScript, Tailwind CSS |
| Database | Supabase PostgreSQL (`supabase/schema.sql`) |
| Tests | Python `unittest` (PyUnit), standard library only |

Business rules live in small pure functions (`lib/business/membership.ts`, `lib/validation/member.ts`), and the REST API lives in `app/api/**/route.ts`.

## 2. Testing Objectives

1. Verify that each documented business rule behaves correctly in isolation (unit testing).
2. Verify that internal conditions and branches of those rules are exercised on both outcomes (white-box testing).
3. Verify that the API, validation and database work together for the member workflow (integration testing).
4. Verify externally visible behaviour of the API without relying on implementation details (black-box testing).
5. Identify, record and retest defects, and re-run tests after changes (regression testing).
6. Produce genuine, reproducible execution evidence.

## 3. Scope of Testing

In scope: the member business rules, member input validation, the Members REST API (`/api/members`, `/api/members/:id`), and the Supabase constraints that back them (unique email, phone format, date order).

## 4. Features to Be Tested

| Feature | Test level |
|---|---|
| Membership status from dates (Active/Expired, boundary) | Unit, white-box |
| Phone, email, name, plan, date validation | Unit, white-box |
| Create / read / update / delete member via API | Integration, black-box |
| Rejection of invalid or duplicate input via API | Integration, black-box |
| Not-found and malformed-ID handling | Black-box |
| Member search by name or email | Integration (IT-09) |
| Role permission matrix (admin / trainer / customer) | Unit, white-box |
| Password and registration validation | Unit, white-box |
| Login, logout, session, 401 for anonymous requests | Integration, black-box |
| 403 for roles that lack permission | Black-box (needs trainer and customer test accounts) |
| UI behaviour (forms, validation messages, delete confirmation) | Manual (checklist in the report) |

## 5. Features Excluded From Testing

- Password reset, email verification and rate limiting (not implemented).
- Supabase Auth internals (token signing, password hashing).
- Trainer API and trainer UI: kept deliberately small; only exercised manually, with no automated tests.
- Performance, load and security (penetration) testing.
- Cross-browser compatibility and accessibility audits.
- Supabase platform behaviour itself.

## 6. Testing Strategy

| Category | Approach | Files |
|---|---|---|
| Unit | Python reference implementation of the written rules (including the role permission matrix), tested with `unittest.TestCase`, fixed dates | `tests/unit/` |
| White-box | Unit tests chosen so that each true/false branch of each rule is hit; mapping in the report | `tests/unit/` |
| Integration | Python `urllib` calls to the real running Next.js API backed by a test Supabase project | `tests/integration/` |
| Black-box | Python `urllib` calls checking only status code and error code | `tests/blackbox/` |
| Regression | Re-run the same suites after any change to validation or API code | `scripts/run_tests.py` |

**Important:** PyUnit cannot import TypeScript. The unit tests exercise a separate Python implementation of the rules (`tests/reference_rules.py`), written from the specification. The HTTP tests are what exercise the real TypeScript application. Agreement between the two is checked only indirectly, through the HTTP tests.

Branch-oriented tests are not a coverage measurement. No coverage tool has been run, so no coverage percentage is claimed.

## 7. Test Environment

| Item | Value |
|---|---|
| OS | Windows 11 |
| Python | 3.12 |
| Node.js | 22 |
| Next.js | 16.4.2 |
| Database | A dedicated Supabase test project (never production data) |
| Test accounts | An admin account (required), plus optional trainer and customer accounts, created in the test project |
| Server | `npm run dev` or `npm run build && npm start` on `http://localhost:3000` |

HTTP tests are skipped unless `RUN_INTEGRATION_TESTS=1` is set, and they refuse non-local servers unless `ALLOW_REMOTE_TEST_SERVER=1` is also set.

## 8. Testing Tools

| Tool | Use |
|---|---|
| `unittest` | Test framework and runner |
| `urllib.request` | HTTP client for integration and black-box tests |
| `scripts/run_tests.py` | Runs suites and saves real output to `test-results/` |
| ESLint, `tsc`, `next build` | Static checks of the application code |
| Pandoc | Converts the report to DOCX or PDF |

## 9. Test Data

- Unit tests use fixed, deterministic dates (current date fixed to `2025-06-15`) and fixed strings.
- HTTP tests generate a unique email per run (`pyunit.<label>.<random>@gymtest.example`).
- Test accounts are created by the student in the test project; the tests never create or delete accounts.
- HTTP test members use 2030 dates so their status is `Active` for the duration of the project.
- Tests create and delete only their own records. No arbitrary records are deleted.

## 10. Roles and Responsibilities

| Role | Responsibility |
|---|---|
| Student / developer | Implement, write and run tests, capture screenshots |
| Student / tester | Execute manual UI checks, record defects |
| Guide / examiner | Review the report and evidence |

(Replace with actual names in the report.)

## 11. Risks and Mitigation

| Risk | Mitigation |
|---|---|
| Python reference rules drift from the TypeScript rules | HTTP tests check the real behaviour; the rules are documented in one place |
| Tests write to a real database | Opt-in flag, local-server guard, dedicated test project, own-record cleanup |
| Cleanup fails and leaves test records | `tearDown` fails the test and prints the IDs to remove manually |
| Test accounts missing | Admin credentials are required (suite skips otherwise); role tests skip individually and are reported as skipped |
| Passwords in the environment | Read from environment variables only, never written to files or printed |
| Clock-dependent status | Unit tests use a fixed current date |

## 12. Test Entry Criteria

- Application builds (`npm run build`) and lints without errors.
- `supabase/schema.sql` has been run in the test project.
- `.env.local` points to the test project and the server is running (HTTP tests only).
- `TEST_ADMIN_EMAIL` / `TEST_ADMIN_PASSWORD` are set (and trainer / customer accounts for the role tests).
- Python is available.

## 13. Test Exit Criteria

- All planned test cases are executed, or explicitly marked Not Run with a reason.
- No unresolved critical or high severity defects.
- Evidence (`test-results/*.txt`) exists for every claimed result.

## 14. Test Pass/Fail Criteria

A test passes when the observed result equals the expected result. It fails if an assertion fails, and errors if the test itself raises an unexpected exception. A skipped test is neither passed nor failed and is reported as skipped or Not Run.

Project acceptance criteria:

- All critical business rules pass their tests.
- All planned test cases are executed or explicitly marked Not Run with reasons.
- No unresolved critical defects remain.
- The member CRUD workflow works in the configured environment.
- Invalid requests are rejected.
- Database changes are persisted and can be retrieved.
- The final report reflects real test execution results.

These are criteria to be evaluated, not a statement that they have been met.

## 15. Schedule and Deliverables

| Phase | Deliverable |
|---|---|
| Plan | This document |
| Design tests | Test cases in `tests/` and the report tables |
| Execute | `test-results/*.txt`, `test-results/summary.json`, `test-results/blackbox-cases.json` |
| Report | `miniprojectreport.md` converted to DOCX/PDF |
| Evidence | Screenshots in `screenshots/` |

Dates are to be filled in by the student.
