---
title: 自动化测试工具
module: 102.auto-test
keywords: [wn-ping, wn-up, wn-down, wn-cli, walnut-at-init, check-node, init-node, 会话缓存, 工具脚本, JSON输出, 退出码, whoami, bin目录]
source:
  - web/module/AppModule.java
  - web/module/ObjModule.java
  - web/module/UsrModule.java
  - web/impl/WnAppService.java
related: [README, 10-at-build, 20-at-setup, 40-at-project]
---

# 自动化测试工具概述

## 要解决的问题

智能体调用工具的方式和人类不同：它要解析输出、判断退出码、把工具串进
自己的流程里。所以这组工具的设计基调是——**零依赖（纯标准库 Python 3）、
JSON 输出、退出码有语义、每个脚本一个目录**。

工具维护在 Walnut 仓库的 `bin/` 目录下（`bin/<工具名>/<工具名>.py`
+ 同目录 `README.md`），与内置命令的 `impl/box/cmd/` 平级——这里是
**所有智能体共享的公共工具集**：每个智能体直接用 Walnut 仓库里的
同一份，不必往自己的工作目录复制。某次测试私有的临时脚本放该项目
工作目录的 `.at/_ottl/`（见 [工作目录测试准备](40-at-project.md)），打磨成熟后再晋升回
`bin/`（见 [Agent 测试守则](50-at-rules.md)）。

## 通用约定

- **目录结构**：`bin/<工具名>/<工具名>.py` + 同目录 `README.md`
  （用法示例）。脚本头部注释写清：用途、用法、退出码、输出格式。
- **退出码**：`0` 成功；`1` 参数/用法错误；`2` 目标不存在或探活失败；
  `3` 认证失败（含登录 3 次未果）；`4` 服务器响应不符合契约。
- **输出**：正常情况 stdout 输出单行 JSON；进度信息走 stderr，
  方便程序捕获时过滤。
- **Windows 兼容**：不依赖 bash，路径处理用 `pathlib`。
- **身份**：需要登记"我是谁"的工具（wn-up），默认读工作目录
  `.at/whoami.json` 的 `name` 字段，`--who` 可覆盖；文件不存在则
  报错并提示先跑 `walnut-at-init --name <代号>`。

## 工具一览

### wn-ping：探活

探测一个 Walnut 实例是否可服务。`GET /a/login_mode`，判 ajax 外层
`ok:true`。

```bash
python bin/wn-ping/wn-ping.py --url http://localhost:8080
# → {"ok":true,"alive":true,"system":"PASSWD_ONLY","domain":"PASSWD_ONLY",
#    "agent_ready":true,"url":"http://localhost:8080"}
```

`agent_ready` = 两个登录模式都是 `PASSWD_ONLY`。探测超时默认 3 秒。

### wn-up：启动实例

封装"探活 → 复用或启动 → 等就绪"的完整决策：

```bash
python bin/wn-up/wn-up.py --dist D:/workspace/git/github/zozoh/walnut/dist/wn4j \
    [--conf D:/work/myproj/.at/walnut/conf] [--port 8090] [--wait 60] \
    [--who big-dad] [--pid-dir <run 目录>] [--creds <凭据 JSON>]
```

- 探活通 → 立即输出 `{"action":"reuse", ...}` 退出（0），不动现有实例。
- 探活不通 → 拉起实例（级别 3 传 `--conf`，`--pid-dir` 原样透传）。
  启动脚本自动选择：产物里带 `sbin/start_with_redis.cmd`（构建时
  `make.json` 的 `REDIS_SERVER` 非空才会生成）就调它——先按生效
  conf 的 `redis-port` 拉私有 redis 进程再委托 `start.cmd`
  （2/3 级完全数据隔离，机制见 [启动服务与建立测试域](20-at-setup.md)
  的数据库隔离一节）；没有则用 `sbin/start.cmd` 直启。它打印的
  数据源清单（redis/mongo/mysql/buck/swap）在 out.log 里可查。
- 拉起前从 `.at/whoami.json`（或 `--who`）读取代号，经环境变量
  `WN_AGENT_NAME` / `WN_WORK_PATH` 注入启动脚本，写进运行锁文件
  （见 [启动服务与建立测试域](20-at-setup.md)）。
- 就绪后若给了 `--creds`（含 `root_usr` / `root_passwd` 的 JSON），
  自动做空白节点初始化（机制见 [启动服务与建立测试域](20-at-setup.md) 的 init-node 一节），
  输出带 `node_init` 字段（`done` / `already` / `no-creds`）与
  `node_init_detail`；结果登记到 `run/node-init.json` 标记，重复
  启动不会重做。
- 输出 `{"action":"start","pid":...,"app_port":...,"admin_port":...}`。

### wn-down：停止实例

温柔停止，杀进程只作兜底（机制见 [启动服务与建立测试域](20-at-setup.md)）：

```bash
python bin/wn-down/wn-down.py --dist .../dist/wn4j [--port 8090] \
    [--timeout 5] [--pid-dir <run 目录>] [--who big-dad]
```

- 读 `run/pid-<port>.json`（`--pid-dir` 改目录），`agent`/`work_path`
  不是当前身份 → 拒绝停止（退出码 2，附明确理由）。
- 发 `stop` 到 admin-port → 等进程退出（默认 5 秒）→ 必要时 taskkill 兜底。
- 运行锁带 `redis_*` 字段（start_with_redis.cmd 拉起的私有 redis）→
  停完 Walnut 后连带关闭：exe 模式 `redis-cli shutdown save`（先落
  RDB 再退出）→ 等 20 秒 → taskkill 兜底；docker 模式
  `docker stop <容器>` → 等 20 秒 → `docker kill` 兜底；并清理
  `redis-<port>.json` 锁。
  人类手动停止等价脚本是 `sbin/stop_with_redis.cmd`（先委托
  `stop.cmd`，再关私有 redis；`stop.cmd` 本身不含 redis 逻辑）。
- 输出 `{"action":"stop","gentle":true}` 或 `{"action":"stop","gentle":false}`
  （走了强杀）；停掉私有 redis 时附带 `"redis":{"pid":...,"gentle":true|false}`。

### wn-cli：Walnut 客户端

智能体操作 Walnut 的主力工具，对接 `UsrModule`（登录）、`AppModule`
（`/a/run` 命令执行）、`ObjModule`（对象读写）。既是 CLI 也是库：
`from wn_cli import WnClient` 可嵌入更大流程。

```bash
python bin/wn-cli/wn-cli.py --url http://localhost:8080 \
    [--se se-00] [--session-dir .at/client/session] \
    login --usr demo --passwd 123456 [--root-passwd 123456]
wn-cli.py cmd 'o ~/data -l'
wn-cli.py cmd 'mkdir ~/tmp/x && echo hello > ~/tmp/x/a.txt'
wn-cli.py obj children ~/data
wn-cli.py mnt ~/site file://D:/work/myproj/www
wn-cli.py check-node
wn-cli.py init-node
```

子命令：

- **login**：按 [启动服务与建立测试域](20-at-setup.md) 的决策树执行——域登录失败转 root，
  root 查 `/home/<域>` 判断是"建域"还是"密码错"；密码错计 3 次
  终止（退出码 3）。`--root-passwd` 可省（读测试信息文件）。
  成功后票据写入会话缓存。
- **cmd**：命令通道。向 `/a/run/wn-term?cmd=...` 发送命令文本，
  stdout/stderr 原样转发。`mkdir`、`mount`、`ln`、`o`、
  `app init`、`sqlx` 全部走这里，语义与控制台完全一致。
  **注意：服务端不做会话级 `~` 展开**（`~` 会解到服务器 OS 的
  HOME，对象写进 `C:/Users/...` 这类怪路径）——wn-cli 在客户端把
  命令文本里单词首的 `~/path` 与裸 `~` 展开成 `/home/<usr>` 再发。
  另外 `&&` 连接符在这个通道里行为异常，多条命令分开传。
- **obj**：对象操作薄封装——`get / children / query / set /
  save-text / upload / content`，对接 `/o/*` 端点，只管文件字节与元数据。
  wn-cli 会自动去掉路径尾斜杠（`/o` 端点对 `/home/demo/` 这种写法
  会报 noexists）。**upload 打的是 `/o/save/stream`**（参数走 query
  string，body 就是文件字节本身，mode r/s/a 语义与端点一致）——
  不是 `/o/upload`，那个端点已 @Deprecated 且是另一套契约
  （2026-11 实测：对它发 multipart 会报莫名其妙的 noexists）。
- **mnt**：挂载本地目录。就是给目标对象写入 `mnt:"file://..."` 元数据
  （走 `o @update`），**写完必须自检**：立即 `ls` 目标路径，列出内容
  与预期不符或读不出端倪 → 报错退出（守则见 [Agent 测试守则](50-at-rules.md)）。
- **check-node / init-node**：空白节点的探测与首次初始化（机制与
  步骤见 [启动服务与建立测试域](20-at-setup.md) 的 init-node 一节）。init-node 必须 root 会话。

会话缓存格式（`.at/client/session/se-00.json`）：

```json
{
  "base": "http://localhost:8080",
  "usr": "demo",
  "ticket": "mw276pxc7muzprp1ru",
  "saved_at": "2026-11-05T01:00:00",
  "creds": {"usr": "demo", "passwd": "123456"}
}
```

`creds` 用于 401 时自动重登；401 重登一次仍失败 → 退出码 3。
`--se se-01` 开第二个会话。

### walnut-at-init：工作目录脚手架

在任意工作目录生成 `.at/` 骨架（布局与样例内容见 [工作目录测试准备](40-at-project.md)）。
**懒惰模式**：要创建的文件或目录已存在就跳过，绝不重置——之前的
测试经验、会话、私有积累都原样保留；想重置由人手动删除。

```bash
python bin/walnut-at-init/walnut-at-init.py [--name big-dad] \
    [--with-server] [--rs <build.rs.json 路径>] [--force] [目标目录]
```

- 默认只初始化**客户端**：`whoami.json`、`client/session/`、`_ottl/`、
  `AGENTS.md`（入口说明，内容是**引用**——指向 walnut 仓库的
  `doc/skill/use-wn-cli.skill.md`，并声明找 walnut 仓库走
  `LOCAL_REF.md` 约定、找不到就停下来问主人；套路升级只改 walnut
  仓库那一份）、样例手册（ATEST.md / LOCAL_REF.md 等）。
  工具集自身的索引见 Walnut 仓库 `bin/README.md`。
- `--name` 建立身份牌；已有 `whoami.json` 时可省略，沿用旧代号。
- `--with-server` 才生成 `.at/walnut/`（级别 3 私有服务器配置）与
  `.at/redis/data/`（独立 redis 数据空间），且**必须提供
  build.rs.json**（查找顺序：`--rs` > 目标目录 `build.rs.json` >
  `{Walnut}/build/build.rs.json`），找不到就拒绝（退出码 2）——
  智能体不能在没有用户确认过数据库配置的情况下私自启用服务器。
  build.rs.json 只作**首次生成 conf 的素材**，不会被复制进
  `.at/walnut/`：生成的 `.at/walnut/conf/web_local.properties`
  即权威配置，之后改端口/db/桶目录直接改它。这份 conf 是**完整
  自足**的——`app-root` / PASSWD_ONLY / `init-mnt` /
  `mnt-file-allow`（白名单 = 本工作目录）全部自带（classpath 里私有
  conf 排在 core/conf 之前，而内核默认那几项是开发者机器路径，缺了
  Jetty 会拿 conf 目录当 webapp 根直接起不来）；生成的 conf 把
  buck/swap 指向 `.at/walnut/` 下，生成的 `log4j2.xml` 把 `@LOG_DIR@`
  注入为 `.at/walnut/log/`（日志写文件，该目录随骨架创建）。
  想把自己项目的挂载固化进配置，就编辑这份 conf 的 `init-mnt` 段，
  见 [工作目录测试准备](40-at-project.md) 的"级别 3：挂载自己的项目"。
- `--force` 刷新骨架样例文件，但仍不覆盖 `whoami.json` 与已有会话。
- 重复运行全幂等：已存在的一律 `kept`，不会清空重来。

## 重要细节

- wn-cli 的 `cmd` 与 `obj` 互补：**结构化操作用 cmd（命令通道），
  文件字节传输用 obj（REST）**——大文件上传下载不要塞进命令行参数。
- **Git Bash 用户注意**：MSYS 会把形如 `/home/demo` 的参数自动转成
  Windows 路径再传给程序，导致对象路径被改坏；而 `~/xxx` 不加引号时
  会被本地 shell 抢先展开成 Windows 用户目录。从 Git Bash 调用这些
  工具时先 `export MSYS2_ARG_CONV_EXCL='*'` 关掉转换，且路径参数
  一律加引号（如 `"~/data"`），展开交给 wn-cli 做。
  wn-cli 自带防线：路径参数出现盘符开头或 `/c/...` 挂载形式、或命令
  文本里出现落在 MSYS 根目录下的盘符片段，直接以退出码 1 报错并提示
  上述环境变量与引号纪律，不会把改坏的路径发给服务端。
- 所有工具不内置任何密码；凭据一律来自参数或工作目录的测试信息文件。
- 工具的 `--help` 必须能独立看懂：智能体经常只读 help 不读文档。

# 扩展阅读

- [启动服务与建立测试域](20-at-setup.md) —— 启动/停止/登录机制的原理
- [工作目录测试准备](40-at-project.md) —— .at/ 目录布局与测试信息文件
