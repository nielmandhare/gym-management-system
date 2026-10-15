# Gym Management System — Web edition

This is the browser alternative to the existing Java Swing project. It uses Java 17's HTTP server, JDBC/MySQL, the existing `app_user` roles and database schema, and the original six report datasets. HTML, CSS, and JavaScript provide the responsive interface. No frontend build service is required.

## Start on Windows

1. Install JDK 17+, MySQL Server 8.x binaries, and Chrome or Edge.
2. Follow `AGENTS.md` and check MySQL port 3307. An occupied port may be used only when the listener is this checkout's own mysqld with this checkout's `.local/mysql/my.ini`.
3. Run `powershell -NoProfile -ExecutionPolicy Bypass -File .\Setup-Database.ps1` from the checkout. Use `-MySqlBin` if needed.
4. Run `powershell -NoProfile -ExecutionPolicy Bypass -File .\Run-Web.ps1 -Background`.
5. Open **http://127.0.0.1:8081**. Stop this checkout's background server with `Stop-Web.ps1`.

`Run-Web.ps1 -Port 8090` selects another web port without changing the database port. The database remains 127.0.0.1:3307. Connection settings come from the local, ignored `src/resources/db.properties`. Startup keeps Java selector socket files under `.local/java-sockets` and puts OpenPDF before legacy iText libraries for PDF compatibility.

Fresh demo accounts: Admin `admin` / `123`, Trainer `trainer` / `123`, Member `user1` / `123`. Setup preserves edited passwords; never reset an existing account just to run tests. These credentials are for the local college demo.

For a classroom demonstration, see `PROJECT_DEMO_GUIDE.md` in the project root. `Start-Demo.ps1 -OpenBrowser` checks port ownership, starts missing project services and opens an already running project without stopping it.

## Feature mapping

| Desktop feature | Browser workflow |
|---|---|
| Common role login | Shared sign-in; separate session and navigation for each role |
| Dashboard totals | Members, trainers, daily attendance, monthly payments and product sales; genuine weekly activity |
| Members | Create with initial measurements/BMI, edit, status, delete, membership and paid/due flag |
| Trainers | Create profile and login together; edit, status and guarded deletion |
| Attendance | Named active-member choice, duplicate prevention, paid/due feedback and searchable history |
| Memberships | Assignment through member form; plan-price editing |
| Equipment | Create/edit/delete, condition, serial number, purchase and warranty dates |
| Products | Create/edit/delete, stock and price; referenced sales protect deletion |
| Payment/sales history | Searchable lists and CSV reports; inherited read-only workflow |
| Workout plans | List and seven-day detail dialog |
| Progress | Admin history; Trainer writes only for active assigned members; Member reads own entries |
| User management | Three roles, unique linked profiles, active state, password reset, last-admin/self protection |
| Trainer dashboard | Own profile/attendance, assigned members and attendance/progress/training records |
| Member dashboard | Own profile/membership, attendance, measurements, training; own profile/password updates |
| Six original report types | Readable OpenPDF exports from Reports; original Jasper templates kept for the desktop |
| Database backup | Timestamped local SQL backup; Admin only |

Forms use named choices and inline errors. Tables search all loaded rows, paginate, and export filtered CSV. The interface supports light/dark themes and mobile navigation. All mutations use prepared SQL; paired record changes are transactions; role/profile activity is rechecked on authenticated requests.

## Selenium regression

Python 3.10+ and an installed Chrome/Edge are required. Install dependencies inside the checkout:

```powershell
python -m pip install --cache-dir .local/pip-cache --target .local/selenium -r web/tests/requirements.txt
powershell -NoProfile -ExecutionPolicy Bypass -File .\Verify-Web.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\Verify-Web.ps1 -Browser edge
```

Selenium Manager obtains the matching driver in `.local/selenium-cache`. The suite verifies the database data-directory path before creating unique test records, cleans up only those fixture IDs, and leaves existing demo passwords/profile values intact. Results, CSV, HTML and screenshots are saved in `output/stqa/runs/`.

For Selenium IDE replay, install Node.js/npm and then:

```powershell
npm install --cache .local/npm-cache --prefix .local/selenium-ide selenium-side-runner@4.0.13
powershell -NoProfile -ExecutionPolicy Bypass -File .\Run-SeleniumIDE.ps1
```

The importable project is `web/tests/Gym_STQA.side`. The local wrapper handles Windows paths containing spaces while using the official runner's existing execution engine. Its 30 cases are read-only navigation, role and login regression checks. JSON evidence is saved in `output/stqa/ide/`.

## Java Selenium companion

The full detailed regression is complemented by eight executed Java WebDriver smoke cases. To demonstrate Java tests after obtaining ChromeDriver with the Python suite:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\Verify-WebJava.ps1
```

This downloads Selenium 4.51.0 from the official release into `.local/selenium-java`, compiles the source with JDK 17, and writes `output/stqa/selenium_java_results.json`. Supply `-DriverPath` for a different matching ChromeDriver. A CDP-version warning may appear; these tests use standard WebDriver commands. The source is `web/tests/java/src/main/java/GymWebSmoke.java`.

## Report and evidence

`web/tools/build_stqa_report.py` builds the STQA PDF from successful execution JSON and screenshots. `web/tools/capture_evidence.py` captures the actual browser screens and displays saved code/result excerpts. `web/docs/DEFECTS.md` distinguishes application defects, environment problems and test harness corrections. The generated requirements_traceability.csv maps all detailed cases to requirement groups and outcomes. The first report page has fill-in fields for the student names, roll numbers and faculty details.

To regenerate the evidence/report with Python 3.10+, install its dependencies locally, capture the running app and build the report:

```powershell
python -m pip install --cache-dir .local/pip-cache --target .local/report-tools -r web/tools/requirements.txt
python web/tools/capture_evidence.py
python web/tools/build_stqa_report.py
python web/tools/package_submission.py
```

Run the tests first when collecting new evidence. The build requires successful Chrome, Edge, IDE, Java and focused results. `web/tests/exploratory_checks.py` supplies the focused checks. The supplied `.side` file is importable in Selenium IDE; the recorded results use its official command-line runner.

This is a Windows local demo. Payment transaction entry, product order entry and creation of training assignments were not part of the existing desktop workflows and are not presented as newly implemented features. No Internet deployment or production security/load assurance is claimed.
