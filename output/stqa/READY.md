# Gym web project and STQA submission

Open the running project at **http://127.0.0.1:8081**.

Demo usernames are **admin**, **trainer**, and **user1**; password **123**. Existing edited passwords are preserved by setup. The web application uses this checkout's own MySQL database on port 3307.

## Deliverables

- `Gym_Management_System_STQA_Report.pdf`: remade in the supplied plain STQA format, using Times text and full black borders around all test tables; includes the test plan, taxonomy, 79 detailed cases, execution evidence and screenshots.
- `selenium_chrome_results.json/csv`, `selenium_edge_results.json/csv`, corresponding HTML reports, and `selenium_ide_results.json`: machine-readable and browser-readable execution evidence.
- `selenium_java_results.json`: executed Java Selenium smoke checks; source and launcher are included.
- `requirements_traceability.csv`: each detailed case mapped to a requirement, procedure, expected result, and Chrome/Edge outcome.
- `exploratory_results.json`: independent sessions, keyboard/dialog handling, mobile dismissal and numeric boundaries; final 8/8 checks passed.
- `screenshots/`: actual final GUI and code/output screenshots.
- `gym-reports/`: all six clean business report PDFs exported after fixture cleanup.
- `../Gym_Web_STQA_Submission.zip`: source, scripts, dependencies and requested report artifacts. It excludes private local connection files, MySQL data, seed markers, generated SQL backups, application logs, build outputs and caches.

## Final results

**79/79 Chrome + 79/79 Edge + 30/30 Selenium IDE replay + 8/8 focused checks + 8/8 Java WebDriver checks (204 executions, zero final failures).** Test records were cleaned up. The six PDFs, filtered CSV and SQL backup workflows were verified.

The original desktop project and its report templates remain available. The browser report layouts reproduce the same six report datasets using OpenPDF so long values cannot overlap adjacent rows.

Fill the first page's student names, roll numbers, college/class and faculty details before submitting. Full run and verification instructions are in `web/README.md`; the supplied `.side` file can also be imported into the Selenium IDE graphical application. The recorded IDE execution used its official command-line runner.

For the faculty demonstration, use `PROJECT_DEMO_GUIDE.md` in the project root. `Start-Demo.ps1 -OpenBrowser` reuses the running app and starts missing project services after ownership checks. Student details are now on the report's first page under Prepared By.
