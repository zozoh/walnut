---
title: 启动服务与建立测试域
module: 102.auto-test
keywords: [启动, 停止, admin-port, wn-ping, login_mode, adduser, 测试域, PASSWORD_ONLY, 票据, pid, whoami, init-node, 数据库隔离, build.rs.json, 私有 redis, 数据源清单]
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
  "started_at": "2026-11-05T01:00:00",
  "redis_pid": 23456,     // 私有 redis 进程（start_with_redis.cmd 拉起的；无则 null）
  "redis_port": 6390,     // 私有 redis 端口
  "redis_exe": "D:/opt/redis/redis-server.exe",
  "redis_dir": "<Walnut>/.at/redis/data",  // 私有 redis RDB 持久化目录
  "redis_pass": "",        // 私有 redis 密码（无密码为空串）
  "redis_mode": "docker",  // exe = 本机进程 / docker = 容器（redis_container 为容器名）
  "redis_container": "wn4j-redis-L2"
}
```

`agent` 与 `work_path` 两个字段是给人看的：哪个智能体起了这个实例、
它的营地在哪，一目了然。人类手动启动时两字段留空。`redis_*` 字段
由 `start_with_redis.cmd` 在启动后补登（见下文"数据库隔离"）：
只有走私有 redis 启动才非空，`start.cmd` 直启的实例没有这些字段。

`start_with_redis.cmd` 启动时会把全部数据源打到控制台，启动方一眼
核对隔离是否生效：

```text
data sources:
  redis: 127.0.0.1:6390 db=9 [private]
    exe: D:/opt/redis/redis-server.exe
    dir: D:/workspace/git/github/zozoh/walnut/.at/redis/data
  mongo: 127.0.0.1:27017 db=walnut_wn4j
  mysql: 127.0.0.1:3306 db=walnut usr=root [info only]   # 仅 conf 配了 mysql-* 键时打印
  buck : D:/workspace/git/github/zozoh/walnut/.at/walnut/buck
  swap : D:/workspace/git/github/zozoh/walnut/.at/walnut/swap
```

（级别 3 时 buck/swap/redis dir 指向智能体工作目录 `.at/` 下的对应
路径；redis 数据源行按 REDIS_SERVER 烘焙来源显示 exe 或 img/ctr。）

**停止优先走温柔路线**：nutz-web 的 WebServer 启动后会在 admin-port 上
监听一个 socket（`new ServerSocket(port)` 绑所有网卡——名叫 localListen
但并不 local，公网部署是个隐患），收到一行 `stop` 就调用
`server.stop()`——Jetty 停止时 `WnSetup` 的 destroy 会被调用：
**interrupt 三个 BGT 线程并 join 等待**（WnSetup.destroy），沙箱、IO 清洁、
过期对象清洁线程也随之退出（约 1~5 秒），JVM 自然死亡，输出
`gentle:true`。`stop.cmd` / `wn-down` 的完整流程：

```text
读 run/pid-<port>.json → 确认 agent/work_path 是自己 →
连接 127.0.0.1:admin_port 发送 "stop"
  → 等待进程退出（默认 5 秒）
  → 还活着？taskkill /PID（先不带 /F，再超时带 /F）兜底（gentle:false）
```

`stop.cmd` 只负责 Walnut 进程本身。若实例是 `start_with_redis.cmd`
拉起的（运行锁带 `redis_*` 字段），停止走 `stop_with_redis.cmd`（或
`wn-down`，两者等价）：先读锁里的 redis 字段 → 委托 `stop.cmd` →
按 `redis_mode` 温柔关闭私有 redis——exe 模式 `redis-cli -p <port>
shutdown save`（本机 Windows 版裸 SHUTDOWN 不落盘，必须显式 SAVE；
redis 先落 RDB 再退出），docker 模式 `docker stop <容器>`（SIGTERM
同样先落 RDB）→ 超时强杀兜底 → 删除 `redis-<port>.json` 运行锁。

> 历史注记： destroy 曾经不中断 BGT 线程、清洁线程的睡眠又吞中断，
> 导致温柔停止必然超时、永远走 taskkill 兜底。该缺陷已修复（内核
> `WnSetup.destroy` / 三个 BGT 消费者 / 两个 Cleaner），正常停机应得
> `gentle:true`；若仍出现 `gentle:false`，按异常处理——汇报，不要无视。

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

级别 2/3 的智能体实例必须与开发者日常实例**完全数据隔离**（级别 1
复用开发者进程，天然共享）。隔离的**唯一配置来源是生效的
`web_local.properties`**（构建时由 `build.rs.json` 注入产物 conf，
级别 3 由智能体在自己的私有 conf 目录里覆盖，见
[构建 Walnut 测试服务](10-at-build.md)）。各数据源的隔离方式不同：

| 数据源 | 隔离方式 |
|--------|----------|
| redis | `start_with_redis.cmd` 拉起一个**私有 redis**，工作在生效 conf 的 `redis-port`（必须与开发者实例的 6379 错开，如 6390+），RDB 持久化到数据家（见下表）。redis 来源两种形态，由构建配置 `make.json` 的 `REDIS_SERVER` 烘焙进脚本：**exe 路径**（本机 redis-server 进程）或 **Docker 规格** `Docker:<镜像名><sha256:...>=<容器名>`（本机 Docker 按 sha256 钉死的镜像起固定容器名，镜像须已在本机、不自动 pull） |
| mongo | 共享 mongod 进程，只换 `mongo-db` 库名 |
| mysql | 共享 mysqld 进程，只换 `db` 库名（Walnut 本身不连 mysql，仅信息记录与域内 .dao 参考） |
| buck / swap | `global-bm-bucket` / `global-bm-swap` 独立目录（auto = 数据家的 `walnut/buck`、`walnut/swap`） |

**数据家**（持久化数据一律放这里，dist 是程序目录、删了不心疼）：

| 级别 | buck / swap | redis RDB |
|------|-------------|-----------|
| 2（默认 conf） | `<Walnut>/.at/walnut/{buck,swap}` | `<Walnut>/.at/redis/data/`（`@WALNUT_AT@` 烘焙） |
| 3（`--conf`） | `<work>/.at/walnut/{buck,swap}`（walnut-at-init 生成） | `<work>/.at/redis/data/`（由 conf 目录位置推导：`<conf>\..\..`） |

**职责划分**（重要）：`start.cmd` / `stop.cmd` 永远只启停 Walnut
服务本身，不含任何 redis 逻辑。redis 来源是**构建期**的
`make.json REDIS_SERVER`（非空才生成 with_redis 脚本对，两种形态：
exe 路径 / Docker 规格）；运行期需要的一切（端口/db/密码/mongo/桶
目录）都从生效 conf 读——Walnut 的 `web_local.properties` 是唯一
统一来源，不引入任何额外配置文件。

**私有 redis 的运行时契约**（`start_with_redis.cmd` / `wn-up` 启动）：

- 从生效 conf 读出 `redis-host` / `redis-port` / `redis-database` /
  `redis-password` 等全部键（私有 conf 的覆盖优先于产物 conf），
  打印全部数据源，随后检查 walnut 运行锁防双开。`redis-host` 不是
  本机（127.0.0.1/localhost）时拒绝启动——私有 redis 只对本机有意义。
- 先看 redis 运行锁 `run/redis-<port>.json`：锁在且进程活着 →
  **直接复用**（它的 RDB 里可能已有数据，这正是我们要维持的持久化）；
  锁不在而端口已被占用 → **拒绝启动**（分不清是谁的 redis，宁可不动）；
  否则拉起 redis-server（工作目录即持久化目录，RDB 保持默认开启；
  docker 模式创建等效容器），写锁，并用 redis-cli ping（exe）或
  TCP 端口（docker）探测就绪。
- docker 模式的容器复用有**两道校验**：宿主端口绑定必须等于生效 conf
  的 `redis-port`，数据卷挂载点必须等于本次的数据目录——旧布局创建
  的容器两项必居其一不匹配，拒绝并提示 `docker rm -f` 手动处理
  （它的 RDB 还挂在旧目录里，人不介入不动它）。
- 然后**原样委托 `start.cmd`** 启动 Walnut，成功后把
  `redis_pid` / `redis_port` / `redis_exe` / `redis_dir` /
  `redis_pass` 补登进 walnut 运行锁 `pid-<port>.json`——
  `wn-down` 与 `stop_with_redis.cmd` 据此温柔关闭 redis。
- `wn-up` 自动做这个选择：产物里带 `start_with_redis.cmd`（构建时
  `make.json` 的 `REDIS_SERVER` 非空才会生成）就用它，否则用
  `start.cmd`。智能体不需要关心差别，手册声明级别即可。
- 级别 3 要完全隔离时，智能体只需在**自己的私有 conf** 里指明一个
  空闲的 `redis-port`（以及 `app-port`），`start_with_redis.cmd
  --conf <私有conf>` 即自动按该端口起私有 redis、RDB 落自己工作
  目录的 `.at/redis/data/`。

mongo/redis 换了库，两边也不能共用同一个**桶目录**（桶文件是本地
路径，共用直接报 `e.io.bm.LocalSha1BM.LostBucket`），所以 buck/swap
的独立目录是隔离的必要组成。智能体构建或自建配置前，必须向用户
确认这些连接信息。

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
