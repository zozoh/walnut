#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""CASE-203：声明 http-resp-code:302 的接口，取回 302 与 Location。

对应用例：atest/cases/CASE-203.md。
atlib.http() 不跟随重定向（跟过去就测不到跳转本身）；assertion 在用例期望里。
"""

from atlib import fixture, http, emit, note

PATH = "/api/demo/go"


def main():
    note("铺夹具 atest/atest.init（@API go，快捷元数据 http302）")
    fixture()

    note("GET %s（不跟随重定向）" % PATH)
    status, headers, body = http(PATH)
    location = headers.get("Location", "")

    note("状态 %d，Location=%r，body=%r" % (status, location, body))
    emit({"status": status, "location": location})


if __name__ == "__main__":
    main()
