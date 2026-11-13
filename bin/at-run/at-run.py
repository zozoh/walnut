#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
at-run — ATEST 运行器（v2）

一条命令串起 ATEST 的三件事：

    --check                 只校验用例（字段 / 命名 / 四节 / 期望声明一致性），不执行
    （无参数）              发现用例 → 解析实例 → 复用会话 → 逐例执行 → 写 JSON 报告
    --only / --suite        选例执行；--stop-on-fail 遇到红灯即停
    --instance L1|L2        跑哪一级实例（缺省读 atest.json 的 instance）

实例解析（两个轴分开，都不猜）:
    级别:  --instance  →  atest.json 的 instance  →  报错
    端点:  .wn-instances.json（--instances > $WN_INSTANCES > 从项目目录逐级向上
           > 用户目录；先命中先用）→ 报错，并打印搜过的路径清单与模板
    会话:  工作区级会话池（.wn-instances.json 所在目录的 .at/client/session）里的
           <级别>-<测试域>-00；**只复用、不代登录**——缺失或失效就退出 2 并提示登录命令。

配置解析（项目根 atest.json，唯一来源）:
    root:         atest.json 的 root（缺省 atest）→ --root 覆盖。
    instance:     --instance 覆盖 → atest.json 的 instance → 空则退出 2。
    test_domain:  --domain 覆盖 → atest.json 的 test_domain → 空则退出 2。
    init_template: atest.json 的 init_template（缺省 <root>/atest.init；v1 只读取回显，
                   渲染归 ATESET-05）。
    atest.json 缺失 → 退出 2，提示先跑 at-init 或补上配置。

脚本契约（ATESET-03 T1 定稿，见同目录 README）:
    runner 以子进程执行 <root>/scripts/<CASE-ID>.py（argv 传参，绝不经 shell），
    工作目录 = 项目目录，并注入 AT_LEVEL / AT_URL / AT_SESSION_DIR / AT_DOMAIN /
    AT_WNCLI / AT_CASE_ID / AT_OUT。脚本 stdout 的**最后一行**必须是单行 JSON:
        成功 {"ok": true, "actual": {...}}
        失败 {"ok": false, "error": "..."}
    断言不在脚本里做：runner 按用例 md 的 `## 期望` 统一比较（防"实现即预期"）。
    另把 bin/at-run 放进子进程 PYTHONPATH，脚本可 `from atlib import ...`（薄库见
    atlib.py；手工跑脚本需自己设 PYTHONPATH）。

期望 matcher（ATESET-04 T1 定稿的硬接口，三者互斥）:
    {"$ignore": true}                 跳过该键（等价于不写，但显式声明"它不稳定"）
    {"$range": ["$t0", "$t1"]}        实际值须落在 runner 取的毫秒区间内
    {"$regex": "^CASE-\\d+$"}         实际值须为字符串且正则命中（Python re.search）
    $t0 / $t1 由 runner 在用例开始前、结束后各取一次，脚本的本地时钟不参与；
    不认识 / 混用多个 matcher → --check 报 expected-invalid。

报告（JSON，唯一源；Markdown 归 07）:
    <root>/report/<suite>/<timestamp>.json  +  同内容的 latest.json
    {"schema","suite","timestamp","instance","instance_source","total","passed",
     "failed","blocked","cases":[{"caseID","status","duration_ms","evidence_dir",
     "diff","evidence"}]}
    schema = "atest-report/2"（v1 无此键，只增不改）；
    status = passed / failed / blocked（blocked = 未实现或缺基线，不算红）；
    evidence 是相对证据目录的文件清单（summary.txt / objects/… 等）。

退出码: 0 全绿（failed 为 0，blocked 不算红）；1 有红 / 用法错误；
        2 环境不可用（atest.json 或实例登记缺失、端口不通、会话缺失或失效）。

零依赖 Python3；所有写入 UTF-8 无 BOM、LF。
"""

import argparse
import json
import os
import re
import socket
import subprocess
import sys
import time
from datetime import datetime
from pathlib import Path

EXIT_OK = 0
EXIT_USAGE = 1
EXIT_ENV = 2

INSTANCES_NAME = ".wn-instances.json"
CONFIG_NAME = "atest.json"

ROOT_DEFAULT = "atest"
# 集成测试根目录名：不许路径分隔符，也不许 . / ..（否则路径会跑出项目）
ROOT_RE = re.compile(r"^[A-Za-z0-9._-]+$")

REQUIRED_FIELDS = ("caseID", "title", "source", "contract", "expected")
EXPECTED_KINDS = ("inline", "file", "baseline")
SECTIONS = ("前置", "步骤", "期望", "证据")

# 比较时忽略的易变字段（DRAFT §4.4：每次执行都不同），以及以 _ 开头的内部键
IGNORE_KEYS = ("id", "pid", "ct", "lm", "sha1")

# 期望 matcher（ATESET-04 T1 定稿的硬接口）：$ 前缀与普通字段区分，三者互斥
MATCHER_KEYS = ("$ignore", "$range", "$regex")
REPORT_SCHEMA = "atest-report/2"

# contract 只写命令族不算"被测契约"（02 反例错误 3）
GENERIC_CONTRACTS = ("o", "obj", "cmd", "cmd_o", "cmd_obj", "cmd_sqlx", "sqlx",
                     "jsc", "api", "regapi", "cmd_app")

# 步骤里出现这些词 = 把断言写进了步骤（02 反例错误 5）
ASSERT_WORDS = ("检查", "确认", "断言", "判失败", "判为", "不得", "必须",
                "应该", "expect", "assert")

CASE_ID_RE = re.compile(r"^CASE-[A-Z0-9-]{1,32}$")
FIELD_RE = re.compile(r"^([A-Za-z_][A-Za-z0-9_]*)\s*:\s*(.*)$")
H2_RE = re.compile(r"^##\s+(.+?)\s*$")

INSTANCES_TEMPLATE = ('{\n'
                      '  "L1": {"host": "127.0.0.1", "port": 8080},\n'
                      '  "L2": {"host": "127.0.0.1", "port": 8090}\n'
                      '}')

DESCRIPTION = """\
ATEST 运行器：校验用例、解析实例、复用会话、逐例执行并写 JSON 报告。
退出码 0 全绿 / 1 有红（或用法错误） / 2 环境不可用。"""

EPILOG = """\
根目录:
  atest.json 的 root（缺省 atest）→ --root 覆盖；cases / scripts / expect /
  baseline/<实例> / report 都在 <root>/ 下。atest.json 缺失 → 退出 2（先跑 at-init）。
  基线按实例分目录：<root>/baseline/L1|L2/<CASE-ID>.json（L1 对照审核、L2 最终判定）。

配置（项目根 atest.json，唯一来源）:
  root / instance / test_domain / init_template 都从这里读；
  --root / --instance / --domain 可覆盖；instance 为空 → 退出 2（--check 也一样）。

用例发现:
  <dir>/<root>/cases/*.md    文件名 = caseID；脚本 <root>/scripts/<caseID>.py

实例与会话:
  级别  --instance L1|L2  →  atest.json 的 instance  →  报错
  端点  .wn-instances.json（--instances > $WN_INSTANCES > 逐级向上 > 用户目录）
  会话  工作区池 .at/client/session 的 <级别>-<测试域>-00（只复用；缺失请先 wn-cli login）

脚本契约（ATESET-03 T1 定稿）:
  注入 AT_LEVEL / AT_URL / AT_SESSION_DIR / AT_DOMAIN / AT_WNCLI / AT_CASE_ID / AT_OUT
  （另把 bin/at-run 放进子进程 PYTHONPATH，脚本可 from atlib import ...）
  stdout 最后一行: {"ok":true,"actual":{...}} 或 {"ok":false,"error":"..."}
  exit 2 = 环境不可用（记 blocked）；其它非 0 = 记 failed

期望 matcher（ATESET-04 T1 定稿，$ 前缀，三者互斥）:
  {"$ignore": true}            跳过该键（显式声明"它不稳定"，不要求存在）
  {"$range": ["$t0", "$t1"]}   实际值须是数字且落在 runner 取的毫秒区间内
  {"$regex": "..."}            实际值须是字符串且正则命中（Python re.search）
  不认识 / 多 matcher 混用 / 放在期望根上 → --check 报 expected-invalid

报告:
  <root>/report/<suite>/<timestamp>.json 与同内容 latest.json
  schema = "atest-report/2"（03 的 9 个顶层键与每例 5 个键保持不变，只增不改）
  cases[] = {"caseID","status","duration_ms","evidence_dir","diff","evidence"}
  status = passed / failed / blocked（blocked 不算红）
  evidence = 相对证据目录的文件清单（summary.txt / objects/…）

--check 错误码:
  filename-mismatch / caseid-invalid / missing-field / expected-invalid /
  contract-too-generic / missing-section / assertion-in-steps /
  expected-mismatch / expected-invalid-json / volatile-field / caseid-duplicate

示例:
  python bin/at-run/at-run.py --check
  python bin/at-run/at-run.py --check bin/at-init/tmpl/CASE-001.md
  python bin/at-run/at-run.py --instance L1 --suite smoke
  python bin/at-run/at-run.py --instance L1 --only CASE-001,CASE-002
  python bin/at-run/at-run.py --instance L2 --stop-on-fail
  python bin/at-run/at-run.py --root spec --check
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


def load_config(project):
    """读项目根 atest.json（唯一配置源）；缺失/坏档 → 退出 2，不猜。"""
    path = Path(project) / CONFIG_NAME
    if not path.is_file():
        raise EnvError("找不到 %s：先跑 bin/at-init/at-init.py 生成它，"
                       "或在项目根建 atest.json 并填 instance" % path)
    try:
        data = json.loads(path.read_text(encoding="utf-8-sig"))
    except (ValueError, OSError) as e:
        raise EnvError("%s 不是合法 JSON（%s）：请修正后再跑" % (path, e))
    if not isinstance(data, dict):
        raise EnvError("%s 格式不对：顶层应为对象（version / root / instance ...）"
                       % path)
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
        raise EnvError("%s 的 root 不合法：%r（需匹配 %s，且不能是 . 或 ..）"
                       % (config_path, root, ROOT_RE.pattern))
    return root


def rel(path, root):
    try:
        return str(Path(path).resolve().relative_to(Path(root).resolve())
                   ).replace("\\", "/")
    except ValueError:
        return str(path)


def child_env(extra=None):
    """子进程环境：固定 UTF-8（否则中文证据在 Windows 管道里乱码）。"""
    env = os.environ.copy()
    env["PYTHONUTF8"] = "1"
    env["PYTHONIOENCODING"] = "utf-8"
    # 脚本 import atlib 会在 bin/at-run 下生成 __pycache__（未 gitignore）；
    # 用例执行不该弄脏仓库，直接禁用字节码缓存。
    env["PYTHONDONTWRITEBYTECODE"] = "1"
    if extra:
        env.update({k: str(v) for k, v in extra.items()})
    return env


def script_env(extra=None):
    """用例脚本的环境：在 child_env 基础上把本目录放进 PYTHONPATH。

    这样脚本可以 `from atlib import wn, snap, emit, note`，不必自己拼路径、
    也不新增 AT_* 契约变量。手工跑脚本时需自己设 PYTHONPATH=bin/at-run。
    """
    env = child_env(extra)
    here = str(Path(__file__).resolve().parent)
    old = env.get("PYTHONPATH")
    env["PYTHONPATH"] = here + (os.pathsep + old if old else "")
    return env


def clock_text(clock):
    """$t0/$t1 落盘（毫秒 + 本地 ISO），供事后核对区间。"""
    def iso(ms):
        return datetime.fromtimestamp(ms / 1000.0).astimezone() \
            .isoformat(timespec="milliseconds")
    return ("t0_ms=%d\nt0_iso=%s\nt1_ms=%d\nt1_iso=%s\n"
            % (clock["t0"], iso(clock["t0"]),
               clock["t1"], iso(clock["t1"])))


# ---------------------------------------------------------------- 用例解析

def split_front_matter(text):
    """切出 (front-matter, 正文)；没有 front-matter 时返回 (None, 原文)。"""
    lines = text.splitlines()
    if not lines or lines[0].strip() != "---":
        return None, text
    for i in range(1, len(lines)):
        if lines[i].strip() == "---":
            return "\n".join(lines[1:i]), "\n".join(lines[i + 1:])
    return None, text


def parse_front_matter(fm):
    """只认 `key: value`；值一律转成字符串（YAML 的日期类型会被 str 归一，02 的 F2）。"""
    fields = {}
    if fm is None:
        return fields
    for raw in fm.splitlines():
        s = raw.strip()
        if not s or s.startswith("#"):
            continue
        m = FIELD_RE.match(s)
        if not m:
            continue
        value = m.group(2).strip()
        if len(value) >= 2 and value[0] == value[-1] and value[0] in "\"'":
            value = value[1:-1]
        fields[m.group(1)] = value
    return fields


def split_sections(body):
    """按固定四节切正文；只认这四个 `##` 标题。"""
    sections = {}
    current = None
    buf = []
    for raw in body.splitlines():
        m = H2_RE.match(raw)
        if m:
            if current is not None:
                sections[current] = "\n".join(buf)
            name = m.group(1).strip()
            current = name if name in SECTIONS else None
            buf = []
            continue
        if current is not None:
            buf.append(raw)
    if current is not None:
        sections[current] = "\n".join(buf)
    return sections


def json_blocks(text):
    """取出 ```json 围栏块的内容（只要围栏语言是 json）。"""
    blocks = []
    lines = text.splitlines()
    i = 0
    while i < len(lines):
        if lines[i].strip().lower() in ("```json", "``` json"):
            i += 1
            buf = []
            while i < len(lines) and lines[i].strip() != "```":
                buf.append(lines[i])
                i += 1
            blocks.append("\n".join(buf))
            i += 1
        else:
            i += 1
    return blocks


def has_matcher_key(value):
    """是否是 matcher 对象（含 $ 前缀键的 dict）。"""
    return isinstance(value, dict) and any(
        isinstance(k, str) and k.startswith("$") for k in value)


def matcher_problem(value):
    """一个 matcher 对象的合法性问题；合法返回 None。

    调用前提：has_matcher_key(value) 为真。规则只三条，宁缺毋滥：
    $ignore 的值必须 true、$range 必须两个元素（$t0/$t1 或数字）、
    $regex 必须是能编译的字符串；matcher 对象只能恰好一个键。
    """
    if len(value) != 1:
        return ("matcher 必须恰好一个键（现有 %s）：不能和普通字段混用"
                % " / ".join(sorted(str(k) for k in value)))
    key = next(iter(value))
    arg = value[key]
    if key not in MATCHER_KEYS:
        return ("不认识的 matcher %s（只支持 %s）"
                % (key, " / ".join(MATCHER_KEYS)))
    if key == "$ignore":
        if arg is not True:
            return "「$ignore」的值只能是 true（现在是 %r）" % (arg,)
        return None
    if key == "$range":
        if not isinstance(arg, list) or len(arg) != 2:
            return ('「$range」的值必须是两个元素的数组，如 ["$t0", "$t1"]')
        for i, bound in enumerate(arg):
            if bound in ("$t0", "$t1"):
                continue
            if isinstance(bound, bool) or not isinstance(bound, (int, float)):
                return ('「$range」第 %d 个元素 %r 不合法：只能是 "$t0" / '
                        '"$t1" 或数字' % (i + 1, bound))
        return None
    if not isinstance(arg, str):
        return "「$regex」的值必须是字符串（现在是 %r）" % (arg,)
    try:
        re.compile(arg)
    except re.error as e:
        return "「$regex」不是合法正则：%s（%s）" % (arg, e)
    return None


def matcher_problems(value, path=""):
    """递归收集期望里 matcher 的问题；返回 [(路径, 说明)]。"""
    out = []
    if has_matcher_key(value):
        problem = matcher_problem(value)
        if problem:
            out.append((path or "$", problem))
        return out
    if isinstance(value, dict):
        for key, val in value.items():
            child = "%s.%s" % (path, key) if path else str(key)
            out.extend(matcher_problems(val, child))
    elif isinstance(value, list):
        for i, val in enumerate(value):
            out.extend(matcher_problems(val, "%s[%d]" % (path, i)))
    return out


def expected_matcher_errors(data):
    """期望值的 matcher 校验；返回可直接进 --check 的说明列表。"""
    if has_matcher_key(data):
        return ["matcher 不能作为期望的根对象：否则整例会无条件跳过/匹配，"
                "matcher 只能放在某个字段的值上"]
    return ["路径 %s 的 matcher 不合法：%s" % (where, problem)
            for where, problem in matcher_problems(data)]


def volatile_paths(value, path=""):
    """递归找易变字段（id/pid/ct/lm/sha1）的路径。

    显式写了 matcher 的不算"写死"（如 ct: {"$range": ["$t0","$t1"]}）。
    """
    out = []
    if isinstance(value, dict):
        for key, val in value.items():
            child = "%s.%s" % (path, key) if path else str(key)
            if key in IGNORE_KEYS and not has_matcher_key(val):
                out.append(child)
            out.extend(volatile_paths(val, child))
    elif isinstance(value, list):
        for i, val in enumerate(value):
            out.extend(volatile_paths(val, "%s[%d]" % (path, i)))
    return out


def err_of(path, case_id, code, message):
    return {"file": str(path), "caseID": case_id, "code": code,
            "message": message}


def check_case(path, text, root=ROOT_DEFAULT, project=None, level=None):
    """校验一个用例文件；返回 (fields, sections, errors)。root 只用于路径文案。"""
    errors = []
    fm, body = split_front_matter(text)
    if fm is None:
        errors.append(err_of(path, "", "front-matter-missing",
                             "缺少 front-matter：文件要以 --- 开头、以 --- 收尾"))
    fields = parse_front_matter(fm)
    case_id = fields.get("caseID", "")

    if case_id and path.stem != case_id:
        errors.append(err_of(path, case_id, "filename-mismatch",
                             "文件名 %s 与 caseID %s 不一致（报告/脚本/基线都按 "
                             "CASE-ID 对账）" % (path.name, case_id)))
    if case_id and not CASE_ID_RE.match(case_id):
        errors.append(err_of(path, case_id, "caseid-invalid",
                             "caseID %r 不匹配 %s"
                             % (case_id, CASE_ID_RE.pattern)))
    for name in REQUIRED_FIELDS:
        if not fields.get(name):
            errors.append(err_of(path, case_id, "missing-field",
                                 "缺少必填字段 %s" % name))

    kind = fields.get("expected", "")
    if kind and kind not in EXPECTED_KINDS:
        errors.append(err_of(path, case_id, "expected-invalid",
                             "expected 只能是 %s，现在是 %r"
                             % (" / ".join(EXPECTED_KINDS), kind)))
    contract = fields.get("contract", "").strip()
    if contract and contract.lower() in GENERIC_CONTRACTS:
        errors.append(err_of(path, case_id, "contract-too-generic",
                             "contract %r 过泛：要具体到子命令/端点"
                             "（如 cmd_o @create）" % contract))

    sections = split_sections(body)
    for name in SECTIONS:
        if name not in sections:
            errors.append(err_of(path, case_id, "missing-section",
                                 "缺 `## %s` 节（正文四节缺一 = 用例不完整）"
                                 % name))

    steps = sections.get("步骤", "")
    hits = [w for w in ASSERT_WORDS if w in steps]
    if hits:
        errors.append(err_of(path, case_id, "assertion-in-steps",
                             "`## 步骤` 里出现断言词 %s：步骤只写动作，"
                             "断言一律进 `## 期望`" % " / ".join(hits)))

    expected_section = sections.get("期望")
    if expected_section is not None and kind in EXPECTED_KINDS:
        blocks = json_blocks(expected_section)
        data = None
        if kind == "inline":
            if len(blocks) != 1:
                errors.append(err_of(path, case_id, "expected-mismatch",
                                     "expected: inline 要求 `## 期望` 里恰好一个 "
                                     "```json 块（现有 %d 个）" % len(blocks)))
            else:
                try:
                    data = json.loads(blocks[0])
                except ValueError as e:
                    errors.append(err_of(path, case_id,
                                         "expected-invalid-json",
                                         "`## 期望` 的 json 块解析失败：%s" % e))
        elif kind == "file":
            if blocks:
                errors.append(err_of(path, case_id, "expected-mismatch",
                                     "expected: file 却贴了内联 json 块：期望值应放 "
                                     "%s/expect/%s.json" % (root, case_id)))
            elif case_id and ("%s/expect/%s.json" % (root, case_id)) \
                    not in expected_section:
                errors.append(err_of(path, case_id, "expected-mismatch",
                                     "expected: file 但 `## 期望` 没写 "
                                     "%s/expect/%s.json 路径" % (root, case_id)))
            # 期望文件存在就顺手校验（含 matcher）；缺失留给执行时记 blocked
            expect_path = (Path(project or ".") / root / "expect"
                           / ("%s.json" % case_id))
            if case_id and expect_path.is_file():
                try:
                    data = json.loads(expect_path.read_text(encoding="utf-8-sig"))
                except (ValueError, OSError) as e:
                    errors.append(err_of(path, case_id, "expected-invalid-json",
                                         "%s/expect/%s.json 解析失败：%s"
                                         % (root, case_id, e)))
        elif kind == "baseline" and blocks:
            errors.append(err_of(path, case_id, "expected-mismatch",
                                 "expected: baseline 却贴了内联 json 块：基线的值只能"
                                 "来自 %s/baseline/%s/%s.json（按实例分目录，"
                                 "审核者写）"
                                 % (root, level or "<instance>", case_id)))

        if data is None and blocks and kind != "inline":
            # 声明与内容矛盾已单独报错；这里只为易变字段检查试着解析
            try:
                data = json.loads(blocks[0])
            except ValueError:
                data = None
        if data is not None:
            for message in expected_matcher_errors(data):
                errors.append(err_of(path, case_id, "expected-invalid", message))
        # 易变字段只查内联期望（03 的既有行为）；期望文件/基线不在 --check 展开
        if kind == "inline" and data is not None:
            volatiles = volatile_paths(data)
            if volatiles:
                errors.append(err_of(path, case_id, "volatile-field",
                                     "`## 期望` 写死了易变字段 %s：id/pid/ct/lm/"
                                     "sha1 每次执行都不同，必然红"
                                     % ", ".join(volatiles)))
    return fields, sections, errors


def collect_cases(paths, project, root=ROOT_DEFAULT):
    """按 --check 的 paths（或 <dir>/<root>/cases）收集用例文件。"""
    targets = []
    for raw in paths:
        p = Path(raw)
        if not p.is_absolute():
            p = Path.cwd() / p
        if p.is_dir():
            targets.extend(sorted(x for x in p.glob("*.md") if x.is_file()))
        elif p.is_file():
            targets.append(p)
        else:
            raise UsageError("用例路径不存在：%s" % raw)
    if paths:
        return targets
    cases_dir = Path(project) / root / "cases"
    if not cases_dir.is_dir():
        raise EnvError("找不到用例目录 %s：先跑 bin/at-init/at-init.py" % cases_dir)
    return sorted(x for x in cases_dir.glob("*.md") if x.is_file())


def run_check(project, paths, root=ROOT_DEFAULT, level=None):
    targets = collect_cases(paths, project, root)
    errors = []
    seen = {}
    checked = []
    for path in targets:
        try:
            text = read_text(path)
        except EnvError as e:
            errors.append(err_of(path, "", "read-failed", str(e)))
            continue
        fields, _sections, errs = check_case(path, text, root, project, level)
        errors.extend(errs)
        case_id = fields.get("caseID", "")
        checked.append(str(path))
        if case_id:
            if case_id in seen:
                errors.append(err_of(path, case_id, "caseid-duplicate",
                                     "caseID %s 重复（另一个文件 %s）"
                                     % (case_id, seen[case_id])))
            else:
                seen[case_id] = str(path)
    ok = not errors
    emit({"ok": ok, "check": True, "root": root, "checked": len(checked),
          "files": checked, "error_count": len(errors), "errors": errors})
    if not ok:
        print("at-run --check：%d 个用例文件，%d 条错误（修完再跑；"
              "错误码含义见 --help）" % (len(checked), len(errors)),
              file=sys.stderr)
    return EXIT_OK if ok else EXIT_USAGE


# ---------------------------------------------------------------- 实例与会话

def find_instances_file(project, explicit):
    """--instances > $WN_INSTANCES > 逐级向上 > 用户目录；返回 (文件|None, 搜过的路径)。

    显式给出的路径（--instances / $WN_INSTANCES）必须存在：静默回退到别的
    登记文件会跑错实例（L1/L2 混跑正是要防的事）。
    """
    explicit_paths = []
    if explicit:
        explicit_paths.append(("--instances", Path(explicit)))
    env = os.environ.get("WN_INSTANCES")
    if env:
        explicit_paths.append(("$WN_INSTANCES", Path(env)))
    for label, path in explicit_paths:
        if not path.is_file():
            raise EnvError("%s 指定的实例登记文件不存在：%s" % (label, path))

    candidates = [path for _label, path in explicit_paths]
    here = Path(project).resolve()
    candidates.extend([d / INSTANCES_NAME for d in [here] + list(here.parents)])
    candidates.append(Path.home() / INSTANCES_NAME)
    searched = []
    for f in candidates:
        searched.append(str(f))
        if f.is_file():
            return f, searched
    return None, searched


def load_instances(project, explicit):
    found, searched = find_instances_file(project, explicit)
    if found is None:
        raise EnvError("找不到实例登记 %s（从 %s 逐级向上找过）：\n%s\n"
                       "请开发者创建（本工具不代写）：\n%s"
                       % (INSTANCES_NAME, project, "\n".join(searched),
                          INSTANCES_TEMPLATE))
    try:
        data = json.loads(found.read_text(encoding="utf-8-sig"))
    except (ValueError, OSError) as e:
        raise EnvError("实例登记文件不是合法 JSON：%s（%s）" % (found, e))
    if not isinstance(data, dict):
        raise EnvError("实例登记格式不对（应为 {级别: {host, port}}）：%s" % found)
    return data, found, searched


def endpoint_of(level, instances):
    ep = instances.get(level)
    if not isinstance(ep, dict) or not ep.get("port"):
        return None
    try:
        port = int(ep["port"])
    except (TypeError, ValueError):
        return None
    host = str(ep.get("host") or "127.0.0.1")
    return host, port


def resolve_session_dir(explicit, instances_file, project):
    """--session-dir > $WN_SESSION_DIR > 工作区级（登记文件所在目录）> 项目级。"""
    if explicit:
        return Path(explicit).resolve()
    env = os.environ.get("WN_SESSION_DIR")
    if env:
        return Path(env).resolve()
    if instances_file is not None:
        return (Path(instances_file).parent / ".at" / "client" / "session"
                ).resolve()
    return (Path(project) / ".at" / "client" / "session").resolve()


def session_base(path):
    """会话 TOML 里的 base（wn-cli 用 toml_escape 写成带引号的字符串）。"""
    try:
        text = read_text(path)
    except EnvError:
        return ""
    m = re.search(r'^base\s*=\s*"([^"]*)"', text, re.M)
    return m.group(1) if m else ""


def port_open(host, port, timeout=3.0):
    try:
        with socket.create_connection((host, int(port)), timeout=timeout):
            return True
    except OSError:
        return False


def find_wncli():
    """本脚本旁边的 ../wn-cli/wn-cli.py（bin/ 工具同仓，不复制）。"""
    cand = Path(__file__).resolve().parent.parent / "wn-cli" / "wn-cli.py"
    return cand if cand.is_file() else None


def run_wncli(wncli, argv, cwd, timeout=60.0):
    cmd = [sys.executable, str(wncli)] + [str(x) for x in argv]
    proc = subprocess.run(cmd, cwd=str(cwd), env=child_env(),
                          capture_output=True, timeout=timeout)
    return (proc.returncode,
            proc.stdout.decode("utf-8", "replace"),
            proc.stderr.decode("utf-8", "replace"))


def ensure_session(wncli, level, domain, session_dir, url, project):
    """复用工作区池会话 <级别>-<测试域>-00；缺失/失效/指错端点一律退出 2。"""
    se = "%s-%s-00" % (level, domain)
    sess_file = session_dir / (se + ".toml")
    if not sess_file.is_file():
        raise EnvError("会话池里没有 %s：先登录一次——\n"
                       "  python bin/wn-cli/wn-cli.py --level %s login "
                       "--usr <登录名> --passwd <密码>\n（会话池：%s）"
                       % (se, level, session_dir))
    base = session_base(sess_file)
    if base and base.rstrip("/") != url.rstrip("/"):
        raise EnvError("会话 %s 指向 %s，与本次端点 %s 不一致：拒绝复用"
                       "（防 L1/L2 混跑）。确认后用 wn-cli 重新登录"
                       % (se, base, url))
    try:
        rc, out, err = run_wncli(
            wncli,
            ["--level", level, "--se", se, "--session-dir", str(session_dir),
             "--url", url, "cmd", "o ~ @json -cqn"],
            project)
    except subprocess.TimeoutExpired:
        raise EnvError("会话探活超时（wn-cli 60s 未返回）：实例可能挂了"
                       "（L1 上挂了断点也会这样），请开发者确认")
    if rc != 0:
        detail = (err.strip() or out.strip() or "").replace("\n", " ")[:500]
        raise EnvError("会话 %s 不可用（wn-cli 退出码 %d）：%s\n"
                       "失效会话已被 wn-cli 当场删除；请重新 login"
                       % (se, rc, detail))
    return se


# ---------------------------------------------------------------- 期望与比较

def read_json_file(path):
    try:
        return json.loads(Path(path).read_text(encoding="utf-8-sig"))
    except (ValueError, OSError) as e:
        raise EnvError("期望文件不是合法 JSON：%s（%s）" % (path, e))


def load_expected(project, case, kind, root=ROOT_DEFAULT, level=None):
    """按 expected 声明取期望值；返回 (值, 缺失原因)。缺基线/期望文件 = blocked。

    baseline **按实例分目录**：<root>/baseline/<实例>/<CASE-ID>.json
    （L1 基线用于对照审核，L2 基线是最终判定；级别不同期望值可能不同，
    所以不共用一份文件）。
    """
    case_id = case["caseID"]
    if kind == "inline":
        return case["expected_value"], None
    if kind == "file":
        path = Path(project) / root / "expect" / ("%s.json" % case_id)
        if not path.is_file():
            return None, "期望文件缺失：%s（expected: file）" % rel(path, project)
        return read_json_file(path), None
    instance = level or "<instance>"
    path = Path(project) / root / "baseline" / instance / ("%s.json" % case_id)
    if not path.is_file():
        return None, ("基线缺失：%s（expected: baseline，按实例分目录；"
                      "L1 基线用于对照审核、L2 基线为最终判定，由审核者建立，08）"
                      % rel(path, project))
    return read_json_file(path), None


def diff_of(path, expected, actual):
    return {"path": path or "$", "expected": expected, "actual": actual}


def compare_matcher(expected, actual, clock, path):
    """应用一个 matcher；返回 diff 列表（空 = 通过）。"""
    key = next(iter(expected))
    arg = expected[key]
    if key == "$ignore":
        # 显式声明"这个键不稳定"：不要求存在，也不比较
        return []
    if key == "$range":
        if clock is None:
            return [diff_of(path, expected, "（runner 未提供 $t0/$t1）")]
        bounds = []
        for bound in arg:
            bounds.append(clock[bound[1:]]
                          if bound in ("$t0", "$t1") else bound)
        shown = {"$range": bounds}
        if isinstance(actual, bool) or not isinstance(actual, (int, float)):
            return [diff_of(path, shown, actual)]
        if bounds[0] <= actual <= bounds[1]:
            return []
        return [diff_of(path, shown, actual)]
    # $regex
    if not isinstance(actual, str):
        return [diff_of(path, expected, actual)]
    if re.search(arg, actual):
        return []
    return [diff_of(path, expected, actual)]


def compare(expected, actual, clock=None, path=""):
    """子集比较：只比 expected 里出现的键；数组按顺序、等长；忽略易变键。

    expected 里的 matcher 对象（$ignore/$range/$regex）按 T1 语义处理；
    $t0/$t1 来自 clock（runner 在用例前后取的毫秒时间戳）。
    """
    diffs = []
    if has_matcher_key(expected):
        return compare_matcher(expected, actual, clock, path)
    if isinstance(expected, dict):
        if not isinstance(actual, dict):
            return [diff_of(path, expected, actual)]
        for key, value in expected.items():
            name = str(key)
            child = "%s.%s" % (path, name) if path else name
            matcher = has_matcher_key(value)
            # 易变键默认忽略；但作者显式写了 matcher 时以显式为准
            # （T1 的示例就是 ct: {"$range": ["$t0","$t1"]}）
            if not matcher and (name in IGNORE_KEYS or name.startswith("_")):
                continue
            if name not in actual:
                if matcher and next(iter(value)) == "$ignore":
                    continue
                diffs.append(diff_of(child, value, None))
            else:
                diffs.extend(compare(value, actual[name], clock, child))
        return diffs
    if isinstance(expected, list):
        if not isinstance(actual, list):
            return [diff_of(path, expected, actual)]
        if len(expected) != len(actual):
            diffs.append(diff_of(path, "len=%d" % len(expected),
                                 "len=%d" % len(actual)))
        for i in range(min(len(expected), len(actual))):
            diffs.extend(compare(expected[i], actual[i], clock,
                                 "%s[%d]" % (path, i)))
        return diffs
    if expected != actual:
        diffs.append(diff_of(path, expected, actual))
    return diffs


# ---------------------------------------------------------------- 执行

def last_json_line(stdout):
    """取 stdout 最后一行 JSON；返回 (对象, 错误说明)。"""
    lines = [x for x in stdout.splitlines() if x.strip()]
    if not lines:
        return None, "stdout 为空：最后一行必须是结果 JSON"
    last = lines[-1].strip()
    try:
        return json.loads(last), None
    except ValueError:
        return None, "stdout 最后一行不是 JSON：%r" % last[:200]


def judge(returncode, stdout, expected, evidence, clock=None):
    """按脚本契约判定一例；返回 (status, diff, note)。"""
    result, problem = last_json_line(stdout)
    if problem:
        return "failed", [], problem
    write_json(Path(evidence) / "result.json", result)
    if not isinstance(result, dict):
        return "failed", [], "结果 JSON 不是对象"
    ok = result.get("ok")
    message = result.get("error") or result.get("msg") or ""
    if ok is False:
        if returncode == 2:
            return "blocked", [], ("脚本报环境不可用（exit 2）：%s"
                                   % (message or "（无 error 字段）"))
        return "failed", [], ("脚本报失败：%s"
                              % (message or "（无 error 字段）"))
    if ok is not True:
        return "failed", [], "结果 JSON 缺 ok 布尔字段"
    if returncode != 0:
        return "failed", [], ("脚本退出码 %d，但结果 JSON 声明 ok=true（矛盾）"
                              % returncode)
    if "actual" not in result:
        return "failed", [], "结果 JSON 声明 ok=true 但缺 actual 字段"
    diffs = compare(expected, result["actual"], clock)
    if diffs:
        return "failed", diffs, "与期望不一致（%d 处）" % len(diffs)
    return "passed", [], ""


# ---------------------------------------------------------------- 证据包

def evidence_files(evidence):
    """证据目录下的全部文件（相对路径、正斜杠、排序稳定）→ 报告的 evidence 清单。"""
    root = Path(evidence)
    out = []
    for path in root.rglob("*"):
        if path.is_file():
            out.append(str(path.relative_to(root)).replace("\\", "/"))
    return sorted(out)


def show(value, limit=200):
    """diff 里的人读形式：JSON 值，过长的截断（不改变报告里的原值）。"""
    text = json.dumps(value, ensure_ascii=False, sort_keys=False)
    return text if len(text) <= limit else text[:limit] + " …"


def script_notes(evidence):
    """脚本用 atlib.note() 写的行（[script] 前缀）；runner 重写 summary 时保留。"""
    path = Path(evidence) / "summary.txt"
    if not path.is_file():
        return []
    try:
        return [line for line in read_text(path).splitlines() if line.strip()]
    except EnvError:
        return []


def write_summary(evidence, case_id, status, duration, diff, note, repro,
                  clock, level, notes):
    """写人一眼看懂为什么红/绿的一句话总结（报告 evidence 清单会指向它）。"""
    lines = []
    if status == "passed":
        lines.append("%s passed —— 与期望一致（%dms）" % (case_id, duration))
    elif status == "failed":
        lines.append("%s failed —— %s" % (case_id, note or "判定失败"))
    else:
        lines.append("%s blocked —— %s" % (case_id, note or "未执行"))
    if clock is not None:
        lines.append("时间区间: $t0=%d / $t1=%d（毫秒，runner 取）"
                     % (clock["t0"], clock["t1"]))
    if diff:
        lines.append("")
        lines.append("diff（前 3 处，共 %d 处）:" % len(diff))
        for i, item in enumerate(diff[:3]):
            lines.append("  %d) %s: 期望 %s ≠ 实际 %s"
                         % (i + 1, item.get("path", "$"),
                            show(item.get("expected")),
                            show(item.get("actual"))))
    lines.append("")
    lines.append("复现: %s" % repro)
    if level == "L1":
        lines.append("服务端日志: 不可用（L1：日志在 IDE 控制台，只有 HTTP 侧证据与"
                     "对象快照）")
    else:
        lines.append("服务端日志: 见实例日志目录（L2 抓取由后续任务补，不阻塞本任务）")
    if notes:
        lines.append("")
        lines.append("脚本记录:")
        lines.extend(notes)
    write_text(Path(evidence) / "summary.txt", "\n".join(lines) + "\n")


def pick_suite(args, selected):
    suites = split_list(args.suite)
    if len(suites) == 1:
        return suites[0]
    if len(suites) > 1:
        return "default"
    names = {(c["fields"].get("suite") or "default") for c in selected}
    if len(names) == 1:
        return names.pop()
    return "default"


def split_list(values):
    out = []
    for value in values or []:
        for part in str(value).split(","):
            part = part.strip()
            if part:
                out.append(part)
    return out


def unique_run_id(report_base, started):
    """时间戳目录/文件名，同一秒重复跑时加序号，不覆盖历史报告。"""
    stamp = started.strftime("%Y%m%dT%H%M%S")
    candidate = stamp
    n = 2
    while (report_base / (candidate + ".json")).exists() \
            or (report_base / candidate).exists():
        candidate = "%s-%d" % (stamp, n)
        n += 1
    return candidate


def run_suite(project, args, root, config, config_path):
    project = Path(project).resolve()

    level = args.instance or config.get("instance")
    if not level:
        raise EnvError("实例级别未定：在 %s 里填 instance（如 L1 / L2），"
                       "或用 --instance 覆盖" % config_path)
    if level not in ("L1", "L2"):
        raise EnvError("instance 只能是 L1 / L2，现在是 %r" % level)
    instance_source = "cli" if args.instance else str(config_path)

    domain = args.domain or config.get("test_domain")
    if not domain:
        raise EnvError("测试域未定：在 %s 里填 test_domain，或用 --domain 覆盖"
                       % config_path)

    init_template = config.get("init_template") or ("%s/atest.init" % root)

    instances, inst_file, _searched = load_instances(project, args.instances)
    endpoint = endpoint_of(level, instances)
    if endpoint is None:
        raise EnvError("实例登记 %s 里没有 %s 的 host+port（现有级别：%s）"
                       % (inst_file, level, ", ".join(sorted(instances)) or "无"))
    host, port = endpoint
    url = "http://%s:%d" % (host, port)
    session_dir = resolve_session_dir(args.session_dir, inst_file, project)

    wncli = find_wncli()
    if wncli is None:
        raise EnvError("找不到 wn-cli.py（应为 %s）"
                       % (Path(__file__).resolve().parent.parent
                          / "wn-cli" / "wn-cli.py"))

    cases_dir = project / root / "cases"
    if not cases_dir.is_dir():
        raise EnvError("找不到用例目录 %s：先跑 bin/at-init/at-init.py"
                       % cases_dir)
    files = sorted(x for x in cases_dir.glob("*.md") if x.is_file())

    parsed = []
    for path in files:
        fields, sections, errors = check_case(path, read_text(path), root,
                                              project, level)
        expected_value = None
        if fields.get("expected") == "inline":
            blocks = json_blocks(sections.get("期望", ""))
            if len(blocks) == 1:
                try:
                    expected_value = json.loads(blocks[0])
                except ValueError:
                    expected_value = None
        parsed.append({"file": path, "caseID": fields.get("caseID", ""),
                       "fields": fields, "sections": sections,
                       "errors": errors, "expected_value": expected_value})
    if not parsed:
        raise EnvError("%s 下没有用例（*.md）：先照 bin/at-init/tmpl/CASE-001.md "
                       "写一个" % rel(cases_dir, project))

    only = set(split_list(args.only))
    suites = set(split_list(args.suite))

    def match(case):
        case_id = case["fields"].get("caseID", "")
        if only and case_id not in only:
            return False
        if suites and (case["fields"].get("suite") or "default") not in suites:
            return False
        return True

    selected = [c for c in parsed if match(c)]
    if only:
        known = {c["fields"].get("caseID", "") for c in parsed}
        unknown = sorted(only - known)
        if unknown:
            raise UsageError("--only 指定了不存在的用例：%s（现有：%s）"
                             % (", ".join(unknown),
                                ", ".join(sorted(x for x in known if x)) or "无"))
    if not selected:
        raise UsageError("筛选条件没有命中任何用例（--only / --suite 太窄）")

    broken = [c for c in selected if c["errors"]]
    if broken:
        emit({"ok": False,
              "error": "用例格式错误，未执行（先跑 at-run --check 修掉）",
              "cases": [{"file": str(c["file"]),
                         "errors": c["errors"]} for c in broken]})
        return EXIT_USAGE

    started = datetime.now().astimezone()
    report_base = project / root / "report" / pick_suite(args, selected)
    run_id = unique_run_id(report_base, started)
    run_dir = report_base / run_id
    report_path = report_base / (run_id + ".json")
    latest_path = report_base / "latest.json"

    if not port_open(host, port):
        raise EnvError("实例 %s 端点 %s 连不上（探活 3s 超时）：智能体不启动实例，"
                       "请开发者确认实例状态" % (level, url))
    se = ensure_session(wncli, level, domain, session_dir, url, project)

    results = []
    not_run = []
    stopped = False
    for case in selected:
        case_id = case["fields"]["caseID"]
        if stopped:
            not_run.append(case_id)
            continue
        evidence = run_dir / case_id
        evidence.mkdir(parents=True, exist_ok=True)
        entry = {"caseID": case_id, "status": "blocked", "duration_ms": 0,
                 "evidence_dir": str(evidence.resolve()), "diff": [],
                 "evidence": []}
        note = ""
        repro = ('python "%s" --dir "%s" --instance %s --domain %s --only %s'
                 % (Path(__file__).resolve(), project, level, domain, case_id))

        case_instance = case["fields"].get("instance")
        script = project / root / "scripts" / ("%s.py" % case_id)
        expected = None
        if case_instance and case_instance != level:
            note = ("该例声明 instance=%s，与本次运行 %s 不一致：单独用 "
                    "--instance %s 跑" % (case_instance, level, case_instance))
        elif not script.is_file():
            note = "脚本未实现：缺 %s" % rel(script, project)
        else:
            try:
                expected, missing = load_expected(
                    project, case, case["fields"].get("expected", ""), root,
                    level)
            except EnvError as e:
                # 坏的期望文件/基线是项目资产问题：记 blocked 并继续跑其余用例，
                # 不整体中断（失败隔离），也不把它伪装成红/绿。
                missing = "期望值读取失败：%s" % e
            if missing:
                note = missing
        if note:
            write_text(evidence / "note.txt", note + "\n")
            write_summary(evidence, case_id, "blocked", 0, [], note, repro,
                          None, level, [])
            entry["evidence"] = evidence_files(evidence)
            results.append(entry)
            print("at-run: %s blocked —— %s" % (case_id, note),
                  file=sys.stderr)
            continue

        env_extra = {"AT_LEVEL": level, "AT_URL": url,
                     "AT_SESSION_DIR": str(session_dir),
                     "AT_DOMAIN": domain, "AT_WNCLI": str(wncli),
                     "AT_CASE_ID": case_id, "AT_OUT": str(evidence)}
        write_text(evidence / "cmd.txt",
                   "\n".join([sys.executable, str(script)]) + "\n")
        write_text(evidence / "env.txt",
                   "\n".join("%s=%s" % (k, env_extra[k]) for k in sorted(env_extra))
                   + "\n")
        # $t0 / $t1：runner 在用例开始前、结束后各取一次（脚本的本地时钟不参与）
        t0 = int(time.time() * 1000)
        t1 = t0
        try:
            proc = subprocess.run([sys.executable, str(script)],
                                  cwd=str(project), env=script_env(env_extra),
                                  capture_output=True, timeout=args.timeout)
            t1 = int(time.time() * 1000)
            duration = t1 - t0
            stdout = proc.stdout.decode("utf-8", "replace")
            stderr = proc.stderr.decode("utf-8", "replace")
            write_text(evidence / "stdout.txt", stdout)
            write_text(evidence / "stderr.txt", stderr)
            status, diff, note = judge(proc.returncode, stdout, expected,
                                       evidence, {"t0": t0, "t1": t1})
            entry["status"] = status
            entry["duration_ms"] = duration
            entry["diff"] = diff
        except subprocess.TimeoutExpired as e:
            t1 = int(time.time() * 1000)
            duration = t1 - t0
            partial = (e.stdout or b"").decode("utf-8", "replace")
            write_text(evidence / "stdout.txt", partial)
            entry["status"] = "failed"
            entry["duration_ms"] = duration
            note = "超时（%.0fs）已被终止" % args.timeout
        except OSError as e:
            t1 = int(time.time() * 1000)
            entry["status"] = "failed"
            note = "脚本启动失败：%s" % e
        clock = {"t0": t0, "t1": t1}
        write_text(evidence / "clock.txt", clock_text(clock))
        if note:
            write_text(evidence / "note.txt", note + "\n")
        write_summary(evidence, case_id, entry["status"], entry["duration_ms"],
                      entry["diff"], note, repro, clock, level,
                      script_notes(evidence))
        entry["evidence"] = evidence_files(evidence)
        results.append(entry)
        line = "at-run: %s %s（%dms）" % (case_id, entry["status"],
                                         entry["duration_ms"])
        if note:
            line += " —— " + note
        print(line, file=sys.stderr)
        if args.stop_on_fail and entry["status"] == "failed":
            stopped = True
            print("at-run: --stop-on-fail 命中，剩余用例不再执行", file=sys.stderr)

    passed = sum(1 for r in results if r["status"] == "passed")
    failed = sum(1 for r in results if r["status"] == "failed")
    blocked = sum(1 for r in results if r["status"] == "blocked")
    report = {"schema": REPORT_SCHEMA,
              "suite": report_base.name,
              "timestamp": started.isoformat(timespec="seconds"),
              "instance": level,
              "instance_source": instance_source,
              "total": len(results),
              "passed": passed,
              "failed": failed,
              "blocked": blocked,
              "cases": results}
    write_json(report_path, report)
    write_json(latest_path, report)

    exit_code = EXIT_OK if failed == 0 else EXIT_USAGE
    emit({"ok": failed == 0, "suite": report_base.name, "root": root,
          "instance": level, "test_domain": domain,
          "init_template": init_template,
          "instance_source": instance_source, "session": se,
          "total": len(results), "passed": passed, "failed": failed,
          "blocked": blocked, "skipped": len(not_run),
          "report": str(report_path.resolve()),
          "latest": str(latest_path.resolve()), "exit": exit_code})
    print("at-run: %s %s —— %d 例：%d 绿 / %d 红 / %d blocked%s（报告 %s）"
          % (report_base.name, level, len(results), passed, failed, blocked,
             "，%d 例未执行" % len(not_run) if not_run else "",
             rel(report_path, project)),
          file=sys.stderr)
    return exit_code


# ---------------------------------------------------------------- 入口

def main(argv=None):
    force_utf8_stdio()
    ap = _Parser(prog="at-run", description=DESCRIPTION, epilog=EPILOG,
                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--check", action="store_true",
                    help="只校验用例（字段/命名/四节/期望声明一致性），不执行")
    ap.add_argument("paths", nargs="*", metavar="PATH",
                    help="--check 要检查的用例文件或目录（缺省 <dir>/<root>/cases）")
    ap.add_argument("--dir", default=".", metavar="DIR",
                    help="项目目录（默认当前目录）")
    ap.add_argument("--root", metavar="NAME",
                    help="集成测试根目录名（缺省读 <dir>/atest.json 的 root，"
                         "再缺省 atest）")
    ap.add_argument("--instance", choices=["L1", "L2"],
                    help="实例级别（缺省读 atest.json 的 instance）")
    ap.add_argument("--domain", metavar="DOMAIN",
                    help="覆盖 atest.json 的 test_domain；会话名用 <级别>-<域>-00")
    ap.add_argument("--instances", metavar="FILE",
                    help="实例登记文件（缺省 $WN_INSTANCES > 逐级向上 > 用户目录）")
    ap.add_argument("--session-dir", metavar="DIR",
                    help="会话池目录（缺省：登记文件所在目录的 .at/client/session）")
    ap.add_argument("--only", action="append", default=[], metavar="IDS",
                    help="只跑这些 CASE-ID（逗号分隔，可重复）")
    ap.add_argument("--suite", action="append", default=[], metavar="NAMES",
                    help="只跑这些 suite（逗号分隔，可重复；缺省全部）")
    ap.add_argument("--stop-on-fail", action="store_true",
                    help="遇到第一个红灯即停（blocked 不算红）")
    ap.add_argument("--timeout", type=float, default=120.0, metavar="SEC",
                    help="单个用例脚本超时秒数（默认 120）")
    args = ap.parse_args(argv)

    if args.timeout <= 0:
        emit({"ok": False, "error": "--timeout 必须大于 0"})
        return EXIT_USAGE
    if args.check and (args.only or args.suite or args.stop_on_fail):
        emit({"ok": False,
              "error": "--check 不接受 --only / --suite / --stop-on-fail"})
        return EXIT_USAGE
    if not args.check and args.paths:
        emit({"ok": False,
              "error": "位置参数只用于 --check；执行请用 --only / --suite"})
        return EXIT_USAGE

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
        if not (args.instance or config.get("instance")):
            raise EnvError("实例级别未定：在 %s 里填 instance（如 L1 / L2），"
                           "或用 --instance 覆盖" % config_path)
        if args.check:
            return run_check(project, args.paths, root,
                             args.instance or config.get("instance"))
        return run_suite(project, args, root, config, config_path)
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
