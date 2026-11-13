#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""CASE-204：调写对象的接口，再从对象树读回副作用对象。

对应用例：atest/cases/CASE-204.md。
`content` 是对象**元数据**（不是文件正文）：snap() 读对象 JSON 取证。
"""

from atlib import fixture, http, snap, emit, note

PATH = "/api/demo/write"
TARGET = "~/atest-lab/api-side-effect.txt"


def main():
    note("铺夹具 atest/atest.init（@API write 会在 %s 造对象）" % TARGET)
    fixture()

    note("GET %s（接口自己写对象）" % PATH)
    status, _, body = http(PATH)
    note("接口状态 %d，body 前 120 字=%r" % (status, body[:120]))

    obj = snap(TARGET)
    note("副作用对象 nm=%r，content 元数据=%r"
         % (obj.get("nm"), obj.get("content")))

    emit({"status": status, "nm": obj.get("nm"),
          "content": obj.get("content")})


if __name__ == "__main__":
    main()
