"""Rebuild the STQA report in the supplied assignment's plain document format."""
from pathlib import Path
import csv
import json
import re
import shutil
import sys
import textwrap
from xml.sax.saxutils import escape

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / '.local/report-tools'))
from reportlab.lib import colors
from reportlab.lib.enums import TA_JUSTIFY
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.platypus import (SimpleDocTemplate, Paragraph, Spacer, PageBreak,
                               Table, TableStyle, Image, KeepTogether)
from PIL import Image as PILImage, ImageDraw, ImageFont

OUT = ROOT / 'output/stqa'
WIDTH = A4[0] - 108
STYLES = {
    'body': ParagraphStyle('Body', fontName='Times-Roman', fontSize=11, leading=14,
                           spaceAfter=6, alignment=TA_JUSTIFY),
    'heading': ParagraphStyle('Heading', fontName='Times-Bold', fontSize=12, leading=16,
                              spaceBefore=10, spaceAfter=6, keepWithNext=True),
    'cell': ParagraphStyle('Cell', fontName='Times-Roman', fontSize=9.5, leading=12),
    'head': ParagraphStyle('Head', fontName='Times-Bold', fontSize=9.5, leading=12),
    'caption': ParagraphStyle('Caption', fontName='Times-Roman', fontSize=10, leading=13,
                              spaceBefore=5, spaceAfter=12),
}


def plain(value):
    return str(value).translate(str.maketrans({'\u2013': '-', '\u2014': '-', '\u2011': '-',
        '\u2018': "'", '\u2019': "'", '\u201c': '"', '\u201d': '"', '\u00d7': 'x'}))


def para(value, style='body'):
    return Paragraph(escape(plain(value)).replace('\n', '<br/>'), STYLES[style])


def load_evidence():
    latest = {}
    for path in sorted((OUT / 'runs').glob('*/results.json')):
        data = json.loads(path.read_text(encoding='utf8'))
        latest[data['metadata']['browser']] = (path, data)
    assert {'chrome', 'edge'} <= latest.keys(), 'Both final browser results are required.'
    for path, data in latest.values():
        m = data['metadata']
        assert m['total'] >= 79 and m['failed'] == 0 and not m.get('cleanup_error'), m
    ide_path = sorted((OUT / 'ide').glob('results-*.json'))[-1]
    ide = json.loads(ide_path.read_text(encoding='utf8'))
    assert ide['success'] and ide['numPassedTests'] >= 30 and ide['numFailedTests'] == 0
    java = json.loads((OUT / 'selenium_java_results.json').read_text(encoding='utf8'))
    focused = json.loads((OUT / 'exploratory_results.json').read_text(encoding='utf8'))
    assert java['metadata']['failed'] == 0 and java['metadata']['total'] == 8
    assert focused['failed'] == 0 and len(focused['cases']) == 8
    for browser, (path, data) in latest.items():
        for extension in ['json', 'csv']:
            shutil.copy2(path.with_name('results.' + extension), OUT / f'selenium_{browser}_results.{extension}')
        shutil.copy2(path.with_name('report.html'), OUT / f'selenium_{browser}_report.html')
    shutil.copy2(ide_path, OUT / 'selenium_ide_results.json')
    return latest, ide_path, ide, java, focused


def source_image(path, lines):
    """Render source/results verbatim, with soft line wrapping for legibility."""
    font = ImageFont.truetype('C:/Windows/Fonts/consola.ttf', 20)
    wrapped = []
    for line in lines:
        wrapped.extend(textwrap.wrap(plain(line), width=77, replace_whitespace=False,
                       drop_whitespace=False) or [''])
    image = PILImage.new('RGB', (1000, 55 + len(wrapped) * 26), 'white')
    ImageDraw.Draw(image).multiline_text((24, 22), '\n'.join(wrapped), fill='black', font=font, spacing=6)
    image.save(path)


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    latest, ide_path, ide, java, focused = load_evidence()
    chrome, edge = latest['chrome'][1], latest['edge'][1]
    edge_cases = {case['id']: case for case in edge['cases']}
    cases = chrome['cases']
    story = []

    def p(value): story.append(para(value))
    def h(value): story.append(para(value, 'heading'))
    def page(): story.append(PageBreak())
    def table(headers, rows, widths=None):
        widths = widths or [WIDTH / len(headers)] * len(headers)
        cells = [[para(value, 'head') for value in headers]]
        cells += [[para(value, 'cell') for value in row] for row in rows]
        result = Table(cells, colWidths=widths, repeatRows=1, hAlign='LEFT', splitByRow=1)
        result.setStyle(TableStyle([
            ('GRID', (0, 0), (-1, -1), 0.65, colors.black),
            ('BOX', (0, 0), (-1, -1), 0.8, colors.black),
            ('VALIGN', (0, 0), (-1, -1), 'TOP'),
            ('LEFTPADDING', (0, 0), (-1, -1), 5),
            ('RIGHTPADDING', (0, 0), (-1, -1), 5),
            ('TOPPADDING', (0, 0), (-1, -1), 5),
            ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
        ]))
        story.extend([result, Spacer(1, 10)])

    def screenshot(filename, caption, width=WIDTH, maximum_height=600):
        path = OUT / 'screenshots' / filename
        with PILImage.open(path) as image:
            w, height = image.size
        displayed_height = width * height / w
        if displayed_height > maximum_height:
            width *= maximum_height / displayed_height
            displayed_height = maximum_height
        story.append(KeepTogether([Image(str(path), width=width, height=displayed_height,
                                        hAlign='LEFT'), para(caption, 'caption')]))

    h('Title:')
    p('Create a small web-based Gym Management System by selecting relevant system environments, '
      'platforms and programming languages. Prepare a concise test plan with features to be tested '
      'and bug taxonomy, regression scripts, and test reports using Selenium WebDriver and IDE '
      'including exploratory testing.')
    h('Problem Definition:')
    p('Perform web testing, identify bugs using Selenium WebDriver and Selenium IDE, and generate '
      'test reports for the Gym Management System.')
    h('Prepared By:')
    p('Name: __________________________________    Roll No.: __________________\n'
      'Class: BE Computer Engineering    Division: _____________________________\n'
      'College: __________________________    Faculty: ________________________')
    h('Prerequisite:')
    p('Knowledge of Core Java.')
    h('Software Requirements:')
    table(['Software', 'Requirement / use'], [
        ['Operating system', 'Windows; the project uses Windows PowerShell scripts.'],
        ['Java', 'JDK 17 or newer; tested with JDK 17.0.12. Java HTTP server and JDBC backend.'],
        ['Database', 'MySQL 8.x; dedicated project instance at 127.0.0.1:3307.'],
        ['Web interface', 'HTML, CSS and JavaScript; Chrome or Edge. App URL: http://127.0.0.1:8081.'],
        ['Testing tools', 'Selenium WebDriver 4.51.0 (Java and Python), Selenium IDE runner 4.0.13; Python 3.10+ and Node.js for the supplied runners.'],
    ], [115, WIDTH - 115])
    h('Hardware Requirement:')
    p('Suggested local demonstration configuration: a dual-core processor, 8 GB RAM, sufficient '
      'free storage for the JDK, MySQL and browser, a keyboard, mouse and display. These are '
      'planning recommendations rather than measured minimum system requirements.')

    page(); h('Theory:')
    h('Introduction of mini project')
    p('The Gym Management System maintains member and trainer profiles, memberships, attendance, '
      'equipment, products, fitness measurements, account permissions and related histories. '
      'The browser edition reproduces the existing Java Swing application using Java 17, JDBC '
      'and the same MySQL schema. HTML, CSS and JavaScript provide the browser interface.')
    p('Admin manages records, account access and membership prices, views payment and sales '
      'histories, downloads six operational PDF reports and creates a local SQL backup. Trainer '
      'views assigned members and records progress for an active assigned member. Member views '
      'their own profile, membership, attendance, progress and training history. Trainer and Member '
      'can change their permitted profile fields and password.')
    p('The account role and linked profile determine access after sign-in. Passwords use salted '
      'PBKDF2 hashes, JDBC statements use parameters, and related changes use database transactions. '
      'Payment and sales entry, workout editing and creation of training assignments are outside '
      'the inherited interface; their stored histories and detail views remain available.')
    h('Testing Tool: Selenium')
    p('Selenium automates interactions with a web application in a real browser. Test scripts locate '
      'page elements, enter data, click controls, wait for page changes and assert expected results. '
      'It supports repeatable checks of authentication, forms, navigation, role access and downloads.')
    p('Selenium WebDriver controls a browser through its driver. Selenium IDE stores command '
      'sequences in a .side project that can be replayed. Selenium Grid coordinates browser '
      'sessions across machines. Selenium Remote Control (RC), named in the supplied theory '
      'format, is a legacy component; this project uses WebDriver and IDE replay.')
    p('The detailed regression is written in Python WebDriver and executed on Chrome and Edge. '
      'An additional Java WebDriver program demonstrates Core Java test code. The 30-case '
      'Selenium IDE project was executed by the official command-line replay engine. Graphical '
      'IDE recording/playback was not used as execution evidence.')
    h('Advantages of Selenium')
    p('i) Open-source browser automation.\nii) Supports multiple browsers and programming languages.\n'
      'iii) Repeats regression checks consistently.\niv) Supports explicit waits and browser sessions.\n'
      'v) Can be integrated with test runners and result-processing scripts.')
    h('Disadvantages of Selenium')
    p('i) Requires a supported browser and a compatible driver.\nii) Changing page structure can '
      'break element locators.\niii) Timing and asynchronous page changes require suitable waits.\n'
      'iv) Image and visual layout checks require additional inspection.\nv) Reports and test '
      'data cleanup must be provided by the test framework or supporting scripts.')

    page(); h('Test Plan')
    h('INTRODUCTION')
    p('This test plan defines the scope, approach, resources, responsibilities, schedule and '
      'criteria for testing the Gym Management System web application. It covers the Admin, '
      'Trainer and Member workflows implemented in the current project.')
    h('1 TEST STRATEGY')
    h('1.1 Scope of Testing')
    h('1.1.1 Feature to be tested')
    table(['Module Name', 'Applicable Roles', 'Description'], [
        ['Login and logout', 'All roles', 'Valid username/mobile, incorrect input, inactive accounts and session logout.'],
        ['Dashboard', 'All roles', 'Saved-record totals, own-role metrics and recorded weekly activity.'],
        ['Member management', 'Admin', 'Create with initial measurements/BMI, search, edit, membership/payment flags, activate/deactivate and delete.'],
        ['Trainer management', 'Admin', 'Create trainer and linked login together; edit/status/delete and validation.'],
        ['Attendance', 'Admin; scoped views for Trainer/Member', 'Active member check-in, duplicate prevention, payment-due warning and own/assigned history.'],
        ['Equipment and products', 'Admin', 'Create/edit/delete, stock, condition, numeric bounds and referenced-record protection.'],
        ['Memberships and workouts', 'Admin', 'Plan-price validation/update and readable seven-day workout details.'],
        ['User management', 'Admin', 'Linked roles, unique profiles, active state, password reset and self-protection.'],
        ['Progress and training', 'Trainer; Member reads own data', 'Assigned-member progress, calculated BMI, date/measurement bounds and scope restrictions.'],
        ['Profile and password', 'Trainer, Member', 'Own profile changes, current-password verification and confirmation validation.'],
        ['History and exports', 'Admin', 'Payment/sales histories, filtered CSV, six PDF downloads and local SQL backup.'],
        ['Interface and access', 'All roles', 'Search, pagination, mobile navigation, keyboard focus, theme and protected routes.'],
    ], [100, 112, WIDTH - 212])
    h('1.1.2 Feature not to be tested')
    p('Production deployment, load/performance benchmarking, a comprehensive penetration test, '
      'hardware interfaces and the original Swing interface are outside this web test cycle. '
      'New payment/sale entry and editing workout or training assignments are outside the '
      'current application workflow. Existing related history/detail views are tested.')
    h('1.2 Test Type')
    table(['Test type', 'Application in this project'], [
        ['Integration testing', 'Java HTTP routes, JDBC and MySQL persistence; paired member/progress and trainer/account saves.'],
        ['System testing', 'Complete sign-in, role workflow, record management and export flows through the browser.'],
        ['API testing', 'Authenticated browser fetch requests verify server-side bounds, forbidden roles and invalid/duplicate actions.'],
        ['Regression testing', '79 detailed scenarios on each browser, 30 IDE replay scenarios and 8 Java smoke scenarios.'],
        ['Compatibility and UI', 'Chrome/Edge runs, 1440px desktop and 390px mobile layouts, keyboard and theme behavior.'],
        ['Exploratory testing', 'Independent role sessions, modal focus/Escape, mobile outside click and measurement boundaries.'],
    ], [130, WIDTH - 130])
    h('Regression scripts')
    p('Detailed suite: web/tests/selenium_suite.py, launched by Verify-Web.ps1.\n'
      'Java companion: web/tests/java/src/main/java/GymWebSmoke.java, launched by Verify-WebJava.ps1.\n'
      'IDE project: web/tests/Gym_STQA.side, launched by Run-SeleniumIDE.ps1.\n'
      'Exploration: web/tests/exploratory_checks.py.\n'
      'Scripts use stable identifiers and explicit waits. Detailed suites create uniquely tagged '
      'test records, verify the database data-directory path and clean up only their fixture IDs.')
    h('1.3 Risk and Issues')
    table(['Risk', 'Mitigation'], [
        ['An unrelated service occupies a project port.', 'Verify MySQL 3307 ownership before setup. Stop setup for a foreign listener. Use the documented free web port 8081.'],
        ['Browser and driver versions differ.', 'Use Selenium Manager or a matching cached ChromeDriver; record the actual browser version in results.'],
        ['Dynamic forms and page transitions affect selectors.', 'Scope locators to the visible dialog, use stable IDs, explicit waits and mobile device emulation.'],
        ['Tests accidentally alter existing gym data.', 'Verify this checkout database; create distinct fixtures, use exact IDs and preserve original profiles/passwords.'],
        ['PDF compatibility or overflowing rows.', 'Use OpenPDF first in the classpath, parse downloaded PDFs and visually inspect wrapping/page breaks.'],
    ], [190, WIDTH - 190])
    h('Bug taxonomy and identified defects')
    p('The following observations were recorded during the migration and test work. Discovery '
      'by code inspection is identified explicitly; those observations are not represented as '
      'Selenium-discovered failures.')
    table(['ID / category', 'Observation / discovery', 'Correction and evidence'], [
        ['D01 Database compatibility', 'Live request: an unquoted member SQL alias caused dashboard HTTP 500.', 'Quoted aliases; dashboard and module regressions pass.'],
        ['D02 Report library compatibility', 'Live PDF request: legacy iText/OpenPDF conflict caused NoSuchMethodError.', 'OpenPDF precedence; TC42-TC47 download/parse all six PDFs.'],
        ['D03 Startup environment', 'Actual launch: selector socket setup failed; web port 8080 was occupied.', 'Project-local socket directory and web port 8081; browser connection succeeds.'],
        ['D04 Functional/data integrity', 'Inspection: draft lacked atomic initial measurements and trainer-account creation.', 'Transactional paired saves and valid form IDs; TC14, TC15 and TC23 pass.'],
        ['D05 Validation', 'Inspection: some numeric bounds and password confirmation checks were incomplete.', 'Server and form validation; negative value/date/password cases pass.'],
        ['D06 Automation compatibility', 'Selenium: select_by_value could not find implicit page-size option values.', 'Explicit 10/25/50 values; pagination and TC79 pass.'],
        ['D07 Report layout', 'Visual inspection: DAYPASS wrapped into an adjacent member-report row.', 'Automatic table-row heights and readable PDF layouts; downloads were rerun.'],
        ['D08 Keyboard interface', 'Focused Selenium check: Tab moved focus outside the open dialog.', 'Explicit first/last-control wrapping; 12 Tab transitions and Escape pass.'],
    ], [97, 183, WIDTH - 280])
    p('Test-harness issues included a hidden-field locator, mobile transition timing, and Windows '
      'path quoting in the IDE runner. Their diagnostic failures were corrected and retained '
      'separately. Final acceptance uses the passing runs identified in the execution summary.')
    h('1.4 Test Logistics')
    h('1.4.1 Who will test?')
    p('The student/team named under Prepared By owns test planning, script review, exploratory '
      'review and the faculty demonstration. Automated WebDriver and IDE scripts execute '
      'repeatable checks. Student names and roll numbers must be completed before submission.')
    h('1.4.2 When will test occur?')
    p('Testing starts when the application and dedicated database are available, specifications '
      'and test data are ready, browser drivers are available, and the environment ownership '
      'checks pass. The recorded final runs were executed on 15 October 2026 after corrections.')
    h('2 TEST OBJECTIVE')
    p('Verify correct authentication and role access; reliable member/trainer and inventory '
      'management; persistent attendance and progress; validation and relationship protection; '
      'readable reports/exports; and an operable desktop/mobile interface. Confirm that test '
      'fixtures are removed without resetting existing profiles or passwords.')
    h('3 TEST CRITERIA')
    h('3.1 Suspension Criteria')
    p('Suspend when the database/server is unavailable, a foreign database listener is detected, '
      'test-data isolation cannot be established, or 40% of the current cases fail as described '
      'in the supplied plan example. Repair the blocking cause and rerun before acceptance.')
    h('3.2 Exit Criteria')
    p('Run rate must be 100%. The supplied format gives an 80% pass-rate threshold; this project '
      'uses the stricter requirement of zero final failed cases in the planned suites, successful '
      'fixture cleanup, and complete saved reports/screenshots. The documented final runs meet these criteria.')
    h('4 RESOURCE PLANNING')
    h('4.1 System Resource')
    table(['No.', 'Resource', 'Description'], [
        ['1', 'Application server', 'Java HTTP server on this computer at http://127.0.0.1:8081.'],
        ['2', 'Database server', 'This checkout MySQL instance at 127.0.0.1:3307; schema gms.'],
        ['3', 'Test tools', 'Selenium 4.51.0, Chrome '+chrome['metadata']['browser_version']+', Edge '+edge['metadata']['browser_version']+', IDE runner 4.0.13.'],
        ['4', 'Network', 'Local loopback during demonstration; Internet is needed only for missing initial dependencies/drivers.'],
        ['5', 'Computer', 'Windows machine with JDK, MySQL binaries and an installed supported browser.'],
    ], [28, 113, WIDTH - 141])
    h('4.2 Human Resource')
    table(['Role', 'Responsibility'], [
        ['Project student/team', 'Review the requirements and project, prepare test data, execute scripts, inspect failures, verify fixes and maintain the report.'],
        ['Faculty reviewer', 'Review the submitted report and observe the project/test demonstration.'],
    ], [125, WIDTH - 125])
    h('5 SCHEDULE & ESTIMATION')
    h('5.1 All project task and estimation')
    p('The effort values below are planning allowances for a local test/report cycle, not measured '
      'labour hours or claims about actual elapsed implementation time.')
    table(['Task', 'Member / role', 'Estimated effort'], [
        ['Review requirements and features', 'Project student/team', '30 minutes'],
        ['Prepare cases and scripts', 'Project student/team', '45 minutes'],
        ['Execute tests and investigate defects', 'Project student/team', '90 minutes'],
        ['Review fixes and rerun regression', 'Project student/team', '45 minutes'],
        ['Prepare report and demonstration', 'Project student/team', '30 minutes'],
        ['Total planning allowance', 'Project student/team', '240 minutes'],
    ], [238, 135, WIDTH - 373])
    h('Schedule to complete these task')
    table(['Stage', 'Sequence / completion'], [
        ['Before testing', 'Inspect assignment/project; prepare environment, plan, cases and test data.'],
        ['During testing', 'Run WebDriver, correct observed issues, repeat Chrome/Edge, replay IDE and run focused/Java checks.'],
        ['After testing', 'Confirm final passing JSON and cleanup, capture screenshots, prepare report, package and demo instructions.'],
    ], [113, WIDTH - 113])
    h('TEST DELIVERABLES')
    h('7.1 Before testing phase')
    p('- Test plan and feature scope.\n- Test cases and procedures.\n- Test design and data-isolation rules.')
    h('7.2 During the testing')
    p('- WebDriver/IDE/Java scripts and test data.\n- Browser execution traces, error records and screenshots.\n'
      '- Requirements traceability matrix (requirements_traceability.csv).\n'
      '- Local JSON, CSV and HTML results; separate diagnostic observations.')
    h('7.3 After the testing cycles is over')
    p('- Final test results and this report.\n- Defect report and exploratory results.\n'
      '- Project run and test instructions.\n- Source/report submission archive and release notes in web/README.md.')

    page(); h('Test Report for Test Cases inclusive of Test Procedures for identified Test Scenarios.')
    p('All 79 cases below were executed independently on Chrome and Edge. The Actual Result '
      'column records the final Chrome observation; Pass means the corresponding case passed '
      'on both browsers. The separate raw files preserve each browser result and fixture identifier.')
    details = []
    for case in cases:
        assert edge_cases[case['id']]['status'] == case['status'] == 'PASS'
        actual = case['actual']
        if case['id'] == 'TC01': actual = 'Required-field validation blocked the empty sign-in form.'
        elif case['id'] in {'TC40', 'TC41'}:
            count = re.search(r',\s*(\d+)\)', actual)
            actual = ('Payment history' if case['id'] == 'TC40' else 'Product sales') + ' loaded with '+count.group(1)+' records.'
        elif actual.startswith('Verified: '): actual = actual[len('Verified: '):]
        details.append([case['id'], case['name'], case['steps'], case['expected'], actual, 'Pass'])
    table(['Sr. No.', 'Test Objective', 'Steps', 'Expected Result', 'Actual Result', 'Test Status'],
          details, [36, 79, 124, 85, WIDTH - 364, 40])
    page(); h('Test execution summary')
    table(['Suite', 'Pass', 'Fail', 'Recorded execution'], [
        ['WebDriver - Chrome', str(chrome['metadata']['passed']), '0', chrome['metadata']['timestamp']],
        ['WebDriver - Edge', str(edge['metadata']['passed']), '0', edge['metadata']['timestamp']],
        ['Selenium IDE replay', str(ide['numPassedTests']), '0', 'Official runner 4.0.13; raw JSON saved.'],
        ['Java WebDriver smoke checks', str(java['metadata']['passed']), '0', java['metadata']['timestamp']],
        ['Focused exploratory checks', str(focused['passed']), '0', focused['timestamp']],
    ], [166, 34, 34, WIDTH - 234])
    total = chrome['metadata']['total'] + edge['metadata']['total'] + ide['numTotalTests'] + java['metadata']['total'] + len(focused['cases'])
    p(f'{total} test executions passed. This comprises 79 detailed scenarios repeated on two '
      'browsers, plus overlapping IDE, Java and focused checks. The suites share functional '
      'coverage. Fixture cleanup completed without an error.')
    h('Exploratory testing report')
    p('Additional exploration covered independent role sessions, keyboard interaction, mobile '
      'dismissal and invalid measurement boundaries. The final observed results follow.')
    table(['Test / condition', 'Actual result', 'Test Status'], [
        [case['name'], case['actual'], 'Pass'] for case in focused['cases']
    ], [166, WIDTH - 211, 45])
    p('Long workout text, empty searches, profile refresh, light/dark theme behavior and PDF '
      'row wrapping were also reviewed during the recorded work. The first modal-focus '
      'diagnostic found focus outside the dialog; the corrected keyboard wrap passed the final '
      '12-Tab test. Downloaded PDFs were parsed and the report layouts were visually inspected.')

    shots = OUT / 'screenshots'
    java_source = (ROOT / 'web/tests/java/src/main/java/GymWebSmoke.java').read_text(encoding='utf8')
    login_excerpt = java_source[java_source.index('    static void login('):java_source.index('    static void page(')]
    example = next(line.strip() for line in java_source.splitlines() if 'test("JAVA07"' in line)
    source_image(shots/'10-java-code.png', ['GymWebSmoke.java - actual source excerpt', '', *login_excerpt.splitlines(), '', example])
    jm = java['metadata']
    source_image(shots/'11-java-output.png', ['Saved Java WebDriver execution results', 'Source: output/stqa/selenium_java_results.json', '',
        'Timestamp: '+jm['timestamp'], 'Browser: Chrome '+jm['browser_version'], 'Selenium: '+jm['selenium_version'], '',
        *[c['id']+' '+c['status']+' '+c['name'] for c in java['cases']], '',
        f"Passed: {jm['passed']}   Failed: {jm['failed']}   Total: {jm['total']}"])
    page(); h('Screenshot of Code and Output')
    screenshot('10-java-code.png', 'Figure 1. Actual Java WebDriver login method and Member role test using user1 / 123.', width=WIDTH, maximum_height=350)
    screenshot('11-java-output.png', 'Figure 2. Saved output of the eight executed Java WebDriver checks.', width=WIDTH, maximum_height=270)
    page(); h('Screenshot of Code and Output (continued)')
    screenshot('08-code-and-output.png', 'Figure 3. Actual Python WebDriver regression code and final Chrome/Edge execution metadata.', maximum_height=640)
    page(); h('Screenshot of Code and Output (continued)')
    screenshot('09-selenium-ide-output.png', 'Figure 4. Actual saved official Selenium IDE runner output: 30 passed, zero failed.', maximum_height=640)
    page(); h('(2-3 screenshots of GUI)')
    screenshot('01-login.png', 'Figure 5. Common login for Admin, Trainer and Member.', width=435, maximum_height=320)
    screenshot('02-admin-overview.png', 'Figure 6. Admin overview with totals from saved gym records.', width=435, maximum_height=320)
    page(); h('(2-3 screenshots of GUI) (continued)')
    screenshot('03-member-management.png', 'Figure 7. Member management with search, pagination, status and record actions.', maximum_height=500)

    def page_number(canvas, doc):
        canvas.setFont('Times-Roman', 10)
        canvas.drawCentredString(A4[0]/2, 27, str(doc.page))

    output = OUT/'Gym_Management_System_STQA_Report.pdf'
    document = SimpleDocTemplate(str(output), pagesize=A4, leftMargin=54, rightMargin=54,
        topMargin=48, bottomMargin=48, title='Gym Management System - STQA Mini Project Report',
        author='Project student/team')
    document.build(story, onFirstPage=page_number, onLaterPages=page_number)
    metadata = {'format': 'Plain assignment order; Times; full black table borders',
        'chrome': chrome['metadata'], 'edge': edge['metadata'],
        'ide': {'passed': ide['numPassedTests'], 'failed': ide['numFailedTests'], 'source': str(ide_path.relative_to(ROOT))},
        'browser_sources': {browser: str(path.relative_to(ROOT)) for browser, (path, _) in latest.items()},
        'java': java['metadata'], 'exploratory': {'passed': focused['passed'], 'failed': focused['failed']}}
    (OUT/'REPORT_METADATA.json').write_text(json.dumps(metadata, indent=2), encoding='utf8')
    # Every case remains traceable to the unchanged executed evidence.
    def requirement(case):
        number = int(case['id'][2:])
        groups = [('R01 Authentication and access', [*range(1, 6), 7, 27, 28, 29, 72, 73, 74, 75]),
            ('R02 Dashboard and summaries', [6, 78]), ('R03 Members and measurements', [*range(8, 20), 76]),
            ('R04 Attendance', [20, 21, 22]), ('R05 Trainers and linked accounts', [23, 24, 25, 26, 77]),
            ('R06 Equipment', [30, 31, 32]), ('R07 Products', [33, 34, 35, 36, 37]),
            ('R08 Membership plans and workout details', [38, 39, 79]),
            ('R09 History, reports and backup', list(range(40, 50))),
            ('R10 Trainer workspace', list(range(50, 61))), ('R11 Member workspace', list(range(61, 69))),
            ('R12 Responsive interface and theme', [69, 70, 71])]
        return next(name for name, ids in groups if number in ids)

    with (OUT/'requirements_traceability.csv').open('w', encoding='utf8', newline='') as file:
        writer = csv.writer(file)
        writer.writerow(['Requirement', 'Case ID', 'Scenario', 'Procedure', 'Expected', 'Actual Chrome', 'Chrome status', 'Edge status'])
        for case in cases:
            writer.writerow([requirement(case), case['id'], case['name'], case['steps'], case['expected'], case['actual'], case['status'], edge_cases[case['id']]['status']])
    print('Created', output)


if __name__ == '__main__':
    main()
