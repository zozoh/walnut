#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""CASE-202：令牌接口——不带令牌一次、带 ?at=<令牌> 一次，取回两个状态码。

对应用例：atest/cases/CASE-202.md。
403 是预期响应不是故障：atlib.http() 4xx 不抛，脚本照常 emit；
只有连不上才是 SystemExit(2)（runner 记 blocked）。
"""

from atlib import fixture, http, emit, note

TOKEN = "s3cr3t-atest"
PATH = "/api/demo/secret"


def main():
    note("铺夹具 atest/atest.init（@API secret + 令牌文件 ~/.domain/api_access_token）")
    fixture()

    no_token, _, _ = http(PATH)
    note("不带令牌：%s -> %d" % (PATH, no_token))

    yes_token, _, body = http("%s?at=%s" % (PATH, TOKEN))
    note("带 ?at=%s：%s -> %d，body=%r" % (TOKEN, PATH, yes_token, body))

    emit({"without_token": no_token, "with_token": yes_token})


if __name__ == "__main__":
    main()
