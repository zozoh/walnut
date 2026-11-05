@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion
REM ============================================================
REM  Walnut stop WITH the private redis process (agent test tool)
REM  Usage: stop_with_redis.cmd [--port N] [--pid-dir <dir>]
REM                             [--timeout secs]
REM
REM  Companion of start_with_redis.cmd: delegates the walnut
REM  shutdown to stop.cmd unchanged, then stops the private
REM  redis that start_with_redis.cmd launched (redis_* fields in
REM  the walnut run lock):
REM    read redis fields from run lock -> call stop.cmd ->
REM    stop the private redis (exe: redis-cli shutdown save;
REM    docker: docker stop) -> wait -> kill fallback -> remove lock
REM
REM  NOTE: keep every user-facing message ASCII-only, and avoid
REM  unquoted parentheses inside (...) blocks - cmd.exe parses
REM  them at load time and Chinese bytes break under chcp 65001.
REM  Subroutines live at the top (before :main) - labels defined
REM  after the first call may not be found by cmd.exe.
REM
REM  Contract: doc/reference/102.auto-test/20-at-setup.md
REM ============================================================
set "DIST_HOME=%~dp0.."
set "PORT="
set "PID_DIR="
set "TIMEOUT=5"

:parse
if "%~1"=="" goto parsed
if /i "%~1"=="--port" (set "PORT=%~2"& shift& shift& goto parse)
if /i "%~1"=="--pid-dir" (set "PID_DIR=%~2"& shift& shift& goto parse)
if /i "%~1"=="--timeout" (set "TIMEOUT=%~2"& shift& shift& goto parse)
echo *** unknown arg: %~1 1>&2
exit /b 1
:parsed

if "%PID_DIR%"=="" set "PID_DIR=%DIST_HOME%\run"
if not exist "%PID_DIR%" mkdir "%PID_DIR%" >nul
goto main

REM ============================================================
REM  subroutine: locate docker (PATH first, then the well-known
REM  Docker Desktop install dir). usage: call :find_docker
REM ============================================================
:find_docker
set "DOCKER_EXE="
for /f "usebackq delims=" %%J in (`where.exe docker 2^>nul`) do if not defined DOCKER_EXE set "DOCKER_EXE=%%J"
if not defined DOCKER_EXE if exist "C:\Program Files\Docker\Docker\resources\bin\docker.exe" set "DOCKER_EXE=C:\Program Files\Docker\Docker\resources\bin\docker.exe"
exit /b 0

REM ============================================================
REM  subroutine: stop the private redis (exe mode)
REM  expects REDIS_PID / REDIS_PORT / REDIS_EXE / REDIS_PASS /
REM  PID_DIR. redis-cli shutdown save - this Windows build's
REM  bare SHUTDOWN does not persist, SAVE is required.
REM ============================================================
:stop_redis_exe
echo stopping private redis: port=%REDIS_PORT% pid=%REDIS_PID% 1>&2
set "REDIS_CLI="
for %%I in ("%REDIS_EXE%") do if exist "%%~dpIredis-cli.exe" set "REDIS_CLI=%%~dpIredis-cli.exe"
if not defined REDIS_CLI for /f "usebackq delims=" %%J in (`where.exe redis-cli 2^>nul`) do if not defined REDIS_CLI set "REDIS_CLI=%%J"
if defined REDIS_CLI (
  set "REDIS_CLI_AUTH="
  if defined REDIS_PASS set "REDIS_CLI_AUTH=-a %REDIS_PASS%"
  "%REDIS_CLI%" -p %REDIS_PORT% %REDIS_CLI_AUTH% shutdown save >nul 2>&1
)
set /a RN=0
:redis_wait
tasklist /FI "PID eq %REDIS_PID%" 2>nul | findstr /C:"%REDIS_PID%" >nul
if errorlevel 1 goto redis_stopped
ping -n 2 127.0.0.1 >nul
set /a RN+=1
if %RN% lss 20 goto redis_wait
echo ... redis did not exit, fallback to taskkill /F pid=%REDIS_PID% 1>&2
taskkill /F /PID %REDIS_PID% >nul 2>&1
:redis_stopped
exit /b 0

REM ============================================================
REM  subroutine: stop the private redis (docker mode)
REM  expects REDIS_CONTAINER / REDIS_PORT / PID_DIR
REM ============================================================
:stop_redis_docker
echo stopping private redis container: %REDIS_CONTAINER% 1>&2
call :find_docker
if not defined DOCKER_EXE (echo *** docker.exe not found - stop the container manually: docker stop %REDIS_CONTAINER% 1>&2 & exit /b 3)
"%DOCKER_EXE%" stop "%REDIS_CONTAINER%" >nul 2>&1
set /a RN=0
:redis_dwait
"%DOCKER_EXE%" inspect -f "{{.State.Running}}" "%REDIS_CONTAINER%" 2>nul | findstr /C:"true" >nul
if errorlevel 1 goto redis_dstopped
ping -n 2 127.0.0.1 >nul
set /a RN+=1
if %RN% lss 20 goto redis_dwait
echo ... container did not stop, fallback to docker kill %REDIS_CONTAINER% 1>&2
"%DOCKER_EXE%" kill "%REDIS_CONTAINER%" >nul 2>&1
:redis_dstopped
exit /b 0

REM ============================================================
REM  main
REM ============================================================
:main
REM ---------- locate the walnut run lock (same rule as stop.cmd) ----------
if "%PORT%"=="" (
  set "PID_FILE="
  for /f "usebackq delims=" %%F in (`powershell -NoProfile -Command "$fs = @(Get-ChildItem '%PID_DIR%\pid-*.json' -ErrorAction SilentlyContinue); if ($fs.Count -eq 1) { $fs[0].FullName }"`) do set "PID_FILE=%%F"
  if not defined PID_FILE (echo *** no --port given and pid lock not unique/readable in %PID_DIR% 1>&2 & exit /b 1)
) else (
  set "PID_FILE=%PID_DIR%\pid-%PORT%.json"
)
if not exist "%PID_FILE%" (echo *** run lock not found: %PID_FILE% 1>&2 & exit /b 2)

REM ---------- read redis fields from the lock BEFORE stop.cmd removes it ----------
REM NOTE: null/empty json fields are coalesced to '~' - for /f collapses
REM adjacent delimiters, which would shift every field after an empty one
set "INFO_FILE=%TEMP%\_wn_info_%RANDOM%%RANDOM%.txt"
powershell -NoProfile -Command "$j = Get-Content -Raw '%PID_FILE%' | ConvertFrom-Json; $f = @($j.redis_pid, $j.redis_port, $j.redis_exe, $j.redis_pass, $j.redis_mode, $j.redis_container) | ForEach-Object { if ($null -eq $_ -or $_ -eq '') { '~' } else { $_ } }; '{0}|{1}|{2}|{3}|{4}|{5}' -f $f" >"%INFO_FILE%" 2>nul
set /p INFO=<"%INFO_FILE%"
del "%INFO_FILE%" >nul 2>nul
for /f "tokens=1-6 delims=|" %%A in ("%INFO%") do (set "REDIS_PID=%%A"& set "REDIS_PORT=%%B"& set "REDIS_EXE=%%C"& set "REDIS_PASS=%%D"& set "REDIS_MODE=%%E"& set "REDIS_CONTAINER=%%F")
if "%REDIS_PID%"=="~" set "REDIS_PID="
if "%REDIS_PORT%"=="~" set "REDIS_PORT="
if "%REDIS_EXE%"=="~" set "REDIS_EXE="
if "%REDIS_PASS%"=="~" set "REDIS_PASS="
if "%REDIS_MODE%"=="~" set "REDIS_MODE="
if "%REDIS_CONTAINER%"=="~" set "REDIS_CONTAINER="
if not defined REDIS_MODE set "REDIS_MODE=exe"
if not defined REDIS_PORT set "REDIS_PORT=6379"
if not defined REDIS_PID if not defined REDIS_CONTAINER (
  echo no private redis in %PID_FILE% - plain stop.cmd is enough 1>&2
)

REM ---------- delegate the walnut shutdown to stop.cmd (unchanged) ----------
set "SCMD=%DIST_HOME%\sbin\stop.cmd"
set "SARGS="
if defined PORT set "SARGS=%SARGS% --port %PORT%"
if defined PID_DIR set "SARGS=%SARGS% --pid-dir %PID_DIR%"
if defined TIMEOUT set "SARGS=%SARGS% --timeout %TIMEOUT%"
call "%SCMD%" %SARGS%
set "SRC=%errorlevel%"
if not "%SRC%"=="0" (echo *** stop.cmd failed exit=%SRC%, leaving private redis running 1>&2 & exit /b %SRC%)

REM ---------- stop the private redis (graceful save) ----------
if not defined REDIS_PID if not defined REDIS_CONTAINER exit /b 0
if "%REDIS_MODE%"=="docker" (call :stop_redis_docker) else (call :stop_redis_exe)
set "REDIS_LOCK=%PID_DIR%\redis-%REDIS_PORT%.json"
if exist "%REDIS_LOCK%" del "%REDIS_LOCK%" >nul 2>nul
exit /b 0
