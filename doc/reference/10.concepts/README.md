---
title: Walnut 核心概念
module: api.io
keywords: [概念, 设计初衷, 逻辑结构, IO, 对象, 域, 会话, 命令, 数据源]
source:
  - api/io/WnIo.java
  - api/io/WnObj.java
  - impl/box/WnSystem.java
related: [quick-start, ../20.io-storage/README.md, source-layout]
---

# Walnut 核心概念

本目录是 Walnut 主要抽象概念的导览。读完本篇，再按链接深入各概念文档，就能顺畅阅读全部参考手册。

```bash
10.concepts/
|-- README.md             # 本篇：设计初衷、逻辑结构、概念导览
|-- 01-std-obj.md         # 一切皆对象：id/nm、race、标准元数据、ln、expi
|-- 02-domain-home.md     # Domain 与 Home：域、主目录与目录布局
|-- 03-session-env.md     # 会话与环境变量
|-- 04-command-model.md   # 命令模型：沙箱、管道与三种命令写法
|-- 05-3x-data-source.md  # 3X 数据源：各种外部数据接入统一 IO
```

## 1. 设计初衷

Walnut 最初的想法很简单：**用统一的方式操作数据**。

现实中的数据散落在各种地方：操作系统文件、SQL 数据库、NoSQL、Redis、云存储、第三方平台的 API……
每种存储都有自己的 SDK 和心智负担。Walnut 在它们之上抽象出一层统一的对象模型（WnObj）与数据接口（WnIO），
无论什么底层，对使用者来说都是同一套「树形空间里的对象」：创建、读写、查询、授权、过期、同步，语义完全一致。

在这个基础上，Walnut **内置了大多数现用系统都需要的模块**——账户与会话、权限、命令管道、
HTTP 注册接口（regapi）、定时任务、消息队列、邮件短信、支付、微信集成——做到开箱即用。
二次开发时通常只需要写命令、配 regapi、放脚本，不需要搭基础设施。

## 2. 主要逻辑结构

```bash
+------------------------------------------------+
| JvmBox: Command        | HTTP: Jetty           |  # 调用
+------------------------------------------------+
| LoginApi:Session | Hooks | XApi | Cache | Lock |  # 逻辑模块
+------------------------------------------------+
|       WnIO: Unify Data Input/Output            |  # 数据接口
+------------------------------------------------+
|     Indexer           |      BM                |  # 实现接口
+------------------------------------------------+
| OS File | SQL | NoSql | Redis | AliYun | ...   |  # 具体实现
+------------------------------------------------+
| More 3X Data Source ...                        |  # 各个数据源
+------------------------------------------------+
```

从上往下读：

- **调用层**：命令（JvmBox）与 HTTP（Jetty 内嵌）是两种主要的触发方式，最终都落到同一套逻辑模块上
- **逻辑模块**：会话（LoginApi）、钩子（Hooks）、第三方 API 网关（XApi）、缓存（Cache）、锁（Lock）等，
  是构建业务的「积木」，彼此独立，均可被命令或 regapi 调用
- **数据接口**：WnIO 是唯一的对外数据接口，统一数据的读写查
- **实现接口**：元数据索引管理器 `WnIoIndexer`（查），内容桶管理器 `WnIoBM`（读写）——
  两者独立可插拔，元数据与内容可以存不同介质、任意组合
- **具体实现**：索引器有 Mongo/SQL/本地文件/VOF 等实现，桶有 本地文件/Redis/SQL/BML/VOF 等实现
- **3X 数据源**：各种各样的第三方数据，都可以通过映射挂进这棵统一的树（见 06）

## 3. 特殊的抽象概念

### `WnObj`: 标准对象

> `com.site0.walnut.api.io.WnObj`

对象是 `Walnut` 概念里的基本单位， 文件、目录、会话、支付单……任何数据都是一个对象。
系统有两种对象 `文件:FILE` 与 `目录:DIR`。

一个对象有两部分信息:
1. 元数据
2. 内容桶。

字段缩写体系、mode 权限、链接与 expi 机制等，详见 01。

### `WnIo`: 统一数据读写接口

> `com.site0.walnut.api.io.WnIo`

对外的唯一数据入口：**一切读写查都走 WnIo**。
使用者（命令、regapi、服务类）只面对它，不需要知道底层存储是什么。

### `WnIoIndexer`: 对象元数据索引管理器

> `com.site0.walnut.api.io.WnIoIndexer`

管对象的**元数据**：树结构、字段、查询、聚合。

### `WnIoBM`: 对象内容桶管理器

> `com.site0.walnut.core.WnIoBM`

管对象的**内容**：字节流的读写（BM = Bucket Manager）。

**对象的两部分分开保存**：元数据与内容由 Indexer 与 BM 分开保存、可存不同介质，按实现任意组合
（元数据进 SQL 表 + 内容进 Redis 桶，或 Mongo + 本地文件，皆可）。以上四个接口的详解见
数据存储与映射。

### Domain 与 Home: 域与主目录

> `com.site0.walnut.login.WnLoginApi`

域（Domain）= 用户的主组，是一个独立的数据空间与权限边界；每个用户有自己的主目录（`~`），
系统目录有全局布局约定（/home /sys /var /etc）。域内还有一组约定目录（regapi、job、lib 等），
是二次开发的核心知识。详见 03。

### 会话与环境变量

> `com.site0.walnut.login.session.WnSession`

会话是命令执行的身份载体（登录即建会话），环境变量是会话级的名值对，
承担路径查找、行为开关与业务上下文三类职责。详见 04。

### 命令沙箱与管道

> `com.site0.walnut.impl.box.JvmBox`

一切操作都是命令，基本高仿 Linux/Unix 命令模型——有标准三流、管道 `|`、
重定向 `>`，命令按写法分简单/复杂/过滤三种模式，可模块化扩展。详见 05。

### 3X 数据源

> `com.site0.walnut.ext.data.*` 等

SQL 表、Redis、阿里云 OSS、第三方 API 等外部数据，都可以通过映射或网关接入统一 IO，
用起来与普通对象无异。详见 06。

## 4. 总结

了解了上述概念，就可以顺利阅读文档了。Walnut 并没有提出什么特别怪异的高概念——
对象就是文件系统的对象，命令就是 Shell 的命令，域就是主组，会话就是登录态——
它做的事情是把这些熟悉的概念统一起来，并让每种能力都可以按需插拔。
