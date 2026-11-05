#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
wn-ping — Walnut 探活工具

探测目标实例的 GET /a/login_mode（无需登录），输出 JSON 结论：
  - alive:        HTTP 可达且 ajax 外层 ok=true
  - agent_ready:  system 与 domain 登录模式均为 PASSWD_ONLY（智能体测试前提）

用法:
    python wn-ping.py [--url http://localhost:8080] [--timeout 3]

输出(stdout，单行 JSON):
    {"ok":true,"alive":true,"url":"...","system":"PASSWD_ONLY","domain":"PASSWD_ONLY","agent_ready":true}
    探活失败时: {"ok":true,"alive":false,"url":"...","error":"..."}

退出码:
    0  实例活着
    2  探活失败（连接拒绝/超时/响应不符合契约）
    1  参数错误

文档: doc/reference/102.auto-test/30-at-tools.md
"""

import argparse
import json
import sys
import urllib.request

EXIT_OK = 0
EXIT_USAGE = 1
EXIT_NO_TARGET = 2
EXIT_BAD_RESPONSE = 4


def ping(url, timeout):
    """返回 (alive, info dict)"""
    api = url.rstrip("/") + "/a/login_mode"
    try:
        with urllib.request.urlopen(api, timeout=timeout) as resp:
            text = resp.read().decode("utf-8", "replace")
    except Exception as e:
        return False, {"error": "%s: %s" % (type(e).__name__, e)}
    try:
        j = json.loads(text)
    except ValueError:
        return False, {"error": "响应不是 JSON: %s" % text[:200]}
    if not j.get("ok"):
        return False, {"error": "ok 不为 true: %s" % text[:200]}
    data = j.get("data") or {}
    system = data.get("system", "")
    domain = data.get("domain", "")
    return True, {
        "system": system,
        "domain": domain,
        "agent_ready": system == "PASSWD_ONLY" and domain == "PASSWD_ONLY",
    }


def main():
    ap = argparse.ArgumentParser(description="Walnut 探活")
    ap.add_argument("--url", default="http://localhost:8080", help="目标实例地址")
    ap.add_argument("--timeout", type=float, default=3, help="探测超时(秒)")
    args = ap.parse_args()

    alive, info = ping(args.url, args.timeout)
    out = {"ok": True, "alive": alive, "url": args.url}
    out.update(info)
    print(json.dumps(out, ensure_ascii=False))
    if alive:
        return EXIT_OK
    if "error" in info and "JSON" not in info["error"] and "ok 不为" not in info["error"]:
        return EXIT_NO_TARGET
    return EXIT_BAD_RESPONSE


if __name__ == "__main__":
    sys.exit(main())
