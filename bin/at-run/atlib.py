#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""atlib — ATEST 用例脚本薄库（ATESET-04 / 05）

脚本只"执行 + 取证"，断言交给 at-run（按用例 md 的 `## 期望` 统一比较）。
本库把每个脚本都会重写的样板收成六个函数：

    wn(cmd)       经 wn-cli 执行一条 Walnut 命令（自动带级别 / 会话 / 端点）
    http(...)     用标准库 urllib 真打 HTTP 接口（ATESET-09；不跟随重定向）
    fixture(...)  用 Walnut 自己的 `app init` 把 <root>/atest.init 铺进域（ATESET-05）
    snap(path)    读对象 JSON，落 <AT_OUT>/objects/，返回 dict
    emit(actual)  输出契约要求的最后一行 JSON，并退出 0
    note(text)    往 <AT_OUT>/summary.txt 追加一行（runner 重写 summary 时保留）

环境变量由 at-run 注入（契约见 bin/at-run/README.md）：
    AT_LEVEL / AT_URL / AT_SESSION_DIR / AT_DOMAIN / AT_WNCLI / AT_CASE_ID / AT_OUT

约定：

- 只依赖标准库与上述环境变量；`from atlib import ...` 能成立是因为 runner 把
  bin/at-run 放进了子进程的 PYTHONPATH（手工跑脚本时自己设）；
- 任何一步环境不可用（wn-cli 失败、snap 拿不到对象、缺变量）→ `SystemExit(2)`，
  原因同时写进 summary.txt：runner 记 **blocked**，绝不伪装成红/绿；
- `emit()` 打印后直接退出 0——保证 stdout 最后一行就是结果 JSON，后面别再加输出。

示例（`bin/at-init/tmpl/CASE-001.py` 就是可直接跑的版本）：

    from atlib import wn, snap, emit, note

    note("创建 ~/atest-dir（-upsert，可重复执行）")
    wn("o ~ @create atest-dir -race DIR -upsert")
    obj = snap("~/atest-dir")
    emit({"nm": obj["nm"], "race": obj["race"]})

夹具（ATESET-05 / 15，静态布局必须写进模板，不在用例里现拼）：

    from atlib import fixture, snap, emit

    fixture()                       # 新布局：init/<suite>.init；旧布局：atest.init
    obj = snap("~/.atest/demo")     # 模板铺好的对象
    emit({"atest-owner": obj.get("atest-owner")})

  - 存在 `<root>/init/` → **目录即 suite**：suite 名 = 用例 md 相对 `cases/` 的
    目录名（由 AT_CASE_ID 反查），入口是 `init/<suite>.init`，可写
    `@INCLUDE common.init` 组合公共片段；整个 `init/**/*.init` 会按原相对路径
    上传到 `~/.atest/init/`，再执行 `app init -conf ~/.atest/init/<suite>.init`。
  - 否则走旧布局：单个 `<root>/atest.init` 上传成 `~/.atest/init/<name>.init`。

夹具由 `app init` 执行：模板里可以使用它的全部指令
（@DIR / @FILE / @API / @HOME / @ENV / @INCLUDE / %TMPL / %COPY / %EXPLAIN）。

示例（ATESET-09，真 HTTP，不绕命令通道）：

    from atlib import http, emit

    st, hdrs, body = http("/api/demo/ping")     # 302/403 也照常返回
    emit({"status": st, "ctype": hdrs.get("Content-Type", "")})
"""

import json
import os
import re
import subprocess
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path, PurePosixPath

REQUIRED_ENV = ("AT_LEVEL", "AT_URL", "AT_SESSION_DIR", "AT_DOMAIN",
                "AT_WNCLI", "AT_OUT")

SUMMARY_NAME = "summary.txt"
OBJECTS_DIR = "objects"
HTTP_DIR = "http"

CONFIG_NAME = "atest.json"
ROOT_DEFAULT = "atest"
INIT_NAME = "atest.init"
INIT_DIR_NAME = "init"
VARS_NAME = "vars.json"

# 远程工具目录：模板与变量每次都重写，避免旧文件残留（ATESET-05 T1 约定）
REMOTE_INIT_DIR = "~/.atest/init"
REMOTE_VARS = "~/.atest/vars.json"

# 夹具名就是上传后的模板文件名，只允许安全字符
NAME_RE = re.compile(r"^[A-Za-z0-9._-]+$")
# 解析 @INCLUDE 只认行首指令（`@INCLUDE <相对路径>`），与 at-run 的哈希公式一致
INCLUDE_RE = re.compile(r"^@INCLUDE\s+(\S+)\s*$", re.I)


def note(text):
    """往 summary.txt 追加一行（[script] 前缀）；同时回显到 stderr 留痕。"""
    line = "[script] %s" % str(text).replace("\r", " ").replace("\n", " ")
    print("atlib: " + line, file=sys.stderr)
    out = os.environ.get("AT_OUT")
    if not out:
        return
    try:
        path = Path(out) / SUMMARY_NAME
        path.parent.mkdir(parents=True, exist_ok=True)
        with open(str(path), "a", encoding="utf-8", newline="\n") as f:
            f.write(line + "\n")
    except OSError:
        # 证据写不进去不该让用例本身崩掉；stderr 里已经留了痕
        pass


def _die(message):
    note(message)
    raise SystemExit(2)


def _env(name):
    value = os.environ.get(name)
    if not value:
        _die("缺少环境变量 %s：本脚本只能由 at-run 执行（见 bin/at-run/README.md）"
             % name)
    return value


def _session_name():
    return "%s-%s-00" % (_env("AT_LEVEL"), _env("AT_DOMAIN"))


def _child_env():
    """子进程统一 UTF-8：模板/变量里有中文，Windows 默认 cp936 会拆坏。"""
    env = os.environ.copy()
    env["PYTHONUTF8"] = "1"
    env["PYTHONIOENCODING"] = "utf-8"
    env["PYTHONDONTWRITEBYTECODE"] = "1"
    return env


def _call_wncli(extra, stdin_text=None, timeout=90):
    """跑一个 wn-cli 子命令，返回 (rc, stdout, stderr)，不抛 SystemExit。"""
    argv = [sys.executable, _env("AT_WNCLI"),
            "--level", _env("AT_LEVEL"),
            "--se", _session_name(),
            "--session-dir", _env("AT_SESSION_DIR"),
            "--url", _env("AT_URL")] + [str(x) for x in extra]
    data = None if stdin_text is None else stdin_text.encode("utf-8")
    try:
        proc = subprocess.run(argv, input=data, capture_output=True,
                              env=_child_env(), timeout=timeout)
    except subprocess.TimeoutExpired:
        return None, "", "timeout"
    except OSError as e:
        return None, "", str(e)
    return (proc.returncode,
            proc.stdout.decode("utf-8", "replace"),
            proc.stderr.decode("utf-8", "replace"))


def _run_wncli(extra, stdout_label=None, stdin_text=None, timeout=90):
    """经 wn-cli 跑一个子命令；成功返回 stdout，失败 SystemExit(2)。

    argv 传参、不经 shell。stdin_text 用于 `obj save-text`（模板可能较大，
    也避免命令行长度与引号问题）。
    """
    label = stdout_label or " ".join(str(x) for x in extra)
    rc, out, err = _call_wncli(extra, stdin_text=stdin_text, timeout=timeout)
    if rc is None:
        if err == "timeout":
            _die("wn-cli 超时（%ss）：%s" % (timeout, label))
        _die("wn-cli 启动失败：%s（命令：%s）" % (err, label))
    if rc != 0:
        detail = (err.strip() or out.strip() or "（无输出）")
        detail = detail.replace("\r", " ").replace("\n", " ")[:500]
        _die("wn-cli 退出码 %d：%s（命令：%s）" % (rc, detail, label))
    return out


def wn(command, timeout=90):
    """经 wn-cli 跑一条命令；成功返回 stdout，失败 SystemExit(2)。

    argv 传参、不经 shell；级别 / 会话 / 端点全部来自 runner 注入的环境变量，
    脚本里**不要**手写 --level 或地址（防 L1/L2 混跑）。
    """
    print("atlib: wn %s" % command, file=sys.stderr)
    return _run_wncli(["cmd", str(command)], stdout_label=str(command),
                      timeout=timeout)


class Headers(dict):
    """响应头：键保持服务端原样，`get()` 大小写不敏感。

    HTTP 头名本来就不区分大小写（`Content-Type` / `content-type` 是一回事），
    脚本不该为服务端写法猜大小写。它仍是 dict，可直接遍历 / 序列化。
    """

    def get(self, name, default=None):
        want = str(name).lower()
        for key, value in self.items():
            if key.lower() == want:
                return value
        return default


class _NoRedirect(urllib.request.HTTPRedirectHandler):
    """302 是被测行为（ATESET-09 CASE-203），不能让 urllib 自己跟过去。"""

    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


def _decode_body(raw, content_type):
    """按响应头 charset 解码；缺省 / 不认识 / 解不动都退回 UTF-8 替换。"""
    charset = "utf-8"
    m = re.search(r"charset=([A-Za-z0-9_.-]+)", content_type or "", re.I)
    if m:
        charset = m.group(1)
    try:
        return raw.decode(charset)
    except (LookupError, UnicodeDecodeError):
        return raw.decode("utf-8", "replace")


def _http_evidence(method, url, status, headers, text):
    """把原始响应落到 <AT_OUT>/http/（状态行 + 响应头 + body），供复核。"""
    out = os.environ.get("AT_OUT")
    if not out:
        return
    folder = Path(out) / HTTP_DIR
    label = re.sub(r"[^A-Za-z0-9._-]+", "_", url.split("://", 1)[-1])[:80]
    try:
        folder.mkdir(parents=True, exist_ok=True)
        seq = len(list(folder.glob("*.txt"))) + 1
        target = folder / ("%02d-%s-%s.txt"
                           % (seq, method, label.strip("_") or "root"))
        with open(str(target), "w", encoding="utf-8", newline="\n") as f:
            f.write("%s %s -> %d\n" % (method, url, status))
            for key in headers:
                f.write("%s: %s\n" % (key, headers[key]))
            f.write("\n")
            f.write(text)
    except OSError:
        # 证据写不进去不该让用例本身崩掉；stderr 里已经留了痕
        pass


def http(path, method="GET", headers=None, data=None, timeout=30):
    """真打一个 HTTP 接口，返回 `(status, headers, body_text)`。

    - `path` 形如 `/api/<域>/<接口>`，拼在 `AT_URL` 上；也接受完整
      `http(s)://` URL；
    - **4xx/5xx 不抛**：403 / 302 都是被测行为，照常返回状态码与响应体；
    - **不跟随重定向**：302 原样返回，`Location` 在 headers 里；
    - 连不上 / 超时 / URL 不合法 → `SystemExit(2)`（runner 记 blocked）：
      这是"实例挂了"，与"接口回了错误码"是两件事；
    - 每次调用的原始响应落 `<AT_OUT>/http/`（状态行 + 全部响应头 + body），
      与 `snap()` 一样属于取证，不是断言；
    - `headers` 是 dict；`data` 传 dict 走 url-encoded 表单，传 str / bytes
      原样发送，None 表示无请求体。

    断言不在这里做：脚本只把可观察值 `emit()` 出去，判定交给 runner。
    """
    base = _env("AT_URL").rstrip("/")
    raw = str(path)
    if re.match(r"^https?://", raw, re.I):
        url = raw
    else:
        url = base + ("" if raw.startswith("/") else "/") + raw
    verb = str(method or "GET").upper()

    body = None
    if data is not None:
        if isinstance(data, (dict, list)):
            body = urllib.parse.urlencode(data).encode("ascii")
        elif isinstance(data, str):
            body = data.encode("utf-8")
        else:
            body = data

    req = urllib.request.Request(url, data=body,
                                 headers=dict(headers or {}), method=verb)
    opener = urllib.request.build_opener(_NoRedirect)
    try:
        with opener.open(req, timeout=timeout) as resp:
            status = resp.status
            hdrs = Headers(resp.headers.items())
            raw_body = resp.read()
    # HTTPError 是 URLError 的子类，必须先接：403/404/500 是响应不是故障
    except urllib.error.HTTPError as e:
        status = e.code
        hdrs = Headers(e.headers.items()) if e.headers else Headers()
        raw_body = e.read()
    except (OSError, ValueError) as e:
        _die("http %s %s 失败（端点不可用？）：%s" % (verb, url, e))

    text = _decode_body(raw_body, hdrs.get("Content-Type"))
    print("atlib: http %s %s -> %d" % (verb, url, status), file=sys.stderr)
    _http_evidence(verb, url, status, hdrs, text)
    return status, hdrs, text


def _load_config():
    """项目根 atest.json（可选）：fixture 只用 root / init_template。"""
    path = Path(CONFIG_NAME)
    if not path.is_file():
        return {}
    try:
        data = json.loads(path.read_text(encoding="utf-8-sig"))
    except (ValueError, OSError) as e:
        _die("%s 不是合法 JSON（%s）：fixture() 无法定位夹具模板" % (path, e))
        return {}
    if not isinstance(data, dict):
        _die("%s 格式不对：顶层应为对象（version / root / init_template ...）"
             % path)
        return {}
    return data


def _fixture_root(root, config):
    """夹具根目录名：参数 > atest.json 的 root > atest；非法即 blocked。"""
    root_name = str(root or config.get("root") or ROOT_DEFAULT)
    if root_name in (".", "..") or not NAME_RE.match(root_name):
        _die("夹具根目录 %r 不合法：只允许字母/数字/._-，且不能是 . / .."
             % (root_name,))
    return root_name


def _init_template_path(root_name, config):
    """旧布局的夹具模板：atest.json 的 init_template > <root>/atest.init。"""
    raw = config.get("init_template") or ("%s/%s" % (root_name, INIT_NAME))
    path = Path(raw)
    if not path.is_absolute():
        path = Path.cwd() / path
    return path


def _safe_rel(name, what):
    """相对路径名（可含 `/` 分隔的层级）：逐段校验，拒绝 . / .. 与怪字符。"""
    parts = [x for x in str(name).split("/") if x]
    if not parts or any(x in (".", "..") or not NAME_RE.match(x)
                        for x in parts):
        _die("%s %r 不合法：只允许字母/数字/._-（可用 / 分层）" % (what, name))
    return "/".join(parts)


def _case_suite(root_name):
    """从 AT_CASE_ID 反查用例 md，取其相对 `cases/` 的目录名（目录即 suite）。"""
    base = Path.cwd() / root_name / "cases"
    if not base.is_dir():
        _die("找不到用例目录 %s：fixture() 无法判断 suite" % base)
    case_id = os.environ.get("AT_CASE_ID") or ""
    if case_id:
        hits = sorted(x for x in base.rglob("%s.md" % case_id) if x.is_file())
        if len(hits) > 1:
            _die("用例 %s 有多份 md（%s）：目录即 suite 要求 caseID 全局唯一"
                 % (case_id, ", ".join(str(x) for x in hits)))
        if hits:
            parent = hits[0].parent.relative_to(base).as_posix()
            return "" if parent == "." else parent
    _die("定位不到用例 md（AT_CASE_ID=%r，%s）：新布局要求 "
         "cases/<suite>/<CASE-ID>.md" % (case_id, base))


def _resolve_includes(entry):
    """递归解析 `@INCLUDE`（相对入口模板目录，与 app init 的 oHome 一致）。

    只做安全检查：循环 include / 缺片段 → `SystemExit(2)`（runner 记 blocked），
    不静默地把夹具铺一半。
    """
    base = entry.parent
    stack = []
    done = set()

    def walk(path):
        key = str(path.resolve())
        if key in stack:
            _die("循环 @INCLUDE：%s" % " -> ".join(stack + [key]))
        if key in done:
            return
        stack.append(key)
        text = _read_text_file(path, "被包含的夹具片段")
        for lineno, line in enumerate(text.splitlines(), 1):
            m = INCLUDE_RE.match(line.strip())
            if not m:
                continue
            target = base / m.group(1)
            if not target.is_file():
                _die("第 %d 行的 @INCLUDE 找不到文件 %s（相对 %s）"
                     % (lineno, m.group(1), base))
            walk(target)
        stack.pop()
        done.add(key)

    walk(entry)


def _upload_init_tree(folder, timeout):
    """把 `<root>/init/**/*.init` 按原相对路径上传到 `~/.atest/init/`。

    必须保持层级：`@INCLUDE` 的路径相对**入口模板**所在目录（app init 的
    oHome），片段与入口在同一棵树里才解析得到。
    """
    files = sorted(x for x in folder.rglob("*.init") if x.is_file())
    if not files:
        _die("%s 下没有 *.init：新布局要求至少 init/<suite>.init" % folder)
    rels = [x.relative_to(folder).as_posix() for x in files]
    wn("mkdir -p -noexists %s" % REMOTE_INIT_DIR, timeout=timeout)
    dirs = sorted({str(PurePosixPath(r).parent) for r in rels
                   if str(PurePosixPath(r).parent) != "."})
    for sub in dirs:
        wn("mkdir -p -noexists %s"
           % _cmd_arg("%s/%s" % (REMOTE_INIT_DIR, sub)), timeout=timeout)
    for relp, path in zip(rels, files):
        _save_text("%s/%s" % (REMOTE_INIT_DIR, relp),
                   _read_text_file(path, "夹具模板"), timeout)
    return rels


def fixture(root=None, name=None, vars=None, timeout=90):
    """用 Walnut 自己的 `app init` 把静态夹具铺进域。

    两套布局（ATESET-15）：

    - **新布局**（存在 `<root>/init/`，目录即 suite）：suite = 用例 md 相对
      `cases/` 的目录名（也可用 `name` 显式指定），入口
      `init/<suite>.init`；把 `init/**/*.init` 按原相对路径上传到
      `~/.atest/init/`，再执行
      `app init -conf ~/.atest/init/<suite>.init -script off`。
      入口可写 `@INCLUDE common.init` 组合公共片段。
    - **旧布局**（没有 `init/`）：单个 `<root>/atest.init`（或 atest.json 的
      `init_template`）上传成 `~/.atest/init/<name>.init` 再执行。

    公共步骤：

    1. 读 `<root>/atest.json` 的 `root` / `init_template`，定位模板与
       `<root>/vars.json`；
    2. 模板（新布局是整个 `init/` 树）`obj save-text` 到 `~/.atest/init/`
       （覆盖写）；
    3. 变量 = `vars` 参数 + `<root>/vars.json`（项目同名键优先）；非空则
       再 `obj save-text` 到 `~/.atest/vars.json`，并作为 `-vars` 传给 app init；
    4. 执行 `app init -conf <入口> -script off`（`-script` 默认 **off**，
       脚本测试要收尾脚本时自己调）；
    5. 幂等由 `app init` 自身保证：目标文件已存在且内容非空时跳过写入，
       目录 / 元数据按模板合并，同一模板连跑两次安全。

    - `root` 缺省读 `atest.json` 的 `root`（再缺省 `atest`）；`name` 缺省为
      suite 名（新布局）或模板文件名（旧布局，`atest.init` → `atest`）；
    - 失败（模板缺失 / suite 入口缺失 / 循环 @INCLUDE / vars.json 非法 /
      上传失败 / `app init` 非 0）→ `SystemExit(2)`：runner 记 **blocked**，
      并带上原始输出与"缺哪个文件"。

    返回值形状保持 `{"name", "template", "suite", "vars", "entries",
    "counts", "uploaded"}`（`entries` / `counts` 由 `app init` 的输出统计，
    仅供日志与调用方参考；`suite` 在旧布局为 None）。
    """
    if vars is not None and not isinstance(vars, dict):
        _die("fixture(vars=...) 必须是 dict（现在是 %r）" % (vars,))

    config = _load_config()
    root_name = _fixture_root(root, config)
    vars_path = Path.cwd() / root_name / VARS_NAME
    init_folder = Path.cwd() / root_name / INIT_DIR_NAME

    suite = None
    uploaded = []
    if init_folder.is_dir():
        suite = _safe_rel(name or _case_suite(root_name), "suite 名")
        template = init_folder / ("%s.init" % suite)
        if not template.is_file():
            _die("缺 suite 入口 %s（suite %r）：新布局要求 init/<suite>.init；"
                 "通常写成 `@INCLUDE common.init` + 本 suite 专属项"
                 % (template, suite))
        fixture_name = suite
        _resolve_includes(template)
        uploaded = _upload_init_tree(init_folder, timeout)
        remote_init = "%s/%s.init" % (REMOTE_INIT_DIR, suite)
        print("atlib: fixture %s：suite=%s，上传 %d 份 init 片段（入口 %s）"
              % (fixture_name, suite, len(uploaded), template.name),
              file=sys.stderr)
    else:
        init_path = _init_template_path(root_name, config)
        fixture_name = str(name or init_path.stem)
        if not fixture_name or fixture_name in (".", "..") \
                or not NAME_RE.match(fixture_name):
            _die("夹具名 %r 不合法：只允许 %s（且不能是 . / ..）"
                 % (fixture_name, NAME_RE.pattern))
        if not init_path.is_file():
            _die("夹具模板不存在：%s（先照 bin/at-init/tmpl/atest.init 写一份）"
                 % init_path)
        template = init_path
        text = _read_text_file(init_path, "夹具模板")

        print("atlib: fixture %s：模板=%s，变量键=%s"
              % (fixture_name, init_path.name,
                 ", ".join(sorted(vars or {})) if vars else "（无）"),
              file=sys.stderr)

        # 模板 / 变量都上传到工具目录；app init 从对象树里读模板
        wn("mkdir -p -noexists %s" % REMOTE_INIT_DIR, timeout=timeout)
        remote_init = "%s/%s.init" % (REMOTE_INIT_DIR, fixture_name)
        _save_text(remote_init, text, timeout)

    merged = dict(vars or {})
    project_vars = {}
    if vars_path.is_file():
        raw = _read_text_file(vars_path, "夹具变量文件")
        try:
            project_vars = json.loads(raw)
        except ValueError as e:
            _die("%s 不是合法 JSON（%s）：vars.json 必须是对象" % (vars_path, e))
        if not isinstance(project_vars, dict):
            _die("%s 格式不对：顶层应为对象（{\"key\": \"value\"}）" % vars_path)
        merged.update(project_vars)

    vars_json = None
    if merged:
        vars_json = json.dumps(merged, ensure_ascii=False)
        _save_text(REMOTE_VARS, vars_json, timeout)

    cmd = "app init -conf %s -script off" % _cmd_arg(remote_init)
    if vars_json is not None:
        cmd += " -vars %s" % _cmd_arg(vars_json)

    rc, out, err = _call_wncli(["cmd", cmd], timeout=timeout)
    if rc is None:
        if err == "timeout":
            _die("app init 超时（%ss）：%s" % (timeout, cmd))
        _die("app init 启动失败：%s（命令：%s）" % (err, cmd))
    if rc != 0:
        raw = "\n".join(x for x in (out.strip(), err.strip()) if x)
        if raw:
            print("atlib: app init 原始输出：\n%s" % raw, file=sys.stderr)
        detail = raw.replace("\r", " ").replace("\n", " ")[:800] or "（无输出）"
        _die("app init 退出码 %d：%s（命令：%s）" % (rc, detail, cmd))

    entries, counts = _scan_counts(out)
    note("fixture %s：模板=%s，app init 成功（%d 条，DIR %d / FILE %d），变量键=%s"
         % (fixture_name, template.name, entries,
            counts["DIR"], counts["FILE"],
            ", ".join(sorted(merged)) if merged else "（无）"))
    return {"name": fixture_name, "template": str(template), "suite": suite,
            "vars": sorted(merged), "entries": entries,
            "counts": counts, "uploaded": len(uploaded)}


def _read_text_file(path, what):
    try:
        return path.read_text(encoding="utf-8-sig")
    except UnicodeDecodeError as e:
        _die("%s 不是 UTF-8 编码（%s）：%s 必须 UTF-8 无 BOM" % (path, e, what))
    except OSError as e:
        _die("读不了 %s：%s（%s）" % (what, path, e))
    return ""


def _cmd_arg(text):
    """命令通道的参数：无空白原样；有空白用单引号（含单引号则拒绝）。"""
    text = str(text)
    if not re.search(r"[\s'\"]", text):
        return text
    if "'" in text:
        _die("不支持含单引号的路径/参数：%r（JSON 里不要出现单引号）" % text)
    return "'%s'" % text


def _save_text(path, content, timeout):
    _run_wncli(["obj", "save-text", path], stdin_text=content or "",
               timeout=timeout)


def _scan_counts(text):
    """从 `app init` 的输出里数条目（`@DIR xxx` / `@FILE xxx` 行）。

    只做统计，用来保持返回值形状与日志可读；真正的解析在 `app init` 里。
    被 `@INCLUDE` 展开的条目带 `>> ` 前缀（入口模板的 outPrefix），一并数进去。
    """
    counts = {"DIR": 0, "FILE": 0}
    entries = 0
    for line in text.splitlines():
        m = re.match(r"^(?:>+\s*)?@([A-Za-z]+)\s", line.strip())
        if not m:
            continue
        entries += 1
        typ = m.group(1).upper()
        if typ in counts:
            counts[typ] += 1
    return entries, counts


def _object_name(path, name):
    """证据文件名：路径里的 / 换成 __（T1 约定的命名）。"""
    if name:
        return str(name).strip().replace("\\", "/").replace("/", "__")
    raw = str(path).strip().replace("\\", "/")
    raw = raw[2:] if raw.startswith("./") else raw
    return raw.replace("/", "__") or "root"


def snap(path, name=None):
    """读一个对象的 JSON 快照，落 <AT_OUT>/objects/<路径>.json，返回 dict。

    取不到对象 = 环境/数据问题：抛 SystemExit(2)（runner 记 blocked），不吞。
    """
    text = wn("o %s @json -cqn" % path).strip()
    line = text.splitlines()[-1].strip() if text else ""
    try:
        obj = json.loads(line)
    except ValueError:
        _die("snap(%s) 返回的不是 JSON：%r" % (path, line[:200]))
        return {}
    target = Path(_env("AT_OUT")) / OBJECTS_DIR / (
        _object_name(path, name) + ".json")
    target.parent.mkdir(parents=True, exist_ok=True)
    with open(str(target), "w", encoding="utf-8", newline="\n") as f:
        f.write(json.dumps(obj, ensure_ascii=False, indent=2) + "\n")
    return obj


def emit(actual):
    """打印契约要求的结果 JSON 并退出 0（stdout 最后一行，之后不再输出）。"""
    try:
        line = json.dumps({"ok": True, "actual": actual}, ensure_ascii=False)
    except (TypeError, ValueError) as e:
        _die("emit 的 actual 不能序列化成 JSON：%s" % e)
        return
    print(line)
    sys.stdout.flush()
    raise SystemExit(0)
