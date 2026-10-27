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
- **10.concepts/01-std-obj.md** — 一切皆对象 — 模块：api.io — 关键词：WnObj,对象,id,nm,race,ln,expi,元数据
- **10.concepts/02-domain-home.md** — Domain 与 Home — 模块：api.io — 关键词：域,主目录,HOME,/sys,/var,约定目录
- **10.concepts/03-session-env.md** — 会话与环境变量 — 模块：login — 关键词：会话,session,env,PATH,.env
- **10.concepts/04-command-model.md** — 命令模型 — 模块：impl.box — 关键词：命令,沙箱,管道,简单命令,复杂命令,过滤命令
- **10.concepts/05-3x-data-source.md** — 3X 数据源 — 模块：core.mapping — 关键词：数据源,第三方,dao,mnt,XApi,XO

## 20.io-storage

- **20.io-storage/README.md** — 数据存储与映射（核心模型 + 目录导航） — 模块：core — 关键词：io,存储,映射,阅读路径
- **20.io-storage/10-io-mapping.md** — IO 映射 — 模块：core.mapping — 关键词：mnt,映射,索引管理器,桶管理器,两段式ID
- **20.io-storage/11-io-hooks.md** — IO 钩子 — 模块：api.hook — 关键词：hook,hook_by,六类事件,JS钩子,缩略图
- **20.io-storage/xo-storage.md** — 对象存储集成（xo） — 模块：ext.xo — 关键词：xo,对象存储,s3,cos,oss,临时密钥,vofs,vxds
- **20.io-storage/20-indexer-mongo.md** — 索引管理器 - MongoDB — 模块：core.indexer.mongo — 关键词：mongo,默认索引器,集合
- **20.io-storage/21-indexer-redis.md** — 索引管理器 - Redis（规划中） — 模块：core.indexer.redis — 关键词：redis,规划中,键值
- **20.io-storage/22-indexer-sql.md** — 索引管理器 - SQL — 模块：core.indexer.sql — 关键词：sql,SqlIndexer,sqlx,dao,AMS,cmg,filter
- **20.io-storage/30-bm-sha1.md** — 桶管理器 - sha1 — 模块：core.bm.bml — 关键词：sha1,内容寻址,去重,引用计数,全局桶
- **20.io-storage/31-bm-vxds.md** — 桶管理器 - vxds — 模块：core.bm.bmv — 关键词：vxds,对象存储,内容签名,引用计数
- **20.io-storage/32-bm-sql.md** — 桶管理器 - sql — 模块：core.bm.sql — 关键词：sql,SqlBM,blob,内容进表
- **20.io-storage/33-bm-redis.md** — 桶管理器 - redis — 模块：core.bm.redis — 关键词：redis,小文件,会话,WedisConfig
- **20.io-storage/34-bm-mem.md** — 桶管理器 - mem（规划中） — 模块：core.bm.mem — 关键词：mem,内存,规划中
- **20.io-storage/35-bm-multi.md** — 桶管理器 - multi（规划中） — 模块：core.bm.multi — 关键词：multi,条件映射,规划中
- **20.io-storage/40-indexer-file.md** — 文件系统映射 — 模块：core.indexer.localfile — 关键词：file,filew,本地目录,只读,挂载
- **20.io-storage/41-indexer-vofs.md** — 对象存储映射 — 模块：core.indexer.vofs — 关键词：vofs,虚拟对象文件系统,s3,cos,两段式ID

## 30.auth

- **30.auth/README.md** — 账号认证机制概述 — 模块：login — 关键词：WnIo模型,for_sys,for_domain,for_hydrate,权限,验证码
- **30.auth/10-auth-user-role.md** — 系统用户、角色与密码存储 — 模块：login — 关键词：/sys/usr,/sys/grp,密码salt,组角色,Std/Sql
- **30.auth/20-auth-session.md** — 系统用户会话管理 — 模块：login.session — 关键词：会话票据,轮换,su会话树,过期
- **30.auth/30-auth-in-domain.md** — 域内用户存储与认证 — 模块：login.domain — 关键词：WnLoginSite,accounts,roles,forDomain
- **30.auth/40-auth-hydrate.md** — 混合认证机制 — 模块：login.hydrate — 关键词：域内登录,标准会话,sysSessionStore,site属性
- **30.auth/0-auth-pvg.md** — 标准对象的权限体系 — 模块：api.io / ext.data.pvg — 关键词：mode,pvg,业务权限矩阵,组角色,admin
- **30.auth/60-auth-otp.md** — 动态密码 OTP：WnOTPService 门面、生成/存储/发送 — 模块：security.otp — 关键词：动态密码,OTP,邮箱验证码,一发一验,WnOTPService
- **30.auth/70-auth-captcha.md** — 人机验证码 captcha：接口骨架、拖拽验证、与 OTP 协作 — 模块：security.captcha — 关键词：验证码,captcha,人机校验,拖拽验证,WnCaptchaApi

## 40.commands

- **40.commands/README.md** — Walnut 命令机制 — 模块：impl.box — 关键词：命令机制,简单命令,复杂命令,过滤命令,管道,重定向,man
- **40.commands/01-cmd-simple.md** — 简单命令 — 模块：impl.box — 关键词：JvmExecutor,cmd_cat,ZParams,WnSystem,man
- **40.commands/02-cmd-complex.md** — 复杂命令 — 模块：impl.box — 关键词：JvmHdlExecutor,JvmHdl,cmd_xapi,子命令,服务类
- **40.commands/03-cmd-filter.md** — 过滤命令 — 模块：impl.box — 关键词：JvmFilterExecutor,JvmFilter,过滤器,cmd_tmpl,上下文
- **40.commands/04-cmd-pipe.md** — 命令管道 — 模块：impl.box — 关键词：管道,pipe,JvmAtomRunner,run,变量替换
- **40.commands/05-cmd-std-in-out.md** — 命令输入输出 — 模块：impl.box — 关键词：stdin,stdout,stderr,重定向,JvmBoxInput,JvmBoxOutput
- **40.commands/60-cmd-table.md** — 命令总表（157 个命令的来源/写法/分类） — 模块：impl.box / ext — 关键词：命令清单,内置,扩展,简单命令,复杂命令,过滤命令,分类

## 50.regapi

- **50.regapi/README.md** — HTTP 注册接口（文件即接口的机制概述） — 模块：ext.sys.httpapi — 关键词：regapi,文件即接口,请求对象,命令模板,动态API
- **50.regapi/10-api-object.md** — API 对象与元数据 — 模块：ext.sys.httpapi — 关键词：API对象,命令模板,http-header,跨域
- **50.regapi/20-request-object.md** — 请求对象 — 模块：ext.sys.httpapi — 关键词：http-qs,http-header,http-cookie,body
- **50.regapi/30-path-params.md** — 路径参数 — 模块：ext.sys.httpapi — 关键词：_ANY,通配,REST,api-param-name
- **50.regapi/40-cache.md** — 缓存策略 — 模块：ext.sys.httpapi — 关键词：cache-test,cache-duration,finger,302
- **50.regapi/50-auth-history.md** — 鉴权与历史埋点 — 模块：ext.sys.httpapi — 关键词：站点会话,pvg,preload,history

## 60.sched-async

- **60.sched-async/README.md** — 定时任务 — 模块：60.sched-async — 关键词：三张表,后台线程,sys-schedule-enabled
- **60.sched-async/10-cron-task.md** — cron 命令与任务定义 — 模块：ext.sys.cron — 关键词：cron_task,cron add,cron preview,user
- **60.sched-async/20-cron-syntax.md** — WnCron 表达式 — 模块：cron — 关键词：Quartz,T[],D[],区间,fill
- **60.sched-async/30-schedule-slot.md** — 分钟计划表 — 模块：ext.sys.schedule — 关键词：分钟槽,schedule load,schedule push,预加载
- **60.sched-async/40-task-queue.md** — 后台任务栈 — 模块：ext.sys.task — 关键词：task add,task notify,弹出即删除,BGT_TASK

## 70.functionate

- **70.functionate/README.md** — 功能扩展 — 模块：70.functionate — 关键词：开箱即用,功能命令
- **70.functionate/10-email-mailx.md** — Email 收发 — 模块：ext.net.mailx — 关键词：mailx,smtp,imap,模板邮件,附件
- **70.functionate/20-sms.md** — SMS 短信（占位） — 模块：ext.net.sms — 关键词：sms,占位,provider
- **70.functionate/30-office-ooml.md** — Office 文档解析与模板渲染 — 模块：ext.media.ooml — 关键词：ooml,docx,xlsx,模板渲染
- **70.functionate/40-sheet-csv.md** — 表格与 CSV 输出 — 模块：ext.media.sheet — 关键词：sheet,csv,xls,json,导出
- **70.functionate/50-image-imagic.md** — 图片处理 — 模块：ext.media.imagic — 关键词：imagic,缩略图,缩放,裁剪,旋转
- **70.functionate/60-video-ffmpeg.md** — 视频信息与封图 — 模块：ext.media.ffmpeg — 关键词：videoi,videoc,ffmpeg,封图,预览
- **70.functionate/70-qrcode.md** — 二维码 — 模块：ext.media.qrcode — 关键词：qrcode,二维码,生成,解码

## 100.ops

- **100.ops/deploy.md** — 部署与运行配置 — 模块：web — 关键词：web.xml,conf/ioc,web.properties,集群
- **100.ops/domain-mapping.md** — 域名映射 — 模块：ext.data.domain — 关键词：/domain,hostmap,WalnutFilter
- **100.ops/sys-admin.md** — 系统管理命令 — 模块：ext.sys — 关键词：mgadmin,redis,refer,aclog,sysconf
- **100.ops/security-notes.md** — 安全实践 — 模块：core — 关键词：secret,权限,注入,HTTPS
- **100.ops/troubleshooting.md** — 排查手册 — 模块：web — 关键词：日志,错误码,诊断命令

## 101.dev

- **101.dev/source-layout.md** — src 包结构导览 — 模块：root — 关键词：包结构,关键类,导航
- **101.dev/java-lib.md** — 作为 Java 库嵌入 — 模块：api — 关键词：WnIo,IoC,WnRun,二次开发
- **101.dev/custom-command.md** — 编写自定义命令 — 模块：impl.box — 关键词：cmd_*,JvmExecutor,man手册
- **101.dev/jsexec-api.md** — JS 脚本 API 全集 — 模块：jsexec — 关键词：sys.io,sys.exec,Wn函数
- **101.dev/extend-regapi.md** — regapi 扩展实践 — 模块：ext.sys.httpapi — 关键词：模板渲染,第三方,复杂后端

## 不收录（已弃用）

www 站点模型（cmd_www 已删除）、Thing 数据集（cmd_thing 仅维护）、商城/站点 API —— 不进入本索引；历史文档已移至 `doc/_old/`（`markdown/`、`draft/`），待统一删除。
