#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""CASE-301：HTTP 打接口 → 接口跑 jsbin → jsbin 用命令落库。

对应用例：atest/cases/CASE-301.md。
接口对象与 jsbin 脚本都由夹具声明；脚本只发 HTTP、解析信封、读副作用对象。
信封里的 data 与对象里的 nm/amount 都 emit 出去，期望里两处都要对。
"""

import json

from atlib import fixture, http, snap, emit, note

PATH = "/api/demo/order/create?amount=3"
TARGET = "~/atest-lab/orders/order-3.json"


def main():
    note("铺夹具 atest/atest.init（@FILE jsbin/order_flow.js + @API order/create）")
    fixture()

    note("POST %s（接口内部 jsc → $wn.exec → o @create）" % PATH)
    status, headers, body = http(PATH, method="POST")
    ctype = headers.get("Content-Type", "")

    payload = json.loads(body)
    data = payload.get("data") or {}
    note("状态 %d，ctype=%s，body.ok=%r，data=%r"
         % (status, ctype, payload.get("ok"), data))

    obj = snap(TARGET)
    note("副作用对象 nm=%r，amount 元数据=%r"
         % (obj.get("nm"), obj.get("amount")))

    emit({
        "status": status,
        "ctype": ctype,
        "ok": payload.get("ok"),
        "orderNm": data.get("orderNm"),
        "amount": data.get("amount"),
        "obj_nm": obj.get("nm"),
        "obj_amount": obj.get("amount"),
    })


if __name__ == "__main__":
    main()
