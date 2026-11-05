@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion
REM ============================================================
REM  Walnut stop script (Windows)
REM  Usage: stop.cmd [--port N] [--pid-dir <dir>] [--timeout secs]
REM
REM  Env:
REM    WN_AGENT_NAME - caller agent code name; if the lock records a
REM                    different owner, we refuse to stop the instance.
REM
REM  Stop order: send "stop" to admin-port (graceful, triggers
REM              WnSetup destroy) -> wait for exit -> taskkill fallback
REM
REM  NOTE: keep messages ASCII-only (see start.cmd header).
REM  Contract: doc/reference/102.auto-test/20-at-setup.md
REM ============================================================
set "DIST_HOME=%~dp0.."
set "PORT="
set "PID_DIR="
set "TIMEOUT=15"

:parse
if "%~1"=="" goto parsed
if /i "%~1"=="--port" (set "PORT=%~2"& shift& shift& goto parse)
if /i "%~1"=="--pid-dir" (set "PID_DIR=%~2"& shift& shift& goto parse)
if /i "%~1"=="--timeout" (set "TIMEOUT=%~2"& shift& shift& goto parse)
echo *** unknown arg: %~1 1>&2
exit /b 1
:parsed

if "%PID_DIR%"=="" set "PID_DIR=%DIST_HOME%\run"

REM ---------- locate run lock ----------
if "%PORT%"=="" (
  set "PID_FILE="
  set "LS_FILE=%TEMP%\_wn_ls_%RANDOM%%RANDOM%.txt"
  powershell -NoProfile -Command "$fs = @(Get-ChildItem '%PID_DIR%\pid-*.json' -ErrorAction SilentlyContinue); if ($fs.Count -eq 1) { Write-Output $fs[0].FullName } else { Write-Output ('__COUNT_' + $fs.Count + '__') }" >"!LS_FILE!" 2>nul
  set /p PID_FILE=<"!LS_FILE!"
  del "!LS_FILE!" >nul 2>nul
  if not defined PID_FILE (echo *** no --port given and pid dir not readable: %PID_DIR% 1>&2 & exit /b 1)
  echo !PID_FILE! | findstr /C:"__COUNT_" >nul
  if not errorlevel 1 (
    echo !PID_FILE! | findstr /C:"__COUNT_0__" >nul
    if not errorlevel 1 (echo *** no instance lock in %PID_DIR% ^(already stopped?^) 1>&2) else (echo *** multiple pid files in %PID_DIR%, use --port please 1>&2)
    exit /b 1
  )
) else (
  set "PID_FILE=%PID_DIR%\pid-%PORT%.json"
)
if not exist "%PID_FILE%" (echo *** run lock not found: %PID_FILE% 1>&2 & exit /b 2)

REM ---------- read pid / admin_port / owner ----------
set "INFO="
set "INFO_FILE=%TEMP%\_wn_info_%RANDOM%%RANDOM%.txt"
powershell -NoProfile -Command "$j = Get-Content -Raw '%PID_FILE%' | ConvertFrom-Json; '{0}|{1}|{2}|{3}' -f $j.pid, $j.admin_port, $j.agent, $j.work_path" >"%INFO_FILE%" 2>nul
set /p INFO=<"%INFO_FILE%"
del "%INFO_FILE%" >nul 2>nul
for /f "tokens=1-4 delims=|" %%A in ("%INFO%") do (set "WPID=%%A"& set "ADMIN_PORT=%%B"& set "AGENT=%%C"& set "WPATH=%%D")
if not defined WPID (echo *** invalid run lock: %PID_FILE% 1>&2 & exit /b 2)

REM ---------- ownership check ----------
if defined AGENT (
  if not "%AGENT%"=="%WN_AGENT_NAME%" (
    echo *** this instance belongs to agent "%AGENT%" ^(%WPATH%^), not you. ask the owner first 1>&2
    exit /b 2
  )
)

REM ---------- gentle stop via admin-port ----------
powershell -NoProfile -Command "try { $c = New-Object Net.Sockets.TcpClient('127.0.0.1', [int]%ADMIN_PORT%); $s = $c.GetStream(); $b = [Text.Encoding]::ASCII.GetBytes(\"stop`n\"); $s.Write($b, 0, $b.Length); Start-Sleep -Milliseconds 300; $c.Close(); 'SENT' } catch { 'FAIL' }" 1>nul

REM ---------- wait for exit (ping doubles as sleep; timeout needs a console) ----------
set "GENTLE=true"
set /a N=0
:wait
tasklist /FI "PID eq %WPID%" 2>nul | findstr /C:"%WPID%" >nul
if errorlevel 1 goto stopped
ping -n 2 127.0.0.1 >nul
set /a N+=1
if %N% lss %TIMEOUT% goto wait

REM ---------- fallback: taskkill, polite then /F ----------
echo ... timed out, fallback to taskkill pid=%WPID% 1>&2
taskkill /PID %WPID% >nul 2>&1
ping -n 3 127.0.0.1 >nul
tasklist /FI "PID eq %WPID%" 2>nul | findstr /C:"%WPID%" >nul
if not errorlevel 1 taskkill /F /PID %WPID% >nul 2>&1
set "GENTLE=false"

:stopped
del "%PID_FILE%" >nul 2>nul
powershell -NoProfile -Command "@{action='stop'; gentle=$%GENTLE%; pid=%WPID%; port=$null} | ConvertTo-Json -Compress"
exit /b 0
