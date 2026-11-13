#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
at-baseline — ATEST 基线工具（ATESET-08）

一个工具、两个模式：

    --from <actual.json>   把 `at-run --record` 录到的 actual 清洗成**可移植**基线：
                           <root>/baseline/<CASE-ID>.json，带 meta（来源哈希 / 录制实例 /
                           录制时间 / 审核者 / 被丢字段 / 被转 matcher 的字段）；
                           已有基线缺 --force 时拒绝覆盖。
    --audit                扫全部基线，比对来源哈希：用例 md 或夹具模板改了 → 报 stale
                           （"基线待复核"），退出码 1。

清洗规则（ATESET-08 定稿，写进同目录 README）：

    - 丢掉键 `id` / `pid` / `sha1` / `ph` / `mnt` / `ln` / `c` / `m` / `g` /
      `d0` / `d1`（任意深度）；
    - 丢掉**值里含绝对路径**的键（`/home/…`、`C:\\…`、`file://…`、UNC `\\\\host\\…`）；
    - **时间类键 `ct` / `lm` / `expi` 不丢**：数值转成 `{"$range": ["$t0", "$t1"]}`
      ——"这个字段在运行期被写入过"仍是可断言的，只是不写死时刻（runner 的比较器
      对基线 `expected` 与 md 内联期望**用同一套 matcher**）；
    - 其余原样保留——它们就是"业务结果"；
    - 被丢的列进 `meta.dropped`、被转 matcher 的列进 `meta.matcherized`，
      审核者据此复核"没把业务字段洗掉、也没漏掉时间断言"。

基线格式（runner 只读 `expected`，`meta` 不是断言）：

    {"meta": {"case","source_hash","instance","recorded_at","reviewer",
              "dropped","matcherized"},
     "expected": {…}}

来源哈希 = sha256(用例 md + 夹具模板) 前 16 位；公式与 `bin/at-run/at-run.py` 的
`source_hash()` **必须完全一致**（两处都带注释，改一处就得改另一处）：

    sha256("md\\n" + md + "\\x00" + "init\\n" + init + "\\x00")[:16]

CRLF 先归一成 LF，同一份内容在 Windows / Linux checkout 上哈希相同。

本工具**不判断值对不对**（那是审核者的事）：只做机械清洗 + 留痕。录完把
`meta.reviewer` 从 `unreviewed` 改成复核者标识，再入库。

退出码: 0 成功 / 审计全 ok；1 用法错误、拒绝覆盖、审计有 stale；
        2 环境不可用（atest.json、用例目录、actual 文件、夹具模板缺失或坏档）。
零依赖 Python3；所有写入 UTF-8 无 BOM、LF；argv 传参不经 shell。
"""

import argparse
import hashlib
import json
import re
import sys
from datetime import datetime
from pathlib import Path

EXIT_OK = 0
EXIT_USAGE = 1
EXIT_ENV = 2

CONFIG_NAME = "atest.json"
ROOT_DEFAULT = "atest"
ROOT_RE = re.compile(r"^[A-Za-z0-9._-]+$")
CASE_ID_RE = re.compile(r"^CASE-[A-Z0-9-]{1,32}$")
FIELD_RE = re.compile(r"^expected\s*:\s*(\S+)\s*$", re.M)

# 清洗规则（定稿）：每次执行都不同 / 与环境绑定的键，进基线就没法移植
DROP_KEYS = ("id", "pid", "sha1", "ph", "mnt", "ln", "c", "m", "g",
             "d0", "d1")
# 时间类键：不丢、不写死——转成 $range matcher，"运行期被写入过"仍可断言
RANGE_KEYS = ("ct", "lm", "expi")
RANGE_MATCHER = {"$range": ["$t0", "$t1"]}
# 值里含绝对路径（含本机目录 / 文件 URL / UNC）→ 整键丢弃
ABS_PATH_RE = re.compile(
    r"(?:^|[\s\"'(=,:;])/(?:home|users|root|tmp|var|opt|mnt|usr|etc|srv|private|data)/"
    r"|\bfile://"
    r"|[A-Za-z]:[\\/]"
    r"|^\\\\[^\\\s]+\\",
    re.I)
REVIEWER_DEFAULT = "unreviewed"

DESCRIPTION = """\
ATEST 基线工具：--from 把录到的 actual 清洗成可移植基线（带来源哈希与留痕），
--audit 比对来源哈希找"用例改了、基线忘了重审"。退出码 0 好 / 1 待处理 / 2 环境不可用。"""

EPILOG = """\
基线路径（单一路径，不分实例）:
  <root>/baseline/<CASE-ID>.json —— 同一份基线在 L1 / L2、在别人的机器上都要能过；
  不同实例结果不同本身就是 bug，不用"两套基线"消化。

清洗（--from 自动做，规则见同目录 README）:
  丢键 id/pid/sha1/ph/mnt/ln/c/m/g/d0/d1；丢值里含绝对路径的键；
  ct/lm/expi 不丢——数值转成 {"$range": ["$t0", "$t1"]}（时间不写死，仍可断言）；
  被丢的进 meta.dropped、被转 matcher 的进 meta.matcherized。
  基线里的 expected 与用例 md 内联期望**用同一套 matcher**（比较器只看这个结构）。

来源哈希:
  sha256(用例 md + 夹具模板) 前 16 位（CRLF 归一），与 at-run 的 source_hash 一致。
  用例 md 或 atest.init 一改，--audit 就报 stale。

审核流程（写进 README）:
  执行者 --record + --from（机械动作）→ 审核者逐条看 expected、签 meta.reviewer
  （默认 unreviewed）→ 之后用例改动触发 --audit → 重新走复核。

示例:
  python bin/at-run/at-run.py --record
  python bin/at-baseline/at-baseline.py --from atest/report/20261113T153000.actual.json
  python bin/at-baseline/at-baseline.py --audit
  python bin/at-baseline/at-baseline.py --from <file> --only CASE-101 --reviewer 智群
  python bin/at-baseline/at-baseline.py --from <file> --force
"""


class _Parser(argparse.ArgumentParser):
    """argparse 默认用退出码 2 报参数错误，与本工具的「2 = 环境问题」冲突。"""

    def error(self, message):
        emit({"ok": False, "error": "%s（--help 看用法）" % message})
        raise SystemExit(EXIT_USAGE)


class EnvError(Exception):
    """环境不可用（退出码 2）"""


class UsageError(Exception):
    """用法错误 / 拒绝覆盖（退出码 1）"""


def force_utf8_stdio():
    """Windows 控制台默认按 cp936 编码，中文 JSON 进管道会变味。"""
    for stream in (sys.stdout, sys.stderr):
        try:
            stream.reconfigure(encoding="utf-8")
        except (AttributeError, ValueError, OSError):
            pass


def emit(obj):
    print(json.dumps(obj, ensure_ascii=False))


def read_text(path):
    """按 UTF-8 读；容忍 BOM（读出来不带 BOM）。"""
    try:
        return Path(path).read_text(encoding="utf-8-sig")
    except UnicodeDecodeError as e:
        raise EnvError("%s 不是 UTF-8 编码（%s）：请先转成 UTF-8 无 BOM"
                       % (path, e))


def write_text(path, text):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(str(path), "w", encoding="utf-8", newline="\n") as f:
        f.write(text)


def write_json(path, obj):
    write_text(path, json.dumps(obj, ensure_ascii=False, indent=2) + "\n")


def rel(path, root):
    try:
        return str(Path(path).resolve().relative_to(Path(root).resolve())
                   ).replace("\\", "/")
    except ValueError:
        return str(path)


def load_config(project):
    """读项目根 atest.json（唯一配置源）；缺失/坏档 → 退出 2，不猜。"""
    path = Path(project) / CONFIG_NAME
    if not path.is_file():
        raise EnvError("找不到 %s：先跑 bin/at-init/at-init.py 生成它" % path)
    try:
        data = json.loads(path.read_text(encoding="utf-8-sig"))
    except (ValueError, OSError) as e:
        raise EnvError("%s 不是合法 JSON（%s）：请修正后再跑" % (path, e))
    if not isinstance(data, dict):
        raise EnvError("%s 格式不对：顶层应为对象（version / root / instance ...）"
                       % path)
    return data


def resolve_root(config, override):
    """根目录：--root 覆盖 > atest.json 的 root > atest；非法值退出 2 / 1。"""
    if override is not None:
        if override in (".", "..") or not ROOT_RE.match(override):
            raise UsageError("--root 不合法：%r（需匹配 %s，且不能是 . 或 ..）"
                             % (override, ROOT_RE.pattern))
        return override
    root = config.get("root")
    if root is None or root == "":
        return ROOT_DEFAULT
    if not isinstance(root, str) or root in (".", "..") \
            or not ROOT_RE.match(root):
        raise EnvError("atest.json 的 root 不合法：%r（需匹配 %s）"
                       % (root, ROOT_RE.pattern))
    return root


def init_template_path(project, config, root):
    """夹具模板路径：atest.json 的 init_template > <root>/atest.init。"""
    raw = (config or {}).get("init_template") or ("%s/atest.init" % root)
    path = Path(raw)
    if not path.is_absolute():
        path = Path(project) / path
    return path


def source_hash(case_path, init_path):
    """基线来源哈希：sha256(用例 md + 夹具模板) 前 16 位（ATESET-08 定稿）。

    公式必须与 bin/at-run/at-run.py 的 source_hash() 完全一致：
        sha256("md\n" + md + "\x00" + "init\n" + init + "\x00")[:16]
    CRLF 先归一成 LF，同一份内容在 Windows / Linux checkout 上哈希相同。
    """
    digest = hashlib.sha256()
    for label, path in (("md", case_path), ("init", init_path)):
        text = read_text(path).replace("\r\n", "\n").replace("\r", "\n")
        digest.update(label.encode("ascii") + b"\n")
        digest.update(text.encode("utf-8"))
        digest.update(b"\x00")
    return digest.hexdigest()[:16]


def case_md_path(project, root, case_id):
    return Path(project) / root / "cases" / ("%s.md" % case_id)


def expected_kind(md_text):
    """从 front-matter 里取 `expected:` 的值（只认顶格的标量行）。"""
    lines = md_text.splitlines()
    if not lines or lines[0].strip() != "---":
        return ""
    fm = []
    for raw in lines[1:]:
        if raw.strip() == "---":
            break
        fm.append(raw)
    m = FIELD_RE.search("\n".join(fm))
    if not m:
        return ""
    return m.group(1).strip().strip("\"'")


def has_absolute_path(value):
    return isinstance(value, str) and ABS_PATH_RE.search(value) is not None


def is_number(value):
    """JSON 数字（排除 bool——Python 里 True 也是 int）。"""
    return isinstance(value, (int, float)) and not isinstance(value, bool)


def clean_actual(value, path="", dropped=None, matcherized=None):
    """递归清洗 actual；返回干净副本，并留痕两类处理。

    - 易变/环境键 → dropped（字段路径）；
    - 时间类键（ct/lm/expi）且值是数字 → 换成 {"$range": ["$t0", "$t1"]}，
      路径进 matcherized（"运行期被写入过"仍可断言，只是不写死时刻）；
    - 含绝对路径的值 → dropped（带 (absolute-path)）。
    """
    if dropped is None:
        dropped = []
    if matcherized is None:
        matcherized = []
    if isinstance(value, dict):
        out = {}
        for key, val in value.items():
            child = "%s.%s" % (path, key) if path else str(key)
            if str(key) in RANGE_KEYS:
                if is_number(val):
                    out[key] = dict(RANGE_MATCHER)
                    matcherized.append(child)
                else:
                    # 非数字的时间类值转不了 $range，只能丢（留痕）
                    dropped.append(child)
                continue
            if str(key) in DROP_KEYS:
                dropped.append(child)
                continue
            if has_absolute_path(val):
                dropped.append("%s (absolute-path)" % child)
                continue
            out[key] = clean_actual(val, child, dropped, matcherized)
        return out
    if isinstance(value, list):
        out = []
        for i, val in enumerate(value):
            child = "%s[%d]" % (path, i)
            if has_absolute_path(val):
                dropped.append("%s (absolute-path)" % child)
                continue
            out.append(clean_actual(val, child, dropped, matcherized))
        return out
    return value


def split_list(values):
    out = []
    for value in values or []:
        for part in str(value).split(","):
            part = part.strip()
            if part:
                out.append(part)
    return out


def read_json_file(path, what):
    try:
        data = json.loads(Path(path).read_text(encoding="utf-8-sig"))
    except (ValueError, OSError, EnvError) as e:
        raise EnvError("%s读不了或不是合法 JSON：%s（%s）" % (what, path, e))
    return data


def build_baseline(project, root, config, case_id, actual, meta_in, reviewer):
    """把一个 actual 变成基线文档；用例不齐 / 声明不对 → 抛错（不写半份）。"""
    md = case_md_path(project, root, case_id)
    if not md.is_file():
        raise UsageError("找不到用例 md %s（caseID = 文件名）：actual 里的 %s 对不上"
                         "任何用例" % (rel(md, project), case_id))
    kind = expected_kind(read_text(md))
    if kind != "baseline":
        raise UsageError("用例 %s 的 expected 是 %r，不是 baseline：生成基线不会生效，"
                         "先改用例再录" % (case_id, kind or "（缺）"))
    init = init_template_path(project, config, root)
    if not init.is_file():
        raise EnvError("缺夹具模板 %s：来源哈希必须含它（夹具变了期望常跟着变）"
                       % rel(init, project))
    dropped = []
    matcherized = []
    expected = clean_actual(actual, dropped=dropped, matcherized=matcherized)
    instance = meta_in.get("instance") or config.get("instance") or ""
    recorded_at = meta_in.get("timestamp") or datetime.now().astimezone(
    ).isoformat(timespec="seconds")
    meta = {"case": case_id,
            "source_hash": source_hash(md, init),
            "instance": instance,
            "recorded_at": recorded_at,
            "reviewer": reviewer,
            "dropped": dropped,
            "matcherized": matcherized}
    return {"meta": meta, "expected": expected}


def cmd_from(project, root, config, args):
    from_path = Path(args.from_file).expanduser()
    if not from_path.is_absolute():
        from_path = Path.cwd() / from_path
    if not from_path.is_file():
        raise EnvError("--from 指定的 actual 文件不存在：%s" % from_path)
    data = read_json_file(from_path, "actual 文件")
    if not isinstance(data, dict):
        raise EnvError("actual 文件顶层应为对象 {CASE-ID: actual}：%s" % from_path)
    meta_in = data.get("_meta")
    if not isinstance(meta_in, dict):
        meta_in = {}
    case_ids = [k for k in data if not str(k).startswith("_")]
    if not case_ids:
        raise UsageError("actual 文件里没有用例（只有 _meta？）：先跑 "
                         "at-run --record，见 bin/at-run/README.md")
    only = split_list(args.only)
    if only:
        unknown = sorted(set(only) - set(case_ids))
        if unknown:
            raise UsageError("--only 指向 actual 里没有的用例：%s（现有：%s）"
                             % (", ".join(unknown), ", ".join(case_ids)))
        case_ids = [c for c in case_ids if c in set(only)]
    for case_id in case_ids:
        if not CASE_ID_RE.match(str(case_id)):
            raise UsageError("actual 里的键 %r 不像 CASE-ID（%s）"
                             % (case_id, CASE_ID_RE.pattern))
        if not isinstance(data[case_id], dict):
            raise UsageError("actual[%s] 不是对象：脚本给的 actual 必须是 JSON 对象"
                             % case_id)

    # 先在内存里全部算完再落盘：一个用例不齐就一个文件都不写（不留半套基线）
    docs = []
    for case_id in case_ids:
        doc = build_baseline(project, root, config, case_id, data[case_id],
                             meta_in, args.reviewer)
        target = Path(project) / root / "baseline" / ("%s.json" % case_id)
        docs.append((case_id, target, doc))

    existing = [rel(target, project) for _cid, target, _doc in docs
                if target.exists()]
    if existing and not args.force:
        emit({"ok": False,
              "error": "基线已存在，拒绝覆盖（逐个复核；确要重录加 --force）：%s"
                       % ", ".join(existing),
              "existing": existing, "force": False})
        return EXIT_USAGE

    written = []
    for case_id, target, doc in docs:
        write_json(target, doc)
        written.append(rel(target, project))
        print("at-baseline: %s → %s（dropped: %s；matcherized: %s；reviewer: %s）"
              % (case_id, rel(target, project),
                 ", ".join(doc["meta"]["dropped"]) or "无",
                 ", ".join(doc["meta"]["matcherized"]) or "无",
                 doc["meta"]["reviewer"]), file=sys.stderr)
    if args.reviewer == REVIEWER_DEFAULT:
        print("at-baseline: meta.reviewer 仍是 %r —— 审核者复核后改成自己的标识"
              % REVIEWER_DEFAULT, file=sys.stderr)

    emit({"ok": True, "root": root,
          "from": str(from_path),
          "instance": meta_in.get("instance") or config.get("instance") or "",
          "cases": [{"caseID": cid, "file": rel(target, project),
                     "source_hash": doc["meta"]["source_hash"],
                     "reviewer": doc["meta"]["reviewer"],
                     "dropped": doc["meta"]["dropped"],
                     "matcherized": doc["meta"]["matcherized"]}
                    for cid, target, doc in docs],
          "written": written, "forced": bool(existing),
          "exit": EXIT_OK})
    return EXIT_OK


def cmd_audit(project, root, config):
    baseline_dir = Path(project) / root / "baseline"
    if not baseline_dir.is_dir():
        raise EnvError("找不到基线目录 %s：先跑 bin/at-init/at-init.py"
                       % rel(baseline_dir, project))
    init = init_template_path(project, config, root)
    files = sorted(x for x in baseline_dir.glob("*.json") if x.is_file())
    results = []
    for path in files:
        case_id = path.stem
        md = case_md_path(project, root, case_id)
        item = {"caseID": case_id, "file": rel(path, project)}
        if not md.is_file():
            item.update(status="orphan",
                        reason="用例 md %s 不见了：基线该删或用例该补"
                               % rel(md, project))
            results.append(item)
            continue
        try:
            data = read_json_file(path, "基线")
        except EnvError as e:
            item.update(status="invalid", reason=str(e))
            results.append(item)
            continue
        if not isinstance(data, dict):
            item.update(status="invalid", reason="基线顶层应为对象")
            results.append(item)
            continue
        meta = data.get("meta")
        stored = meta.get("source_hash") if isinstance(meta, dict) else None
        try:
            current = source_hash(md, init)
        except (EnvError, OSError) as e:
            item.update(status="hash-skipped", reason="算不了来源哈希：%s" % e)
            results.append(item)
            continue
        item["baseline_hash"] = stored or ""
        item["source_hash"] = current
        if not stored:
            item.update(status="no-hash",
                        reason="基线缺 meta.source_hash：用例改了也发现不了；"
                               "重录（当前哈希 %s）" % current)
        elif stored != current:
            item.update(status="stale",
                        reason="用例 md / 夹具模板改了，基线待复核（基线 %s ≠ 当前 %s）"
                               % (stored, current))
        else:
            item["status"] = "ok"
        results.append(item)

    bad = [r for r in results if r["status"] != "ok"]
    emit({"ok": not bad, "root": root, "checked": len(results),
          "stale": len(bad), "results": results,
          "exit": EXIT_OK if not bad else EXIT_USAGE})
    if bad:
        for item in bad:
            print("at-baseline: %s %s —— %s" % (
                item["caseID"], item["status"], item.get("reason", "")),
                file=sys.stderr)
        print("at-baseline: %d/%d 份基线待复核（用例改了没重审？）"
              % (len(bad), len(results)), file=sys.stderr)
    else:
        print("at-baseline: %d 份基线哈希全部对得上（无需复核）"
              % len(results), file=sys.stderr)
    return EXIT_OK if not bad else EXIT_USAGE


def main(argv=None):
    force_utf8_stdio()
    ap = _Parser(prog="at-baseline", description=DESCRIPTION, epilog=EPILOG,
                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--from", dest="from_file", metavar="ACTUAL_JSON",
                    help="at-run --record 产出的 <ts>.actual.json（清洗后写基线）")
    ap.add_argument("--audit", action="store_true",
                    help="扫全部基线，比对来源哈希；stale 时退出 1")
    ap.add_argument("--dir", default=".", metavar="DIR",
                    help="项目目录（默认当前目录）")
    ap.add_argument("--root", metavar="NAME",
                    help="集成测试根目录名（缺省读 <dir>/atest.json 的 root）")
    ap.add_argument("--only", action="append", default=[], metavar="IDS",
                    help="只处理这些 CASE-ID（逗号分隔，可重复；只用于 --from）")
    ap.add_argument("--reviewer", default=REVIEWER_DEFAULT, metavar="NAME",
                    help="写进 meta.reviewer 的审核者标识（默认 %r = 未审核）"
                         % REVIEWER_DEFAULT)
    ap.add_argument("--force", action="store_true",
                    help="基线已存在时覆盖（默认拒绝；覆盖 = 期望换了，要重新复核）")
    args = ap.parse_args(argv)

    if bool(args.from_file) == bool(args.audit):
        emit({"ok": False, "error": "要么 --from <actual.json>，要么 --audit（二选一）"})
        return EXIT_USAGE
    if args.audit and (args.only or args.force):
        emit({"ok": False, "error": "--only / --force 只用于 --from，不用于 --audit"})
        return EXIT_USAGE

    project = Path(args.dir).expanduser()
    if not project.exists() or not project.is_dir():
        emit({"ok": False, "error": "项目目录不存在或不是目录：%s" % project})
        return EXIT_ENV

    try:
        config = load_config(project)
        root = resolve_root(config, args.root)
        cases_dir = project / root / "cases"
        if not cases_dir.is_dir():
            raise EnvError("找不到用例目录 %s：先跑 bin/at-init/at-init.py"
                           % rel(cases_dir, project))
        if args.audit:
            return cmd_audit(project, root, config)
        return cmd_from(project, root, config, args)
    except UsageError as e:
        emit({"ok": False, "error": str(e)})
        return EXIT_USAGE
    except EnvError as e:
        emit({"ok": False, "error": str(e)})
        return EXIT_ENV
    except OSError as e:
        emit({"ok": False, "error": "文件操作失败：%s" % e})
        return EXIT_ENV


if __name__ == "__main__":
    sys.exit(main())
