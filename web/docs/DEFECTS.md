# Observed defects and corrective work

| ID | Category / discovery | Observation | Correction and regression evidence |
|---|---|---|---|
| D01 | Database compatibility / live request | Admin dashboard returned HTTP 500. MySQL rejected an unquoted `member` result-column alias. | Quoted SQL aliases. All module views and TC06 passed in the browser suites. |
| D02 | Reporting compatibility / live request | PDF export raised `NoSuchMethodError` for `com.lowagie.text.Phrase.add`; the legacy iText JAR preceded OpenPDF. | Put OpenPDF first in the launcher classpath. Return a controlled error for linkage failures. TC42–TC47 downloaded and parsed all six PDFs. |
| D03 | Environment / actual startup | Java HTTP server failed to establish its selector loopback connection using the Windows temporary socket path. Port 8080 was also occupied by another application. | Project-local socket directory and configurable web port 8081. Launch and browser connection verified. |
| D04 | Functional parity / code inspection | The first web draft lacked atomic initial member measurements and combined trainer/profile account creation; blank IDs also failed numeric parsing. | Transactional member/progress and trainer/account creation, valid zero IDs, named form choices. TC14, TC15, TC23 and account tests passed. |
| D05 | Validation / code inspection | The draft accepted some negative/non-finite amounts and did not validate password confirmation. | Server bounds and password confirmation checks, inline form validation. TC34, TC38, TC55, TC58–TC60 passed. |
| D06 | Browser-control automation compatibility / Selenium TC79 | The page-size options relied on text as their value. Selenium `Select.select_by_value('50')` could not locate an explicit value attribute, although human selection worked. | Added explicit option values 10/25/50. Pagination and positive membership-price regression verify the corrected control. |

## Test automation issues (separate from application defects)

- H01: The first Python runner selected the hidden sign-in username field when filling a trainer dialog. Scoped field selectors to the active dialog and closed dialogs after a failed case. The first diagnostic run was incomplete and had cascading harness failures; it is retained as evidence, not counted as a successful application regression.
- H02: A mobile check inspected the navigation drawer during its CSS transition and used the desktop window's minimum size. Added real 390px device emulation and waits for the drawer position. TC70 passed after this correction.
- H03: Selenium IDE runner 4.0.13 constructed an unquoted Jest shell command for a Windows path containing spaces. The local `run_ide.js` wrapper passes the existing runner's Jest command as an argument array and supplies its dependency resolution path. The official test engine replayed all 30 `.side` cases successfully; installed dependency files were not changed.

## Remaining scope limits

The final test results describe the local demo and the covered workflows. No load benchmark, Internet deployment, penetration test, or Selenium IDE browser-extension GUI execution is claimed. The `.side` project was replayed with the official command-line runner and can be imported into the IDE graphical application. Payment and sale history, workout plans, and training assignments retain the existing read-only desktop workflows.

## Visual report review

D07: The original member report wrapped DAYPASS into the next table row. The browser exports now use OpenPDF tables over the same six datasets, with automatic row height, readable Active/Inactive statuses, suitable page orientation, repeated headers and a separate equipment-maintenance section. Original desktop .jasper files were retained. The final PDF download scenarios were rerun after this layout repair.

D08: Focused Selenium keyboard exploration found that native Tab cycling returned activeElement outside the dialog DOM. Added explicit first/last-control keyboard wrap. Twelve Tab transitions, Escape dismissal, independent role sessions, mobile outside-click dismissal and measurement boundary checks passed in the final eight-case focused run.
