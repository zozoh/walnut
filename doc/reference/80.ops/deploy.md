---
title: 部署与运行配置
module: 80.ops
keywords: [部署, web.xml, WnMainModule, conf/ioc, web.properties, 集群, Redis 锁]
source: [web/, jetty/, conf/]
related: [domain-mapping, sys-admin, security-notes, troubleshooting, ../00.start-here/quick-start.md]
---

# 部署与运行配置

Walnut 是一个 Servlet 应用（war 包或内嵌 jetty），运行骨架由 `web.xml` + `WnMainModule` + `conf/` 组成。

## 请求处理链

`WebContent/WEB-INF/web.xml` 过滤器链：

```text
请求 → WalnutFilter（域名路由，见 [域名映射](domain-mapping.md)）
     → NutFilter（Nutz MVC，进入 WnMainModule）
     → regapi / 静态资源 / websocket ...
```

可选的 `AccessLogFilter`（web.xml 中已注释）开启访问日志。

## 主模块：WnMainModule

`src/com/site0/walnut/web/WnMainModule.java` 的注解即启动装配方式：

- **`@SetupBy(WnSetup.class)`**：启动初始化（建 /sys、/domain、默认钩子等）
- **`@IocBy(ComboIocProvider, "*js", "ioc")`**：IoC 容器读 `conf/ioc/*.js`
- **`@Modules(by = "ioc:webscan")`**：MVC 模块从 IoC 扫描
- **`@ChainBy("chain/walnut-chain.js")`**：动作链配置
- **`@Localization("msg")` / `@Views`**：国际化与视图（AjaxViewMaker/WnViewMaker）

## conf/ 目录

```text
conf/
|-- web.properties        # 核心运行参数（见下）
|-- hostmap               # 域名路由正则表（见域名映射）
|-- mime.properties       # 扩展名 → MIME
|-- log4j2.xml            # 日志
|-- chain/walnut-chain.js # Nutz 动作链
|-- ioc/
|   |-- conf.js           # 通用配置读取
|   |-- io.js             # I/O 核心：锁/句柄/索引器实现切换
|   |-- mongo.js          # MongoDB 连接与集合
|   |-- redis.js          # Redis 连接
|   |-- mq.js             # 消息队列装配
|   |-- service.js        # 业务服务装配
```

### conf/ioc/io.js 的实现切换

同一个接口有单机/集群两套实现，换 bean 名即切换：

- **锁** — 单机：`lockApiByMemory` — 集群：Redis 锁
- **句柄管理** — 单机：`ioHandleManagerByMemory` — 集群：`ioHandleManager`
- **桶管理** — 单机：`redisBM` 内存 — 集群：`redisBMFactory`
- **索引器** — 单机：— — 集群：`mongoFlatIndexerFactory`

## web.properties 关键键

- **`app-port` / `admin-port`**：服务端口 / 管理端口
- **`mongo-host` / `mongo-db`**：MongoDB 连接
- **`redis-host/port/ssl/database`**：Redis 连接
- **`global-bm-bucket/swap/autocreate`**：全局桶映射缓存位置
- **`hdl-timeout`**：句柄超时（秒，默认 3600）
- **`mq-enabled` / `mq-*`**：外部 MQ（RocketMQ 接入）
- **`init-mnt`**：启动挂载表（`路径 : file://本地目录`）
- **`init-usr-envs` / `usr-dft-meta`**：新用户默认环境变量与元数据
- **`root-init-passwd`**：root 初始密码（**生产必须改**）
- **`se-sys-du` / `se-tmp-du`**：会话时长（系统/临时，分钟）
- **`expi-obj-clean-*`**：过期对象清理线程参数
- **`main-host`**：不做域名路由的主机白名单（正则）
- **`jvmbox-pkgs`**：JVM 命令扫描包（加自定义命令包）
- **`web-module-pkgs`**：MVC 模块扫描包

## 集群部署要点

1. **Redis 锁 + 句柄**：多节点必须切到 Redis 实现（io.js），否则 `lock`/`schedule` 会重复执行；
2. **Mongo 共享**：所有节点连同一 Mongo（walnut 库）；
3. **本地挂载**：`init-mnt` 里的 `file://` 路径在各节点要一致或各自部署；
4. **负载均衡**：LB 后注意 `main-host` 白名单只放行内网主机名，让域名路由生效。

## 启动初始化（WnSetup）

首次启动自动创建：`/sys` 体系（cron/schedule/tasks/hook/...）、`/domain`、`/root` 账号（用 `root-init-passwd`）、默认全局钩子（图片缩略图）。详见 [快速开始](../00.start-here/quick-start.md)。

## 延伸阅读

- [域名映射](domain-mapping.md)
- [故障排查](troubleshooting.md)
- [快速开始](../00.start-here/quick-start.md)
