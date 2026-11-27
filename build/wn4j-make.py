#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
build/wn4j-make.py — Walnut 构建脚本

契约见 wn-doc/public/atest/reference/10-at-build.md。

用法:
    python build/wn4j-make.py            # 默认: 编译(mvn) + 打包(dist) + 校验
    python build/wn4j-make.py --mvn      # 仅编译核心包
    python build/wn4j-make.py --dist     # 仅打包 dist（要求已编译过）
    python build/wn4j-make.py --clear    # 清理 dist 与 target
    python build/wn4j-make.py --docker   # 构建 Docker 镜像（需 Dockerfile_* 与本脚本同目录）
    python build/wn4j-make.py --upload   # 上传镜像到仓库
    python build/wn4j-make.py --push     # = --docker + --upload
    python build/wn4j-make.py --all      # clear + mvn + dist + docker + upload
    --config/-f <file>                   # 指定配置文件

    产物同时带 start.cmd/stop.cmd（Windows 测试实例）与 start.sh/stop.sh
    （Linux 镜像：Dockerfile 的 CMD 就是 sbin/start.sh），两边都能用。

配置:
    build/make.json         个人配置（.gitignore，不出现在仓库里）
    build/make-example.json 共享模板（make.json 缺失时回退）
    build/build.rs.json     运行时服务配置 mongo/redis/桶（.gitignore，打包 dist 必需，
                            缺失即报错；模板见 build/build-example.rs.json）
    make.json 的 REDIS_SERVER: 私有 redis 来源；非空 = 产物生成
                            start/stop_with_redis.cmd 测试脚本（2/3 级完全数据
                            隔离用），并把来源烘焙进脚本；留空 = 不生成。
                            两种形态:
                              exe 路径    D:/opt/redis/redis-server.exe
                              Docker 镜像 Docker:<镜像名><sha256:...>=<容器名>
                              例 Docker:redis<sha256:bdff...>=wn4j-redis-L2
    make.json 的 APP_PORT:    二级启动的默认端口（缺省 7709），构建时经
                            web_local.properties 模板的 @APP_PORT@ 占位注入
                            产物 conf。不用 8080：那是开发者 IDE 实例的常用
                            端口，撞车时二级实例会 BindException
    make.json 的 TI_SRC:       Ti 仓库的 src 目录；非空 = 额外拷成 dist/titanium
                            （镜像 Dockerfile 里 ADD ./titanium，出镜像必须给）
    make.json 的 WEB_LOCAL_AT: 部署用的 web_local.properties；非空 = 直接用它做
                            产物 conf（跳过模板生成）。镜像里的 buck/init-mnt
                            都是容器内路径，跟构建机绝对路径不同，必须单独给；
                            此时 build.rs.json 不再必需
    make.json 的 LOG_DIR:      log4j2 的日志目录；缺省 <工作区根 .at>/walnut/log
                            （Windows 测试实例），镜像里应给 /var/log/walnut
    make.json 的 ROOT_FULL:    true = 整份拷 ROOT（jsbin/rs/sys/site_example）。
                            出镜像必须开：部署 conf 会挂 /opt/mnt/walnut/jsbin
                            与 /opt/mnt/walnut/rs/*，缺了静态资源全 404

约定:
    - 本脚本住在 Walnut 仓库的 build/ 目录里，仓库根 = 本脚本的上一级
    - 默认只构建 dist；镜像打包/上传属于显式请求（--docker/--upload/--push），
      智能体标准流程永远不要带这些参数
    - 产物布局固定（见 DIST_LAYOUT），构建结束逐项校验，缺件非零退出
"""

import json
import os
import platform
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET
from datetime import datetime
from pathlib import Path

VERSION = "2.0"

# ============================================================
# 定位: 脚本所在目录与其上一级（Walnut 仓库根）
# ============================================================
BIN_FILE = Path(os.path.abspath(sys.argv[0]))
D_BIN = BIN_FILE.parent
D_WN = D_BIN.parent

# ============================================================
# 参数解析（步骤开关）
# ============================================================
IS = {"clear": False, "mvn": False, "dist": False, "docker": False, "upload": False}
CONF_AT = D_BIN / "make.json"
ARGV = sys.argv[1:]

i = 0
while i < len(ARGV):
    arg = ARGV[i]
    if arg in ("--help", "-h"):
        print(__doc__)
        sys.exit(0)
    elif arg == "--version":
        print(VERSION)
        sys.exit(0)
    elif arg == "--info":
        pass
    elif arg == "--clear":
        IS["clear"] = True
    elif arg == "--mvn":
        IS["mvn"] = True
    elif arg == "--dist":
        IS["dist"] = True
    elif arg == "--docker":
        IS["docker"] = True
    elif arg == "--upload":
        IS["upload"] = True
    elif arg == "--push":
        IS["docker"] = True
        IS["upload"] = True
    elif arg == "--all":
        for k in IS:
            IS[k] = True
    elif arg in ("--config", "-f"):
        i += 1
        CONF_AT = Path(ARGV[i])
    else:
        print("!!! 未知参数: %s" % arg)
        print(__doc__)
        sys.exit(1)
    i += 1

# 没有任何步骤参数时 = 默认构建（mvn + dist）
if not any(IS.values()):
    IS["mvn"] = True
    IS["dist"] = True

# ============================================================
# 读取配置: make.json 缺失时回退 make-example.json
# ============================================================
if not CONF_AT.exists() and CONF_AT.name == "make.json":
    fallback = CONF_AT.with_name("make-example.json")
    if fallback.exists():
        CONF_AT = fallback

if not CONF_AT.exists():
    print("!!! 配置文件不存在: %s" % CONF_AT)
    print("!!! 请复制 build/make-example.json 为 build/make.json 后修改")
    sys.exit(1)

print("load config:> %s" % CONF_AT)
with open(CONF_AT, "r", encoding="utf-8") as f:
    Conf = json.load(f)

# 优先级：WN_OUT_DIST（临时换输出目录）> make.json 的 OUT_DIST > 仓库内 dist/wn4j
OUT_DIST = os.getenv("WN_OUT_DIST") or Conf.get("OUT_DIST") or str(D_WN / "dist" / "wn4j")
if not os.path.isabs(OUT_DIST):
    OUT_DIST = str(D_WN / OUT_DIST)
JAVA_HOME = Conf.get("JAVA_HOME", "")
MVN_CMD = Conf.get("MVN_CMD", "mvn")
# 二级启动默认端口：缺省 7709（不用 8080，避开开发者 IDE 实例）。
# 构建时经 conf 模板的 @APP_PORT@ 占位注入产物的 web_local.properties
APP_PORT = int(Conf.get("APP_PORT") or 7709)
# 管理端口 = app-port + 1。模板必须显式声明：内核 web.properties
# 写死了 admin-port=8081，不覆盖时二级实例的管理端口会错落到 8081
ADMIN_PORT = APP_PORT + 1
# 出镜像用的三项（缺省 None = 老行为，Windows 测试实例不受影响）：
#   TI_SRC        Ti 仓库 src → dist/titanium（Dockerfile 里 ADD ./titanium）
#   WEB_LOCAL_AT  部署用 web_local.properties → 直接做产物 conf，跳过模板生成
#   LOG_DIR       日志目录；缺省 <工作区根 .at>/walnut/log
TI_SRC = (Conf.get("TI_SRC") or "").strip()
WEB_LOCAL_AT = (Conf.get("WEB_LOCAL_AT") or "").strip()
LOG_DIR_CONF = (Conf.get("LOG_DIR") or "").strip()
# 整份拷 ROOT（含 jsbin/rs/sys/site_example）。测试实例只用到 wn-term 与
# 默认登录页，拷整份没必要；出镜像则必须整份——部署 conf 会把
# /opt/mnt/walnut/jsbin、/opt/mnt/walnut/rs/* 挂出去，缺了静态资源就 404。
ROOT_FULL = bool(Conf.get("ROOT_FULL", False))

# 同级前端工程的应用挂载（init-mnt）：逐一检查 <walnut仓库同级>/<name>/dist/
# index.html，存在才生成挂载行（别的开发者机器没有这些工程也能正常构建）
APP_REPOS = ["wn-hub", "wn-rich", "wn-code"]


def gen_app_mounts():
    """生成 init-mnt 的应用挂载行（@APP_MOUNTS@ 的展开内容）"""
    lines = []
    for name in APP_REPOS:
        dist_dir = D_WN.parent / name / "dist"
        if (dist_dir / "index.html").is_file():
            lines.append("/app/%s            : file://%s"
                         % (name, dist_dir.as_posix()))
        else:
            print(" -- skip app mount: %s (no %s)" % (name, dist_dir))
    return "\n".join(lines) + ("\n" if lines else "")
# redis-server 来源：非空 = 产物生成 start/stop_with_redis.cmd 测试脚本
# （私有 redis 进程生命周期，供 2/3 级完全数据隔离）；留空 = 不生成。
# 两种形态：
#   1) exe 路径      D:/opt/redis/redis-server.exe
#   2) Docker 镜像   Docker:<image><sha256:...>=<container>
#      例: Docker:redis<sha256:bdff...>=wn4j-redis-L2
RD_SERVER = (Conf.get("REDIS_SERVER", "") or "").strip()
RD_EXE = ""
RD_DOCKER_IMAGE = ""
RD_DOCKER_IMAGE_ID = ""
RD_DOCKER_CONTAINER = ""
if RD_SERVER:
    _m = re.match(r"(?i)^Docker:(.+)<([^<>]+)>=\s*([^=<>]+?)\s*$", RD_SERVER)
    if _m:
        RD_DOCKER_IMAGE = _m.group(1).strip()
        RD_DOCKER_IMAGE_ID = _m.group(2).strip()
        RD_DOCKER_CONTAINER = _m.group(3).strip()
        if not (RD_DOCKER_IMAGE and RD_DOCKER_IMAGE_ID and RD_DOCKER_CONTAINER):
            print("!!! REDIS_SERVER 的 Docker 形态解析失败: %s" % RD_SERVER)
            print("!!! 正确格式: Docker:<镜像名><sha256:...>=<容器名>")
            sys.exit(1)
    elif re.match(r"(?i)^docker:", RD_SERVER):
        print("!!! REDIS_SERVER 的 Docker 形态解析失败: %s" % RD_SERVER)
        print("!!! 正确格式: Docker:<镜像名><sha256:...>=<容器名>")
        sys.exit(1)
    else:
        RD_EXE = RD_SERVER.replace("\\", "/")

# ============================================================
# 运行时服务配置 build.rs.json（打包 dist 时必需）
# 智能体须先向主人确认 mongo/redis/db 等信息，再构建；
# 缺失时非零退出，而不是悄悄用模板默认值 —— 默认值几乎一定不对。
# ============================================================
RS_AT = D_BIN / "build.rs.json"


def load_rs():
    if not RS_AT.exists():
        print("!!! 缺少运行时服务配置: %s" % RS_AT)
        print("!!! 请复制 build/build-example.rs.json 为 build/build.rs.json，")
        print("!!! 并逐条向主人确认 mongo-db / redis-database 等不与本机")
        print("!!! 其他 Walnut 实例（尤其是开发者日常实例）冲突")
        sys.exit(1)
    with open(RS_AT, "r", encoding="utf-8") as f:
        return json.load(f)

IMG_TAG = Conf.get("IMG_TAG", "latest")
UP_HOST = Conf.get("UP_HOST", "")
UP_CORE = Conf.get("UP_CORE", "/site0/wn4j")
UP_DEPS = Conf.get("UP_DEPS", "/site0/wn4jdeps")
UP_ACC = Conf.get("UP_ACC", "")
UP_PWD_AT = Conf.get("UP_PWD_AT", str(Path(OUT_DIST) / "pass.txt"))
UP_CORE_URI = "".join([UP_HOST, UP_CORE])
UP_DEPS_URI = "".join([UP_HOST, UP_DEPS])

D_OUT = Path(OUT_DIST)
D_WN_TARGET = D_WN / "target"

# 二级启动（默认 conf）的数据家：工作区根 .at/ 目录（Walnut 仓库的上一级）——
# dist 是程序目录（删了不心疼），持久化数据（桶/swap/redis RDB）
# 全部落在这里，与智能体共享的 instances.json / client/session 同处一份 .at；
# 运行锁与日志仍是可丢状态，留在产物 run/ 下
WALNUT_AT = D_WN.parent / ".at"

# ============================================================
# 产物布局契约: (相对 dist 的路径, 校验方式)
#   file=  必须是非空文件
#   dir=   必须存在且非空目录
#   edir=  必须存在（允许为空，如 run/）
# ============================================================
DIST_LAYOUT = [
    ("deps", "dir"),
    ("conf/web_local.properties", "file"),
    ("conf/log4j2.xml", "file"),
    ("ROOT/app/wn-term/index.html", "file"),
    ("ROOT/etc/hosts.d/default", "edir"),
    ("core/conf/web.properties", "file"),
    ("core/walnut.jar", "file"),
    ("sbin/start.cmd", "file"),
    ("sbin/stop.cmd", "file"),
    ("sbin/start.sh", "file"),
    ("sbin/stop.sh", "file"),
    ("run", "edir"),
    ]

# ============================================================
# 计时
# ============================================================
ms_begin = datetime.now()
ms_mark = ms_begin


def sw(tag):
    global ms_mark
    now = datetime.now()
    print("[%8s] 用时: %s" % (tag.upper(), now - ms_mark))
    ms_mark = now


# ============================================================
# 版本号：从 pom.xml 读
# ============================================================
JAR_VER = None
VERSION_INFO = {"version": None, "alias": "--", "jar": None, "image": IMG_TAG}


def prepare_version():
    global JAR_VER
    pom = D_WN / "pom.xml"
    tree = ET.parse(str(pom))
    root = tree.getroot()
    for child in root:
        if child.tag.endswith("version"):
            JAR_VER = child.text
        elif child.tag.endswith("description"):
            VERSION_INFO["alias"] = child.text
    VERSION_INFO["version"] = JAR_VER
    VERSION_INFO["jar"] = JAR_VER


# ============================================================
# 计算 mnt-file-allow: Walnut 仓库上两级；过浅则 Windows 取盘符根，*nix 取 ~
# ============================================================
def calc_mnt_file_allow():
    repo = Path(D_WN).resolve()
    allow = repo.parent.parent
    if platform.system() == "Windows":
        anchor = Path(repo.anchor)  # 如 D:\
        if len(allow.parts) < 2:
            allow = anchor
    else:
        home = Path.home()
        if len(allow.parts) < len(home.parts):
            allow = home
    return str(allow).replace(os.sep, "/")


# ============================================================
# 清理
# ============================================================
def clear_directory(path):
    if not path.exists():
        print("> not exists: %s" % path)
        return
    print("> clear dir: %s" % path)
    shutil.rmtree(str(path), ignore_errors=True)


def do_clear():
    print("清除发布目录: %s" % D_OUT)
    clear_directory(D_OUT)
    print("清除编译目录: %s" % D_WN_TARGET)
    clear_directory(D_WN_TARGET)


# ============================================================
# 编译
# ============================================================
def do_mvn():
    env = dict(os.environ)
    if JAVA_HOME:
        env["JAVA_HOME"] = JAVA_HOME
    cmd = '%s package -DskipTests dependency:copy-dependencies' % MVN_CMD
    print("mvn:> %s  (cwd=%s)" % (cmd, D_WN))
    r = subprocess.run(cmd, shell=True, cwd=str(D_WN), env=env)
    if r.returncode != 0:
        print("!!! Maven 构建失败 (exit=%s)" % r.returncode)
        sys.exit(1)


# ============================================================
# 打包 dist
# ============================================================
def pid_alive(pid):
    if os.name == "nt":
        r = subprocess.run(["tasklist", "/FI", "PID eq %d" % pid],
                           capture_output=True, text=True)
        return str(pid) in r.stdout
    try:
        os.kill(pid, 0)
    except OSError:
        return False
    return True


def assert_no_running_instance():
    """dist/run/ 里存在活着的运行锁 = 有实例正从这个产物目录运行。

    此时重建会先删掉 conf/sbin，然后才撞上 core/walnut.jar 的文件锁
    留下半毁产物（2026-11 真实事故：7709 实例在跑时重建，
    rmtree 删了一半才报 PermissionError）。必须先拒绝。"""
    run_dir = D_OUT / "run"
    if not run_dir.is_dir():
        return
    for lock in run_dir.glob("pid-*.json"):
        try:
            pid = int(json.loads(lock.read_text(encoding="utf-8-sig")).get("pid") or 0)
        except (ValueError, OSError):
            continue
        if pid and pid_alive(pid):
            print("!!! 有实例正在从本产物运行: %s (pid=%d)" % (lock.name, pid))
            print("!!! 请先用 sbin/stop_with_redis.cmd（或 sbin/stop.cmd）停止实例，再重建")
            sys.exit(1)


def do_dist():
    # 先校验运行时配置（build.rs.json），失败立即退出，
    # 避免删空 dist/wn4j 后才发现配置缺失（半毁状态）。
    # 例外：WEB_LOCAL_AT 直接给整份部署 conf 时，构建机这侧的
    # mongo/redis/buck 全都用不上，就不强制 build.rs.json。
    rs = {} if WEB_LOCAL_AT else load_rs()
    # 同理：有实例从本产物运行时拒绝重建（运行锁是活的就是证据）
    assert_no_running_instance()

    jar = D_WN_TARGET / ("walnut-%s.jar" % JAR_VER)
    if not jar.exists():
        print("!!! 找不到编译产物: %s" % jar)
        print("!!! 请先运行: python build/wn4j-make.py --mvn")
        sys.exit(1)

    # 重建产物：dist 是程序目录，整目录删除重建，数据不在里面（见 WALNUT_AT）
    if D_OUT.exists():
        shutil.rmtree(str(D_OUT))
    D_OUT.mkdir(parents=True)
    for d in ["conf", "core", "sbin", "deps", "ROOT", "run"]:
        (D_OUT / d).mkdir()

    # 确保二级启动的数据目录存在（桶/swap/redis RDB/日志）。
    # 部署 conf 自带容器内路径时不需要这些构建机目录。
    if not WEB_LOCAL_AT:
        for d in ["walnut/buck", "walnut/swap", "walnut/log", "redis/data"]:
            (WALNUT_AT / d).mkdir(parents=True, exist_ok=True)

    # --- conf: 两种来源 ---
    #   1) 缺省：模板改写，注入端口 / mongo / redis（Windows 测试实例用）
    #   2) WEB_LOCAL_AT：整份部署 conf 直接上位。镜像里 buck / init-mnt 都是
    #      容器内路径，构建机的绝对路径写进去就是错的，所以不能靠模板生成
    mo, rd = rs.get("mongo", {}), rs.get("redis", {})
    if WEB_LOCAL_AT:
        wit = Path(WEB_LOCAL_AT)
        if not wit.is_file():
            print("!!! WEB_LOCAL_AT 指向的文件不存在: %s" % wit)
            sys.exit(1)
        shutil.copyfile(str(wit), str(D_OUT / "conf" / "web_local.properties"))
        print(" ++ conf/web_local.properties (= %s)" % wit)
    else:
        mnt_allow = calc_mnt_file_allow()
        tpl = (D_BIN / "web_local.properties").read_text(encoding="utf-8")
        gen = (tpl.replace("@DIST_HOME@", str(D_OUT).replace(os.sep, "/"))
                  .replace("@WALNUT_AT@", str(WALNUT_AT).replace(os.sep, "/"))
                  .replace("@MNT_FILE_ALLOW@", mnt_allow)
                  .replace("@APP_PORT@", str(APP_PORT))
                  .replace("@ADMIN_PORT@", str(ADMIN_PORT))
                  .replace("@APP_MOUNTS@", gen_app_mounts())
                  .replace("@MONGO_HOST@", str(mo.get("host", "127.0.0.1")))
                  .replace("@MONGO_PORT@", str(mo.get("port", 27017)))
                  .replace("@MONGO_DB@", str(mo.get("db", "walnut_wn4j")))
                  .replace("@REDIS_HOST@", str(rd.get("host", "127.0.0.1")))
                  .replace("@REDIS_PORT@", str(rd.get("port", 6379)))
                  .replace("@REDIS_DB@", str(rd.get("db", 9))))
        # 可选：mongo/redis 认证
        if mo.get("usr"):
            gen += "\nmongo-usr=%s\nmongo-pwd=%s" % (mo["usr"], mo.get("passwd", ""))
        if rd.get("passwd"):
            gen += "\nredis-password=%s" % rd["passwd"]
        # 可选：自定义桶/交换目录（默认 = 工作区根 .at/walnut/ 下，见模板 @WALNUT_AT@）
        if rs.get("buck") and rs["buck"] != "auto":
            gen = re.sub(r"^global-bm-bucket=.*$",
                         "global-bm-bucket=%s" % rs["buck"], gen, flags=re.M)
        if rs.get("swap") and rs["swap"] != "auto":
            gen = re.sub(r"^global-bm-swap=.*$",
                         "global-bm-swap=%s" % rs["swap"], gen, flags=re.M)
        (D_OUT / "conf" / "web_local.properties").write_text(gen, encoding="utf-8")
        print(" ++ conf/ (web_local.properties 已生成, app-port=%s, mnt-file-allow=%s, mongo-db=%s, redis-db=%s)"
              % (APP_PORT, mnt_allow, mo.get("db"), rd.get("db")))

    # log4j2.xml 以模板注入日志目录：二级启动的日志放数据家
    # （<工作区>/.at/walnut/log/），镜像里则给 /var/log/walnut（见 make.json 的 LOG_DIR）
    LOG_DIR = LOG_DIR_CONF or str(WALNUT_AT / "walnut" / "log")
    log_xml = (D_BIN / "log4j2.xml").read_text(encoding="utf-8")
    (D_OUT / "conf" / "log4j2.xml").write_text(
        log_xml.replace("@LOG_DIR@", LOG_DIR.replace(os.sep, "/")),
        encoding="utf-8")
    print(" ++ conf/log4j2.xml (log-dir=%s)" % LOG_DIR)

    # --- core: 内核配置（剔除个人 web_local.properties）+ jar ---
    shutil.copytree(str(D_WN / "conf"), str(D_OUT / "core" / "conf"),
                    ignore=shutil.ignore_patterns("web_local.properties"))
    shutil.copyfile(str(jar), str(D_OUT / "core" / "walnut.jar"))
    print(" ++ core/ (walnut.jar=%s)" % JAR_VER)

    # --- deps ---
    shutil.copytree(str(D_WN_TARGET / "dependency"), str(D_OUT / "deps"),
                    dirs_exist_ok=True)

    # --- ROOT: 测试实例只取 app/wn-term 与 etc/hosts.d/default；
    #     出镜像（ROOT_FULL）取整份，否则 jsbin / rs/* 在容器里是空的 ---
    if ROOT_FULL:
        shutil.copytree(str(D_WN / "ROOT"), str(D_OUT / "ROOT"), dirs_exist_ok=True)
        print(" ++ ROOT/ (整份: app, etc, jsbin, rs, sys, site_example)")
    else:
        (D_OUT / "ROOT" / "app").mkdir()
        shutil.copytree(str(D_WN / "ROOT" / "app" / "wn-term"),
                        str(D_OUT / "ROOT" / "app" / "wn-term"))
        (D_OUT / "ROOT" / "etc" / "hosts.d").mkdir(parents=True)
        shutil.copytree(str(D_WN / "ROOT" / "etc" / "hosts.d" / "default"),
                        str(D_OUT / "ROOT" / "etc" / "hosts.d" / "default"))
        print(" ++ ROOT/ (app/wn-term, etc/hosts.d/default)")

    # --- sbin: 启停脚本模板 ---
    # Windows 测试实例用 .cmd，Linux 镜像用 .sh（Dockerfile 的 CMD 就是
    # sbin/start.sh）。两个平台都产，这样从 Windows 出镜像也不会缺文件。
    shutil.copyfile(str(D_BIN / "start.cmd"), str(D_OUT / "sbin" / "start.cmd"))
    shutil.copyfile(str(D_BIN / "stop.cmd"), str(D_OUT / "sbin" / "stop.cmd"))
    for name in ("start.sh", "stop.sh"):
        dst = D_OUT / "sbin" / name
        shutil.copyfile(str(D_BIN / name), str(dst))
        # 不管谁在哪个平台构建，产物里的 .sh 必须是 LF：
        # Windows 上 core.autocrlf 会把它们签出成 CRLF，进容器就成
        # "bad interpreter: /bin/bash^M"
        dst.write_bytes(dst.read_bytes().replace(b"\r\n", b"\n"))
        dst.chmod(0o755)
    # start/stop_with_redis.cmd 是测试工具（私有 redis 进程生命周期），
    # 仅在 make.json 的 REDIS_SERVER 非空时生成，并把程序路径烘焙进脚本；
    # 留空 = 产物不带私有 redis 能力，直接用 start.cmd 起
    if RD_SERVER:
        swr = (D_BIN / "start_with_redis.cmd").read_text(encoding="utf-8")
        swr = (swr.replace("@REDIS_SERVER@", RD_EXE)
                  .replace("@RD_DOCKER_IMAGE@", RD_DOCKER_IMAGE)
                  .replace("@RD_DOCKER_IMAGE_ID@", RD_DOCKER_IMAGE_ID)
                  .replace("@RD_DOCKER_CONTAINER@", RD_DOCKER_CONTAINER)
                  .replace("@WALNUT_AT@", str(WALNUT_AT).replace(os.sep, "/")))
        (D_OUT / "sbin" / "start_with_redis.cmd").write_text(swr, encoding="utf-8")
        shutil.copyfile(str(D_BIN / "stop_with_redis.cmd"),
                        str(D_OUT / "sbin" / "stop_with_redis.cmd"))
        print(" ++ sbin/ (start/stop, start/stop_with_redis, redis=%s)" % RD_SERVER)
    else:
        print(" ++ sbin/ (start/stop; REDIS_SERVER 为空，不生成 with_redis 脚本)")

    # --- version.json ---
    ver_json = D_OUT / "core" / "conf" / "version.json"
    ver_json.write_text(json.dumps(VERSION_INFO, indent=2), encoding="utf-8")

    # --- titanium（只有出镜像才需要：Dockerfile_wn4j 里 ADD ./titanium /opt/mnt/titanium）---
    if TI_SRC:
        ti = Path(TI_SRC)
        if not ti.is_dir():
            print("!!! TI_SRC 指向的目录不存在: %s" % ti)
            sys.exit(1)
        shutil.copytree(str(ti), str(D_OUT / "titanium"))
        print(" ++ titanium/ (= %s)" % ti)
    else:
        print(" -- skip titanium/（TI_SRC 为空；出镜像需要它在产物里）")

    # --- 布局校验 ---
    missing = []
    for rel, kind in DIST_LAYOUT:
        p = D_OUT / rel
        if kind == "file" and not (p.is_file() and p.stat().st_size > 0):
            missing.append("%s (缺文件)" % rel)
        elif kind == "dir" and not (p.is_dir() and any(p.iterdir())):
            missing.append("%s (缺目录或为空)" % rel)
        elif kind == "edir" and not p.is_dir():
            missing.append("%s (缺目录)" % rel)
    if missing:
        print("!!! 产物校验失败，缺失项:")
        for m in missing:
            print("  - %s" % m)
        sys.exit(1)
    print(" >> 产物校验通过: %s" % D_OUT)


# ============================================================
# Docker（保留旧逻辑；智能体默认流程不执行）
# ============================================================
def do_docker():
    # Dockerfile_wn4j 里有 ADD ./titanium，产物缺这个目录 docker build 会直接失败
    if not (D_OUT / "titanium").is_dir():
        print("!!! Dockerfile_wn4j 会 ADD ./titanium，但产物里没有钛仓库")
        print("!!! 请在 make.json 里配 TI_SRC 后重新 --dist（或 --all）")
        sys.exit(1)
    df0 = str(D_BIN / "Dockerfile")
    df1 = str(D_OUT / "Dockerfile")
    for suf in ["_wn4jdeps", "_wn4j"]:
        if not Path(df0 + suf).exists():
            print("!!! 缺少 %s，无法构建镜像" % (df0 + suf))
            sys.exit(1)
        shutil.copyfile(df0 + suf, df1 + suf)
    subprocess.run("docker build -t wn4jdeps:%s -f ./Dockerfile_wn4jdeps ./" % IMG_TAG,
                   shell=True, cwd=str(D_OUT))
    subprocess.run("docker build -t wn4j:%s -f ./Dockerfile_wn4j ./" % IMG_TAG,
                   shell=True, cwd=str(D_OUT))


def do_upload():
    subprocess.run("docker tag wn4jdeps:%s %s:%s" % (IMG_TAG, UP_DEPS_URI, IMG_TAG),
                   shell=True, cwd=str(D_OUT))
    subprocess.run("docker tag wn4j:%s %s:%s" % (IMG_TAG, UP_CORE_URI, IMG_TAG),
                   shell=True, cwd=str(D_OUT))
    with open(UP_PWD_AT, "r", encoding="utf-8") as f:
        subprocess.run("docker login -u %s --password-stdin %s" % (UP_ACC, UP_HOST),
                       shell=True, cwd=str(D_OUT), stdin=f)
    subprocess.run("docker push %s:%s" % (UP_DEPS_URI, IMG_TAG), shell=True, cwd=str(D_OUT))
    subprocess.run("docker push %s:%s" % (UP_CORE_URI, IMG_TAG), shell=True, cwd=str(D_OUT))


# ============================================================
# 主流程
# ============================================================
prepare_version()
print(HR := "-" * 48)
print("Walnut4Java 构建 v%s" % VERSION)
print("> D_BIN   : %s" % D_BIN)
print("> D_WN    : %s" % D_WN)
print("> OUT_DIST: %s" % D_OUT)
print("> JAR_VER : %s" % JAR_VER)
print("> 步骤    : %s" % {k: v for k, v in IS.items() if v})
print(HR)

if IS["clear"]:
    do_clear()
    sw("clear")
if IS["mvn"]:
    do_mvn()
    sw("mvn")
if IS["dist"]:
    do_dist()
    sw("dist")
if IS["docker"]:
    do_docker()
    sw("docker")
if IS["upload"]:
    do_upload()
    sw("upload")

print("总用时: %s" % (datetime.now() - ms_begin))
