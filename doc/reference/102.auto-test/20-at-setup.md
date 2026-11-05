---
title: 启动服务与建立测试域
module: 102.auto-test
keywords: [启动, 停止, admin-port, wn-ping, login_mode, adduser, 测试域, PASSWORD_ONLY, 票据, pid, whoami, init-node, 数据库隔离, build.rs.json]
source:
  - web/WnLauncher.java
  - web/setup/WnSetup.java
  - web/module/AppModule.java
  - web/module/UsrModule.java
  - web/impl/WnAppService.java
  - login/WnSimpleLoginApi.java
  - impl/box/cmd/cmd_adduser.java
  - org/nutz/web/WebServer.java
  - org/nutz/lang/socket/Sockets.java
related: [README, 10-at-build, 30-at-tools, 50-at-rules]
---

# 启动服务与建立测试域概述

## 要解决的问题

智能体拿到构建产物后，要回答三个问题：这个 Walnut 是不是已经在跑了？
没有的话怎么起？起来之后我怎么拥有一个可以随便折腾的测试域？

## 解决思路：先探活，再决定动手

一切从探针开始。**`wn-ping` 探测 `GET /a/login_mode`**：这个端点总是可用、
无需登录，Nutz 的 ajax 视图会把结果包一层 `{ok:true, data:{...}}`——
所以探活只需要看外层 `ok:true`，天然不用担心把"某个需要登录的接口
返回 401"误判成"服务活着"。

探针返回的 `data` 里有 `system` / `domain` 两个登录模式，顺手就能检查
智能体测试的硬性前提：**两个模式都必须是 `PASSWD_ONLY`**。
不满足就明确报错，别等到登录失败才发现。

判定规则：

```text
wn-ping 通  → 是 Walnut，按手册级别直接用（级别 1：共享实例）
wn-ping 不通 → 端口被占？那是别的程序，报错走人
              端口空闲？wn-up 启动，等探针就绪
```

## 我是谁：whoami 文件

每个智能体开工前先有自己的代号。工作目录的 `.at/whoami.json` 就是
它的身份牌（由 walnut-at-init 生成，格式如下），之后的实例所有权、
协作识别都靠它：

```json
{
  "name": "big-dad",
  "created_at": "2026-11-05T12:00:00",
  "note": "可选，一句话说明自己负责测什么"
}
```

## 启动与停止的机制

**启动**是 `java $JAVA_OPTS com.site0.walnut.web.WnLauncher`：主类从
classpath 读 `web.properties`，按 `app-port` 起内嵌 Jetty。
`start.cmd` 负责拼 classpath（生效 conf 目录 → `core/conf/` →
`core/walnut.jar` → `deps/*`，见 [构建 Walnut 测试服务](10-at-build.md)）、探测 java、后台拉起
JVM，并把启动信息登记到**运行锁文件** `run/pid-<port>.json`（`--pid-dir`
可改目录，默认 `<dist>/run`）：

```json
{
  "pid": 12345,
  "app_port": 8090,
  "admin_port": 8091,     // = app-port + 1（web.properties 未显式声明时）
  "agent": "big-dad",     // 来自 WN_AGENT_NAME 环境变量（wn-up 注入 whoami 代号）
  "work_path": "D:/work/myproj",  // 来自 WN_WORK_PATH（wn-up 注入）
  "conf_dir": "<dist>/conf",      // 实际生效的 conf 目录
  "started_at": "2026-11-05T01:00:00"
}
```

`agent` 与 `work_path` 两个字段是给人看的：哪个智能体起了这个实例、
它的营地在哪，一目了然。人类手动启动时两字段留空。

**停止优先走温柔路线**：nutz-web 的 WebServer 启动后会在 admin-port 上
监听一个本地 socket（仅 127.0.0.1），收到一行 `stop` 就调用
`server.stop()`——Jetty 停止时 `WnSetup` 的 destroy 会被调用，后台线程
（BGT_TASK / BGT_SCHEDULE / BGT_CRON）得以体面收场。`stop.cmd` /
`wn-down` 的完整流程：

```text
读 run/pid-<port>.json → 确认 agent/work_path 是自己 →
连接 127.0.0.1:admin_port 发送 "stop"
  → 等待进程退出（默认 10 秒）
  → 还活着？taskkill /PID（先不带 /F，再超时带 /F）兜底
```

找不到运行锁文件就不动手——**分不清是谁的实例，宁可不停**。
级别 3 的多实例共存靠各自独立的 app-port / admin-port 错开。

## 建立测试域：登录失败的两种含义

Walnut 的用户模型里，**一个用户就是一个隔离域**（注册即建组、建主目录），
非 root 用户只能在自己域里折腾——测试域不用专门"创建"这个概念，
`adduser` 一个用户就是开通一个域。

凭据来自工作目录的测试信息文件（位置与格式见 [工作目录测试准备](40-at-project.md)）。
登录流程的精髓在于：**登录失败不是一种错误，而是两种**。
`WnSimpleLoginApi.loginByPassword` 对"用户不存在"和"密码错误"抛出
**同一个** `e.auth.login.Failed`（`UsrNoExists` 只在注册类接口出现），
所以不能用错误码区分，改由 root 代为查证（wn-cli 已内置该决策）：

```text
域登录抛 e.auth.login.Failed
  → 转 root 登录（root 失败 =  root 密码不对，计数 3 次终止并警告用户）
  → root 会话查 /home/<域> 是否存在
      ├─ 不存在 → adduser <域> -p <密码> 建域 → 重试域登录
      │            （adduser 不带 -exists 时用户已存在会报错，
      │              正好充当"别人已经建过了"的信号）
      └─ 存在   → 域密码真的不对：计数，累计 3 次终止整个测试流程
                   并警告用户（可能密码被改了，必须人来管）
```

root 密码的缺省值是构建配置里的 `root-init-passwd`；测试信息文件里
写了 root 密码就以文件为准（root 可能被其他开发者重置，不能假设
构建时的初始密码还有效）。

## 票据与会话

- 客户端把会话票据放在请求头 **`x-walnut-ticket`** 里传递，
  不依赖 cookie，也就没有跨域 SameSite 的麻烦。
- 票据同时支持查询参数 `_wn_ticket_`（GET 资源场景）和 cookie `SEID`
  （浏览器场景），但智能体工具统一用请求头。
- 会话是对象（`/var/session/${TICKET}`），**每次被读取都会自我顺延过期
  时间**——只要持续在用就不会过期；空置超时后，客户端凭缓存的凭据
  重新登录即可（wn-cli 遇到 401 自动重登一次，再失败才报错）。
- 每个智能体一个会话就够；确实需要第二个，就在 wn-cli 里换个会话名
  （对应缓存目录里的另一个 `se-XX.json`）。

## 重要细节

- **登录模式是测试前提，不是可选项**：服务器若配置了 captcha/OTP，
  API 登录会被拦。探针发现模式不是 PASSWD_ONLY 时，报错信息里要
  明确指出"该实例未开启智能体测试模式"。
- **建域后是裸域**：业务布局（regapi、数据源、环境变量）由项目的
  `.init` 模板用 `app init` 铺设（见 [101.dev](../101.dev/README.md) 的域应用初始化）。
  手册应指明模板位置，建域流程把它跑一遍。
- **ADDUSER 的 -setup**：开通时可指定初始化脚本路径，一步完成
  "建用户 + 铺布局"。
- 多个智能体**可以故意共用同一个测试域**（分模块测试、测协同），
  域名冲突不是错误——手册说了算。

## 数据库隔离：2 级与 3 级的生命线

无论哪一级，智能体用的 mongo / redis 都应该与开发者日常实例**不同库**，
否则两边互相踩数据（桶文件是本地路径，共用库直接报
`e.io.bm.LocalSha1BM.LostBucket`）。隔离的入口就是构建时的
`build.rs.json`（见 [构建 Walnut 测试服务](10-at-build.md)）：级别 2 靠它选不同的 mongo-db /
redis-database；级别 3 更进一步——每个智能体一套独立的库，从根上
避免冲突。智能体构建或自建配置前，必须向用户确认这些连接信息。

## 空白节点的首次初始化（init-node）

从**空白数据库**启动的 Walnut，顶级目录权限、系统钩子、MongoDB
索引都是缺的，域用户会撞到各种迷惑性报错。初始化规则参考
`walnut-doc` 的 `admin/a-walnut-init.md`，固化为 `wn-cli` 的两个
子命令：

- `wn-cli check-node`：看 `/app` 的权限位就知道节点状态——空白库
  启动时它是 `0750`，初始化完成后是 `0755`。只读探测，随时可跑。
- `wn-cli init-node`：幂等的初始化，必须以 root 会话执行。做三件事：
  1. 顶级目录放开读+执行（`/etc` `/sys` `/var` `/home` `/app` 等，
     不存在的路径跳过）；
  2. 建系统钩子 `/sys/hook/write/create_thumbnail` 与
     `create_video_preview`；
  3. 用 `mgadmin` 建系统对象的 MongoDB 索引（11+1 条，幂等）。

`wn-up --creds <凭据文件>` 把这套判断自动化了：启动就绪后检查
`run/node-init.json` 标记 → 无标记则 root 登录调 check-node →
判定为空白库就执行 init-node → 写标记。输出里的 `node_init` 字段
（`done` / `already` / `no-creds`）供程序判断；`no-creds` 表示没给
凭据跳过了初始化，不是错误。复用已初始化实例时 wn-up 直接走
reuse 分支，不会重复初始化。

## 主要代码类

- `web/module/AppModule.java` —— `/a/login_mode` 探针端点
- `web/module/UsrModule.java` —— `/u/do/login/ajax` 系统登录
- `login/WnSimpleLoginApi.java` —— 登录与错误码（`loginByPassword`
  对"用户不存在/密码错"统一抛 `Failed`，需 root 查 `/home/<域>` 区分）
- `org/nutz/web/WebServer.java` —— admin-port 的 `stop` 监听
- `web/impl/WnAppService.java` —— `/a/run` 命令执行的 app 解析

# 扩展阅读

- [构建 Walnut 测试服务](10-at-build.md) —— start.cmd 的 conf 参数与 classpath 优先级
- [自动化测试工具](30-at-tools.md) —— wn-up / wn-down / wn-cli 的行为契约
- [Agent 测试守则](50-at-rules.md) —— 登录失败与停止实例的守则
