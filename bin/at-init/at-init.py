#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
at-init — 运行时集成测试（ATEST）项目骨架脚手架

一条命令在项目里铺开 ATEST 约定好的东西：

    test/atest.init             静态布局模板（夹具，人编辑）
    test/cases/                 用例 <CASE-ID>.md
    test/scripts/               用例脚本 <CASE-ID>.<ext>
    test/baseline/              基线 <CASE-ID>.json（审核者写）
    test/report/                执行报告（写进 .gitignore，不进库）
    ATEST.md                    集成测试标记段（instance / 测试域 / 凭据位置 / init 模板）
    .wn-instances.example.json  实例登记**样例**（真身由开发者维护，本工具不写）

分工：`.at/`（身份、会话、私有配置）归 `walnut-at-init`；本工具**只管 test/**
与 ATEST.md 的标记段，检测到 `.at/` 缺失时只提示、不代建。

幂等：已存在的文件一律 `kept`，**绝不覆盖用户内容**。`--force` 只刷新"样例段"
（ATEST.md 的 `<!-- at-init:begin -->` ~ `<!-- at-init:end -->` 之间，以及
`.wn-instances.example.json`），且**保留段内用户设定的字段值**
（`instance` / `test_domain` / `credentials_file` / `sql_datasource`，以及没给
`--name` 时的 `project`）——只刷新说明文字、注释与缺失字段，不会把
`instance: L1` 静默改回样例默认的 `L2`。`cases/` / `scripts/` / `baseline/` 里的
内容和 `test/atest.init` 永远不动。

所有写入都是 UTF-8 无 BOM（新文件 LF 换行；改写已有文件时沿用其原换行——
带 BOM 会让 TOML/JSON 解析出空值，本项目踩过）。

用法:
    python at-init.py                              # 当前目录
    python at-init.py --dir D:/work/myproj --name myproj
    python at-init.py --dry-run                    # 只报计划，不落盘
    python at-init.py --force                      # 刷新样例段

退出码: 0 成功；1 用法错误；2 环境问题（目录不存在 / 模板缺失 / 写入失败 /
ATEST.md 标记段损坏）
"""

import argparse
import json
import re
import sys
from pathlib import Path

EXIT_OK = 0
EXIT_USAGE = 1
EXIT_ENV = 2

# 模板真身就在本脚本旁边：bin/at-init/tmpl/（与代码同仓，可离线使用）
TMPL_DIR = Path(__file__).resolve().parent / "tmpl"

ATEST_MD = "ATEST.md"
INSTANCES_EXAMPLE = ".wn-instances.example.json"
INSTANCES_REAL = ".wn-instances.json"

BEGIN_PREFIX = "<!-- at-init:begin"
END_MARK = "<!-- at-init:end -->"

PROJECT_RE = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._-]{0,31}$")

# 标记段里的字段行：`- key: 值   # 注释`
FIELD_RE = re.compile(r"^-\s*([A-Za-z_][A-Za-z0-9_]*)\s*:\s*(.*)$")

# --force 刷新标记段时必须保留的**用户设定值**（不能让样例默认值把它们改回去）
KEEP_ON_FORCE = ("instance", "test_domain", "credentials_file",
                 "sql_datasource")

GITIGNORE_MARK = "# at-init: ATEST 产物（报告不进库；实例登记真身是本机配置）"
GITIGNORE_BLOCK = GITIGNORE_MARK + "\n/test/report/\n/.wn-instances.json\n"

TREE_DIRS = ("test", "test/cases", "test/scripts", "test/baseline", "test/report")
GITKEEP_FILES = ("test/cases/.gitkeep", "test/scripts/.gitkeep",
                 "test/baseline/.gitkeep")

DESCRIPTION = """\
在项目里生成 ATEST（运行时集成测试）的 test/ 骨架、ATEST.md 标记段与实例登记样例。
幂等：已存在一律 kept；--force 只刷新样例段，不动用户内容。"""

EPILOG = """\
生成物:
  test/atest.init             静态布局模板（夹具；可编辑，--force 不覆盖）
  test/cases/                 用例 <CASE-ID>.md
  test/scripts/               用例脚本 <CASE-ID>.<ext>
  test/baseline/              基线 <CASE-ID>.json
  test/report/                报告输出（追加进 .gitignore）
  ATEST.md                    集成测试标记段（instance / test_domain / ...）
  .wn-instances.example.json  实例登记样例（真身由开发者维护，本工具不写）

幂等:
  已存在一律 kept，重复执行不产生任何写入。--force 只刷新"样例段"
  （ATEST.md 的 begin~end 之间、.wn-instances.example.json），并且**保留用户
  设定的字段值**（instance / test_domain / credentials_file / sql_datasource，
  以及没给 --name 时的 project），只刷新说明文字与缺失字段。
  cases/ scripts/ baseline/ 与 test/atest.init 里的内容永不覆盖。

分工:
  .at/（身份 / 会话 / 凭据）归 walnut-at-init；本工具只管 test/ 与 ATEST.md 段。

退出码:
  0  成功（含"全部 kept"）
  1  用法错误（参数不合法、项目名不合法）
  2  环境问题（目标目录不存在、模板缺失、写入失败、ATEST.md 标记段损坏）

输出（stdout，单行 JSON）:
  {"ok":true,"dry_run":false,"dir":"...","project":"...",
   "created":[...],"appended":[...],"refreshed":[...],"kept":[...],
   "preserved":["instance",...],"warnings":[...]}

示例:
  python at-init.py --dir D:/work/myproj --name myproj
  python at-init.py --dry-run
  python at-init.py --force
"""


class _Parser(argparse.ArgumentParser):
    """argparse 默认用退出码 2 报参数错误，与本工具的「2 = 环境问题」冲突。"""

    def error(self, message):
        print(json.dumps({"ok": False,
                          "error": "%s（--help 看用法）" % message},
                         ensure_ascii=False))
        raise SystemExit(EXIT_USAGE)


class EnvError(Exception):
    pass


def read_text(path):
    """按 UTF-8 读；容忍 BOM（读出来就不带 BOM），编码不对给可读的报错。"""
    try:
        return path.read_text(encoding="utf-8-sig")
    except UnicodeDecodeError as e:
        raise EnvError("%s 不是 UTF-8 编码（%s）：请先转成 UTF-8 无 BOM"
                       % (path, e))


def write_text(path, text, newline="\n"):
    """UTF-8 无 BOM；newline 控制换行（新文件 LF，改写已有文件时沿用原换行）。"""
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(str(path), "w", encoding="utf-8", newline=newline) as f:
        f.write(text)


def ensure_file(path, content, force, dry_run):
    """返回 created / refreshed / kept；force 只在内容确有差异时才重写。"""
    if path.exists():
        if not force:
            return "kept"
        if read_text(path) == content:
            return "kept"
        if not dry_run:
            write_text(path, content)
        return "refreshed"
    if not dry_run:
        write_text(path, content)
    return "created"


def split_tmpl(text):
    """从模板里切出 (段前, 标记段, 段后)；模板坏了返回 None。"""
    lines = text.splitlines(keepends=True)
    begin = end = None
    for i, line in enumerate(lines):
        s = line.strip()
        if begin is None and s.startswith(BEGIN_PREFIX):
            begin = i
        elif begin is not None and s == END_MARK:
            end = i
            break
    if begin is None or end is None:
        return None
    return ("".join(lines[:begin]),
            "".join(lines[begin:end + 1]),
            "".join(lines[end + 1:]))


def find_markers(text):
    """在任意 markdown 里找工具维护段；返回 (begin, end) 行号或 None。"""
    lines = text.splitlines(keepends=True)
    begin = end = None
    for i, line in enumerate(lines):
        s = line.strip()
        if begin is None and s.startswith(BEGIN_PREFIX):
            begin = i
        elif begin is not None and s == END_MARK:
            end = i
            break
    if begin is None:
        return None
    return (begin, end)


def check_atest(work):
    """先校验 ATEST.md，坏档在写任何文件之前就退出（失败尽量原子）。"""
    path = work / ATEST_MD
    if not path.exists():
        return
    found = find_markers(read_text(path))
    if found is not None and found[1] is None:
        raise EnvError("ATEST.md 的 at-init 标记段不完整（有 begin 无 end），"
                       "请人工补上 %s 再跑" % END_MARK)


def _split_comment(rest):
    """把「- key: 值   # 注释」拆成 (值, 注释)；注释 = 空白 + # 到行尾。"""
    m = re.search(r"\s+#", rest)
    if m:
        return rest[:m.start()].strip(), rest[m.start():]
    return rest.strip(), ""


def parse_fields(block):
    """取出标记段里的 `- key: value  # 注释`；返回 {key: value}。"""
    fields = {}
    for line in block.splitlines():
        m = FIELD_RE.match(line.strip())
        if not m:
            continue
        value, _comment = _split_comment(m.group(2))
        fields[m.group(1)] = value
    return fields


def merge_block(old_block, new_block, keep):
    """按新模板重建标记段，但 keep 里的字段沿用旧段里的值。

    返回 (合并后的段, 被保留的字段名列表)。旧值为空时不保留（视为缺失字段，
    用模板默认值补上）。
    """
    old = parse_fields(old_block)
    preserved = []
    out = []
    for line in new_block.splitlines():
        m = FIELD_RE.match(line.strip())
        if m and m.group(1) in keep:
            key = m.group(1)
            tmpl_value, comment = _split_comment(m.group(2))
            value = old.get(key, "")
            if value:
                line = "- %s: %s%s" % (key, value, comment)
                if value != tmpl_value:
                    preserved.append(key)
        out.append(line)
    text = "\n".join(out)
    if new_block.endswith("\n"):
        text += "\n"
    return text, preserved


def ensure_atest(work, doc, block, force, dry_run, warnings, keep,
                 preserved):
    """生成或续写 ATEST.md；只动标记段，段外内容按字节保留。

    `--force` 刷新时，`keep` 里的字段**沿用文件里的既有值**（用户设定的
    instance / test_domain 等不能被样例默认值静默改回去），只刷新说明文字、
    注释与缺失字段。
    """
    path = work / ATEST_MD
    if not path.exists():
        if not dry_run:
            write_text(path, doc)
        return "created"

    raw = path.read_bytes()
    had_bom = raw.startswith(b"\xef\xbb\xbf")
    file_nl = "\r\n" if b"\r\n" in raw else "\n"
    text = read_text(path)          # 通用换行：比较一律用 LF
    found = find_markers(text)

    if found is None:
        # 没有标记段：追加，不碰已有内容
        if text == "":
            new = block
        elif text.endswith("\n"):
            new = text + "\n" + block
        else:
            new = text + "\n\n" + block
        if not dry_run:
            write_text(path, new, newline=file_nl)
        return "appended"

    begin, end = found
    if end is None:
        raise EnvError("ATEST.md 的 at-init 标记段不完整（有 begin 无 end），"
                       "请人工补上 %s 再跑" % END_MARK)
    lines = text.splitlines(keepends=True)
    old_block = "".join(lines[begin:end + 1])
    new_block, kept_fields = merge_block(old_block, block, keep)
    if new_block == old_block:
        # 用户改的值已经生效、模板说明文字没变：无事可做，也不报"模板不同"
        return "kept"
    if not force:
        warnings.append("ATEST.md 的 at-init 段与当前模板不同（含你设定的 %s 等值）："
                        "未改动它；--force 可刷新说明文字并保留这些字段值"
                        % " / ".join(sorted(keep)))
        return "kept"
    if had_bom:
        warnings.append("ATEST.md 原带 BOM：本次重写后为 UTF-8 无 BOM")
    preserved.extend(kept_fields)
    new = "".join(lines[:begin]) + new_block + "".join(lines[end + 1:])
    if not dry_run:
        write_text(path, new, newline=file_nl)
    return "refreshed"


def find_instances(work):
    """从项目目录逐级向上找 .wn-instances.json（只用于给提示）。"""
    for d in [work] + list(work.parents):
        p = d / INSTANCES_REAL
        if p.is_file():
            return p
    return None


def _force_utf8_stdio():
    """stdout/stderr 固定 UTF-8：Windows 默认按控制台代码页（cp936）编码，
    中文 JSON 到了管道里就成了另一种编码，下游按 UTF-8 解析会乱。"""
    for stream in (sys.stdout, sys.stderr):
        try:
            stream.reconfigure(encoding="utf-8")
        except (AttributeError, ValueError, OSError):
            pass


def main(argv=None):
    _force_utf8_stdio()
    ap = _Parser(prog="at-init", description=DESCRIPTION, epilog=EPILOG,
                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--dir", default=".", metavar="DIR",
                    help="目标项目目录（默认当前目录）")
    ap.add_argument("--name", metavar="NAME",
                    help="项目名，写进 ATEST.md 的 project 段（默认取目录名）")
    ap.add_argument("--force", action="store_true",
                    help="刷新样例段（ATEST.md 标记段：刷新说明文字，"
                         "保留你设定的 project/instance/test_domain/"
                         "credentials_file/sql_datasource 值；"
                         "以及 .wn-instances.example.json）；"
                         "不动 cases/ scripts/ baseline/ 与 test/atest.init")
    ap.add_argument("--dry-run", action="store_true",
                    help="只计算并输出计划，不写任何文件")
    args = ap.parse_args(argv)

    work = Path(args.dir).expanduser()
    if not work.exists():
        print(json.dumps({"ok": False, "error": "目标目录不存在: %s" % work},
                         ensure_ascii=False))
        return EXIT_ENV
    if not work.is_dir():
        print(json.dumps({"ok": False, "error": "目标不是目录: %s" % work},
                         ensure_ascii=False))
        return EXIT_ENV
    work = work.resolve()

    project = args.name if args.name else work.name
    if not PROJECT_RE.match(project or ""):
        print(json.dumps({"ok": False,
                          "error": "项目名不合法: %r（需匹配 %s；用 --name 指定）"
                                   % (project, PROJECT_RE.pattern)},
                         ensure_ascii=False))
        return EXIT_USAGE

    tmpl_atest = TMPL_DIR / "ATEST.md"
    tmpl_init = TMPL_DIR / "atest.init"
    tmpl_instances = TMPL_DIR / "wn-instances.example.json"
    missing = [str(p) for p in (tmpl_atest, tmpl_init, tmpl_instances)
               if not p.is_file()]
    if missing:
        print(json.dumps({"ok": False,
                          "error": "模板缺失，工具不完整: %s（%s）"
                                   % (", ".join(missing), TMPL_DIR)},
                         ensure_ascii=False))
        return EXIT_ENV

    try:
        parts = split_tmpl(read_text(tmpl_atest))
    except (OSError, EnvError) as e:
        print(json.dumps({"ok": False, "error": "读模板失败: %s" % e},
                         ensure_ascii=False))
        return EXIT_ENV
    if parts is None:
        print(json.dumps({"ok": False,
                          "error": "模板 %s 缺少 at-init 标记段" % tmpl_atest},
                         ensure_ascii=False))
        return EXIT_ENV
    before, block, tail = parts
    block = block.replace("{{project}}", project)
    doc = (before + block + tail).replace("{{project}}", project)

    dry = args.dry_run
    created, appended, refreshed, kept, warnings, preserved = \
        [], [], [], [], [], []
    # --force 刷新时保留的字段：四个用户设定值 +（没给 --name 时的）project
    keep = set(KEEP_ON_FORCE)
    if not args.name:
        keep.add("project")

    def bucket(action):
        return {"created": created, "appended": appended,
                "refreshed": refreshed, "kept": kept}[action]

    def ensure(rel, content, force=False):
        action = ensure_file(work / rel, content, force, dry)
        bucket(action).append(rel)
        return action

    try:
        check_atest(work)
        for rel in TREE_DIRS:
            p = work / rel
            if p.is_dir():
                kept.append(rel + "/")
                continue
            if not dry:
                p.mkdir(parents=True, exist_ok=True)
            created.append(rel + "/")

        ensure("test/atest.init", read_text(tmpl_init))
        for rel in GITKEEP_FILES:
            ensure(rel, "")

        bucket(ensure_atest(work, doc, block, args.force, dry, warnings,
                            keep, preserved)).append(ATEST_MD)

        ensure(INSTANCES_EXAMPLE, read_text(tmpl_instances), force=True)

        # .gitignore：报告与本机实例配置不进库
        gi = work / ".gitignore"
        if gi.exists():
            old = read_text(gi)
            file_nl = "\r\n" if b"\r\n" in gi.read_bytes() else "\n"
            if GITIGNORE_MARK in old:
                kept.append(".gitignore")
            else:
                sep = "" if (old == "" or old.endswith("\n")) else "\n"
                lead = "\n" if old.strip() else ""
                if not dry:
                    write_text(gi, old + sep + lead + GITIGNORE_BLOCK,
                               newline=file_nl)
                appended.append(".gitignore")
        else:
            if not dry:
                write_text(gi, GITIGNORE_BLOCK)
            created.append(".gitignore")
    except EnvError as e:
        print(json.dumps({"ok": False, "error": str(e)}, ensure_ascii=False))
        return EXIT_ENV
    except OSError as e:
        print(json.dumps({"ok": False, "error": "写入失败: %s" % e},
                         ensure_ascii=False))
        return EXIT_ENV

    if not (work / ".at").is_dir():
        warnings.append("未发现 .at/（身份 / 会话 / 凭据）：先跑 "
                        "walnut-at-init --name <代号>；at-init 不代建 .at/")
    if find_instances(work) is None:
        warnings.append("从 %s 逐级向上没找到 %s：at-run 会报错；本工具只生成样例 "
                        "%s，真身请开发者放到工作区根或用户目录"
                        % (work, INSTANCES_REAL, INSTANCES_EXAMPLE))

    print(json.dumps({"ok": True, "dry_run": dry, "dir": str(work),
                      "project": project, "created": created,
                      "appended": appended, "refreshed": refreshed,
                      "kept": kept, "preserved": sorted(preserved),
                      "warnings": warnings},
                     ensure_ascii=False))
    if dry:
        print("dry-run：以上只是计划，未写任何文件。", file=sys.stderr)
    else:
        print("下一步: 改 ATEST.md 的 instance / test_domain，写 test/atest.init，"
              "再照 bin/at-init/tmpl/ 写第一个用例。", file=sys.stderr)
    return EXIT_OK


if __name__ == "__main__":
    sys.exit(main())
