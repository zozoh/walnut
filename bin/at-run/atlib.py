#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""atlib — ATEST 用例脚本薄库（ATESET-04）

脚本只"执行 + 取证"，断言交给 at-run（按用例 md 的 `## 期望` 统一比较）。
本库把每个脚本都会重写的样板收成四个函数：

    wn(cmd)       经 wn-cli 执行一条 Walnut 命令（自动带级别 / 会话 / 端点）
    snap(path)    读对象 JSON，落 <AT_OUT>/objects/，返回 dict
    emit(actual)  输出契约要求的最后一行 JSON，并退出 0
    note(text)    往 <AT_OUT>/summary.txt 追加一行（runner 重写 summary 时保留）

环境变量由 at-run 注入（契约见 bin/at-run/README.md）：
    AT_LEVEL / AT_URL / AT_SESSION_DIR / AT_DOMAIN / AT_WNCLI / AT_CASE_ID / AT_OUT

约定：

- 只依赖标准库与上述环境变量；`from atlib import ...` 能成立是因为 runner 把
  bin/at-run 放进了子进程的 PYTHONPATH（手工跑脚本时自己设）；
- 任何一步环境不可用（wn-cli 失败、snap 拿不到对象、缺变量）→ `SystemExit(2)`，
  原因同时写进 summary.txt：runner 记 **blocked**，绝不伪装成红/绿；
- `emit()` 打印后直接退出 0——保证 stdout 最后一行就是结果 JSON，后面别再加输出。

示例（`bin/at-init/tmpl/CASE-001.py` 就是可直接跑的版本）：

    from atlib import wn, snap, emit, note

    note("创建 ~/atest-dir（-upsert，可重复执行）")
    wn("o ~ @create atest-dir -race DIR -upsert")
    obj = snap("~/atest-dir")
    emit({"nm": obj["nm"], "race": obj["race"]})
"""

import json
import os
import subprocess
import sys
from pathlib import Path

REQUIRED_ENV = ("AT_LEVEL", "AT_URL", "AT_SESSION_DIR", "AT_DOMAIN",
                "AT_WNCLI", "AT_OUT")

SUMMARY_NAME = "summary.txt"
OBJECTS_DIR = "objects"


def note(text):
    """往 summary.txt 追加一行（[script] 前缀）；同时回显到 stderr 留痕。"""
    line = "[script] %s" % str(text).replace("\r", " ").replace("\n", " ")
    print("atlib: " + line, file=sys.stderr)
    out = os.environ.get("AT_OUT")
    if not out:
        return
    try:
        path = Path(out) / SUMMARY_NAME
        path.parent.mkdir(parents=True, exist_ok=True)
        with open(str(path), "a", encoding="utf-8", newline="\n") as f:
            f.write(line + "\n")
    except OSError:
        # 证据写不进去不该让用例本身崩掉；stderr 里已经留了痕
        pass


def _die(message):
    note(message)
    raise SystemExit(2)


def _env(name):
    value = os.environ.get(name)
    if not value:
        _die("缺少环境变量 %s：本脚本只能由 at-run 执行（见 bin/at-run/README.md）"
             % name)
    return value


def _session_name():
    return "%s-%s-00" % (_env("AT_LEVEL"), _env("AT_DOMAIN"))


def wn(command, timeout=90):
    """经 wn-cli 跑一条命令；成功返回 stdout，失败 SystemExit(2)。

    argv 传参、不经 shell；级别 / 会话 / 端点全部来自 runner 注入的环境变量，
    脚本里**不要**手写 --level 或地址（防 L1/L2 混跑）。
    """
    argv = [sys.executable, _env("AT_WNCLI"),
            "--level", _env("AT_LEVEL"),
            "--se", _session_name(),
            "--session-dir", _env("AT_SESSION_DIR"),
            "--url", _env("AT_URL"),
            "cmd", str(command)]
    print("atlib: wn %s" % command, file=sys.stderr)
    try:
        proc = subprocess.run(argv, capture_output=True, timeout=timeout)
    except subprocess.TimeoutExpired:
        _die("wn-cli 超时（%ss）：%s" % (timeout, command))
        return ""
    except OSError as e:
        _die("wn-cli 启动失败：%s（命令：%s）" % (e, command))
        return ""
    out = proc.stdout.decode("utf-8", "replace")
    err = proc.stderr.decode("utf-8", "replace")
    if proc.returncode != 0:
        detail = (err.strip() or out.strip() or "（无输出）")
        detail = detail.replace("\r", " ").replace("\n", " ")[:500]
        _die("wn-cli 退出码 %d：%s（命令：%s）"
             % (proc.returncode, detail, command))
    return out


def _object_name(path, name):
    """证据文件名：路径里的 / 换成 __（T1 约定的命名）。"""
    if name:
        return str(name).strip().replace("\\", "/").replace("/", "__")
    raw = str(path).strip().replace("\\", "/")
    raw = raw[2:] if raw.startswith("./") else raw
    return raw.replace("/", "__") or "root"


def snap(path, name=None):
    """读一个对象的 JSON 快照，落 <AT_OUT>/objects/<路径>.json，返回 dict。

    取不到对象 = 环境/数据问题：抛 SystemExit(2)（runner 记 blocked），不吞。
    """
    text = wn("o %s @json -cqn" % path).strip()
    line = text.splitlines()[-1].strip() if text else ""
    try:
        obj = json.loads(line)
    except ValueError:
        _die("snap(%s) 返回的不是 JSON：%r" % (path, line[:200]))
        return {}
    target = Path(_env("AT_OUT")) / OBJECTS_DIR / (
        _object_name(path, name) + ".json")
    target.parent.mkdir(parents=True, exist_ok=True)
    with open(str(target), "w", encoding="utf-8", newline="\n") as f:
        f.write(json.dumps(obj, ensure_ascii=False, indent=2) + "\n")
    return obj


def emit(actual):
    """打印契约要求的结果 JSON 并退出 0（stdout 最后一行，之后不再输出）。"""
    try:
        line = json.dumps({"ok": True, "actual": actual}, ensure_ascii=False)
    except (TypeError, ValueError) as e:
        _die("emit 的 actual 不能序列化成 JSON：%s" % e)
        return
    print(line)
    sys.stdout.flush()
    raise SystemExit(0)
