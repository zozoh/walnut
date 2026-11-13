#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
at-report — ATEST 报告视图（JSON → Markdown）

读最近一次（或指定）JSON 报告，生成两份内容相同的 Markdown：

    <root>/report/<timestamp>.md      本次报告的人读视图
    <root>/report/latest.md           同内容（覆盖写，供人"只看一眼"）

报告结构（ATESET-07 定稿，改动先改任务书）：

    # ATEST 报告 · <timestamp>
    - 实例 / 测试域 / 根目录
    - 时间 / 几绿几红几 blocked / 耗时 / 退出码
    ## 用例        表格一行一例（CASE-ID / 状态 / 耗时 / 摘要）
    ## 红灯        仅在有 failed 时出现；每例 diff、证据相对路径、复现命令
    ## 趋势        只与**上一份**比：新红 / 修好 / 仍红（另列新增、消失）
    ## 建议        固定留空，人/智能体补充；工具不写

纯读：只读 JSON 报告与证据里的 summary.txt，**不执行用例、不连实例**。
证据与报告路径一律相对项目根（报告可整体挪走而不失效）。

报告 JSON 由 at-run 产出（schema = "atest-report/2"；v1 无 schema 也接受）。
"上一份" = 与源 JSON **同目录**、按时间戳排序紧邻的前一份（排除 latest.json）。

退出码（沿用 bin 工具语义）:
    0  生成成功且报告无红（blocked 不算红）
    1  生成成功但报告有红 / 用法错误
    2  环境不可用（atest.json 或报告 JSON 缺失、JSON 损坏、写入失败）

零依赖 Python3；输出 UTF-8、LF。
"""

import argparse
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
REPORT_DIR_NAME = "report"
LATEST_JSON = "latest.json"
LATEST_MD = "latest.md"

REPORT_SCHEMA = "atest-report/2"
ROOT_RE = re.compile(r"^[A-Za-z0-9._-]+$")
RUN_ID_RE = re.compile(r"^\d{8}T\d{6}(?:-\d+)?$")
STATUSES = ("passed", "failed", "blocked")
BADGE = {"passed": "✅ passed", "failed": "❌ failed", "blocked": "⛔ blocked"}
IGNORED_REPORTS = (LATEST_JSON,)

DESCRIPTION = "at-report — 把 ATEST JSON 报告渲染成人读的 Markdown"
EPILOG = """示例:
  python bin/at-report/at-report.py
  python bin/at-report/at-report.py latest
  python bin/at-report/at-report.py atest/report/20261113T145425.json
  python bin/at-report/at-report.py --dir D:/work/myproj --root spec
"""


class _Parser(argparse.ArgumentParser):
    """argparse 默认用退出码 2 报参数错误，与本工具的「2 = 环境问题」冲突。"""

    def error(self, message):
        emit({"ok": False, "error": "%s（--help 看用法）" % message})
        raise SystemExit(EXIT_USAGE)


class EnvError(Exception):
    """环境不可用（退出码 2）"""


class UsageError(Exception):
    """用法错误（退出码 1）"""


# ---------------------------------------------------------------- 基础

def force_utf8_stdio():
    """Windows 控制台默认按 cp936 编码，中文进管道会变味。"""
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
    """写 UTF-8 无 BOM、LF（显式 newline='\\n'，Windows 上也不转 CRLF）。"""
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(str(path), "w", encoding="utf-8", newline="\n") as f:
        f.write(text)


def rel(root, path):
    """相对项目根的 POSIX 路径；不在根下时原样返回（并保持可读）。"""
    try:
        return str(Path(path).resolve().relative_to(Path(root).resolve())
                   ).replace("\\", "/")
    except ValueError:
        return str(path)
    except OSError:
        return str(path)


# ---------------------------------------------------------------- 配置与报告

def load_config(project):
    """读项目根 atest.json（唯一配置源）；缺失/坏档 → 退出 2，不猜。"""
    path = Path(project) / CONFIG_NAME
    if not path.is_file():
        raise EnvError("找不到 %s：先跑 bin/at-init/at-init.py 生成它，"
                       "或在项目根建 atest.json" % path)
    try:
        data = json.loads(read_text(path))
    except ValueError as e:
        raise EnvError("%s 不是合法 JSON（%s）：请修正后再跑" % (path, e))
    if not isinstance(data, dict):
        raise EnvError("%s 格式不对：顶层应为对象（version / root / instance ...）"
                       % path)
    return data, path


def resolve_root(config, config_path, override):
    """根目录：--root 覆盖 > atest.json 的 root > atest；非法值退出 1 / 2。"""
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
        raise EnvError("%s 的 root 不合法：%r（需匹配 %s，且不能是 . 或 ..）"
                       % (config_path, root, ROOT_RE.pattern))
    return root


def resolve_report(project, root, arg):
    """报告路径：latest（缺省）→ <root>/report/latest.json；否则按路径解析。"""
    if arg is None or arg == "" or arg == "latest":
        path = Path(project) / root / REPORT_DIR_NAME / LATEST_JSON
        if not path.is_file():
            raise EnvError("找不到最近的报告 %s：先跑 bin/at-run/at-run.py"
                           "（或用位置参数指定一份 JSON 报告）" % path)
        return path
    path = Path(arg).expanduser()
    if not path.is_absolute():
        path = Path(project) / path
    if not path.is_file():
        raise EnvError("报告文件不存在：%s" % path)
    return path


def load_report(path):
    """读并校验报告 JSON；只做结构检查，数值一律以 cases 为准。"""
    try:
        data = json.loads(read_text(path))
    except ValueError as e:
        raise EnvError("%s 不是合法 JSON（%s）：报告可能写坏了" % (path, e))
    if not isinstance(data, dict):
        raise EnvError("%s 格式不对：顶层应为对象" % path)
    schema = data.get("schema")
    if schema is not None and schema != REPORT_SCHEMA:
        raise EnvError("%s 的 schema 不认识：%r（本工具支持 %s 与无 schema 的 v1）"
                       % (path, schema, REPORT_SCHEMA))
    cases = data.get("cases")
    if not isinstance(cases, list):
        raise EnvError("%s 缺 cases 数组：不是 at-run 产出的报告" % path)
    for i, case in enumerate(cases):
        where = "%s 的 cases[%d]" % (path, i)
        if not isinstance(case, dict):
            raise EnvError("%s 不是对象" % where)
        case_id = case.get("caseID")
        if not isinstance(case_id, str) or not case_id:
            raise EnvError("%s 缺 caseID（或不是字符串）" % where)
        status = case.get("status")
        if status not in STATUSES:
            raise EnvError("%s（%s）的 status 不认识：%r"
                           % (where, case_id, status))
        duration = case.get("duration_ms", 0)
        if isinstance(duration, bool) or not isinstance(duration, (int, float)):
            raise EnvError("%s（%s）的 duration_ms 不是数字" % (where, case_id))
        evidence_dir = case.get("evidence_dir", "")
        if not isinstance(evidence_dir, str):
            raise EnvError("%s（%s）的 evidence_dir 不是字符串"
                           % (where, case_id))
        diff = case.get("diff", [])
        if not isinstance(diff, list):
            raise EnvError("%s（%s）的 diff 不是数组" % (where, case_id))
        evidence = case.get("evidence", [])
        if not isinstance(evidence, list):
            raise EnvError("%s（%s）的 evidence 不是数组" % (where, case_id))
    return data


def run_id_of(path, report):
    """报告的运行 ID：优先用文件名（<timestamp>.json），否则由 timestamp 推。"""
    stem = Path(path).stem
    if RUN_ID_RE.match(stem):
        return stem
    stamp = report.get("timestamp")
    if not isinstance(stamp, str) or not stamp:
        raise EnvError("%s 既不是 <timestamp>.json，也没有 timestamp 字段："
                       "无法决定 Markdown 文件名" % path)
    try:
        return datetime.fromisoformat(stamp.replace("Z", "+00:00")) \
            .strftime("%Y%m%dT%H%M%S")
    except ValueError:
        raise EnvError("%s 的 timestamp 不是 ISO 时间：%r" % (path, stamp))


def find_previous(source, run_id):
    """同目录中按运行 ID 紧邻的前一份（排除 latest.json）；返回 (path, report, err)。"""
    parent = Path(source).parent
    candidates = []
    try:
        entries = list(parent.glob("*.json"))
    except OSError:
        entries = []
    for path in entries:
        if path.name in IGNORED_REPORTS:
            continue
        if path == Path(source):
            continue
        stem = path.stem
        if not RUN_ID_RE.match(stem) or stem >= run_id:
            continue
        candidates.append((stem, path))
    if not candidates:
        return None, None, ""
    _, prev_path = max(candidates, key=lambda item: item[0])
    try:
        return prev_path, load_report(prev_path), ""
    except EnvError as e:
        return prev_path, None, str(e)


# ---------------------------------------------------------------- 取值与格式化

def counts(report):
    cases = report["cases"]
    passed = sum(1 for c in cases if c["status"] == "passed")
    failed = sum(1 for c in cases if c["status"] == "failed")
    blocked = sum(1 for c in cases if c["status"] == "blocked")
    return passed, failed, blocked


def fmt_duration(ms):
    ms = int(ms)
    if ms < 1000:
        return "%dms" % ms
    if ms < 60000:
        return "%.1fs" % (ms / 1000.0)
    return "%dm%02ds" % (ms // 60000, (ms % 60000) // 1000)


def show(value, limit=200):
    """diff 里值的人读形式：JSON 值，过长截断（不改报告原值）。"""
    try:
        text = json.dumps(value, ensure_ascii=False)
    except (TypeError, ValueError):
        text = repr(value)
    text = text.replace("\r", " ").replace("\n", " ")
    if len(text) > limit:
        text = text[:limit] + " …"
    return text


def code(text):
    """Markdown 行内代码；值里有反引号时用双反引号包。"""
    text = str(text)
    if "`" in text:
        return "`` %s ``" % text
    return "`%s`" % text


def cell(text):
    """表格单元格：单行、转义竖线。"""
    return str(text).replace("|", "\\|").replace("\r", " ").replace("\n", " ")


def summary_line(case):
    """从证据 summary.txt 首行取一句结论（失败/blocked 摘要用）；读不到返回空。"""
    evidence_dir = case.get("evidence_dir") or ""
    if not evidence_dir:
        return ""
    path = Path(evidence_dir) / "summary.txt"
    try:
        first = read_text(path).splitlines()[0].strip()
    except (EnvError, OSError, IndexError):
        return ""
    marker = " —— "
    if marker in first:
        return first.split(marker, 1)[1].strip()
    return first


def case_summary(case):
    """表格「摘要」列：失败看 diff 路径，blocked 看一句话，通关是 —。"""
    if case["status"] == "passed":
        return "—"
    diffs = case.get("diff") or []
    if case["status"] == "failed" and diffs:
        paths = [str(d.get("path") or "$") for d in diffs if isinstance(d, dict)]
        text = "diff: " + "、".join(paths[:3])
        if len(paths) > 3:
            text += " 等 %d 处" % len(paths)
        return text
    note = summary_line(case)
    if note:
        return note[:80] + (" …" if len(note) > 80 else "")
    return "—"


def evidence_paths(project, case):
    """证据文件相对项目根的路径；summary.txt 在前，objects/ 次之。"""
    evidence_dir = case.get("evidence_dir") or ""
    names = [str(n) for n in (case.get("evidence") or [])]

    def order(name):
        if name == "summary.txt":
            return (0, name)
        if name.startswith("objects/"):
            return (1, name)
        return (2, name)

    out = []
    for name in sorted(names, key=order):
        path = Path(evidence_dir) / name if evidence_dir else None
        out.append(rel(project, path) if path is not None else name)
    return out


def fmt_source(project, source):
    if not source:
        return "?"
    if source == "cli":
        return "cli"
    path = Path(source)
    return rel(project, path) if path.is_absolute() else source


def fmt_ids(ids):
    return "、".join(ids) if ids else "无"


# ---------------------------------------------------------------- 渲染

def compute_trend(source, run_id, report):
    """与同目录上一份报告比：新红 / 修好 / 仍红 / 新增 / 消失。"""
    previous_path, previous, previous_err = find_previous(source, run_id)
    trend = {"previous": None, "error": previous_err,
             "new_red": [], "fixed": [], "still_red": [],
             "added": [], "removed": []}
    if previous is not None:
        trend["previous"] = run_id_of(previous_path, previous)
        prev_status = {c["caseID"]: c["status"] for c in previous["cases"]}
        cur_status = {c["caseID"]: c["status"] for c in report["cases"]}
        trend["new_red"] = [c["caseID"] for c in report["cases"]
                            if c["status"] == "failed"
                            and prev_status.get(c["caseID"]) != "failed"]
        trend["fixed"] = [cid for cid, status in prev_status.items()
                          if status == "failed" and cur_status.get(cid) != "failed"]
        trend["still_red"] = [c["caseID"] for c in report["cases"]
                              if c["status"] == "failed"
                              and prev_status.get(c["caseID"]) == "failed"]
        trend["added"] = [c["caseID"] for c in report["cases"]
                          if c["caseID"] not in prev_status]
        trend["removed"] = [cid for cid in prev_status if cid not in cur_status]
    return trend


def render(report, project, root, config, run_id, trend):
    """把报告渲染成 Markdown 文本（换行统一 LF，结尾一个换行）。"""
    cases = report["cases"]
    passed, failed, blocked = counts(report)
    total_ms = sum(int(c.get("duration_ms", 0)) for c in cases)
    exit_code = EXIT_OK if failed == 0 else EXIT_USAGE
    timestamp = report.get("timestamp") or run_id

    lines = []
    lines.append("# ATEST 报告 · %s" % run_id)
    lines.append("")
    lines.append("- 实例：%s（来源 %s）　测试域：%s　根目录：%s"
                 % (report.get("instance") or "?",
                    fmt_source(project, report.get("instance_source")),
                    config.get("test_domain") or "?",
                    root))
    lines.append("- 时间：%s" % timestamp)
    lines.append("- 结果：%d 绿 / %d 红 / %d blocked，耗时 %s，退出码 %d"
                 % (passed, failed, blocked, fmt_duration(total_ms), exit_code))
    lines.append("")

    # ---- 用例
    lines.append("## 用例")
    lines.append("")
    lines.append("| CASE-ID | 状态 | 耗时 | 摘要 |")
    lines.append("|---|---|---|---|")
    for case in cases:
        lines.append("| %s | %s | %s | %s |"
                     % (cell(case["caseID"]),
                        BADGE[case["status"]],
                        fmt_duration(case.get("duration_ms", 0)),
                        cell(case_summary(case))))
    lines.append("")

    # ---- 红灯
    reds = [c for c in cases if c["status"] == "failed"]
    if reds:
        lines.append("## 红灯")
        lines.append("")
        for case in reds:
            case_id = case["caseID"]
            diffs = [d for d in (case.get("diff") or []) if isinstance(d, dict)]
            if diffs:
                paths = [str(d.get("path") or "$") for d in diffs]
                if len(paths) <= 3:
                    lines.append("### %s diff: %s"
                                 % (case_id, "、".join(paths)))
                else:
                    lines.append("### %s diff: %s 等 %d 处"
                                 % (case_id, "、".join(paths[:3]), len(paths)))
                for diff in diffs:
                    path = str(diff.get("path") or "$")
                    expected = show(diff.get("expected"))
                    actual = show(diff.get("actual"))
                    if len(diffs) == 1:
                        lines.append("- 期望：%s　实际：%s"
                                     % (code(expected), code(actual)))
                    else:
                        lines.append("- %s：期望 %s　实际 %s"
                                     % (code(path), code(expected),
                                        code(actual)))
            else:
                lines.append("### %s（无 diff）" % case_id)
                note = summary_line(case)
                if note:
                    lines.append("- 结论：%s" % note)
            evidence = evidence_paths(project, case)
            lines.append("- 证据：%s"
                         % ("、".join(code(p) for p in evidence)
                            if evidence else "—"))
            lines.append("- 复现：%s" % code(repro_command(report, config, root,
                                                          case_id)))
            lines.append("")

    # ---- 趋势
    if trend["previous"] is not None:
        lines.append("## 趋势（对比 %s）" % trend["previous"])
    else:
        lines.append("## 趋势")
    lines.append("")
    if trend["error"]:
        lines.append("- 上一份报告不可用：%s" % trend["error"])
    elif trend["previous"] is None:
        lines.append("- 无上一份报告可比（首份）")
    else:
        lines.append("- 新红：%s　修好：%s　仍红：%s"
                     % (fmt_ids(trend["new_red"]), fmt_ids(trend["fixed"]),
                        fmt_ids(trend["still_red"])))
        if trend["added"] or trend["removed"]:
            lines.append("- 新增：%s　消失：%s"
                         % (fmt_ids(trend["added"]),
                            fmt_ids(trend["removed"])))
    lines.append("")

    # ---- 建议（固定留空）
    lines.append("## 建议")
    lines.append("")
    lines.append("（人/智能体补充；工具不写）")
    lines.append("")
    return "\n".join(lines)


def repro_command(report, config, root, case_id):
    """可直接复制到项目根执行的复现命令。"""
    return ("python bin/at-run/at-run.py --dir . --root %s --instance %s "
            "--domain %s --only %s"
            % (root, report.get("instance") or "<instance>",
               config.get("test_domain") or "<test_domain>", case_id))


# ---------------------------------------------------------------- 入口

def main(argv=None):
    force_utf8_stdio()
    ap = _Parser(prog="at-report", description=DESCRIPTION, epilog=EPILOG,
                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("report", nargs="?", default="latest", metavar="REPORT",
                    help="JSON 报告路径，或 latest（缺省：<root>/report/latest.json）")
    ap.add_argument("--dir", default=".", metavar="DIR",
                    help="项目目录（默认当前目录）")
    ap.add_argument("--root", metavar="NAME",
                    help="集成测试根目录名（缺省读 <dir>/atest.json 的 root，"
                         "再缺省 atest）")
    args = ap.parse_args(argv)

    project = Path(args.dir).expanduser()
    if not project.exists():
        emit({"ok": False, "error": "项目目录不存在：%s" % project})
        return EXIT_ENV
    if not project.is_dir():
        emit({"ok": False, "error": "项目路径不是目录：%s" % project})
        return EXIT_ENV

    try:
        config, config_path = load_config(project)
        root = resolve_root(config, config_path, args.root)
        source = resolve_report(project, root, args.report)
        report = load_report(source)
        run_id = run_id_of(source, report)
        trend = compute_trend(source, run_id, report)
        text = render(report, project, root, config, run_id, trend)

        report_dir = Path(project) / root / REPORT_DIR_NAME
        md_path = report_dir / (run_id + ".md")
        latest_md = report_dir / LATEST_MD
        write_text(md_path, text)
        write_text(latest_md, text)

        passed, failed, blocked = counts(report)
        exit_code = EXIT_OK if failed == 0 else EXIT_USAGE
        previous_err = trend["error"]
        previous = trend["previous"]
        trend_out = {"previous": previous, "new_red": trend["new_red"],
                     "fixed": trend["fixed"], "still_red": trend["still_red"],
                     "added": trend["added"], "removed": trend["removed"]}
        print("at-report: 已生成 %s（%d 绿 / %d 红 / %d blocked）"
              % (rel(project, md_path), passed, failed, blocked))
        if previous is None:
            if previous_err:
                print("at-report: 趋势 —— 上一份不可用：%s" % previous_err)
            else:
                print("at-report: 趋势 —— 无上一份报告可比（首份）")
        else:
            print("at-report: 趋势 %s → %s —— 新红 %d / 修好 %d / 仍红 %d"
                  % (previous, run_id, len(trend["new_red"]),
                     len(trend["fixed"]), len(trend["still_red"])))
        emit({"ok": failed == 0,
              "source": rel(project, source),
              "report": rel(project, md_path),
              "latest": rel(project, latest_md),
              "timestamp": run_id,
              "total": len(report["cases"]),
              "passed": passed, "failed": failed, "blocked": blocked,
              "trend": trend_out,
              "exit": exit_code})
        return exit_code
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
