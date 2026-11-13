#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
wn-cli — Walnut 智能体客户端

对接 web2 路由（wn-doc/public/reference/102.auto-test/30-at-tools.md）:
    AppSysLoginByPasswdController  /a/sys_login_by_passwd  登录（ajax 信封）
    AppRunController               /a/run/wn-term          命令通道（mkdir/mount/ln/o/app init/sqlx ...）
    Obj*Controller                 /o/*                    对象与文件字节
                                                           （fetch/children/query/update/save/content）

子命令:
    login   登录并缓存票据（密码错 3 次终止）
    cmd     执行命令文本（缺省参数 '-' 从 stdin 读）
    obj     对象操作: get/children/set/save-text/content/upload/find
    mnt     挂载本地目录（写 mnt 元数据 + 自动 ls 自检）

全局选项:
    --url           实例地址（缺省按 --level 从实例登记解析，再退到会话缓存里的 base）
    --level         实例级别 L1/L2：决定会话名与会话解析（见下）
    --instances     实例登记文件（缺省 $WN_INSTANCES > 当前目录逐级向上 > 用户目录）
    --se            会话名（缺省 <级别>-<登录名>-00，如 L1-demo-00；
                    没有级别时沿用 se-<登录名>-00，其他子命令按级别找唯一会话）
    --session-dir   会话缓存目录（默认 ./.at/client/session）
    --dist          dist/wn4j 目录：root 密码发现（conf/web_local.properties
                    → core/conf/web.properties 的 root-init-passwd）
    --who           智能体代号（写入会话缓存 agent.name；缺省读 .at/whoami.toml）
    --creds         凭据 JSON（默认若 ./.at/credentials.json 存在则用它）

实例登记 `.wn-instances.json`（开发者维护，智能体只读）:
    {"L1": {"host": "127.0.0.1", "port": 8080},
     "L2": {"host": "127.0.0.1", "port": 8090}}
    给了 --level 就能省掉 --url；会话按级别分开存，同一项目在 L1/L2 上互不串用。

会话缓存是 TOML 文件（<se>.toml），只存票据与身份（base/loginName/ticket/
agent/create_on/saved_at），不存任何密码：root 会话的密码每次从实例配置现找，
域会话密码不落盘。

退出码: 0 成功；1 用法错误；2 目标不存在；3 认证失败；4 服务器响应异常

本文件既可作为 CLI 使用，也可作为库:
    from wn_cli import WnClient
    c = WnClient("http://localhost:8080", session_dir=".at/client/session", se="se-00")
    c.login("demo", "123456")
    print(c.cmd("o ~/data -l"))
"""

import argparse
import base64
import getpass
import json
import mimetypes
import os
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

try:
    import tomllib  # Python 3.11+：会话缓存与身份文件用 TOML
except ImportError:  # 3.11 以下走内置子集解析器
    tomllib = None

EXIT_OK = 0
EXIT_USAGE = 1
EXIT_NO_TARGET = 2
EXIT_AUTH = 3
EXIT_BAD = 4

TICKET_HEADER = "x-walnut-ticket"

# Git Bash / MSYS2 会把 /app 这类参数转换成盘符路径（C:/Program Files/Git/app），
# 裸 ~ 也会被 bash 本地展开成 /c/Users/<name>（MSYS 盘符挂载形式）。
# Walnut 路径永远不可能是这两种形态，所以在客户端直接拦截并报出解方。
MSYS_HINT = ("这个参数像是被 Git Bash 的 MSYS2 路径转换/波浪号展开改写过。\n"
             "请在执行 wn-cli 前先: export MSYS2_ARG_CONV_EXCL='*'，\n"
             "并且路径参数一律加引号（如 \"~/tmp\"）")


def _msys_root_fragments():
    """已知的 MSYS 根目录特征片段（小写、正斜杠）。"""
    frags = ["program files/git", "program files (x86)/git",
             "msys64", "mingw64", "/usr/bin"]
    for var in ("EXEPATH", "MSYS2_ROOT"):
        v = os.environ.get(var)
        if v:
            frags.append(v.replace("\\", "/").lower().rstrip("/"))
    return frags


def guard_msys_path(path, what="Walnut 路径"):
    """Walnut 路径参数出现 X:/ 盘符开头，或 /c/... 这种 MSYS 挂载形式
    （Walnut 顶层目录没有单字母的），几乎一定是本地 shell 改写产物。"""
    if path and (re.match(r"^[A-Za-z]:[/\\]", path)
                 or re.match(r"^/[A-Za-z][/\\]", path)
                 or re.match(r"^/[A-Za-z]$", path)):
        raise WnError(EXIT_USAGE, "%s 不应该是盘符/MSYS 挂载路径: %s\n%s"
                      % (what, path, MSYS_HINT))


def guard_msys_text(text):
    """命令文本里扫描被 MSYS 转换/波浪号展开过的片段:
    - X:/<已知 MSYS 根目录>（路径可带空格；file://D:/... 冒号前是 / 不误伤）
    - /x/users/ 与 /x/<已知 MSYS 根目录>（bash 把裸 ~ 展开成 /c/Users/<name>）
    """
    low = text.replace("\\", "/").lower()
    pats = [r"(?<![\w/.:\\-])/[a-z]/users/"]
    for f in _msys_root_fragments():
        f = re.sub(r"^[a-z]:", "", f).lstrip("/")  # EXEPATH 可能自带盘符
        if not f:
            continue
        pats.append(r"(?<![\w/.:\\-])[a-z]:/" + re.escape(f))
        pats.append(r"(?<![\w/.:\\-])/[a-z]/" + re.escape(f))
    for p in pats:
        m = re.search(p, low)
        if m:
            raise WnError(EXIT_USAGE,
                          "命令文本里发现疑似 MSYS 转换产物: %s...\n%s"
                          % (m.group(0), MSYS_HINT))


class WnError(Exception):
    """带退出码的工具错误"""

    def __init__(self, code, msg):
        super().__init__(msg)
        self.code = code
        self.msg = msg


class WnNoUser(WnError):
    def __init__(self, msg="e.auth.login.UsrNoExists"):
        super().__init__(EXIT_AUTH, msg)


class WnLoginFailed(WnError):
    def __init__(self, msg="e.auth.login.Failed"):
        super().__init__(EXIT_AUTH, msg)


# ---------------- 实例登记（.wn-instances.json） ----------------
# 约定：项目根/工作区到用户目录逐级向上找；键是级别（L1/L2），值是 host+port。
# 用途：由 --level（或 url 反查）决定会话名 <级别>-<登录名>-<序号>。
INSTANCES_NAME = ".wn-instances.json"
_LOOPBACK = {"localhost", "127.0.0.1", "::1"}


def _same_host(a, b):
    a, b = (a or "").lower(), (b or "").lower()
    if a in _LOOPBACK and b in _LOOPBACK:
        return True
    return a == b


def load_instances(start=None, explicit=None):
    """按 --instances > $WN_INSTANCES > 当前目录逐级向上 > 用户目录 找实例登记。

    返回 (dict, Path|None)；找不到返回 ({}, None)，不抛异常。
    """
    cands = []
    if explicit:
        cands.append(Path(explicit))
    env = os.environ.get("WN_INSTANCES")
    if env:
        cands.append(Path(env))
    here = Path(start or os.getcwd()).resolve()
    cands.extend([d / INSTANCES_NAME for d in [here] + list(here.parents)])
    cands.append(Path.home() / INSTANCES_NAME)
    for f in cands:
        if f.is_file():
            try:
                return json.loads(f.read_text(encoding="utf-8")), f
            except ValueError:
                return {}, f
    return {}, None


def level_of(url, instances):
    """按 url 的 host:port 反查级别；找不到返回 None。"""
    if not url or not instances:
        return None
    u = urllib.parse.urlparse(url if "://" in url else "http://" + url)
    host = u.hostname or "localhost"
    port = u.port or (443 if u.scheme == "https" else 80)
    for name, ep in instances.items():
        if not isinstance(ep, dict):
            continue
        try:
            if (int(ep.get("port", 0) or 0) == port
                    and _same_host(str(ep.get("host", "127.0.0.1")), host)):
                return name
        except (TypeError, ValueError):
            continue
    return None


def endpoint_of(level, instances):
    """由级别取 base，如 http://127.0.0.1:8080；取不到返回 None。"""
    ep = (instances or {}).get(level) or {}
    if not ep.get("port"):
        return None
    return "http://%s:%d" % (ep.get("host", "127.0.0.1"), int(ep["port"]))


def resolve_session_dir(explicit, instances_file):
    """会话缓存目录：--session-dir > $WN_SESSION_DIR > 工作区级 > 项目级。

    「工作区级」= 命中的 .wn-instances.json 所在目录下的 .at/client/session ——
    同一个工作区里所有项目共用一个会话池，`L1-demo-00` 只需登录一次。
    """
    if explicit:
        return explicit
    env = os.environ.get("WN_SESSION_DIR")
    if env:
        return env
    if instances_file:
        return str(Path(instances_file).parent / ".at" / "client" / "session")
    return ".at/client/session"


def default_test_user(start=None):
    """项目默认的测试用户。

    依次看：`atest.json` 的 `test_domain` → `.at/credentials.json` 的 `usr`。
    找不到就返回 None（由调用方退化为"非 root 会话优先"）。
    """
    here = Path(start or os.getcwd()).resolve()
    chain = [here] + list(here.parents)
    for d in chain:
        cfg = d / "atest.json"
        if cfg.is_file():
            try:
                data = json.loads(cfg.read_text(encoding="utf-8-sig"))
                usr = data.get("test_domain") if isinstance(data, dict) else None
                if usr:
                    return str(usr)
            except (ValueError, OSError):
                pass
            break
    for d in chain:
        cred = d / ".at" / "credentials.json"
        if cred.is_file():
            try:
                data = json.loads(cred.read_text(encoding="utf-8-sig"))
                usr = data.get("usr") if isinstance(data, dict) else None
                if usr:
                    return str(usr)
            except (ValueError, OSError):
                pass
            break
    return None


def _session_seq(path):
    """<级别>-<用户>-<序号>.toml 里的序号；解析不了排最后。"""
    try:
        return int(path.stem.rsplit("-", 1)[1])
    except (ValueError, IndexError):
        return 10 ** 6


def pick_session(session_dir, level):
    """非 login 子命令：按级别挑一个会话。

    顺序（ATESET 定稿）：
    1. 项目默认用户（`atest.json` 的 `test_domain` → `.at/credentials.json` 的 `usr`）
       对应的 `<级别>-<用户>-NN`；
    2. 没有该级别会话 → 报错；
    3. 有多个用户时：优先非 root；仍多个 → 报错并列出候选；
    4. 同一用户的多个会话（00/01/02…）→ 取序号最小的，**不报错**（共享会话是预期特征）。
    """
    hits = sorted(Path(session_dir).glob("%s-*.toml" % level), key=_session_seq)
    if not hits:
        raise WnError(EXIT_USAGE, "%s 级别还没有会话，先 login（或用 --se 指定）" % level)

    usr = default_test_user()
    if usr:
        mine = [h for h in hits
                if h.stem.startswith("%s-%s-" % (level, usr))]
        if not mine:
            raise WnError(EXIT_USAGE, "%s 级别没有用户 %s 的会话（现有：%s）；"
                                      "先 login，或用 --se 指定"
                          % (level, usr, ", ".join(h.stem for h in hits)))
        return mine[0].stem

    non_root = [h for h in hits if not h.stem.startswith("%s-root-" % level)]
    pick_from = non_root or hits
    users = sorted({h.stem.split("-")[1] for h in pick_from
                    if len(h.stem.split("-")) >= 3})
    if len(users) > 1:
        raise WnError(EXIT_USAGE,
                      "%s 级别有多个用户的会话（%s），请用 --se 指定，"
                      "或在 atest.json 里设 test_domain"
                      % (level, ", ".join(h.stem for h in hits)))
    return pick_from[0].stem


def http(base, method, path, params=None, form=None, body=None,
         headers=None, timeout=120):
    """返回 (status, bytes)。不抛异常（HTTPError 也返回其状态与响应体）。"""
    url = base.rstrip("/") + path
    if params:
        url += ("&" if "?" in url else "?") + urllib.parse.urlencode(params)
    data = None
    hdrs = dict(headers or {})
    if form is not None:
        data = urllib.parse.urlencode(form).encode("utf-8")
        hdrs.setdefault("Content-Type", "application/x-www-form-urlencoded")
    elif body is not None:
        data = body if isinstance(body, bytes) else body.encode("utf-8")
    req = urllib.request.Request(url, data=data, method=method, headers=hdrs)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return resp.status, resp.read()
    except urllib.error.HTTPError as e:
        return e.code, e.read()
    except urllib.error.URLError as e:
        raise WnError(EXIT_NO_TARGET, "连接失败: %s" % e)


def api_login(base, usr, passwd):
    """登录，返回 (ticket, data)。按错误码区分 NoUser / Failed。

    端点与载荷以 web2 AppSysLoginByPasswdController 为准：
    POST 表单 name/passwd/ajax=1；成功 {"ok":true,"data":{ticket,...}}，
    失败 {"ok":false,"errCode":...,"data":原因}。"""
    st, body = http(base, "POST", "/a/sys_login_by_passwd",
                    form={"name": usr, "passwd": passwd, "ajax": "1"})
    text = body.decode("utf-8", "replace")
    try:
        j = json.loads(text)
    except ValueError:
        raise WnError(EXIT_BAD, "登录响应不是 JSON: %s" % text[:300])
    if j.get("ok"):
        data = j.get("data") or {}
        ticket = data.get("ticket") or data.get("nm")
        if not ticket:
            raise WnError(EXIT_BAD, "登录响应缺少票据: %s" % text[:300])
        return ticket, data
    msg = str(j.get("errCode") or j.get("msg") or j.get("err") or text)
    reason = j.get("data")
    if reason:
        if not isinstance(reason, str):
            reason = json.dumps(reason, ensure_ascii=False)
        msg += ": " + reason
    if "UsrNoExists" in msg or "noexists" in msg:
        raise WnNoUser(msg)
    raise WnLoginFailed(msg)


def guess_root_passwd(dist, explicit=None):
    """root 密码发现（30-at-tools.md 契约）:
    1. 显式传入（--root-passwd / 凭据文件 root_passwd）
    2. <dist>/conf/web_local.properties 的 root-init-passwd
    3. <dist>/core/conf/web.properties 的 root-init-passwd
    4. 都没有 → 返回 None（调用方回退到缺省 123456；失败则终止询问用户）
    """
    if explicit:
        return explicit
    if dist:
        d = Path(dist)
        for rel in ("conf/web_local.properties", "core/conf/web.properties"):
            p = d / rel
            if not p.exists():
                continue
            try:
                for line in p.read_text(encoding="utf-8",
                                        errors="replace").splitlines():
                    line = line.strip()
                    if line.startswith("root-init-passwd"):
                        v = line.split("=", 1)[1].strip()
                        if v:
                            return v
            except OSError:
                pass
    return None


def resolve_agent_name(who):
    """智能体代号: --who 优先，其次工作目录 .at/whoami.toml
    （兼容旧 .at/whoami.json）的 name 字段。
    都没有 → None（人类手动使用 wn-cli 的场景）。"""
    if who:
        return who
    at = Path(".at")
    for f, loader in ((at / "whoami.toml", toml_parse),
                      (at / "whoami.json", json.loads)):
        if not f.exists():
            continue
        try:
            j = loader(f.read_text(encoding="utf-8"))
            if isinstance(j, dict) and j.get("name"):
                return j["name"]
        except (ValueError, OSError):
            pass
    return None


def merge_agent(old, name):
    """把新代号并入 agent 对象，保留已有的其它字段（如 duty）。"""
    if not name:
        return old
    a = dict(old or {})
    a["name"] = name
    return a


# ---------------- TOML 读写（会话缓存与身份文件） ----------------
def toml_escape(s):
    """把一个字符串写成 TOML 基本字符串（单行，换行用 \\n 转义）。"""
    out = []
    for ch in s:
        if ch == "\\":
            out.append("\\\\")
        elif ch == '"':
            out.append('\\"')
        elif ch == "\n":
            out.append("\\n")
        elif ch == "\r":
            out.append("\\r")
        elif ch == "\t":
            out.append("\\t")
        elif ord(ch) < 0x20:
            out.append("\\u%04X" % ord(ch))
        else:
            out.append(ch)
    return '"%s"' % "".join(out)


def _toml_key(k):
    """合法裸键原样写，其余（如中文键）用引号包起来。"""
    return k if re.match(r"^[A-Za-z0-9_-]+$", k) else toml_escape(k)


def _toml_unescape(s):
    out, i = [], 0
    simple = {"n": "\n", "t": "\t", "r": "\r", '"': '"', "\\": "\\"}
    while i < len(s):
        ch = s[i]
        if ch == "\\" and i + 1 < len(s):
            nxt = s[i + 1]
            if nxt in simple:
                out.append(simple[nxt])
                i += 2
                continue
            if nxt == "u" and i + 6 <= len(s):
                try:
                    out.append(chr(int(s[i + 2:i + 6], 16)))
                    i += 6
                    continue
                except ValueError:
                    pass
        out.append(ch)
        i += 1
    return "".join(out)


def _toml_scalar(v):
    v = v.strip()
    if len(v) >= 2 and v.startswith('"') and v.endswith('"'):
        return _toml_unescape(v[1:-1])
    if len(v) >= 2 and v.startswith("'") and v.endswith("'"):
        return v[1:-1]
    if v == "true":
        return True
    if v == "false":
        return False
    for cast in (int, float):
        try:
            return cast(v)
        except ValueError:
            pass
    return v


def _toml_parse_mini(text):
    """Python 3.11 以下用的 TOML 子集解析器：注释、[table]、
    key = value、基本/字面量字符串（含多行 \"\"\"...\"\"\"）与布尔/数字。
    只覆盖会话与身份文件用到的语法。"""
    root, cur = {}, {}
    lines = text.splitlines()
    i, n = 0, len(lines)
    while i < n:
        line = lines[i].strip()
        i += 1
        if not line or line.startswith("#"):
            continue
        if line.startswith("[") and line.endswith("]"):
            cur = root.setdefault(line[1:-1].strip().strip('"').strip("'"), {})
            continue
        if "=" not in line:
            continue
        key, _, rest = line.partition("=")
        key = key.strip().strip('"').strip("'")
        val = rest.strip()
        if val.startswith('"""') or val.startswith("'''"):
            delim, body, chunks = val[:3], val[3:], []
            while True:
                end = body.find(delim)
                if end >= 0:
                    chunks.append(body[:end])
                    break
                chunks.append(body)
                if i >= n:
                    break
                body = lines[i]
                i += 1
            joined = "\n".join(chunks)
            cur[key] = _toml_unescape(joined) if delim == '"""' else joined
            continue
        cur[key] = _toml_scalar(val)
    return root


def toml_parse(text):
    """解析 TOML：优先 tomllib（3.11+），失败或缺失时用子集解析器。"""
    if tomllib is not None:
        try:
            return tomllib.loads(text)
        except Exception:
            pass
    return _toml_parse_mini(text)


class Session:
    """会话缓存: <session_dir>/<se>.toml（兼容读旧 <se>.json）

    契约见 wn-doc/public/reference/102.auto-test/30-at-tools.md 的 wn-cli 一节:

      base       实例地址
      loginName  登录名（root 或域用户）
      ticket     会话票据
      agent      智能体身份（[agent] 表：name / duty / 自定义字段）
      dist       dist/wn4j 目录（root 会话用于重登时找回 root-init-passwd）
      create_on  首次创建时间
      saved_at   最后保存时间

    **不存任何密码**（旧格式里的 usr/creds 只读不写）。
    """

    def __init__(self, session_dir, se="se-00"):
        self.dir = Path(session_dir)
        self.se = se
        self.path = self.dir / ("%s.toml" % se)
        self.legacy_path = self.dir / ("%s.json" % se)
        self.base = None
        self.usr = None
        self.ticket = None
        self.agent = None
        self.dist = None
        self.create_on = None
        self.saved_at = None
        self.load()

    def load(self):
        data = None
        if self.path.exists():
            try:
                data = toml_parse(self.path.read_text(encoding="utf-8-sig"))
            except (ValueError, OSError):
                data = None
        if not isinstance(data, dict) and self.legacy_path.exists():
            try:
                data = json.loads(self.legacy_path.read_text(encoding="utf-8-sig"))
            except (ValueError, OSError):
                data = None
        if not isinstance(data, dict):
            return
        self.base = data.get("base")
        # 兼容旧格式: 旧文件里叫 usr，新格式叫 loginName
        self.usr = data.get("loginName") or data.get("usr")
        self.ticket = data.get("ticket")
        self.agent = data.get("agent")
        self.dist = data.get("dist")
        self.create_on = data.get("create_on")
        self.saved_at = data.get("saved_at")

    def save(self):
        self.dir.mkdir(parents=True, exist_ok=True)
        now = time.strftime("%Y-%m-%dT%H:%M:%S")
        if not self.create_on:
            self.create_on = now
        self.saved_at = now
        lines = ["# wn-cli 会话缓存（TOML；不存任何密码）",
                 "base = %s" % toml_escape(self.base or ""),
                 "loginName = %s" % toml_escape(self.usr or ""),
                 "ticket = %s" % toml_escape(self.ticket or "")]
        if self.dist:
            lines.append("dist = %s" % toml_escape(self.dist))
        lines.append("create_on = %s" % toml_escape(self.create_on))
        lines.append("saved_at = %s" % toml_escape(self.saved_at))
        if isinstance(self.agent, dict) and self.agent:
            lines.extend(["", "[agent]"])
            for k, v in self.agent.items():
                if isinstance(v, str):
                    lines.append("%s = %s" % (_toml_key(k), toml_escape(v)))
                else:
                    lines.append("%s = %s"
                                 % (_toml_key(k),
                                    json.dumps(v, ensure_ascii=False)))
        self.path.write_text("\n".join(lines) + "\n", encoding="utf-8")


class WnClient:
    def __init__(self, base=None, session_dir=".at/client/session", se="se-00"):
        self.session = Session(session_dir, se)
        self.base = base or self.session.base
        if not self.base:
            raise WnError(EXIT_USAGE, "缺少 --url，且会话缓存里没有 base")

    # ---------------- 认证 ----------------
    def login(self, usr, passwd, retries=3):
        """域登录；UsrNoExists 时由调用方决定 root fallback。失败计 3 次。
        会话缓存不存密码。"""
        last = None
        for n in range(retries):
            try:
                ticket, data = api_login(self.base, usr, passwd)
                self.session.base = self.base
                self.session.usr = usr
                self.session.ticket = ticket
                self.session.save()
                return ticket
            except WnLoginFailed as e:
                last = e
                print("登录失败(%d/%d): %s" % (n + 1, retries, e.msg),
                      file=sys.stderr)
        raise WnError(EXIT_AUTH,
                      "密码登录失败 %d 次，终止。请与主人核对凭据" % retries)

    def login_or_create_domain(self, usr, passwd,
                               root_usr="root", root_passwd=""):
        """完整决策树（doc 20-at-setup）:
        域登录 → 失败 → root 登录 → 查 /home/<usr> 是否存在
               → 不存在则 adduser 建域 → 域登录重试
               → 已存在则说明域密码不对，报错
        注意: loginByPassword 对"用户不存在"和"密码错"都抛
        e.auth.login.Failed，无法区分，只能靠 root 查目录来判断。
        root 本人登录（usr == root_usr）直接登录，不建域。
        root 密码由调用方经 guess_root_passwd 准备好（配置发现 →
        缺省 123456）；失败即终止，向主人询问。
        """
        if usr == root_usr:
            try:
                return self.login(usr, passwd, retries=1), "login"
            except WnError as e:
                raise WnError(
                    EXIT_AUTH,
                    "root 登录失败: %s —— 实例配置的 root-init-passwd 与"
                    "缺省 123456 均不可用。终止流程，请向主人询问 root 密码"
                    % e.msg)
        # 第一次探测用 api_login 而不是 self.login（login 会把
        # WnLoginFailed 包成 WnError，这里要自己接住分支）
        try:
            ticket, _ = api_login(self.base, usr, passwd)
            self.session.base = self.base
            self.session.usr = usr
            self.session.ticket = ticket
            self.session.save()
            return ticket, "login"
        except (WnNoUser, WnLoginFailed):
            pass
        if not root_passwd:
            raise WnError(EXIT_USAGE,
                          "建域需要 root 密码: 请传 --root-passwd 或在凭据文件里给出")
        try:
            rticket, _ = api_login(self.base, root_usr, root_passwd)
        except WnLoginFailed:
            raise WnError(EXIT_AUTH,
                          "root 登录失败: 实例配置的 root-init-passwd 与"
                          "缺省 123456 均不可用。终止流程，请向主人询问"
                          " root 密码")
        r = WnClient.__new__(WnClient)
        r.session = self.session
        r.base = self.base
        r._ticket = rticket
        # root 视角检查域主目录是否已存在
        try:
            r.obj_get("/home/" + usr)
            exists = True
        except WnError:
            exists = False
        if exists:
            raise WnError(EXIT_AUTH,
                          "用户 %s 已存在但域密码不对，请与主人核对凭据" % usr)
        out = r.cmd("adduser %s -p %s" % (usr, passwd))
        print(out.strip(), file=sys.stderr)
        return self.login(usr, passwd), "created"

    # ---------------- 请求（含 401 自动重登） ----------------
    @property
    def _ticket(self):
        return self.session.ticket

    @_ticket.setter
    def _ticket(self, v):
        self.session.ticket = v

    def _ph(self, path):
        """把 ~ 展开为会话用户主目录，并去掉尾斜杠（/o 端点对
        /home/demo/ 这种尾斜杠路径会报 noexists）。

        服务端 /o 系列的 str 参数不做会话级路径展开（Wn.getObj(io,str)
        直通 io.fetch，~ 会被解到服务器 OS 的 HOME）。客户端必须先展开，
        否则对象会写进以服务器 OS 用户目录开头的怪路径里。
        """
        usr = self.session.usr or ""
        home = "/root" if usr == "root" else "/home/" + usr
        if path == "~":
            return home
        if path.startswith("~/"):
            path = home + path[1:]
        if len(path) > 1 and path.endswith("/"):
            path = path.rstrip("/")
        guard_msys_path(path)
        return path

    def _try_relogin(self):
        """401 自动重登一次: 只有 root 会话能自动重登——密码从实例配置
        现找（<dist>/conf → core/conf 的 root-init-passwd，缺省 123456）。
        域会话不再缓存密码，401 只能重新 login。"""
        usr = self.session.usr
        if usr != "root":
            return False
        passwd = guess_root_passwd(self.session.dist) or "123456"
        try:
            t, _ = api_login(self.base, usr, passwd)
            self._ticket = t
            self.session.save()
            return True
        except WnError:
            return False

    def _req(self, method, path, retried=False, **kw):
        ticket = self._ticket
        if ticket:
            hdrs = kw.setdefault("headers", {})
            hdrs[TICKET_HEADER] = ticket
        st, body = http(self.base, method, path, **kw)
        if st == 401 and not retried and self._try_relogin():
            print("会话过期，自动重新登录 ...", file=sys.stderr)
            return self._req(method, path, retried=True, **kw)
        if st == 401:
            # 票据确认失效：删掉本地缓存，下次 login 从干净状态开始。
            # 只删当前会话文件；网络错误/连不上不走这里，不误删。
            if self.session.ticket and self.session.path.exists():
                try:
                    self.session.path.unlink()
                    print("会话已失效，已删除缓存 %s（请重新 login）"
                          % self.session.path, file=sys.stderr)
                except OSError as e:
                    print("会话已失效，但删除缓存失败：%s" % e, file=sys.stderr)
            raise WnError(EXIT_AUTH,
                          "未登录或会话失效（401）。root 会话重登失败时，"
                          "请与主人核对 root 密码；域会话请重新 login")
        return st, body

    def _check(self, st, body):
        """Walnut ajax 端点错误也返回 HTTP 200 + {"ok":false} 信封，
        必须查 ok 字段，不能只看状态码。"""
        text = body.decode("utf-8", "replace")
        if st >= 400:
            raise WnError(EXIT_BAD, text.strip()[:300] or ("HTTP %d" % st))
        try:
            j = json.loads(text)
        except ValueError:
            return
        if isinstance(j, dict) and j.get("ok") is False:
            raise WnError(EXIT_BAD,
                          str(j.get("msg") or j.get("errCode") or j)[:300])

    # ---------------- 命令通道 ----------------
    def _expand_tilde(self, text):
        """命令文本里的 ~ 同样不能交给服务端展开（会解到服务器 OS 的
        HOME，对象写进 C:/Users/... 这类怪路径）。单词首的 ~/path 与
        裸 ~ 在客户端展开为会话主目录。"""
        usr = self.session.usr or ""
        home = "/root" if usr == "root" else "/home/" + usr
        return re.sub(r"(?<![\w/])~(?=/|\s|$)", home, text)

    def cmd(self, text):
        guard_msys_text(text)
        text = self._expand_tilde(text)
        st, body = self._req("POST", "/a/run/wn-term",
                             form={"cmd": text, "mime": "text/plain"})
        if st >= 400:
            raise WnError(EXIT_BAD, body.decode("utf-8", "replace").strip()[:500]
                          or ("HTTP %d" % st))
        return body.decode("utf-8", "replace")

    # ---------------- 对象操作 ----------------
    def obj_get(self, path):
        path = self._ph(path)
        st, body = self._req("GET", "/o/fetch", params={"str": path})
        self._check(st, body)
        return json.loads(body.decode("utf-8", "replace"))

    def obj_children(self, path):
        path = self._ph(path)
        st, body = self._req("GET", "/o/children", params={"str": path, "pg": "0"})
        self._check(st, body)
        return json.loads(body.decode("utf-8", "replace"))

    def obj_set(self, path, metas):
        """POST /o/update：整个 body 就是 JSON，元数据平铺 + str 键定位对象
        （ObjUpdateController 只从 body 取，query 参数一律忽略）。"""
        path = self._ph(path)
        payload = dict(metas or {})
        payload["str"] = path
        st, body = self._req("POST", "/o/update",
                             body=json.dumps(payload, ensure_ascii=False),
                             headers={"Content-Type": "application/json"})
        self._check(st, body)
        return json.loads(body.decode("utf-8", "replace"))

    def obj_save_text(self, path, content, create=True):
        path = self._ph(path)
        st, body = self._req("POST", "/o/save/text",
                             form={"str": path, "content": content,
                                   "cine": "1" if create else "0"})
        self._check(st, body)
        return json.loads(body.decode("utf-8", "replace"))

    def obj_content(self, path, download=False):
        path = self._ph(path)
        q = urllib.parse.quote(os.path.basename(path) or "obj", safe="")
        st, body = self._req("GET", "/o/content/%s" % q,
                             params={"str": path, "d": "1" if download else "0"})
        self._check(st, body)
        return body

    def obj_upload(self, path, file_path, mode="r"):
        """原始字节流上传到 /o/save/stream（QueryStringAdaptor：
        参数走 query string，body 就是文件内容本身）。
        mode: r=替换/自动 s=严格(目标文件必须存在) a=追加(按模板重名避让)
        注意不是 /o/upload——该端点已不在 web2 路由里。"""
        path = self._ph(path)
        file_path = Path(file_path)
        data = file_path.read_bytes()
        mime = mimetypes.guess_type(file_path.name)[0] \
            or "application/octet-stream"
        params = {"str": path, "nm": file_path.name, "m": mode,
                  "sz": str(len(data)), "mime": mime}
        st, rb = self._req("POST", "/o/save/stream", params=params, body=data,
                           headers={"Content-Type":
                                    "application/octet-stream"})
        self._check(st, rb)
        return json.loads(rb.decode("utf-8", "replace"))

    def obj_find(self, pairs, limit=100):
        """GET /o/query：list 是查询条件 JSON 数组（单对象内多键 = AND，
        多对象 = OR），limit 为分页大小（ObjQueryController）。"""
        conds = [dict(pairs)] if pairs else [{}]
        params = {"list": json.dumps(conds, ensure_ascii=False),
                  "limit": str(limit)}
        st, body = self._req("GET", "/o/query", params=params)
        self._check(st, body)
        return json.loads(body.decode("utf-8", "replace"))

    # ---------------- 挂载（含自检） ----------------
    def mnt(self, path, file_url, check=True):
        path = self._ph(path)
        if not file_url.startswith("file://"):
            file_url = "file://" + file_url.replace("\\", "/")
        # 目标目录不存在先建（已存在会报 exists，忽略即可）
        try:
            self.cmd('mkdir "%s"' % path)
        except WnError:
            pass
        # 注意：命令通道里双引号要用 \" 转义（box 会吃掉裸双引号），
        # 否则 @update 的值解析会失败
        self.cmd('o "%s" @update \'mnt:\\"%s\\"\'' % (path, file_url))
        if not check:
            return {"mounted": path, "url": file_url, "checked": False}
        # 自检：读不回来 / 报错即异常（守则见 50-at-rules）
        try:
            listing = self.cmd('o "%s" -l' % path)
        except WnError as e:
            raise WnError(EXIT_BAD,
                          "挂载自检失败: %s（挂载可能未生效，请示主人）" % e.msg)
        if not listing.strip():
            raise WnError(EXIT_BAD,
                          "挂载自检失败: 目标目录列出为空（mnt-file-allow 白名单挡了？）")
        return {"mounted": path, "url": file_url, "checked": True,
                "listing_head": listing.strip().splitlines()[:10]}

    # ---------------- 节点初始化（root，见 wn-doc/public/reference/102.auto-test/） ----------------
    def check_node(self):
        """看 /app 权限就知道节点是否初始化过（空白库启动时是 0750）。
        返回 initialized + 细节，自身总是成功（除非连不上）。"""
        j = self.obj_get("/app")
        data = j.get("data") or {}
        md = data.get("md")
        return {"initialized": md == 493, "app_md": md, "app_ph": data.get("ph")}

    def init_node(self):
        """空白数据库节点的首次初始化（幂等，可重复跑）。
        步骤来自 wn-doc/public/reference/102.auto-test：
        1. 顶级目录放开读+执行
        2. 系统钩子（缩略图/视频预览）
        3. MongoDB 索引（mgadmin）
        必须以 root 会话调用。返回每步结果汇总。
        """
        if self.session.usr != "root":
            raise WnError(EXIT_AUTH, "init-node 必须用 root 会话执行（当前: %s）"
                          % self.session.usr)
        steps = {}

        # 1. 顶级目录权限（逐条执行：不存在的路径只跳过自己，
        #    不能把 /app 的权限也拖下水——曾因 /mnt 缺失导致整组失败）
        chmods = ["chmod -R +rx /etc",
                  "chmod +rx /sys", "chmod +rx /var", "chmod +rx /home",
                  "chmod +rx /mnt", "chmod +rx /app",
                  "chmod -R +rx /sys/hook",
                  "chmod -R +rx /rs",
                  "chmod -R +rx /jsbin"]
        ok, fail = [], []
        for c in chmods:
            try:
                self.cmd(c)
                ok.append(c)
            except WnError as e:
                fail.append({"cmd": c, "err": e.msg})
        steps["chmod"] = {"ok": ok, "ignored": fail}  # 忽略的多半是路径不存在

        # 2. 系统钩子（与 ROOT/jsbin/init_root_hooks.js 等价，内联避免依赖挂载）
        hooks = [
            ("create_thumbnail",
             'hook_by:[{mime:"^image/",ph:"!^/home/.+/(.thumbnail/gen|.publish/gen|www)"}]',
             "iimg id:${id} -thumb 256x256 -Q"),
            ("create_video_preview",
             'hook_by:[{mime:"^video/",ph:"!^/home/.+/(.thumbnail/gen|.publish/gen|www)"}]',
             'videoc id:${id} -mode "preview_image"'),
        ]
        hooks_ok = []
        for nm, hook_by, body in hooks:
            ph = "/sys/hook/write/" + nm
            try:
                self.cmd('touch "%s"' % ph)
            except WnError:
                pass
            self.cmd('obj "%s" -u \'%s\'' % (ph, hook_by))
            self.cmd("echo '%s' > %s" % (body, ph))
            hooks_ok.append(ph)
        steps["hooks"] = {"created": hooks_ok}

        # 3. MongoDB 索引（已存在的索引会报错，忽略即可 —— 幂等）
        indexes = [
            ('{"id":1}', '{"name":"OBJ_ID"}', None),
            ('{"d0":1,"d1":1}', '{"name":"D0_D1"}', None),
            ('{"d0":1,"d1":1,"tp":1}', '{"name":"D0_D1_TP"}', None),
            ('{"d0":1,"d1":1,"sha1":1}', '{"sparse":true,"name":"D0_D1_SHA1"}', None),
            ('{"d0":1,"d1":1,"websocket_watch":1}',
             '{"sparse":true,"name":"D0_D1_WEBSOCKET_WATCH"}', None),
            ('{"expi":1}', '{"sparse":true,"name":"EXPI"}', None),
            ('{"pid":1}', '{"name":"PID"}', None),
            ('{"pid":1,"nm":1}', '{"name":"PID_NM"}', None),
            ('{"pid":1,"tp":1}', '{"name":"PID_TP"}', None),
            ('{"pid":1,"ct":1}', '{"name":"PID_CT"}', None),
            ('{"pid":1,"sort":1}', '{"sparse":true,"name":"PID_SORT"}', None),
            ('{"id":1}', None, "expi"),  # 过期对象表
        ]
        idx_ok, idx_fail = [], []
        for keys, opts, co in indexes:
            cmd = "mgadmin index "
            if co:
                cmd += "-co %s " % co
            cmd += "create '%s'" % keys
            if opts:
                cmd += " '%s'" % opts
            try:
                self.cmd(cmd)
                idx_ok.append(cmd)
            except WnError as e:
                idx_fail.append({"cmd": cmd, "err": e.msg})
        steps["indexes"] = {"created": idx_ok, "existed_or_failed": idx_fail}
        return steps


# ============================================================
# CLI
# ============================================================
def load_creds(path):
    p = Path(path) if path else Path(".at/credentials.json")
    if p.exists():
        try:
            return json.loads(p.read_text(encoding="utf-8"))
        except (ValueError, OSError):
            pass
    return {}


def kv_pairs(pairs):
    """k=v 列表 → dict（v 尝试解析为 JSON）"""
    metas = {}
    for kv in pairs:
        k, _, v = kv.partition("=")
        if not k:
            raise WnError(EXIT_USAGE, "无效键值对: %s" % kv)
        try:
            metas[k] = json.loads(v)
        except ValueError:
            metas[k] = v
    return metas


def main():
    ap = argparse.ArgumentParser(description="Walnut 智能体客户端")
    ap.add_argument("--url", help="实例地址（缺省按 --level 从实例登记解析，再退到会话缓存）")
    ap.add_argument("--level", choices=["L1", "L2"],
                    help="实例级别：决定会话名（<级别>-<登录名>-00）与端点解析")
    ap.add_argument("--instances", help="实例登记文件（缺省 $WN_INSTANCES > 逐级向上 > 用户目录）")
    ap.add_argument("--se", help="会话名（缺省 <级别>-<登录名>-00；无级别时 se-<登录名>-00）")
    ap.add_argument("--session-dir",
                    help="会话缓存目录（缺省：$WN_SESSION_DIR > 工作区级 .at/client/session > 项目级）")
    ap.add_argument("--dist", help="dist/wn4j 目录（root 密码发现）")
    ap.add_argument("--who", help="智能体代号（写入会话缓存 agent.name）")
    ap.add_argument("--creds", help="凭据 JSON 文件")
    sub = ap.add_subparsers(dest="sub", required=True)

    sp = sub.add_parser("login", help="登录（含建域决策树）")
    sp.add_argument("--usr", help="测试域用户名")
    sp.add_argument("--passwd", help="测试域密码（root 会话缺省自动从实例配置发现）")
    sp.add_argument("--root-usr", help="root 用户名（默认 root）")
    sp.add_argument("--root-passwd", help="root 密码（建域用；缺省按实例配置发现）")

    sp = sub.add_parser("cmd", help="执行命令文本")
    sp.add_argument("text", help="命令文本，'-' 表示从 stdin 读")

    sp = sub.add_parser("obj", help="对象操作")
    osb = sp.add_subparsers(dest="obj_sub", required=True)
    p = osb.add_parser("get"); p.add_argument("path")
    p = osb.add_parser("children"); p.add_argument("path")
    p = osb.add_parser("set"); p.add_argument("path"); p.add_argument("pairs", nargs="+")
    p = osb.add_parser("save-text"); p.add_argument("path")
    p.add_argument("--text"); p.add_argument("--file")
    p = osb.add_parser("content"); p.add_argument("path"); p.add_argument("--out")
    p = osb.add_parser("upload"); p.add_argument("local_file"); p.add_argument("target")
    p.add_argument("--mode", default="r", choices=["r", "s", "a"])
    p = osb.add_parser("find"); p.add_argument("pairs", nargs="*")
    p.add_argument("--limit", type=int, default=100)

    sp = sub.add_parser("mnt", help="挂载本地目录（自动自检）")
    sp.add_argument("path", help="域内目标路径")
    sp.add_argument("file_url", help="本地目录，如 D:/work/proj/www")
    sp.add_argument("--no-check", action="store_true")

    sp = sub.add_parser("check-node", help="检查节点是否已初始化（看 /app 权限）")
    sp = sub.add_parser("init-node", help="空白节点初始化：目录权限/系统钩子/Mongo 索引（须 root）")

    args = ap.parse_args()
    creds = load_creds(args.creds)
    dist = args.dist or creds.get("dist")

    instances, inst_file = load_instances(explicit=args.instances)
    level = args.level or level_of(args.url, instances)
    # 没给 --url 但知道级别：直接用登记里的端点（智能体"自己找到地方"）
    base = args.url or endpoint_of(level, instances)
    # 会话池默认落在工作区级：同一工作区的所有项目共用一份登录
    session_dir = resolve_session_dir(args.session_dir, inst_file)

    try:
        # 会话名约定：<级别>-<登录名>-<序号>（如 L1-demo-00）；
        # 没有级别时沿用旧约定 se-<登录名>-00，保证向后兼容。
        usr_for_se = args.usr or creds.get("usr") if args.sub == "login" else None
        if args.se:
            se = args.se
        elif usr_for_se:
            se = "%s-%s-00" % (level or "se", usr_for_se)
        elif level:
            se = pick_session(session_dir, level)
        else:
            se = "se-00"

        c = WnClient(base, session_dir=session_dir, se=se)
        if dist:
            c.session.dist = dist

        if args.sub == "login":
            usr = args.usr or creds.get("usr")
            passwd = args.passwd or creds.get("passwd")
            if not usr:
                raise WnError(EXIT_USAGE, "缺少 --usr（或凭据文件里的 usr）")
            root_usr = args.root_usr or creds.get("root_usr") or "root"
            # root 会话: 密码从实例配置发现（web_local.properties →
            # web.properties → 缺省 123456），失败即终止询问用户
            if usr == root_usr and passwd is None:
                passwd = guess_root_passwd(dist) or "123456"
            elif passwd is None:
                passwd = getpass.getpass("密码: ")
            root_passwd = (args.root_passwd if args.root_passwd is not None
                           else creds.get("root_passwd") or None)
            root_passwd = guess_root_passwd(dist, root_passwd) or "123456"
            # 智能体身份: --who > .at/whoami.toml > null（人类使用）
            c.session.agent = merge_agent(c.session.agent,
                                          resolve_agent_name(args.who))
            ticket, how = c.login_or_create_domain(usr, passwd, root_usr, root_passwd)
            print(json.dumps({"ok": True, "action": how, "usr": usr,
                              "base": c.base,
                              "session": str(c.session.path)},
                             ensure_ascii=False))
            return EXIT_OK

        if args.sub == "cmd":
            text = sys.stdin.read() if args.text == "-" else args.text
            sys.stdout.write(c.cmd(text))
            return EXIT_OK

        if args.sub == "obj":
            o = args.obj_sub
            if o == "get":
                print(json.dumps(c.obj_get(args.path), ensure_ascii=False))
            elif o == "children":
                print(json.dumps(c.obj_children(args.path), ensure_ascii=False))
            elif o == "set":
                print(json.dumps(c.obj_set(args.path, kv_pairs(args.pairs)),
                                 ensure_ascii=False))
            elif o == "save-text":
                if args.file:
                    content = Path(args.file).read_text(encoding="utf-8")
                elif args.text is not None:
                    content = args.text
                else:
                    content = sys.stdin.read()
                print(json.dumps(c.obj_save_text(args.path, content),
                                 ensure_ascii=False))
            elif o == "content":
                data = c.obj_content(args.path)
                if args.out:
                    Path(args.out).write_bytes(data)
                    print(json.dumps({"ok": True, "bytes": len(data),
                                      "out": args.out}, ensure_ascii=False))
                else:
                    sys.stdout.buffer.write(data)
            elif o == "upload":
                print(json.dumps(c.obj_upload(args.target, args.local_file,
                                              args.mode), ensure_ascii=False))
            elif o == "find":
                print(json.dumps(c.obj_find(kv_pairs(args.pairs), args.limit),
                                 ensure_ascii=False))
            return EXIT_OK

        if args.sub == "mnt":
            print(json.dumps(c.mnt(args.path, args.file_url,
                                   check=not args.no_check),
                             ensure_ascii=False, indent=2))
            return EXIT_OK

        if args.sub == "check-node":
            print(json.dumps(c.check_node(), ensure_ascii=False))
            return EXIT_OK

        if args.sub == "init-node":
            print(json.dumps(c.init_node(), ensure_ascii=False, indent=2))
            return EXIT_OK

    except WnError as e:
        print(json.dumps({"ok": False, "error": e.msg}, ensure_ascii=False),
              file=sys.stderr)
        return e.code


if __name__ == "__main__":
    sys.exit(main())
