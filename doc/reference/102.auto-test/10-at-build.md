---
title: 构建 Walnut 测试服务
module: 102.auto-test
keywords: [构建, wn4j-make, dist, 构建产物, make-example.json, WebContent, 打包, build目录]
source:
  - web/WnLauncher.java
related: [README, 20-at-setup, 30-at-tools]
---

# 构建 Walnut 测试服务概述

## 要解决的问题

智能体没有 IDE，也不该为了跑测试先学 Maven。它需要的是：一个命令构建，
一个目录拿走，目录里就是能直接启动的完整 Walnut。

**这个目录就是 `dist/wn4j/`。**

## 解决思路

构建由 Walnut 仓库 `build/wn4j-make.py` 完成。脚本脱胎于 walnut-doc 仓库
`sbin/` 下的旧构建脚本，复制过来后按本系列的要求做了改造，以后构建只认
这一份。

`build/` 目录装的是构建的全部家当，构建时只从这里改写或拷入，不现场
发明任何配置：

- **`build/make-example.json`**：共享模板，提交进 git。开发者复制一份为
  `build/make.json` 再按本机修改（`make.json` 已加入 `.gitignore`，
  个人路径不进仓库）。
- **`build/start.cmd` / `build/stop.cmd`**：预置的启停脚本模板，构建时
  原样拷入产物的 `sbin/`。想调整启动行为（JVM 参数、classpath 规则），
  改这里的模板。
- **`build/web_local.properties` / `build/log4j2.xml`**：conf 层的预置
  模板。构建时 `log4j2.xml` 原样拷入；`web_local.properties` 以它为
  底改写（写入 PASSWD_ONLY 与自动计算的 mnt-file-allow，见下文）。
- **`build/build-example.rs.json` / `build/build.rs.json`**：运行时服务
  配置（见下文"运行时服务配置"一节）。模板提交进 git，个人的
  `build.rs.json` 已加入 `.gitignore`。
- **配置内容**：GIT_HOME 指向本机各工程仓库的父目录、OUT_DIST 指向发布
  目录；`WN_OUT_DIST` 环境变量可临时覆盖。
- **默认发布目录**：`<walnut>/dist/wn4j/`。仓库 `.gitignore` 已忽略
  `/dist/`，产物不会污染 git。
- **默认只构建，不碰镜像**：标准流程到 dist 为止——不打包 Docker 镜像、
  不发布到镜像仓库。脚本里保留了原有的镜像构建/上传逻辑（那是给需要
  发布的人用的），但智能体永远不要主动执行那些步骤。

产物布局（固定结构，工具脚本依赖它，构建脚本结束会逐项校验）：

```text
dist/wn4j/
|-- deps/              # 依赖的 jar 包（mvn dependency:copy-dependencies 产物）
|-- conf/              # 本地覆盖配置层（classpath 中最优先）
|   |-- web_local.properties   # 由 build/ 模板改写：PASSWD_ONLY、自动计算的 mnt-file-allow
|   |-- log4j2.xml             # 日志配置（原样拷贝自 build/ 模板）
|-- ROOT/              # init-mnt 默认挂载源，只取这两个子目录
|   |-- app/wn-term/           # 内置终端应用，挂载到 /app/wn-term
|   |-- etc/hosts.d/default/   # 系统默认登录界面，挂载到 /etc/hosts.d/default/
|-- core/
|   |-- conf/                  # 内核配置：web.properties、ioc/、i18n/ 等
|   |-- walnut.jar             # 编译产物
|-- sbin/
|   |-- start.cmd              # Windows 启动脚本（预置模板在 build/，构建时拷入）
|   |-- stop.cmd               # Windows 停止脚本（同上）
|-- run/               # 运行锁目录：pid-<port>.json（见 20-at-setup）
|-- WebContent/        # Jetty 的 web 根目录（JSP/Servlet 时代遗留，暂时照拷）
```

**构建结束时脚本按上表逐项校验产物**，缺件或空目录即以非零退出并列出
缺失项——智能体构建完看到 `ok` 就可以信任这个目录，不需要人工检查。

## 配置双层结构

启动时 classpath 顺序是 `conf/` → `core/conf/` → jar。主配置
`web.properties` 在 `core/conf/`（随 Walnut 仓库走），`conf/web_local.properties`
是覆盖层——构建时以 `build/` 的模板改写生成，不是从开发者机器现拷
（开发者的个人路径和密码模式绝不能进产物）。

**start.cmd 接受 `--conf` 参数打破这个默认值**：

```bash
start.cmd [--conf <配置目录>] [--pid-dir <运行锁目录>]
```

- `--conf`：生效配置目录；不指定就用 `<dist>/conf`。拼 classpath 时
  该目录置于 `core/conf/` **之前**，保证内容优先于内核默认配置——
  智能体级别 3 的私有 `.at/walnut/conf` 就是这么进来的。
- 端口取生效配置的最终 `app-port`（`--conf` 目录里的覆盖优先）。

## conf 模板的立场

- `app-module-sys-login` 与 `app-module-dmn-login` 都为 `PASSWD_ONLY`
  （智能体测试的硬性前提，见 [启动服务与建立测试域](20-at-setup.md)）。
- `mnt-file-allow` **构建时自动计算**：取 Walnut 仓库目录向上两级
  （父目录的父目录）。例如 Walnut 在
  `D:/workspace/git/github/zozoh/walnut`，则白名单为
  `D:/workspace/git/github`——开发者的工程都挂在下面，级别 1/2 的
  挂载需求默认被覆盖。层级过浅时：Windows 取到盘符根（如 `D:/`），
  Linux/macOS 取 `~`。

## 运行时服务配置（build.rs.json）

构建产物连的是谁家的数据库，必须由人明确指定，不能由智能体猜。
`build/build.rs.json` 就是这个声明，构建 `--dist` 时**必需**，缺失即
报错退出（且在删除旧产物**之前**校验，不会留下半毁的 dist）：

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

- 智能体构建前必须确保该文件存在，并向用户**确认里面的连接信息是
  正确的**（这是守则里的"必须请示"项）。用法：复制
  `build/build-example.rs.json` 为 `build/build.rs.json` 后按本机修改。
- 配置在构建时注入产物的 `conf/web_local.properties`（mongo-db /
  redis-database / global-bm-bucket / global-bm-swap 等），因此
  **级别 2 启动天然与开发者的实例隔离**——前提是这里填的库不是
  开发者正在用的那个。
- 级别 3 时智能体在自己的工作目录复刻这份文件（如
  `.at/walnut/build.rs.json`），同样要先向用户确认，再走
  `walnut-at-init --with-server`（见 [工作目录测试准备](40-at-project.md)）。

## 构建脚本的修改清单

相对旧版（walnut-doc/sbin 里的原版）：

- 摘除 DIR_TI / do_less / do_scss / dist titanium 目录等全部 titanium
  相关逻辑——titanium 前端库已废弃，产物里不再有它
- conf/ 产物由 `build/` 的模板生成：`web_local.properties` 写入
  PASSWD_ONLY 与自动计算的 mnt-file-allow，`log4j2.xml` 原样拷入
- 把 `build/` 里预置的 start.cmd / stop.cmd 模板拷进产物的 sbin/，
  并创建空的 `run/` 目录
- 保留原有的 Docker 镜像构建/上传逻辑不动；文档立场是智能体默认只
  构建 dist、不打包镜像、不发布（说明约束，不是删除功能）
- 默认 OUT_DIST 指向 `<walnut>/dist/wn4j/`
- 构建结束自动校验产物布局
- 配置读取改为 `make.json`（缺省回退 `make-example.json`）
- 新增 `build.rs.json` 运行时服务配置：构建 `--dist` 时必需，先于
  一切文件操作加载校验（缺失即退出，不留半毁产物），并把
  mongo/redis/桶目录等注入产物的 conf

## 主要代码类

- `build/wn4j-make.py`（Walnut 仓库）—— 构建主脚本
- `web/WnLauncher.java` —— 启动主类，从 classpath 读 web.properties

# 扩展阅读

- [启动服务与建立测试域](20-at-setup.md) —— 产物构建好后如何启动、运行锁与建域流程
- [自动化测试工具](30-at-tools.md) —— wn-ping 的契约与输出格式
