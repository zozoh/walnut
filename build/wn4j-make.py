#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
build/wn4j-make.py — Walnut 构建脚本

契约见 doc/reference/102.auto-test/10-at-build.md。

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

配置:
    build/make.json         个人配置（.gitignore，不出现在仓库里）
    build/make-example.json 共享模板（make.json 缺失时回退）
    build/build.rs.json     运行时服务配置 mongo/redis/桶（.gitignore，打包 dist 必需，
                            缺失即报错；模板见 build/build-example.rs.json）

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

OUT_DIST = Conf.get("OUT_DIST") or os.getenv("WN_OUT_DIST") or str(D_WN / "dist" / "wn4j")
if not os.path.isabs(OUT_DIST):
    OUT_DIST = str(D_WN / OUT_DIST)
JAVA_HOME = Conf.get("JAVA_HOME", "")
MVN_CMD = Conf.get("MVN_CMD", "mvn")

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
    ("run", "edir"),
    ("WebContent", "dir"),
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
def do_dist():
    # 先校验运行时配置（build.rs.json），失败立即退出，
    # 避免删空 dist/wn4j 后才发现配置缺失（半毁状态）
    rs = load_rs()

    jar = D_WN_TARGET / ("walnut-%s.jar" % JAR_VER)
    if not jar.exists():
        print("!!! 找不到编译产物: %s" % jar)
        print("!!! 请先运行: python build/wn4j-make.py --mvn")
        sys.exit(1)

    # 重建输出目录
    if D_OUT.exists():
        shutil.rmtree(str(D_OUT))
    D_OUT.mkdir(parents=True)
    for d in ["conf", "core", "sbin", "deps", "ROOT", "run"]:
        (D_OUT / d).mkdir()

    # --- conf: 模板改写 ---
    mnt_allow = calc_mnt_file_allow()
    tpl = (D_BIN / "web_local.properties").read_text(encoding="utf-8")
    gen = (tpl.replace("@DIST_HOME@", str(D_OUT).replace(os.sep, "/"))
              .replace("@MNT_FILE_ALLOW@", mnt_allow)
              .replace("@MONGO_HOST@", str(rs.get("mongo", {}).get("host", "127.0.0.1")))
              .replace("@MONGO_PORT@", str(rs.get("mongo", {}).get("port", 27017)))
              .replace("@MONGO_DB@", str(rs.get("mongo", {}).get("db", "walnut_wn4j")))
              .replace("@REDIS_HOST@", str(rs.get("redis", {}).get("host", "127.0.0.1")))
              .replace("@REDIS_PORT@", str(rs.get("redis", {}).get("port", 6379)))
              .replace("@REDIS_DB@", str(rs.get("redis", {}).get("db", 9))))
    # 可选：mongo/redis 认证
    mo, rd = rs.get("mongo", {}), rs.get("redis", {})
    if mo.get("usr"):
        gen += "\nmongo-usr=%s\nmongo-pwd=%s" % (mo["usr"], mo.get("passwd", ""))
    if rd.get("passwd"):
        gen += "\nredis-password=%s" % rd["passwd"]
    # 可选：自定义桶/交换目录（默认在产物 run/ 下）
    if rs.get("buck") and rs["buck"] != "auto":
        gen = re.sub(r"^global-bm-bucket=.*$",
                     "global-bm-bucket=%s" % rs["buck"], gen, flags=re.M)
    if rs.get("swap") and rs["swap"] != "auto":
        gen = re.sub(r"^global-bm-swap=.*$",
                     "global-bm-swap=%s" % rs["swap"], gen, flags=re.M)
    (D_OUT / "conf" / "web_local.properties").write_text(gen, encoding="utf-8")
    shutil.copyfile(str(D_BIN / "log4j2.xml"),
                    str(D_OUT / "conf" / "log4j2.xml"))
    print(" ++ conf/ (web_local.properties 已生成, mnt-file-allow=%s, mongo-db=%s, redis-db=%s)"
          % (mnt_allow, rs.get("mongo", {}).get("db"), rs.get("redis", {}).get("db")))

    # --- core: 内核配置（剔除个人 web_local.properties）+ jar ---
    shutil.copytree(str(D_WN / "conf"), str(D_OUT / "core" / "conf"),
                    ignore=shutil.ignore_patterns("web_local.properties"))
    shutil.copyfile(str(jar), str(D_OUT / "core" / "walnut.jar"))
    print(" ++ core/ (walnut.jar=%s)" % JAR_VER)

    # --- deps ---
    shutil.copytree(str(D_WN_TARGET / "dependency"), str(D_OUT / "deps"),
                    dirs_exist_ok=True)

    # --- ROOT: 只取 app/wn-term 与 etc/hosts.d/default ---
    (D_OUT / "ROOT" / "app").mkdir()
    shutil.copytree(str(D_WN / "ROOT" / "app" / "wn-term"),
                    str(D_OUT / "ROOT" / "app" / "wn-term"))
    (D_OUT / "ROOT" / "etc" / "hosts.d").mkdir(parents=True)
    shutil.copytree(str(D_WN / "ROOT" / "etc" / "hosts.d" / "default"),
                    str(D_OUT / "ROOT" / "etc" / "hosts.d" / "default"))
    print(" ++ ROOT/ (app/wn-term, etc/hosts.d/default)")

    # --- sbin: 启停脚本模板 ---
    shutil.copyfile(str(D_BIN / "start.cmd"), str(D_OUT / "sbin" / "start.cmd"))
    shutil.copyfile(str(D_BIN / "stop.cmd"), str(D_OUT / "sbin" / "stop.cmd"))

    # --- WebContent ---
    shutil.copytree(str(D_WN / "WebContent"), str(D_OUT / "WebContent"))

    # --- version.json ---
    ver_json = D_OUT / "core" / "conf" / "version.json"
    ver_json.write_text(json.dumps(VERSION_INFO, indent=2), encoding="utf-8")

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
