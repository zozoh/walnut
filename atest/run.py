#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""ATEST 总入口：在项目与 walnut 工具之间做一层薄转发。

用法（在项目根执行）：

    python atest/run.py                       # 跑全部用例
    python atest/run.py --only CASE-101,CASE-102
    python atest/run.py --check               # 只校验用例格式，不执行
    python atest/run.py --instance L2 --reset

所有参数原样转给 `bin/at-run/at-run.py`（`--help` 看全集）。
工具位置查找顺序：`$WALNUT_HOME` → 本项目目录 → 各级父目录（找 `bin/at-run/at-run.py`）。
零依赖：只用 Python 标准库。
"""

import os
import subprocess
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent      # <项目>/<root>
PROJECT = HERE.parent                       # <项目>


def find_runner():
    """找 walnut 仓库里的 at-run.py。"""
    bases = []
    env = os.environ.get("WALNUT_HOME")
    if env:
        bases.append(Path(env))
    bases.append(PROJECT)
    bases.extend(PROJECT.parents)
    for base in bases:
        cand = base / "bin" / "at-run" / "at-run.py"
        if cand.is_file():
            return cand
    return None


def main():
    runner = find_runner()
    if runner is None:
        print("找不到 bin/at-run/at-run.py。\n"
              "请设置环境变量 WALNUT_HOME 指向你的 walnut 仓库，例如：\n"
              "  $env:WALNUT_HOME = 'D:/work/walnut'      # PowerShell\n"
              "  export WALNUT_HOME=/work/walnut          # bash",
              file=sys.stderr)
        return 2
    argv = [sys.executable, str(runner), "--dir", str(PROJECT)] + sys.argv[1:]
    return subprocess.call(argv)


if __name__ == "__main__":
    sys.exit(main())
