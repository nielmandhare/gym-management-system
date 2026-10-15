# Demonstrating the Gym Management System

Project folder on this computer:

```text
C:\Users\NIEL MANDHARE\Desktop\OOMD mini project\gym-management-system-main (1)\gym-management-system-main
```

The web application runs on this computer at **http://127.0.0.1:8081/**. Open that address in Chrome or Edge. `127.0.0.1` refers to this computer; it is a local demonstration URL. The dedicated project MySQL database runs at **127.0.0.1:3307**, schema `gms`.

## Before the demonstration

The application is currently running. Open its URL and sign in. The logins are:

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `123` |
| Trainer | `trainer` | `123` |
| Member | `user1` | `123` |

To start it after restarting the computer, open PowerShell and paste:

```powershell
Set-Location -LiteralPath 'C:\Users\NIEL MANDHARE\Desktop\OOMD mini project\gym-management-system-main (1)\gym-management-system-main'
powershell -NoProfile -ExecutionPolicy Bypass -File .\Start-Demo.ps1 -OpenBrowser
```

`Start-Demo.ps1` checks that any listener on database port 3307 and web port 8081 belongs to this checkout. It starts a missing local database/web server and reuses an already running project server. It does not stop a running project. If it reports a port conflict, use the message to identify the conflict before proceeding. The current computer uses MySQL binaries under `C:\Program Files\MySQL\MySQL Server 8.0\bin`; an alternative installation can be supplied through `-MySqlBin`.

Java 17+ and MySQL 8.x binaries must be installed. A browser is required to display the application. Starting the web edition uses `Run-Web.ps1`; `Run.ps1` is the original desktop application entry point.

## What to show ma'am

1. Say: "This is our Gym Management System web application. It uses a Java backend, JDBC and MySQL, with HTML, CSS and JavaScript in the browser."
2. Sign in as **admin / 123**. Show the overview totals, then Members. Search for a saved member and show the membership, paid/due status and record actions. You can open an Add/Edit form and cancel without changing data.
3. Show Attendance, Trainers, Equipment and Products. Explain that trainer creation saves the trainer profile and linked login together, and that initial member measurements include calculated BMI.
4. Open Reports and download one PDF. Explain that all six report types use saved gym data. Payment and product-sales history are available as record views.
5. Sign out, confirm, and sign in as **trainer / 123**. Show Assigned members and Progress tracking. Explain that the trainer can record measurements only for an active assigned member.
6. Sign out and sign in as **user1 / 123**. Show My profile, My attendance and My progress. Explain that a Member sees their own records and has no Admin management navigation.
7. Open the STQA report and show its test plan, bug taxonomy, the six-column test-case tables with borders, code/output screenshots and GUI screenshots.
8. Show `output\stqa\selenium_chrome_report.html` or the saved JSON/CSV results. The recorded final suites passed 79 Chrome cases, 79 Edge cases, 30 IDE replay cases, 8 Java smoke checks and 8 focused checks. These are 204 executions across overlapping suites.

## Running Selenium in front of ma'am

The shortest Java demonstration is:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\Verify-WebJava.ps1
```

It opens a headless Chrome session, checks login/navigation/search/report choices/logout, and prints `PASS` lines. It writes `output\stqa\selenium_java_results.json`. The Java source to show is `web\tests\java\src\main\java\GymWebSmoke.java`.

For the detailed Python regression, if Python 3.10+ is on PATH:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\Verify-Web.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\Verify-Web.ps1 -Browser edge
```

On this computer, the configured bundled Python can be supplied explicitly:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\Verify-Web.ps1 -Python 'C:\Users\NIEL MANDHARE\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
```

For Selenium IDE, open/import `web\tests\Gym_STQA.side` in an installed compatible Selenium IDE application. The saved report evidence uses the official CLI replay engine. On this computer it can be replayed with:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\Run-SeleniumIDE.ps1 -Node 'C:\Users\NIEL MANDHARE\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe'
```

Use saved results if there is no time for a complete run during the demonstration. The detailed regression uses separate test records and cleans them up after execution.

## Files to keep ready

- Report: `output\stqa\Gym_Management_System_STQA_Report.pdf`.
- Submission archive: `output\Gym_Web_STQA_Submission.zip`.
- Case procedures: `web\tests\case_steps.json`.
- Browser evidence and screenshots: `output\stqa\`.
- Runtime, feature mapping and full test setup: `web\README.md`.

Complete the report's student names, roll numbers, division, college and faculty fields before submission.
