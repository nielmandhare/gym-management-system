# Recovery and crash diagnosis - 2026-10-22 / 23

Project file damage has been repaired. Offline analysis identifies a
Selenium-launched Firefox child initiating the registry call that crashed
Windows during the second incident. The underlying Windows fault is unresolved.
The user authorized Chrome testing as a workaround; Firefox remains blocked.

## Confirmed evidence

- Crash timestamps: 2026-10-22 23:31:03.650 and 23:38:01.727 IST.
- Subsequent boot events: 23:31:22 and 23:38:18 IST.
- Both crashes: SYSTEM_SERVICE_EXCEPTION (0x3B), access violation 0xC0000005,
  bucket `AV_nt!CmpKeySecurityIncrementReferenceCount`.
- Windows 11 Pro 22H2, kernel build 22621.4317. This release ended support on
  2024-10-08. A Windows Update software scan completed with zero updates offered.
- A scan of 73 first-party source files found no reboot, registry or native OS
  calls. A Node RADAR memory warning does not establish the cause of the crash.

Both minidumps resolve to the same stack using matching Microsoft symbols:

```text
nt!CmpKeySecurityIncrementReferenceCount+0x1b
nt!CmpReferenceSecurityNode+0x57
nt!CmRenameKey+0xd84
nt!NtRenameKey+0x3e3
nt!KiSystemServiceCopyEnd+0x25
```

The user copied the dumps locally using administrator PowerShell. The larger
`MEMORY.DMP` identifies the second thread's owner and parent chain:

```text
firefox.exe (11944) -> firefox.exe (13796) -> geckodriver.exe (14144)
  -> node.exe -> cmd.exe -> node.exe -> pwsh.exe -> codex.exe
```

The attached Registry process (PID 112) is not the owning executable. The first
crash's owning PID 10980 cannot be resolved from its small dump. The second
dump proves the Firefox/Selenium trigger, but does not establish whether the
underlying defect is in Windows or another kernel component.

Analysis used signed Microsoft DbgEng components against saved dumps only.
Evidence, debugger tools and process-chain output remain locally under the
git-ignored `test-results/crash-diagnostics/`. Dumps contain private memory;
do not commit or upload them publicly. No execution policy, sandbox, security
software or registry protections were disabled.

## Completed repair and workaround

`package.json`, `.vscode/tasks.json`, and `docs/local-testing-setup.md` were
zero-filled after an interrupted write. Restored contents using flushed
temporary files and atomic replacement; verified JSON, absence of NUL bytes,
and dependency agreement with the lockfile. Damaged originals remain under
`test-results/recovery/20261022-235018/`.

The project's Node and Python browser wrappers, plus the native smoke suite,
check `selenium/local-browser-status.json` before creating a browser. Firefox
is held locally. Chrome is the default for the authorized resumed tests.
Results are saved in browser-specific directories; see
[local testing setup](local-testing-setup.md).

Windows repair or migration to a supported OS remains separate work. A passing
Chrome run does not prove that the OS crash defect has been repaired.

References:
[Windows 11 Home and Pro lifecycle](https://learn.microsoft.com/en-us/lifecycle/products/windows-11-home-and-pro),
[installed build release notes](https://support.microsoft.com/en-us/servicing/os/windows-11/2024/10/october-8-2024-kb5044285-os-builds-22621-4317-and-22631-4317).
