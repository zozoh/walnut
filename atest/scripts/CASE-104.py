#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""CASE-104：改夹具对象 hello.txt 的元数据，再按路径读回新值。

对应用例：atest/cases/CASE-104.md。
`lm` 每次执行都会变：脚本如实取回，期望用 {"$range": ["$t0", "$t1"]} 判定。
"""

from atlib import fixture, wn, snap, emit, note


def main():
    note("铺夹具 atest/atest.init（hello.txt 已存在则跳过内容写入）")
    fixture()

    note("改元数据 owner/seq（重复执行结果一致）")
    wn("o ~/atest-lab/hello.txt @update '{\"owner\":\"walnut\",\"seq\":7}'")

    obj = snap("~/atest-lab/hello.txt")
    note("读回 lm=%s（期望用 $range，不写死时间）" % obj.get("lm"))
    emit({"nm": obj["nm"], "owner": obj["owner"], "seq": obj["seq"],
          "lm": obj["lm"]})


if __name__ == "__main__":
    main()
