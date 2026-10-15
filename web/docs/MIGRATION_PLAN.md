# Web migration and STQA delivery plan

The current Swing/JDBC application is the feature reference. The browser edition keeps Java 17, JDBC, the existing MySQL schema, demo roles, and the six desktop JasperReports templates.

## Work sequence

1. Resolve Windows startup; use the checkout's dedicated MySQL instance on port 3307 and a free web port.
2. Finish feature parity: three-role access, dashboard, members and initial measurements, trainer/account creation, attendance, memberships, equipment, products, user management, own-profile/password changes, scoped trainer/member records, reports and backup.
3. Rebuild browser forms around named choices, clear validation, responsive navigation, accessible dialogs, searchable tables and genuine database metrics.
4. Run Selenium WebDriver regression tests with isolated test records; cover positive, negative, role access, CRUD, report downloads, responsive layouts and persistence.
5. Supply Selenium IDE scripts and execute the supported replay workflow. Record exactly how they were run.
6. Produce the STQA mini-project PDF, test plan/cases, defect taxonomy, execution report, exploratory testing notes, screenshots and run instructions.

## Scope inherited from the desktop application

- Payment and sales history, workout plans and training sessions are existing read-only workflows.
- Trainers record progress for active assigned members; members read their own records.
- Admins control membership assignment/payment flags and plan prices. A payment flag is not a transaction.
- Browser report outputs use clean OpenPDF layouts over the same six datasets; the desktop JasperReports templates remain available.
- Existing local data and edited demo passwords are preserved. Test fixtures use distinct names and exact IDs and are cleaned up after execution.

## Acceptance evidence

At least 30 test cases with expected and actual results, Selenium scripts, real run logs, fixed defect records, 2–3 or more GUI screenshots, code/output screenshots, and a report based on measured outcomes. No result is marked passed without execution evidence.

## Completed delivery

- Running local web app at 127.0.0.1:8081 on this checkout's MySQL instance.
- Final detailed regression: 79/79 on Chrome and 79/79 on Edge.
- Java Selenium companion: 8/8 using Java 17 and Selenium 4.51.0.
- Selenium IDE: 30/30 through the official replay engine.
- Focus/session/boundary exploration: 8/8 after the dialog keyboard wrap fix.
- Six clean business PDFs, STQA report, CSV/HTML/JSON results, screenshots and a portable source/report package prepared.
- Student/roll/college/faculty cover fields await the user's details.
