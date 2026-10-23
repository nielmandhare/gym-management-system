// Launch the existing IDE suite with one worker, honoring local crash holds.
import fs from 'node:fs';
import path from 'node:path';
import { spawn } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import chrome from 'selenium-webdriver/chrome.js';
import manager from 'selenium-webdriver/common/seleniumManager.js';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const args = process.argv.slice(2);
const browserIndex = args.indexOf('--browser');
const browser = browserIndex < 0 ? 'chrome' : args[browserIndex + 1];
const filterIndex = args.indexOf('--filter');
const filter = filterIndex < 0 ? '^All 100 tests$' : args[filterIndex + 1];
if (!['chrome', 'firefox', 'edge'].includes(browser)) {
  console.error('Choose --browser chrome, firefox or edge.');
  process.exit(2);
}
const holdPath = path.join(root, 'selenium', 'local-browser-status.json');
if (fs.existsSync(holdPath)) {
  const holds = JSON.parse(fs.readFileSync(holdPath, 'utf8'));
  if (holds[browser]?.blocked) {
    console.error(`Browser execution is paused: ${holds[browser].reason}`);
    process.exit(2);
  }
}

const destination = path.join(root, 'test-results', 'selenium', browser);
const command = [
  path.join(root, 'node_modules', 'selenium-side-runner', 'dist', 'bin.js'),
  '--capabilities', `browserName=${browser === 'edge' ? 'MicrosoftEdge' : browser}`,
  '--max-workers', '1', '--filter', filter,
  '--timeout', '15000', '--jest-timeout', '120000',
  '--output-directory', destination,
  '--screenshot-failure-directory', path.join(destination, 'screenshots'),
  path.join(root, 'selenium', 'gym-management.side'),
];
if (args.includes('--dry-run')) {
  console.log(JSON.stringify({ executable: process.execPath, args: command }, null, 2));
  process.exit(0);
}
// Resolve ChromeDriver once rather than running Manager for every test.
// Each test still creates a separate Chrome session and temporary profile.
let service;
try {
  if (browser === 'chrome') {
    const { driverPath } = manager.binaryPaths([
      '--browser', 'chrome', '--language-binding', 'javascript', '--output', 'json',
    ]);
    service = new chrome.ServiceBuilder(driverPath).setLoopback(true).build();
    const server = await service.start();
    command.splice(1, 0, '--server', server);
  }
  const code = await new Promise((resolve, reject) => {
    const child = spawn(process.execPath, command, { cwd: root, stdio: 'inherit' });
    child.once('error', reject);
    child.once('exit', (status) => resolve(status ?? 1));
  });
  process.exitCode = code;
} catch (error) {
  console.error(error.message);
  process.exitCode = 1;
} finally {
  if (service) await service.kill();
}
