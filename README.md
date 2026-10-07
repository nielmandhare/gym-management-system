# Gym Management System

Gym Management System is a desktop-based gym management application built with Java Swing, JDBC, and MySQL. It gives gym administrators one place to review member and trainer records, record attendance, follow progress, inspect equipment and product records, view payment and sales information, and open operational reports.

## Project Overview

The project demonstrates how a small gym can keep common operational records in a relational database and work with them through a desktop interface. It is intended for an administrator or front-desk operator. A dashboard summarizes activity, while dedicated screens provide member, trainer, attendance, equipment, product, payment, workout-plan, and progress views. Member, trainer, and equipment records have management dialogs; other record screens focus mainly on viewing and searching.

For a college mini-project, the application illustrates a graphical client connected to a MySQL database through JDBC. It is a desktop program, not a web application.

## Features

The following features are present in the source and project resources:

- Administrator sign-in using an account in the `admin` table. The sign-in field accepts the admin username or mobile number, and inactive accounts are rejected.
- Dashboard totals for members, trainers, daily attendance, monthly payments, and product sales.
- Member records with add, edit/status, and remove workflows; membership type and an initial progress record are saved when a member is created.
- Trainer records with add, edit/status, and remove workflows.
- Member attendance recording with member-ID validation and duplicate check-in prevention, plus attendance history and summary values.
- Equipment inventory management with add, edit, and delete actions. Equipment and maintenance records are also available to the equipment report and summary.
- Membership types and status values used by member records.
- Payment-history and product/sales records with searchable list views and summary values. The current screens primarily display and search these records; they do not provide a full payment or sales-entry workflow.
- Workout-plan and member progress-history views with search. The member form records starting height, weight, body-fat percentage, and BMI as a progress entry.
- Search fields with database-backed suggestions in the main data panels.
- Six JasperReports templates, opened in JasperViewer, for members, trainers, member attendance, payments, product sales, and equipment.
- A database backup action that invokes `mysqldump` and writes a timestamped SQL file in `backups/`.
- Local logging, input validation, and a FlatLaf dark theme.

## Technology Stack

| Technology | Use in this project | Version/configuration in project |
|---|---|---|
| Java | Application language | Source and target level 17 |
| Java Swing | Desktop user interface | JDK UI toolkit |
| JDBC | Database access | MySQL Connector/J 8.4.0 |
| MySQL | Relational database | MySQL Community Server 8.0.46 in the verified local setup |
| Apache Ant | Build system | Build script downloads Ant 1.10.15 launcher JARs locally when absent |
| NetBeans project metadata | Project and Ant wiring | `nbproject/` |
| FlatLaf | Swing look and feel | FlatLaf 3.6, Mac Dark theme |
| JasperReports | Compiled reports and viewer | JasperReports 6.21.3 |
| OpenPDF / iText libraries | PDF/report dependencies | OpenPDF 1.3.30 and vendored iText compatibility JARs |
| Other vendored libraries | Calendar, SVG, barcode, and report dependencies | See `lib/` and `nbproject/project.properties` |

## System Architecture

```text
Administrator
     ↓
Java Swing windows and panels
     ↓
UI event handlers, dialogs, and application logic
     ↓
connection.MySQL (JDBC, prepared queries)
     ↓
MySQL database (`gms`)
```

The code is organized around Swing windows, feature panels, dialogs, and a shared database utility. It is not structured as a formal MVC framework.

- `gui.LoginForm` authenticates an administrator. `gui.MainInterface` builds the main window, dashboard, navigation, and feature panels; `gui.SplashSceen` is the configured entry point.
- `panels/` contains the dashboard modules and report screen. `panels/dialog/` contains focused dialogs for member, trainer, attendance, and equipment workflows.
- `connection.MySQL` loads database settings, opens the JDBC connection, executes prepared SQL statements, and runs the backup command.
- `util/` and `validation/` contain shared UI, logging, date/time, and input-validation helpers.
- `src/report/` contains compiled JasperReports templates.

## Project Structure

```text
.
├── backups/                  # Schema, demo seed, and generated SQL backups
├── lib/                      # Vendored runtime and build dependencies
├── nbproject/                # NetBeans project settings and Ant build metadata
├── src/
│   ├── connection/           # JDBC connection and database operations
│   ├── gui/                  # Splash, login, and main application window
│   ├── panels/               # Feature screens and dialogs
│   ├── report/               # Compiled JasperReports templates
│   ├── resources/            # Configuration examples and app resources
│   ├── util/                 # Shared helpers
│   └── validation/           # Input validation rules
├── test/                     # Swing/database integration verification harness
├── work/                     # Local verification scratch output
├── Build.ps1                 # Local Ant build launcher
├── Run.ps1                   # Database setup, build, and application launcher
├── Setup-Database.ps1        # Dedicated local MySQL setup and SQL import
├── Stop-Database.ps1         # Stops the dedicated local MySQL instance
├── Verify.ps1                # Runs the integration verification harness
├── AGENTS.md                 # Clone/setup guidance and safety rules for AI agents
├── build.xml                 # NetBeans Ant project entry point
├── manifest.mf               # JAR manifest input
├── PASSWORDS.txt             # Local-only credential reference; ignored by Git
└── README.md
```

Generated folders such as `.local/`, `build/`, `dist/`, `logs/`, and `work/` contain local database, build, log, or verification artifacts and are ignored by Git.

## Database

The application uses MySQL database `gms`. The checked local development setup binds it to `127.0.0.1:3307`; this is a dedicated instance separate from a typical MySQL service on port 3306. The JDBC driver is MySQL Connector/J 8.4.0 (`com.mysql.cj.jdbc.Driver`).

The schema is defined in [`backups/gms-database-init.sql`](backups/gms-database-init.sql) and contains 15 tables: `admin`, `equipment`, `maintenance_log`, `member`, `membership`, `member_attendance`, `order_history`, `payment_history`, `personal_training`, `product`, `progress_track`, `status`, `trainer`, `trainer_attendance`, and `workout_plan`.

Important relationships include members and their attendance, progress and payment records; trainers and trainer attendance; products and order history; and equipment and maintenance logs. Foreign keys in the schema connect related records.

**Local development/demo database credentials only:**

| Account | Username | Password | Purpose |
|---|---|---|---|
| Application database account | `gms_dev` | `1234` | Application access to the local `gms` database |
| Local MySQL maintenance account | `root` | `1234` | Local instance setup and SQL import |

These values are intentionally for a local demo. Do not reuse them for a real deployment; set new credentials and protect configuration files. The database setup script binds the dedicated server to localhost.

## Demo Login

These are demonstration credentials for the seeded administrator account:

| Login field | Value |
|---|---|
| Username | `admin` |
| Password | `1234` |
| Mobile login | `0770000001` |
| Password for mobile login | `1234` |

The login form accepts either the username or mobile number with the password.

## Demo Data

The idempotent local seed script populates realistic Indian demo records. Names in the dataset include Vedant, Shruti, Niel, Tanisha, Atharva, Tasbiya, and Pradynesh. Seeded data covers members, trainers, attendance, membership types, payments, progress records, workout plans, equipment, maintenance, products, sales/orders, and related records.

The saved integration verification checked the seeded database and confirmed 25 members, 6 trainer profiles, 75 progress entries, 6 workout plans, 12 equipment records, 7 maintenance records, 8 products, 20 sales/order records, and 19 payment records. Attendance volume is checked by the harness rather than stated here as a fixed count.

## Requirements

- Windows with PowerShell (the supplied setup and run scripts are PowerShell scripts).
- JDK 17 or newer, with both `java` and `javac` available on `PATH`.
- MySQL Server 8.x binaries, including `mysqld`, `mysql`, and `mysqladmin`. The setup script defaults to `C:\Program Files\MySQL\MySQL Server 8.0\bin` and initializes an isolated data directory under `.local/`.
- Internet access the first time `Build.ps1` runs, so it can download the two Apache Ant 1.10.15 launcher JARs if they are not already present.

Apache Ant does not need to be installed globally for the supplied scripts. NetBeans is optional; the project includes NetBeans metadata. The verified environment was JDK 17.0.12 and MySQL Community Server 8.0.46.

## Installation / Setup

From PowerShell, clone the repository and enter the project directory:

```powershell
$repositoryUrl = Read-Host 'Paste the repository URL'
$checkoutPath = Read-Host 'Enter a new checkout directory'
git clone $repositoryUrl $checkoutPath
Set-Location -LiteralPath $checkoutPath
```

Then initialize the local database and demo data, build, and run:

> **Port requirement:** The local MySQL instance uses `127.0.0.1:3307`. `Setup-Database.ps1` checks the listener before connecting: it proceeds only when the listener is this checkout's own `mysqld.exe` using this checkout's `.local/mysql/my.ini`. If another process or MySQL service is listening there, setup stops without connecting to it. Resolve the conflict yourself or reconfigure the project port before running `Setup-Database.ps1` or `Run.ps1`; the scripts do not automatically choose another port.

```powershell
powershell -ExecutionPolicy Bypass -File .\Setup-Database.ps1
powershell -ExecutionPolicy Bypass -File .\Build.ps1
powershell -ExecutionPolicy Bypass -File .\Run.ps1
```

`Run.ps1` runs the setup and build scripts before launching the JAR, so the first command can be skipped if using `Run.ps1` directly. Running setup separately is useful when preparing the database before building. The MySQL installation path can be supplied to setup with `-MySqlBin`, for example:

```powershell
powershell -ExecutionPolicy Bypass -File .\Setup-Database.ps1 -MySqlBin 'D:\MySQL\bin'
```

The normal `Run.ps1` uses the setup script's default MySQL path. If MySQL is installed elsewhere, update that invocation in `Run.ps1` or run the setup with the correct path before building and launching.

The setup script creates `src/resources/db.properties` and `src/resources/mysql.cnf` only if they do not already exist. It imports the base schema and local demo seed into the dedicated local instance. Keep local credential files out of public repositories and change the demo values before any non-local use.

## Quick Start

For a teacher who has the required JDK and MySQL Server installed, open PowerShell in the project directory and run:

```powershell
Set-Location -LiteralPath '<project-directory>'
powershell -ExecutionPolicy Bypass -File .\Run.ps1
```

Sign in with the demo login `admin` / `1234` (or mobile `0770000001` / `1234`).

For an AI-agent-friendly replication checklist that avoids the original machine's paths and checks for port conflicts before setup, see [`AGENTS.md`](AGENTS.md).

## Database Scripts

- `Setup-Database.ps1` initializes the dedicated local MySQL data directory on first run, writes local MySQL configuration, starts the server if needed, creates the `gms_dev` account, and imports the schema and demo seed. It uses `CREATE TABLE IF NOT EXISTS` plus an idempotent seed designed to be rerun without duplicating demo rows; canonical demo rows may be updated while their established IDs and relationships are preserved. It uses local demo passwords shown above.
- `Stop-Database.ps1` asks the dedicated local MySQL instance to shut down using its local root option file. It does not target the separate system MySQL service.
- `backups/gms-database-init.sql` creates the `gms` database and its schema with 15 tables. It is safe to re-run against the dedicated demo instance because the tables are created conditionally; it does not populate the demo dataset.
- `backups/local-demo-seed.sql` inserts/updates the lookup and realistic demo records while preserving established IDs and relationships. It is intended to be rerun as part of setup.

## Build and Run Scripts

- `Build.ps1` downloads Ant 1.10.15 launcher JARs into `.local/tools/` when needed, then runs the NetBeans Ant `clean jar` target. It produces `dist/Gym_Management_System.jar` and its runtime libraries.
- `Run.ps1` runs database setup, builds the project, and starts the application JAR.
- `Verify.ps1` runs setup and build, compiles `test/LocalVerification.java`, and runs the Swing/database integration verification harness. It uses temporary records and transactional checks; see the testing section.
- `Stop-Database.ps1` stops the isolated development MySQL server. Use it when finished with the local database.

## Testing and Verification

The saved integration run in `outputs/verification.txt` records **86 passing assertions**. The harness in `test/LocalVerification.java` exercises the Swing forms and the local database. Recorded checks include:

- Clean Ant build and JDBC connectivity.
- Seeded demo data and mobile-number validation.
- Valid administrator login, invalid password, SQL-injection-like login input, inactive-admin rejection, and mobile-number login.
- Dashboard loading and navigation through the main panels.
- Search and refresh for the member, trainer, workout-plan, attendance, progress, equipment, product, and payment panels.
- Member, trainer, attendance, and equipment create/update/delete behavior, including validation and related-record cleanup.
- Report data, JasperViewer display, report filling, and PDF generation through JasperReports.
- Database backup, reads and writes, and empty product/equipment table handling.
- No unexpected error dialogs or severe log messages during that run.

These results describe the recorded local verification run; they are not a guarantee for a different machine or database configuration. `Verify.ps1` may create temporary local report and dashboard files under `work/`.

## Reports

The application includes six compiled JasperReports templates in `src/report/`:

- `gms_member_report.jasper`
- `gms_trainer_report.jasper`
- `gms_member_attendance_report.jasper`
- `gms_payments_report.jasper`
- `gms_product_sales_report.jasper`
- `gms_equipment_report.jasper`

The Generate Reports panel fills the selected template from the current JDBC connection and opens it in JasperViewer. The integration harness also fills the templates and exports their output to PDF using JasperReports APIs.

## Security / Configuration Notes

- All passwords documented here are local development/demo credentials, not production secrets. Change them before any deployment beyond a local demonstration.
- The dedicated local MySQL configuration binds to `127.0.0.1`; it is not a remote database configuration.
- `PASSWORDS.txt` contains local credentials and is listed in `.gitignore`. Git also ignores `src/resources/db.properties`, `src/resources/mysql.cnf`, `.local/`, `build/`, `dist/`, and `logs/`.
- This README intentionally documents the local database and demo application credentials requested for the project handoff. Anyone publishing the repository should understand that these are demo-only values and must not be copied into a real environment.
- Local database files, logs, build outputs, and generated backups are development artifacts. Review generated SQL backups before sharing them because they can contain the full demo database.
- The application login uses a plain password comparison against the `admin` table. The included credentials and current authentication approach are for coursework/demo use, not a production identity system.

## Troubleshooting

- **Java version/build error:** Install JDK 17 or newer and confirm `java -version` and `javac -version` work in PowerShell. `Build.ps1` needs both executables.
- **PowerShell blocks a script:** Run the supplied command with `-ExecutionPolicy Bypass`, as shown above, or use an execution policy allowed by your system.
- **Ant dependencies fail to download:** `Build.ps1` fetches Ant 1.10.15 from Maven Central the first time. Check internet access and retry, or provide the required Ant JARs under `.local/tools/`.
- **MySQL binaries not found:** Install MySQL Server 8.x or pass the correct binary folder to `Setup-Database.ps1 -MySqlBin`. `Run.ps1` uses the default path unless its setup invocation is adjusted.
- **Port 3307 is unavailable / MySQL does not start:** Check `.local/mysql/server.log` and ensure another process is not using port 3307. The dedicated server is configured for `127.0.0.1` and does not use the standard `MYSQL80` service on port 3306.
- **JDBC connection failure:** Run `Setup-Database.ps1`, check the generated `src/resources/db.properties` values, and confirm the local MySQL process is listening on `127.0.0.1:3307`.
- **Application does not start:** Run `Build.ps1` first and inspect `logs/logger.log`; launch from the project root so relative configuration and backup paths resolve.
- **Login fails:** Confirm setup imported the demo seed and use `admin` / `1234` or `0770000001` / `1234`. The seeded account must remain active.
- **Backup fails:** Confirm the MySQL client tools were found and the generated `src/resources/mysql.cnf` credentials work. Backups are written under `backups/` relative to the project root.

## Known Limitations

- This is a Windows-oriented Swing desktop application and a local MySQL setup, not a hosted or distributed service.
- Member, trainer, attendance, and equipment workflows include user-facing record management. Workout plans, progress history, payment history, and product/sales records are mainly view/search screens; they do not all have full CRUD or transaction-entry flows.
- The schema includes trainer login-like columns, but application authentication is implemented for administrator records; trainer rows in the seeded data are profiles, not demonstrated application accounts.
- The demo MySQL instance, shared demo passwords, and plain admin password storage are suitable only for coursework/local demonstrations.

## UML / OOMD Mini Project

This repository is being used as a college Object-Oriented Modeling and Design (OOMD) / UML mini-project. The system can be modeled using the following seven diagrams; the diagrams themselves are not included here:

1. Use Case Diagram
2. Class Diagram
3. Sequence Diagram
4. Activity Diagram
5. State Machine Diagram
6. Component Diagram
7. Deployment Diagram

## Future Improvements

The following are ideas for future work and are not claims about current functionality:

- Role-based authentication and secure password hashing.
- A remotely hosted database with production-grade credentials and deployment configuration.
- Online membership payments and payment-entry workflows.
- Email or SMS notifications.
- A mobile application, multi-branch support, and broader analytics.
- More complete payment, sales, workout-plan, and progress-record management.

## License / Academic Use

Developed as an academic mini-project. No open-source license is included in the repository.

## Author / Team

The repository does not identify an author or project team by name. This README uses the neutral project attribution: **Academic Mini Project — Gym Management System**.
