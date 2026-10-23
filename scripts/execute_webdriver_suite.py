"""Execute the native browser cases and retain structured, unmodified outcomes."""
import argparse
import datetime
import json
import os
import platform
import sys
import time
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))
from tests.selenium import browser_helpers as helpers


class EvidenceResult(unittest.TextTestResult):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.records = {}

    def startTest(self, test):
        super().startTest(test)
        info = dict(getattr(getattr(test, test._testMethodName), 'case_info', {}))
        self.records[test.id()] = dict(info, method=test.id(), status='running',
                                      started=datetime.datetime.now().astimezone().isoformat(timespec='seconds'))

    def addSuccess(self, test):
        super().addSuccess(test)
        self.records[test.id()]['status'] = 'passed'

    def addFailure(self, test, err):
        super().addFailure(test, err)
        self.record_error(test, err, 'failed')

    def addError(self, test, err):
        super().addError(test, err)
        self.record_error(test, err, 'error')

    def record_error(self, test, err, status):
        record = self.records.setdefault(test.id(), {'id': test.id(), 'method': test.id()})
        record.update(status=status, error=self._exc_info_to_string(err, test))

    def addSkip(self, test, reason):
        super().addSkip(test, reason)
        self.records.setdefault(test.id(), {'id': test.id()}).update(status='skipped', reason=reason)

    def stopTest(self, test):
        self.records[test.id()].update(
            duration_seconds=round(time.perf_counter() - getattr(test, 'started', time.perf_counter()), 3),
            actual=getattr(test, 'notes', []), screenshots=getattr(test, 'screenshots', []),
            cleanup=getattr(test, 'cleanup', []))
        super().stopTest(test)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--match', help='Method-name substring for a pilot or focused retest')
    args = parser.parse_args()
    loader = unittest.TestLoader()
    if args.match:
        loader.testNamePatterns = ['*' + pattern.strip() + '*' for pattern in args.match.split(',')]
    suite = loader.loadTestsFromName('tests.selenium.test_webdriver')
    selected = suite.countTestCases()
    if selected == 0:
        raise SystemExit('No matching tests; nothing was run')
    started = datetime.datetime.now().astimezone().isoformat(timespec='seconds')
    began = time.perf_counter()
    result = unittest.TextTestRunner(verbosity=2, resultclass=EvidenceResult).run(suite)
    report = dict(started=started, finished=datetime.datetime.now().astimezone().isoformat(timespec='seconds'),
                  duration_seconds=round(time.perf_counter() - began, 3), planned=30, selected=selected,
                  executed=result.testsRun, passed=sum(r.get('status') == 'passed' for r in result.records.values()),
                  failures=len(result.failures), errors=len(result.errors), skipped=len(result.skipped),
                  successful=result.wasSuccessful(), filter=args.match, python=platform.python_version(),
                  browser=helpers.VERSIONS, successful_ui_logins=helpers.LOGIN_COUNTS,
                  cases=list(result.records.values()))
    destination = helpers.OUTPUT
    destination.mkdir(parents=True, exist_ok=True)
    (destination / 'results.json').write_text(json.dumps(report, indent=2), encoding='utf-8')
    latest = destination.parent / ('latest-focused.json' if args.match else 'latest-full.json')
    latest.write_text(json.dumps({'report': str(destination / 'results.json'), 'started': started}, indent=2), encoding='utf-8')
    print('Structured WebDriver evidence:', destination / 'results.json', flush=True)
    return 0 if result.wasSuccessful() and result.testsRun == selected and not result.skipped else 1


if __name__ == '__main__':
    sys.exit(main())
