---
title: 什么是 Walnut
module: root
keywords: [概述, 设计思想, 架构, 虚拟文件系统, 命令, 域]
source:
  - README.md
  - WnVersion.java
related: [quick-start, ../10.concepts/01-std-obj.md, ../10.concepts/05-command-model.md, ../20.io-storage/io-model.md]
---

# 什么是 Walnut

Walnut 是一个**业务系统开发基座平台**（Java 17 / Maven / 内嵌 Jetty）。它把「存储、账户、命令执行、HTTP 接口、定时任务、第三方整合」做成一套统一的基础设施，让二次开发者用**目录布局 + 元数据 + 命令脚本（可含 JS）**来组装业务系统，而不是从零写服务器代码。

## 五大核心机制

- **一切皆对象** — 一句话：所有数据是树形空间（WnObj）里的对象，字段用缩写（id/nm/sha1/mode/expi...），Unix 风格 — 详见：[标准对象](../10.concepts/01-std-obj.md)
- **统一存储** — 一句话：任何目录可声明 `mnt` 映射到 MongoDB / SQL / Redis / 本地文件 / 内存 / MQ，索引与内容（桶）分离 — 详见：[io-model](../20.io-storage/io-model.md)
- **命令拼装** — 一句话：一切操作是命令，支持管道 `|`、重定向 `>`；内置 73 个 + 扩展 84 个命令 — 详见：[命令模型](../10.concepts/05-command-model.md)
- **JS 动态逻辑** — 一句话：`jsc` 命令在域内执行 JS，脚本可读写对象、执行命令，业务逻辑可热更新 — 详见：[jsexec-api](../90.dev/jsexec-api.md)
- **文件即 HTTP 接口** — 一句话：`~/.regapi/` 下一个文件就是一个 API，内容是命令模板，请求被对象化后注入 — 详见：[api-object](../50.regapi/api-object.md)

## 系统全景

```bash
浏览器/客户端
   │ HTTP
   ▼
┌─ Jetty ────────────────────────────────────────────────┐
│ WalnutFilter(域名转发) → Nutz MVC (WnMainModule)      │
│   /a 应用与登录     AppModule                           │
│   /u 用户与会话     UsrModule                           │
│   /o 对象访问       ObjModule(缩略图/下载)              │
│   /api 注册接口     HttpApiModule ← ~/.regapi           │
├─ 命令沙箱 WnBoxService ────────────────────────────────┤
│   每条命令以「某用户的会话」身份在沙箱中执行(Wn.WC().su) │
├─ 核心服务 ────────────────────────────────────────────┤
│   WnIo(对象树+桶)  WnLoginApi(账户)  WnLockApi(锁)     │
│   cron/schedule/task(定时)  MQ(消息)  Hook(钩子)       │
├─ 存储 ────────────────────────────────────────────────┤
│   MongoDB(主索引)  Redis(锁/句柄/引用/可选桶)          │
│   SQL(可选映射)    本地文件(SHA1 桶)                    │
└─────────────────────────────────────────────────────────┘
```

## 两个世界

- **系统账户**（`/sys/usr`）：操作 Walnut 本身的账户，主组即域，数据在 `/home/$域/`。
- **域**：每个用户/团队一个域（`~` = `/home/$域`），域内用约定目录接入能力：`~/.regapi`（接口）、`~/.dao`（SQL 数据源）、`~/.weixin`（微信）、`~/.aliyun`（阿里云）等。
- 域账户（站点用户库 `~/accounts`）可凭票据升级成系统会话。

## 常用术语速查

- **WnObj / 对象**：文件系统节点的统一抽象（DIR/FILE/LINK/OBJ）
- **域 (domain/group)**：一组数据的归属单位；用户主组即其域
- **桶 (Bucket)**：对象内容的实际存储（SHA1 去重的块存储）
- **句柄 (Handle)**：打开对象读写的凭证，写句柄独占且限时
- **mnt / 映射**：目录的 `mnt` 元数据，把子树委托给其他存储
- **regapi**：HTTP 注册接口机制
- **box / 沙箱**：命令执行容器，保证每条命令有确定的用户身份

## 已弃用

以下功能仍在源码中但**不要用于新开发**，本文档体系不覆盖：www 站点模型（`cmd_www` 已删除）、Thing 数据集概念（`cmd_thing` 仅维护）、商城/站点 API 文档（留在 `doc/_old/`（待统一删除））。
