"""Run opt-in browser tests and save the actual console output."""

import os
import json
import argparse
import subprocess
import sys
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--browser", choices=("chrome", "firefox"), default="chrome")
    parser.add_argument("--match", help="Run only a method containing this substring")
    args = parser.parse_args()
    browser = args.browser
    hold_file = ROOT / "selenium" / "local-browser-status.json"
    if hold_file.exists():
        hold = json.loads(hold_file.read_text(encoding="utf-8")).get(browser, {})
        if hold.get("blocked"):
            print(browser + " execution is paused: " + hold["reason"], file=sys.stderr)
            sys.exit(2)
    run_name = datetime.now().strftime("%Y%m%d-%H%M%S") + ("-focused" if args.match else "-full")
    output = ROOT / "test-results" / "webdriver" / browser / run_name
    output.mkdir(parents=True, exist_ok=True)
    env = dict(os.environ, RUN_SELENIUM_TESTS="1", SELENIUM_BROWSER=browser,
               WEBDRIVER_OUTPUT=str(output), PYTHONIOENCODING="utf-8")
    command = [sys.executable, "scripts/execute_webdriver_suite.py"]
    if args.match:
        command.extend(["--match", args.match])
    with (output / "results.txt").open("w", encoding="utf-8") as log:
        with subprocess.Popen(command, cwd=ROOT, env=env, stdout=subprocess.PIPE,
                              stderr=subprocess.STDOUT, text=True, encoding="utf-8") as proc:
            for line in proc.stdout:
                print(line, end="", flush=True)
                log.write(line)
                log.flush()
            code = proc.wait()
    print(f"WebDriver results: {output / 'results.txt'}")
    sys.exit(code)
