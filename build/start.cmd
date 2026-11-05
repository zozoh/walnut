@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion
REM ============================================================
REM  Walnut start script (Windows)
REM  Usage: start.cmd [--conf <dir>] [--pid-dir <dir>]
REM
REM  Env:
REM    JAVA_HOME       - preferred JDK
REM    WN_AGENT_NAME   - agent code name (injected by wn-up)
REM    WN_WORK_PATH    - agent working dir (injected by wn-up)
REM
REM  NOTE: keep every user-facing message ASCII-only, and avoid
REM  unquoted parentheses inside (...) blocks - cmd.exe parses
REM  them at load time and Chinese bytes break under chcp 65001.
REM
REM  Contract: doc/reference/102.auto-test/20-at-setup.md
REM ============================================================
set "DIST_HOME=%~dp0.."
set "CONF_DIR="
set "PID_DIR="

:parse
if "%~1"=="" goto parsed
if /i "%~1"=="--conf" (set "CONF_DIR=%~2"& shift& shift& goto parse)
if /i "%~1"=="--pid-dir" (set "PID_DIR=%~2"& shift& shift& goto parse)
echo *** unknown arg: %~1 1>&2
exit /b 1
:parsed

if "%CONF_DIR%"=="" set "CONF_DIR=%DIST_HOME%\conf"
if "%PID_DIR%"=="" set "PID_DIR=%DIST_HOME%\run"
if not exist "%PID_DIR%" mkdir "%PID_DIR%" >nul

REM ---------- resolve ports: conf dir web_local.properties first ----------
set "APP_PORT="
set "ADMIN_PORT="
for %%F in ("%CONF_DIR%\web_local.properties" "%DIST_HOME%\core\conf\web.properties") do (
  if exist "%%~fF" (
    if "!APP_PORT!"=="" (
      for /f "usebackq tokens=1,2 delims==" %%A in ("%%~fF") do (
        if "!APP_PORT!"=="" (
          echo %%A|findstr /i /r "^app-port" >nul
          if !errorlevel! equ 0 set "APP_PORT=%%B"
        )
      )
    )
    if "!ADMIN_PORT!"=="" (
      for /f "usebackq tokens=1,2 delims==" %%A in ("%%~fF") do (
        if "!ADMIN_PORT!"=="" (
          echo %%A|findstr /i /r "^admin-port" >nul
          if !errorlevel! equ 0 set "ADMIN_PORT=%%B"
        )
      )
    )
  )
)
set "APP_PORT=%APP_PORT: =%"
set "ADMIN_PORT=%ADMIN_PORT: =%"
if "%APP_PORT%"=="" (echo *** cannot resolve app-port from config 1>&2 & exit /b 1)
if "%ADMIN_PORT%"=="" set /a ADMIN_PORT=%APP_PORT%+1

set "PID_FILE=%PID_DIR%\pid-%APP_PORT%.json"

REM ---------- refuse to double-start ----------
set "OLD_PID="
if exist "%PID_FILE%" (
  set "OPID_FILE=%TEMP%\_wn_oldpid_%RANDOM%%RANDOM%.txt"
  powershell -NoProfile -Command "$j=$null; try { $j = Get-Content -Raw '%PID_FILE%' | ConvertFrom-Json } catch {}; if ($j -and $j.pid -and (Get-Process -Id $j.pid -ErrorAction SilentlyContinue)) { Write-Output $j.pid }" >"!OPID_FILE!" 2>nul
  set /p OLD_PID=<"!OPID_FILE!"
  del "!OPID_FILE!" >nul 2>nul
)
if defined OLD_PID (
  echo *** instance already running on port %APP_PORT% pid=!OLD_PID! see %PID_FILE% 1>&2
  exit /b 2
)

REM ---------- locate java ----------
set "JAVA_EXE="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not defined JAVA_EXE for /f "usebackq delims=" %%J in (`where.exe java 2^>nul`) do if not defined JAVA_EXE set "JAVA_EXE=%%J"
if not defined JAVA_EXE (echo *** java not found, set JAVA_HOME or PATH 1>&2 & exit /b 1)

REM ---------- classpath: effective conf dir wins over core\conf ----------
set "CP=%CONF_DIR%;%DIST_HOME%\core\conf;%DIST_HOME%\core\walnut.jar;%DIST_HOME%\deps\*"
set "LOG_OUT=%PID_DIR%\walnut-%APP_PORT%.log"
set "LOG_ERR=%PID_DIR%\walnut-%APP_PORT%.err"

echo starting Walnut: port=%APP_PORT% admin=%ADMIN_PORT% 1>&2
echo   java: %JAVA_EXE% 1>&2
echo   conf: %CONF_DIR% 1>&2
echo   log : %LOG_OUT% 1>&2

REM ---------- launch JVM in background, capture PID ----------
set "WPID_FILE=%TEMP%\_wn_start_%RANDOM%%RANDOM%.txt"
powershell -NoProfile -Command "$p = Start-Process -FilePath '%JAVA_EXE%' -ArgumentList '-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -cp \"%CP%\" com.site0.walnut.web.WnLauncher' -WorkingDirectory '%DIST_HOME%' -WindowStyle Hidden -RedirectStandardOutput '%LOG_OUT%' -RedirectStandardError '%LOG_ERR%' -PassThru; $p.Id | Out-File -Encoding ascii '%WPID_FILE%'" || (echo *** JVM launch failed 1>&2 & exit /b 4)
set /p WPID=<"%WPID_FILE%"
del "%WPID_FILE%" >nul 2>nul
if not defined WPID (echo *** cannot get JVM pid 1>&2 & exit /b 4)

REM ---------- write run lock pid-<port>.json ----------
powershell -NoProfile -Command "$o = [ordered]@{pid=[long]%WPID%; app_port=[int]%APP_PORT%; admin_port=[int]%ADMIN_PORT%; agent=$env:WN_AGENT_NAME; work_path=$env:WN_WORK_PATH; conf_dir='%CONF_DIR%'; dist_home='%DIST_HOME%'; started_at=(Get-Date -Format 'yyyy-MM-ddTHH:mm:ss')}; $o | ConvertTo-Json -Compress | Out-File -Encoding utf8 '%PID_FILE%'" || (echo *** run lock write failed 1>&2 & exit /b 4)

echo readiness probe: curl http://localhost:%APP_PORT%/a/login_mode 1>&2
type "%PID_FILE%"
exit /b 0
