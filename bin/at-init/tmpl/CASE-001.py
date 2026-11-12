#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""CASE-001 脚本骨架（`bin/at-init/tmpl/` 模板；复制成
`test/scripts/CASE-001.py` 后改写）

脚本契约**尚未定稿**（ATESET-03/04 负责）：本文件只固定骨架形状，不承诺
参数与输出格式。定稿前的默认行为是**退出 2 并说明"未实现"**——绝不假装通过。

约定草案（预计由 at-run 注入，定稿以任务 03 的 README 为准）：

    AT_INSTANCE   L1 | L2
    AT_DOMAIN     测试域
    AT_SESSION    会话名
    AT_OUT        证据输出目录

脚本只做"执行 + 取证"，断言交给 at-run（结果对象比较）。
stdout 最后一行是结果 JSON：{"case": "<CASE-ID>", "ok": bool, "evidence": {...}}
退出码：0 通过 / 1 用例失败 / 2 环境不可用（含"脚本未实现"）。
"""

import json
import os
import sys

CASE_ID = "CASE-001"


def main():
    missing = [k for k in ("AT_INSTANCE", "AT_DOMAIN") if not os.environ.get(k)]
    if missing:
        print(json.dumps({"case": CASE_ID, "ok": False,
                          "error": "缺少环境变量: %s" % ", ".join(missing)},
                         ensure_ascii=False))
        return 2

    # TODO 1: 用 wn-cli 执行被测契约（cmd / HTTP / jsbin），原始响应存到 AT_OUT
    # TODO 2: 把结果写进域（对象树），供 at-run 断言
    print(json.dumps({"case": CASE_ID, "ok": False,
                      "error": "脚本未实现：请按用例步骤填充 TODO 1/2"},
                     ensure_ascii=False))
    return 2


if __name__ == "__main__":
    sys.exit(main())
