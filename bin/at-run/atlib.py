#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""atlib — ATEST 用例脚本薄库（ATESET-04 / 05）

脚本只"执行 + 取证"，断言交给 at-run（按用例 md 的 `## 期望` 统一比较）。
本库把每个脚本都会重写的样板收成五个函数：

    wn(cmd)       经 wn-cli 执行一条 Walnut 命令（自动带级别 / 会话 / 端点）
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

夹具（ATESET-05，静态布局必须写进模板，不在用例里现拼）：

    from atlib import fixture, snap, emit

    fixture()                       # <root>/atest.init + <root>/vars.json
    obj = snap("~/.atest/demo")     # 模板铺好的对象
    emit({"atest-owner": obj.get("atest-owner")})

夹具由 `app init` 执行：`atest.init` 里可以使用它的全部指令
（@DIR / @FILE / @API / @HOME / @ENV / @INCLUDE / %TMPL / %COPY / %EXPLAIN）。
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

# 远程工具目录：模板与变量每次都重写，避免旧文件残留（ATESET-05 T1 约定）
REMOTE_INIT_DIR = "~/.atest/init"
REMOTE_VARS = "~/.atest/vars.json"

# 夹具名就是上传后的模板文件名，只允许安全字符
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
    """
    counts = {"DIR": 0, "FILE": 0}
    entries = 0
    for line in text.splitlines():
        m = re.match(r"^@([A-Za-z]+)\s", line.strip())
        if not m:
            continue
        entries += 1
        typ = m.group(1).upper()
        if typ in counts:
            counts[typ] += 1
    return entries, counts


def fixture(root=None, name=None, vars=None, timeout=90):
    """用 Walnut 自己的 `app init` 把 `<root>/atest.init` 铺进域。

    步骤（ATESET-05）：

    1. 读 `<root>/atest.json` 的 `root` / `init_template`，定位模板与
       `<root>/vars.json`；
    2. 模板原文 `obj save-text` 到 `~/.atest/init/<name>.init`（覆盖写）；
    3. 变量 = `vars` 参数 + `<root>/vars.json`（项目同名键优先）；非空则
       再 `obj save-text` 到 `~/.atest/vars.json`，并作为 `-vars` 传给 app init；
    4. 执行 `app init -conf ~/.atest/init/<name>.init -script off`
       （所以模板里可以直接用 `@DIR` / `@FILE` / `@API` / `@HOME` / `@ENV` /
       `@INCLUDE` 与 `%TMPL` / `%COPY` / `%EXPLAIN`；`-script` 默认 **off**，
       脚本测试要收尾脚本时自己调）；
    5. 幂等由 `app init` 自身保证：目标文件已存在且内容非空时跳过写入，
       目录 / 元数据按模板合并，同一模板连跑两次安全。

    - `root` 缺省读 `atest.json` 的 `root`（再缺省 `atest`）；`name` 缺省为
      模板文件名（`atest.init` → `atest`）；
    - 失败（模板缺失、上传失败、`app init` 非 0、vars.json 非法）→
      `SystemExit(2)`：runner 记 **blocked**，并带上 `app init` 的原始输出。

    返回值形状保持 `{"name", "template", "vars", "entries", "counts"}`
    （`entries` / `counts` 由 `app init` 的输出统计，仅供日志与调用方参考）。
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

    print("atlib: fixture %s：模板=%s，变量键=%s"
          % (fixture_name, init_path.name,
             ", ".join(sorted(merged)) if merged else "（无）"),
          file=sys.stderr)

    # 模板 / 变量都上传到工具目录；app init 从对象树里读模板
    wn("mkdir -p -noexists %s" % REMOTE_INIT_DIR, timeout=timeout)
    remote_init = "%s/%s.init" % (REMOTE_INIT_DIR, fixture_name)
    _save_text(remote_init, text, timeout)

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
         % (fixture_name, init_path.name, entries,
            counts["DIR"], counts["FILE"],
            ", ".join(sorted(merged)) if merged else "（无）"))
    return {"name": fixture_name, "template": str(init_path),
            "vars": sorted(merged), "entries": entries,
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
