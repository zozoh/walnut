# Walnut 文档（doc/reference/）

> 本文档体系以 **AI Agent 为主要读者**，人类读者同样适用。内容以 `src/` 源码为准编写；
> `doc/_old/markdown` 与 `doc/_old/draft` 仅作历史参考，其中的内容若与本目录冲突，以本目录为准。

## AI 阅读协议

1. 找文档：先读 `INDEX.md`（全量清单，一行一篇，含 keywords 与源码模块），
   按关键词定位候选文件。
2. 判相关：读目标文件的 YAML front-matter（`module` / `keywords` / `source`），
   不相关就换下一篇，不要通读全文碰运气。
3. 下钻实现：需要核对行为时查 `SOURCEMAP.md` 找到源码入口文件，直接读源码。
4. 找不到：回到 `INDEX.md` 按「场景 → 命令 → 模块」重新检索，或读源码包结构导览
   （`90.dev/source-layout.md`）。

## 目录结构（两层）

```bash
doc/reference/
|-- README.md                    # 本文件：总入口 + 阅读协议 + 写作规范
|-- INDEX.md                      # 全量索引（AI 首选入口）
|-- SOURCEMAP.md                  # 源码包 ↔ 文档双向映射
|
|-- 00.start-here/                # 快速开始：30 分钟建立整体认知
|   |-- what-is-walnut.md         #   是什么：设计思想、五大核心机制、系统全景图
|   |-- quick-start.md            #   跑起来：构建、conf/ioc 配置、启动、系统目录初始化
|   |-- first-api.md              #   第一个 HTTP 接口：.regapi 从建文件到 curl 调通
|
|-- 10.concepts/                  # 核心概念：Walnut 的思维模型
|   |-- README.md                 #   设计初衷、逻辑结构图、概念导览
|   |-- 01-std-obj.md             #   一切皆对象：id/nm、race、标准元数据、ln、expi
|   |-- 02-domain-home.md         #   Domain 与 Home：/home/$grp、~ 展开、约定目录
|   |-- 03-session-env.md         #   会话与环境变量：.env 文件、会话变量、PATH 约定
|   |-- 04-command-model.md       #   命令模型：沙箱、管道、简单/复杂/过滤三种写法
|   |-- 05-3x-data-source.md      #   3X 数据源：SQL/Redis/OSS/XApi 接入统一 IO
|
|-- 20.io-storage/                # 存储体系：虚拟文件系统的全部机制
|   |-- README.md                 #   核心模型：IO/Indexer/BM 四接口 + 目录导航与阅读路径
|   |-- 10-io-mapping.md          #   IO 映射：索引管理器 × 桶管理器、全局映射与自定义映射
|   |-- 11-io-hooks.md            #   IO 钩子：对象生命周期六类事件的自动响应
|   |-- xo-storage.md             #   对象存储集成：xo 命令、s3/cos 配置、vofs/vxds 挂载
|   |-- 20/21/22-indexer-*.md     #   索引管理器专题：mongo（默认）/ redis（规划）/ sql
|   |-- 30~35-bm-*.md             #   桶管理器专题：sha1（默认）/ vxds / sql / redis / mem / multi
|   |-- 40-indexer-file.md        #   文件系统映射：file/filew 本地目录挂载
|   |-- 41-indexer-vofs.md        #   对象存储映射：vofs 桶即目录树
|
|-- 30.auth/                      # 账号认证、会话与权限
|   |-- README.md                 #   概述：基于 WnIo 的认证思路、三种存储模式、权限与验证码
|   |-- 10-auth-user-role.md      #   系统用户(/sys/usr)与组角色：密码 salt、Std/Sql 双存储
|   |-- 20-auth-session.md        #   会话管理：会话对象、票据轮换、su 会话树、过期
|   |-- 30-auth-in-domain.md      #   域内用户存储与认证：WnLoginSite、accounts/roles 配置
|   |-- 40-auth-hydrate.md        #   混合认证：域内用户登录建立标准账户会话
|   |-- 0-auth-pvg.md             #   标准对象的权限体系：mode / pvg / 业务权限矩阵
|   |-- 60-auth-otp-captcha.md    #   验证码、OTP：图形/短信/邮箱、场景、一发一验
|
|-- 40.commands/                  # 命令参考：73 内置 + 84 扩展，三种写法与组合手段
|   |-- README.md                 #   命令机制概览：要解决的问题、三种模式、管道与 IO
|   |-- 01-cmd-simple.md          #   简单命令（JvmExecutor）：代码形态、接口与参数详解
|   |-- 02-cmd-complex.md         #   复杂命令（JvmHdlExecutor）：hdl 子命令 + 服务类
|   |-- 03-cmd-filter.md          #   过滤命令（JvmFilterExecutor）：@过滤器共享上下文
|   |-- 04-cmd-pipe.md            #   命令管道：| 组合、变量替换、run 批量执行
|   |-- 05-cmd-std-in-out.md      #   命令输入输出：三流、> >> 2>&1 重定向、输出惯例
|   |-- 60-cmd-table.md           #   命令总表：157 个命令的来源/写法/逻辑分类
|
|-- 50.regapi/                    # HTTP 注册接口（动态 API 的核心机制）
|   |-- README.md                 #   本篇：文件即接口的思路、请求对象化、机制全景
|   |-- 10-api-object.md             #   API 对象：文件即接口、元数据全集、命令模板
|   |-- 20-request-object.md         #   请求对象：http-qs-*/http-header-*/body 注入规则
|   |-- 30-path-params.md            #   路径参数：_ANY 通配、形参命名、REST 风格
|   |-- 40-cache.md                  #   缓存策略：cache-test/duration/finger/redirect
|   |-- 50-auth-history.md           #   站点会话注入、业务权限校验、preload、history 埋点
|
|-- 60.sched-async/               # 定时任务与异步机制
|   |-- README.md                 #   本篇：三张表流水线、三个后台线程、可靠性
|   |-- 10-cron-task.md           #   cron 命令与 /sys/cron 任务定义
|   |-- 20-cron-syntax.md         #   WnCron 表达式：Quartz 兼容 + T[]/D[] 扩展语法
|   |-- 30-schedule-slot.md       #   /sys/schedule 分钟计划表：槽对象、预加载与推送线程
|   |-- 40-task-queue.md          #   /sys/tasks 后台任务栈：消费线程、弹出即删除
|
|-- 70.functionate/               # 功能场景：支付、ooml 解析、购物车等业务功能
|   |-- payment.md                #   通用支付模型：支付单、pay 命令、多平台回调
|   |-- weixin.md                 #   微信：公号/小程序配置、模板消息、oauth、JS-SDK
|   |-- alipay-aliyun.md          #   支付宝与阿里云（OSS/VOD）
|   |-- sms-mail.md               #   短信（多供应商）与邮件（mailx）
|   |-- xapi.md                   #   XAPI 通用第三方 API：配置驱动接入任意 HTTP API
|   |-- data-sync.md              #   数据同步与备份：dsync/bulk/truck/导入导出
|
|-- 80.ops/                       # 部署与运维
|   |-- deploy.md                 #   部署形态：内嵌 Jetty、web.xml/chain、ioc 配置详解
|   |-- domain-mapping.md         #   域名映射：/domain 记录、hostmap 转发规则
|   |-- sys-admin.md              #   系统管理命令：mgadmin/redis/refer/aclog/sysconf
|   |-- security-notes.md         #   安全实践：secret 存放、权限边界、常见坑
|   |-- troubleshooting.md        #   排查手册：日志位置、常见错误码、诊断命令
|
|-- 90.dev/                       # 二次开发
|   |-- source-layout.md          #   src 包结构导览：每个包一句话 + 关键类
|   |-- java-lib.md               #   作为 Java 库嵌入：核心 IoC beans、WnIo 直接编程
|   |-- custom-command.md         #   编写 cmd_* 命令：JvmExecutor、参数、注册、man 手册
|   |-- jsexec-api.md             #   JS 脚本 API 全集：sys.io/sys.exec/Wn 工具函数
|   |-- extend-regapi.md          #   扩展实践：给 regapi 写复杂后端、调第三方、模板渲染
```

## 写作规范（每篇文档必须遵守）

1. **front-matter**（机器可读，缺一则视为未完成）：

```yaml
---
title: 基础权限模型
module: core                 # 源码模块：api|core|impl|util|login|ext.xxx|web|cheap|...
keywords: [权限, mode, pvg, WnAuthSession]   # 检索关键词，中英都放
source:                      # 关联源码（相对 src/com/site0/walnut/）
  - api/io/WnObj.java
related: [accounts, biz-privilege]          # 本文档内相关文件（不带扩展名）
---
```

2. **正文结构**：是什么 → 核心概念/数据布局 → 用法（命令示例 + JS 示例，**示例必须可直接运行**，优先改写自 `test/` 用例）→ 配置参考 → 常见场景 3 个 → 注意事项 → 延伸阅读。

3. **语言**：正文中文；命令、代码、路径保持原样；示例注释中文。

4. **不写入本体系的内容**（已弃用，仅源码中保留）：www 站点模型（cmd_www 已删除）、
   Thing 数据集概念（cmd_thing 仅维护）、商城/站点 API。相关历史文档留在
   `doc/_old/markdown`、`doc/_old/draft` 不作迁移。

5. **与代码同步**：改动 `src/` 中已 documented 的行为时，同步更新对应文档；
   新增模块必须先有对应 `90.dev/source-layout.md` 条目与 INDEX.md 行。

## INDEX.md / SOURCEMAP.md 维护

- 两文件由脚本从各文档 front-matter 生成（脚本放 `tools/`），人工只改 front-matter。
- 新增/移动/删除文档后必须重新生成，CI 或提交前检查 diff 为空。
