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
|   |-- everything-is-obj.md      #   一切皆对象：WnObj 字段缩写体系、race、mode 三段权限
|   |-- domain-and-home.md        #   域与主目录：/home/$grp、~ 展开、/sys /var /etc 布局
|   |-- command-pipeline.md       #   命令机制：管道/重定向、WnBox 沙箱、身份切换、man
|   |-- env-vars.md               #   环境变量：.env 文件、会话变量、PATH/APP_PATH 约定
|   |-- js-scripting.md           #   JS 脚本：jsc、执行上下文、挂载点一览
|   |-- metadata-conventions.md   #   元数据约定速查：mnt/expi/pvg/thumb/lbls/d0-d1...
|
|-- 20.io-storage/                # 存储体系：虚拟文件系统的全部机制
|   |-- io-model.md               #   索引器/桶管理器/句柄三分模型、读写路径、SHA1 去重
|   |-- mount-mapping.md          #   mnt 语法全集：mongo/dao/mem/file/redis/mq × 各桶
|   |-- dao-mapping.md            #   .dao 数据源 + .io/ix 表映射定义（字段/索引/分表）
|   |-- expiration.md             #   过期对象机制：expi、EOT 表、多节点清理竞争
|   |-- query-agg.md              #   WnQuery 查询语法与聚合(agg)分组
|
|-- 30.auth/                      # 账户、会话与权限
|   |-- accounts.md               #   系统账户(/sys/usr)与域账户(~/accounts)双轨模型
|   |-- session.md                #   会话对象、多级登录(su/exit)、票据、自动过期
|   |-- privilege.md              #   三层权限：mode 八进制 / pvg 元数据 / 组角色
|   |-- biz-privilege.md          #   业务权限模型：角色×动作 JSON、pvg 命令与服务
|   |-- auto-login.md             #   ackey 签名自动登录与 Token 机制(nonce/timestamp)
|   |-- vcode-captcha.md          #   验证码：图形/短信/邮箱、场景与目录布局
|
|-- 40.commands/                  # 命令参考：161 个命令的用法与组合
|   |-- command-model.md          #   执行模型、三种写法（简单/过滤/复杂）、76+85 全量清单
|   |-- file-obj.md               #   文件对象组：ls/cat/cp/mv/rm/find/o/mount...
|   |-- data-cmd.md               #   数据组：sqlx/entity(like,favor,score,history)/xo/val
|   |-- media-cmd.md              #   媒体组：imagic/videoc/qrcode/pdf/sheet/ooml...
|   |-- net-cmd.md                #   网络组：curl/httpc/xapi/mailx/sms/ftp/aliyun...
|   |-- sys-cmd.md                #   系统组：cron/schedule/task/mq/hook/lock/trash...
|   |-- util-cmd.md               #   工具组：base64/hexbin/strx/jsonx/datex/seq...
|   |-- o.md                      #   【重点】o 命令：对象操作瑞士军刀（上下文+过滤器）
|   |-- sqlx.md                   #   【重点】sqlx：SQL 数据库网关（模板/事务/乐观锁）
|   |-- webx.md                   #   【重点】webx：站点用户自助（登录/会话/改密/绑定）
|   |-- mailx.md                  #   【重点】mailx：邮件收发一体化（SMTP/IMAP/模板）
|   |-- tmpl.md                   #   【重点】tmpl：文本模板渲染（占位/循环/分支）
|
|-- 50.regapi/                    # HTTP 注册接口（动态 API 的核心机制）
|   |-- api-object.md             #   API 对象：文件即接口、元数据全集、命令模板
|   |-- request-object.md         #   请求对象：http-qs-*/http-header-*/body 注入规则
|   |-- path-params.md            #   路径参数：_ANY 通配、形参命名、REST 风格
|   |-- cache.md                  #   缓存策略：cache-test/duration/finger/redirect
|   |-- auth-history.md           #   站点会话注入、业务权限校验、preload、history 埋点
|
|-- 60.sched-async/               # 定时任务与异步机制
|   |-- cron-syntax.md            #   WnCron 表达式：Quartz 兼容 + T[]/D[] 扩展语法
|   |-- cron-pipeline.md          #   cron→schedule→task 三表流转与后台消费线程
|   |-- mq.md                     #   消息队列：消息格式、secret 鉴权、消费端扩展
|   |-- hooks.md                  #   钩子：六类事件、全局/用户钩子、条件匹配、JS 钩子
|   |-- notify.md                 #   通知队列：短信/微信模板消息、优先级与重试
|
|-- 70.integrations/              # 第三方整合
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
