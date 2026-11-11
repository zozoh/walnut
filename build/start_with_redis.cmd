@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion
REM ============================================================
REM  Walnut start WITH a private redis process (agent test tool)
REM  Usage: start_with_redis.cmd [--conf <dir>] [--pid-dir <dir>]
REM
REM  For level 2/3 tests that need FULL data isolation: brings up
REM  a dedicated redis (native exe OR docker container, baked in
REM  at build time), then delegates the Walnut launch to
REM  start.cmd unchanged.
REM
REM  start.cmd / stop.cmd only ever handle the Walnut service
REM  itself - this wrapper adds the private redis lifecycle:
REM    print data sources (read from the effective conf) ->
REM    pre-check the walnut run lock -> bring redis up on the
REM    conf's redis-port (reuse a live one, refuse a stranger's
REM    port, recreate our own container when its baked port/volume
REM    no longer matches) -> call start.cmd -> register redis info
REM    into the walnut run lock (pid-<port>.json redis_*)
REM
REM  REDIS_SERVER (make.json) two forms, baked into this script:
REM    exe:    D:/opt/redis/redis-server.exe   (RD_SERVER below)
REM    docker: Docker:redis<sha256:...>=name   (RD_DOCKER_* below)
REM  Everything else (port/db/mongo/buck/swap) is read from the
REM  effective web_local.properties - the single unified source.
REM
REM  Env:
REM    JAVA_HOME       - preferred JDK (passed through)
REM    WN_AGENT_NAME   - agent code name (passed through)
REM    WN_WORK_PATH    - agent working dir (passed through)
REM
REM  NOTE: keep every user-facing message ASCII-only, and avoid
REM  unquoted parentheses inside (...) blocks - cmd.exe parses
REM  them at load time and Chinese bytes break under chcp 65001.
REM  Subroutines live at the top (before :main) - labels defined
REM  after the first call may not be found by cmd.exe.
REM
REM  Contract: wn-doc/public/reference/102.auto-test/20-at-setup.md
REM ============================================================
set "DIST_HOME=%~dp0.."
set "CONF_DIR="
set "CONF_GIVEN=false"
set "PID_DIR="

REM ---------- baked-in by wn4j-make.py from make.json REDIS_SERVER ----------
set "RD_SERVER=@REDIS_SERVER@"
set "RD_DOCKER_IMAGE=@RD_DOCKER_IMAGE@"
set "RD_DOCKER_IMAGE_ID=@RD_DOCKER_IMAGE_ID@"
set "RD_DOCKER_CONTAINER=@RD_DOCKER_CONTAINER@"
REM level-2 redis data home (Walnut repo .at/); level 3 derives from --conf
set "WALNUT_AT=@WALNUT_AT@"

:parse
if "%~1"=="" goto parsed
if /i "%~1"=="--conf" (set "CONF_DIR=%~2"& set "CONF_GIVEN=true"& shift& shift& goto parse)
if /i "%~1"=="--pid-dir" (set "PID_DIR=%~2"& shift& shift& goto parse)
echo *** unknown arg: %~1 1>&2
exit /b 1
:parsed

if "%CONF_DIR%"=="" set "CONF_DIR=%DIST_HOME%\conf"
if "%PID_DIR%"=="" set "PID_DIR=%DIST_HOME%\run"
if not exist "%PID_DIR%" mkdir "%PID_DIR%" >nul
if not defined RD_SERVER if not defined RD_DOCKER_IMAGE_ID (
  echo *** no redis source baked in - this script was built without REDIS_SERVER. 1>&2
  echo *** use start.cmd for a plain walnut launch 1>&2
  exit /b 1
)
if defined RD_SERVER if not exist "%RD_SERVER%" (echo *** redis-server exe not found: %RD_SERVER% 1>&2 & exit /b 3)
goto main

REM ============================================================
REM  subroutine: read one flat key=value from the effective conf
REM  usage: call :conf_key <key> <varname>
REM  scans CONF_DIR\web_local.properties first, then core\conf\web.properties
REM ============================================================
:conf_key
set "%~2="
for %%F in ("%CONF_DIR%\web_local.properties" "%DIST_HOME%\core\conf\web.properties") do (
  if exist "%%~fF" if not defined %~2 (
    REM one findstr per file (it returns only the matching lines) - the
    REM old line-by-line echo|findstr cost one process spawn per line,
    REM 16 keys x 324 lines ~= 57s on this machine; this is ~1s
    for /f "usebackq tokens=1,2 delims==" %%A in (`findstr /i /r /c:"^%~1=" "%%~fF"`) do (
      if not defined %~2 set "%~2=%%B"
    )
  )
)
exit /b 0

REM ============================================================
REM  subroutine: verify the container's data volume source resolves
REM  to this run's RD_DIR (a container created by an older layout
REM  mounts somewhere else - refuse instead of writing the wrong dir)
REM  expects DOCKER_EXE / RD_DOCKER_CONTAINER / RD_DIR. exit 0 = ok
REM ============================================================
:docker_mount_ok
powershell -NoProfile -Command "$q=[char]34; $p = & '%DOCKER_EXE%' inspect -f '{{json .Mounts}}' '%RD_DOCKER_CONTAINER%' 2>$null | Out-String; $re = $q+'Source'+$q+'\s*:\s*'+$q+'([^'+$q+']+)'+$q; $src = $null; if ($p -match $re) { $src = $Matches[1] }; if (-not $src) { exit 1 }; $srcR = [IO.Path]::GetFullPath($src); $wantR = [IO.Path]::GetFullPath('%RD_DIR%'); if ($srcR -ieq $wantR) { exit 0 }; exit 1"
exit /b %errorlevel%

REM ============================================================
REM  subroutine: kill the redis process we started this run
REM  (reused private redis is never touched here)
REM ============================================================
:cleanup_redis
if not "%REDIS_STARTED%"=="true" exit /b 0
echo cleaning up private redis pid=%REDIS_PID% 1>&2
taskkill /PID %REDIS_PID% >nul 2>&1
if defined REDIS_LOCK del "%REDIS_LOCK%" >nul 2>nul
exit /b 0

:main
REM ---------- read everything from the effective conf ----------
call :conf_key app-port APP_PORT
call :conf_key admin-port ADMIN_PORT
call :conf_key redis-host RD_HOST
call :conf_key redis-port RD_PORT
call :conf_key redis-database RD_DB
call :conf_key redis-password RD_PASS
call :conf_key mongo-host MONGO_HOST
call :conf_key mongo-port MONGO_PORT
call :conf_key mongo-db MONGO_DB
call :conf_key global-bm-bucket BUCK_DIR
call :conf_key global-bm-swap SWAP_DIR
call :conf_key mysql-host MY_HOST
call :conf_key mysql-port MY_PORT
call :conf_key mysql-db MY_DB
call :conf_key mysql-usr MY_USR
set "APP_PORT=%APP_PORT: =%"
set "RD_PORT=%RD_PORT: =%"
if not defined APP_PORT set "APP_PORT=8080"
if not defined ADMIN_PORT set /a ADMIN_PORT=%APP_PORT%+1
if not defined RD_HOST set "RD_HOST=127.0.0.1"
if not defined RD_PORT set "RD_PORT=6379"
if not defined RD_DB set "RD_DB=0"
if not defined MONGO_HOST set "MONGO_HOST=127.0.0.1"
if not defined MONGO_PORT set "MONGO_PORT=27017"
if not defined MONGO_DB set "MONGO_DB=walnut"
if not defined BUCK_DIR set "BUCK_DIR=%DIST_HOME%\run\buck"
if not defined SWAP_DIR set "SWAP_DIR=%DIST_HOME%\run\swap"
REM private redis only makes sense on this machine
if /i not "%RD_HOST%"=="127.0.0.1" if /i not "%RD_HOST%"=="localhost" (
  echo *** redis-host is %RD_HOST% - a private redis process only makes sense locally. 1>&2
  echo *** use start.cmd to connect to a remote redis 1>&2
  exit /b 1
)

REM ---------- data sources (print before anything starts) ----------
REM redis persistence dir (flat data dir, RDB inside):
REM   level 2 (default conf) -> WALNUT_AT\redis\data (Walnut repo .at/, baked)
REM   level 3 (--conf)       -> <work>\.at\redis\data, derived from the conf
REM     dir (<conf>\..\..) — the agent working dir owns its own data, so
REM     several level-3 instances never share RDB files
if "%CONF_GIVEN%"=="true" (
  set "RD_DIR=%CONF_DIR%\..\..\redis\data"
) else (
  set "RD_DIR=%WALNUT_AT%\redis\data"
)
for %%I in ("%RD_DIR%") do set "RD_DIR=%%~fI"
if not exist "%RD_DIR%" mkdir "%RD_DIR%" >nul
if defined RD_DOCKER_IMAGE_ID (
  echo data sources: 1>&2
  echo   redis: %RD_HOST%:%RD_PORT% db=%RD_DB% [private container] 1>&2
  echo     img: %RD_DOCKER_IMAGE% "[%RD_DOCKER_IMAGE_ID%]" 1>&2
  echo     ctr: %RD_DOCKER_CONTAINER% 1>&2
  echo     dir: %RD_DIR% 1>&2
) else (
  echo data sources: 1>&2
  echo   redis: %RD_HOST%:%RD_PORT% db=%RD_DB% [private] 1>&2
  echo     exe: %RD_SERVER% 1>&2
  echo     dir: %RD_DIR% 1>&2
)
echo   mongo: %MONGO_HOST%:%MONGO_PORT% db=%MONGO_DB% 1>&2
if defined MY_HOST echo   mysql: %MY_HOST%:%MY_PORT% db=%MY_DB% usr=%MY_USR% [info only] 1>&2
echo   buck : %BUCK_DIR% 1>&2
echo   swap : %SWAP_DIR% 1>&2
echo   app  : http://127.0.0.1:%APP_PORT% 1>&2
echo   admin: 127.0.0.1:%ADMIN_PORT% 1>&2

REM ---------- refuse double-start (mirror start.cmd's rule) ----------
set "PID_FILE=%PID_DIR%\pid-%APP_PORT%.json"
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

REM ============================================================
REM  Docker mode: a pinned local image runs as a named container
REM ============================================================
if defined RD_DOCKER_IMAGE_ID goto docker_redis

REM ============================================================
REM  Native exe mode
REM ============================================================
set "RD_MODE=exe"
set "REDIS_PID="
set "REDIS_LOCK=%PID_DIR%\redis-%RD_PORT%.json"
set "REDIS_STARTED=false"

REM existing lock? reuse a live one (its RDB may hold data - persistence
REM is intentional), drop it when the process is already gone (stale)
set "RLK_PID="
set "RLK_FILE=%TEMP%\_wn_rlk_%RANDOM%%RANDOM%.txt"
if exist "%REDIS_LOCK%" (
  powershell -NoProfile -Command "$j=$null; try { $j = Get-Content -Raw '%REDIS_LOCK%' | ConvertFrom-Json } catch {}; if ($j -and $j.pid -and (Get-Process -Id $j.pid -ErrorAction SilentlyContinue)) { Write-Output $j.pid }" >"!RLK_FILE!" 2>nul
  set /p RLK_PID=<"!RLK_FILE!"
)
del "!RLK_FILE!" >nul 2>nul
if defined RLK_PID (
  set "REDIS_PID=%RLK_PID%"
  echo reusing private redis: port=%RD_PORT% pid=%RLK_PID% 1>&2
  goto redis_ready
)
if exist "%REDIS_LOCK%" del "%REDIS_LOCK%" >nul 2>nul

REM no lock: the port must be free - cannot tell whose redis it is otherwise
set "RPORT_FILE=%TEMP%\_wn_rport_%RANDOM%%RANDOM%.txt"
powershell -NoProfile -Command "$r='CLOSED'; try { $c=New-Object Net.Sockets.TcpClient; $c.Connect('%RD_HOST%',[int]%RD_PORT%); $r='OPEN'; $c.Close() } catch {}; Write-Output $r" >"!RPORT_FILE!" 2>nul
set /p RPORT_ST=<"!RPORT_FILE!"
del "!RPORT_FILE!" >nul 2>nul
if "%RPORT_ST%"=="OPEN" (echo *** redis port %RD_HOST%:%RD_PORT% already in use, and no run lock %REDIS_LOCK% - refusing to touch a stranger's redis 1>&2 & exit /b 3)

REM launch redis-server (working dir = persistence dir; RDB stays on)
REM NOTE: stdin is redirected to a dummy file - an inherited stdin
REM handle would keep the caller's pipe open and hang `cmd | tee`
set "REDIS_LOG=%PID_DIR%\redis-%RD_PORT%.log"
set "REDIS_ELOG=%PID_DIR%\redis-%RD_PORT%.err"
set "REDIS_ARGS=--port %RD_PORT%"
if defined RD_PASS set "REDIS_ARGS=%REDIS_ARGS% --requirepass %RD_PASS%"
set "RSTDIN_FILE=%TEMP%\_wn_rstdin_%RANDOM%%RANDOM%.txt"
type nul >"%RSTDIN_FILE%"
set "RPID_FILE=%TEMP%\_wn_rstart_%RANDOM%%RANDOM%.txt"
powershell -NoProfile -Command "$p = Start-Process -FilePath '%RD_SERVER%' -ArgumentList '%REDIS_ARGS%' -WorkingDirectory '%RD_DIR%' -WindowStyle Hidden -RedirectStandardInput '%RSTDIN_FILE%' -RedirectStandardOutput '%REDIS_LOG%' -RedirectStandardError '%REDIS_ELOG%' -PassThru; $p.Id | Out-File -Encoding ascii '%RPID_FILE%'" || (echo *** redis launch failed, see %REDIS_ELOG% 1>&2 & exit /b 4)
del "%RSTDIN_FILE%" >nul 2>nul
set /p REDIS_PID=<"%RPID_FILE%"
del "%RPID_FILE%" >nul 2>nul
if not defined REDIS_PID (echo *** cannot get redis pid 1>&2 & exit /b 4)
set "REDIS_STARTED=true"
powershell -NoProfile -Command "$o = [ordered]@{pid=[long]%REDIS_PID%; port=[int]%RD_PORT%; exe='%RD_SERVER%'; dir='%RD_DIR%'; agent=$env:WN_AGENT_NAME; started_at=(Get-Date -Format 'yyyy-MM-ddTHH:mm:ss')}; $o | ConvertTo-Json -Compress | Out-File -Encoding utf8 '%REDIS_LOCK%'" || (echo *** redis lock write failed 1>&2 & call :cleanup_redis & exit /b 4)
goto redis_ready

REM ============================================================
REM  Docker mode implementation
REM ============================================================
:docker_redis
set "RD_MODE=docker"
set "REDIS_LOCK=%PID_DIR%\redis-%RD_PORT%.json"
set "REDIS_STARTED=false"

REM locate docker.exe (PATH first, then the well-known Desktop location).
REM NOTE: ask where.exe for docker.exe explicitly - Docker Desktop's bin dir
REM also ships an extensionless bash shim named "docker" which where.exe
REM returns first; cmd tolerates it (PATHEXT retry) but powershell & does not
set "DOCKER_EXE="
for /f "usebackq delims=" %%J in (`where.exe docker.exe 2^>nul`) do if not defined DOCKER_EXE set "DOCKER_EXE=%%J"
if not defined DOCKER_EXE if exist "C:\Program Files\Docker\Docker\resources\bin\docker.exe" set "DOCKER_EXE=C:\Program Files\Docker\Docker\resources\bin\docker.exe"
if not defined DOCKER_EXE (echo *** docker.exe not found - neither in PATH nor the Docker Desktop install dir 1>&2 & exit /b 3)

REM daemon up?
"%DOCKER_EXE%" info >nul 2>&1
if errorlevel 1 (echo *** docker daemon not responding - is Docker Desktop running? 1>&2 & exit /b 3)

REM image must exist locally - the id pins an exact image, never auto-pull
"%DOCKER_EXE%" image inspect "%RD_DOCKER_IMAGE_ID%" >nul 2>&1
if errorlevel 1 (echo *** docker image not found locally: %RD_DOCKER_IMAGE_ID% "%RD_DOCKER_IMAGE%" 1>&2 & echo *** pull it manually first: docker pull %RD_DOCKER_IMAGE% 1>&2 & exit /b 3)
set "REDIS_ELOG=%PID_DIR%\redis-%RD_PORT%.err"

REM container state: missing / running / exited
set "DCK_ST="
set "DCK_FILE=%TEMP%\_wn_dck_%RANDOM%%RANDOM%.txt"
"%DOCKER_EXE%" inspect -f "{{.State.Status}}" "%RD_DOCKER_CONTAINER%" >"%DCK_FILE%" 2>nul
if not errorlevel 1 set /p DCK_ST=<"%DCK_FILE%"
del "%DCK_FILE%" >nul 2>nul

if "%DCK_ST%"=="running" goto docker_reuse
if "%DCK_ST%"=="exited" goto docker_restart
if not "%DCK_ST%"=="" (echo *** container %RD_DOCKER_CONTAINER% is in unexpected state: %DCK_ST% - remove it manually 1>&2 & exit /b 3)
REM no container: fall through to :docker_fresh (host port must be free)

:docker_fresh
set "RPORT_FILE=%TEMP%\_wn_rport_%RANDOM%%RANDOM%.txt"
powershell -NoProfile -Command "$r='CLOSED'; try { $c=New-Object Net.Sockets.TcpClient; $c.Connect('%RD_HOST%',[int]%RD_PORT%); $r='OPEN'; $c.Close() } catch {}; Write-Output $r" >"!RPORT_FILE!" 2>nul
set /p RPORT_ST=<"!RPORT_FILE!"
del "!RPORT_FILE!" >nul 2>nul
if "%RPORT_ST%"=="OPEN" (echo *** redis port %RD_HOST%:%RD_PORT% already in use, and no container %RD_DOCKER_CONTAINER% - refusing to touch a stranger's redis 1>&2 & exit /b 3)

REM create the container (RDB volume = per-port dir under the run lock dir)
echo creating redis container: %RD_DOCKER_CONTAINER% image=%RD_DOCKER_IMAGE_ID% 1>&2
set "RD_DIR_M=%RD_DIR:\=/%"
set "DCK_CID_FILE=%TEMP%\_wn_dcid_%RANDOM%%RANDOM%.txt"
set "DCK_RUN_ARGS=-d --name %RD_DOCKER_CONTAINER% -p 127.0.0.1:%RD_PORT%:6379 -v %RD_DIR_M%:/data %RD_DOCKER_IMAGE_ID%"
if defined RD_PASS set "DCK_RUN_ARGS=%DCK_RUN_ARGS% --requirepass %RD_PASS%"
"%DOCKER_EXE%" run %DCK_RUN_ARGS% >"%DCK_CID_FILE%" 2>"%REDIS_ELOG%"
if errorlevel 1 (del "%DCK_CID_FILE%" >nul 2>nul & echo *** docker run failed, see %REDIS_ELOG% 1>&2 & exit /b 4)
set "DCK_CID="
set /p DCK_CID=<"%DCK_CID_FILE%"
del "%DCK_CID_FILE%" >nul 2>nul
if not defined DCK_CID (echo *** docker run gave no container id, see %REDIS_ELOG% 1>&2 & exit /b 4)
set "REDIS_STARTED=true"
goto docker_wait

:docker_reuse
:docker_restart
REM an existing container is reused only when BOTH its static config matches
REM this run: host port binding == conf redis-port, and data volume == this
REM run's data dir. Port bindings are immutable after creation, and a volume
REM from an older layout holds its RDB elsewhere - in either case the fixed
REM thing to do is recreate the container (RDB lives in the mounted host dir,
REM never in the container itself, so rm -f loses nothing)
powershell -NoProfile -Command "$p = & '%DOCKER_EXE%' inspect -f '{{json .HostConfig.PortBindings}}' '%RD_DOCKER_CONTAINER%' 2>$null | Out-String; if ($p -match 'HostPort[\D]*%RD_PORT%') { exit 0 }; exit 1"
if errorlevel 1 goto docker_recreate
call :docker_mount_ok
if errorlevel 1 goto docker_recreate
if "%DCK_ST%"=="running" (echo reusing private redis container: %RD_DOCKER_CONTAINER% port=%RD_PORT% 1>&2 & goto docker_lock)
echo starting existing redis container: %RD_DOCKER_CONTAINER% 1>&2
"%DOCKER_EXE%" start "%RD_DOCKER_CONTAINER%" >nul 2>&1
if errorlevel 1 (echo *** docker start failed 1>&2 & exit /b 4)
goto docker_wait

:docker_recreate
echo recreating container %RD_DOCKER_CONTAINER% for host port %RD_PORT% 1>&2
echo   (RDB persists in the data dir %RD_DIR% - nothing is lost) 1>&2
"%DOCKER_EXE%" rm -f "%RD_DOCKER_CONTAINER%" >nul 2>&1
if errorlevel 1 (echo *** docker rm -f %RD_DOCKER_CONTAINER% failed 1>&2 & exit /b 4)
goto docker_fresh

REM wait for the redis TCP port (docker-proxy may take a few seconds)
:docker_wait
set /a RN=0
:docker_wait_loop
set "RW_FILE=%TEMP%\_wn_rwait_%RANDOM%%RANDOM%.txt"
powershell -NoProfile -Command "$r='CLOSED'; try { $c=New-Object Net.Sockets.TcpClient; $c.Connect('127.0.0.1',[int]%RD_PORT%); $r='OPEN'; $c.Close() } catch {}; Write-Output $r" >"!RW_FILE!" 2>nul
set /p RW_ST=<"!RW_FILE!"
del "!RW_FILE!" >nul 2>nul
if "%RW_ST%"=="OPEN" goto docker_lock
ping -n 2 127.0.0.1 >nul
set /a RN+=1
if %RN% lss 30 goto docker_wait_loop
echo ... redis container not answering on port %RD_PORT% after ~30s, continue anyway (see %REDIS_ELOG%) 1>&2

REM write the redis run lock (pid stays 0 - docker containers have no
REM usable host pid on Docker Desktop; liveness goes through docker inspect)
:docker_lock
powershell -NoProfile -Command "$o = [ordered]@{pid=0; port=[int]%RD_PORT%; exe='docker'; container='%RD_DOCKER_CONTAINER%'; agent=$env:WN_AGENT_NAME; started_at=(Get-Date -Format 'yyyy-MM-ddTHH:mm:ss')}; $o | ConvertTo-Json -Compress | Out-File -Encoding utf8 '%REDIS_LOCK%'" || (echo *** redis lock write failed 1>&2 & exit /b 4)
goto redis_ready

REM ============================================================
REM  common: best-effort readiness probe, then start walnut
REM ============================================================
:redis_ready
set "REDIS_CLI="
if defined RD_SERVER for %%I in ("%RD_SERVER%") do if exist "%%~dpIredis-cli.exe" set "REDIS_CLI=%%~dpIredis-cli.exe"
if not defined REDIS_CLI for /f "usebackq delims=" %%J in (`where.exe redis-cli 2^>nul`) do if not defined REDIS_CLI set "REDIS_CLI=%%J"
set "REDIS_CLI_AUTH="
if defined RD_PASS set "REDIS_CLI_AUTH=-a %RD_PASS%"
if not defined REDIS_CLI goto redis_nocli
set /a RN=0
:redis_ping
"%REDIS_CLI%" -p %RD_PORT% %REDIS_CLI_AUTH% ping 2>nul | findstr /C:"PONG" >nul
if not errorlevel 1 goto redis_done
ping -n 2 127.0.0.1 >nul
set /a RN+=1
if %RN% lss 20 goto redis_ping
echo ... redis not answering PONG after 20s, continue anyway (see %REDIS_ELOG%) 1>&2
goto redis_done
:redis_nocli
ping -n 3 127.0.0.1 >nul
:redis_done

REM ---------- delegate the walnut launch to start.cmd (unchanged) ----------
REM NOTE: conf/pid-dir paths must not contain spaces (toolchain-wide rule)
set "SCMD=%DIST_HOME%\sbin\start.cmd"
set "SARGS="
if defined CONF_DIR set "SARGS=%SARGS% --conf %CONF_DIR%"
if defined PID_DIR set "SARGS=%SARGS% --pid-dir %PID_DIR%"
echo starting walnut via start.cmd: %SARGS% 1>&2
call "%SCMD%" %SARGS%
set "SRC=%errorlevel%"
if not "%SRC%"=="0" (
  echo *** start.cmd failed exit=%SRC%, cleaning up private redis 1>&2
  if "%RD_MODE%"=="docker" (
    if "%REDIS_STARTED%"=="true" "%DOCKER_EXE%" rm -f "%RD_DOCKER_CONTAINER%" >nul 2>&1
  ) else (
    call :cleanup_redis
  )
  exit /b %SRC%
)

REM ---------- register redis info into the walnut run lock ----------
set "WLOCK=%PID_DIR%\pid-%APP_PORT%.json"
if not exist "%WLOCK%" (
  REM start.cmd may have resolved a different port - take the newest lock
  for /f "usebackq delims=" %%F in (`powershell -NoProfile -Command "$f = Get-ChildItem '%PID_DIR%\pid-*.json' -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1; if ($f) { $f.FullName }"`) do set "WLOCK=%%F"
)
if not exist "%WLOCK%" (echo *** walnut started but no run lock found in %PID_DIR% - redis NOT registered, stop it manually 1>&2 & exit /b 4)
if "%RD_MODE%"=="docker" (set "RD_REG_PID=0") else (set "RD_REG_PID=%REDIS_PID%")
powershell -NoProfile -Command "$j = Get-Content -Raw '%WLOCK%' | ConvertFrom-Json; $j | Add-Member -NotePropertyName redis_pid -NotePropertyValue '%RD_REG_PID%' -Force; $j | Add-Member -NotePropertyName redis_port -NotePropertyValue '%RD_PORT%' -Force; $j | Add-Member -NotePropertyName redis_exe -NotePropertyValue '%RD_SERVER%' -Force; $j | Add-Member -NotePropertyName redis_dir -NotePropertyValue '%RD_DIR%' -Force; $j | Add-Member -NotePropertyName redis_pass -NotePropertyValue '%RD_PASS%' -Force; $j | Add-Member -NotePropertyName redis_mode -NotePropertyValue '%RD_MODE%' -Force; $j | Add-Member -NotePropertyName redis_container -NotePropertyValue '%RD_DOCKER_CONTAINER%' -Force; $j | ConvertTo-Json -Compress | Out-File -Encoding utf8 '%WLOCK%'" || (echo *** cannot register redis into %WLOCK% 1>&2 & exit /b 4)
echo registered private redis: lock=%WLOCK% mode=%RD_MODE% 1>&2
exit /b 0
