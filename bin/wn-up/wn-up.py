#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
wn-up — 启动（或复用）一个 Walnut 实例

决策流程（doc/reference/102.auto-test/20-at-setup.md）:
    1. wn-ping 探活
       - 活着 → 输出 {"action":"reuse"}，退出（绝不停止现有实例）
       - 端口被非 Walnut 程序占用 → 报错退出 2
    2. 调 dist/sbin/start.cmd 拉起（级别 3 传 --conf，--pid-dir 原样透传）。
       若产物带 start_with_redis.cmd（构建时 make.json 的 REDIS_SERVER
       非空才会生成），则改调它 —— 先按生效 conf 的 redis-port 拉起
       私有 redis 进程，再原样委托 start.cmd（2/3 级完全数据隔离，
       机制见 doc/reference/102.auto-test/20-at-setup.md）
       - 启动前把身份（.at/whoami.json 的代号，或 --who）经
         WN_AGENT_NAME / WN_WORK_PATH 环境变量注入，写进运行锁
    3. 循环探活直到就绪（--wait 秒），超时退出 4

用法:
    python wn-up.py --dist <dist/wn4j 路径> \
        [--url http://localhost:8080] [--conf <conf 目录>] [--port N] \
        [--wait 90] [--who 代号] [--pid-dir <run 目录>] [--creds <凭据 JSON>]

退出码: 0 成功/复用；1 用法错误；2 端口被占或探活失败；4 启动失败或超时

文档: doc/reference/102.auto-test/30-at-tools.md
"""

import argparse
import json
import os
import socket
import subprocess
import sys
import time
from pathlib import Path

EXIT_OK = 0
EXIT_USAGE = 1
EXIT_NO_TARGET = 2
EXIT_BAD_RESPONSE = 4

HERE = Path(__file__).resolve().parent
PING_PY = HERE.parent / "wn-ping" / "wn-ping.py"


def read_prop(path, key):
    """从 properties 文件读单个键（忽略注释与空行）"""
    try:
        for line in Path(path).read_text(encoding="utf-8", errors="replace").splitlines():
            line = line.strip()
            if line.startswith("#") or "=" not in line:
                continue
            k, _, v = line.partition("=")
            if k.strip() == key:
                return v.strip()
    except OSError:
        pass
    return None


def find_whoami(cwd, who):
    """确定身份代号: --who 优先，其次 <cwd>/.at/whoami.json"""
    if who:
        return who
    f = Path(cwd) / ".at" / "whoami.json"
    if f.exists():
        try:
            return json.loads(f.read_text(encoding="utf-8")).get("name")
        except (ValueError, OSError):
            pass
    return None


def do_ping(url, timeout=3):
    r = subprocess.run(
        [sys.executable, str(PING_PY), "--url", url, "--timeout", str(timeout)],
        capture_output=True, text=True, timeout=timeout + 5,
        encoding="utf-8", errors="replace")
    try:
        return json.loads(r.stdout.strip().splitlines()[-1])
    except (ValueError, IndexError):
        return {"alive": False, "error": r.stderr.strip()[:200]}


def port_open(port, host="127.0.0.1"):
    s = socket.socket()
    s.settimeout(1.5)
    try:
        return s.connect_ex((host, port)) == 0
    finally:
        s.close()


def resolve_port(dist, conf_dir, port):
    if port:
        return port
    cands = []
    if conf_dir:
        cands.append(Path(conf_dir) / "web_local.properties")
    cands.append(Path(dist) / "conf" / "web_local.properties")
    cands.append(Path(dist) / "core" / "conf" / "web.properties")
    for c in cands:
        v = read_prop(c, "app-port")
        if v and v.isdigit():
            return int(v)
    return 8080


def newest_pid_file(pid_dir):
    files = list(Path(pid_dir).glob("pid-*.json"))
    if not files:
        return None
    return max(files, key=lambda p: p.stat().st_mtime)


def node_init(dist, url, creds_path):
    """空白数据库节点的初始化固化（wn-cli check-node / init-node）。

    识别依据 run/node-init.json 标记；标记缺失则用 root 看 /app 权限
    （空白库启动时是 0750）判断，必要时跑 init-node 并写标记。
    返回 (status, detail): already / done / no-creds / fail
    """
    marker = dist / "run" / "node-init.json"
    if marker.exists():
        return "already", {}
    if not creds_path:
        return "no-creds", {}
    try:
        creds = json.loads(Path(creds_path).read_text(encoding="utf-8"))
    except (ValueError, OSError) as e:
        return "no-creds", {"error": "凭据文件不可读: %s" % e}
    root_usr = creds.get("root_usr") or (creds.get("root") or {}).get("usr") or "root"
    root_passwd = (creds.get("root_passwd")
                   or (creds.get("root") or {}).get("passwd") or "")
    if not root_passwd:
        return "no-creds", {}
    cli = HERE.parent / "wn-cli" / "wn-cli.py"
    import tempfile
    se_dir = Path(tempfile.mkdtemp(prefix="wn_node_"))

    def cli_run(*a):
        return subprocess.run([sys.executable, str(cli), "--url", url,
                               "--se", "wn-up", "--session-dir", str(se_dir)]
                              + list(a),
                              capture_output=True, text=True, timeout=180,
                              encoding="utf-8", errors="replace")

    r = cli_run("login", "--usr", root_usr, "--passwd", root_passwd)
    if r.returncode != 0:
        return "fail", {"step": "login",
                        "err": ((r.stderr or "") + (r.stdout or ""))[:300]}
    initialized = False
    r = cli_run("check-node")
    try:
        initialized = json.loads(r.stdout.strip().splitlines()[-1]).get("initialized")
    except (ValueError, IndexError):
        pass
    detail = {}
    if not initialized:
        r = cli_run("init-node")
        detail["init-node"] = (r.stdout or "")[:600]
        if r.returncode != 0:
            return "fail", {"step": "init-node",
                            "err": ((r.stderr or "") + (r.stdout or ""))[:300]}
    marker.write_text(json.dumps({
        "initialized_at": time.strftime("%Y-%m-%dT%H:%M:%S"),
        "by": "wn-up", "url": url}, ensure_ascii=False, indent=2),
        encoding="utf-8")
    return "done", detail


def main():
    ap = argparse.ArgumentParser(description="启动或复用 Walnut 实例")
    ap.add_argument("--dist", required=True, help="dist/wn4j 目录")
    ap.add_argument("--url", help="目标地址（缺省由端口推导）")
    ap.add_argument("--conf", help="级别 3：私有 conf 目录")
    ap.add_argument("--port", type=int, help="期望端口（缺省从配置解析）")
    ap.add_argument("--wait", type=int, default=90, help="就绪等待秒数")
    ap.add_argument("--who", help="智能体代号（缺省读 .at/whoami.json）")
    ap.add_argument("--pid-dir", help="运行锁目录（透传启动脚本）")
    ap.add_argument("--creds", help="凭据 JSON（含 root_usr/root_passwd），"
                                   "提供后启动就绪即自动做空白节点初始化")
    args = ap.parse_args()

    dist = Path(args.dist).resolve()
    start_cmd = dist / "sbin" / "start.cmd"
    if not start_cmd.exists():
        print(json.dumps({"ok": False, "error": "找不到启动脚本: %s" % start_cmd},
                         ensure_ascii=False))
        return EXIT_NO_TARGET

    # 产物带 start_with_redis.cmd（构建时 make.json 的 REDIS_SERVER 非空
    # 才会生成）就用它 —— 先按生效 conf 的 redis-port 拉起私有 redis 进程，
    # 再原样委托 start.cmd（2/3 级完全数据隔离）
    swr = dist / "sbin" / "start_with_redis.cmd"
    launcher = swr if swr.exists() else start_cmd

    cwd = os.getcwd()
    port = resolve_port(dist, args.conf, args.port)
    url = args.url or ("http://localhost:%d" % port)

    # 1) 探活
    first = do_ping(url)
    if first.get("alive"):
        out = {"ok": True, "action": "reuse", "url": url,
               "agent_ready": first.get("agent_ready", False)}
        print(json.dumps(out, ensure_ascii=False))
        print("已存在的实例，复用之（不要停止它）", file=sys.stderr)
        return EXIT_OK
    if port_open(port):
        print(json.dumps({"ok": False, "action": "refuse",
                          "error": "端口 %d 被非 Walnut 程序占用" % port,
                          "ping": first.get("error")}, ensure_ascii=False))
        return EXIT_NO_TARGET

    # 2) 身份与启动
    who = find_whoami(cwd, args.who)
    if not who:
        print(json.dumps({"ok": False, "error":
                          "缺少身份：请先用 walnut-at-init --name <代号> 建立 .at/whoami.json，"
                          "或传 --who"}, ensure_ascii=False))
        return EXIT_USAGE

    env = dict(os.environ, WN_AGENT_NAME=who, WN_WORK_PATH=cwd)
    cmd = ['"%s"' % launcher]
    if args.conf:
        cmd.append('--conf "%s"' % args.conf)
    if args.pid_dir:
        cmd.append('--pid-dir "%s"' % args.pid_dir)
    print("启动: %s" % " ".join(cmd), file=sys.stderr)
    # 注意: 不能 capture_output 管道捕获 start.cmd 的输出 —— JVM 经
    # Start-Process 继承了管道写句柄，子进程退出后 communicate() 等
    # EOF 会永久挂起。改重定向到临时文件，结束后读文件。
    import tempfile
    tmp = Path(tempfile.mkdtemp(prefix="wn_up_"))
    out_f = open(str(tmp / "out.log"), "w", encoding="utf-8", errors="replace")
    err_f = open(str(tmp / "err.log"), "w", encoding="utf-8", errors="replace")
    try:
        r = subprocess.run(" ".join(cmd), shell=True, env=env,
                           stdout=out_f, stderr=err_f, timeout=120)
    except subprocess.TimeoutExpired:
        r = None
        rc = -1
    finally:
        out_f.close()
        err_f.close()
    r_out = (tmp / "out.log").read_text(encoding="utf-8", errors="replace")
    r_err = (tmp / "err.log").read_text(encoding="utf-8", errors="replace")
    if r is not None:
        rc = r.returncode
    if rc != 0:
        print(json.dumps({"ok": False, "action": "start-fail",
                          "error": (r_err.strip() or r_out.strip())[:400]},
                         ensure_ascii=False))
        return EXIT_BAD_RESPONSE

    # 3) 等就绪
    pid_file = (Path(args.pid_dir) if args.pid_dir else dist / "run") / ("pid-%d.json" % port)
    if not pid_file.exists():
        found = newest_pid_file(args.pid_dir or dist / "run")
        pid_file = found if found else pid_file
    deadline = time.time() + args.wait
    last = None
    while time.time() < deadline:
        last = do_ping(url, timeout=3)
        if last.get("alive"):
            out = {"ok": True, "action": "start", "url": url,
                   "agent_ready": last.get("agent_ready", False),
                   "pid_file": str(pid_file)}
            # 4) 空白节点初始化（有凭据才自动做，结果写进输出与标记）
            status, detail = node_init(dist, url, args.creds)
            out["node_init"] = status
            if detail:
                out["node_init_detail"] = detail
            try:
                info = json.loads(pid_file.read_text(encoding="utf-8-sig"))
                out.update({k: info.get(k) for k in
                            ("pid", "app_port", "admin_port") if info.get(k)})
            except (ValueError, OSError):
                pass
            print(json.dumps(out, ensure_ascii=False))
            return EXIT_OK
        time.sleep(2)

    print(json.dumps({"ok": False, "action": "timeout",
                      "error": "%d 秒内未就绪" % args.wait,
                      "log": "见 %s" % ((args.pid_dir or str(dist / "run")) + "/walnut-%d.log" % port)},
                     ensure_ascii=False))
    return EXIT_BAD_RESPONSE


if __name__ == "__main__":
    sys.exit(main())
