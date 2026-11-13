#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""CASE-101：在 ~/atest-lab 下建目录，再按路径读回。

对应用例：atest/cases/CASE-101.md。
脚本只"执行 + 取证"，断言由 runner 按用例的 `## 期望` 做。
"""

from atlib import fixture, wn, snap, emit, note


def main():
    note("铺夹具 atest/atest.init（~/atest-lab、docs、hello.txt）")
    fixture()

    note("建目录 ~/atest-lab/case101-dir（-race DIR -upsert，可重复执行）")
    wn("o @create -p ~/atest-lab -race DIR -upsert '{\"nm\":\"case101-dir\"}'")

    obj = snap("~/atest-lab/case101-dir")
    emit({"nm": obj["nm"], "race": obj["race"], "pid": obj["pid"]})


if __name__ == "__main__":
    main()
