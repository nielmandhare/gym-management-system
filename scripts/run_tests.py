"""Run unittest suites and save the REAL console output under test-results/.

Usage (from the repository root):
    python scripts/run_tests.py unit
    python scripts/run_tests.py integration
    python scripts/run_tests.py blackbox
    python scripts/run_tests.py all        # ONE combined run of every test file

Each run writes test-results/<name>.txt containing a metadata header
(date, command, exit code, counts) followed by the unmodified stdout+stderr
of the test process. Nothing is fabricated: counts are parsed from the
output unittest itself printed. The exit code is non-zero if any suite failed.
"""

import datetime
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = os.path.join(ROOT, "test-results")

SUITES = {
    "unit": ("unit-tests.txt", ["-s", "tests", "-p", "test_unit_*.py"]),
    "integration": ("integration-tests.txt", ["-s", "tests", "-p", "test_integration_*.py"]),
    "blackbox": ("blackbox-tests.txt", ["-s", "tests", "-p", "test_blackbox_*.py"]),
    "all": ("full-test-suite.txt", ["-s", "tests", "-p", "test_*.py"]),
}


def parse_counts(output):
    """Counts come from unittest's own verbose lines and summary."""
    ran = re.search(r"^Ran (\d+) tests? in", output, re.M)
    ran = int(ran.group(1)) if ran else 0
    count = lambda key: sum(
        int(n) for n in re.findall(rf"{key}=(\d+)", " ".join(re.findall(r"^(?:OK|FAILED) \((.*)\)$", output, re.M)))
    )
    failed, errored, skipped = count("failures"), count("errors"), count("skipped")
    # A skipped setUpClass/setUpModule is NOT counted in "Ran"; a test skipped
    # individually is. Only the latter reduces the passed count.
    class_skips = len(re.findall(r"^(?:setUpClass|setUpModule) .*\.\.\. skipped", output, re.M))
    passed = max(ran - failed - errored - (skipped - class_skips), 0)
    return {"ran": ran, "passed": passed, "failed": failed, "errored": errored, "skipped": skipped}


def run_suite(name):
    filename, args = SUITES[name]
    cmd = [sys.executable, "-m", "unittest", "discover", *args, "-v"]
    env = dict(os.environ, PYTHONIOENCODING="utf-8")
    started = datetime.datetime.now().astimezone()
    proc = subprocess.run(
        cmd, cwd=ROOT, env=env, text=True, encoding="utf-8",
        stdout=subprocess.PIPE, stderr=subprocess.STDOUT,  # keep stdout/stderr order
    )
    output = proc.stdout
    counts = parse_counts(output)
    if proc.returncode != 0:
        status = "FAILED"
    elif counts["ran"] == 0 and counts["skipped"] > 0:
        status = "NOT RUN (all skipped)"
    elif counts["skipped"] > 0:
        status = "PASSED with skips"
    else:
        status = "PASSED"

    header = (
        f"Suite:        {name}\n"
        f"Command:      python -m unittest discover {' '.join(args)} -v\n"
        f"Executed at:  {started.isoformat(timespec='seconds')}\n"
        f"Exit code:    {proc.returncode}\n"
        f"Result:       {status}\n"
        f"Counts:       ran={counts['ran']} passed={counts['passed']} failed={counts['failed']} "
        f"errored={counts['errored']} skipped={counts['skipped']}\n"
        f"{'=' * 70}\n"
    )
    os.makedirs(RESULTS, exist_ok=True)
    with open(os.path.join(RESULTS, filename), "w", encoding="utf-8") as fh:
        fh.write(header + output)
    print(output, end="")
    print(f"\n[runner] {name}: {status} (exit {proc.returncode}) -> test-results/{filename}")
    return {"suite": name, "file": filename, "executed_at": started.isoformat(timespec="seconds"),
            "exit_code": proc.returncode, "result": status, **counts}


def main(argv):
    choice = argv[1] if len(argv) > 1 else "all"
    if choice not in SUITES:
        print(f"Unknown suite '{choice}'. Choose from: {', '.join(SUITES)}")
        return 2
    summary = run_suite(choice)
    summary_path = os.path.join(RESULTS, "summary.json")
    try:
        with open(summary_path, encoding="utf-8") as fh:
            previous = {r["suite"]: r for r in json.load(fh)}
    except (OSError, ValueError):
        previous = {}
    previous[choice] = summary  # keep the latest run of each suite
    with open(summary_path, "w", encoding="utf-8") as fh:
        json.dump(list(previous.values()), fh, indent=2)
    return 1 if summary["exit_code"] != 0 else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
