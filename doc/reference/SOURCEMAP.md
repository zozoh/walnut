# SOURCEMAP — 源码包 ↔ 文档映射

> 两个用途：① 读完文档想核对实现 → 找源码入口；② 改某模块代码 → 找该更新的文档。
> 路径均相对 `src/com/site0/walnut/`。「文档」列为 ✅ 表示文档尚未编写（见 INDEX.md）。

## 根与启动

- **WnVersion.java** — 说明：版本声明 — 文档：—
- **web/WnLauncher.java** — 说明：main 入口，内嵌 Jetty 启动 — 文档：[quick-start](00.start-here/quick-start.md)
- **web/WnSetup.java** — 说明：启动 Setup 链 + 后台线程 — 文档：[quick-start](00.start-here/quick-start.md)
- **web/WnConfig.java** — 说明：web.properties 封装 — 文档：[quick-start](00.start-here/quick-start.md)、80.ops/deploy.md ✅
- **web/WnInitMount.java** — 说明：init-mnt 初始化映射 — 文档：80.ops/deploy.md ✅
- **web/WnMainModule.java** — 说明：Nutz MVC 主模块 — 文档：80.ops/deploy.md ✅
- **jetty/WalnutFilter.java** — 说明：域名转发过滤器 — 文档：80.ops/domain-mapping.md ✅

## Web 层（web/）

- **web/module/AppModule.java** — 说明：/a 应用打开/登录/run — 文档：50.regapi/* ✅
- **web/module/UsrModule.java** — 说明：/u 登录/登出/会话切换 — 文档：30.auth/session.md ✅
- **web/module/ObjModule.java** — 说明：/o 缩略图/下载 — 文档：40.commands/file-obj.md ✅
- **web/module/FuseModule.java** — 说明：域名转发路由 — 文档：80.ops/domain-mapping.md ✅
- **web/module/GuestModule.java** — 说明：/g 访客接口 — 文档：✅
- **web/filter/** — 说明：WnAsUsr/WnCheckSession 等 — 文档：30.auth/* ✅
- **web/bgt/** — 说明：后台三消费线程 — 文档：60.sched-async/cron-pipeline.md ✅
- **web/clean/** — 说明：过期清理线程 — 文档：20.io-storage/expiration.md ✅
- **web/setup/** — 说明：启动 Setup 集合 — 文档：[quick-start](00.start-here/quick-start.md)

## API 层（api/）——核心接口

- **api/io/WnObj.java** — 说明：对象模型 — 文档：10.concepts/everything-is-obj.md ✅
- **api/io/WnIo.java** — 说明：IO 主接口 — 文档：20.io-storage/io-model.md ✅
- **api/io/WnQuery.java** — 说明：查询抽象 — 文档：20.io-storage/query-agg.md ✅
- **api/io/agg/** — 说明：聚合查询 — 文档：20.io-storage/query-agg.md ✅
- **api/io/WnExpiObjTable.java** — 说明：过期对象表 — 文档：20.io-storage/expiration.md ✅
- **api/lock/** — 说明：锁接口 — 文档：40.commands/sys-cmd.md ✅
- **api/hook/** — 说明：钩子接口 — 文档：60.sched-async/hooks.md ✅
- **api/box/** — 说明：沙箱/执行上下文 — 文档：10.concepts/command-pipeline.md ✅

## 核心实现（core/）

- **core/io/** — 说明：WnIoImpl2 + 缓存/钩子包装 — 文档：20.io-storage/io-model.md ✅
- **core/mapping/** — 说明：映射工厂（mnt 解析） — 文档：20.io-storage/mount-mapping.md ✅
- **core/indexer/{mongo,sql,localfile,vofs}** — 说明：四种索引器 — 文档：20.io-storage/mount-mapping.md ✅
- **core/bm/** — 说明：桶管理器（bml/localfile/redis/sql/vofs） — 文档：20.io-storage/io-model.md ✅
- **core/hdl/{memory,redis}** — 说明：句柄管理 — 文档：20.io-storage/io-model.md ✅
- **core/eot/** — 说明：过期对象表实现 — 文档：20.io-storage/expiration.md ✅
- **core/refer/{mongo,redis}** — 说明：桶引用计数 — 文档：20.io-storage/io-model.md ✅
- **core/bean/** — 说明：WnIoObj — 文档：10.concepts/everything-is-obj.md ✅

## 服务实现（impl/）

- **impl/box/** — 说明：WnSystem/WnBox/JvmExecutorFactory — 文档：10.concepts/command-pipeline.md ✅
- **impl/box/cmd/** — 说明：76 个内置命令 — 文档：40.commands/file-obj.md ✅
- **impl/box/cmd/man/zh_cn/** — 说明：内置命令中文手册 — 文档：40.commands/command-model.md ✅
- **impl/io/** — 说明：WnSecurityImpl — 文档：30.auth/privilege.md ✅
- **impl/lock/{memory,redis}** — 说明：锁实现 — 文档：40.commands/sys-cmd.md ✅
- **impl/hook/** — 说明：钩子实现 — 文档：60.sched-async/hooks.md ✅

## 账户（login/）

- **login/usr/** — 说明：WnUserStore（Std/Sql） — 文档：30.auth/accounts.md ✅
- **login/role/** — 说明：WnRoleStore — 文档：30.auth/accounts.md ✅
- **login/session/** — 说明：WnSessionStore — 文档：30.auth/session.md ✅
- **login/WnLoginApi.java** — 说明：三级存储组合入口 — 文档：30.auth/accounts.md ✅
- **login/WnLoginApiMaker.java** — 说明：按站点配置构建域内 API — 文档：30.auth/accounts.md ✅

## 其他顶层模块

- **util/WnRun.java** — 说明：命令运行器 — 文档：10.concepts/command-pipeline.md ✅
- **util/Wn.java、WnContext.java** — 说明：全局工具与线程上下文 — 文档：90.dev/java-lib.md ✅
- **util/{Wobj,WnStr,Wtime,Wpath,Ws}.java** — 说明：高频工具 — 文档：40.commands/util-cmd.md ✅
- **cron/WnCron.java** — 说明：WnCron 表达式引擎 — 文档：60.sched-async/cron-syntax.md ✅
- **jsexec/JsExec.java** — 说明：JS 引擎封装 — 文档：10.concepts/js-scripting.md ✅
- **val/** — 说明：值生成器（ID/序列/日期/IP） — 文档：40.commands/data-cmd.md ✅
- **lookup/** — 说明：快捷查询 — 文档：40.commands/data-cmd.md ✅
- **cache/** — 说明：缓存抽象 — 文档：20.io-storage/io-model.md ✅
- **cheap/** — 说明：Cheap 文档解析器（dom/css/markdown/xml） — 文档：90.dev/jsexec-api.md ✅
- **ooml/** — 说明：OOML Office 模板 — 文档：40.commands/media-cmd.md ✅
- **security/** — 说明：安全权鉴（建设中） — 文档：80.ops/security-notes.md ✅
- **alg/** — 说明：算法工具 — 文档：40.commands/util-cmd.md ✅

## 扩展层（ext/）

- **ext/sys/httpapi/** — 说明：HTTP 注册接口（HttpApiModule + cmd） — 文档：[first-api](00.start-here/first-api.md)、50.regapi/* ✅
- **ext/sys/cron,schedule,task** — 说明：定时任务三件套 — 文档：60.sched-async/cron-pipeline.md ✅
- **ext/sys/mq,websocket** — 说明：消息队列 / WebSocket — 文档：60.sched-async/mq.md ✅
- **ext/sys/hookx,lock,refer,trash** — 说明：钩子/锁/引用/回收站 — 文档：60.sched-async/hooks.md ✅
- **ext/sys/dsync,truck** — 说明：数据同步与搬运 — 文档：70.integrations/data-sync.md ✅
- **ext/sys/redis,mgadmin,aclog,i18n,datex** — 说明：系统工具命令 — 文档：40.commands/sys-cmd.md、util-cmd.md ✅
- **ext/data/sqlx** — 说明：SQL 操作 — 文档：40.commands/data-cmd.md ✅
- **ext/data/entity** — 说明：通用实体(like/favor/score/history) — 文档：40.commands/data-cmd.md ✅
- **ext/data/o** — 说明：o 增强命令 — 文档：40.commands/file-obj.md ✅
- **ext/data/pvg** — 说明：业务权限 — 文档：30.auth/biz-privilege.md ✅
- **ext/data/vcode,captcha** — 说明：验证码 — 文档：30.auth/vcode-captcha.md ✅
- **ext/data/app** — 说明：域应用初始化 — 文档：80.ops/deploy.md ✅
- **ext/data/domain** — 说明：域名映射 — 文档：80.ops/domain-mapping.md ✅
- **ext/data/xo(xo/)** — 说明：XO 数据集框架 — 文档：40.commands/data-cmd.md ✅
- **ext/media/imagic,videoc,ffmpeg** — 说明：图像/视频 — 文档：40.commands/media-cmd.md ✅
- **ext/media/ooml,sheet,edi** — 说明：文档/表格 — 文档：40.commands/media-cmd.md ✅
- **ext/media/qrcode,pdf,subtitle,avatar** — 说明：其他媒体 — 文档：40.commands/media-cmd.md ✅
- **ext/net/payment,alipay** — 说明：支付 — 文档：70.integrations/payment.md ✅
- **ext/net/xapi** — 说明：XAPI 第三方 API — 文档：70.integrations/xapi.md ✅
- **ext/net/sms,mailx** — 说明：短信/邮件 — 文档：70.integrations/sms-mail.md ✅
- **ext/net/aliyun,ftp,http** — 说明：阿里云/FTP/HTTP 客户端 — 文档：70.integrations/alipay-aliyun.md ✅
- **ext/util/{base64,hexbin,jsonx,strx,seq,shorturl,btea,crc,react}** — 说明：工具命令 — 文档：40.commands/util-cmd.md ✅
- **ext/data/thing ⚠️、ext/data/titanium ⚠️** — 说明：已弃用（仅维护） — 文档：不投入文档

## 配置

- **conf/web.properties** — 说明：主配置（启动必读） — 文档：[quick-start](00.start-here/quick-start.md)
- **conf/ioc/*.js** — 说明：IoC beans（mongo/redis/io/mq/service） — 文档：80.ops/deploy.md ✅
- **conf/hostmap** — 说明：域名转发规则 — 文档：80.ops/domain-mapping.md ✅
- **WebContent/WEB-INF/web.xml** — 说明：Servlet 配置（过滤器链） — 文档：80.ops/deploy.md ✅
