---
title: 快速开始：把 Walnut 跑起来
module: web
keywords: [启动, 部署, 构建, web.properties, ioc, Jetty, 安装]
source:
  - web/WnLauncher.java
  - web/setup/WnSetup.java
  - web/WnConfig.java
  - conf/web.properties
related: [what-is-walnut, first-api, deploy, ../20.io-storage/README.md]
---

# 快速开始：把 Walnut 跑起来

## 依赖环境

- **JDK 17+** — 必需：✅ — 说明：pom 指定 source/target 17
- **Maven 3.x** — 必需：✅ — 说明：标准 Maven 工程
- **MongoDB** — 必需：✅ — 说明：主索引（对象树元数据）
- **Redis** — 必需：建议 — 说明：锁/句柄/引用计数/可选桶；缺省配置下 io.js 需要可连接的 Redis
- **ffmpeg / ImageMagick** — 必需：可选 — 说明：视频转码、图像处理命令用

## 构建

```bash
# 编译（src 为源码目录，conf 作为资源目录打进 classpath）
mvn compile

# 运行全部测试（可选，约 330 个测试类）
mvn test
```

工程打包为 jar，**没有可执行 fat-jar**。本地运行的常规方式是用 IDE 运行主类，或手工拼 classpath：

```bash
mvn dependency:copy-dependencies -DoutputDirectory=target/lib
java -cp "target/classes;target/lib/*;conf" com.site0.walnut.web.WnLauncher
# Linux 下：-cp "target/classes:target/lib/*:conf"
```

`WnLauncher` 继承 nutz-web 的 `WebLauncher`：从 classpath 读 `web.properties`，按 `app-port` 启动内嵌 Jetty，挂 `app-root`（即 WebContent）。

## 最小配置（conf/web.properties）

启动前必须按本机环境修改的配置项（完整配置含注释见该文件本身）：

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

> `web.properties` 末尾的 `$include: web_local.properties` 会导入本地覆盖文件，
> 个人环境差异建议放那里，避免改动主文件。

## 首次启动时系统做了什么（WnSetup）

1. 创建/校验 **root 账户**（密码取 `root-init-passwd`）。
2. 建立系统目录树：`/sys`（账户/钩子/定时）、`/var`（session/payment/vcode）、`/etc`（hostmap/thumbnail/sysconf）、`/home`、`/bin` 等。
3. 执行 `init-mnt` 段声明的初始化映射（把 `/app/*`、`/rs/*`、`/etc/thumbnail` 等目录挂载到本地文件）。
4. 创建默认系统钩子 `/sys/hook/write/create_thumbnail`（图片写入自动生成 256 缩略图）。
5. 加载 `init-setup` 段声明的扩展 Setup（默认启用 `WnCleanExpiObjTableSetup`）。
6. 启动后台线程：**BGT_TASK**（任务栈消费）、**BGT_SCHEDULE**（分钟计划入栈）、**BGT_CRON**（cron 展开为分钟槽），三者驱动定时任务体系。

## 验证启动

```bash
# 日志出现 THREAD: BGT_* started 即就绪
curl http://localhost:8080/u/do/login/ajax?nm=root&passwd=123456
# → {"ok":true,...} 说明账户体系可用
```

## 下一步

[第一个 HTTP 接口](first-api.md)：3 分钟调通 `~/.regapi`。
