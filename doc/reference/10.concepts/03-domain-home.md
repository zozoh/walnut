---
title: Domain 与 Home（域与主目录）
module: api.io
keywords: [域, domain, 主目录, HOME, /sys, /var, /etc, 目录布局, 约定目录]
source:
  - web/setup/WnSetup.java
  - util/Wn.java
related: [std-obj, accounts, session-env, quick-start]
---

# Domain 与 Home（域与主目录）

**域（domain）= 用户主组**。每个系统账户注册时自动创建同名组，该组的数据空间就是一个域。
所有数据、应用、配置都挂在域下，系统按域做权限隔离与资源计量。理解域，就理解了 Walnut 的多租户模型。

## 全局目录布局

```bash
/                    # 逻辑根（root 组所有）
|-- home/
|   |-- demo/        # demo 域的主目录（demo 用户即域主）
|   |-- zozoh/       # 每个用户一个域
|-- sys/             # 系统区（root 所有）
|   |-- usr/         # 系统账户
|   |-- role/        # 系统角色（uid,grp,role）
|   |-- grp/         # 用户组（旧结构，自动迁移到 role）
|   |-- hook/        # 全局钩子（create/write/delete/move/meta/mount）
|   |-- cron/        # 定期任务表
|   |-- schedule/    # 分钟计划表（含 index/）
|   |-- tasks/       # 后台任务堆栈
|   |-- job/         # 旧任务机制（已由 cron 体系取代）
|   |-- setup/usr/   # 用户创建/登录初始化脚本
|-- var/             # 运行数据区
|   |-- session/     # 系统会话
|   |-- payment/     # 支付单
|   |-- vcode/       # 验证码（/域/场景/账号）
|   |-- noti/        # 通知队列
|-- etc/             # 配置区
|   |-- hostmap      # 域名转发规则
|   |-- thumbnail/   # 默认缩略图
|   |-- sysconf      # 系统配置 JSON
|-- bin/  /sbin/     # 系统命令目录（PATH 默认含）
|-- tmp/             # 系统临时区
```

> 具体清单以 `WnSetup` 启动时创建为准；上表覆盖主要目录。

## 域内约定目录（二次开发的核心知识）

域主目录下有一批**约定目录**，系统各模块按约定自动识别：

- **`~/.regapi/api/`** — HTTP 注册接口（一文件一 API） — 详见 [first-api](../00.start-here/first-api.md)
- **`~/.regapi/tmp/`** — 请求对象临时区 — 详见 [request-object](../50.regapi/request-object.md)
- **`~/.dao/*.dao.json`** — SQL 数据源定义 — 详见 [dao-mapping](../20.io-storage/dao-mapping.md)
- **`~/.io/bm/*.json`** — Redis 桶管理器定义 — 详见 [mount-mapping](../20.io-storage/mount-mapping.md)
- **`~/.io/{s3,cos}/`** — 对象存储连接配置 — 详见 [xo-storage](../20.io-storage/xo-storage.md)
- **`~/.sqlx/*.sql`** — SQL 模板（sqlx 命令） — 详见 [sqlx](../40.commands/sqlx.md)
- **`~/.weixin/{name}/`** — 微信配置（wxconf + access_token） — 详见 [weixin](../70.integrations/weixin.md)
- **`~/.aliyun/{oss,vod}/`** — 阿里云配置 — 详见 [alipay-aliyun](../70.integrations/alipay-aliyun.md)
- **`~/.alipay/{name}/`** — 支付宝配置 — 详见 [payment](../70.integrations/payment.md)
- **`~/.mediax/{host}/`** — 媒体平台账号 — 详见 [复杂命令](../40.commands/02_cmd_complex.md)
- **`~/.mq/secret`** — MQ 密钥 — 详见 [mq](../60.sched-async/mq.md)
- **`~/.env`** — 用户环境变量 — 详见 [会话与环境变量](04-session-env.md)
- **`~/.pvg/*.json`** — 业务权限表（推荐位置） — 详见 [biz-privilege](../30.auth/biz-privilege.md)
- **`~/.hook/`** — 用户钩子 — 详见 [hooks](../60.sched-async/hooks.md)
- **`~/.dsync/*.json`** — 数据同步配置 — 详见 [data-sync](../70.integrations/data-sync.md)
- **`~/buck/ ~/swap/`** — 域级本地桶（默认映射在域下时） — 详见 [io-model](../20.io-storage/io-model.md)
- **`~/bin/`** — 用户命令目录（PATH 含 `~/bin`，可 JS 覆盖系统命令） — 详见 [自定义命令](../90.dev/custom-command.md)

## HOME 与 ~

- 登录后 `~` 展开为当前用户主目录（默认 `/home/$USER`），可用元数据 `home` 改写
- 命令中 `~`、环境变量 `$HOME`、`${domain}` 等占位符在模板渲染时展开
- 域账户登录系统后，`!HOME` 强制设置目标主目录，避免 `ti` 等命令越权读错目录

## 权限边界

- 非 root 用户默认只能读写自己域（`/home/$USER`）下的对象
- `/sys`、`/var`、`/etc` 属 root 组；域用户只能通过服务接口（登录、验证码、支付回调）间接产生其中对象
- 域名绑定即路由：`www.example.com` 的请求按 hostmap 转发到对应域的应用（详见 [域名映射](../80.ops/domain-mapping.md)）

## 深入阅读

- [账户模型](../30.auth/accounts.md)：系统账户与域账户的双轨结构
- [会话与环境变量](04-session-env.md)：登录之后发生的事
- [命令模型](05-command-model.md)：在域里执行命令的安全模型
