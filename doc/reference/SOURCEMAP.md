# SOURCEMAP — 源码包 ↔ 文档映射

> 两个用途：① 读完文档想核对实现 → 找源码入口；② 改某模块代码 → 找该更新的文档。
> 路径均相对 `src/com/site0/walnut/`。「文档」列为 ✅ 表示文档尚未编写（见 INDEX.md）。

## 根与启动

| 源码 | 说明 | 文档 |
|------|------|------|
| WnVersion.java | 版本声明 | — |
| web/WnLauncher.java | main 入口，内嵌 Jetty 启动 | [quick-start](00.start-here/quick-start.md) |
| web/WnSetup.java | 启动 Setup 链 + 后台线程 | [quick-start](00.start-here/quick-start.md) |
| web/WnConfig.java | web.properties 封装 | [quick-start](00.start-here/quick-start.md)、80.ops/deploy.md ✅ |
| web/WnInitMount.java | init-mnt 初始化映射 | 80.ops/deploy.md ✅ |
| web/WnMainModule.java | Nutz MVC 主模块 | 80.ops/deploy.md ✅ |
| jetty/WalnutFilter.java | 域名转发过滤器 | 80.ops/domain-mapping.md ✅ |

## Web 层（web/）

| 源码 | 说明 | 文档 |
|------|------|------|
| web/module/AppModule.java | /a 应用打开/登录/run | 50.regapi/* ✅ |
| web/module/UsrModule.java | /u 登录/登出/会话切换 | 30.auth/session.md ✅ |
| web/module/ObjModule.java | /o 缩略图/下载 | 40.commands/file-obj.md ✅ |
| web/module/FuseModule.java | 域名转发路由 | 80.ops/domain-mapping.md ✅ |
| web/module/GuestModule.java | /g 访客接口 | ✅ |
| web/filter/ | WnAsUsr/WnCheckSession 等 | 30.auth/* ✅ |
| web/bgt/ | 后台三消费线程 | 60.sched-async/cron-pipeline.md ✅ |
| web/clean/ | 过期清理线程 | 20.io-storage/expiration.md ✅ |
| web/setup/ | 启动 Setup 集合 | [quick-start](00.start-here/quick-start.md) |

## API 层（api/）——核心接口

| 源码 | 说明 | 文档 |
|------|------|------|
| api/io/WnObj.java | 对象模型 | 10.concepts/everything-is-obj.md ✅ |
| api/io/WnIo.java | IO 主接口 | 20.io-storage/io-model.md ✅ |
| api/io/WnQuery.java | 查询抽象 | 20.io-storage/query-agg.md ✅ |
| api/io/agg/ | 聚合查询 | 20.io-storage/query-agg.md ✅ |
| api/io/WnExpiObjTable.java | 过期对象表 | 20.io-storage/expiration.md ✅ |
| api/lock/ | 锁接口 | 40.commands/sys-cmd.md ✅ |
| api/hook/ | 钩子接口 | 60.sched-async/hooks.md ✅ |
| api/box/ | 沙箱/执行上下文 | 10.concepts/command-pipeline.md ✅ |

## 核心实现（core/）

| 源码 | 说明 | 文档 |
|------|------|------|
| core/io/ | WnIoImpl2 + 缓存/钩子包装 | 20.io-storage/io-model.md ✅ |
| core/mapping/ | 映射工厂（mnt 解析） | 20.io-storage/mount-mapping.md ✅ |
| core/indexer/{mongo,sql,localfile,vofs} | 四种索引器 | 20.io-storage/mount-mapping.md ✅ |
| core/bm/ | 桶管理器（bml/localfile/redis/sql/vofs） | 20.io-storage/io-model.md ✅ |
| core/hdl/{memory,redis} | 句柄管理 | 20.io-storage/io-model.md ✅ |
| core/eot/ | 过期对象表实现 | 20.io-storage/expiration.md ✅ |
| core/refer/{mongo,redis} | 桶引用计数 | 20.io-storage/io-model.md ✅ |
| core/bean/ | WnIoObj | 10.concepts/everything-is-obj.md ✅ |

## 服务实现（impl/）

| 源码 | 说明 | 文档 |
|------|------|------|
| impl/box/ | WnSystem/WnBox/JvmExecutorFactory | 10.concepts/command-pipeline.md ✅ |
| impl/box/cmd/ | 76 个内置命令 | 40.commands/file-obj.md ✅ |
| impl/box/cmd/man/zh_cn/ | 内置命令中文手册 | 40.commands/command-model.md ✅ |
| impl/io/ | WnSecurityImpl | 30.auth/privilege.md ✅ |
| impl/lock/{memory,redis} | 锁实现 | 40.commands/sys-cmd.md ✅ |
| impl/hook/ | 钩子实现 | 60.sched-async/hooks.md ✅ |

## 账户（login/）

| 源码 | 说明 | 文档 |
|------|------|------|
| login/usr/ | WnUserStore（Std/Sql） | 30.auth/accounts.md ✅ |
| login/role/ | WnRoleStore | 30.auth/accounts.md ✅ |
| login/session/ | WnSessionStore | 30.auth/session.md ✅ |
| login/WnLoginApi.java | 三级存储组合入口 | 30.auth/accounts.md ✅ |
| login/WnLoginApiMaker.java | 按站点配置构建域内 API | 30.auth/accounts.md ✅ |

## 其他顶层模块

| 源码 | 说明 | 文档 |
|------|------|------|
| util/WnRun.java | 命令运行器 | 10.concepts/command-pipeline.md ✅ |
| util/Wn.java、WnContext.java | 全局工具与线程上下文 | 90.dev/java-lib.md ✅ |
| util/{Wobj,WnStr,Wtime,Wpath,Ws}.java | 高频工具 | 40.commands/util-cmd.md ✅ |
| cron/WnCron.java | WnCron 表达式引擎 | 60.sched-async/cron-syntax.md ✅ |
| jsexec/JsExec.java | JS 引擎封装 | 10.concepts/js-scripting.md ✅ |
| val/ | 值生成器（ID/序列/日期/IP） | 40.commands/data-cmd.md ✅ |
| lookup/ | 快捷查询 | 40.commands/data-cmd.md ✅ |
| cache/ | 缓存抽象 | 20.io-storage/io-model.md ✅ |
| cheap/ | Cheap 文档解析器（dom/css/markdown/xml） | 90.dev/jsexec-api.md ✅ |
| ooml/ | OOML Office 模板 | 40.commands/media-cmd.md ✅ |
| security/ | 安全权鉴（建设中） | 80.ops/security-notes.md ✅ |
| alg/ | 算法工具 | 40.commands/util-cmd.md ✅ |

## 扩展层（ext/）

| 源码 | 说明 | 文档 |
|------|------|------|
| ext/sys/httpapi/ | HTTP 注册接口（HttpApiModule + cmd） | [first-api](00.start-here/first-api.md)、50.regapi/* ✅ |
| ext/sys/cron,schedule,task | 定时任务三件套 | 60.sched-async/cron-pipeline.md ✅ |
| ext/sys/mq,websocket | 消息队列 / WebSocket | 60.sched-async/mq.md ✅ |
| ext/sys/hookx,lock,refer,trash | 钩子/锁/引用/回收站 | 60.sched-async/hooks.md ✅ |
| ext/sys/dsync,truck | 数据同步与搬运 | 70.integrations/data-sync.md ✅ |
| ext/sys/redis,mgadmin,aclog,i18n,datex | 系统工具命令 | 40.commands/sys-cmd.md、util-cmd.md ✅ |
| ext/data/sqlx | SQL 操作 | 40.commands/data-cmd.md ✅ |
| ext/data/entity | 通用实体(like/favor/score/history) | 40.commands/data-cmd.md ✅ |
| ext/data/o | o 增强命令 | 40.commands/file-obj.md ✅ |
| ext/data/pvg | 业务权限 | 30.auth/biz-privilege.md ✅ |
| ext/data/vcode,captcha | 验证码 | 30.auth/vcode-captcha.md ✅ |
| ext/data/app | 域应用初始化 | 80.ops/deploy.md ✅ |
| ext/data/domain | 域名映射 | 80.ops/domain-mapping.md ✅ |
| ext/data/xo(xo/) | XO 数据集框架 | 40.commands/data-cmd.md ✅ |
| ext/media/imagic,videoc,ffmpeg | 图像/视频 | 40.commands/media-cmd.md ✅ |
| ext/media/ooml,sheet,edi | 文档/表格 | 40.commands/media-cmd.md ✅ |
| ext/media/qrcode,pdf,subtitle,avatar | 其他媒体 | 40.commands/media-cmd.md ✅ |
| ext/net/payment,alipay | 支付 | 70.integrations/payment.md ✅ |
| ext/net/xapi | XAPI 第三方 API | 70.integrations/xapi.md ✅ |
| ext/net/sms,mailx | 短信/邮件 | 70.integrations/sms-mail.md ✅ |
| ext/net/aliyun,ftp,http | 阿里云/FTP/HTTP 客户端 | 70.integrations/alipay-aliyun.md ✅ |
| ext/util/{base64,hexbin,jsonx,strx,seq,shorturl,btea,crc,react} | 工具命令 | 40.commands/util-cmd.md ✅ |
| ext/data/thing ⚠️、ext/data/titanium ⚠️ | 已弃用（仅维护） | 不投入文档 |

## 配置

| 路径 | 说明 | 文档 |
|------|------|------|
| conf/web.properties | 主配置（启动必读） | [quick-start](00.start-here/quick-start.md) |
| conf/ioc/*.js | IoC beans（mongo/redis/io/mq/service） | 80.ops/deploy.md ✅ |
| conf/hostmap | 域名转发规则 | 80.ops/domain-mapping.md ✅ |
| WebContent/WEB-INF/web.xml | Servlet 配置（过滤器链） | 80.ops/deploy.md ✅ |
