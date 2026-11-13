#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""CASE-103：按元数据条件查询，并按名字排序取回命中集合。

对应用例：atest/cases/CASE-103.md。
排序固定 `-sort '{nm:1}'`，不依赖后端默认顺序；`-l` 保证结果始终是数组。
"""

import json

from atlib import fixture, wn, snap, emit, note

DOCS = "~/atest-lab/docs"
TAG = "q103"
HITS = ("c103-a", "c103-b", "c103-c")


def main():
    note("铺夹具 atest/atest.init（~/atest-lab/docs 已就绪）")
    fixture()

    for nm in HITS:
        note("建命中对象 %s（tag=%s，-upsert）" % (nm, TAG))
        wn("o @create -p %s -upsert '{\"nm\":\"%s\",\"tag\":\"%s\"}'"
           % (DOCS, nm, TAG))

    note("建干扰对象 c103-x（tag=other，不该出现在结果里）")
    wn("o @create -p %s -upsert '{\"nm\":\"c103-x\",\"tag\":\"other\"}'" % DOCS)

    note("按 tag=%s 查询并 -sort {nm:1}" % TAG)
    out = wn("o %s @query '{\"tag\":\"%s\"}' -sort '{nm:1}' -l @json -cqn"
             % (DOCS, TAG))
    rows = json.loads(out.strip().splitlines()[-1])
    snap("%s/%s" % (DOCS, "c103-b"))
    note("查询命中 %d 个对象：%s" % (len(rows),
                                 ", ".join(row["nm"] for row in rows)))
    emit({"nm": [row["nm"] for row in rows]})


if __name__ == "__main__":
    main()
