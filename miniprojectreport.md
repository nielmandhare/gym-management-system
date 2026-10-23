---
title: "Testing of Gym Management System Using PyUnit"
subtitle: "STQA Mini Project Report"
---

# 1. Cover Page

**STQA Mini Project Report**

**Title: Testing of Gym Management System Using PyUnit**

| Item | Details |
|---|---|
| Student name | [STUDENT NAME] |
| Roll number | [ROLL NUMBER] |
| Class | [CLASS / DIVISION] |
| Department | [DEPARTMENT] |
| College | [COLLEGE NAME] |
| Academic year | [ACADEMIC YEAR] |
| Guide | [GUIDE NAME] |
| Submission date | [DATE] |

> Items in square brackets are placeholders. Replace them before submission.

# 2. Abstract

This mini project develops a small Gym Management System and tests it using Software Testing and Quality Assurance (STQA) techniques. The application is built with Next.js, TypeScript and Tailwind CSS, and stores data in a Supabase PostgreSQL database. It supports member and trainer management, membership plans and a dashboard, with login and three roles (admin, trainer and customer) that see different pages. Testing uses Python's built-in `unittest` framework (PyUnit). Unit tests and white-box tests check the business rules in isolation. Integration and black-box tests send HTTP requests to the running application. Defects found during development were recorded, fixed and retested.

This report describes the design, the test plan, the test cases and the results. Only results from real executions are reported. At the time of writing, all 98 automated tests (64 unit, 12 integration and 22 black-box) were executed and passed. The 34 HTTP-level tests (12 integration and 22 black-box) were also executed against a running application and a Supabase project and all passed. The 12 manual user-interface checks were not recorded and are reported as **Not Run**.

# 3. Problem Definition

Small gyms often keep member records on paper or in spreadsheets. This causes duplicate entries, invalid phone numbers and emails, wrong membership dates and difficulty in finding expired memberships. A simple web application with validated input can reduce these problems. Such an application needs systematic testing to show that its rules are enforced correctly and that changes do not break existing behaviour.

# 4. Objectives

1. Build a simple Gym Management System with member and trainer CRUD operations.
2. Enforce input validation on both the server and the database.
3. Apply black-box, white-box, unit, integration and regression testing.
4. Use PyUnit (`unittest`) to automate the tests and produce readable execution reports.
5. Record defects, fix them and retest.
6. Prepare a report containing only genuine test evidence.

# 5. Scope of the Project

**In scope:** login and role-based access (admin, trainer, customer), dashboard, member management, trainer management, user-account management, membership plans, REST API, database schema, automated tests for the member business rules, the role permissions and the member and authentication API.

**Out of scope:** password reset, email verification, payments, attendance tracking, notifications, automated user-interface testing (no Selenium), performance and security testing.

# 6. Prerequisites

- Basic knowledge of web applications, REST APIs and SQL.
- Basic knowledge of Python and the `unittest` module.
- A free Supabase account (a separate project for testing is recommended).
- Node.js and Python installed.

# 7. Software and Hardware Requirements

| Item | Requirement |
|---|---|
| Processor | Any modern dual-core processor |
| Memory | 4 GB RAM or more |
| Disk space | About 1 GB (mostly `node_modules`) |
| Operating system | Windows 10/11, Linux or macOS (developed on Windows 11) |
| Node.js | Version 20 or later (22 used) |
| Python | Python 3 (3.12 used); no extra packages |
| Browser | Any current browser |
| Network | Internet access for Supabase |
| Editor | VS Code |

# 8. Technology Stack

| Layer | Technology |
|---|---|
| Frontend framework | Next.js 16.4.2 (App Router) |
| Language | TypeScript |
| Styling | Tailwind CSS 4 |
| Database | Supabase PostgreSQL |
| Database client | `@supabase/supabase-js` |
| Testing | Python `unittest` (PyUnit), `urllib.request` |
| Documentation | Markdown, converted with Pandoc |

# 9. Introduction to the Gym Management System

The system is a single-administrator dashboard with four sections.

- **Dashboard:** total members, active members, expired memberships, total trainers, five most recent members and a quick action to add a member. All values come from the database.
- **Members:** list, search by name or email, add, view details, edit, and delete after confirmation.
- **Trainers:** list, add and delete.
- **Plans:** Monthly, Quarterly and Yearly plans with example prices in INR (Rs. 1000, Rs. 2700 and Rs. 9600). These are demonstration values, not real market prices.

**Login and roles.** Users log in with an email and password (Supabase Auth). Each account has one role, and each role has its own pages.

| Role | Permissions | Pages |
|---|---|---|
| Admin | Manage members, trainers, user accounts and roles | Dashboard, Members, Trainers, Users, My Profile, Plans |
| Trainer | Read dashboard, members and own trainer record (no changes) | Dashboard, Members (view only), My Profile, Plans |
| Customer | Read only their own membership | My Membership, Plans |

Anyone can register, and a new account is always a *customer*. Only an admin can change a role. A customer or trainer account is linked to the gym record that has the same email address. The first admin is promoted with one SQL statement (see Appendix C).

The interface includes loading indicators, empty states, validation messages, success notifications, error messages, delete confirmation and a layout that adapts to small screens.

**Business rule for membership status.** Status is not stored. It is calculated from the end date: a membership is *Active* if `end_date >= today` and *Expired* otherwise. A membership whose end date is today is therefore still Active. This keeps the status consistent with the date and avoids stale data.

# 10. System Architecture

```text
Browser (React pages)
      |  fetch JSON
      v
Next.js API routes  (app/api/**/route.ts)
      |  validate input (lib/validation), apply rules (lib/business)
      v
Service layer       (lib/services)
      |  supabase-js (parameterized queries)
      v
Supabase PostgreSQL (tables, constraints, RLS)
```

- The browser never calls Supabase directly. Only the server-side API routes use the Supabase client with the public anon key.
- After login, the session tokens are stored in `httpOnly` cookies. `proxy.ts` redirects visitors without a session to the login page and refreshes an expired access token.
- Every protected API route calls `requireAuth(action)`, which returns 401 (not logged in) or 403 (role not allowed).
- The API sends the logged-in user's own token to Supabase, so the database also enforces access per user.
- Validation is performed on the server, regardless of the validation done in the form.
- Business rules are small pure functions so that they can be tested independently.
- Errors are converted into safe JSON messages. Stack traces and database details are not sent to clients.

**Access strategy (three layers).**

1. *Pages:* the proxy and the navigation hide pages that a role cannot use. This is a convenience only.
2. *API:* every route checks the role with `requireAuth` (401 or 403).
3. *Database:* Row Level Security policies apply to the logged-in user (admin: all; trainer: read members and trainers; customer: read only the member row with their own email). The anonymous role has no access to the tables.

Roles are stored in the `profiles` table and set to *customer* by a database trigger, so a user cannot choose their own role at sign-up. Only the public anon key is used; there is no service-role key. The design is suitable for a college demonstration, not for production (no password reset, no email verification, no rate limiting of its own).

**Testing architecture.** PyUnit cannot import TypeScript. Therefore:

- *Unit and white-box tests* run against a separate Python implementation of the written rules (`tests/reference_rules.py`).
- *Integration and black-box tests* send HTTP requests to the real Next.js application.

The Python reference implementation and the TypeScript application are two implementations of the same specification. They are not the same code.

# 11. Functional Requirements

| ID | Requirement |
|---|---|
| FR-1 | The system shall list members and support search by name or email. |
| FR-2 | The system shall create a member with name, email, phone, plan, start date and end date. |
| FR-3 | The system shall show details of one member. |
| FR-4 | The system shall update a member's details. |
| FR-5 | The system shall delete a member after confirmation. |
| FR-6 | The system shall reject invalid names, emails, phone numbers, plans and date ranges. |
| FR-7 | The system shall reject a duplicate email (case-insensitive). |
| FR-8 | The system shall calculate membership status from the end date. |
| FR-9 | The system shall list, add and delete trainers. |
| FR-10 | The system shall display the three membership plans. |
| FR-11 | The dashboard shall show counts derived from stored records. |
| FR-12 | Users shall register (as customers), log in and log out. |
| FR-13 | The system shall allow only the permitted roles to use each page and API operation (admin, trainer, customer). |
| FR-14 | An admin shall create accounts and change roles, but not change their own role. |
| FR-15 | A customer shall see only their own membership and a trainer only their own trainer record. |

# 12. Non-Functional Requirements

| ID | Requirement |
|---|---|
| NFR-1 | Usability: simple navigation, clear messages, responsive layout. |
| NFR-2 | Reliability: errors are handled and shown without crashing the page. |
| NFR-3 | Data integrity: constraints in the database back up server validation. |
| NFR-4 | Security: no secrets in source control; no service-role key; passwords handled only by Supabase Auth; session tokens in `httpOnly` cookies; no internal error details in responses; the same message for unknown email and wrong password. |
| NFR-5 | Maintainability: business logic is separated into small, testable functions. |
| NFR-6 | Testability: tests run with the standard library only and are repeatable. |

# 13. Database Design

## 13.1 Members table

| Column | Type | Rules |
|---|---|---|
| `id` | UUID | Primary key, generated automatically |
| `full_name` | text | Required, not blank |
| `email` | text | Required, unique ignoring case |
| `phone` | text | Required, exactly 10 digits |
| `membership_plan` | text | Monthly, Quarterly or Yearly |
| `start_date` | date | Required |
| `end_date` | date | Required, not earlier than `start_date` |
| `created_at` | timestamptz | Defaults to current time |

## 13.2 Trainers table

| Column | Type | Rules |
|---|---|---|
| `id` | UUID | Primary key, generated automatically |
| `full_name` | text | Required, not blank |
| `email` | text | Required, unique ignoring case |
| `phone` | text | Required, exactly 10 digits |
| `specialization` | text | Required, not blank |
| `created_at` | timestamptz | Defaults to current time |

## 13.3 Profiles table

| Column | Type | Rules |
|---|---|---|
| `id` | UUID | Primary key, references the Supabase Auth user |
| `email` | text | Copied from the account, stored in lower case |
| `full_name` | text | Optional |
| `role` | text | `admin`, `trainer` or `customer` (default `customer`) |
| `created_at` | timestamptz | Defaults to current time |

A database trigger creates the profile when an account is created and always assigns the role `customer`.

## 13.4 Constraints

| Constraint | Rule |
|---|---|
| `members_full_name_not_blank` | Name must not be blank |
| `members_phone_ten_digits` | Phone matches `^[0-9]{10}$` |
| `members_plan_allowed` | Plan in (Monthly, Quarterly, Yearly) |
| `members_dates_ordered` | `end_date >= start_date` |
| `members_email_lower_key` (unique index) | Unique `lower(email)` |
| `trainers_*` | Equivalent rules for trainers |

Indexes are created on `lower(full_name)` (search) and `created_at` (recent members). The full definition, including Row Level Security, is in `supabase/schema.sql`.

## 13.5 Data flow

1. The user logs in; the session is stored in `httpOnly` cookies.
2. The user fills in a form; the browser validates it for quick feedback.
3. The browser sends JSON to an API route.
4. The route checks the session and role (`401` or `403`), then validates the body again on the server and returns `400` with field messages if invalid.
5. The service layer sends a parameterized query through `supabase-js`, using the user's token so that Row Level Security applies.
6. PostgreSQL enforces constraints. A duplicate email returns `409`; a missing record returns `404`.
7. The route returns `{ "data": ... }` or `{ "error": { "code", "message", "fields" } }`.

# 14. Testing Tool: PyUnit (`unittest`)

PyUnit is the `unittest` module in Python's standard library. Main elements used:

| Element | Use in this project |
|---|---|
| `unittest.TestCase` | One class per subject (status, dates, plan, phone, email, name, member input) |
| `assert*` methods | Compare actual and expected values |
| `assertRaises` | Check that invalid input raises an error |
| `subTest` | Report each value in a small loop separately |
| `setUpClass` / `SkipTest` | Skip HTTP tests safely when the environment is not configured |
| `tearDown` | Delete test records created by the test |
| Discovery (`discover -s ... -p ...`) | Finds test files by pattern |
| `-v` | Prints one line per test, which gives readable evidence |

Test method names contain a test ID, for example `test_UT03_end_date_equal_to_today_is_active_boundary`.

# 15. Types of Testing

## 15.1 Black-box testing

The tester sends input and observes the output without using knowledge of the code. Here, the black-box tests send HTTP requests to the API and check the status code and error code (`tests/blackbox/`).

## 15.2 White-box testing

The tester designs tests from the internal structure. Here, unit tests are chosen so that both outcomes (true and false) of each internal condition are exercised. This is *branch-oriented* testing. No coverage tool was run, so **no coverage percentage is claimed**.

## 15.3 Unit testing

Each business rule is tested in isolation, with fixed data and a fixed current date so that results never depend on the real clock.

## 15.4 Integration testing

Tests the interaction of several components: API routes, server validation, Supabase and JSON serialization (`tests/integration/`).

## 15.5 Regression testing

After any change to validation or API code, the existing tests are run again to confirm that earlier behaviour still works. No separate framework is used; the same suites are re-run (`python scripts/run_tests.py all`).

## 15.6 How the categories relate

| Category | Question answered |
|---|---|
| Black-box | Does the system behave correctly as seen from outside? |
| White-box | Were the internal conditions and branches exercised? |
| Unit | Does each rule work alone? |
| Integration | Do the connected components work together? |

One test may give evidence for more than one category (for example, a unit test can also be a white-box test). In Section 19 each automated test is counted **once**.

# 16. Test Plan

The full plan is in `docs/test-plan.md`. A summary follows.

## 16.1 Test objectives

Verify business rules, validation, member CRUD through the API, database persistence, error handling, and regression safety.

## 16.2 Features tested

Membership status calculation; phone, email, name, plan and date validation; member create, read, update, delete and list through the API; duplicate, invalid and non-existent record handling.

## 16.3 Features not tested

Trainer API (manual only), password reset and email verification (not implemented), performance, load, penetration testing, accessibility, cross-browser compatibility. Token signing and password hashing are Supabase's responsibility and are not tested.

## 16.4 Test strategy

Unit and white-box tests on a Python reference implementation; integration and black-box tests over HTTP on the real application; manual checks for the interface; regression by re-running suites.

## 16.5 Test environment

Windows 11, Python 3.12, Node.js 22, Next.js 16.4.2, Supabase test project, server at `http://localhost:3000`.

## 16.6 Test schedule

| Activity | Planned date |
|---|---|
| Test planning | [DATE] |
| Unit and white-box testing | [DATE] |
| Integration and black-box testing | [DATE] |
| Defect fixing and regression | [DATE] |
| Report | [DATE] |

## 16.7 Entry and exit criteria

*Entry:* application builds and lints; schema applied; `.env.local` set to a test project; server running (HTTP tests).

*Exit:* all planned tests executed or marked Not Run with a reason; no unresolved critical defects; evidence files exist for every reported result.

## 16.8 Risks and mitigation

| Risk | Mitigation |
|---|---|
| Python and TypeScript rules differ | HTTP tests check the real behaviour |
| Tests write to a real database | Opt-in flag, local-server guard, test project, own-record cleanup |
| Cleanup fails | `tearDown` fails loudly and lists the IDs |
| No authentication | Documented as a limitation |

# 17. Test Scenarios and Test Cases

In every table below, "Expected" is the planned behaviour and "Actual" is what was observed in a real execution. Where a test has not been executed, Actual is **Not Run** and Status is **Not Run**.

## 17.1 Black-box Testing

Automated tests in `tests/blackbox/test_blackbox_members_api.py`. The suite needs a running server, `RUN_INTEGRATION_TESTS=1` and an admin test account. BB-19 to BB-22 also need trainer and customer test accounts; without them those four tests are skipped individually. The suite was executed (22 ran, 22 passed, none skipped) against the application running locally and the student's Supabase project. The Actual column is copied from `test-results/blackbox-cases.json`, which the test run wrote; the run's console output is in `test-results/blackbox-tests.txt`.

| Test ID | Scenario | Input | Steps | Expected result | Actual result | Status |
|---|---|---|---|---|---|---|
| BB-01 | Create member with valid data | All six fields valid | POST `/api/members` | HTTP 201 with the new member | HTTP 201 | Passed |
| BB-02 | Empty full name | `full_name = ""` | POST `/api/members` | HTTP 400, `VALIDATION_ERROR`, field `full_name` | HTTP 400, code=VALIDATION_ERROR | Passed |
| BB-03 | Invalid email | `email = "not-an-email"` | POST `/api/members` | HTTP 400, `VALIDATION_ERROR`, field `email` | HTTP 400, code=VALIDATION_ERROR | Passed |
| BB-04 | Invalid phone | `phone = "98ab"` | POST `/api/members` | HTTP 400, `VALIDATION_ERROR`, field `phone` | HTTP 400, code=VALIDATION_ERROR | Passed |
| BB-05 | Missing required fields | `{}` | POST `/api/members` | HTTP 400, six field errors | HTTP 400, code=VALIDATION_ERROR | Passed |
| BB-06 | Invalid membership plan | `membership_plan = "Weekly"` | POST `/api/members` | HTTP 400, field `membership_plan` | HTTP 400, code=VALIDATION_ERROR | Passed |
| BB-07 | End date earlier than start date | start 2030-05-01, end 2030-04-01 | POST `/api/members` | HTTP 400, `VALIDATION_ERROR` | HTTP 400, code=VALIDATION_ERROR | Passed |
| BB-08 | Duplicate email | Same email twice | POST twice | HTTP 409, `DUPLICATE_EMAIL` | HTTP 409, code=DUPLICATE_EMAIL | Passed |
| BB-09 | Request a nonexistent member | Random valid UUID | GET `/api/members/<uuid>` | HTTP 404, `NOT_FOUND` | HTTP 404, code=NOT_FOUND | Passed |
| BB-10 | Invalid UUID format | `not-a-uuid` | GET `/api/members/not-a-uuid` | HTTP 400, `INVALID_ID` | HTTP 400, code=INVALID_ID | Passed |
| BB-11 | Update a nonexistent member | Random UUID, valid phone | PATCH `/api/members/<uuid>` | HTTP 404, `NOT_FOUND` | HTTP 404, code=NOT_FOUND | Passed |
| BB-12 | Delete a nonexistent member | Random valid UUID | DELETE `/api/members/<uuid>` | HTTP 404, `NOT_FOUND` | HTTP 404, code=NOT_FOUND | Passed |
| BB-13 | Malformed JSON body | `{not json` | POST `/api/members` | HTTP 400, `INVALID_JSON` | HTTP 400, code=INVALID_JSON | Passed |
| BB-14 | Update with invalid phone | `phone = "abc"` | Create member, then PATCH | HTTP 400, `VALIDATION_ERROR` | HTTP 400, code=VALIDATION_ERROR | Passed |
| BB-15 | Anonymous visitor lists members | No login cookie | GET `/api/members` | HTTP 401, `UNAUTHENTICATED` | HTTP 401, code=UNAUTHENTICATED | Passed |
| BB-16 | Anonymous visitor creates a member | Valid data, no login | POST `/api/members` | HTTP 401, `UNAUTHENTICATED` | HTTP 401, code=UNAUTHENTICATED | Passed |
| BB-17 | Login with wrong password | Valid admin email, wrong password | POST `/api/auth/login` | HTTP 401, `INVALID_CREDENTIALS` | HTTP 401, code=INVALID_CREDENTIALS | Passed |
| BB-18 | Anonymous visitor lists user accounts | No login cookie | GET `/api/users` | HTTP 401, `UNAUTHENTICATED` | HTTP 401, code=UNAUTHENTICATED | Passed |
| BB-19 | Customer lists all members | Logged in as customer | GET `/api/members` | HTTP 403, `FORBIDDEN` | HTTP 403, code=FORBIDDEN | Passed |
| BB-20 | Customer deletes a member | Logged in as customer, random UUID | DELETE `/api/members/<uuid>` | HTTP 403, `FORBIDDEN` | HTTP 403, code=FORBIDDEN | Passed |
| BB-21 | Trainer creates a member | Logged in as trainer, valid data | POST `/api/members` | HTTP 403, `FORBIDDEN` | HTTP 403, code=FORBIDDEN | Passed |
| BB-22 | Trainer lists user accounts | Logged in as trainer | GET `/api/users` | HTTP 403, `FORBIDDEN` | HTTP 403, code=FORBIDDEN | Passed |

### Manual user-interface checks (black-box)

These are performed by the student in the browser and are separate from the automated counts.

| Test ID | Scenario | Steps | Expected result | Actual result | Status |
|---|---|---|---|---|---|
| UI-01 | Dashboard shows counts | Open `/` with some members | Four cards and recent-members table match the data | Not Run | Not Run |
| UI-02 | Dashboard empty state | Open `/` with no members | "No members yet" message | Not Run | Not Run |
| UI-03 | Add member with blank fields | Open `/members/new`, click Add | Validation messages shown under fields | Not Run | Not Run |
| UI-04 | Add valid member | Fill the form, submit | Success notification, member in list | Not Run | Not Run |
| UI-05 | Search members | Type part of a name | List is filtered; no-match shows empty state | Not Run | Not Run |
| UI-06 | Edit member | Open member, Edit, change phone, Save | Success notification, new value shown | Not Run | Not Run |
| UI-07 | Delete member | Open member, Delete, Confirm | Confirmation dialog, then removal | Not Run | Not Run |
| UI-08 | Trainers add/delete | Add a trainer, then delete it (as admin) | Trainer appears, then disappears after confirmation | Not Run | Not Run |
| UI-09 | Login with wrong password | Open `/login`, enter a wrong password | Error message "Invalid email or password" | Not Run | Not Run |
| UI-10 | Visit a page without logging in | Open `/members` in a private window | Redirected to `/login` | Not Run | Not Run |
| UI-11 | Customer pages | Log in as customer | Lands on My Membership; menu shows only My Membership and Plans; `/members` shows "Access denied" | Not Run | Not Run |
| UI-12 | Trainer pages | Log in as trainer | Members list is visible without Add, Edit or Delete buttons; no Users or Trainers menu entry | Not Run | Not Run |

## 17.2 White-box Testing

The unit tests below were chosen so that each condition takes both its true and false outcome. The "code path" is the condition in the TypeScript source (`lib/business/membership.ts`, `lib/validation/member.ts`); the unit tests run against the Python reference implementation of the same condition. Status is the status of the mapped tests in the real run recorded in `test-results/unit-tests.txt`.

| WB ID | Condition / branch | Outcome | Code path | Test ID(s) | Status |
|---|---|---|---|---|---|
| WB-01 | Phone is a string of exactly 10 ASCII digits | True | `isValidPhone` | UT-17 | Passed |
| WB-02 | Phone contains non-digits | False | `isValidPhone` | UT-18, UT-21, UT-23 | Passed |
| WB-03 | Phone has fewer than 10 digits | False | `isValidPhone` | UT-19 | Passed |
| WB-04 | Phone has more than 10 digits | False | `isValidPhone` | UT-20 | Passed |
| WB-05 | Phone is empty or not a string | False | `isValidPhone` | UT-22 | Passed |
| WB-06 | Email matches pattern and length <= 254 | True | `isValidEmail` | UT-24 | Passed |
| WB-07 | Email has no `@`, no domain dot, whitespace or empty label | False | `isValidEmail` | UT-25, UT-26, UT-27, UT-28 | Passed |
| WB-08 | Email empty or not a string | False | `isValidEmail` | UT-29 | Passed |
| WB-09 | Email longer than 254 characters | False | `isValidEmail` | UT-30 | Passed |
| WB-10 | Name nonblank and <= 100 characters | True | `isValidName` | UT-31 | Passed |
| WB-11 | Name empty or whitespace only | False | `isValidName` | UT-32, UT-33 | Passed |
| WB-12 | Name longer than 100 characters | False | `isValidName` | UT-34 | Passed |
| WB-13 | Name missing / not a string | False | `isValidName` | UT-35 | Passed |
| WB-14 | Plan is one of the three allowed values | True | `isValidPlan` | UT-13 | Passed |
| WB-15 | Plan is unsupported, wrong case, empty or not a string | False | `isValidPlan` | UT-14, UT-15, UT-16 | Passed |
| WB-16 | Date matches `YYYY-MM-DD` and is a real calendar date | True | `isValidDateString` | UT-12 | Passed |
| WB-17 | Date has wrong format | False | `isValidDateString` | UT-10 | Passed |
| WB-18 | Date has valid format but does not exist (e.g. 30 Feb, 29 Feb in non-leap year) | False | `isValidDateString` | UT-11, UT-12 | Passed |
| WB-19 | End date before start date | Error returned | `validateMembershipDates` | UT-07 | Passed |
| WB-20 | End date equal to start date | No error | `validateMembershipDates` | UT-08 | Passed |
| WB-21 | End date after start date | No error | `validateMembershipDates` | UT-09 | Passed |
| WB-22 | Start or end date malformed | Error returned | `validateMembershipDates` | UT-10 | Passed |
| WB-23 | Date range invalid | Throws | `calculateMembershipStatus` | UT-05 | Passed |
| WB-24 | Current date invalid | Throws | `calculateMembershipStatus` | UT-06 | Passed |
| WB-25 | `end_date >= current_date` (future) | True, Active | `calculateMembershipStatus` | UT-01 | Passed |
| WB-26 | `end_date >= current_date` (equal) | True, Active | `calculateMembershipStatus` | UT-03 | Passed |
| WB-27 | `end_date < current_date` (past) | False, Expired | `calculateMembershipStatus` | UT-02, UT-04 | Passed |
| WB-28 | Request body is not an object | `_form` error | `validateMemberInput` | UT-42 | Passed |
| WB-29 | Each field error is reported independently | Errors per field | `validateMemberInput` | UT-37, UT-38, UT-39, UT-40, UT-41 | Passed |
| WB-30 | All fields valid | Accepted | `validateMemberInput` | UT-36 | Passed |
| WB-31 | Email and phone are trimmed before checking | Accepted | `validateMemberInput` | UT-43 | Passed |
| WB-32 | API receives malformed request data (bad JSON, bad UUID, missing fields) | HTTP 400 | `readJson`, `assertUuid`, route handlers | BB-13, BB-10, BB-05 | Passed |
| WB-33 | Role is a known role and the action is in its permission list | True | `can` | UT-44, UT-45, UT-49 | Passed |
| WB-34 | Role is known but the action is not in its list | False | `can` | UT-46, UT-47, UT-48, UT-50, UT-51 | Passed |
| WB-35 | Role is unknown, wrongly cased, missing or not a string | False | `isValidRole`, `can` | UT-52, UT-53, UT-54 | Passed |
| WB-36 | Password length is below, at and above each limit (7, 8, 72, 73) | False / True / True / False | `isValidPassword` | UT-55, UT-56, UT-57, UT-58 | Passed |
| WB-37 | Password empty or not a string | False | `isValidPassword` | UT-59 | Passed |
| WB-38 | Registration: valid; each invalid field reported; body not an object | Accepted / errors / `_form` | `validateRegister` | UT-60, UT-61, UT-62, UT-63, UT-64 | Passed |
| WB-39 | No session cookie / expired session | 401 | `requireAuth` | BB-15, BB-16, BB-18, IT-11 | Passed |
| WB-40 | Session valid but role lacks the permission | 403 | `requireAuth` | BB-19, BB-20, BB-21, BB-22 | Passed |

**Note.** These are branch-oriented tests. A coverage tool was not run, so the report makes no claim about statement or branch coverage percentages.

## 17.3 Unit Testing

Files: `tests/unit/test_unit_membership.py` (UT-01 to UT-16), `tests/unit/test_unit_validation.py` (UT-17 to UT-43) and `tests/unit/test_unit_auth.py` (UT-44 to UT-64: role permissions, password rule, registration validation). All tests use fixed data; the "current date" is fixed at 2025-06-15. The Actual and Status columns were taken from the saved output `test-results/unit-tests.txt`.

| Test ID | Method | Purpose | Expected result | Actual result | Status |
|---|---|---|---|---|---|
| UT-01 | `test_UT01_end_date_in_future_is_active` | End date in future is active | Active | As expected | Passed |
| UT-02 | `test_UT02_end_date_in_past_is_expired` | End date in past is expired | Expired | As expected | Passed |
| UT-03 | `test_UT03_end_date_equal_to_today_is_active_boundary` | End date equal to today is active boundary | Active (end date itself is Active) | As expected | Passed |
| UT-04 | `test_UT04_day_after_end_date_is_expired_boundary` | Day after end date is expired boundary | Expired | As expected | Passed |
| UT-05 | `test_UT05_invalid_range_raises_error` | Invalid range raises error | ValueError raised | As expected | Passed |
| UT-06 | `test_UT06_invalid_current_date_raises_error` | Invalid current date raises error | ValueError raised | As expected | Passed |
| UT-07 | `test_UT07_end_date_before_start_date_is_rejected` | End date before start date is rejected | Error message returned | As expected | Passed |
| UT-08 | `test_UT08_end_date_equal_to_start_date_is_accepted` | End date equal to start date is accepted | No error | As expected | Passed |
| UT-09 | `test_UT09_end_date_after_start_date_is_accepted` | End date after start date is accepted | No error | As expected | Passed |
| UT-10 | `test_UT10_malformed_date_is_rejected` | Malformed date is rejected | Rejected / error message | As expected | Passed |
| UT-11 | `test_UT11_impossible_calendar_date_is_rejected` | Impossible calendar date is rejected | Rejected | As expected | Passed |
| UT-12 | `test_UT12_leap_day_is_accepted_only_in_leap_year` | Leap day is accepted only in leap year | 2024-02-29 valid, 2025-02-29 invalid | As expected | Passed |
| UT-13 | `test_UT13_each_supported_plan_is_accepted` | Each supported plan is accepted | All three accepted | As expected | Passed |
| UT-14 | `test_UT14_unsupported_plan_is_rejected` | Unsupported plan is rejected | Rejected | As expected | Passed |
| UT-15 | `test_UT15_plan_is_case_sensitive` | Plan is case sensitive | Rejected | As expected | Passed |
| UT-16 | `test_UT16_empty_or_non_string_plan_is_rejected` | Empty or non string plan is rejected | All rejected | As expected | Passed |
| UT-17 | `test_UT17_valid_ten_digit_phone_is_accepted` | Valid ten digit phone is accepted | Accepted | As expected | Passed |
| UT-18 | `test_UT18_phone_with_letters_is_rejected` | Phone with letters is rejected | Rejected | As expected | Passed |
| UT-19 | `test_UT19_phone_shorter_than_ten_digits_is_rejected` | Phone shorter than ten digits is rejected | Rejected | As expected | Passed |
| UT-20 | `test_UT20_phone_longer_than_ten_digits_is_rejected` | Phone longer than ten digits is rejected | Rejected | As expected | Passed |
| UT-21 | `test_UT21_phone_with_spaces_or_symbols_is_rejected` | Phone with spaces or symbols is rejected | Both rejected | As expected | Passed |
| UT-22 | `test_UT22_empty_or_non_string_phone_is_rejected` | Empty or non string phone is rejected | All rejected | As expected | Passed |
| UT-23 | `test_UT23_non_ascii_digits_are_rejected` | Non ascii digits are rejected | Rejected | As expected | Passed |
| UT-24 | `test_UT24_valid_email_is_accepted` | Valid email is accepted | Accepted | As expected | Passed |
| UT-25 | `test_UT25_email_without_at_sign_is_rejected` | Email without at sign is rejected | Rejected | As expected | Passed |
| UT-26 | `test_UT26_email_without_domain_dot_is_rejected` | Email without domain dot is rejected | Rejected | As expected | Passed |
| UT-27 | `test_UT27_email_with_whitespace_is_rejected` | Email with whitespace is rejected | Rejected | As expected | Passed |
| UT-28 | `test_UT28_email_with_empty_domain_label_is_rejected` | Email with empty domain label is rejected | Both rejected | As expected | Passed |
| UT-29 | `test_UT29_empty_or_non_string_email_is_rejected` | Empty or non string email is rejected | Both rejected | As expected | Passed |
| UT-30 | `test_UT30_overlong_email_is_rejected` | Overlong email is rejected | Rejected | As expected | Passed |
| UT-31 | `test_UT31_nonblank_name_is_accepted` | Nonblank name is accepted | Accepted | As expected | Passed |
| UT-32 | `test_UT32_empty_name_is_rejected` | Empty name is rejected | Rejected | As expected | Passed |
| UT-33 | `test_UT33_whitespace_only_name_is_rejected` | Whitespace only name is rejected | Rejected | As expected | Passed |
| UT-34 | `test_UT34_overlong_name_is_rejected` | Overlong name is rejected | Rejected | As expected | Passed |
| UT-35 | `test_UT35_missing_name_is_rejected` | Missing name is rejected | Rejected | As expected | Passed |
| UT-36 | `test_UT36_correct_member_data_is_accepted` | Correct member data is accepted | Accepted, no errors | As expected | Passed |
| UT-37 | `test_UT37_blank_name_makes_member_invalid` | Blank name makes member invalid | Only full_name reported | As expected | Passed |
| UT-38 | `test_UT38_unsupported_plan_makes_member_invalid` | Unsupported plan makes member invalid | Only membership_plan reported | As expected | Passed |
| UT-39 | `test_UT39_end_before_start_makes_member_invalid` | End before start makes member invalid | Only end_date reported | As expected | Passed |
| UT-40 | `test_UT40_bad_phone_and_email_are_both_reported` | Bad phone and email are both reported | email and phone reported | As expected | Passed |
| UT-41 | `test_UT41_missing_required_fields_are_all_reported` | Missing required fields are all reported | All six fields reported | As expected | Passed |
| UT-42 | `test_UT42_malformed_request_body_is_rejected` | Malformed request body is rejected | _form error for every non-object body | As expected | Passed |
| UT-43 | `test_UT43_surrounding_spaces_in_email_and_phone_are_trimmed` | Surrounding spaces in email and phone are trimmed | Accepted | As expected | Passed |
| UT-44 | `test_UT44_admin_can_perform_every_action` | Admin can perform every action | All seven actions allowed | As expected | Passed |
| UT-45 | `test_UT45_trainer_can_read_members_trainers_and_dashboard` | Trainer can read members trainers and dashboard | All four actions allowed | As expected | Passed |
| UT-46 | `test_UT46_trainer_cannot_write_members` | Trainer cannot write members | Denied | As expected | Passed |
| UT-47 | `test_UT47_trainer_cannot_write_trainers` | Trainer cannot write trainers | Denied | As expected | Passed |
| UT-48 | `test_UT48_trainer_cannot_manage_users` | Trainer cannot manage users | Denied | As expected | Passed |
| UT-49 | `test_UT49_customer_can_only_read_own_details` | Customer can only read own details | Only me:read allowed | As expected | Passed |
| UT-50 | `test_UT50_customer_cannot_list_members` | Customer cannot list members | Denied | As expected | Passed |
| UT-51 | `test_UT51_customer_has_no_dashboard` | Customer has no dashboard | Denied | As expected | Passed |
| UT-52 | `test_UT52_unknown_role_has_no_permissions` | Unknown role has no permissions | Denied for every action | As expected | Passed |
| UT-53 | `test_UT53_missing_or_non_string_role_has_no_permissions` | Missing or non string role has no permissions | Denied | As expected | Passed |
| UT-54 | `test_UT54_role_names_are_valid_only_when_exact` | Role names are valid only when exact | Exact role names valid; others invalid | As expected | Passed |
| UT-55 | `test_UT55_eight_character_password_is_accepted` | Eight character password is accepted | Accepted | As expected | Passed |
| UT-56 | `test_UT56_seven_character_password_is_rejected` | Seven character password is rejected | Rejected | As expected | Passed |
| UT-57 | `test_UT57_seventy_two_character_password_is_accepted` | Seventy two character password is accepted | Accepted | As expected | Passed |
| UT-58 | `test_UT58_seventy_three_character_password_is_rejected` | Seventy three character password is rejected | Rejected | As expected | Passed |
| UT-59 | `test_UT59_empty_or_non_string_password_is_rejected` | Empty or non string password is rejected | All rejected | As expected | Passed |
| UT-60 | `test_UT60_valid_registration_is_accepted` | Valid registration is accepted | Accepted, no errors | As expected | Passed |
| UT-61 | `test_UT61_invalid_email_is_reported` | Invalid email is reported | Only email reported | As expected | Passed |
| UT-62 | `test_UT62_short_password_is_reported` | Short password is reported | Only password reported | As expected | Passed |
| UT-63 | `test_UT63_blank_name_is_reported` | Blank name is reported | Only full_name reported | As expected | Passed |
| UT-64 | `test_UT64_non_object_body_is_rejected` | Non object body is rejected | _form error for every non-object body | As expected | Passed |

The requirement list from the project brief maps to the tests as follows.

| Required case | Test ID(s) |
|---|---|
| End date in future gives Active | UT-01 |
| End date in past gives Expired | UT-02 |
| End date equal to current date (boundary) | UT-03 (Active), UT-04 (day after is Expired) |
| Valid 10-digit phone | UT-17 |
| Phone with letters | UT-18 |
| Phone shorter than 10 digits | UT-19 |
| Phone longer than 10 digits | UT-20 |
| Valid email | UT-24 |
| Invalid email | UT-25 to UT-28 |
| Blank member name | UT-32, UT-33, UT-37 |
| Valid plan | UT-13 |
| Unsupported plan | UT-14, UT-38 |
| End date earlier than start date | UT-07, UT-39 |
| Correct member data | UT-36 |

## 17.4 Integration Testing

**What is tested.** `tests/integration/test_integration_members_api.py` sends HTTP requests with `urllib.request` to the running Next.js server. A request passes through the API route, server-side validation, the service layer and Supabase, and the JSON response is checked. This covers the interaction of those components.

**Setup requirements.**

1. A Supabase **test** project with `supabase/schema.sql` applied and "Confirm email" turned off.
2. `.env.local` with that project's URL and anon key.
3. An **admin test account**: register it in the app, then promote it with the SQL statement in Appendix C.
4. Server running: `npm run dev`.
5. In the terminal running Python: `RUN_INTEGRATION_TESTS=1`, `TEST_ADMIN_EMAIL` and `TEST_ADMIN_PASSWORD`. Passwords are read from the environment only and are never written to files or printed.

**Execution command.**

```powershell
$env:RUN_INTEGRATION_TESTS = "1"
$env:TEST_ADMIN_EMAIL = "admin@gymtest.example"
$env:TEST_ADMIN_PASSWORD = "<your test password>"
python -m unittest discover -s tests -p "test_integration_*.py" -v
```

**Safety and cleanup.** Without the flag, or if the server or database is unreachable, the suite is *skipped* with a reason; a skip is never counted as a pass. A non-local `TEST_BASE_URL` is refused unless `ALLOW_REMOTE_TEST_SERVER=1`. The tests log in as the admin test account and send the session cookie with each request. Every test uses a unique email (`@gymtest.example`) and registers each record it creates. The tests never create or delete user accounts. `tearDown` deletes only those records and fails the test if a deletion fails, so cleanup problems are reported and not hidden.

| Test ID | Method | Scenario | Expected result | Actual result | Status |
|---|---|---|---|---|---|
| IT-01 | `test_IT01_create_valid_member` | Create a valid member | 201; fields echoed; `status` = Active; ID and `created_at` present | As expected | Passed |
| IT-02 | `test_IT02_retrieve_created_member` | Retrieve the new member | 200; same ID and email | As expected | Passed |
| IT-03 | `test_IT03_update_member_details` | Update phone, plan, end date | 200; change persisted when read again | As expected | Passed |
| IT-04 | `test_IT04_reject_invalid_phone` | Invalid phone | 400 `VALIDATION_ERROR`, field `phone` | As expected | Passed |
| IT-05 | `test_IT05_reject_invalid_date_range` | End before start | 400, field `end_date` | As expected | Passed |
| IT-06 | `test_IT06_reject_duplicate_email` | Duplicate email (also upper case) | 409 `DUPLICATE_EMAIL` | As expected | Passed |
| IT-07 | `test_IT07_delete_member` | Delete member | 200 | As expected | Passed |
| IT-08 | `test_IT08_deleted_member_cannot_be_retrieved` | Retrieve after delete | 404 `NOT_FOUND` | As expected | Passed |
| IT-09 | `test_IT09_list_members_includes_created_member` | List and search | 200; list contains the member; search by email prefix finds it | As expected | Passed |
| IT-10 | `test_IT10_login_creates_session_that_identifies_the_admin` | Log in as admin, call `/api/auth/me` | 200; role `admin`; no token in the JSON body | As expected | Passed |
| IT-11 | `test_IT11_logout_ends_the_session` | Log out, then call `/api/auth/me` | 401 `UNAUTHENTICATED` | As expected | Passed |
| IT-12 | `test_IT12_admin_can_list_users_and_sees_own_account` | Admin lists users | 200; list contains the admin's own ID; roles are valid | As expected | Passed |

**Actual execution.** The suite was executed with the command above against the locally running application and the student's Supabase project: 12 tests ran, 12 passed, 0 failed, 0 errored, 0 skipped (see `test-results/integration-tests.txt` for the date, exit code and console output). Before the schema and accounts existed, the same suite was run and was skipped (for example with `Could not log in as test admin: HTTP 401`); those skips were not counted as passes. All records created by the tests were removed by `tearDown`, which would have failed a test if a deletion had failed.

# 18. Test Execution Procedures

All commands are run from the repository root.

**Prepare:**

```bash
npm install
cp .env.example .env.local   # then edit the two values
```

**Unit tests (no server or database):**

```bash
python -m unittest discover -s tests/unit -v
python -m unittest discover -s tests -p "test_unit_*.py" -v
```

**Integration tests (server running, test database, admin test account):**

```powershell
$env:RUN_INTEGRATION_TESTS = "1"
$env:TEST_ADMIN_EMAIL = "admin@gymtest.example"
$env:TEST_ADMIN_PASSWORD = "<your test password>"
python -m unittest discover -s tests -p "test_integration_*.py" -v
```

**Black-box tests (same variables; add the optional trainer and customer accounts for BB-19 to BB-22):**

```powershell
$env:RUN_INTEGRATION_TESTS = "1"
# plus TEST_ADMIN_*; optional: TEST_TRAINER_EMAIL / TEST_TRAINER_PASSWORD, TEST_CUSTOMER_EMAIL / TEST_CUSTOMER_PASSWORD
python -m unittest discover -s tests -p "test_blackbox_*.py" -v
```

**Complete suite:**

```bash
python -m unittest discover -s tests -p "test_*.py" -v
```

**Save real output to `test-results/`:**

```bash
python scripts/run_tests.py unit
python scripts/run_tests.py integration
python scripts/run_tests.py blackbox
python scripts/run_tests.py all
```

**Static checks:**

```bash
npm run lint
npm run typecheck
npm run build
```

# 19. Test Execution Results

Source: `test-results/unit-tests.txt`, `test-results/integration-tests.txt`, `test-results/blackbox-tests.txt`, `test-results/full-test-suite.txt` and `test-results/summary.json`, produced by `scripts/run_tests.py`. The execution date and time of each run are in the header of each file.

## 19.1 Automated Python tests

Each test is counted once, in the category where it was written.

| Suite | Planned | Executed | Passed | Failed | Errored | Skipped / Not Run |
|---|---|---|---|---|---|---|
| Unit (incl. white-box evidence) | 64 | 64 | 64 | 0 | 0 | 0 |
| Integration | 12 | 12 | 12 | 0 | 0 | 0 |
| Black-box | 22 | 22 | 22 | 0 | 0 | 0 |
| **Total automated** | **98** | **98** | **98** | **0** | **0** | **0** |

Notes on counting:

- The combined run (`full-test-suite.txt`) shows 98 tests ran and 98 passed. It repeats the same tests and is not added to the totals.
- If the HTTP suites are run without the opt-in flag or without a reachable server and admin account, unittest reports a skipped test class ("skipped=1", "Ran 0 tests"). That is a skip, not a pass.
- The runs took about 0.002 s for the unit suite and about 20 to 40 s for the HTTP suites, because every HTTP request reaches Supabase.
- The white-box table in Section 17.2 reuses unit tests, so it adds no executions.

## 19.2 Manual interface checks

| Planned | Executed | Passed | Failed | Not Run |
|---|---|---|---|---|
| 12 (UI-01 to UI-12) | 0 | 0 | 0 | 12 |

These counts are kept separate from the automated counts.

## 19.3 Static checks

| Check | Command | Result |
|---|---|---|
| Lint | `npm run lint` | No errors reported |
| Type check | `npx tsc --noEmit` | No errors reported |
| Production build | `npm run build` | Succeeded, with no errors or warnings in the build log (after the fixes in D-02 and D-04) |

## 19.4 Why the manual checks were not run

The 12 manual interface checks (UI-01 to UI-12) are performed by the student in a browser and were not recorded, so they remain Not Run. The student informally reported that all three demo accounts could log in and see their own pages; this was not recorded step by step and is not counted. Note also that the student's single Supabase project was used for the HTTP tests, not a separate test project; the tests only create and delete their own `@gymtest.example` records. Section 19 must then be updated with the real numbers.

## 19.5 Informal smoke check (not counted)

During development the built application was started with a dummy, unreachable Supabase URL and checked manually with `curl`. Before the HTTP suites could be run, requests without a login cookie returned HTTP 401 `UNAUTHENTICATED` for the members, users and `/api/auth/me` endpoints, and visiting `/` or `/members` without a cookie redirected to `/login`. Login and registration requests with invalid fields returned HTTP 400 with field messages, and a request carrying a garbage session cookie returned HTTP 401. The HTTP test client's cookie handling was also checked against a small local fake server (login, authenticated request, logout, request after logout). These checks were superseded by the recorded HTTP test runs above. This was an ad hoc check, was not recorded as a test run, and is not counted in any table.

# 20. Test Evidence

The figures below show the evidence for the tests and the application.

**Figure 1: Dashboard with member and trainer counts**

\

\

\

\

\

\

\

\

\

\

**Figure 2: Member listing**

\

\

\

\

\

\

\

\

\

\

**Figure 3: Add member form showing validation messages**

\

\

\

\

\

\

\

\

\

\

**Figure 4: Unit test execution in the terminal**

\

\

\

\

\

\

\

\

\

\

**Figure 5: Black-box test execution in the terminal**

\

\

\

\

\

\

\

\

\

\

**Figure 6: Integration test execution in the terminal**

\

\

\

\

\

\

\

\

\

\

**Figure 7: Supabase Table Editor showing member records**

\

\

\

\

\

\

\

\

\

\


# 21. Defect Report

Only defects that were actually observed during development are listed as real. Severity (impact) and priority (urgency of fixing) are defined separately in Section 24.

## 21.1 Defect log

| Field | D-01 | D-02 |
|---|---|---|
| Title | Missing end date not reported on its own field | Production build failed on `/members/[id]` |
| Description | Validation of a request with no dates reported an error only for `start_date`; the missing `end_date` was not reported. Both the TypeScript validator and the Python reference reported only the first date problem. | `next build` failed while prerendering `/members/[id]` because the sidebar called `usePathname()` outside a `<Suspense>` boundary. |
| Steps to reproduce | Run unit test UT-41 (`validate_member_input({})`). | Run `npm run build`. |
| Expected result | All six fields (including `start_date` and `end_date`) reported. | Build completes. |
| Actual result | Only five fields reported: `email, full_name, membership_plan, phone, start_date`. | Error: `Route "/members/[id]": Next.js encountered URL data usePathname() in a Client Component outside of <Suspense>`. |
| Severity | Low (error messages incomplete; invalid data still rejected) | High (application could not be built for production) |
| Priority | Medium | High |
| Category | Input-validation defect | Configuration and environment defect |
| Status | Closed | Closed |
| Related test ID | UT-41 | Not an automated test; found by `npm run build` |
| Fix and retest result | See Section 22 | See Section 22 |

Defects found while adding login and roles:

| Field | D-03 | D-04 |
|---|---|---|
| Title | Email lookup could match a different account | Build log showed an unexpected-error message for two API routes |
| Description | The first version of `GET /api/me` found the trainer or member record with a case-insensitive pattern match (`ilike`). In a pattern match `_` and `%` are wildcards, and `_` is common in email addresses, so a trainer's email could match a different trainer's record. | `handle()` caught every exception. During `next build`, Next.js aborts the prerender of GET routes that read cookies by throwing an internal signal, which `handle()` caught and logged as an "Unexpected API error". |
| Steps to reproduce | Found by code review, not by a test: read `app/api/me/route.ts`. | Run `npm run build`. |
| Expected result | Exact match on the lower-cased email. | Clean build log. |
| Actual result | Pattern match with wildcards. | Message `Unexpected API error: Error: During prerendering, cookies() rejects ...` for `/api/me` and `/api/auth/me`. |
| Severity | Medium (wrong record could be shown; customers were also protected by Row Level Security) | Low (log noise only; the routes still worked) |
| Priority | High | Low |
| Category | Functional / data-integrity | Error-handling |
| Status | Closed | Closed |
| Related test ID | None. Not caught by any test | Not an automated test; found by `npm run build` |
| Fix and retest result | See Section 22 | See Section 22 |

## 21.2 Evidence for D-01 (first run, copied from the console)

```text
FAIL: test_UT41_missing_required_fields_are_all_reported (test_unit_validation.TestMemberInputValidation.test_UT41_missing_required_fields_are_all_reported)
AssertionError: Lists differ: ['email', 'full_name', 'membership_plan', 'phone', 'start_date'] != ['email', 'end_date', 'full_name', 'membership_plan', 'phone']
Ran 43 tests in 0.003s

FAILED (failures=1)
```

(The expected list in that first run did not yet include `start_date`; the comparison showed that the implementation reported `start_date` and not `end_date`. The intended rule is that each missing field is reported, so the implementation, and then the test's expected list, were corrected.)

## 21.3 Defect log template

Use this template for future defects.

| Field | Value |
|---|---|
| Defect ID | D-nn |
| Title | |
| Description | |
| Steps to reproduce | |
| Expected result | |
| Actual result | |
| Severity | Critical / High / Medium / Low |
| Priority | High / Medium / Low |
| Status | Open / In progress / Fixed / Closed / Rejected |
| Related test ID | |
| Fix and retest result | |

## 21.4 Hypothetical examples (not real defects)

These illustrate the taxonomy only and did not occur.

| ID | Hypothetical defect | Category |
|---|---|---|
| H-1 | A duplicate email differing only in letter case is accepted | Database and data-integrity |
| H-2 | PATCH with an end date earlier than the stored start date returns 200 | Functional / API |
| H-3 | A database error message is shown to the user | Error-handling |

# 22. Defect Retesting Report

| Defect | Fix applied | Retest | Result |
|---|---|---|---|
| D-01 | `validateMemberInput` (TypeScript) and `validate_member_input` (Python) now check each date separately and apply the ordering rule only if both dates are valid. The expected list in UT-41 was updated to include all six fields. | `python -m unittest discover -s tests/unit -v` | `Ran 43 tests`, `OK` (saved in `test-results/unit-tests.txt`) |
| D-02 | The sidebar in `app/layout.tsx` is wrapped in `<Suspense>`. | `npm run build` | Build succeeded and all routes were generated |
| D-03 | The lookup now uses an exact match (`eq`) on the lower-cased email. | `npx tsc --noEmit`, `npm run lint`. No automated test covers this route. | Compiles and lints cleanly. Behaviour is **not** verified by a test; it would be covered by a manual check with a trainer account. |
| D-04 | `handle()` now calls `unstable_rethrow(err)` so Next.js internal signals pass through, and `requireAuth` calls `connection()` so the routes are evaluated at request time. | `npm run build` | The build log no longer shows the message. A first attempt with only `connection()` still logged it; adding `unstable_rethrow` removed it. |

The PyUnit unit tests exercise the Python implementation, so they alone could not verify the TypeScript change for D-01. Black-box test BB-05 and integration test IT-04, which call the real application, passed after the HTTP suites were run.

# 23. Regression Testing Results

**Procedure.** After any change to validation or API code, re-run: all unit tests; all configured integration tests; all black-box tests. Compare the new result with the previous one. Any test that passed before and fails now is a regression.

**Regression runs performed.**

| Trigger | Suite | Result |
|---|---|---|
| Fix for D-01 | Unit | 43 of 43 passed at that time (the suite later grew to 64 tests with the role and registration tests; all 64 passed) |
| Fix for D-01 | Integration | Not Run at that time (no database was configured). Run later: 12 of 12 passed |
| Fix for D-01 | Black-box | Not Run at that time. Run later: 22 of 22 passed, including BB-05 |
| Fix for D-02 | Lint, type check, build | Completed without errors |
| Adding login and roles (changes to every API route) | Unit | 64 of 64 passed |
| Adding login and roles | Integration, black-box | 12 of 12 and 22 of 22 passed, so the member API still works after authentication was added |
| Adding login and roles | Lint, type check, build | Completed without errors |

The HTTP suites were run after Supabase keys were added and the regression check for D-01 is complete.

# 24. Bug Taxonomy

## 24.1 Categories

| Category | Examples in this project |
|---|---|
| Functional defects | Wrong Active/Expired status |
| Input-validation defects | Invalid phone or email accepted; D-01 |
| Database and data-integrity defects | Duplicate email stored; end date before start date stored |
| API and integration defects | Wrong HTTP status code; response missing fields |
| Interface and usability defects | Missing validation message; no loading state |
| Error-handling defects | Stack trace shown to client; crash on malformed JSON |
| Authentication and authorization defects | Anyone can change data (known limitation of this demo) |
| Configuration and environment defects | Missing environment variable; D-02 |

## 24.2 Severity

| Severity | Meaning |
|---|---|
| Critical | System unusable, data loss or security breach |
| High | Major feature broken; no reasonable workaround |
| Medium | Feature partly broken; workaround exists |
| Low | Minor or cosmetic issue |

## 24.3 Priority

Priority is how soon a defect should be fixed, and is independent of severity.

| Priority | Meaning |
|---|---|
| High | Fix immediately |
| Medium | Fix in the current cycle |
| Low | Fix when time permits |

Example: a spelling mistake in the college name on the cover page has *Low* severity but may have *High* priority before submission.

# 25. Test Summary and Analysis

- 98 of 98 executed automated tests passed (64 unit, 12 integration, 22 black-box). The 12 manual checks were not run.
- Two defects were found by tests or the build (D-01, D-02), one by code review (D-03) and one by the build log (D-04). All are closed. D-01 now also has an HTTP-level retest (BB-05 and IT-04 passed against the real TypeScript application); D-03 still has no test.
- The business rules (status calculation, phone, email, name, plan, dates) are verified at the unit level for both outcomes of each condition.
- The unit tests verify a Python reference implementation. They do not by themselves prove the TypeScript code is correct. Evidence for the real application requires the HTTP suites.
- The HTTP tests verified, against a real Supabase project, member create/read/update/delete, persistence of updates, rejection of invalid and duplicate input (including case-insensitive duplicate email), login, logout, 401 for anonymous requests, and 403 for the customer and trainer accounts. The role permission matrix is verified in its Python reference form (unit tests) and through those 403 results.
- Not verified: the user interface (manual checks), the trainer API, `/api/me`, the Users page operations (create user, change role), and the admin-only Row Level Security policies beyond the effects visible through the API.
- No code-coverage measurement was made.

# 26. Acceptance Decision

**Decision: Conditionally accepted.**

| Criterion | Evaluation |
|---|---|
| All critical business rules pass their tests | Met at unit level (64 of 64 passed) |
| All planned tests executed or marked Not Run with reasons | Met: all 98 automated tests were executed; the 12 manual tests are marked Not Run with the reason in Section 19.4 |
| No unresolved critical defects | Met: no critical defects recorded; D-01 to D-04 are closed |
| Member CRUD works in the configured environment | Met: IT-01 to IT-09 passed |
| Invalid requests are rejected | Met: UT tests, IT-04 to IT-06 and BB-02 to BB-14 passed |
| Unauthenticated and unauthorized requests are refused | Met for the tested operations: BB-15 to BB-22 passed (401 for anonymous, 403 for customer and trainer). Other operations were not tested. |
| Database changes are persisted and retrievable | Met: IT-02, IT-03 and IT-08 passed |
| Report reflects real results | Met |

**Condition for full acceptance.** Perform the 12 manual interface checks (UI-01 to UI-12) and record their results in Section 17.1, and resolve any defect that they reveal. The decision is *conditional* only because of those unrecorded manual checks and the untested areas listed in Section 25; if a manual check fails, the decision should be revised.

# 27. Limitations

- Authentication is basic: no password reset, no email verification flow, no multi-factor login and no rate limiting of its own. Anyone can self-register, but only as a customer. Cookie sessions rely on `SameSite=Lax` and have no separate CSRF token. The design is for a college demonstration, not for production.
- Accounts are linked to member and trainer records by email address only. If the email differs, the customer sees "No membership found".
- The first admin has to be promoted with an SQL statement.
- "Confirm email" must be switched off in Supabase for the demo, otherwise new accounts cannot log in until they confirm.
- The Python reference implementation is separate from the TypeScript code; PyUnit does not run TypeScript.
- Phone validation (exactly 10 digits) is a simplification for this demo.
- Trainers have no automated tests.
- No coverage measurement, performance testing or automated UI testing.
- Membership status is computed with the server's local date; time zones are not handled.
- HTTP tests write to a real Supabase project (the student used their single project, not a separate test project) and need a running server and demo accounts.

# 28. Future Enhancements

- Add password reset, email verification and rate limiting; let customers update their own phone number.
- Add a stronger link between accounts and member or trainer records (for example a foreign key) and a trainer-to-member assignment.
- Add trainer API tests and a trainer-to-member assignment.
- Measure coverage with `coverage.py` for the Python code and a JavaScript test runner for the TypeScript code.
- Add pagination and filtering by status.
- Add attendance, payments and renewal reminders.
- Run the test suite automatically in continuous integration.

# 29. Conclusion

The project delivers a small but complete Gym Management System with login and three roles, and a PyUnit-based test suite covering unit, white-box, black-box, integration and regression testing. Ninety-eight automated tests were executed and passed after one validation defect was found and fixed. Three further defects (a build failure, an unsafe email lookup and build-log noise) were also found and fixed. The 34 HTTP-level tests were then run against a real Supabase project and all passed, so all 98 automated tests passed. The 12 manual interface checks are ready but have not been recorded. After the student adds Supabase keys and runs them, the results should be added to this report to complete the evaluation.

# 30. Appendix

## A. Test commands

```bash
python -m unittest discover -s tests/unit -v
python -m unittest discover -s tests -p "test_unit_*.py" -v
python -m unittest discover -s tests -p "test_integration_*.py" -v
python -m unittest discover -s tests -p "test_blackbox_*.py" -v
python -m unittest discover -s tests -p "test_*.py" -v
python scripts/run_tests.py all
```

## B. Environment variables

| Variable | Used by | Purpose |
|---|---|---|
| `NEXT_PUBLIC_SUPABASE_URL` | Next.js server | Supabase project URL |
| `NEXT_PUBLIC_SUPABASE_ANON_KEY` | Next.js server | Supabase anon (public) key |
| `RUN_INTEGRATION_TESTS` | Python tests | Must be `1` to run HTTP tests |
| `TEST_ADMIN_EMAIL`, `TEST_ADMIN_PASSWORD` | Python tests | Admin test account (required for HTTP tests) |
| `TEST_TRAINER_EMAIL`, `TEST_TRAINER_PASSWORD` | Python tests | Optional; enables BB-21 and BB-22 |
| `TEST_CUSTOMER_EMAIL`, `TEST_CUSTOMER_PASSWORD` | Python tests | Optional; enables BB-19 and BB-20 |
| `TEST_BASE_URL` | Python tests | Server URL (default `http://localhost:3000`) |
| `ALLOW_REMOTE_TEST_SERVER` | Python tests | Must be `1` to allow a non-local server |

## C. Database setup

1. Create a Supabase project (a separate one for testing).
2. Open the SQL Editor.
3. Paste and run `supabase/schema.sql`.
4. Copy the project URL and anon key into `.env.local`.
5. In Authentication > Providers > Email, turn off "Confirm email".
6. Check in the Table Editor that `members`, `trainers` and `profiles` exist.
7. Register the first account in the app, then run in the SQL Editor (use your own email):

```sql
update public.profiles set role = 'admin' where email = 'you@example.com';
```

8. Log out and log in again. Create trainer and customer accounts from the Users page, and add a member or trainer record with the same email for each.

## D. Pandoc commands

```bash
pandoc miniprojectreport.md -o miniprojectreport.docx --toc
pandoc miniprojectreport.md -o miniprojectreport.pdf --toc
```
