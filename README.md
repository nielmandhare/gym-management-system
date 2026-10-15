# Gym Management System

Gym Management System is an OOMD college mini-project built with Java Swing, JDBC, and MySQL. It provides separate Admin, Trainer, and Member dashboards through one login flow. Administrators manage gym operations, trainers work with assigned members, and members view their own records.

## Browser-based edition (STQA assignment)

The browser edition replicates the current gym workflows using Java 17, JDBC, MySQL and a responsive HTML/CSS/JavaScript interface. Detailed setup, feature mapping and Selenium instructions are in [web/README.md](web/README.md).

After following the port-3307 ownership checks in AGENTS.md and setting up this checkout's dedicated MySQL instance:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\Run-Web.ps1 -Background
```

Open **http://127.0.0.1:8081**. `-Port` selects a different web port. `Stop-Web.ps1` stops only the recorded server for this checkout. Local database credentials remain in ignored `src/resources/db.properties`; existing demo passwords are preserved. Fresh demo logins are admin / 123, trainer / 123 and user1 / 123.

The web edition includes all three role workspaces, original dashboard totals, searchable/paginated module tables and summaries, member/initial-progress creation, trainer/login creation, membership management, attendance, equipment, products, scoped progress, user access controls, own-profile/password changes, CSV exports, six clean PDF report downloads and local SQL backups. Payment/sales entry and editing workout/training assignments were not current desktop workflows; their existing read-only views are retained.

`Verify-Web.ps1` runs the Selenium WebDriver regression on Chrome or Edge using isolated test records. `Run-SeleniumIDE.ps1` replays the importable 30-case `web/tests/Gym_STQA.side` project through the official runner. Results and the generated STQA report are under `output/stqa/`. Student and faculty cover fields should be filled before submission.

## Project Overview

The project stores gym records in a relational database and exposes them through role-specific desktop interfaces. The Admin Dashboard summarizes activity and provides member, trainer, attendance, equipment, product, payment, workout-plan, progress, and reporting modules. Trainer and Member dashboards restrict records and actions using the authenticated account and its linked profile.

Trainers and members use the same login window and receive separate dashboards scoped to their linked profiles. For a college mini-project, the application illustrates a graphical client connected to a MySQL database through JDBC. The original Swing edition is a desktop program; the browser alternative is documented above.

## Features

The following features are present in the source and project resources:

- Admin, Trainer and Member sign-in using `app_user` accounts. The sign-in field accepts username or mobile number; inactive accounts and profiles are rejected.
- Admin User Management: list/search, create, edit, assign all three roles, activate/deactivate, and reset passwords. Trainer/member accounts require a unique corresponding profile link.
- Trainer Dashboard: own profile/attendance, assigned members, member attendance/progress and personal-training sessions; record progress for an active assigned member, edit own name/mobile, change password, and log out.
- Member Dashboard: own profile/membership, attendance, progress and training history; edit own name/mobile, change password, and log out.
- Admin membership assignment/payment status and plan price management; product create, edit and delete, with related sales protected by foreign keys.
- Dashboard totals for members, trainers, daily attendance, monthly payments, and product sales.
- Member records with add, edit/status, and remove workflows; membership type and an initial progress record are saved when a member is created.
- Trainer records with add, edit/status, and remove workflows.
- Member attendance recording with member-ID validation and duplicate check-in prevention, plus attendance history and summary values.
- Equipment inventory management with add, edit, and delete actions. Equipment and maintenance records are also available to the equipment report and summary.
- Membership types and status values used by member records.
- Payment-history and product/sales records with searchable list views and summary values. The current screens primarily display and search these records; they do not provide a full payment or sales-entry workflow.
- Workout-plan and member progress-history views with search. The member form records starting height, weight, body-fat percentage, and BMI as a progress entry.
- Search fields with database-backed suggestions in the main data panels.
- Six desktop JasperReports templates, opened in JasperViewer, for members, trainers, member attendance, payments, product sales, and equipment.
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
Admin / Trainer / Member
     ↓
Common login → trusted database role → role-specific Swing dashboard
     ↓
Session authorization, account operations, and scoped queries
     ↓
Existing feature panels/dialogs + connection.MySQL (JDBC)
     ↓
MySQL database (`gms`)
```

The code is organized around Swing windows, feature panels, dialogs, and a shared database utility. It is not structured as a formal MVC framework.

- `gui.LoginForm` authenticates all roles and routes using the trusted database role. `gui.MainInterface` builds the administrator window; `gui.TrainerDashboard` and `gui.MemberDashboard` provide the other role interfaces. `gui.SplashSceen` is the configured entry point.
- `panels/` contains the admin modules, User Management, and report screen. `panels/dialog/` contains member, trainer, attendance, equipment, product, and membership management dialogs.
- `connection.MySQL` loads database settings, opens the JDBC connection, executes prepared SQL statements, and runs the backup command.
- `security.Accounts`, `security.Session`, and `security.Passwords` handle account operations, role authorization, scoped data and password hashing. `panels.UserManagementPanel` is admin-only.
- `util/` and `validation/` contain shared UI, logging, date/time, and input-validation helpers.
- `src/report/` contains compiled desktop JasperReports templates.

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
│   ├── report/               # Compiled desktop JasperReports templates
│   ├── resources/            # Configuration examples and app resources
│   ├── security/             # Account service, sessions and password hashing
│   ├── util/                 # Shared helpers
│   └── validation/           # Input validation rules
├── test/                     # Swing/database integration verification harness
├── work/                     # Local verification scratch output
├── Build.ps1                 # Local Ant build launcher
├── Run.ps1                   # Database setup, build, and application launcher
├── Setup-Database.ps1        # Dedicated local MySQL setup and SQL import
├── Stop-Database.ps1         # Stops the dedicated local MySQL instance
├── Verify.ps1                # Runs all three Swing/MySQL verification harnesses
├── AGENTS.md                 # Clone/setup guidance and safety rules for AI agents
├── build.xml                 # NetBeans Ant project entry point
├── manifest.mf               # JAR manifest input
├── PASSWORDS.txt             # Local-only credential reference; ignored by Git
└── README.md
```

Generated folders such as `.local/`, `build/`, `dist/`, `logs/`, and `work/` contain local database, build, log, or verification artifacts and are ignored by Git.

## Database

The application uses MySQL database `gms`. The checked local development setup binds it to `127.0.0.1:3307`; this is a dedicated instance separate from a typical MySQL service on port 3306. The JDBC driver is MySQL Connector/J 8.4.0 (`com.mysql.cj.jdbc.Driver`).

The base schema in [`backups/gms-database-init.sql`](backups/gms-database-init.sql) contains 15 tables: `admin`, `equipment`, `maintenance_log`, `member`, `membership`, `member_attendance`, `order_history`, `payment_history`, `personal_training`, `product`, `progress_track`, `status`, `trainer`, `trainer_attendance`, and `workout_plan`. [`backups/user-accounts-migration.sql`](backups/user-accounts-migration.sql) adds `app_user`. [`backups/three-role-migration.sql`](backups/three-role-migration.sql) extends that account table with the Member role, unique member-profile link, and role/profile constraint. The total remains 16 tables.

Important relationships include members and their attendance, progress and payment records; trainers and trainer attendance; products and order history; and equipment and maintenance logs. Foreign keys in the schema connect related records.

**Local development/demo database credentials only:**

| Account | Username | Password | Purpose |
|---|---|---|---|
| Application database account | `gms_dev` | `1234` | Application access to the local `gms` database |
| Local MySQL maintenance account | `root` | `1234` | Local instance setup and SQL import |

These values are intentionally for a local demo. Do not reuse them for a real deployment; set new credentials and protect configuration files. The database setup script binds the dedicated server to localhost.

## Demo Login

These accounts are created for local demonstrations. Application password hashes are stored in `app_user`; the local ignored `PASSWORDS.txt` contains the handoff credentials.

| Role | Username | Password | Profile / mobile |
|---|---|---|---|
| Admin | `admin` | `123` | admin / `0770000001` |
| Trainer | `trainer` | `123` | trainer / `9876512001` |
| Member | `user1` | `123` | user1 / `9876501002` |

The login form accepts either the username or mobile number with the password. The three demo usernames and profile names are `admin`, `trainer`, and `user1`, all with password `123`. New passwords may contain 3–128 characters. Database connection credentials are listed separately above. Setup preserves existing accounts and changed passwords; these defaults apply to a fresh demo installation.

## User Management and Role Dashboards

Sign in as admin, then open **User Management** in the sidebar. Use **New / Clear**, enter a username, name, 10-digit mobile, role, active status, and a password of 3–128 characters. Choose a trainer profile for Trainer or member profile for Member. Click **Save User**. Select a table row to edit account details/status/role, or choose **Reset Password**; stored passwords are never displayed. Usernames, account mobile numbers, and corresponding profile links must be unique. Role changes clear the old profile link and require the new one. The current admin cannot disable or demote their own account, and at least one active admin is retained.

To create a new trainer profile and account together, use the existing **Trainer Management** add form. Its username/password fields create a linked Trainer account in the same transaction. To create a member, first use **Members → Manage** to create their profile and initial progress, then create a linked Member account in User Management. Existing profiles can also receive accounts through User Management. Profile-only trainer seed values do not grant a login.

A trainer signs in using their account username/mobile and password. The Trainer Dashboard shows only their own profile/attendance and members, attendance, progress, and sessions linked through `personal_training`. Historical assignments remain readable; **Record Progress** requires an ACTIVE assignment and an active member, rechecked by the service before writing. Enter date, height, weight and body fat; BMI is computed from the measurements. **Edit My Profile**, **Change Password**, and **Log Out** are available. Admin modules, backups, other trainer profiles and role changes are denied.

A member receives **Profile / Membership**, **My attendance**, **My progress**, and **My training** tabs. Every query derives the member ID from the authenticated session; the member cannot supply another profile ID. Own name/mobile and password can be changed. Member sessions cannot access trainer data, administrative dialogs or unrestricted legacy SQL operations.

For admin membership management, select a member in **Members** and click **Membership**. Save their membership and payment flag, or update the selected plan price for all members using that plan. This flag does not create payment transactions. Use **Other Products → Manage** for inventory CRUD; products referenced by sales history cannot be deleted.

## Actual Object Model and Relationships (OOMD)

The application uses role-associated profiles rather than artificial Admin/Trainer/Member subclasses. `Session.Identity` is an immutable account identity containing the user ID, trusted role and permitted profile ID. `Session` encapsulates the current login and permission checks; `Accounts` encapsulates account transactions and scoped operations; `Passwords` encapsulates hashing. Swing windows compose tabbed panes, panels and tables and inherit the actual Swing frame/panel/dialog classes. No Java domain class is claimed for a table that is represented by JDBC rows.

- Each `app_user` has exactly one role. A Trainer account links to exactly one `trainer`; a Member account links to exactly one `member`. Each profile has zero or one login account. Admin accounts have no trainer/member link; a legacy admin may link to its original `admin` row.
- Each member has one membership type. One membership type serves many members. A member has zero or many attendance, progress and payment records; every such record belongs to one member.
- Each personal-training record belongs to one trainer and one member. A trainer and a member can each have many training records, forming the existing assignment association. Trainer attendance belongs to one trainer.
- Products have many sales records and equipment has many maintenance records. Existing foreign keys govern lifecycle: profile deletion cascades its account and dependent records; lookup references and products with sales are protected.
- Account active/inactive state and profile active/inactive state are both checked. Password reset changes the hash; role changes replace the allowed profile association and invalidate an existing session at its next guarded operation. Login creates a session and logout clears it. Existing training states are ACTIVE, COMPLETED and CANCELLED.

These are the actual code/database associations suitable for UML. There is no member-to-workout-plan relationship in the schema; **My training** displays existing trainer assignments/session counts, while progress shows saved fitness measurements.

Passwords use JDK-native PBKDF2-HMAC-SHA256 with 600,000 iterations, a random 16-byte salt and a 256-bit derived key; no extra dependency is required. Legacy admin passwords are converted transactionally to hashes in `app_user`, with the old `admin.password` replaced by `MIGRATED`. Fresh setup creates the demo account hashes directly. Normal migration/setup preserves existing hashes. Existing admin/profile status flags are still respected. Keep the pre-migration SQL backup for recovery; re-running the migration does not create duplicate accounts.

## Demo Data

The local seed script populates realistic Indian demo records including Shruti, Niel, Tanisha, Atharva, Tasbiya, and Pradynesh. The three login profiles use the simple names `admin`, `trainer`, and `user1`. Seeded data covers members, trainers, attendance, membership types, payments, progress records, workout plans, equipment, maintenance, products, sales/orders, and related records.

The saved integration verification checked the seeded database and confirmed 25 members, 6 trainer profiles, 75 progress entries, 6 workout plans, 12 equipment records, 7 maintenance records, 8 products, 20 sales/order records, and 19 payment records. Attendance volume is checked by the harness rather than stated here as a fixed count.

## Requirements

- Windows with PowerShell (the supplied setup and run scripts are PowerShell scripts).
- JDK 17 or newer, with both `java` and `javac` available on `PATH`.
- MySQL Server 8.x binaries, including `mysqld`, `mysql`, and `mysqladmin`. The setup script defaults to `C:\Program Files\MySQL\MySQL Server 8.0\bin` and initializes an isolated data directory under `.local/`.
- Internet access the first time `Build.ps1` runs, so it can download the two Apache Ant 1.10.15 launcher JARs if they are not already present.

Apache Ant does not need to be installed globally for the supplied scripts. NetBeans is optional; the project includes NetBeans metadata. The verified environment was JDK 17.0.12 and MySQL Community Server 8.0.46.

## Installation / Setup

**Replication requirement:** Every machine must run an independent checkout with its own generated configuration and local database. Do not rely on the original developer's username, Desktop path, `.local/` directory, credentials files, or build outputs. The scripts resolve project paths relative to the checkout. Windows/JDK/MySQL prerequisites still need to be installed on the destination machine; see [`AGENTS.md`](AGENTS.md) for the agent setup checklist.

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
java -jar .\dist\Gym_Management_System.jar
```

Alternatively, run only `Run.ps1`, which performs all three steps. Close any running copy of the application before rebuilding; on Windows, its JAR can remain locked while the application is open. For MySQL installed outside the default location, use the separate setup/build/launch sequence:

```powershell
powershell -ExecutionPolicy Bypass -File .\Setup-Database.ps1 -MySqlBin 'D:\MySQL\bin'
powershell -ExecutionPolicy Bypass -File .\Build.ps1
java -jar .\dist\Gym_Management_System.jar
```

`Run.ps1` invokes setup with the default MySQL path; use the separate commands above for a different binary location.

The setup script creates `src/resources/db.properties` and `src/resources/mysql.cnf` only if they do not already exist. It imports the base schema and local demo seed into the dedicated local instance. Keep local credential files out of public repositories and change the demo values before any non-local use.

## Quick Start

For a teacher who has the required JDK and MySQL Server installed, open PowerShell in the project directory and run:

```powershell
Set-Location -LiteralPath '<project-directory>'
powershell -ExecutionPolicy Bypass -File .\Run.ps1
```

Sign in with `admin` / `123`, `trainer` / `123`, or `user1` / `123`. Each opens its appropriate dashboard. See the Demo Login table for mobile login alternatives.

For an AI-agent-friendly replication checklist that avoids the original machine's paths and checks for port conflicts before setup, see [`AGENTS.md`](AGENTS.md).

## Database Scripts

- `Setup-Database.ps1` initializes the dedicated local MySQL instance, writes local configuration and imports the schema. After a successful first demo seed it writes `.local/mysql/demo-seed-v1.complete`; subsequent launches preserve edited profiles, statuses, memberships, products and progress. The raw demo seed is rerunnable but deliberately restores canonical demo values, so do not manually reimport it to restart the application. A fresh clone starts with its own local instance and marker.
- `Stop-Database.ps1` asks the dedicated local MySQL instance to shut down using its local root option file. It does not target the separate system MySQL service.
- `backups/gms-database-init.sql` creates the `gms` database and its schema with 15 tables. It is safe to re-run against the dedicated demo instance because the tables are created conditionally; it does not populate the demo dataset.
- `backups/local-demo-seed.sql` installs lookup values and the demo dataset during the first successful setup. Reimporting it manually restores canonical demo values; normal subsequent launches skip it using the local seed marker.
- `backups/user-accounts-migration.sql` conditionally creates the account table with unique usernames/profile links and foreign keys. Setup makes `backups/pre-user-accounts_<timestamp>.sql` before installing it for the first time. Password conversion occurs in the application; no destructive table recreation is used.
- `backups/three-role-migration.sql` extends `app_user` with Member role, a nullable unique `member_id` foreign key and a role/profile CHECK. Setup backs up to `backups/pre-three-role_<timestamp>.sql` before this schema extension. It replaces the older two-role CHECK and is rerunnable; the total remains 16 tables.
- `backups/role-demo-accounts.sql` creates the Admin, Trainer, and Member demo accounts with salted hashes. Admin links to the seeded admin row; Trainer/Member profiles are found by their seeded mobile numbers. Existing accounts and changed passwords are not overwritten. If a profile or username is already owned by a different account, the insert is skipped; inspect User Management rather than replacing an existing account.

## Build and Run Scripts

- `Build.ps1` downloads Ant 1.10.15 launcher JARs into `.local/tools/` when needed, then runs the NetBeans Ant `clean jar` target. It produces `dist/Gym_Management_System.jar` and its runtime libraries.
- `Run.ps1` runs database setup, builds the project, and starts the application JAR.
- `Verify.ps1` runs setup, the clean Ant build, and all three real Swing/database verification harnesses. It uses temporary records and transactional checks; see the testing section.
- `Stop-Database.ps1` stops the isolated development MySQL server. Use it when finished with the local database.

## Testing and Verification

`Verify.ps1` compiles and runs three harnesses against the real dedicated MySQL database and Swing windows. `LocalVerification` covers the original admin modules; `AccountVerification` covers trainer creation, user management, hashing and trainer persistence; `ThreeRoleVerification` adds member login/isolation, three-role associations, session revocation, trainer progress through its Swing dialog, membership/product controls, and setup/restart persistence. Privileged test-fixture SQL is separate from application authorization. Current run logs are written locally under `work/`. Regression checks include:

- Clean Ant build and JDBC connectivity.
- Seeded demo data and mobile-number validation.
- Valid administrator login, invalid password, SQL-injection-like login input, inactive-admin rejection, and mobile-number login.
- Dashboard loading and navigation through the main panels.
- Search and refresh for the member, trainer, workout-plan, attendance, progress, equipment, product, and payment panels.
- Member, trainer, attendance, and equipment create/update/delete behavior, including validation and related-record cleanup.
- Report data, JasperViewer display, report filling, and PDF generation through JasperReports.
- Database backup, reads and writes, and empty product/equipment table handling.
- No unexpected error dialogs or severe log messages during that run.

Run the full verification from the project directory:

```powershell
powershell -ExecutionPolicy Bypass -File .\Verify.ps1
```

The latest recorded run on **11 October 2026**, after simplifying the demo logins to `admin`, `trainer`, and `user1` with password `123`, passed **221 assertions**:

| Harness | Main-process checks | Fresh-JVM checks |
|---|---:|---:|
| `LocalVerification` | 86 | — |
| `AccountVerification` | 54 | 3 |
| `ThreeRoleVerification` | 74 | 4 |
| **Total** | **214** | **7** |

The clean Ant build and full verification exited successfully. Tests create temporary fixtures and clean them up. Fresh JVM checks verify changed passwords, profile links, edited profiles and progress. The original harness exports all six reports to `work/report-*.pdf`; generated report and dashboard files stay local. The recorded local log is `work/simple-demo-verification.txt`; it is ignored by Git and is not included in a fresh clone. Run `Verify.ps1` to reproduce the checks. The original count assertions expect the restored demo dataset; modified data may change those results.

## Git Handoff

Include the Java sources, integration tests, all five schema/seed/migration scripts under `backups/`, PowerShell scripts, `lib/`, `nbproject/`, application/report resources, `.gitignore`, README, and AGENTS.md. The schema and seeds reconstruct the demo database on another machine; generated database dumps are not required.

Keep `PASSWORDS.txt`, `src/resources/db.properties`, `src/resources/mysql.cnf`, `.local/`, `build/`, `dist/`, `logs/`, `work/`, and generated SQL dumps local. These paths are ignored. The demo credentials in this README and setup scripts are intentionally shared coursework defaults.

Before committing from the project directory, review:

```powershell
git status --short
git diff --check
git diff --stat
```

Stage the intended source/documentation changes and new files, including the account migrations, role dashboards, security package, and verification harnesses. Review the staged result before committing:

```powershell
git diff --cached --stat
git diff --cached
git commit -m "Complete three-role gym management system and setup documentation"
```

Use the repository's configured remote and chosen branch when pushing. Test replication from a fresh checkout using the installation steps above, without copying machine-local files or databases.

## Reports

The application includes six compiled desktop JasperReports templates in `src/report/`:

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
- Application passwords are salted PBKDF2 hashes in `app_user`; legacy admin plaintext is removed by the compatibility migration. Database client credentials remain local configuration values. Generated database dumps, including pre-migration backups that may contain legacy plaintext, are ignored by Git.

## Troubleshooting

- **Java version/build error:** Install JDK 17 or newer and confirm `java -version` and `javac -version` work in PowerShell. `Build.ps1` needs both executables.
- **PowerShell blocks a script:** Run the supplied command with `-ExecutionPolicy Bypass`, as shown above, or use an execution policy allowed by your system.
- **Ant dependencies fail to download:** `Build.ps1` fetches Ant 1.10.15 from Maven Central the first time. Check internet access and retry, or provide the required Ant JARs under `.local/tools/`.
- **MySQL binaries not found:** Install MySQL Server 8.x or pass the correct binary folder to `Setup-Database.ps1 -MySqlBin`. `Run.ps1` uses the default path unless its setup invocation is adjusted.
- **Port 3307 is unavailable / MySQL does not start:** Check `.local/mysql/server.log` and ensure another process is not using port 3307. The dedicated server is configured for `127.0.0.1` and does not use the standard `MYSQL80` service on port 3306.
- **JDBC connection failure:** Run `Setup-Database.ps1`, check the generated `src/resources/db.properties` values, and confirm the local MySQL process is listening on `127.0.0.1:3307`.
- **Application does not start:** Run `Build.ps1` first and inspect `logs/logger.log`; launch from the project root so relative configuration and backup paths resolve.
- **Ant cannot delete `dist/Gym_Management_System.jar`:** Close the running gym application, then retry the build or verification. Avoid stopping unrelated Java applications.
- **Login fails:** Confirm setup imported the demo seed and use `admin` / `123` or `0770000001` / `123`. The seeded account must remain active.
- **Backup fails:** Confirm the MySQL client tools were found and the generated `src/resources/mysql.cnf` credentials work. Backups are written under `backups/` relative to the project root.

## Known Limitations

- This is a Windows-oriented Swing desktop application and a local MySQL setup, not a hosted or distributed service.
- Member, trainer, attendance, equipment and product workflows have management controls. Trainers can append progress for active assigned members. Workout plans, historical progress correction, payment history and sales transactions do not have complete CRUD/entry flows.
- Trainer/member attendance and training assignment/session counts are views in their dashboards. Existing `personal_training` relationships are used; there is no new scheduling or assignment editor, online payment, booking or individual workout-plan association.
- Membership status uses the existing membership type, payment flag and profile status; the schema has no membership expiry date. Updating the payment flag does not insert a payment-history record.
- The demo MySQL instance and shared demo credentials are intended for coursework/local demonstrations. This desktop application uses a shared local database connection and is not a production identity or distributed authorization service.

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

- Broader permission granularity and account recovery flows.
- A remotely hosted database with production-grade credentials and deployment configuration.
- Online membership payments and payment-entry workflows.
- Email or SMS notifications.
- A mobile application, multi-branch support, and broader analytics.
- More complete payment, sales, workout-plan, and progress-record management.

## License / Academic Use

Developed as an academic mini-project. No open-source license is included in the repository.

## Author / Team

The repository does not identify an author or project team by name. This README uses the neutral project attribution: **Academic Mini Project — Gym Management System**.
