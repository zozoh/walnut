#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
wn-down — 温柔停止一个 Walnut 实例

停止顺序（doc/reference/102.auto-test/20-at-setup.md）:
    1. 读运行锁 run/pid-<port>.json，核对归属（agent/work_path 必须是自己）
       - 运行锁缺失/归属不符 → 拒绝（退出 2），宁可不停
    2. 连 127.0.0.1:admin_port 发送 "stop"（温柔，触发 WnSetup destroy）
    3. 等待进程退出（--timeout 秒）
    4. 兜底：先温和 taskkill，再 taskkill /F

用法:
    python wn-down.py --dist <dist/wn4j 路径> [--port N] \
        [--pid-dir <run 目录>] [--timeout 15] [--who 代号]

退出码: 0 停止成功；1 用法错误；2 拒绝停止（归属不符/锁缺失）
输出: {"action":"stop","gentle":true|false,"pid":...}

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


def find_whoami(cwd, who):
    if who:
        return who
    f = Path(cwd) / ".at" / "whoami.json"
    if f.exists():
        try:
            return json.loads(f.read_text(encoding="utf-8")).get("name")
        except (ValueError, OSError):
            pass
    return None


def pid_alive(pid):
    if os.name == "nt":
        r = subprocess.run(["tasklist", "/FI", "PID eq %d" % pid],
                           capture_output=True, text=True)
        return str(pid) in r.stdout
    try:
        os.kill(pid, 0)
    except OSError:
        return False
    return True


def send_stop(admin_port, timeout=5):
    try:
        s = socket.create_connection(("127.0.0.1", admin_port), timeout=timeout)
        s.sendall(b"stop\n")
        s.close()
        return True
    except OSError:
        return False


def kill_pid(pid):
    if os.name == "nt":
        subprocess.run(["taskkill", "/PID", str(pid)],
                       capture_output=True)
        time.sleep(2)
        if pid_alive(pid):
            subprocess.run(["taskkill", "/F", "/PID", str(pid)],
                           capture_output=True)
    else:
        import signal
        try:
            os.kill(pid, signal.SIGTERM)
        except OSError:
            return
        time.sleep(2)
        if pid_alive(pid):
            try:
                os.kill(pid, signal.SIGKILL)
            except OSError:
                pass


def main():
    ap = argparse.ArgumentParser(description="停止 Walnut 实例")
    ap.add_argument("--dist", required=True, help="dist/wn4j 目录")
    ap.add_argument("--port", type=int, help="实例端口（缺省取 run/ 里唯一的锁文件）")
    ap.add_argument("--pid-dir", help="运行锁目录（缺省 <dist>/run）")
    ap.add_argument("--timeout", type=int, default=15, help="温柔停止等待秒数")
    ap.add_argument("--who", help="智能体代号（缺省读 .at/whoami.json）")
    args = ap.parse_args()

    dist = Path(args.dist).resolve()
    pid_dir = Path(args.pid_dir).resolve() if args.pid_dir else dist / "run"

    if args.port:
        pid_file = pid_dir / ("pid-%d.json" % args.port)
    else:
        files = list(pid_dir.glob("pid-*.json"))
        if len(files) != 1:
            print(json.dumps({"ok": False, "error":
                              "未指定 --port，且 %s 里运行锁不唯一" % pid_dir},
                             ensure_ascii=False))
            return EXIT_USAGE
        pid_file = files[0]

    if not pid_file.exists():
        print(json.dumps({"ok": False, "error": "运行锁不存在: %s" % pid_file},
                         ensure_ascii=False))
        return EXIT_NO_TARGET

    try:
        info = json.loads(pid_file.read_text(encoding="utf-8-sig"))
    except (ValueError, OSError) as e:
        print(json.dumps({"ok": False, "error": "运行锁无效: %s" % e},
                         ensure_ascii=False))
        return EXIT_NO_TARGET

    pid = int(info.get("pid") or 0)
    agent = info.get("agent") or ""
    work_path = info.get("work_path") or ""
    admin_port = int(info.get("admin_port") or (int(info.get("app_port") or 0) + 1))
    if not pid or not admin_port:
        print(json.dumps({"ok": False, "error": "运行锁缺少 pid/admin_port"},
                         ensure_ascii=False))
        return EXIT_NO_TARGET

    # 归属核对
    cwd = os.getcwd()
    who = find_whoami(cwd, args.who)
    if agent:
        if not who or agent != who or (work_path and work_path != cwd):
            print(json.dumps({"ok": False, "action": "refuse",
                              "error": "实例属于 %s (%s)，不是你；请示主人后再停止"
                                       % (agent, work_path)}, ensure_ascii=False))
            return EXIT_NO_TARGET
    # agent 为空 = 人类手动启动；who 为空的人类也允许停

    if not pid_alive(pid):
        pid_file.unlink(missing_ok=True)
        print(json.dumps({"ok": True, "action": "stop", "gentle": True,
                          "pid": pid, "note": "进程本已退出"}, ensure_ascii=False))
        return EXIT_OK

    gentle = True
    if send_stop(admin_port):
        deadline = time.time() + args.timeout
        while time.time() < deadline:
            if not pid_alive(pid):
                break
            time.sleep(1)
        else:
            pass
        if pid_alive(pid):
            gentle = False
            kill_pid(pid)
    else:
        # admin 端口不可达，直接兜底
        gentle = False
        kill_pid(pid)

    pid_file.unlink(missing_ok=True)
    print(json.dumps({"ok": True, "action": "stop", "gentle": gentle, "pid": pid},
                     ensure_ascii=False))
    return EXIT_OK


if __name__ == "__main__":
    sys.exit(main())
