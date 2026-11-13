#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""CASE-102：写入固定文本，再用 cat 读回（内容完全相等）。

对应用例：atest/cases/CASE-102.md。
cat 的输出可能带行尾换行，所以取回后 strip()；期望按字符串完全相等比较。
"""

from atlib import fixture, wn, snap, emit, note

CONTENT = "hello from atest case102"
TARGET = "~/atest-lab/docs/case102-note.txt"


def main():
    note("铺夹具 atest/atest.init（~/atest-lab/docs 已就绪）")
    fixture()

    note("写入固定文本到 %s（重定向覆盖，可重复执行）" % TARGET)
    wn("echo -n '%s' > %s" % (CONTENT, TARGET))

    text = wn("cat %s" % TARGET).strip()
    obj = snap(TARGET)
    note("cat 读回 %d 个字符（已去掉首尾空白）；对象 len=%s"
         % (len(text), obj.get("len")))
    emit({"content": text})


if __name__ == "__main__":
    main()
