# Gym Management System OOMD diagrams and report

This folder contains a new set of seven UML diagrams based on the existing Gym Management System. The visual references are the images and PDF under `friends project`. Their thin lines, white backgrounds, simple UML shapes and light blue headings guide the presentation. The content and relationships describe this gym project.

- `diagrams/`: seven PNG images and seven editable SVG files.
- `report/`: printable A4 portrait PDF and Word report with the title, detailed problem statement, and a diagram page plus one explanation page for every UML type.
- `sources/`: local Python generators and the shared report text.

The diagrams cover use cases, classes, login sequence, trainer progress activity, session states, components and deployment. The component diagram includes required sockets and provided lollipop interfaces.

The class diagram is an analysis model of all 16 persistent record types. Its operations represent supported responsibilities implemented by the Swing dialogs and account/database helpers. The record classes are not claimed to be Java declarations. Interface names in the component diagram similarly describe conceptual service contracts.

## Regeneration

Use Python with Pillow, python-docx, reportlab and pypdfium2 available. From this folder, run:

```powershell
python '.\sources\create_diagrams.py'
python '.\sources\build_report.py'
```

Paths are resolved relative to the generator files. On Windows, diagram fonts use Arial from the Windows font directory. On other platforms, adapt `FONT_DIR` and the font filenames to installed equivalents. The gym application's own setup remains Windows-oriented.

The PDF is the authoritative print layout. The Word document contains editable report text and embedded PNG figures. The report builder creates the PDF directly from the same content and does not need Microsoft Word. Word may reflow editable text depending on its available fonts and settings. Native Word rendering could not be checked here because LibreOffice is unavailable; use the verified PDF for printing.

## Project source used

- `README.md`, `src/gui/LoginForm.java`, and the Admin, Trainer and Member dashboards for users and role routing.
- `src/security/Accounts.java`, `Session.java`, and `Passwords.java` for login, role scope and progress-entry rules.
- `src/connection/MySQL.java` and report panels for JDBC, reports and backups.
- `backups/gms-database-init.sql`, `user-accounts-migration.sql` and `three-role-migration.sql` for the 16 record types and their relationships.

No database files, credentials, logs, backups or build outputs have been copied into this report folder. The generators do not start or modify MySQL.
