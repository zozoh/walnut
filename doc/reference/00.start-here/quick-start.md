---
title: 快速开始：把 Walnut 跑起来
module: web
keywords: [启动, 部署, 构建, IDE, IntelliJ, Eclipse, wn4j-make, start.cmd, web.properties, ioc, Jetty, 安装]
source:
  - web/WnLauncher.java
  - web/setup/WnSetup.java
  - web/WnConfig.java
  - conf/web.properties
  - build/wn4j-make.py
  - build/start.cmd
related: [what-is-walnut, first-api, deploy, ../102.auto-test/README.md]
---

# 快速开始概述

把 Walnut 跑起来有两条路线，按你的身份二选一：

- **路线 A：构建产物路线** — 想要一个独立可启动的 Walnut（试用、部署、
  或给自动化测试用）。一条命令构建出 `dist/wn4j/`，双击脚本启动，
  不碰 IDE。构建与产物的全部约定见 [102.auto-test](../102.auto-test/README.md) 系列。
- **路线 B：IDE 开发路线** — 要在源码上开发调试。用 IntelliJ IDEA 或
  Eclipse 导入这个 Maven 工程，直接运行主类 `WnLauncher`。

两条路线启动后行为完全一致：WnLauncher 从 classpath 读 `web.properties`，
按 `app-port` 起内嵌 Jetty，首次启动自动完成系统初始化。

## 依赖环境

- **JDK 17+** — 必需：pom 指定 source/target 17
- **Maven 3.x** — 必需：标准 Maven 工程（构建产物路线的构建脚本内部也调它）
- **MongoDB** — 必需：主索引（对象树元数据）
- **Redis** — 建议：锁/句柄/引用计数/可选桶；缺省配置下 io.js 需要可连接的 Redis
- **ffmpeg / ImageMagick** — 可选：视频转码、图像处理命令用

# 路线 A：构建产物路线

## 要解决的问题

不想装 IDE、不想研究 classpath，只想一条命令拿到一个「拿起来就能启动」
的完整 Walnut。

## 解决思路

仓库 `build/` 目录装好了构建的全部家当：共享模板提交进 git，个人配置
复制模板改名后填本机路径（个人文件已加入 `.gitignore`，不进仓库）。

**第 1 步：准备两份个人配置**

```bash
# 构建配置：指向本机仓库父目录与发布目录
cp build/make-example.json build/make.json

# 运行时服务配置：这个 Walnut 连谁家的 mongo / redis / mysql，哪个人口
cp build/build-example.rs.json build/build.rs.json
```

`build.rs.json` 是打包 dist 的**必需项**，缺了构建直接报错退出：

```jsonc
{
  "mongo": {"host": "127.0.0.1", "port": 27017, "db": "walnut_wn4j"},
  "redis": {"host": "127.0.0.1", "port": 6379, "db": 9, "passwd": ""},
  "mysql": {"host": "127.0.0.1", "port": 3306, "usr": "root", "passwd": ""},
  "app-port": 8090,
  "buck": "auto",   // 或绝对路径；auto = 产物 run/buck 下
  "swap": "auto"    // 同上
}
```

**第 2 步：构建**

```bash
python build/wn4j-make.py          # 默认: 编译(mvn) + 打包(dist) + 校验
# python build/wn4j-make.py --dist   # 已编译过时仅打包
```

产物落在 `dist/wn4j/`（固定布局：deps/ + conf/ 覆盖层 + core/walnut.jar +
sbin/ 启停脚本 + ROOT/ 内置应用）。构建结束逐项校验产物，缺件即非零
退出——看到 `ok` 就可以信任这个目录。

**第 3 步：启动与停止**

```bash
dist/wn4j/sbin/start.cmd    # 后台拉起 JVM，写运行锁 run/pid-<port>.json
dist/wn4j/sbin/stop.cmd     # 优先温柔停止（admin-port 发 stop），兜底 taskkill
```

配置双层：`conf/web_local.properties`（覆盖层，构建时由模板生成，含
`build.rs.json` 注入的连接信息）优先于 `core/conf/web.properties`
（内核默认）。想换配置目录用 `start.cmd --conf <目录>`。

智能体做自动化测试时的完整流程（探活、建测试域、三级启动）见
[102.auto-test](../102.auto-test/README.md) 系列，本篇不展开。

# 路线 B：IDE 开发路线

## 要解决的问题

在源码上开发、设断点调试，需要一个 IDE 工程：源码与依赖按 pom 解析，
`conf/` 目录在 classpath 上（`web.properties` 靠 classpath 定位），
主类一键运行。

## 共通前提

```bash
mvn compile   # 或在 IDE 里让它自动构建；首次会下载依赖
```

工程**没有可执行 fat-jar**，IDE 里直接运行主类即可。启动的实质是：

```bash
java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 \
     -cp "conf;target/classes;target/lib/*" \
     com.site0.walnut.web.WnLauncher
```

（Linux/macOS 的 classpath 分隔符用 `:`；`target/lib` 由
`mvn dependency:copy-dependencies` 生成，IDE 会用自己的依赖解析
代替它，不需要手工准备。）

## IntelliJ IDEA

1. **打开工程**：`Open` → 选仓库根目录（或选 `pom.xml` →
   `Open as Project`）。IDEA 自动识别为 Maven 工程，等待索引完成。
2. **确认 SDK**：`File → Project Structure → Project` → SDK 选 17+。
3. **把 conf 放进 classpath**：右键 `conf` 目录 →
   `Mark Directory as → Resources Root`。这一步是关键——
   `WnLauncher` 从 classpath 找 `web.properties`，不标记会启动即报
   找不到配置。
4. **建运行配置**：`Run → Edit Configurations → + → Application`：
   - Main class：`com.site0.walnut.web.WnLauncher`
   - VM options：`-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8`
   - Working directory：仓库根目录
5. **运行**，控制台出现 `THREAD: BGT_* started` 即就绪。

调试就是在第 4 步的配置上点 Debug 而不是 Run；任何命令的实现类
（`impl/box/cmd/cmd_*.java` 或 `ext/**/cmd_*.java`）都可以直接下断点，
HTTP 请求与控制台命令都会走到。

## Eclipse

仓库自带 `.project` 与 `.classpath`（m2e 工程），零配置导入：

1. **导入**：`File → Import → Maven → Existing Maven Projects` →
   选仓库根目录 → Finish。`.classpath` 里已经把 `src`（含 `conf/`，
   `excluding="**"` 即只把目录本身放进 classpath 不参与编译）和
   Maven 依赖容器配好，JavaSE-17 要求也会自动带过来。
2. **确认 JDK**：`Window → Preferences → Java → Installed JREs` 里
   确保有 JDK 17+，工程的 `Java Build Path` 指向它。
3. **运行**：右键 `WnLauncher → Run As → Java Application`。
   首次运行后在 `Run Configurations` 里把 VM arguments 补上
   `-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8`，与产物的
   启动脚本保持一致。

> Eclipse 换机器导入时如果 `.classpath` 报缺 JRE 容器，改一下
> `Java Build Path → Libraries` 里的 JRE 指向本机 JDK 17 即可，
> 其余条目不要动。

# 启动前的最小配置（conf/web.properties）

无论哪条路线，启动前必须按本机环境修改的配置项（完整配置含注释见该
文件本身）：

```properties
# Web 根目录与端口
app-root=~/workspace/git/github/walnut/WebContent
app-port=8080

# MongoDB：主索引
mongo-host=localhost
mongo-db=walnut

# Redis：锁/句柄/引用计数/桶
redis-host=127.0.0.1
redis-port=6379

# 本地桶存储目录（对象内容）
global-bm-bucket=~/.walnut/io/buck
global-bm-swap=~/.walnut/io/swap

# root 初始密码（仅首次创建 root 时生效）
root-init-passwd=123456

# 域名转发白名单（本机/内网不走路由）
main-host=^(127.0.0.1|192.168.\d+.\d+|localhost)$
```

> `web.properties` 末尾的 `$include: web_local.properties` 会导入本地
> 覆盖文件，个人环境差异建议放那里，避免改动主文件。
> 构建产物路线没有这一步——`web_local.properties` 由构建脚本按
> `build.rs.json` 生成。

# 首次启动时系统做了什么（WnSetup）

1. 创建/校验 **root 账户**（密码取 `root-init-passwd`）。
2. 建立系统目录树：`/sys`（账户/钩子/定时）、`/var`（session/payment/vcode）、
   `/etc`（hostmap/thumbnail/sysconf）、`/home`、`/bin` 等。
3. 执行 `init-mnt` 段声明的初始化映射（把 `/app/*`、`/rs/*`、
   `/etc/thumbnail` 等目录挂载到本地文件）。
4. 创建默认系统钩子 `/sys/hook/write/create_thumbnail`（图片写入自动
   生成 256 缩略图）。
5. 加载 `init-setup` 段声明的扩展 Setup（默认启用 `WnCleanExpiObjTableSetup`）。
6. 启动后台线程：**BGT_TASK**（任务栈消费）、**BGT_SCHEDULE**（分钟计划
   入栈）、**BGT_CRON**（cron 展开为分钟槽），三者驱动定时任务体系。

# 验证启动

```bash
# 日志出现 THREAD: BGT_* started 即就绪
curl http://localhost:8080/u/do/login/ajax?nm=root&passwd=123456
# → {"ok":true,...} 说明账户体系可用
# 产物路线把 8080 换成 build.rs.json 里的 app-port
```

# 延伸阅读

- [102.auto-test](../102.auto-test/README.md) — 构建产物路线的完整约定（产物布局、配置双层、
  运行时服务配置、工具集）
- [第一个 HTTP 接口](first-api.md) — 第一个 HTTP 接口：3 分钟调通 `~/.regapi`
- [部署与运行配置](../100.ops/deploy.md) — 部署形态与集群配置
