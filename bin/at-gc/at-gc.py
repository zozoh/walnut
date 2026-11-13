#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
at-gc — ATEST 报告归档（ATESET-14）

`<root>/report/` 里"一次执行"有两种盘上形态（本工具都认）：

    新一代（ATESET-16 起）       旧一代（现在盘上 70 份）
    <ts>/report.json             <ts>.json
    <ts>/report.md               <ts>.md
    <ts>/actual.json             <ts>.actual.json
    <ts>/cases/<CASE-ID>/…       <ts>/<CASE-ID>/…        （证据目录）

判定顺序：先看 `<ts>/report.json`，没有再退回 `<ts>.json`。
按时间戳排序，**最新 N 个原样保留**（N = --keep > atest.json 的
`report_keep` > 3）；更早的每个 T 打成一个 `<root>/report/archive/T.zip`，
**zip 逐成员校验与原件一致后才删原件**（先压缩、后删除）。

归档后的 zip 统一成"解出来就是新一代的一次执行"：根下 `report.json`、
`report.md`（有则）、`actual.json`（有则）、`cases/…`。旧一代按
`<ts>.json → report.json`、`<ts>.md → report.md`、
`<ts>.actual.json → actual.json`、`<ts>/<CASE-ID>/ → cases/<CASE-ID>/` 改名。

- `latest.json` / `latest.md` 永不归档；
- 正在写的报告跳过：主报告 json 缺失（新形态 `report.json` / 旧形态
  `<ts>.json`）或 5 分钟内有改动；
- 幂等：成功归档后原件已删，重复运行没有可归档对象；若在"校验通过、
  删到一半"时中断，下次核对 zip 覆盖剩余原件后把删除做完，不重复压缩；
- `--dry-run` 只打印计划：不建 archive/、不写 zip、不删任何文件。

输出最后一行单行 JSON：`{ok, kept, archived:[{ts,zip,files,bytes,form}],
freed_bytes, dry_run, keep, skipped, errors}`；人读摘要进 stderr。
`files` 是这次被归档的顶层原件（相对项目根，正斜杠），`bytes` 是原来件总字节。

退出码: 0 好 / 1 用法或 IO 错 / 2 环境不可用（atest.json、report 目录缺失或坏档）。
零依赖 Python3；所有写入 UTF-8 无 BOM、LF；argv 传参不经 shell。
"""

import argparse
import json
import os
import re
import shutil
import sys
import time
import zipfile
from pathlib import Path

EXIT_OK = 0
EXIT_USAGE = 1
EXIT_ENV = 2

CONFIG_NAME = "atest.json"
ROOT_DEFAULT = "atest"
REPORT_DIR_NAME = "report"
ARCHIVE_DIR_NAME = "archive"
LATEST_JSON = "latest.json"
LATEST_MD = "latest.md"

# 一次报告的时间戳（与 at-run / at-report 一致；-2 是同一秒重复跑的序号）
RUN_ID_RE = re.compile(r"^\d{8}T\d{6}(?:-\d+)?$")
ROOT_RE = re.compile(r"^[A-Za-z0-9._-]+$")
REPORT_SUFFIXES = (".actual.json", ".json", ".md")

KEEP_KEY = "report_keep"
KEEP_DEFAULT = 3
# 5 分钟内改过的报告视为"正在写"，不归 gc（防并发误删）
INFLIGHT_SECONDS = 300

DESCRIPTION = """\
ATEST 报告归档：只保留最近 N 次报告，更早的压成 report/archive/<T>.zip
（校验通过才删原件）。新（<T>/report.json）旧（<T>.json）两种形态都认。
退出码 0 好 / 1 用法或 IO 错 / 2 环境不可用。"""

EPILOG = """\
"一次执行"的两种盘上形态（判定顺序：先看 <T>/report.json，再退回 <T>.json）:
  新  <root>/report/<T>/report.json + report.md + actual.json + cases/<CASE-ID>/…
  旧  <root>/report/<T>.json + <T>.md + <T>.actual.json + <T>/<CASE-ID>/…
  保留最新 N 个（默认 3）；更早的每个 T 压成 <root>/report/archive/<T>.zip。
  latest.json / latest.md 永不归档。

zip 内的布局（统一成新一代形态，解出来就是一次执行）:
  report.json / report.md / actual.json / cases/…
  旧形态的 <T>.json → report.json、<T>.md → report.md、
  <T>.actual.json → actual.json、<T>/<CASE-ID>/ → cases/<CASE-ID>/。

安全规则:
  先写 zip → 逐成员比对内容 → 才删原件；
  主报告 json 缺失（还没写完）或 5 分钟内有改动 → 跳过；
  已归档的 T 无原件、不在扫描里，重复运行不会重复压缩；
  若删到一半中断，下次核对 zip 覆盖剩余原件后把删除做完（不重复压缩）。

保留数:
  --keep > atest.json 的 report_keep > 3；项目可在 atest.json 里覆盖。

输出:
  stderr 人读摘要；stdout 最后一行单行 JSON:
  {"ok":true,"kept":3,"archived":[{"ts","zip","files","bytes","form"}],
   "freed_bytes":123,"dry_run":false,"keep":3,"skipped":[],"errors":[]}

示例:
  python bin/at-gc/at-gc.py --dry-run
  python bin/at-gc/at-gc.py --keep 5
  python bin/at-gc/at-gc.py --dir D:/work/myproj --root spec
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


class VerifyError(Exception):
    """zip 校验失败（退出码 1；原件不删）"""


def force_utf8_stdio():
    """Windows 控制台默认按 cp936 编码，中文 JSON 进管道会变味。"""
    for stream in (sys.stdout, sys.stderr):
        try:
            stream.reconfigure(encoding="utf-8")
        except (AttributeError, ValueError, OSError):
            pass


def emit(obj):
    print(json.dumps(obj, ensure_ascii=False))


def rel(path, root):
    try:
        return str(Path(path).resolve().relative_to(Path(root).resolve())
                   ).replace("\\", "/")
    except ValueError:
        return str(path)


def size_text(num):
    """人读字节数（1024 进制，一位小数）。"""
    value = float(num)
    for unit in ("B", "KB", "MB", "GB"):
        if value < 1024.0 or unit == "GB":
            return ("%d %s" % (num, unit)) if unit == "B" \
                else ("%.1f %s" % (value, unit))
        value /= 1024.0
    return "%d B" % num


def load_config(project):
    """读项目根 atest.json（唯一配置源）；缺失/坏档 → 退出 2，不猜。"""
    path = Path(project) / CONFIG_NAME
    if not path.is_file():
        raise EnvError("找不到 %s：先跑 bin/at-init/at-init.py 生成它"
                       % path)
    try:
        data = json.loads(path.read_text(encoding="utf-8-sig"))
    except (ValueError, OSError) as e:
        raise EnvError("%s 不是合法 JSON（%s）：请修正后再跑" % (path, e))
    if not isinstance(data, dict):
        raise EnvError("%s 格式不对：顶层应为对象（version / root / "
                       "report_keep ...）" % path)
    return data, path


def resolve_root(config, config_path, override):
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
        raise EnvError("%s 的 root 不合法：%r（需匹配 %s）"
                       % (config_path, root, ROOT_RE.pattern))
    return root


def load_keep(config, config_path, override):
    """保留数：--keep > atest.json 的 report_keep > 3（非负整数）。"""
    if override is not None:
        if override < 0:
            raise UsageError("--keep 不能为负：%d" % override)
        return override
    raw = config.get(KEEP_KEY)
    if raw is None:
        return KEEP_DEFAULT
    if isinstance(raw, bool) or not isinstance(raw, int) or raw < 0:
        raise EnvError("%s 的 %s 不合法：%r（需 ≥ 0 的整数；缺省 %d）"
                       % (config_path, KEEP_KEY, raw, KEEP_DEFAULT))
    return raw


def run_id_of(name):
    """文件名 → 时间戳前缀；不是报告产物时返回 None。"""
    for suffix in REPORT_SUFFIXES:
        if name.endswith(suffix):
            stem = name[:-len(suffix)]
            return stem if RUN_ID_RE.match(stem) else None
    return None


def walk_files(directory):
    """目录下的全部文件（不跟随目录符号链接；顺序稳定）。"""
    out = []
    for dirpath, dirnames, filenames in os.walk(str(directory)):
        dirnames.sort()
        for name in sorted(filenames):
            out.append(Path(dirpath) / name)
    return out


def scan_runs(report_dir):
    """把 report/ 顶层条目按时间戳分组；返回 (runs, invalid)。

    runs[ts] = {"dir": 目录|None, "files": [顶层文件], "report": 主报告|None,
                "new": 是否新形态}。
    latest.json / latest.md 与 archive/ 直接忽略；名字不像时间戳的报告产物进 invalid。
    """
    dirs = {}
    files = {}
    invalid = []
    for entry in sorted(report_dir.iterdir(), key=lambda p: p.name):
        name = entry.name
        if name in (LATEST_JSON, LATEST_MD, ARCHIVE_DIR_NAME):
            continue
        if entry.is_dir():
            if RUN_ID_RE.match(name):
                dirs[name] = entry
            else:
                invalid.append(entry)
            continue
        if not entry.is_file():
            continue
        ts = run_id_of(name)
        if ts is None:
            invalid.append(entry)
            continue
        files.setdefault(ts, []).append(entry)

    runs = {}
    for ts in sorted(set(dirs) | set(files)):
        directory = dirs.get(ts)
        flat = sorted(files.get(ts, []), key=lambda p: p.name)
        new_report = None
        if directory is not None:
            candidate = directory / "report.json"
            if candidate.is_file():
                new_report = candidate
        if new_report is not None:
            runs[ts] = {"dir": directory, "files": flat,
                        "report": new_report, "new": True}
            continue
        old_report = next((p for p in flat if p.name == ts + ".json"), None)
        runs[ts] = {"dir": directory, "files": flat,
                    "report": old_report, "new": False}
    return runs, invalid


def collect_entries(run, ts):
    """(原件, zip 内路径) 清单；zip 内统一成新一代形态、正斜杠。"""
    entries = []
    if run["new"]:
        directory = run["dir"]
        for path in walk_files(directory):
            entries.append((path, path.relative_to(directory).as_posix()))
        # 同一 T 若还残留旧形态散件，一并收进同一个 zip（避免归档后变孤儿）
        for path in run["files"]:
            entries.append((path, path.name))
    else:
        for path in run["files"]:
            if path.name == ts + ".json":
                arc = "report.json"
            elif path.name == ts + ".actual.json":
                arc = "actual.json"
            elif path.name == ts + ".md":
                arc = "report.md"
            else:
                arc = path.name
            entries.append((path, arc))
        if run["dir"] is not None:
            for path in walk_files(run["dir"]):
                rel_in_dir = path.relative_to(run["dir"]).as_posix()
                entries.append((path, "cases/" + rel_in_dir))
    entries.sort(key=lambda item: item[1])
    return entries


def entries_size(entries):
    return sum(path.stat().st_size for path, _arc in entries)


def verify_zip(zip_path, entries, allow_extra=False):
    """zip 可读 + 每个原件都有同名成员且内容一致；否则 VerifyError。"""
    try:
        with zipfile.ZipFile(str(zip_path)) as zf:
            bad = zf.testzip()
            if bad is not None:
                raise VerifyError("zip 成员 CRC 校验失败：%s" % bad)
            names = set(zf.namelist())
            if not allow_extra and len(names) != len(entries):
                raise VerifyError("zip 成员数 %d ≠ 原件数 %d"
                                  % (len(names), len(entries)))
            for path, arc in entries:
                if arc not in names:
                    raise VerifyError("zip 缺成员 %s" % arc)
                try:
                    same = zf.read(arc) == path.read_bytes()
                except OSError as e:
                    raise VerifyError("读不了原件 %s（%s）" % (path, e))
                if not same:
                    raise VerifyError("zip 成员内容与原件不一致：%s" % arc)
    except zipfile.BadZipFile as e:
        raise VerifyError("zip 坏档（%s）：%s" % (zip_path, e))


def write_zip(entries, zip_path):
    """写临时 zip → 校验 → 原子改名到正式路径；任何失败都不留半份 zip。"""
    tmp = zip_path.with_name(zip_path.name + ".tmp")
    try:
        with zipfile.ZipFile(str(tmp), "w", zipfile.ZIP_DEFLATED) as zf:
            for path, arc in entries:
                zf.write(str(path), arc)
        verify_zip(tmp, entries)
        os.replace(str(tmp), str(zip_path))
    finally:
        if tmp.exists():
            try:
                tmp.unlink()
            except OSError:
                pass


def delete_originals(run):
    """删这次报告的原件（顶层文件先、证据目录后）。"""
    for path in run["files"]:
        path.unlink()
    if run["dir"] is not None:
        shutil.rmtree(str(run["dir"]))


def source_files(run, project):
    """归档前用于输出的顶层原件清单（相对项目根）。"""
    out = [rel(path, project) for path in run["files"]]
    if run["dir"] is not None:
        out.append(rel(run["dir"], project) + "/")
    return out


def cmd_gc(project, root, config, config_path, args):
    project = Path(project).resolve()
    report_dir = project / root / REPORT_DIR_NAME
    if not report_dir.is_dir():
        raise EnvError("找不到报告目录 %s：先跑一次 at-run（它会建）"
                       % rel(report_dir, project))
    keep = load_keep(config, config_path, args.keep)
    archive_dir = report_dir / ARCHIVE_DIR_NAME
    runs, invalid = scan_runs(report_dir)
    ordered = sorted(runs)

    newest = ordered[-keep:] if keep > 0 else []
    older = ordered[:len(ordered) - len(newest)] if newest else ordered

    archived = []
    skipped = []
    errors = []
    freed = 0

    for path in invalid:
        skipped.append({"name": rel(path, project), "reason": "invalid-name",
                        "detail": "名字不是 YYYYMMDDTHHMMSS，跳过"})

    now = time.time()
    for ts in older:
        run = runs[ts]
        zip_path = archive_dir / (ts + ".zip")
        zip_rel = rel(zip_path, project)
        form = "dir" if run["new"] else "flat"
        try:
            entries = collect_entries(run, ts)
            total = entries_size(entries)
        except OSError as e:
            errors.append({"ts": ts, "error": "读不了报告文件：%s" % e})
            continue

        if zip_path.exists():
            # 正常重复运行不会走到这里（已归档的 T 已无原件、不在 runs 里）；
            # 只有"校验后删到一半"才会：核对 zip 覆盖剩余原件，把删除做完。
            try:
                verify_zip(zip_path, entries, allow_extra=True)
            except VerifyError as e:
                errors.append({"ts": ts,
                               "error": "已存在的 zip 校验失败，原件保留：%s" % e})
                continue
            if args.dry_run:
                archived.append({"ts": ts, "zip": zip_rel,
                                 "files": source_files(run, project),
                                 "bytes": total, "form": form, "resumed": True})
                freed += total
                continue
            try:
                delete_originals(run)
            except OSError as e:
                errors.append({"ts": ts,
                               "error": "zip 已校验，但删原件失败：%s" % e})
                continue
            freed += total
            archived.append({"ts": ts, "zip": zip_rel,
                             "files": source_files(run, project),
                             "bytes": total, "form": form, "resumed": True})
            continue

        if run["report"] is None:
            skipped.append({"ts": ts, "reason": "in-flight",
                            "detail": "缺主报告 json（新形态 report.json / "
                                      "旧形态 %s.json），可能正在写" % ts})
            continue
        try:
            report_mtime = run["report"].stat().st_mtime
        except OSError as e:
            errors.append({"ts": ts, "error": "读不了主报告：%s" % e})
            continue
        if now - report_mtime < INFLIGHT_SECONDS:
            skipped.append({"ts": ts, "reason": "in-flight",
                            "detail": "主报告 5 分钟内有改动（可能正在写）"})
            continue

        if args.dry_run:
            archived.append({"ts": ts, "zip": zip_rel,
                             "files": source_files(run, project),
                             "bytes": total, "form": form})
            freed += total
            continue

        try:
            archive_dir.mkdir(parents=True, exist_ok=True)
            write_zip(entries, zip_path)
        except (OSError, VerifyError) as e:
            errors.append({"ts": ts, "error": "归档失败，原件保留：%s" % e})
            continue
        try:
            delete_originals(run)
        except OSError as e:
            errors.append({"ts": ts,
                           "error": "zip 已校验，但删原件失败（下次会补删）：%s"
                                    % e})
            continue
        freed += total
        archived.append({"ts": ts, "zip": zip_rel,
                         "files": source_files(run, project),
                         "bytes": total, "form": form})

    payload = {"ok": not errors, "kept": len(newest),
               "archived": archived, "freed_bytes": freed,
               "dry_run": bool(args.dry_run), "keep": keep,
               "skipped": skipped, "errors": errors}
    emit(payload)

    tag = "[dry-run] " if args.dry_run else ""
    print("at-gc: %s报告 %d 个：保留最新 %d 个，%s归档 %d 个，释放 %s（跳过 %d）"
          % (tag, len(ordered), len(newest),
             "计划" if args.dry_run else "", len(archived),
             size_text(freed), len(skipped)), file=sys.stderr)
    for item in archived:
        print("at-gc: %s%s%s → %s（%d 个顶层原件，%s）%s"
              % (tag, item["ts"],
                 " [目录形态]" if item["form"] == "dir" else " [旧形态]",
                 item["zip"], len(item["files"]),
                 size_text(item["bytes"]),
                 "（补删上次未删完的原件）" if item.get("resumed") else ""),
              file=sys.stderr)
    for item in skipped:
        label = item.get("ts") or item.get("name") or "?"
        print("at-gc: 跳过 %s（%s）" % (label, item.get("detail", "")),
              file=sys.stderr)
    for item in errors:
        print("at-gc: 错误 %s —— %s" % (item.get("ts", "?"), item["error"]),
              file=sys.stderr)
    return EXIT_OK if not errors else EXIT_USAGE


def main(argv=None):
    force_utf8_stdio()
    ap = _Parser(prog="at-gc", description=DESCRIPTION, epilog=EPILOG,
                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--dir", default=".", metavar="DIR",
                    help="项目目录（默认当前目录）")
    ap.add_argument("--root", metavar="NAME",
                    help="集成测试根目录名（缺省读 <dir>/atest.json 的 root）")
    ap.add_argument("--keep", type=int, metavar="N",
                    help="保留最近 N 次报告（缺省读 atest.json 的 report_keep，"
                         "再缺省 %d）" % KEEP_DEFAULT)
    ap.add_argument("--dry-run", action="store_true",
                    help="只打印计划：不建 archive/、不写 zip、不删文件")
    args = ap.parse_args(argv)

    project = Path(args.dir).expanduser()
    if not project.exists() or not project.is_dir():
        emit({"ok": False, "error": "项目目录不存在或不是目录：%s" % project})
        return EXIT_ENV

    try:
        config, config_path = load_config(project)
        root = resolve_root(config, config_path, args.root)
        return cmd_gc(project, root, config, config_path, args)
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
