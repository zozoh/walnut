#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
at-init — 运行时集成测试（ATEST）项目骨架脚手架

一条命令在项目里铺开 ATEST 约定好的东西（`<root>` 默认 `atest`，`--root` 可改）：

    <root>/atest.init           静态布局模板（夹具，人编辑）
    <root>/cases/               用例 <CASE-ID>.md
    <root>/scripts/             用例脚本 <CASE-ID>.<ext>
    <root>/expect/              独立期望文件 <CASE-ID>.json（expected: file）
    <root>/baseline/            基线 <CASE-ID>.json（审核者写）
    <root>/report/              执行报告（写进 .gitignore，不进库）
    atest.json                  项目配置（机器可读；人也能看）
    .wn-instances.example.json  实例登记**样例**（真身由开发者维护，本工具不写）

**配置只有 `atest.json` 一份**：
`project` / `root` / `instance` / `test_domain` / `credentials_file` /
`init_template` / `sql_datasource` 都在里面，`at-run` 从同一份文件读。

根目录名只允许 `^[A-Za-z0-9._-]+$`，且不能是 `.` / `..`（非法 → 退出码 1）。
之所以不叫 `test/`：项目自己的测试框架（如 Maven 的 `testSourceDirectory`）已经
占用了这个目录，集成测试另起一个根。

分工：`.at/`（身份、会话、私有配置）归 `walnut-at-init`；本工具**只管 <root>/**
与 `atest.json`，检测到 `.at/` 缺失时只提示、不代建。

幂等：已存在的文件一律 `kept`，**绝不覆盖用户内容**。`--force` 只刷新
`.wn-instances.example.json`（内容确有差异时）；`atest.json` 只补缺失的键，
**不覆盖用户已写的值**（`--force` 也不碰）。`cases/` / `scripts/` / `expect/` /
`baseline/` 里的内容，以及 `<root>/atest.init`，永远不动。

已有 `.gitignore` 里旧的 `/test/report/` 行会被替换成 `/<root>/report/`（迁移）。

所有写入都是 UTF-8 无 BOM（新文件 LF 换行；改写已有文件时沿用其原换行——
带 BOM 会让 TOML/JSON 解析出空值，本项目踩过）。

用法:
    python at-init.py                              # 当前目录，根目录 atest
    python at-init.py --dir D:/work/myproj --name myproj
    python at-init.py --root spec                  # 集成测试根目录改名 spec
    python at-init.py --dry-run                    # 只报计划，不落盘
    python at-init.py --force                      # 刷新 .wn-instances.example.json

退出码: 0 成功；1 用法错误；2 环境问题（目录不存在 / 模板缺失 / 写入失败 /
atest.json 不是合法 JSON）
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

CONFIG_NAME = "atest.json"
INSTANCES_EXAMPLE = ".wn-instances.example.json"
INSTANCES_REAL = ".wn-instances.json"

ROOT_DEFAULT = "atest"

PROJECT_RE = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._-]{0,31}$")
# 集成测试根目录名：不允许路径分隔符，也不允许 . / ..（否则 <root>/cases 会跑出项目）
ROOT_RE = re.compile(r"^[A-Za-z0-9._-]+$")

GITIGNORE_MARK = "# at-init: ATEST 产物（报告不进库；实例登记真身是本机配置）"


def gitignore_block(root):
    return GITIGNORE_MARK + "\n/%s/report/\n/.wn-instances.json\n" % root


def tree_dirs(root):
    return (root, root + "/cases", root + "/scripts", root + "/expect",
            root + "/baseline", root + "/report")


def gitkeep_files(root):
    return (root + "/cases/.gitkeep", root + "/scripts/.gitkeep",
            root + "/expect/.gitkeep", root + "/baseline/.gitkeep")


def default_config(project, root):
    """atest.json 的默认值（键顺序即写盘顺序）。instance 缺省 L2：完整回归建基线用。"""
    return {
        "version": 1,
        "project": project,
        "root": root,
        "instance": "L2",
        "test_domain": "atest",
        "credentials_file": ".at/credentials.json",
        "init_template": "%s/atest.init" % root,
        "sql_datasource": "default",
    }


DESCRIPTION = """\
在项目里生成 ATEST（运行时集成测试）的 <root>/ 骨架、atest.json 与实例登记样例。
根目录默认 atest（--root 可改）。
幂等：已存在一律 kept；--force 只刷新 .wn-instances.example.json，不动用户内容。"""

EPILOG = """\
生成物（<root> 默认 atest，由 --root 指定）:
  <root>/atest.init           静态布局模板（夹具；可编辑，--force 不覆盖）
  <root>/cases/               用例 <CASE-ID>.md
  <root>/scripts/             用例脚本 <CASE-ID>.<ext>
  <root>/expect/              独立期望文件 <CASE-ID>.json（expected: file）
  <root>/baseline/            基线 <CASE-ID>.json
  <root>/report/              报告输出（追加进 .gitignore）
  atest.json                  项目配置（version / project / root / instance /
                              test_domain / credentials_file / init_template /
                              sql_datasource）
  .wn-instances.example.json  实例登记样例（真身由开发者维护，本工具不写）

根部为什么叫 atest:
  项目自己的测试框架可能已经占用 test/（如 Maven 的 testSourceDirectory）；
  集成测试另起根目录，机器配置写 atest.json 的 root，at-run 据此解析。

配置只有 atest.json 一份:
  atest.json 缺失时新建并写默认值；已存在时**只补缺失的键**，
  不覆盖你已写的值（--force 同样不碰）。instance 缺省 L2（完整回归与基线在
  2 级实例上做），只在 1 级快速回路时改成 L1；instance 为空时 at-run 会退出 2。

幂等:
  已存在一律 kept，重复执行不产生任何写入。--force 只刷新
  .wn-instances.example.json（内容有差异时），并且不碰 atest.json 的已有值。
  cases/ scripts/ expect/ baseline/ 与 <root>/atest.init 里的内容永不覆盖。

.gitignore:
  追加 `# at-init:` 标记块：/<root>/report/ 与 /.wn-instances.json；
  旧块里的 /test/report/ 会被替换成 /<root>/report/（迁移），其余行不动。

分工:
  .at/（身份 / 会话 / 凭据）归 walnut-at-init；本工具只管 <root>/ 与 atest.json。

退出码:
  0  成功（含"全部 kept"）
  1  用法错误（参数不合法、项目名不合法、根目录名不合法）
  2  环境问题（目标目录不存在、模板缺失、写入失败、atest.json 不是合法 JSON）

输出（stdout，单行 JSON）:
  {"ok":true,"dry_run":false,"dir":"...","project":"...","root":"atest",
   "created":[...],"appended":[...],"refreshed":[...],"kept":[...],
   "warnings":[...]}

示例:
  python at-init.py --dir D:/work/myproj --name myproj
  python at-init.py --root spec
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


def ensure_config(work, project, root, dry_run, warnings):
    """项目根 atest.json：新建写默认值；已存在只补缺失键，不覆盖用户已写的值。"""
    path = work / CONFIG_NAME
    defaults = default_config(project, root)
    if not path.exists():
        if not dry_run:
            write_text(path, json.dumps(defaults, ensure_ascii=False, indent=2)
                       + "\n")
        return "created"
    try:
        data = json.loads(read_text(path))
    except (ValueError, EnvError) as e:
        raise EnvError("%s 不是合法 JSON（%s）：请修正后再跑（本工具不猜）"
                       % (path, e))
    if not isinstance(data, dict):
        raise EnvError("%s 格式不对：顶层应为对象（如 "
                       "{\"version\": 1, \"root\": \"%s\"}）" % (path, root))
    before = dict(data)
    for key, value in defaults.items():
        data.setdefault(key, value)
    changed = data != before
    if changed and not dry_run:
        file_nl = "\r\n" if b"\r\n" in path.read_bytes() else "\n"
        write_text(path, json.dumps(data, ensure_ascii=False, indent=2)
                   + "\n", newline=file_nl)
    # 不覆盖用户值，但不一致要当面说出来
    if data.get("root") != root:
        warnings.append("%s 里的 root=%r 与本次 --root=%r 不一致：保留文件里的值"
                        "（不覆盖），请确认后统一"
                        % (CONFIG_NAME, data.get("root"), root))
    if not data.get("instance"):
        warnings.append("%s 的 instance 为空：at-run 会退出 2；请填 L1 或 L2"
                        % CONFIG_NAME)
    return "refreshed" if changed else "kept"


def update_gitignore(work, root, dry_run, warnings):
    """维护 .gitignore 标记块；旧的 /test/report/ 行迁移成 /<root>/report/。"""
    path = work / ".gitignore"
    block = gitignore_block(root)
    if not path.exists():
        if not dry_run:
            write_text(path, block)
        return "created"
    old = read_text(path)
    file_nl = "\r\n" if b"\r\n" in path.read_bytes() else "\n"
    if GITIGNORE_MARK not in old:
        sep = "" if (old == "" or old.endswith("\n")) else "\n"
        lead = "\n" if old.strip() else ""
        if not dry_run:
            write_text(path, old + sep + lead + block, newline=file_nl)
        return "appended"

    new = old
    migrated = False
    if "/test/report/" in new:
        new = new.replace("/test/report/", "/%s/report/" % root)
        migrated = True
    missing = []
    if ("/%s/report/" % root) not in new:
        missing.append("/%s/report/" % root)
    if "/.wn-instances.json" not in new:
        missing.append("/.wn-instances.json")
    if missing:
        lines = new.splitlines()
        idx = next((i for i, line in enumerate(lines)
                    if line.strip() == GITIGNORE_MARK), None)
        if idx is None:
            lines.extend(missing)
        else:
            lines[idx + 1:idx + 1] = missing
        new = "\n".join(lines) + ("\n" if new.endswith("\n") else "")
    if new == old:
        return "kept"
    if not dry_run:
        write_text(path, new, newline=file_nl)
    if migrated:
        warnings.append(".gitignore 的旧行 /test/report/ 已迁移为 /%s/report/"
                        % root)
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
                    help="项目名，写进 atest.json 的 project 键（默认取目录名）")
    ap.add_argument("--root", default=ROOT_DEFAULT, metavar="NAME",
                    help="集成测试根目录名（默认 %s；匹配 %s，不能是 . 或 ..）"
                         % (ROOT_DEFAULT, ROOT_RE.pattern))
    ap.add_argument("--force", action="store_true",
                    help="刷新 .wn-instances.example.json（内容有差异时）；"
                         "atest.json 只补缺失键，不覆盖已有值；"
                         "不动 cases/ scripts/ expect/ baseline/ 与"
                         " <root>/atest.init")
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

    root = args.root
    if root in (".", "..") or not ROOT_RE.match(root):
        print(json.dumps({"ok": False,
                          "error": "根目录名不合法: %r（需匹配 %s，且不能是 . 或 ..）"
                                   % (root, ROOT_RE.pattern)},
                         ensure_ascii=False))
        return EXIT_USAGE

    tmpl_init = TMPL_DIR / "atest.init"
    tmpl_instances = TMPL_DIR / "wn-instances.example.json"
    missing = [str(p) for p in (tmpl_init, tmpl_instances)
               if not p.is_file()]
    if missing:
        print(json.dumps({"ok": False,
                          "error": "模板缺失，工具不完整: %s（%s）"
                                   % (", ".join(missing), TMPL_DIR)},
                         ensure_ascii=False))
        return EXIT_ENV

    dry = args.dry_run
    created, appended, refreshed, kept, warnings = [], [], [], [], []

    def bucket(action):
        return {"created": created, "appended": appended,
                "refreshed": refreshed, "kept": kept}[action]

    def ensure(rel, content, force=False):
        action = ensure_file(work / rel, content, force, dry)
        bucket(action).append(rel)
        return action

    try:
        for rel in tree_dirs(root):
            p = work / rel
            if p.is_dir():
                kept.append(rel + "/")
                continue
            if not dry:
                p.mkdir(parents=True, exist_ok=True)
            created.append(rel + "/")

        ensure(root + "/atest.init",
               read_text(tmpl_init).replace("{{root}}", root))
        for rel in gitkeep_files(root):
            ensure(rel, "")

        bucket(ensure_config(work, project, root, dry, warnings)
               ).append(CONFIG_NAME)

        ensure(INSTANCES_EXAMPLE, read_text(tmpl_instances), force=True)

        # .gitignore：报告与本机实例配置不进库（旧的 /test/report/ 会迁移）
        bucket(update_gitignore(work, root, dry, warnings)).append(".gitignore")
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
                      "project": project, "root": root, "created": created,
                      "appended": appended, "refreshed": refreshed,
                      "kept": kept, "warnings": warnings},
                     ensure_ascii=False))
    if dry:
        print("dry-run：以上只是计划，未写任何文件。", file=sys.stderr)
    else:
        print("下一步: 改 atest.json 的 instance / test_domain，写 %s/atest.init，"
              "再照 bin/at-init/tmpl/ 写第一个用例。" % root, file=sys.stderr)
    return EXIT_OK


if __name__ == "__main__":
    sys.exit(main())
