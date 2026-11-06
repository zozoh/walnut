#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
walnut-at-init — Agent 测试工作目录（.at/）脚手架

默认只初始化**客户端**（连接别人启动的实例）：
生成 .at/whoami.json（身份牌）、.at/client/（会话缓存）、
.at/_ottl/（一次性测试工具）、ATEST.md / LOCAL_REF.md 手册样例。

只有显式加 --with-server 才生成 .at/walnut/（级别 3 私有服务器配置）与
.at/redis/data/（独立 redis 数据空间），且必须提供运行时服务配置
build.rs.json（--rs > ./build.rs.json > {Walnut}/build/build.rs.json）——
它只作首次生成 conf 的素材，不会被复制进 .at/；生成后
.at/walnut/conf/web_local.properties 即权威配置，直接改它。
没有 build.rs.json 就不允许启用服务器 —— 智能体不能私自起服务器，
这是默认行为，也是我们希望的行为。

本工具是**懒惰**的：要创建的文件/目录已存在就跳过，绝不重置
已有的测试经验与工具积累。需要重置时，人类手动删除。

公共工具（wn-ping/wn-up/wn-down/wn-cli）在 Walnut 仓库 bin/ 下，
不在 .at/ 里 —— 每个智能体都直接用同一份。

用法:
    python walnut-at-init.py --name big-dad [目标目录]            # 仅客户端
    python walnut-at-init.py --name big-dad --with-server [--rs build.rs.json]

退出码: 0 成功；1 用法错误；2 配置缺失
"""

import argparse
import json
import re
import sys
from pathlib import Path

EXIT_OK = 0
EXIT_USAGE = 1
EXIT_NO_RS = 2

NAME_RE = re.compile(r"^[a-z0-9][a-z0-9-]{1,31}$")

# 本脚本住在 Walnut 仓库 bin/walnut-at-init/ 里，仓库根 = 上两级
WALNUT_ROOT = Path(__file__).resolve().parents[2]

GITIGNORE_BLOCK = """
# .at 敏感与运行时内容（骨架与样例手册照常提交）
.at/whoami.json
.at/credentials*.json
.at/client/session/*.json
.at/walnut/conf/
.at/walnut/buck/
.at/walnut/swap/
.at/walnut/log/
.at/redis/data/*
!.at/redis/data/.gitkeep
.at/_ottl/*
!.at/_ottl/.gitkeep
"""

ATEST_SAMPLE = """# ATEST — 本项目 Agent 测试手册

> 本文档是 Agent 在本项目执行 Walnut 后台测试的唯一权威说明。
> 通用约定见 Walnut 仓库 `doc/reference/102.auto-test/` 系列。
> 凭据不要写进本文档，放 `.at/credentials.json`（已 gitignore）。

- walnut_url: http://localhost:8080
- walnut_level: 1            # 1=共享实例 / 2=独立+默认conf / 3=独立+私有conf
- walnut_dist: <级别2/3必填：dist/wn4j 路径>
- test_domain: demo
- credentials_file: .at/credentials.json
- init_template: <建域后执行 `app init` 的 .init 模板位置>
- sql_datasource: default    # 域内 .dao 配置名，数据操作走 sqlx

## 协作

<同域的其他 Agent（看各自的 .at/whoami.json 代号）及分工；没有则写"无">

## 模拟数据策略

<测试数据怎么造：通道选择（cmd/obj/mnt/sqlx）、fixture 位置、批量灌数脚本>

## reset

<重置数据的具体步骤：清哪些表（sqlx）、删哪些对象（o @delete）>
"""

LOCAL_REF_SAMPLE = """# LOCAL_REF — 本工作目录引用的工程仓库

| 名称 | 路径 | 用途 |
|------|------|------|
| walnut | D:/workspace/git/github/zozoh/walnut | 构建(build/)、公共工具(bin/)、产物(dist/wn4j) |
"""


def write(path: Path, content: str, overwrite=True):
    if path.exists() and not overwrite:
        return False
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8", newline="\n")
    return True


def resolve_rs(work: Path, rs_arg):
    """--rs > <work>/build.rs.json > {Walnut}/build/build.rs.json"""
    cands = []
    if rs_arg:
        cands.append(Path(rs_arg))
    cands.append(work / "build.rs.json")
    cands.append(WALNUT_ROOT / "build" / "build.rs.json")
    for c in cands:
        if c.exists():
            return json.loads(c.read_text(encoding="utf-8")), str(c)
    return None, None


def gen_server_conf(rs: dict, work: Path) -> str:
    """由 build.rs.json 生成级别 3 私有 web_local.properties。

    生成后本文件即权威配置——改端口/db/桶目录直接改这里，
    不再回源 build.rs.json（它只是首次生成的素材）。
    注意这份 conf 必须**完整自足**：classpath 里它排在 core/conf
    之前，缺了的键不会按预期回退（内核默认 app-root/init-mnt 是
    开发者机器路径，缺了 Jetty 会拿 conf 目录当 webapp 根直接起不来）。"""
    app_port = int(rs.get("app-port") or 8090)
    mo, rd = rs.get("mongo", {}), rs.get("redis", {})
    dist = (WALNUT_ROOT / "dist" / "wn4j").resolve()
    lines = [
        "# 级别 3 私有配置 —— 由 walnut-at-init 依据 build.rs.json 首次生成",
        "# 本文件即权威配置：之后改端口/db/桶目录直接改这里，",
        "# 不要回源 build.rs.json；只有想整个重铺时才 --force --with-server",
        "#",
        "# 若构建产物不在默认位置（%s），" % dist.as_posix(),
        "# 改下面 app-root 与 init-mnt 两节的路径",
        "app-root=%s/WebContent" % dist.as_posix(),
        "",
        "# 登录模式：智能体测试的硬性前提（见 102.auto-test/20-at-setup.md）",
        "app-module-sys-login=PASSWD_ONLY",
        "app-module-dmn-login=PASSWD_ONLY",
        "app-port=%d" % app_port,
        "admin-port=%d" % (app_port + 1),
        "",
        "mongo-host=%s" % mo.get("host", "127.0.0.1"),
        "mongo-port=%s" % mo.get("port", 27017),
        "mongo-db=%s" % mo.get("db", "walnut_at"),
    ]
    if mo.get("usr"):
        lines.append("mongo-usr=%s" % mo["usr"])
        lines.append("mongo-pwd=%s" % mo.get("passwd", ""))
    lines += [
        "redis-host=%s" % rd.get("host", "127.0.0.1"),
        "redis-port=%s" % rd.get("port", 6379),
        "redis-database=%s" % rd.get("db", 9),
    ]
    if rd.get("passwd"):
        lines.append("redis-password=%s" % rd["passwd"])
    buck = rs.get("buck")
    swap = rs.get("swap")
    lines.append("global-bm-bucket=%s" %
                 (buck if buck and buck != "auto" else str(work / ".at/walnut/buck")).replace("\\", "/"))
    lines.append("global-bm-swap=%s" %
                 (swap if swap and swap != "auto" else str(work / ".at/walnut/swap")).replace("\\", "/"))
    lines += [
        "",
        "# 初始化挂载：与二级产物一致（内置终端 + 默认登录页）",
        "init-mnt:",
        "/app/wn-term            : file://%s/ROOT/app/wn-term" % dist.as_posix(),
        "/etc/hosts.d/default    : file://%s/ROOT/etc/hosts.d/default" % dist.as_posix(),
        "# ~ End of init-mnt",
        "",
        "# 允许挂载的本地目录：本工作目录（智能体挂自己的工程）",
        "mnt-file-allow:",
        work.as_posix(),
        "# ~ End of mnt-file-allow",
    ]
    return "\n".join(lines) + "\n"


def main():
    ap = argparse.ArgumentParser(description="生成 .at/ 测试工作目录骨架（懒惰模式）")
    ap.add_argument("--name", help="智能体代号（小写字母/数字/连字符）；"
                                   "已有 whoami.json 时可省略，沿用旧代号")
    ap.add_argument("--with-server", action="store_true",
                    help="同时生成 .at/walnut/（级别 3 私有服务器配置），"
                         "需要 build.rs.json，否则拒绝")
    ap.add_argument("--rs", help="build.rs.json 路径（缺省 ./build.rs.json > "
                                 "{Walnut}/build/build.rs.json）")
    ap.add_argument("--force", action="store_true",
                    help="刷新骨架样例文件（仍不覆盖 whoami.json 与已有会话）")
    ap.add_argument("target", nargs="?", default=".", help="目标工作目录（默认当前）")
    args = ap.parse_args()

    work = Path(args.target).resolve()
    at = work / ".at"
    created, kept = [], []

    def w(rel, content, overwrite=True):
        p = at / rel
        if write(p, content, overwrite):
            created.append(str(p.relative_to(work)))
        else:
            kept.append(str(p.relative_to(work)))

    # ---------- 身份牌：最高优先，绝不覆盖 ----------
    whoami = at / "whoami.json"
    if whoami.exists():
        try:
            name = json.loads(whoami.read_text(encoding="utf-8")).get("name")
        except (ValueError, OSError):
            name = None
        if not name:
            print(json.dumps({"ok": False,
                              "error": "whoami.json 已存在但内容无效，请人工修复"},
                             ensure_ascii=False))
            return EXIT_USAGE
        kept.append(str(whoami.relative_to(work)))
    else:
        name = args.name
        if not name or not NAME_RE.match(name):
            print(json.dumps({"ok": False,
                              "error": "代号需匹配 %s（如 big-dad）；"
                                       "已有身份牌时可省略 --name" % NAME_RE.pattern},
                             ensure_ascii=False))
            return EXIT_USAGE
        w("whoami.json", json.dumps({
            "name": name,
            "created_at": __import__("datetime").datetime.now().isoformat(timespec="seconds"),
            "note": ""
        }, ensure_ascii=False, indent=2) + "\n", overwrite=False)

    # ---------- 客户端骨架（默认行为） ----------
    w("client/session/.gitkeep", "", overwrite=args.force)
    w("_ottl/.gitkeep", "", overwrite=args.force)

    # ---------- 服务器骨架（仅 --with-server） ----------
    if args.with_server:
        rs, rs_path = resolve_rs(work, args.rs)
        if rs is None:
            print(json.dumps({"ok": False,
                              "error": "--with-server 需要 build.rs.json（运行时服务配置），"
                                       "找了: --rs 参数、%s、%s，都不存在。"
                                       "请向主人确认 mongo/redis 等信息后，"
                                       "从 Walnut 仓库 build/build-example.rs.json "
                                       "复制一份再运行" % (work / "build.rs.json",
                                                  WALNUT_ROOT / "build" / "build.rs.json")},
                             ensure_ascii=False, indent=1))
            return EXIT_NO_RS
        # 生成级别 3 私有 conf（本文件即权威配置，见 gen_server_conf 头注释）
        # 注意：不把 build.rs.json 复制进 .at/walnut/ —— 它只是首次生成的
        # 素材，运行期一切信息都在 conf 里，留档只会制造"改哪个"的歧义
        w("walnut/conf/web_local.properties", gen_server_conf(rs, work),
          overwrite=args.force)
        # 日志配置：以 build/ 模板生成，日志目录注入本工作目录
        # .at/walnut/log/（三级启动的日志跟数据一样留在工作目录）
        src_log4j = WALNUT_ROOT / "build" / "log4j2.xml"
        if src_log4j.exists():
            log_dir = (work / ".at/walnut/log").as_posix()
            log_xml = src_log4j.read_text(encoding="utf-8")
            w("walnut/conf/log4j2.xml", log_xml.replace("@LOG_DIR@", log_dir),
              overwrite=args.force)
        w("walnut/buck/.gitkeep", "", overwrite=args.force)
        w("walnut/swap/.gitkeep", "", overwrite=args.force)
        w("walnut/log/.gitkeep", "", overwrite=args.force)
        # 级别 3 也可能要独立 redis 数据空间（start_with_redis.cmd
        # 会从 --conf 推导出 <work>/.at/redis/data）
        w("redis/data/.gitkeep", "", overwrite=args.force)
        print("服务器配置依据: %s" % rs_path, file=sys.stderr)

    # ---------- 手册样例（已存在不覆盖；--force 刷新） ----------
    for rel, sample in (("ATEST.md", ATEST_SAMPLE),
                        ("LOCAL_REF.md", LOCAL_REF_SAMPLE)):
        p = work / rel
        if write(p, sample, overwrite=args.force):
            created.append(rel)
        else:
            kept.append(rel)

    # ---------- .gitignore 追加 ----------
    gi = work / ".gitignore"
    old = gi.read_text(encoding="utf-8") if gi.exists() else ""
    if ".at/whoami.json" not in old:
        write(gi, old.rstrip("\n") + "\n" + GITIGNORE_BLOCK.strip("\n") + "\n")

    out = {"ok": True, "work": str(work), "name": name,
           "server": bool(args.with_server), "created": created, "kept": kept}
    print(json.dumps(out, ensure_ascii=False, indent=2))
    print("下一步: 完善 ATEST.md 与 .at/credentials.json，然后读 "
          "doc/reference/102.auto-test/20-at-setup.md 开始登录。", file=sys.stderr)
    return EXIT_OK


if __name__ == "__main__":
    sys.exit(main())
