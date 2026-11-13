#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""CASE-001 脚本模板（`bin/at-init/tmpl/`；复制成 `atest/scripts/CASE-001.py` 后改写）

与 `CASE-001.md` 配套：在域 home 下建一个目录对象，再把取到的结果交给 runner 断言。
**本文件可直接跑**（`at-run --only CASE-001`）；新用例复制后只改 TODO 1~3。

脚本契约（ATESET-03 T1 定稿）：runner 注入 AT_LEVEL / AT_URL / AT_SESSION_DIR /
AT_DOMAIN / AT_WNCLI / AT_CASE_ID / AT_OUT，并把 bin/at-run 放进子进程 PYTHONPATH
（所以能 `from atlib import ...`；手工跑脚本时自己设 PYTHONPATH）。
stdout 最后一行必须是 {"ok": true, "actual": {...}} 或 {"ok": false, "error": "..."}；
断言不在脚本里做——runner 按用例 md 的 `## 期望` 统一比较（防"实现即预期"）。
"""

from atlib import wn, snap, emit, note


def main():
    # TODO 1: 换成用例要测的契约。wn() 自动带级别/会话/端点，别手写 --level
    note("创建 ~/atest-dir（-upsert，可重复执行）")
    wn("o ~ @create atest-dir -race DIR -upsert")

    # TODO 2: 取结果对象的快照（落 evidence/objects/，同时返回 dict）
    obj = snap("~/atest-dir")

    # TODO 3: 只挑用例 `## 期望` 里声明要比的字段；断言由 runner 做，脚本不比
    emit({"nm": obj["nm"], "race": obj["race"]})


if __name__ == "__main__":
    main()
