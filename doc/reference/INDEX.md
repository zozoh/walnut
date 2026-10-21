# INDEX — Walnut 文档全量索引

> AI Agent 首选入口。每行一篇：`路径 | 标题 | 源码模块 | 关键词 | 状态`
> 状态：✅ 已完成（全部 52 篇；状态由 front-matter 生成）。

## 根文档

| 路径 | 标题 | 模块 | 关键词 | 状态 |
|------|------|------|--------|------|
| README.md | 文档总入口与阅读协议 | root | 导航,规范 | ✅ |
| INDEX.md | 本文件 | root | 索引 | ✅ |
| SOURCEMAP.md | 源码包 ↔ 文档映射 | root | 源码,定位 | ✅ |

## 00.start-here

| 路径 | 标题 | 模块 | 关键词 | 状态 |
|------|------|------|--------|------|
| 00.start-here/what-is-walnut.md | 什么是 Walnut | root | 概述,设计思想,架构,五大机制 | ✅ |
| 00.start-here/quick-start.md | 快速开始：把 Walnut 跑起来 | web | 启动,部署,构建,web.properties,ioc | ✅ |
| 00.start-here/first-api.md | 第一个 HTTP 接口 | ext.sys.httpapi | regapi,HTTP接口,curl,快速上手 | ✅ |

## 10.concepts

| 路径 | 标题 | 模块 | 关键词 | 状态 |
|------|------|------|--------|------|
| 10.concepts/everything-is-obj.md | 一切皆对象 | api.io | WnObj,字段缩写,race,mode | ✅ |
| 10.concepts/domain-and-home.md | 域与主目录 | api.io | 域,主目录,HOME,/sys,/var | ✅ |
| 10.concepts/command-pipeline.md | 命令机制 | impl.box | 管道,重定向,沙箱,su,man | ✅ |
| 10.concepts/env-vars.md | 环境变量 | impl.box | env,PATH,.env,会话变量 | ✅ |
| 10.concepts/js-scripting.md | JS 脚本 | jsexec | jsc,nashorn,rhino,挂载点 | ✅ |
| 10.concepts/metadata-conventions.md | 元数据约定速查 | core | mnt,expi,pvg,thumb,lbls | ✅ |

## 20.io-storage

| 路径 | 标题 | 模块 | 关键词 | 状态 |
|------|------|------|--------|------|
| 20.io-storage/io-model.md | 索引器/桶/句柄模型 | core | 索引器,桶,句柄,SHA1去重,读写路径 | ✅ |
| 20.io-storage/mount-mapping.md | mnt 映射语法全集 | core.mapping | mnt,dao,mongo,redis,file,mq,aliyunoss | ✅ |
| 20.io-storage/dao-mapping.md | .dao 数据源与 SQL 表映射 | core.mapping | dao,数据源,表映射,字段映射,分表 | ✅ |
| 20.io-storage/expiration.md | 过期对象机制 | core.eot | expi,EOT,清理线程,多节点竞争 | ✅ |
| 20.io-storage/query-agg.md | 查询与聚合 | api.io | WnQuery,agg,分组,排序,翻页 | ✅ |

## 30.auth

| 路径 | 标题 | 模块 | 关键词 | 状态 |
|------|------|------|--------|------|
| 30.auth/accounts.md | 系统账户与域账户 | login | 账户,用户组,角色,域账户库 | ✅ |
| 30.auth/session.md | 会话机制 | login.session | 会话,票据,多级登录,su,exit | ✅ |
| 30.auth/privilege.md | 三层权限模型 | api.io | mode,pvg,组角色,admin | ✅ |
| 30.auth/biz-privilege.md | 业务权限模型 | ext.data.pvg | 角色×动作,pvg命令,WnPvgMoreService | ✅ |
| 30.auth/auto-login.md | 自动登录与 Token | login | ackey,签名,nonce,timestamp,防重放 | ✅ |
| 30.auth/vcode-captcha.md | 验证码机制 | ext.data.vcode | 验证码,图形,短信,邮箱,场景 | ✅ |

## 40.commands

| 路径 | 标题 | 模块 | 关键词 | 状态 |
|------|------|------|--------|------|
| 40.commands/command-model.md | 命令执行模型 | impl.box | JvmExecutor,man,自定义命令 | ✅ |
| 40.commands/file-obj.md | 文件对象命令组 | impl.box.cmd | ls,cat,cp,find,obj,mount,touch | ✅ |
| 40.commands/data-cmd.md | 数据处理命令组 | ext.data | sqlx,entity,like,favor,score,xo,val | ✅ |
| 40.commands/media-cmd.md | 媒体命令组 | ext.media | imagic,videoc,qrcode,pdf,sheet,ooml | ✅ |
| 40.commands/net-cmd.md | 网络命令组 | ext.net | curl,httpc,xapi,mailx,sms,ftp | ✅ |
| 40.commands/sys-cmd.md | 系统命令组 | ext.sys | cron,schedule,task,mq,hook,lock | ✅ |
| 40.commands/util-cmd.md | 工具命令组 | ext.util | base64,hexbin,strx,jsonx,datex,seq | ✅ |
| 40.commands/o.md | o 命令：对象操作瑞士军刀 | ext.data.o | @query,@update,@agg,上下文,过滤器 | ✅ |
| 40.commands/sqlx.md | sqlx：SQL 数据库网关 | ext.data.sqlx | 模板SQL,@vars,@dvcheck,事务,乐观锁 | ✅ |
| 40.commands/webx.md | webx：站点用户自助 | ext.net.webx | @auth,@session,@passwd,微信登录,动态密码 | ✅ |
| 40.commands/mailx.md | mailx：邮件收发一体化 | ext.net.mailx | SMTP,IMAP,模板邮件,附件,S/MIME | ✅ |
| 40.commands/tmpl.md | tmpl：文本模板渲染 | ext.data.tmpl | 占位符,循环,分支,默认值,WnTmpl | ✅ |

## 50.regapi

| 路径 | 标题 | 模块 | 关键词 | 状态 |
|------|------|------|--------|------|
| 50.regapi/api-object.md | API 对象与元数据 | ext.sys.httpapi | API对象,命令模板,http-header,跨域 | ✅ |
| 50.regapi/request-object.md | 请求对象 | ext.sys.httpapi | http-qs,http-header,http-cookie,body | ✅ |
| 50.regapi/path-params.md | 路径参数 | ext.sys.httpapi | _ANY,通配,REST,api-param-name | ✅ |
| 50.regapi/cache.md | 缓存策略 | ext.sys.httpapi | cache-test,cache-duration,finger,302 | ✅ |
| 50.regapi/auth-history.md | 鉴权与历史埋点 | ext.sys.httpapi | 站点会话,pvg,preload,history | ✅ |

## 60.sched-async

| 路径 | 标题 | 模块 | 关键词 | 状态 |
|------|------|------|--------|------|
| 60.sched-async/cron-syntax.md | WnCron 表达式 | cron | Quartz,T[],D[],区间,fill | ✅ |
| 60.sched-async/cron-pipeline.md | cron→schedule→task 流转 | ext.sys | 分钟槽,任务栈,后台线程 | ✅ |
| 60.sched-async/mq.md | 消息队列 | ext.sys.mq | mq,CMD消息,secret,消费者 | ✅ |
| 60.sched-async/hooks.md | 钩子机制 | api.hook | 六类事件,hook_by,JS钩子 | ✅ |
| 60.sched-async/notify.md | 通知与提醒 | ext.sys.task | 通知,短信,邮件,告警,task notify | ✅ |

## 70.integrations

| 路径 | 标题 | 模块 | 关键词 | 状态 |
|------|------|------|--------|------|
| 70.integrations/payment.md | 通用支付模型 | ext.net.payment | 支付单,pay,回调,wx,zfb,paypal | ✅ |
| 70.integrations/weixin.md | 微信集成 | ext.net.webx | 小程序,公众号,wxmp,wxgh,wxjssdk | ✅ |
| 70.integrations/alipay-aliyun.md | 支付宝与阿里云 | ext.net | 支付宝,aliyunoss,aliyunvod | ✅ |
| 70.integrations/sms-mail.md | 短信与邮件 | ext.net | sms,mailx,供应商,模板 | ✅ |
| 70.integrations/xapi.md | XAPI 第三方 API 框架 | ext.net.xapi | ThirdXApi,配置驱动,动态票据 | ✅ |
| 70.integrations/data-sync.md | 数据同步与迁移 | ext.sys.dsync | dsync,truck,tree索引,归档,还原 | ✅ |

## 80.ops

| 路径 | 标题 | 模块 | 关键词 | 状态 |
|------|------|------|--------|------|
| 80.ops/deploy.md | 部署与运行配置 | web | web.xml,conf/ioc,web.properties,集群 | ✅ |
| 80.ops/domain-mapping.md | 域名映射 | ext.data.domain | /domain,hostmap,WalnutFilter | ✅ |
| 80.ops/sys-admin.md | 系统管理命令 | ext.sys | mgadmin,redis,refer,aclog,sysconf | ✅ |
| 80.ops/security-notes.md | 安全实践 | core | secret,权限,注入,HTTPS | ✅ |
| 80.ops/troubleshooting.md | 排查手册 | web | 日志,错误码,诊断命令 | ✅ |

## 90.dev

| 路径 | 标题 | 模块 | 关键词 | 状态 |
|------|------|------|--------|------|
| 90.dev/source-layout.md | src 包结构导览 | root | 包结构,关键类,导航 | ✅ |
| 90.dev/java-lib.md | 作为 Java 库嵌入 | api | WnIo,IoC,WnRun,二次开发 | ✅ |
| 90.dev/custom-command.md | 编写自定义命令 | impl.box | cmd_*,JvmExecutor,man手册 | ✅ |
| 90.dev/jsexec-api.md | JS 脚本 API 全集 | jsexec | sys.io,sys.exec,Wn函数 | ✅ |
| 90.dev/extend-regapi.md | regapi 扩展实践 | ext.sys.httpapi | 模板渲染,第三方,复杂后端 | ✅ |

## 不收录（已弃用）

www 站点模型（cmd_www 已删除）、Thing 数据集（cmd_thing 仅维护）、商城/站点 API —— 不进入本索引；历史文档已移至 `doc/_old/`（`markdown/`、`draft/`），待统一删除。
