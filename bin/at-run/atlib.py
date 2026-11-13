#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""atlib — ATEST 用例脚本薄库（ATESET-04 / 05）

脚本只"执行 + 取证"，断言交给 at-run（按用例 md 的 `## 期望` 统一比较）。
本库把每个脚本都会重写的样板收成五个函数：

    wn(cmd)       经 wn-cli 执行一条 Walnut 命令（自动带级别 / 会话 / 端点）
    fixture(...)  用本地最小渲染器把 <root>/atest.init 铺进域（ATESET-05）
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

夹具（ATESET-05，静态布局必须写进模板，不在用例里现拼）：

    from atlib import fixture, snap, emit

    fixture()                       # <root>/atest.init + <root>/vars.json
    obj = snap("~/.atest/demo")     # 模板铺好的对象
    emit({"atest-owner": obj.get("atest-owner")})
"""

import json
import os
import re
import subprocess
import sys
from pathlib import Path

REQUIRED_ENV = ("AT_LEVEL", "AT_URL", "AT_SESSION_DIR", "AT_DOMAIN",
                "AT_WNCLI", "AT_OUT")

SUMMARY_NAME = "summary.txt"
OBJECTS_DIR = "objects"

CONFIG_NAME = "atest.json"
ROOT_DEFAULT = "atest"
INIT_NAME = "atest.init"
VARS_NAME = "vars.json"

# 远程工具目录：模板每次重写，避免旧模板残留（ATESET-05 T1 约定）
REMOTE_INIT_DIR = "~/.atest/init"

# v1 只支持内联内容块；未支持的写法在 _parse_fixture 里明确报错
NAME_RE = re.compile(r"^[A-Za-z0-9._-]+$")


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


def _fixture_paths(root, config):
    """返回 (模板路径, 变量文件路径)；root 为 None 时读 atest.json 的 root。"""
    if root:
        root_name = root
        raw = "%s/%s" % (root_name, INIT_NAME)
    else:
        root_name = config.get("root") or ROOT_DEFAULT
        raw = config.get("init_template") or ("%s/%s" % (root_name, INIT_NAME))
    init_path = Path(raw)
    if not init_path.is_absolute():
        init_path = Path.cwd() / init_path
    root_name = str(root_name)
    if root_name in (".", "..") or not re.match(r"^[A-Za-z0-9._-]+$",
                                                root_name):
        _die("夹具根目录 %r 不合法：只允许字母/数字/._-，且不能是 . / .."
             % (root_name,))
    vars_path = Path.cwd() / root_name / VARS_NAME
    return init_path, vars_path


def _read_text_file(path, what):
    try:
        return path.read_text(encoding="utf-8-sig")
    except UnicodeDecodeError as e:
        _die("%s 不是 UTF-8 编码（%s）：%s 必须 UTF-8 无 BOM" % (path, e, what))
    except OSError as e:
        _die("读不了 %s：%s（%s）" % (what, path, e))
    return ""


# ---------------------------------------------------------------- 夹具渲染器
#
# v1 只覆盖 ATEST 需要的子集（见 README）：
#   @DIR <path> ['quick meta'] [{meta json}]
#   @FILE <path> ...  +  %TMPL: / %COPY: / %EXPLAIN: ... %END%
# 其它写法显式报错，不静默忽略。

ENTRY_RE = re.compile(r"^@([A-Za-z]+)\b(.*)$")
CONTENT_RE = re.compile(
    r"^([%?])(COPY|TMPL|EXPLAIN)(\{.*\})?\s*([:>])\s*(.*)$", re.I)
VAR_RE = re.compile(r"\$\{([^{}]+)\}")


def _var_text(value):
    if value is None:
        return ""
    if value is True:
        return "true"
    if value is False:
        return "false"
    if isinstance(value, (dict, list)):
        return json.dumps(value, ensure_ascii=False)
    return str(value)


def _render(text, variables):
    """最小 TMPL：${var} / ${var?默认值}；没有变量且无默认值时保留原样。"""
    def repl(m):
        expr = m.group(1)
        name, sep, default = expr.partition("?")
        name = name.strip()
        if name in variables:
            return _var_text(variables[name])
        if sep:
            return default
        return m.group(0)

    return VAR_RE.sub(repl, text)


def _truthy(value):
    if isinstance(value, str):
        return value.strip().lower() not in ("", "0", "false", "no", "null")
    return bool(value)


def _parse_default(text):
    if text.startswith("="):
        text = text[1:]
    try:
        return json.loads(text)
    except ValueError:
        return text


def _resolve_explain(value, variables):
    """EXPLAIN 的最小语义：=key / =key?默认 / ==key / !=key。"""
    if isinstance(value, str):
        if value.startswith("=="):
            return _truthy(variables.get(value[2:].strip()))
        if value.startswith("!="):
            return not _truthy(variables.get(value[2:].strip()))
        if value.startswith("="):
            name, sep, default = value[1:].partition("?")
            name = name.strip()
            if name in variables:
                return variables[name]
            if sep:
                return _parse_default(default)
            return None
        return value
    if isinstance(value, dict):
        return {k: _resolve_explain(v, variables) for k, v in value.items()}
    if isinstance(value, list):
        return [_resolve_explain(v, variables) for v in value]
    return value


def _quick_meta(text, variables):
    """把 'i18n:xx/<fas-yy>/cross/json' 译成元数据（兼容旧模板的速记子集）。"""
    meta = {}
    for token in [x for x in _render(text, variables).split("/") if x]:
        if token.startswith("="):
            meta["tp"] = token[1:].strip()
        elif token.startswith("<") and token.endswith(">"):
            meta["icon"] = token[1:-1].strip()
        elif token == "cross":
            meta["http-cross-origin"] = "*"
        elif token == "json":
            meta["http-header-Content-Type"] = "text/json"
        elif token == "dynamic":
            meta["http-dynamic-header"] = True
        elif token == "hook":
            meta["run-with-hook"] = True
        elif re.match(r"^http\d{3}$", token):
            meta["http-resp-code"] = int(token[4:])
        else:
            # 旧约定：其余都当 title（i18n:xxx 原样）
            meta["title"] = token
    return meta


def _die_entry(line_no, message):
    _die("atest.init 第 %d 行：%s" % (line_no, message))


def _split_entry(rest, line_no):
    """拆出 (path, quick meta, 同行 JSON 文本)。"""
    rest = rest.strip()
    if not rest:
        _die_entry(line_no, "缺少条目路径")
    if rest[0] in "'\"":
        quote = rest[0]
        end = rest.find(quote, 1)
        if end < 0:
            _die_entry(line_no, "路径引号没有闭合")
        path = rest[1:end]
        rest = rest[end + 1:].strip()
    else:
        parts = rest.split(None, 1)
        path = parts[0]
        rest = parts[1].strip() if len(parts) > 1 else ""
    quick = ""
    if rest.startswith("'"):
        end = rest.find("'", 1)
        if end < 0:
            _die_entry(line_no, "quick meta 的引号没有闭合")
        quick = rest[1:end]
        rest = rest[end + 1:].strip()
    if rest and not rest.startswith("{"):
        _die_entry(line_no, "条目尾部无法解析（只支持 'quick meta' 与 {json}）：%s"
                   % rest)
    return path, quick, rest


def _normalize_path(path, line_no):
    path = path.strip()
    if not path:
        _die_entry(line_no, "路径为空")
    if path.startswith("./"):
        path = path[2:]
    if path.startswith("~") or path.startswith("/"):
        return path
    return "~/" + path


def _cmd_arg(text):
    """命令通道的参数：无空白原样；有空白用单引号（含单引号则拒绝）。"""
    text = str(text)
    if not re.search(r"[\s'\"]", text):
        return text
    if "'" in text:
        _die("v1 不支持含单引号的路径/参数：%r（请改用无单引号路径）" % text)
    return "'%s'" % text


def _object_of(path, timeout):
    rc, out, err = _call_wncli(["obj", "get", path], timeout=timeout)
    if rc is None:
        _die("检查对象 %s 失败：wn-cli %s" % (path, err))
    if rc == 0:
        try:
            data = json.loads(out)
        except ValueError as e:
            _die("读对象 %s 的返回不是 JSON：%s" % (path, e))
        data = data.get("data") if isinstance(data, dict) else None
        if not isinstance(data, dict):
            _die("读对象 %s 的返回缺 data：%r" % (path, out[:200]))
        return data
    detail = (err + out).lower()
    if "noexist" in detail or "no.exists" in detail:
        return None
    _die("检查对象 %s 失败（wn-cli %s）：%s"
         % (path, rc, (err.strip() or out.strip())[:300]))


def _ensure_parent(path, timeout):
    parent = path.rsplit("/", 1)[0] if "/" in path else ""
    if not parent or parent in ("~", "/"):
        return
    wn("mkdir -p -noexists %s" % _cmd_arg(parent), timeout=timeout)


def _save_text(path, content, timeout):
    _run_wncli(["obj", "save-text", path], stdin_text=content or "",
               timeout=timeout)


def _update_meta(fixture_name, path, meta, timeout):
    """元数据走 `o ... @update`：JSON 先上传成临时对象，再 cat 进 stdin。

    这样不用把 JSON 拼进命令文本，避免引号/空格把参数拆坏。
    """
    if not meta:
        return
    payload = json.dumps(meta, ensure_ascii=False)
    remote = "%s/%s.meta.json" % (REMOTE_INIT_DIR, fixture_name)
    _run_wncli(["obj", "save-text", remote], stdin_text=payload,
               timeout=timeout)
    wn("cat %s | o %s @update" % (remote, _cmd_arg(path)), timeout=timeout)


def _parse_fixture(text, variables):
    """解析 v1 模板；任何不支持的写法都在执行前报错。"""
    lines = text.splitlines()
    items = []
    i = 0
    while i < len(lines):
        raw = lines[i]
        stripped = raw.strip()
        line_no = i + 1
        i += 1
        if not stripped or stripped.startswith("#"):
            continue
        if stripped.startswith(("%", "?")):
            _die_entry(line_no, "内容块必须紧跟在 @FILE 后（这里是 %s）"
                       % stripped)
        m = ENTRY_RE.match(stripped)
        if not m:
            _die_entry(line_no, "无法解析（条目要以 @ 开头）：%s" % stripped)
        typ = m.group(1).upper()
        rest = m.group(2).strip()
        if typ in ("HOME", "ENV", "API", "INCLUDE"):
            _die_entry(line_no, "v1 未支持 @%s：请在用例脚本里直接做"
                       "（@API 可用 o @create + obj save-text；@HOME/@ENV 用 "
                       "o ... @update）" % typ)
        if typ not in ("DIR", "FILE"):
            _die_entry(line_no, "v1 未支持的类型 @%s：只支持 @DIR / @FILE" % typ)

        path, quick, inline_meta = _split_entry(rest, line_no)
        path = _normalize_path(_render(path, variables), line_no)

        meta_parts = [inline_meta] if inline_meta else []
        while i < len(lines):
            nxt = lines[i]
            ns = nxt.strip()
            if not ns:
                i += 1
                continue
            if ns.startswith("#") or ns.startswith("@"):
                break
            if CONTENT_RE.match(ns):
                break
            if ns.startswith(("%", "?")):
                _die_entry(i + 1, "无法识别的内容块/指令：%s" % ns)
            meta_parts.append(nxt)
            i += 1

        meta = _quick_meta(quick, variables) if quick else {}
        if meta_parts:
            meta_text = _render("\n".join(meta_parts), variables)
            try:
                extra = json.loads(meta_text)
            except ValueError as e:
                _die_entry(line_no, "元数据不是合法 JSON（%s）：%s"
                           % (e, meta_text[:300]))
            if not isinstance(extra, dict):
                _die_entry(line_no, "元数据 JSON 顶层必须是对象")
            meta.update(_resolve_explain(extra, variables))

        content = None
        if i < len(lines):
            cm = CONTENT_RE.match(lines[i].strip())
            if cm:
                content_line = i + 1
                flag, kind, _braces, sep, tail = cm.groups()
                kind = kind.upper()
                if sep == ">":
                    _die_entry(content_line,
                               "v1 未支持外部文件内容（%s > 文件）：请先 "
                               "wn-cli obj save-text 上传，或在用例脚本里直接做"
                               % kind)
                if flag != "%":
                    _die_entry(content_line,
                               "v1 只支持 %s: ... %%END%%，不支持 ? 条件写入"
                               % kind)
                if typ == "DIR":
                    _die_entry(content_line, "@DIR 不能带内容块")
                body = [tail] if tail and tail.strip() else []
                i += 1
                closed = False
                while i < len(lines):
                    if lines[i].strip() == "%END%":
                        closed = True
                        i += 1
                        break
                    body.append(lines[i])
                    i += 1
                if not closed:
                    _die_entry(content_line, "%s 内容缺少 %%END%%" % kind)
                raw_content = "\n".join(body)
                if kind == "TMPL":
                    content = _render(raw_content, variables)
                elif kind == "COPY":
                    content = raw_content
                else:
                    try:
                        parsed = json.loads(_render(raw_content, variables))
                    except ValueError as e:
                        _die_entry(content_line, "EXPLAIN 内容不是合法 JSON：%s"
                                   % e)
                    content = json.dumps(_resolve_explain(parsed, variables),
                                         ensure_ascii=False, indent=2)

        items.append({"type": typ, "path": path, "meta": meta,
                      "content": content, "line": line_no})
    return items


def fixture(root=None, name=None, vars=None, timeout=90):
    """按 `<root>/atest.init` 的最小渲染器铺夹具（不调用 `app init`）。

    支持（v1 子集）：

        @DIR <path> ['quick meta'] [{json}]
        @FILE <path> ['quick meta'] [{json}]
        %TMPL: ... %END%      ${var} / ${var?默认值}
        %COPY: ... %END%      原样写入
        %EXPLAIN: ... %END%   JSON；=key / =key?默认 / ==key / !=key

    - 变量：`<root>/vars.json`（JSON 对象，缺省 `{}`）；`vars` 是调用方给的
      默认值，项目 vars.json 覆盖同名键；
    - 路径相对域 home（`@DIR .atest/x` → `~/.atest/x`）；父目录自动建；
    - 文件内容用 `obj save-text` 写；元数据用 `o ... @update` 合并；
    - **幂等**：目标已存在则跳过内容写入（元数据仍合并，与应用初始化器的
      "非覆盖内容"语义一致）；
    - `@HOME` / `@ENV` / `@API` / `@INCLUDE` / `%COPY > 外部文件` 一律明确报错
      （v1 未支持，请在用例脚本里直接做），不静默忽略。

    失败（缺模板、变量/JSON 非法、不支持写法、wn-cli 失败）→ `SystemExit(2)`：
    runner 记 **blocked**，不伪装成红/绿。
    """
    if vars is not None and not isinstance(vars, dict):
        _die("fixture(vars=...) 必须是 dict（现在是 %r）" % (vars,))

    config = _load_config()
    init_path, vars_path = _fixture_paths(root, config)
    fixture_name = str(name or init_path.stem)
    if not fixture_name or fixture_name in (".", "..") \
            or not NAME_RE.match(fixture_name):
        _die("夹具名 %r 不合法：只允许 %s（且不能是 . / ..）"
             % (fixture_name, NAME_RE.pattern))

    if not init_path.is_file():
        _die("夹具模板不存在：%s（先照 bin/at-init/tmpl/atest.init 写一份）"
             % init_path)
    text = _read_text_file(init_path, "夹具模板")

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

    items = _parse_fixture(text, merged)
    if not items:
        _die("夹具模板里没有任何 @DIR / @FILE：%s" % init_path)

    print("atlib: fixture %s：%d 条，变量键=%s"
          % (init_path.name, len(items),
             ", ".join(sorted(merged)) if merged else "（无）"),
          file=sys.stderr)
    # 元数据临时文件放工具目录；先确保目录存在
    wn("mkdir -p -noexists %s" % REMOTE_INIT_DIR, timeout=timeout)

    counts = {"DIR": 0, "FILE": 0}
    for item in items:
        path = item["path"]
        counts[item["type"]] += 1
        obj = _object_of(path, timeout)
        if obj is not None:
            expected = "DIR" if item["type"] == "DIR" else "FILE"
            if obj.get("race") != expected:
                _die("夹具路径 %s 已存在，但类型是 %s（模板要求 %s）"
                     % (path, obj.get("race"), expected))
            # 已存在：不覆盖内容，只合并元数据
        elif item["type"] == "DIR":
            wn("mkdir -p -noexists %s" % _cmd_arg(path), timeout=timeout)
        else:
            _ensure_parent(path, timeout)
            _save_text(path, item["content"] or "", timeout)
        _update_meta(fixture_name, path, item["meta"], timeout)
        print("atlib: fixture %s %s" % (item["type"], path), file=sys.stderr)

    note("fixture %s：模板=%s，%d 条（DIR %d / FILE %d），变量键=%s"
         % (fixture_name, init_path.name, len(items),
            counts["DIR"], counts["FILE"],
            ", ".join(sorted(merged)) if merged else "（无）"))
    return {"name": fixture_name, "template": str(init_path),
            "vars": sorted(merged), "entries": len(items),
            "counts": counts}


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
