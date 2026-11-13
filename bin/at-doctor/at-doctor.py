#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
at-doctor — ATEST 环境自检（ATESET-14）

一条命令回答"这台机器现在能不能跑 at-run"：8 项检查，每项 ok / warn / fail。

    python bin/at-doctor/at-doctor.py [--dir <项目>] [--instance L1|L2]

检查项（id / 判据）:

    python     Python ≥ 3.10
    config     <dir>/atest.json 存在、合法 JSON、root 指向的目录存在
    cases      at-run --check 的 error_count = 0
    instances  .wn-instances.json 可解析，目标级别有合法 host+port
    reachable  wn-ping 返回 alive:true 且 agent_ready:true
    session    会话文件存在，或凭据文件可用（二者之一）
    tools      bin/ 下 at-run / at-report / at-baseline / wn-cli 都在
    report     <root>/report/ 可写（缺失时尝试创建）

不做的事：不登录、不执行用例、不改 atest.json / 实例登记 / 会话；除
"创建缺失的 report/ 并写一个临时探针文件"外不落盘，探针用完即删。

输出：人读逐行（stderr）+ stdout 最后一行单行 JSON
`{ok, checks:[{id,status,detail}], failures}`；warn 不影响退出码。
退出码：0 没有 fail（warn 允许）/ 1 有 fail / 2 工具自身不可用（项目目录不存在）。
零依赖 Python3；argv 传参不经 shell。
"""

import argparse
import json
import os
import re
import subprocess
import sys
import tempfile
from pathlib import Path
from typing import Any, NoReturn, Sequence

EXIT_OK = 0
EXIT_USAGE = 1
EXIT_ENV = 2

CONFIG_NAME = "atest.json"
INSTANCES_NAME = ".wn-instances.json"
ROOT_DEFAULT = "atest"
ROOT_RE = re.compile(r"^[A-Za-z0-9._-]+$")
CREDS_DEFAULT = ".at/credentials.json"
PYTHON_MIN = (3, 10)
TOOLS = ("at-run", "at-report", "at-baseline", "wn-cli")
STATUS_ORDER = {"ok": 0, "warn": 1, "fail": 2}

DESCRIPTION = """\
ATEST 环境自检：8 项检查（Python / atest.json / 用例 / 实例登记 / 可达 /
会话 / 工具 / 报告目录）。退出码 0 无 fail（warn 允许） / 1 有 fail / 2 自身不可用。"""

EPILOG = """\
8 项检查:
  python     当前解释器 ≥ 3.10
  config     <dir>/atest.json 存在、合法、root 目录存在
  cases      at-run --check 的 error_count = 0（warn 不算失败）
  instances  .wn-instances.json 可解析且目标级别有 host+port
  reachable  wn-ping：alive=true 且 agent_ready=true（允许纯密码登录）
  session    会话文件存在，或凭据文件可用（{"usr","passwd"} 非空）
  tools      bin/ 下 at-run / at-report / at-baseline / wn-cli 都在
  report     <root>/report/ 可写

判读:
  fail = 现在跑 at-run 一定过不去；warn = 能跑但有隐患；ok = 通过。
  退出码只看 fail：有 fail → 1；warn 不改变退出码。

示例:
  python bin/at-doctor/at-doctor.py
  python bin/at-doctor/at-doctor.py --instance L2
  python bin/at-doctor/at-doctor.py --dir D:/work/myproj
"""


class _Parser(argparse.ArgumentParser):
    """argparse 默认用退出码 2 报参数错误，与本工具的「2 = 自身不可用」冲突。"""

    def error(self, message: str) -> NoReturn:
        emit({"ok": False, "error": "%s（--help 看用法）" % message})
        raise SystemExit(EXIT_USAGE)


class EnvError(Exception):
    """环境不可用（依赖项缺失；在本工具里是某一项 fail，不是退出 2）"""


def force_utf8_stdio() -> None:
    """Windows 控制台默认按 cp936 编码，中文 JSON 进管道会变味。"""
    # typeshed 的 TextIO 没声明 reconfigure（它只在 TextIOWrapper 上），
    # 这里把两个流当边界收成 Any，免得 strict 报成员不存在。
    streams: tuple[Any, Any] = (sys.stdout, sys.stderr)
    for stream in streams:
        try:
            stream.reconfigure(encoding="utf-8")
        except (AttributeError, ValueError, OSError):
            pass


def emit(obj: object) -> None:
    print(json.dumps(obj, ensure_ascii=False))


def rel(path: str | Path, root: str | Path) -> str:
    try:
        return str(Path(path).resolve().relative_to(Path(root).resolve())
                   ).replace("\\", "/")
    except ValueError:
        return str(path)


def check(check_id: str, status: str, detail: str) -> dict[str, str]:
    return {"id": check_id, "status": status, "detail": detail}


def last_json_line(stdout: str) -> tuple[Any, str | None]:
    """取 stdout 最后一行 JSON；返回 (对象, 错误说明)。"""
    lines = [x for x in stdout.splitlines() if x.strip()]
    if not lines:
        return None, "stdout 为空"
    try:
        return json.loads(lines[-1].strip()), None
    except ValueError:
        return None, "stdout 最后一行不是 JSON"


def json_object(value: object) -> dict[str, Any] | None:
    """JSON 解析结果的收窄入口：是对象就返回，否则 None（Any 只停在边界）。"""
    if not isinstance(value, dict):
        return None
    # 收窄后的类型是 dict[Unknown, Unknown]，先落到具体形状再返回，
    # 免得 strict 的「返回类型部分未知」把它当成 Unknown 泄出去。
    obj: dict[str, Any] = value
    return obj


def child_env() -> dict[str, str]:
    env = os.environ.copy()
    env["PYTHONUTF8"] = "1"
    env["PYTHONIOENCODING"] = "utf-8"
    env["PYTHONDONTWRITEBYTECODE"] = "1"
    return env


def bin_dir() -> Path:
    return Path(__file__).resolve().parent.parent


def run_tool(script: str | Path, argv: Sequence[str], cwd: str | Path,
             timeout: float = 120.0) -> tuple[int, str, str]:
    """跑同仓工具（argv 传参，不经 shell）；返回 (rc, stdout, stderr)。"""
    proc = subprocess.run([sys.executable, str(script)] + [str(x) for x in argv],
                          cwd=str(cwd), env=child_env(),
                          capture_output=True, timeout=timeout)
    return (proc.returncode,
            proc.stdout.decode("utf-8", "replace"),
            proc.stderr.decode("utf-8", "replace"))


# ---------------------------------------------------------------- 8 项检查

def check_python() -> dict[str, str]:
    version = "%d.%d.%d" % sys.version_info[:3]
    if sys.version_info[:2] >= PYTHON_MIN:
        return check("python", "ok", "Python %s（要求 ≥ %d.%d）"
                     % ((version,) + PYTHON_MIN))
    return check("python", "fail", "Python %s < %d.%d：升级解释器"
                 % ((version,) + PYTHON_MIN))


def load_config(project: str | Path) -> tuple[dict[str, Any] | None, Path, str]:
    """读 atest.json；返回 (dict, 路径, 错误说明)。"""
    path = Path(project) / CONFIG_NAME
    if not path.is_file():
        return None, path, "找不到 %s：先跑 bin/at-init/at-init.py" % path
    try:
        raw = json.loads(path.read_text(encoding="utf-8-sig"))
    except (ValueError, OSError) as e:
        return None, path, "%s 不是合法 JSON：%s" % (path, e)
    data = json_object(raw)
    if data is None:
        return None, path, "%s 顶层应为对象（version / root ...）" % path
    return data, path, ""


def resolve_root(config: dict[str, Any],
                 config_path: Path) -> tuple[str | None, str]:
    """返回 (root 名, 错误说明)。"""
    root = config.get("root")
    if root is None or root == "":
        return ROOT_DEFAULT, ""
    if not isinstance(root, str) or root in (".", "..") \
            or not ROOT_RE.match(root):
        return None, "%s 的 root 不合法：%r" % (config_path, root)
    return root, ""


def check_config(project: str | Path, config: dict[str, Any] | None,
                 config_path: Path, config_error: str, root: str | None,
                 root_error: str) -> dict[str, str]:
    if config_error:
        return check("config", "fail", config_error)
    if root_error or root is None:
        return check("config", "fail", root_error)
    root_dir = Path(project) / root
    if not root_dir.is_dir():
        return check("config", "fail",
                     "root 指向的目录不存在：%s（%s 写了 root=%r）"
                     % (rel(root_dir, project), config_path.name, root))
    cfg: dict[str, Any] = config or {}
    instance = cfg.get("instance")
    return check("config", "ok",
                 "atest.json 合法；root=%s，目录存在；instance=%s"
                 % (root, instance or "（未填，at-run 会退出 2）"))


def check_cases(project: str | Path, root: str | None) -> dict[str, str]:
    runner = bin_dir() / "at-run" / "at-run.py"
    if not runner.is_file():
        return check("cases", "fail", "找不到 %s" % runner)
    argv: list[str] = ["--check", "--dir", str(project)]
    if root:
        argv += ["--root", root]
    try:
        rc, out, err = run_tool(runner, argv, project)
    except subprocess.TimeoutExpired:
        return check("cases", "fail", "at-run --check 120s 未返回")
    except OSError as e:
        return check("cases", "fail", "启动 at-run --check 失败：%s" % e)
    raw, problem = last_json_line(out)
    data = json_object(raw)
    if data is None:
        return check("cases", "fail",
                     "at-run --check 输出不可解析（rc=%d，%s）：%s"
                     % (rc, problem, (err.strip() or out.strip())[:200]))
    if data.get("error"):
        return check("cases", "fail",
                     "at-run --check 失败（rc=%d）：%s"
                     % (rc, data.get("error")))
    if data.get("ok") and rc == 0:
        warnings = data.get("warning_count", 0)
        detail = "at-run --check：%d 个用例文件，0 错误" % data.get(
            "checked", 0)
        if warnings:
            detail += "；%d 条警告（不阻塞）" % warnings
        return check("cases", "ok", detail)
    errors: list[dict[str, Any]] = data.get("errors") or []
    codes = sorted({str(e.get("code", "?")) for e in errors})
    first = ""
    if errors:
        first = "；首个：%s %s" % (errors[0].get("caseID") or "",
                                  errors[0].get("message") or "")
    return check("cases", "fail",
                 "at-run --check：%d 条错误（%s）%s"
                 % (data.get("error_count", len(errors)),
                    ", ".join(codes) or "未知错误码", first))


def find_instances(project: str | Path) -> tuple[Path | None, list[str]]:
    """$WN_INSTANCES > 从项目目录逐级向上 > 用户目录；返回 (文件|None, 搜过)。"""
    candidates: list[Path] = []
    env = os.environ.get("WN_INSTANCES")
    if env:
        candidates.append(Path(env))
    here = Path(project).resolve()
    candidates.extend([d / INSTANCES_NAME for d in [here] + list(here.parents)])
    candidates.append(Path.home() / INSTANCES_NAME)
    searched: list[str] = []
    for path in candidates:
        searched.append(str(path))
        if path.is_file():
            return path, searched
    return None, searched


def endpoint_of(level: str, instances: dict[str, Any]
                ) -> tuple[str, int] | None:
    ep = json_object(instances.get(level))
    if ep is None or not ep.get("port"):
        return None
    try:
        port = int(ep["port"])
    except (TypeError, ValueError):
        return None
    host = str(ep.get("host") or "127.0.0.1")
    return host, port


def check_instances(project: str | Path, level: str | None) -> tuple[
        dict[str, str], Path | None, tuple[str, int] | None]:
    """返回 (check, 实例登记路径, endpoint)。"""
    if not level:
        return (check("instances", "fail",
                      "实例级别未定：atest.json 缺 instance，且未给 --instance"),
                None, None)
    env_path = os.environ.get("WN_INSTANCES")
    if env_path and not Path(env_path).is_file():
        return (check("instances", "fail",
                      "$WN_INSTANCES 指定的实例登记文件不存在：%s"
                      % env_path), None, None)
    path, searched = find_instances(project)
    if path is None:
        return (check("instances", "fail",
                      "找不到 %s（从项目目录逐级向上找了 %d 处，最后用户目录）"
                      % (INSTANCES_NAME, len(searched))), None, None)
    try:
        raw = json.loads(path.read_text(encoding="utf-8-sig"))
    except (ValueError, OSError) as e:
        return (check("instances", "fail",
                      "实例登记不是合法 JSON：%s（%s）" % (path, e)),
                path, None)
    data = json_object(raw)
    if data is None:
        return (check("instances", "fail",
                      "实例登记顶层应为对象 {级别: {host, port}}：%s" % path),
                path, None)
    if level not in data:
        return (check("instances", "fail",
                      "%s 里没有 %s（现有：%s）"
                      % (rel(path, project), level,
                         ", ".join(sorted(data)) or "无")), path, None)
    endpoint = endpoint_of(level, data)
    if endpoint is None:
        return (check("instances", "fail",
                      "%s 的 %s 缺合法 host+port：%r"
                      % (rel(path, project), level, data.get(level))),
                path, None)
    host, port = endpoint
    return (check("instances", "ok",
                  "%s = %s:%d（%s）" % (level, host, port, rel(path, project))),
            path, endpoint)


def check_reachable(endpoint: tuple[str, int] | None) -> dict[str, str]:
    if endpoint is None:
        return check("reachable", "fail", "依赖实例登记（instances 项失败）")
    host, port = endpoint
    url = "http://%s:%d" % (host, port)
    ping = bin_dir() / "wn-ping" / "wn-ping.py"
    if not ping.is_file():
        return check("reachable", "fail", "找不到 %s" % ping)
    try:
        rc, out, err = run_tool(ping, ["--url", url], Path.cwd(), timeout=10.0)
    except subprocess.TimeoutExpired:
        return check("reachable", "fail", "wn-ping 10s 未返回：%s" % url)
    except OSError as e:
        return check("reachable", "fail", "启动 wn-ping 失败：%s" % e)
    raw, problem = last_json_line(out)
    data = json_object(raw)
    if data is None:
        return check("reachable", "fail",
                     "wn-ping 输出不可解析（rc=%d，%s）：%s"
                     % (rc, problem, (err.strip() or out.strip())[:200]))
    if not data.get("alive"):
        return check("reachable", "fail",
                     "%s 不可达（wn-ping 退出 %d）：%s"
                     % (url, rc, data.get("error") or "alive=false"))
    modes = "system=%s / domain=%s" % (data.get("system", "?"),
                                       data.get("domain", "?"))
    if not data.get("agent_ready"):
        return check("reachable", "fail",
                     "%s 可达但不能自动登录（%s；需允许纯密码登录）" % (url, modes))
    return check("reachable", "ok", "%s alive，agent_ready（%s）" % (url, modes))


def resolve_session_dir(instances_file: Path | None,
                        project: str | Path) -> Path:
    """$WN_SESSION_DIR > 登记文件所在目录/.at/client/session > 项目目录。"""
    env = os.environ.get("WN_SESSION_DIR")
    if env:
        return Path(env).resolve()
    if instances_file is not None:
        return (Path(instances_file).parent / ".at" / "client" / "session"
                ).resolve()
    return (Path(project) / ".at" / "client" / "session").resolve()


def resolve_creds_path(project: str | Path,
                       config: dict[str, Any] | None) -> Path:
    cfg: dict[str, Any] = config or {}
    raw = cfg.get("credentials_file") or CREDS_DEFAULT
    path = Path(str(raw)).expanduser()
    if not path.is_absolute():
        path = Path(project) / path
    return path.resolve()


def creds_problem(path: Path) -> str:
    """凭据可用返回 ""，否则返回一句原因（不泄露口令）。"""
    if not path.is_file():
        return "文件不存在"
    try:
        raw = json.loads(path.read_text(encoding="utf-8-sig"))
    except (ValueError, OSError) as e:
        return "读不了或不是合法 JSON（%s）" % e
    data = json_object(raw)
    if data is None:
        return "顶层不是对象"
    usr = data.get("usr")
    passwd = data.get("passwd")
    if not isinstance(usr, str) or not usr.strip():
        return "缺 usr"
    if not isinstance(passwd, str) or not passwd:
        return "缺 passwd"
    return ""


def check_session(project: str | Path, config: dict[str, Any] | None,
                  level: str | None,
                  instances_file: Path | None) -> dict[str, str]:
    if not level:
        return check("session", "fail", "实例级别未定，算不出会话名")
    cfg: dict[str, Any] = config or {}
    domain = cfg.get("test_domain")
    if not domain:
        return check("session", "fail",
                     "atest.json 缺 test_domain，算不出会话名（<级别>-<域>-00）")
    name = "%s-%s-00" % (level, domain)
    session_dir = resolve_session_dir(instances_file, project)
    session_file = session_dir / (name + ".toml")
    creds = resolve_creds_path(project, config)
    if session_file.is_file():
        return check("session", "ok",
                     "会话 %s 存在（%s；直接复用，不读凭据）"
                     % (name, rel(session_file, project)))
    problem = creds_problem(creds)
    if not problem:
        return check("session", "ok",
                     "无会话 %s，凭据 %s 可用（需要时自动登录）"
                     % (name, rel(creds, project)))
    return check("session", "fail",
                 "会话 %s 不存在（%s），凭据 %s 也不可用（%s）：二者之一必须有"
                 % (name, rel(session_dir, project), rel(creds, project),
                    problem))


def check_tools() -> dict[str, str]:
    base = bin_dir()
    missing = [name for name in TOOLS
               if not (base / name / (name + ".py")).is_file()]
    if missing:
        return check("tools", "fail",
                     "缺工具 %s（应在 %s/<工具>/<工具>.py）"
                     % (", ".join(missing), base))
    return check("tools", "ok",
                 "at-run / at-report / at-baseline / wn-cli 都在 %s" % base)


def check_report(project: str | Path, root: str | None) -> dict[str, str]:
    if not root:
        return check("report", "fail", "root 未定（config 项失败），定位不了报告目录")
    report = Path(project) / root / "report"
    created = ""
    if not report.is_dir():
        try:
            report.mkdir(parents=True, exist_ok=True)
            created = "（原来缺失，已创建；首次 at-run 也会建）"
        except OSError as e:
            return check("report", "fail",
                         "报告目录 %s 不存在且创建失败：%s"
                         % (rel(report, project), e))
    path: Path | None = None
    try:
        fd, name = tempfile.mkstemp(prefix=".doctor-", suffix=".tmp",
                                    dir=str(report))
        path = Path(name)
        with os.fdopen(fd, "wb") as f:
            f.write(b"atest-doctor writable probe\n")
    except OSError as e:
        return check("report", "fail",
                     "报告目录 %s 不可写：%s" % (rel(report, project), e))
    finally:
        if path is not None:
            try:
                path.unlink()
            except OSError:
                pass
    return check("report", "warn" if created else "ok",
                 "可写：%s%s" % (rel(report, project), created))


def main(argv: Sequence[str] | None = None) -> int:
    force_utf8_stdio()
    ap = _Parser(prog="at-doctor", description=DESCRIPTION, epilog=EPILOG,
                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--dir", default=".", metavar="DIR",
                    help="项目目录（默认当前目录）")
    ap.add_argument("--instance", choices=["L1", "L2"],
                    help="实例级别（缺省读 atest.json 的 instance）")
    args = ap.parse_args(argv)

    project = Path(args.dir).expanduser()
    if not project.exists() or not project.is_dir():
        emit({"ok": False, "error": "项目目录不存在或不是目录：%s" % project})
        return EXIT_ENV
    project = project.resolve()

    config, config_path, config_error = load_config(project)
    root: str | None = None
    root_error: str = ""
    if config is not None:
        root, root_error = resolve_root(config, config_path)
    cfg: dict[str, Any] = config or {}
    level: str | None = args.instance or cfg.get("instance") or None

    checks: list[dict[str, str]] = [
        check_python(),
        check_config(project, config, config_path, config_error, root,
                     root_error),
        check_cases(project, root),
    ]
    instances_check, instances_file, endpoint = check_instances(project, level)
    checks.append(instances_check)
    checks.append(check_reachable(endpoint))
    checks.append(check_session(project, config, level, instances_file))
    checks.append(check_tools())
    checks.append(check_report(project, root))

    failures = sum(1 for item in checks if item["status"] == "fail")
    warnings = sum(1 for item in checks if item["status"] == "warn")
    for item in checks:
        print("at-doctor: %-10s %-4s %s"
              % (item["id"], item["status"], item["detail"]), file=sys.stderr)
    print("at-doctor: %d 项：%d ok / %d warn / %d fail —— %s（exit %d）"
          % (len(checks), len(checks) - failures - warnings, warnings,
             failures, "可以跑 at-run" if failures == 0 else "先修 fail 项",
             EXIT_OK if failures == 0 else EXIT_USAGE), file=sys.stderr)

    emit({"ok": failures == 0, "checks": checks, "failures": failures})
    return EXIT_OK if failures == 0 else EXIT_USAGE


if __name__ == "__main__":
    sys.exit(main())
