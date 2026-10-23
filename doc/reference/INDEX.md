# INDEX — Walnut 文档全量索引

> AI Agent 首选入口。每行一篇：`- **路径** — 标题 — 模块：源码模块 — 关键词：关键词`
> 全部 61 篇均已完成（含根文档 3 篇；状态由各文件 front-matter 的 `status` 字段生成）。

## 根文档

- **README.md** — 文档总入口与阅读协议 — 模块：root — 关键词：导航,规范
- **INDEX.md** — 本文件 — 模块：root — 关键词：索引
- **SOURCEMAP.md** — 源码包 ↔ 文档映射 — 模块：root — 关键词：源码,定位

## 00.start-here

- **00.start-here/what-is-walnut.md** — 什么是 Walnut — 模块：root — 关键词：概述,设计思想,架构,五大机制
- **00.start-here/quick-start.md** — 快速开始：把 Walnut 跑起来 — 模块：web — 关键词：启动,部署,构建,web.properties,ioc
- **00.start-here/first-api.md** — 第一个 HTTP 接口 — 模块：ext.sys.httpapi — 关键词：regapi,HTTP接口,curl,快速上手

## 10.concepts

- **10.concepts/README.md** — Walnut 核心概念 — 模块：api.io — 关键词：设计初衷,逻辑结构,概念导览
- **10.concepts/01-io-indexer-bm.md** — IO / Indexer / BM — 模块：api.io — 关键词：WnIo,Indexer,BM,索引器,桶管理器,mnt
- **10.concepts/02-std-obj.md** — 标准对象（一切皆对象） — 模块：api.io — 关键词：WnObj,字段缩写,race,mode,元数据,pvg
- **10.concepts/03-domain-home.md** — Domain 与 Home — 模块：api.io — 关键词：域,主目录,HOME,/sys,/var,约定目录
- **10.concepts/04-session-env.md** — 会话与环境变量 — 模块：login — 关键词：会话,session,env,PATH,.env
- **10.concepts/05-command-model.md** — 命令模型 — 模块：impl.box — 关键词：命令,沙箱,管道,简单命令,复杂命令,过滤命令
- **10.concepts/06-3x-data-source.md** — 3X 数据源 — 模块：core.mapping — 关键词：数据源,第三方,dao,mnt,XApi,XO

## 20.io-storage

- **20.io-storage/io-model.md** — 索引器/桶/句柄模型 — 模块：core — 关键词：索引器,桶,句柄,SHA1去重,读写路径
- **20.io-storage/mount-mapping.md** — mnt 映射语法全集 — 模块：core.mapping — 关键词：mnt,dao,mongo,redis,file,mq,aliyunoss
- **20.io-storage/dao-mapping.md** — .dao 数据源与 SQL 表映射 — 模块：core.mapping — 关键词：dao,数据源,表映射,字段映射,分表
- **20.io-storage/expiration.md** — 过期对象机制 — 模块：core.eot — 关键词：expi,EOT,清理线程,多节点竞争
- **20.io-storage/query-agg.md** — 查询与聚合 — 模块：api.io — 关键词：WnQuery,agg,分组,排序,翻页

## 30.auth

- **30.auth/accounts.md** — 系统账户与域账户 — 模块：login — 关键词：账户,用户组,角色,域账户库
- **30.auth/session.md** — 会话机制 — 模块：login.session — 关键词：会话,票据,多级登录,su,exit
- **30.auth/privilege.md** — 三层权限模型 — 模块：api.io — 关键词：mode,pvg,组角色,admin
- **30.auth/biz-privilege.md** — 业务权限模型 — 模块：ext.data.pvg — 关键词：角色×动作,pvg命令,WnPvgMoreService
- **30.auth/auto-login.md** — 自动登录与 Token — 模块：login — 关键词：ackey,签名,nonce,timestamp,防重放
- **30.auth/vcode-captcha.md** — 验证码机制 — 模块：ext.data.vcode — 关键词：验证码,图形,短信,邮箱,场景

## 40.commands

- **40.commands/README.md** — Walnut 命令机制 — 模块：impl.box — 关键词：命令机制,简单命令,复杂命令,过滤命令,管道,重定向,man
- **40.commands/01_cmd_simple.md** — 简单命令 — 模块：impl.box — 关键词：JvmExecutor,cmd_cat,ZParams,WnSystem,man
- **40.commands/02_cmd_complex.md** — 复杂命令 — 模块：impl.box — 关键词：JvmHdlExecutor,JvmHdl,cmd_xapi,子命令,服务类
- **40.commands/03_cmd_filter.md** — 过滤命令 — 模块：impl.box — 关键词：JvmFilterExecutor,JvmFilter,过滤器,cmd_tmpl,上下文
- **40.commands/04_cmd_pipe.md** — 命令管道 — 模块：impl.box — 关键词：管道,pipe,JvmAtomRunner,run,变量替换
- **40.commands/05_cmd_std_in_out.md** — 命令输入输出 — 模块：impl.box — 关键词：stdin,stdout,stderr,重定向,JvmBoxInput,JvmBoxOutput
- **40.commands/o.md** — o 命令：对象操作瑞士军刀 — 模块：ext.data.o — 关键词：@query,@update,@agg,上下文,过滤器
- **40.commands/sqlx.md** — sqlx：SQL 数据库网关 — 模块：ext.data.sqlx — 关键词：模板SQL,@vars,@dvcheck,事务,乐观锁
- **40.commands/webx.md** — webx：站点用户自助 — 模块：ext.net.webx — 关键词：@auth,@session,@passwd,微信登录,动态密码
- **40.commands/mailx.md** — mailx：邮件收发一体化 — 模块：ext.net.mailx — 关键词：SMTP,IMAP,模板邮件,附件,S/MIME
- **40.commands/tmpl.md** — tmpl：文本模板渲染 — 模块：ext.data.tmpl — 关键词：占位符,循环,分支,默认值,WnTmpl

## 50.regapi

- **50.regapi/api-object.md** — API 对象与元数据 — 模块：ext.sys.httpapi — 关键词：API对象,命令模板,http-header,跨域
- **50.regapi/request-object.md** — 请求对象 — 模块：ext.sys.httpapi — 关键词：http-qs,http-header,http-cookie,body
- **50.regapi/path-params.md** — 路径参数 — 模块：ext.sys.httpapi — 关键词：_ANY,通配,REST,api-param-name
- **50.regapi/cache.md** — 缓存策略 — 模块：ext.sys.httpapi — 关键词：cache-test,cache-duration,finger,302
- **50.regapi/auth-history.md** — 鉴权与历史埋点 — 模块：ext.sys.httpapi — 关键词：站点会话,pvg,preload,history

## 60.sched-async

- **60.sched-async/cron-syntax.md** — WnCron 表达式 — 模块：cron — 关键词：Quartz,T[],D[],区间,fill
- **60.sched-async/cron-pipeline.md** — cron→schedule→task 流转 — 模块：ext.sys — 关键词：分钟槽,任务栈,后台线程
- **60.sched-async/mq.md** — 消息队列 — 模块：ext.sys.mq — 关键词：mq,CMD消息,secret,消费者
- **60.sched-async/hooks.md** — 钩子机制 — 模块：api.hook — 关键词：六类事件,hook_by,JS钩子
- **60.sched-async/notify.md** — 通知与提醒 — 模块：ext.sys.task — 关键词：通知,短信,邮件,告警,task notify

## 70.integrations

- **70.integrations/payment.md** — 通用支付模型 — 模块：ext.net.payment — 关键词：支付单,pay,回调,wx,zfb,paypal
- **70.integrations/weixin.md** — 微信集成 — 模块：ext.net.webx — 关键词：小程序,公众号,wxmp,wxgh,wxjssdk
- **70.integrations/alipay-aliyun.md** — 支付宝与阿里云 — 模块：ext.net — 关键词：支付宝,aliyunoss,aliyunvod
- **70.integrations/sms-mail.md** — 短信与邮件 — 模块：ext.net — 关键词：sms,mailx,供应商,模板
- **70.integrations/xapi.md** — XAPI 第三方 API 框架 — 模块：ext.net.xapi — 关键词：ThirdXApi,配置驱动,动态票据
- **70.integrations/data-sync.md** — 数据同步与迁移 — 模块：ext.sys.dsync — 关键词：dsync,truck,tree索引,归档,还原

## 80.ops

- **80.ops/deploy.md** — 部署与运行配置 — 模块：web — 关键词：web.xml,conf/ioc,web.properties,集群
- **80.ops/domain-mapping.md** — 域名映射 — 模块：ext.data.domain — 关键词：/domain,hostmap,WalnutFilter
- **80.ops/sys-admin.md** — 系统管理命令 — 模块：ext.sys — 关键词：mgadmin,redis,refer,aclog,sysconf
- **80.ops/security-notes.md** — 安全实践 — 模块：core — 关键词：secret,权限,注入,HTTPS
- **80.ops/troubleshooting.md** — 排查手册 — 模块：web — 关键词：日志,错误码,诊断命令

## 90.dev

- **90.dev/source-layout.md** — src 包结构导览 — 模块：root — 关键词：包结构,关键类,导航
- **90.dev/java-lib.md** — 作为 Java 库嵌入 — 模块：api — 关键词：WnIo,IoC,WnRun,二次开发
- **90.dev/custom-command.md** — 编写自定义命令 — 模块：impl.box — 关键词：cmd_*,JvmExecutor,man手册
- **90.dev/jsexec-api.md** — JS 脚本 API 全集 — 模块：jsexec — 关键词：sys.io,sys.exec,Wn函数
- **90.dev/extend-regapi.md** — regapi 扩展实践 — 模块：ext.sys.httpapi — 关键词：模板渲染,第三方,复杂后端

## 不收录（已弃用）

www 站点模型（cmd_www 已删除）、Thing 数据集（cmd_thing 仅维护）、商城/站点 API —— 不进入本索引；历史文档已移至 `doc/_old/`（`markdown/`、`draft/`），待统一删除。
