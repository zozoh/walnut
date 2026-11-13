#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""CASE-201：真 HTTP 调普通 JSON 接口，取回状态码 / Content-Type / body。

对应用例：atest/cases/CASE-201.md。
接口对象由夹具的 `@API .regapi/api/ping 'cross/json'` 声明，脚本不造接口；
调用走 atlib.http()（标准库 urllib），不经命令通道。
"""

import json

from atlib import fixture, http, emit, note

PATH = "/api/demo/ping"


def main():
    note("铺夹具 atest/atest.init（@API 声明 .regapi/api/ping）")
    fixture()

    note("GET %s（真 HTTP，不走命令通道）" % PATH)
    status, headers, body = http(PATH)
    ctype = headers.get("Content-Type", "")

    payload = json.loads(body)
    note("状态 %d，Content-Type=%s，body.ok=%r，body.msg=%r"
         % (status, ctype, payload.get("ok"), payload.get("msg")))

    emit({"status": status, "ctype": ctype,
          "ok": payload.get("ok"), "msg": payload.get("msg")})


if __name__ == "__main__":
    main()
