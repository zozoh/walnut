#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
at-run — ATEST 运行器（v2）

一条命令串起 ATEST 的三件事：

    --check                 只校验用例（字段 / 命名 / 四节 / 期望声明一致性），不执行
    （无参数）              发现用例 → 解析实例 → 复用会话 → 逐例执行 → 写 JSON 报告
    --record                同一次执行，另把每例 actual 写到
                            <root>/report/<ts>/actual.json，供 bin/at-baseline 建基线；
                            该模式**不比较期望**（录制不是验收，绿以普通运行为准）
    --only                  选例执行；--stop-on-fail 遇到红灯即停
    --instance L1|L2        跑哪一级实例（缺省读 atest.json 的 instance）
    --reset                 整批执行前先跑 <root>/cleanup.py（套件级清理）
    --no-gc                 跑完不自动归档旧报告（缺省会自动调用 bin/at-gc）

夹具（ATESET-05）：静态布局写进 `<root>/atest.init`，用例脚本用
`atlib.fixture()` 的本地最小渲染器铺进域；非幂等用例另带
`<root>/scripts/<CASE-ID>.clean.py`，执行顺序固定为 **清理 → 初始化 → 用例**。
清理脚本失败记 blocked（不判红）。

实例解析（两个轴分开，都不猜）:
    级别:  --instance  →  atest.json 的 instance  →  报错
    端点:  .wn-instances.json（--instances > $WN_INSTANCES > 从项目目录逐级向上
           > 用户目录；先命中先用）→ 报错，并打印搜过的路径清单与模板
    会话:  工作区级会话池（.wn-instances.json 所在目录的 .at/client/session）里的
           <级别>-<测试域>-00；存在就直接复用（不看凭据文件），不存在就用凭据
           自动登录（--creds / atest.json 的 credentials_file /
           .at/credentials.json）；没凭据则退出 2 并给出两条路。执行中 401
           自动重登一次并重跑该例。

配置解析（项目根 atest.json，唯一来源）:
    root:         atest.json 的 root（缺省 atest）→ --root 覆盖。
    instance:     --instance 覆盖 → atest.json 的 instance → 空则退出 2。
    test_domain:  --domain 覆盖 → atest.json 的 test_domain → 空则退出 2。
    credentials_file: atest.json 的 credentials_file（缺省 .at/credentials.json）
                    → --creds 覆盖；只在需要自动登录时读（--check 不读、不登录）。
    init_template: atest.json 的 init_template（缺省 <root>/atest.init）；
                   夹具模板；--check 会警告缺失、异常大与 v1 不支持的外部引用。
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
    脚本退出码 2 = 环境不可用（记 blocked），即使没输出结果 JSON（如 atlib 的
    _die）也按 blocked；其它非 0 或超时 = failed。

期望 matcher（ATESET-04 T1 定稿的硬接口，三者互斥）:
    {"$ignore": true}                 跳过该键（等价于不写，但显式声明"它不稳定"）
    {"$range": ["$t0", "$t1"]}        实际值须落在 runner 取的毫秒区间内
    {"$regex": "^CASE-\\d+$"}         实际值须为字符串且正则命中（Python re.search）
    $t0 / $t1 由 runner 在用例开始前、结束后各取一次，脚本的本地时钟不参与；
    不认识 / 混用多个 matcher → --check 报 expected-invalid。

报告（JSON，唯一源；Markdown 归 07）:
    <root>/report/<timestamp>/report.json  +  同内容的 latest.json
    <root>/report/<timestamp>/report.md    由 bin/at-report 生成
    <root>/report/<timestamp>/actual.json  仅 --record：{CASE-ID: actual} + _meta
    <root>/report/<timestamp>/cases/<CASE-ID>/…  每例证据
    {"schema","timestamp","instance","instance_source","total","passed",
     "failed","blocked","cases":[{"caseID","status","duration_ms","evidence_dir",
     "diff","evidence"}]}
    报告内路径一律**相对项目根 + `/`**（evidence_dir 如
    atest/report/<ts>/cases/CASE-101，instance_source 如 atest.json）；
    stdout 摘要的 report / latest / actual 仍是绝对路径（给人复制粘贴），有意为之。
    schema = "atest-report/2"（v1 无此键，只增不改；v2 用例格式去掉 suite）；
    status = passed / failed / blocked（blocked = 未实现或缺基线，不算红）；
    evidence 是相对证据目录的文件清单（summary.txt / objects/… 等）。
    写完后自动调用 bin/at-gc 归档旧报告（保留最近 atest.json 的 report_keep 次，
    缺省 3）；归档失败只警告、不改变退出码，stdout 摘要的 archived = 本次归档数。
    正在写的报告（含本次）由 at-gc 按"缺 json 或 5 分钟内改过"跳过，绝不动 latest.*。
    stdout 摘要另有 session_created（本次是否新建会话：true = 自动登录新建，
    false = 复用已有会话），便于人确认没有悄悄换身份。

退出码: 0 全绿（failed 为 0，blocked 不算红）；1 有红 / 用法错误；
        2 环境不可用（atest.json 或实例登记缺失、端口不通、缺凭据或自动登录失败）。

零依赖 Python3；所有写入 UTF-8 无 BOM、LF。
"""

import argparse
import hashlib
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
# wn-cli 的认证失败退出码（401 / 登录失败）；执行中命中它 → 重登一次并重跑该例
EXIT_AUTH = 3
WNCLI_AUTH_RE = re.compile(r"wn-cli 退出码 3(?![0-9])")

INSTANCES_NAME = ".wn-instances.json"
CONFIG_NAME = "atest.json"

# 凭据（ATESET 会话透明化）：--creds > atest.json 的 credentials_file > 该缺省
CREDS_DEFAULT = ".at/credentials.json"
# 缺凭据时给人看的可照抄形状；口令由使用者自己填，工具不猜、不写进仓库
CREDS_SHAPE = '{"usr":"…","passwd":"…"}'

ROOT_DEFAULT = "atest"
# 集成测试根目录名：不许路径分隔符，也不许 . / ..（否则路径会跑出项目）
ROOT_RE = re.compile(r"^[A-Za-z0-9._-]+$")

# 夹具（ATESET-05）：套件级清理脚本与 --check 的模板大小阈值
CLEANUP_NAME = "cleanup.py"
CASE_CLEAN_SUFFIX = ".clean.py"
INIT_MAX_BYTES = 256 * 1024
# 夹具 v1 只支持内联内容块；这两类写法引用外部文件
INIT_EXTERNAL_RE = re.compile(r"^[%?](COPY|TMPL|EXPLAIN)(\{.*\})?\s*>", re.I)
INIT_INCLUDE_RE = re.compile(r"^@INCLUDE\b", re.I)

REQUIRED_FIELDS = ("caseID", "title", "source", "contracts", "expected")
EXPECTED_KINDS = ("inline", "file", "baseline")
SECTIONS = ("前置", "步骤", "期望", "证据")
REMOVED_FIELDS = ("instance", "suite")

# 比较时忽略的易变字段（DRAFT §4.4：每次执行都不同），以及以 _ 开头的内部键
IGNORE_KEYS = ("id", "pid", "ct", "lm", "sha1")

# 期望 matcher（ATESET-04 T1 定稿的硬接口）：$ 前缀与普通字段区分，三者互斥
MATCHER_KEYS = ("$ignore", "$range", "$regex")
REPORT_SCHEMA = "atest-report/2"
# --record 产出的 actual 文件（喂给 bin/at-baseline --from）；_meta 是录制元信息，
# 其余键 = CASE-ID → 脚本 actual。at-baseline 把 _ 开头的键当元信息、不当用例。
ACTUAL_SCHEMA = "atest-actual/1"

# contracts 某项只写命令族不算"被测契约"（02 反例错误 3）
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
  baseline / report 都在 <root>/ 下。atest.json 缺失 → 退出 2（先跑 at-init）。
  基线 <root>/baseline/<CASE-ID>.json 必须可移植：只收与环境无关的值（易变字段、
  绝对路径不写进基线），同一份基线在 L1/L2 与别人的机器上都要能过。
  建立：--record 产 actual → bin/at-baseline --from 清洗后落盘（见其 README）。

配置（项目根 atest.json，唯一来源）:
  root / instance / test_domain / init_template / credentials_file 都从这里读；
  --root / --instance / --domain / --creds 可覆盖；instance 为空 → 退出 2
  （--check 也一样）；凭据只在需要自动登录时读（--check 不读、不登录）。

用例发现:
  <dir>/<root>/cases/*.md    文件名 = caseID；脚本 <root>/scripts/<caseID>.py

夹具与清理（ATESET-05）:
  静态布局 -> <root>/atest.init（只支持内联块；用例里用 atlib.fixture() 执行）
  变量     -> <root>/vars.json（可选；对象，项目级覆盖调用方默认值）
  套件清理 -> <root>/cleanup.py（--reset 时先跑；失败全批 blocked）
  用例清理 -> <root>/scripts/<CASE-ID>.clean.py（该例前跑；失败该例 blocked）
  顺序     -> 清理 → 初始化（fixture）→ 用例；清理脚本不许删域/数据目录

实例与会话:
  级别  --instance L1|L2  →  atest.json 的 instance  →  报错
  端点  .wn-instances.json（--instances > $WN_INSTANCES > 逐级向上 > 用户目录）
  会话  工作区池 .at/client/session 的 <级别>-<测试域>-00；
        存在就直接复用（不看凭据文件）；不存在就自动登录（凭据：--creds >
        atest.json 的 credentials_file > .at/credentials.json，形状
        {"usr":"…","passwd":"…"}，不进 git）；没凭据 → 退出 2 并给出两条路
        （建凭据自动登录 / 先手工 wn-cli login 一次）。
        执行中 401 自动重登一次并重跑该例；仍失败记 failed（不是 blocked）。

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
  <root>/report/<timestamp>/report.json 与同内容 latest.json
  <root>/report/<timestamp>/report.md（由 bin/at-report 生成）
  <root>/report/<timestamp>/actual.json（仅 --record，喂给 bin/at-baseline）
  <root>/report/<timestamp>/cases/<CASE-ID>/（每例证据）
  schema = "atest-report/2"（03 的顶层键保留；用例格式 v2 去掉 suite；只增不改）
  cases[] = {"caseID","status","duration_ms","evidence_dir","diff","evidence"}
  status = passed / failed / blocked（blocked 不算红）
  evidence = 相对证据目录的文件清单（summary.txt / objects/…）
  报告内路径一律相对项目根 + `/`（evidence_dir 如 atest/report/<ts>/cases/CASE-101，
  instance_source 如 atest.json）；stdout 摘要的 report / latest / actual 是绝对
  路径（方便复制粘贴）——这个差异是有意的，别把它当 bug。
  跑完自动调用 bin/at-gc 归档旧报告（保留最近 report_keep 次，缺省 3）：
  stderr 打印 at-gc 的摘要，stdout JSON 多一个 archived = 本次归档数；
  --no-gc 关闭；at-gc 失败只警告，不改变本次退出码。

--check 错误码:
  filename-mismatch / caseid-invalid / missing-field / removed-field /
  expected-invalid / contract-too-generic / missing-section /
  assertion-in-steps / expected-mismatch / expected-invalid-json /
  volatile-field / caseid-duplicate

--check 警告码（只警告，不影响退出码）:
  init-template-missing / init-template-not-file / init-template-unreadable /
  init-template-too-large / init-template-external-ref / init-template-include /
  baseline-missing / baseline-invalid / baseline-no-hash / baseline-stale /
  baseline-hash-skipped

示例:
  python bin/at-run/at-run.py --check
  python bin/at-run/at-run.py --check bin/at-init/tmpl/CASE-001.md
  python bin/at-run/at-run.py --record
  python bin/at-run/at-run.py --instance L1 --only CASE-001,CASE-002
  python bin/at-run/at-run.py --instance L1 --reset
  python bin/at-run/at-run.py --instance L2 --stop-on-fail
  python bin/at-run/at-run.py --creds .at/temp-creds.json
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


def _unquote(value):
    value = value.strip()
    if len(value) >= 2 and value[0] == value[-1] and value[0] in "\"'":
        return value[1:-1]
    return value


def _inline_list(value):
    """`[a, b]` → list；优先按 JSON，失败再按逗号切（兼容不加引号的写法）。"""
    text = value.strip()[1:-1].strip()
    if not text:
        return []
    try:
        parsed = json.loads("[%s]" % text)
        if isinstance(parsed, list):
            return [_unquote(str(x)) for x in parsed]
    except ValueError:
        pass
    return [_unquote(x) for x in text.split(",") if x.strip()]


def parse_front_matter(fm):
    """解析 front-matter：标量按 `key: value`，`contracts` 支持块列表/内联列表。

    标量值一律转字符串（YAML 的日期类型会被 str 归一，02 的 F2）；
    `key:` 后没有值表示一个列表字段，后续 `- item` 行逐个收集。
    """
    fields = {}
    current = None
    if fm is None:
        return fields
    for raw in fm.splitlines():
        s = raw.strip()
        if not s or s.startswith("#"):
            continue
        if s.startswith("-"):
            item = _unquote(s[1:].strip())
            if current and isinstance(fields.get(current), list):
                fields[current].append(item)
            continue
        m = FIELD_RE.match(s)
        if not m:
            continue
        key = m.group(1)
        value = m.group(2).strip()
        if not value:
            fields[key] = []
            current = key
        elif value.startswith("[") and value.endswith("]"):
            fields[key] = _inline_list(value)
            current = None
        else:
            fields[key] = _unquote(value)
            current = None
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


def warn_of(path, code, message):
    return {"file": str(path), "code": code, "message": message}


def init_template_path(project, config, root):
    """夹具模板路径：atest.json 的 init_template > <root>/atest.init。"""
    raw = (config or {}).get("init_template") or ("%s/atest.init" % root)
    path = Path(raw)
    if not path.is_absolute():
        path = Path(project) / path
    return path


def init_template_warnings(project, config, root):
    """--check 对夹具模板的**警告**（不阻塞）：缺失、异常大、不可读、v1 不支持。"""
    path = init_template_path(project, config, root)
    if not path.exists():
        return [warn_of(path, "init-template-missing",
                        "缺夹具模板 %s：用例仍可跑，但 atlib.fixture() 会记 "
                        "blocked；照 bin/at-init/tmpl/atest.init 写一份"
                        % rel(path, project))]
    if not path.is_file():
        return [warn_of(path, "init-template-not-file",
                        "%s 不是文件：夹具模板必须是文件" % rel(path, project))]
    try:
        size = path.stat().st_size
    except OSError as e:
        return [warn_of(path, "init-template-unreadable",
                        "读不了 %s 的大小：%s" % (rel(path, project), e))]
    if size > INIT_MAX_BYTES:
        return [warn_of(path, "init-template-too-large",
                        "夹具模板 %s 有 %d 字节（阈值 %d）：模板越大越难审；"
                        "大块内容先用 wn-cli obj save-text 上传再引用"
                        % (rel(path, project), size, INIT_MAX_BYTES))]
    try:
        text = read_text(path)
    except (EnvError, OSError) as e:
        return [warn_of(path, "init-template-unreadable", str(e))]
    out = []
    for i, line in enumerate(text.splitlines(), 1):
        stripped = line.strip()
        if INIT_EXTERNAL_RE.match(stripped):
            out.append(warn_of(path, "init-template-external-ref",
                               "第 %d 行引用了外部文件（`%s`）：`app init` 支持，"
                               "但引用文件须与模板同目录上传"
                               "（atlib.fixture 只上传模板本身）"
                               % (i, stripped)))
        elif INIT_INCLUDE_RE.match(stripped):
            out.append(warn_of(path, "init-template-include",
                               "第 %d 行用了 @INCLUDE：`app init` 支持，"
                               "但被包含文件须与模板同目录上传" % i))
    return out


def source_hash(case_path, init_path):
    """基线来源哈希：sha256(用例 md + 夹具模板) 前 16 位（ATESET-08 定稿）。

    用例 md 或夹具模板改了，期望就可能变——两者都进哈希。
    公式必须与 bin/at-baseline/at-baseline.py 完全一致（两处都写了注释）：
        sha256("md\n" + md + "\\x00" + "init\n" + init + "\\x00")[:16]
    CRLF 先归一成 LF，同一份内容在 Windows / Linux checkout 上哈希相同。
    """
    digest = hashlib.sha256()
    for label, path in (("md", case_path), ("init", init_path)):
        text = read_text(path).replace("\r\n", "\n").replace("\r", "\n")
        digest.update(label.encode("ascii") + b"\n")
        digest.update(text.encode("utf-8"))
        digest.update(b"\x00")
    return digest.hexdigest()[:16]


def baseline_warnings(project, root, config, case_path, case_id):
    """--check 对 expected: baseline 的警告：缺失 / 坏 JSON / 无哈希 / 哈希过期。

    **只警告不阻塞**：基线可能还没建立（新用例常态），用例改了没重审也不该让
    --check 变红——但必须让人看见，否则"绿灯"会拿着过期的期望跑。
    """
    path = Path(project) / root / "baseline" / ("%s.json" % case_id)
    if not path.is_file():
        return [warn_of(path, "baseline-missing",
                        "缺基线 %s：该例执行时记 blocked；建法见 "
                        "bin/at-baseline/README.md" % rel(path, project))]
    try:
        data = json.loads(path.read_text(encoding="utf-8-sig"))
    except (ValueError, OSError, EnvError) as e:
        return [warn_of(path, "baseline-invalid",
                        "基线读不了或不是合法 JSON：%s" % e)]
    if not isinstance(data, dict):
        return [warn_of(path, "baseline-invalid", "基线顶层应为对象")]
    meta = data.get("meta")
    stored = meta.get("source_hash") if isinstance(meta, dict) else None
    try:
        current = source_hash(case_path, init_template_path(project, config, root))
    except (EnvError, OSError) as e:
        return [warn_of(path, "baseline-hash-skipped",
                        "算不了来源哈希（夹具模板缺失/不可读？）：%s" % e)]
    if not stored:
        return [warn_of(path, "baseline-no-hash",
                        "基线缺 meta.source_hash：用例改了也发现不了；"
                        "用 at-baseline --from 重录（当前哈希 %s）" % current)]
    if stored != current:
        return [warn_of(path, "baseline-stale",
                        "用例 md / 夹具模板已改，基线待复核："
                        "基线 %s ≠ 当前 %s（重新审核后 at-baseline --from --force）"
                        % (stored, current))]
    return []


def contract_is_generic(item):
    """契约项是否只写了命令族（02 反例错误 3）。"""
    text = item.strip()
    if not text:
        return True
    low = text.lower()
    if low in GENERIC_CONTRACTS:
        return True
    # 端点或以 .js 结尾的脚本，本身就是具体契约
    if text.startswith("/") or text.endswith(".js"):
        return False
    parts = text.split()
    if len(parts) < 2:
        return True
    # o / cmd / jsc 这类族名必须带 @子命令才算具体
    if parts[0].lower() in ("o", "obj", "cmd", "jsc", "sqlx", "api",
                            "regapi") \
            and not any(p.startswith("@") for p in parts[1:]):
        return True
    return False


def check_case(path, text, root=ROOT_DEFAULT, project=None):
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
    for removed in REMOVED_FIELDS:
        if removed in fields:
            errors.append(err_of(
                path, case_id, "removed-field",
                "字段 `%s` 已移除：%s" % (
                    removed,
                    "实例是运行期信息，请在 atest.json / --instance 指定"
                    if removed == "instance"
                    else "用 --only 选批，不再有 suite")))
    for name in REQUIRED_FIELDS:
        if name == "contracts":
            continue
        if not fields.get(name):
            errors.append(err_of(path, case_id, "missing-field",
                                 "缺少必填字段 %s" % name))

    kind = fields.get("expected", "")
    if kind and kind not in EXPECTED_KINDS:
        errors.append(err_of(path, case_id, "expected-invalid",
                             "expected 只能是 %s，现在是 %r"
                             % (" / ".join(EXPECTED_KINDS), kind)))
    contracts = fields.get("contracts")
    if not contracts:
        errors.append(err_of(path, case_id, "missing-field",
                             "缺少必填字段 contracts：非空数组，按调用顺序列"
                             "（如 - cmd_o @create / - /api/x / - a.js）"))
    elif not isinstance(contracts, list):
        errors.append(err_of(path, case_id, "missing-field",
                             "contracts 必须是非空数组（现在 %r）" % (contracts,)))
    else:
        for idx, item in enumerate(contracts, 1):
            if not isinstance(item, str) or not item.strip():
                errors.append(err_of(path, case_id, "missing-field",
                                     "contracts 第 %d 项为空/不是字符串" % idx))
            elif contract_is_generic(item):
                errors.append(err_of(
                    path, case_id, "contract-too-generic",
                    "contracts 第 %d 项 %r 过泛：要具体到子命令/端点/脚本"
                    "（如 cmd_o @create、/api/x、a.js）" % (idx, item)))

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
                                 "来自 %s/baseline/%s.json（基线必须可移植：只收与"
                                 "环境无关的值）" % (root, case_id)))

        if data is None and blocks and kind != "inline":
            # 声明与内容矛盾已单独报错；这里只为易变字段检查试着解析
            try:
                data = json.loads(blocks[0])
            except ValueError:
                data = None
        if data is not None:
            for message in expected_matcher_errors(data):
                errors.append(err_of(path, case_id, "expected-invalid", message))
        # 易变字段：inline 期望与反例里"声明 baseline 却贴 inline"的块都查；
        # 期望文件/基线不在这里展开。
        if data is not None:
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


def run_check(project, paths, root=ROOT_DEFAULT, config=None):
    targets = collect_cases(paths, project, root)
    errors = []
    warnings = init_template_warnings(project, config or {}, root)
    seen = {}
    checked = []
    for path in targets:
        try:
            text = read_text(path)
        except EnvError as e:
            errors.append(err_of(path, "", "read-failed", str(e)))
            continue
        fields, _sections, errs = check_case(path, text, root, project)
        errors.extend(errs)
        case_id = fields.get("caseID", "")
        if case_id and fields.get("expected") == "baseline":
            warnings.extend(baseline_warnings(project, root, config or {},
                                             path, case_id))
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
          "files": checked, "error_count": len(errors), "errors": errors,
          "warning_count": len(warnings), "warnings": warnings})
    if not ok:
        print("at-run --check：%d 个用例文件，%d 条错误（修完再跑；"
              "错误码含义见 --help）" % (len(checked), len(errors)),
              file=sys.stderr)
    if warnings:
        print("at-run --check：%d 条警告（不阻塞；夹具模板与基线问题见 --help）"
              % len(warnings), file=sys.stderr)
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


def resolve_creds_path(project, config, override):
    """凭据文件：--creds > atest.json 的 credentials_file > .at/credentials.json。

    --creds 的相对路径按当前目录解析（与 --instances / --session-dir 一致）；
    atest.json 里的相对路径按项目目录解析（配置就在项目根）。
    """
    if override:
        return Path(override).expanduser().resolve()
    raw = (config or {}).get("credentials_file") or CREDS_DEFAULT
    path = Path(str(raw)).expanduser()
    if not path.is_absolute():
        path = Path(project) / path
    return path.resolve()


def manual_login_hint(wncli, level, se, session_dir, url):
    """可直接照抄的手工登录命令（缺凭据时给人的 B 路）。"""
    return ('python "%s" --level %s --se %s --session-dir "%s" --url %s '
            'login --usr <登录名> --passwd <密码>'
            % (wncli, level, se, session_dir, url))


def load_credentials(path, manual_hint=""):
    """读凭据文件（{"usr":"…","passwd":"…"}）；缺失 / 坏档 / 缺键 → EnvError。

    错误信息同时给出两条路（用户口径）：A. 建凭据文件让工具自动登录；
    B. 不建凭据，先手工 `wn-cli … login` 一次。会话存在时本工具不读这个文件，
    所以 B 路用户不会被凭据文件的存在与否打扰。
    口令只进 wn-cli 的 argv，不进日志 / 错误信息 / 报告。
    """
    alt = ""
    if manual_hint:
        alt = ("\n  B. 不建凭据文件，先手工登录一次"
               "（之后直接跑，会话会被复用，过期再登录一次）：\n"
               "       %s" % manual_hint)
    hint = ("二选一：\n"
            "  A. 建该文件（UTF-8 无 BOM），内容形如：\n"
            "       %s\n"
            "     该文件不进 git（放在 .at/ 目录里即被忽略；"
            "也可用 --creds 放到别处）。%s"
            % (CREDS_SHAPE, alt))
    if not path.is_file():
        raise EnvError("缺凭据文件 %s：没有它就没法自动建会话。\n%s"
                       % (path, hint))
    try:
        data = json.loads(read_text(path))
    except (ValueError, EnvError, OSError) as e:
        raise EnvError("凭据文件 %s 读不了或不是合法 JSON（%s）。\n%s"
                       % (path, e, hint))
    if not isinstance(data, dict):
        raise EnvError("凭据文件 %s 顶层必须是对象。\n%s" % (path, hint))
    usr = data.get("usr")
    passwd = data.get("passwd")
    if not isinstance(usr, str) or not usr.strip():
        raise EnvError("凭据文件 %s 缺 usr（或不是非空字符串）。\n%s"
                       % (path, hint))
    if not isinstance(passwd, str) or not passwd:
        raise EnvError("凭据文件 %s 缺 passwd（或不是非空字符串）。\n%s"
                       % (path, hint))
    return data


def login_session(wncli, level, se, session_dir, url, project,
                  creds_path, creds):
    """用凭据执行一次 wn-cli login（argv 传参，绝不经 shell）。

    成功才返回；失败抛 EnvError 并原样带上 wn-cli 的输出。口令只在 argv 里，
    不写日志、不进报告、不出现在错误信息中。
    """
    argv = ["--level", level, "--se", se,
            "--session-dir", str(session_dir), "--url", url,
            "--creds", str(creds_path),
            "login", "--usr", creds["usr"], "--passwd", creds["passwd"]]
    print("at-run: 会话 %s 不存在或已失效，自动登录（--usr %s）"
          % (se, creds["usr"]), file=sys.stderr)
    try:
        rc, out, err = run_wncli(wncli, argv, project)
    except subprocess.TimeoutExpired:
        raise EnvError("自动登录超时（wn-cli 60s 未返回）：实例可能挂了，"
                       "请开发者确认 %s" % url)
    if rc != 0:
        raw = ("\n".join(x for x in (out.strip(), err.strip()) if x)
               or "（无输出）")
        raise EnvError("自动登录失败（wn-cli 退出码 %d，会话 %s，凭据 %s）：\n%s"
                       % (rc, se, creds_path, raw))
    return se


def probe_session(wncli, level, se, session_dir, url, project):
    """用一条只读命令确认会话真能连上实例；返回 (rc, out, err)。"""
    try:
        return run_wncli(
            wncli,
            ["--level", level, "--se", se, "--session-dir", str(session_dir),
             "--url", url, "cmd", "o ~ @json -cqn"],
            project)
    except subprocess.TimeoutExpired:
        raise EnvError("会话探活超时（wn-cli 60s 未返回）：实例可能挂了"
                       "（L1 上挂了断点也会这样），请开发者确认")


def ensure_session(wncli, level, domain, session_dir, url, project, creds_path):
    """复用或自动建立工作区池会话 <级别>-<测试域>-00；返回 (会话名, 是否新建)。

    判定（用户口径）：
      - 会话文件存在 → 直接探活复用，**不看凭据文件**（手工登录过的用户走这条）；
        探活 401（wn-cli 退出码 3，缓存已被它当场删掉）→ 有凭据就自动重登；
      - 会话文件不存在 → 有凭据就自动登录后跑；
      - 上面两种情况没凭据 → 退出 2，错误信息同时给"建凭据"与"手工登录"两条路；
      - 端点与会话里的 base 不一致 → 拒绝复用（防 L1/L2 混跑，需人确认配置）。
    """
    se = "%s-%s-00" % (level, domain)
    sess_file = session_dir / (se + ".toml")
    hint = manual_login_hint(wncli, level, se, session_dir, url)
    if sess_file.is_file():
        base = session_base(sess_file)
        if base and base.rstrip("/") != url.rstrip("/"):
            raise EnvError("会话 %s 指向 %s，与本次端点 %s 不一致：拒绝复用"
                           "（防 L1/L2 混跑）。确认后用 wn-cli 重新登录"
                           % (se, base, url))
        rc, out, err = probe_session(wncli, level, se, session_dir, url, project)
        if rc == 0:
            return se, False
        if rc != EXIT_AUTH:
            detail = (err.strip() or out.strip() or "").replace("\n", " ")[:500]
            raise EnvError("会话 %s 不可用（wn-cli 退出码 %d）：%s"
                           % (se, rc, detail))
        print("at-run: 会话 %s 已失效（wn-cli 退出码 3，缓存已删），尝试自动重建"
              % se, file=sys.stderr)
    creds = load_credentials(creds_path, hint)
    login_session(wncli, level, se, session_dir, url, project, creds_path, creds)
    # 登录成功再确认一次：票据写坏 / 目录不可写时不能假装会话可用
    rc, out, err = probe_session(wncli, level, se, session_dir, url, project)
    if rc != 0:
        detail = (err.strip() or out.strip() or "").replace("\n", " ")[:500]
        raise EnvError("自动登录后会话 %s 仍不可用（wn-cli 退出码 %d）：%s"
                       % (se, rc, detail))
    return se, True


# ---------------------------------------------------------------- 期望与比较

def read_json_file(path):
    try:
        return json.loads(Path(path).read_text(encoding="utf-8-sig"))
    except (ValueError, OSError) as e:
        raise EnvError("期望文件不是合法 JSON：%s（%s）" % (path, e))


def load_expected(project, case, kind, root=ROOT_DEFAULT):
    """按 expected 声明取期望值；返回 (值, 缺失原因)。缺基线/期望文件 = blocked。

    baseline 是**单一、可移植**的路径：<root>/baseline/<CASE-ID>.json。
    同一份基线在 L1 / L2、在别人的机器上都要能过；不同实例结果不同就是 bug，
    不用"两套基线"消化。
    """
    case_id = case["caseID"]
    if kind == "inline":
        return case["expected_value"], None
    if kind == "file":
        path = Path(project) / root / "expect" / ("%s.json" % case_id)
        if not path.is_file():
            return None, "期望文件缺失：%s（expected: file）" % rel(path, project)
        return read_json_file(path), None
    path = Path(project) / root / "baseline" / ("%s.json" % case_id)
    if not path.is_file():
        return None, ("基线缺失：%s（expected: baseline；基线必须可移植——只收与"
                      "环境无关的值，L1/L2 与别人的机器上都要能过；由审核者建立，08）"
                      % rel(path, project))
    data = read_json_file(path)
    # 基线格式 {"meta": {...}, "expected": {...}}：runner 只读 expected（meta 不是断言）；
    # 没有 expected 键的裸对象（手写基线）整份当期望，向后兼容。
    if isinstance(data, dict) and "expected" in data:
        expected = data["expected"]
        if not isinstance(expected, dict):
            raise EnvError("基线 %s 的 expected 不是对象：基线期望必须是一个 JSON 对象"
                           % rel(path, project))
        return expected, None
    return data, None


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


def auth_failure(returncode, stdout, stderr):
    """这一例是不是因为认证失败（wn-cli 退出码 3）而中止的。

    atlib 把 wn-cli 的非 0 退出码原样写进原因（`wn-cli 退出码 3：…`），
    所以从脚本输出里认这个标记；脚本自己经 wn-cli 之外取数、把 401 /
    e.auth 写进结果 JSON 的，也认。
    """
    text = "\n".join(x for x in (stdout, stderr) if x)
    if WNCLI_AUTH_RE.search(text):
        return True
    result, _problem = last_json_line(stdout)
    if isinstance(result, dict) and result.get("ok") is False:
        message = str(result.get("error") or result.get("msg") or "")
        return ("e.auth" in message or "401" in message
                or "退出码 3" in message)
    return False


def judge(returncode, stdout, expected, evidence, clock=None, record=False):
    """按脚本契约判定一例；返回 (status, diff, note, actual)。

    record=True 时只验脚本契约（ok / actual 在位），**不比较期望**——那是 --record
    的语义：录制不是验收。actual 供 --record 落盘（没取到时为 None）。
    """
    result, problem = last_json_line(stdout)
    if problem:
        # 脚本以 2 退出但没来得及写结果 JSON（如 atlib 在 fixture/snap 里
        # _die）仍是"环境不可用"，按契约记 blocked，不伪装成红。
        if returncode == 2:
            return "blocked", [], ("脚本报环境不可用（exit 2）：%s"
                                   % problem), None
        return "failed", [], problem, None
    write_json(Path(evidence) / "result.json", result)
    if not isinstance(result, dict):
        return "failed", [], "结果 JSON 不是对象", None
    ok = result.get("ok")
    message = result.get("error") or result.get("msg") or ""
    if ok is False:
        if returncode == 2:
            return "blocked", [], ("脚本报环境不可用（exit 2）：%s"
                                   % (message or "（无 error 字段）")), None
        return "failed", [], ("脚本报失败：%s"
                              % (message or "（无 error 字段）")), None
    if ok is not True:
        return "failed", [], "结果 JSON 缺 ok 布尔字段", None
    if returncode != 0:
        return "failed", [], ("脚本退出码 %d，但结果 JSON 声明 ok=true（矛盾）"
                              % returncode), None
    if "actual" not in result:
        return "failed", [], "结果 JSON 声明 ok=true 但缺 actual 字段", None
    actual = result["actual"]
    if record:
        return "passed", [], "", actual
    diffs = compare(expected, actual, clock)
    if diffs:
        return "failed", diffs, "与期望不一致（%d 处）" % len(diffs), None
    return "passed", [], "", actual


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
                  clock, level, notes, record=False):
    """写人一眼看懂为什么红/绿的一句话总结（报告 evidence 清单会指向它）。"""
    lines = []
    if status == "passed" and record:
        lines.append("%s recorded —— 录制模式：脚本契约通过，本次不比较期望（%dms）"
                     % (case_id, duration))
    elif status == "passed":
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


def run_cleanup(project, script, evidence, env_extra, timeout, label):
    """执行一个清理脚本（ATESET-05），证据落 evidence/。

    返回 (ok, detail)：ok=False 时 detail 是一句可放进 blocked 说明的原因。
    清理脚本的 stdout/stderr/summary 都在 evidence/ 下；脚本自己用 atlib.note()
    写的行会被保留进 summary.txt。
    """
    evidence = Path(evidence)
    evidence.mkdir(parents=True, exist_ok=True)
    write_text(evidence / "cmd.txt",
               "\n".join([sys.executable, str(script)]) + "\n")
    write_text(evidence / "env.txt",
               "\n".join("%s=%s" % (k, env_extra[k])
                          for k in sorted(env_extra)) + "\n")
    print("at-run: %s清理 %s" % (label, rel(script, project)),
          file=sys.stderr)
    stdout = stderr = ""
    detail = ""
    ok = False
    t0 = int(time.time() * 1000)
    try:
        proc = subprocess.run([sys.executable, str(script)],
                              cwd=str(project), env=script_env(env_extra),
                              capture_output=True, timeout=timeout)
        stdout = proc.stdout.decode("utf-8", "replace")
        stderr = proc.stderr.decode("utf-8", "replace")
        ok = proc.returncode == 0
        if not ok:
            detail = "退出码 %d" % proc.returncode
    except subprocess.TimeoutExpired as e:
        stdout = (e.stdout or b"").decode("utf-8", "replace")
        detail = "超时（%.0fs）已被终止" % timeout
    except OSError as e:
        detail = "启动失败：%s" % e
    duration = int(time.time() * 1000) - t0
    write_text(evidence / "stdout.txt", stdout)
    write_text(evidence / "stderr.txt", stderr)

    notes = script_notes(evidence)
    lines = []
    if ok:
        lines.append("%s清理 ok（%dms）" % (label, duration))
    else:
        lines.append("%s清理失败 —— %s（%dms）" % (label, detail, duration))
        tail = (stderr.strip() or stdout.strip()).replace("\n", " ")
        if tail:
            lines.append("输出: %s" % tail[:500])
    lines.append("复现: %s" % " ".join([sys.executable, str(script)]))
    if notes:
        lines.append("")
        lines.append("脚本记录:")
        lines.extend(notes)
    write_text(evidence / "summary.txt", "\n".join(lines) + "\n")
    return ok, detail


def run_case_script(script, project, env_extra, timeout):
    """跑一次用例脚本；返回 dict（超时 / 启动失败也返回，不抛）。

    键：rc（None = 没跑起来）、stdout / stderr、t0 / t1 / duration_ms、
    timeout（是否超时）、oserror（启动失败的说明）。401 重跑时用它把两次
    执行的时间区间与输出分清楚。
    """
    t0 = int(time.time() * 1000)
    out = {"t0": t0, "rc": None, "stdout": "", "stderr": "",
           "timeout": False, "oserror": ""}
    try:
        proc = subprocess.run([sys.executable, str(script)],
                              cwd=str(project), env=script_env(env_extra),
                              capture_output=True, timeout=timeout)
        out["rc"] = proc.returncode
        out["stdout"] = proc.stdout.decode("utf-8", "replace")
        out["stderr"] = proc.stderr.decode("utf-8", "replace")
    except subprocess.TimeoutExpired as e:
        out["timeout"] = True
        out["stdout"] = (e.stdout or b"").decode("utf-8", "replace")
        out["stderr"] = (e.stderr or b"").decode("utf-8", "replace")
    except OSError as e:
        out["oserror"] = str(e)
    out["t1"] = int(time.time() * 1000)
    out["duration_ms"] = out["t1"] - out["t0"]
    return out


# ---------------------------------------------------------------- 报告归档

def run_gc(project, root):
    """跑完自动归档旧报告（bin/at-gc）；失败只警告，返回归档数。

    at-gc 的退出码不影响本次 at-run 的退出码：报告已经写完，归档是收尾动作；
    失败时把原因打到 stderr，让人知道旧报告还堆着，而不是静默跳过。
    """
    script = Path(__file__).resolve().parent.parent / "at-gc" / "at-gc.py"
    if not script.is_file():
        print("at-run: 找不到 %s，跳过报告归档（--no-gc 可显式关闭）"
              % script, file=sys.stderr)
        return 0
    try:
        proc = subprocess.run(
            [sys.executable, str(script), "--dir", str(project),
             "--root", root],
            cwd=str(project), env=child_env(),
            stdout=subprocess.PIPE, timeout=180.0)
    except subprocess.TimeoutExpired:
        print("at-run: 自动归档超时（180s），本次不归档；不影响结果",
              file=sys.stderr)
        return 0
    except OSError as e:
        print("at-run: 自动归档启动失败：%s；不影响结果" % e, file=sys.stderr)
        return 0
    out = proc.stdout.decode("utf-8", "replace")
    data, _problem = last_json_line(out)
    if proc.returncode != 0 or not isinstance(data, dict):
        tail = (out.strip().splitlines() or ["无输出"])[-1]
        print("at-run: 自动归档失败（at-gc 退出码 %d）：%s；旧报告保留，"
              "不影响本次结果" % (proc.returncode, tail[:300]), file=sys.stderr)
        return 0
    count = len(data.get("archived") or [])
    if count:
        print("at-run: 自动归档 %d 个旧报告（保留最近 %s 个；--no-gc 可关闭）"
              % (count, data.get("keep")), file=sys.stderr)
    return count


def run_suite(project, args, root, config, config_path):
    project = Path(project).resolve()

    level = args.instance or config.get("instance")
    if not level:
        raise EnvError("实例级别未定：在 %s 里填 instance（如 L1 / L2），"
                       "或用 --instance 覆盖" % config_path)
    if level not in ("L1", "L2"):
        raise EnvError("instance 只能是 L1 / L2，现在是 %r" % level)
    instance_source = "cli" if args.instance else rel(config_path, project)

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

    creds_path = resolve_creds_path(project, config, args.creds)
    if args.creds and not creds_path.is_file():
        # 显式指定就先校验：别等"要登录了"才发现路径写错
        load_credentials(creds_path)

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
                                              project)
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

    def match(case):
        case_id = case["fields"].get("caseID", "")
        if only and case_id not in only:
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
        raise UsageError("筛选条件没有命中任何用例（--only 太窄）")

    broken = [c for c in selected if c["errors"]]
    if broken:
        emit({"ok": False,
              "error": "用例格式错误，未执行（先跑 at-run --check 修掉）",
              "cases": [{"file": str(c["file"]),
                         "errors": c["errors"]} for c in broken]})
        return EXIT_USAGE

    started = datetime.now().astimezone()
    report_base = project / root / "report"
    run_id = unique_run_id(report_base, started)
    run_dir = report_base / run_id
    report_path = run_dir / "report.json"
    latest_path = report_base / "latest.json"

    if not port_open(host, port):
        raise EnvError("实例 %s 端点 %s 连不上（探活 3s 超时）：智能体不启动实例，"
                       "请开发者确认实例状态" % (level, url))
    se, session_created = ensure_session(wncli, level, domain, session_dir,
                                         url, project, creds_path)
    print("at-run: 会话 %s —— %s"
          % (se, "新建（自动登录）" if session_created else "复用已有会话"),
          file=sys.stderr)

    # 执行中 401：重登一次。重登失败的原因在本批内记住，避免每个用例都去
    # 撞同一堵墙；该例仍记 failed（不是 blocked）。
    auth_login_failed = ""

    def relogin():
        """执行中 401 用：读凭据 → wn-cli login；返回 (ok, 失败说明)。"""
        try:
            creds = load_credentials(
                creds_path,
                manual_login_hint(wncli, level, se, session_dir, url))
            login_session(wncli, level, se, session_dir, url, project,
                          creds_path, creds)
            return True, ""
        except EnvError as e:
            return False, str(e)

    cleanup_state = "skipped"
    batch_cleanup_note = ""
    if args.reset:
        cleanup_script = project / root / CLEANUP_NAME
        if not cleanup_script.is_file():
            cleanup_state = "missing"
            print("at-run: --reset 但缺 %s：跳过套件级清理（视为套件幂等）"
                  % rel(cleanup_script, project), file=sys.stderr)
        else:
            reset_dir = run_dir / "cases" / "_reset"
            reset_env = {"AT_LEVEL": level, "AT_URL": url,
                         "AT_SESSION_DIR": str(session_dir),
                         "AT_DOMAIN": domain, "AT_WNCLI": str(wncli),
                         "AT_CASE_ID": "_RESET", "AT_OUT": str(reset_dir)}
            ok, detail = run_cleanup(project, cleanup_script, reset_dir,
                                     reset_env, args.timeout, "套件")
            cleanup_state = "ok" if ok else "failed"
            if not ok:
                batch_cleanup_note = (
                    "整批 blocked：套件级 cleanup.py 失败（%s）；证据见 "
                    "../_reset/summary.txt" % detail)
                print("at-run: --reset 清理失败（%s），全批记 blocked（不是红）"
                      % detail, file=sys.stderr)

    results = []
    actuals = {}
    not_run = []
    stopped = False
    for case in selected:
        case_id = case["fields"]["caseID"]
        if stopped:
            not_run.append(case_id)
            continue
        evidence = run_dir / "cases" / case_id
        evidence.mkdir(parents=True, exist_ok=True)
        entry = {"caseID": case_id, "status": "blocked", "duration_ms": 0,
                 "evidence_dir": rel(evidence, project), "diff": [],
                 "evidence": []}
        note = ""
        repro = ('python "%s" --dir "%s" --instance %s --domain %s --only %s'
                 % (Path(__file__).resolve(), project, level, domain, case_id))

        if batch_cleanup_note:
            reset_summary = run_dir / "cases" / "_reset" / "summary.txt"
            if reset_summary.is_file():
                write_text(evidence / "cleanup.txt",
                           read_text(reset_summary))
            write_summary(evidence, case_id, "blocked", 0, [],
                          batch_cleanup_note, repro, None, level, [])
            entry["evidence"] = evidence_files(evidence)
            results.append(entry)
            print("at-run: %s blocked —— %s" % (case_id, batch_cleanup_note),
                  file=sys.stderr)
            continue

        script = project / root / "scripts" / ("%s.py" % case_id)
        expected = None
        if not script.is_file():
            note = "脚本未实现：缺 %s" % rel(script, project)
        else:
            try:
                expected, missing = load_expected(
                    project, case, case["fields"].get("expected", ""), root)
            except EnvError as e:
                # 坏的期望文件/基线是项目资产问题：记 blocked 并继续跑其余用例，
                # 不整体中断（失败隔离），也不把它伪装成红/绿。
                missing = "期望值读取失败：%s" % e
            if missing and not args.record:
                note = missing
            elif missing:
                # 录制模式：没有期望正是要录的场景（基线还没建就是这条路）；
                # 原因留在证据里，不静默丢掉"为什么这次没比较"。
                write_text(evidence / "note.txt",
                           "录制模式：本次没有可比的期望（%s）\n" % missing)
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

        # 用例级清理（ATESET-05）：非幂等用例在执行前定点清理；
        # 清理失败 → blocked（不是红），证据落 evidence/clean/。
        clean_script = (project / root / "scripts"
                        / ("%s%s" % (case_id, CASE_CLEAN_SUFFIX)))
        if clean_script.is_file():
            clean_env = dict(env_extra)
            clean_env["AT_OUT"] = str(evidence / "clean")
            clean_ok, clean_detail = run_cleanup(
                project, clean_script, evidence / "clean", clean_env,
                args.timeout, "用例 %s " % case_id)
            if not clean_ok:
                note = ("用例级清理失败：%s（%s）；证据见 clean/summary.txt"
                        % (clean_detail, rel(clean_script, project)))
                write_text(evidence / "note.txt", note + "\n")
                write_summary(evidence, case_id, "blocked", 0, [], note, repro,
                              None, level, [])
                entry["evidence"] = evidence_files(evidence)
                results.append(entry)
                print("at-run: %s blocked —— %s" % (case_id, note),
                      file=sys.stderr)
                continue

        write_text(evidence / "cmd.txt",
                   "\n".join([sys.executable, str(script)]) + "\n")
        write_text(evidence / "env.txt",
                   "\n".join("%s=%s" % (k, env_extra[k]) for k in sorted(env_extra))
                   + "\n")
        # $t0 / $t1：runner 在用例开始前、结束后各取一次（脚本的本地时钟不参与）；
        # 401 重跑时重置，时间区间只覆盖真正产出数据的那一次。
        first_t0 = None
        t0 = t1 = int(time.time() * 1000)
        clock = {"t0": t0, "t1": t1}
        retried_auth = False
        while True:
            attempt = run_case_script(script, project, env_extra, args.timeout)
            if first_t0 is None:
                first_t0 = attempt["t0"]
            t0, t1 = attempt["t0"], attempt["t1"]
            clock = {"t0": t0, "t1": t1}
            write_text(evidence / "stdout.txt", attempt["stdout"])
            write_text(evidence / "stderr.txt", attempt["stderr"])
            if attempt["timeout"]:
                status, diff, note, actual = (
                    "failed", [], "超时（%.0fs）已被终止" % args.timeout, None)
            elif attempt["oserror"]:
                status, diff, note, actual = (
                    "failed", [], "脚本启动失败：%s" % attempt["oserror"], None)
            else:
                status, diff, note, actual = judge(
                    attempt["rc"], attempt["stdout"], expected, evidence,
                    clock, record=args.record)
            if status == "passed" or not auth_failure(
                    attempt["rc"], attempt["stdout"], attempt["stderr"]):
                break
            # 认证失败（wn-cli 退出码 3）：重登一次并重跑该例；仍失败记 failed
            if retried_auth:
                status, diff = "failed", []
                note = ("重新登录后仍认证失败（wn-cli 退出码 3）：%s"
                        % (note or "脚本未给出原因"))
                break
            if auth_login_failed:
                status, diff = "failed", []
                note = ("会话已失效（wn-cli 退出码 3），本批自动重登已失败：%s"
                        % auth_login_failed)
                break
            # 首次失败的输出另存，重跑覆盖 stdout.txt / stderr.txt 也不丢证据
            write_text(evidence / "stdout.attempt1.txt", attempt["stdout"])
            write_text(evidence / "stderr.attempt1.txt", attempt["stderr"])
            print("at-run: %s 认证失败（wn-cli 退出码 3），重新登录一次并重跑"
                  % case_id, file=sys.stderr)
            ok, detail = relogin()
            if not ok:
                auth_login_failed = detail
                status, diff = "failed", []
                note = ("会话已失效（wn-cli 退出码 3），自动重新登录失败：%s"
                        % detail)
                break
            session_created = True
            retried_auth = True
            write_text(evidence / "relogin.txt",
                       "第 1 次执行认证失败（wn-cli 退出码 3），已自动重新登录"
                       "并重跑该例；首次 stdout/stderr 见 stdout.attempt1.txt / "
                       "stderr.attempt1.txt。\n")
        entry["status"] = status
        entry["duration_ms"] = t1 - first_t0
        entry["diff"] = diff
        if args.record:
            entry["recorded"] = actual is not None
            if actual is not None:
                actuals[case_id] = actual
        write_text(evidence / "clock.txt", clock_text(clock))
        if note:
            write_text(evidence / "note.txt", note + "\n")
        notes = script_notes(evidence)
        if retried_auth and status == "passed":
            notes = ["[runner] 第 1 次执行认证失败（wn-cli 退出码 3），"
                     "已自动重新登录并重跑成功"] + notes
        write_summary(evidence, case_id, entry["status"], entry["duration_ms"],
                      entry["diff"], note, repro, clock, level, notes,
                      record=args.record)
        entry["evidence"] = evidence_files(evidence)
        results.append(entry)
        line = "at-run: %s %s（%dms）" % (
            case_id,
            "recorded" if args.record and entry["status"] == "passed"
            else entry["status"],
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
              "timestamp": started.isoformat(timespec="seconds"),
              "instance": level,
              "instance_source": instance_source,
              "reset": bool(args.reset),
              "cleanup": cleanup_state,
              "total": len(results),
              "passed": passed,
              "failed": failed,
              "blocked": blocked,
              "cases": results}

    actual_path = None
    actual_doc = None
    if args.record:
        actual_path = run_dir / "actual.json"
        actual_doc = {"_meta": {"schema": ACTUAL_SCHEMA,
                                "timestamp": report["timestamp"],
                                "instance": level,
                                "root": root,
                                "report": rel(report_path, project),
                                "recorded": len(actuals),
                                "unrecorded": [r["caseID"] for r in results
                                               if r["caseID"] not in actuals]}}
        actual_doc.update(actuals)
        # mode 标在报告上：录制跑出来的 passed 只代表脚本契约通过，不是验收绿。
        report["mode"] = "record"
        report["actual"] = rel(actual_path, project)

    write_json(report_path, report)
    write_json(latest_path, report)
    if actual_path is not None:
        write_json(actual_path, actual_doc)

    # 报告落盘之后再归档旧报告（本次报告 mtime 是新的，at-gc 会跳过它）；
    # 归档失败只警告，不改判定与退出码。
    archived = 0
    if not args.no_gc:
        archived = run_gc(project, root)

    exit_code = EXIT_OK if failed == 0 else EXIT_USAGE
    summary = {"ok": failed == 0, "root": root,
               "instance": level, "test_domain": domain,
               "init_template": init_template,
               "instance_source": instance_source, "session": se,
               "session_created": session_created,
               "reset": bool(args.reset), "cleanup": cleanup_state,
               "total": len(results), "passed": passed, "failed": failed,
               "blocked": blocked, "skipped": len(not_run),
               "archived": archived,
               "report": str(report_path.resolve()),
               "latest": str(latest_path.resolve()), "exit": exit_code}
    if args.record:
        summary["mode"] = "record"
        summary["recorded"] = len(actuals)
        summary["actual"] = str(actual_path.resolve())
    emit(summary)
    if args.record:
        print("at-run: %s —— 录制 %d 例 actual（%d 例未录到）→ %s；"
              "本模式不比较期望，绿以普通 at-run 为准"
              % (level, len(actuals), len(results) - len(actuals),
                 rel(actual_path, project)), file=sys.stderr)
    else:
        print("at-run: %s —— %d 例：%d 绿 / %d 红 / %d blocked%s（报告 %s）"
              % (level, len(results), passed, failed, blocked,
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
    ap.add_argument("--record", action="store_true",
                    help="跑全套并另写 <root>/report/<ts>/actual.json（供 "
                         "at-baseline 建基线；本模式不比较期望）")
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
    ap.add_argument("--creds", metavar="FILE",
                    help="凭据文件（缺省读 atest.json 的 credentials_file，再缺省 "
                         ".at/credentials.json）；只在需要自动登录时读，"
                         '形状 {"usr":"…","passwd":"…"}，该文件不进 git')
    ap.add_argument("--only", action="append", default=[], metavar="IDS",
                    help="只跑这些 CASE-ID（逗号分隔，可重复）")
    ap.add_argument("--stop-on-fail", action="store_true",
                    help="遇到第一个红灯即停（blocked 不算红）")
    ap.add_argument("--reset", action="store_true",
                    help="整批执行前先跑 <root>/cleanup.py（套件级清理；"
                         "失败则全批记 blocked，不算红）")
    ap.add_argument("--no-gc", action="store_true",
                    help="跑完不自动调用 bin/at-gc 归档旧报告（缺省会自动归档）")
    ap.add_argument("--timeout", type=float, default=120.0, metavar="SEC",
                    help="单个用例脚本 / 清理脚本超时秒数（默认 120）")
    args = ap.parse_args(argv)

    if args.timeout <= 0:
        emit({"ok": False, "error": "--timeout 必须大于 0"})
        return EXIT_USAGE
    if args.check and (args.only or args.stop_on_fail or args.reset
                       or args.record or args.no_gc):
        emit({"ok": False,
              "error": "--check 不接受 --only / --stop-on-fail / --reset / "
                       "--record / --no-gc"})
        return EXIT_USAGE
    if not args.check and args.paths:
        emit({"ok": False,
              "error": "位置参数只用于 --check；执行请用 --only"})
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
            return run_check(project, args.paths, root, config)
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
