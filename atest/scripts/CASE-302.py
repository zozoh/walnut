#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""CASE-302：参数变化改变落库对象，同一条连跑两次仍幂等。

对应用例：atest/cases/CASE-302.md。
两次同样的 POST 都走同一条 HTTP → jsbin → 命令链；`-upsert` 让第二次是覆盖，
再用 `o @count` 数同名对象，确认父目录里只有一个。
"""

import json

from atlib import fixture, http, snap, emit, note, wn

PATH = "/api/demo/order/create?amount=5"
TARGET = "~/atest-lab/orders/order-5.json"
COUNT_CMD = "o ~/atest-lab/orders @count '{nm:\"order-5.json\"}'"


def main():
    note("铺夹具 atest/atest.init（与 CASE-301 共用 jsbin 脚本与接口对象）")
    fixture()

    note("第一次 POST %s" % PATH)
    first_status, _, first_body = http(PATH, method="POST")
    first = (json.loads(first_body).get("data") or {})

    note("第二次 POST %s（同一条，验幂等）" % PATH)
    second_status, _, second_body = http(PATH, method="POST")
    second = (json.loads(second_body).get("data") or {})
    note("两次状态 %r / %r，两次 data %r / %r"
         % (first_status, second_status, first, second))

    obj = snap(TARGET)
    count = int(wn(COUNT_CMD).strip())
    note("副作用对象 nm=%r，amount=%r，同名对象数=%d"
         % (obj.get("nm"), obj.get("amount"), count))

    emit({
        "first_status": first_status,
        "second_status": second_status,
        "first_nm": first.get("orderNm"),
        "second_nm": second.get("orderNm"),
        "first_amount": first.get("amount"),
        "second_amount": second.get("amount"),
        "obj_nm": obj.get("nm"),
        "obj_amount": obj.get("amount"),
        "same_nm_count": count,
    })


if __name__ == "__main__":
    main()
