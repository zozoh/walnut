---
title: 工作目录测试准备
module: 102.auto-test
keywords: [.at, ATEST.md, AGENTS.md, LOCAL_REF.md, 测试信息文件, 数据准备, mount, sqlx, fixture, reset, whoami]
source:
  - ext/data/app/hdl/app_init.java
  - impl/box/cmd/cmd_mount.java
  - ext/data/sqlx
related: [README, 20-at-setup, 30-at-tools, 50-at-rules]
---

# 工作目录测试准备概述

## 要解决的问题

智能体的每个工作目录都是一个小营地：它要知道这个项目的 Walnut 在哪、
测试域叫什么、数据怎么造。把这些信息散在聊天历史里，智能体每次都要重新
问一遍人。**约定把它们固化成文件，智能体到营地先看告示。**

## 解决思路：.at 目录 + 三份手册

每个被测试项目的工作目录按统一布局准备：

```text
%WORK%/
|-- .at/                  # Agent 测试工作目录（由 walnut-at-init 懒创建）
|   |-- AGENTS.md         # 入口说明（引用 use-wn-cli skill；找 walnut 走
|   |                     #   LOCAL_REF.md，找不到就停下来问主人）
|   |-- whoami.json       # 本智能体的代号与身份（walnut-at-init --name 生成）
|   |-- walnut/           # 级别 3：服务器配置与私有数据目录（--with-server 才生成）
|   |   |-- buck/         #   global-bm-bucket
|   |   |-- swap/         #   global-bm-swap
|   |   |-- log/          #   Walnut 日志文件（log4j2.xml 的 @LOG_DIR@）
|   |   |-- conf/
|   |       |-- web_local.properties   # 本工程私有的 Walnut 配置（首次由
|   |       |                          #   build.rs.json 生成；之后它就是权威配置，
|   |       |                          #   改端口/db/桶目录直接改这里，不回源）
|   |       |-- log4j2.xml             # 日志配置
|   |-- redis/
|   |   |-- data/         # 级别 3 独立 redis 的 RDB 持久化（start_with_redis
|   |                     #   从 --conf 推导到此；.gitignore 忽略内容）
|   |-- client/
|   |   |-- session/      # 会话缓存：se-00.json（默认工作会话），可多会话
|   |-- _ottl/            # 本工作目录临时的测试工具（一次性的，别沉淀）
|-- AGENTS.md             # Agent 工作手册入口
|-- LOCAL_REF.md          # 本项目引用的其他工程仓库磁盘路径
|-- ATEST.md              # Agent 测试手册：本文件的主角
|-- 测试信息文件           # 凭据等敏感信息（名字与位置由 ATEST.md 指定，见下）
```

公共工具（wn-ping / wn-up / wn-down / wn-cli / walnut-at-init）不在
`.at/` 里，统一收在 Walnut 仓库的 `bin/` 目录下（见 [自动化测试工具](30-at-tools.md)），
每个智能体直接用同一份；`.at/` 只放身份、会话、私有配置与一次性脚本。

级别 2 的对应物不在工作目录，而在 Walnut 仓库根的 `.at/`
（见 [构建 Walnut 测试服务](10-at-build.md) 的数据家一节）：
`{Walnut}/.at/walnut/{buck,swap,log}` 与 `{Walnut}/.at/redis/data/`——
那是二级启动的共享数据家，不是任何智能体的工作目录（没有
whoami/client/_ottl 的私有积累）。

`whoami.json` 由智能体开工时用 `walnut-at-init --name <代号>` 建立，
此后 wn-up 登记实例、wn-down 核对所有权、同域协作互相识别，都凭这块
身份牌（格式见 [启动服务与建立测试域](20-at-setup.md)）。脚手架是懒惰的：已存在的文件一律跳过，
重复运行不会清空已有积累（新行为详见 [自动化测试工具](30-at-tools.md) 的 walnut-at-init）。

## 级别 3：挂载自己的项目

级别 3 的智能体常常"我的前端工程就是 Walnut 的应用"。除了运行时
`wn-cli mnt`，还可以把挂载**固化进私有配置**：直接编辑
`.at/walnut/conf/web_local.properties` 的 `init-mnt` 段，照葫芦画瓢
加一行（这份 conf 生成后就是权威配置，改它不回源）：

```text
init-mnt:
/app/wn-term            : file://<dist>/ROOT/app/wn-term
/etc/hosts.d/default    : file://<dist>/ROOT/etc/hosts.d/default
/app/my-app             : file://D:/work/myproj/dist
# ~ End of init-mnt
```

- 挂载源必须落在 `mnt-file-allow` 白名单内——walnut-at-init 生成的
  白名单就是本工作目录，挂自己工程天然合法；要挂目录之外的路径，
  先扩大白名单（这属于[守则](50-at-rules.md)里的"必须请示"项）。
- 改动在**下次启动**生效（init-mnt 由启动流程读取）；重启用
  wn-down / wn-up，别直接杀进程。
- 二级启动（产物 conf）默认已挂 `wn-term` + 默认登录页，以及构建时
  存在的同级前端工程（`wn-hub` / `wn-rich` / `wn-code`，见
  [构建 Walnut 测试服务](10-at-build.md)）。

## 三份手册各管什么

- **AGENTS.md**：Agent 在这个项目里的总入口——怎么构建前端、跑单测、
  提 git。其中一节指向 ATEST.md："要动 Walnut 后台，先读它"。
- **LOCAL_REF.md**：磁盘上其他工程仓库的路径表。智能体至少要能定位
  Walnut 仓库（`build/` 构建、`bin/` 公共测试工具、`dist/wn4j/` 产物）。
  构建脚本已收编在 Walnut 仓库内，不再需要引用 walnut-doc。
- **ATEST.md**：测试手册，**本项目能不能测、怎么测的唯一权威**。必备字段：

```markdown
# ATEST
- walnut_url: http://localhost:8080        # 目标实例
- walnut_level: 1|2|3                      # 三级启动模型用哪级（见 20-at-setup）
- walnut_dist: <LOCAL_REF 里的相对路径>    # 级别 2/3 时的构建产物位置
- test_domain: demo                        # 测试域（系统用户名）
- domain_passwd: 123456                    # 测试域密码
- root_passwd: <可缺省，缺省取 root-init-passwd>
- init_template: <.init 模板位置>          # 建域后跑 app init 的布局来源
- sql_datasource: <域内 .dao 配置名>       # 数据操作走 sqlx（见下）
- 其他：模拟数据策略、协作说明（同域的其他 Agent 在测什么模块）
```

凭据与手册分离：**密码不写在 ATEST.md 正文里**，由 ATEST.md 用一行
`credentials_file:` 指向单独文件（该文件加入 .gitignore）。

## 数据准备的四条通道

按"像人类一样操作域数据"的原则，从常用到特殊：

- **命令通道（wn-cli cmd）**：`o` / `mkdir` / `ln` / `app init` / `sqlx`。
  结构化操作的标准通道，语义与控制台一致。
- **REST 通道（wn-cli obj）**：对象与文件字节。get/children/save/upload/
  content，适合大文件传输与程序化处理。
- **挂载本地目录（wn-cli mnt）**：把工程目录直接挂进域里
  （`mnt:"file://..."` 元数据）。Walnut 访问该目录即直读本地文件，
  agent 用本地编辑器改文件、Walnut 侧立即可见，最适合"我的前端工程
  就是 Walnut 的应用"这类场景。受服务端 `mnt-file-allow` 白名单约束——
  级别 1/2 的构建模板默认覆盖到 Walnut 仓库上两级目录（见 [构建 Walnut 测试服务](10-at-build.md)），
  超出范围时走级别 3 自建配置，**默认值要请示用户确认**
  （见 [Agent 测试守则](50-at-rules.md)）。挂载后必须自检（ls 内容是否符合预期）。
- **SQL（sqlx）**：域内数据表操作一律走 `sqlx` 命令（数据源是域里
  `.dao/` 配置的连接，不直接向智能体发放数据库账号）。大批量灌测试数据
  （十万行级）允许写一个固定的 Python 脚本直连数据库——这是一次性
  fixture 工具，放 `.at/_ottl/`，不进 bin/。

## reset 与卫生

- **不删数据目录**。域里残留的垃圾目录、过期对象不影响测试有效性。
- 重置数据 = 清数据表（sqlx）或 `o @delete` 删数据对象。
- `app init` 重铺布局是安全的（非空文件不覆盖），换配置后重跑即可。
- 智能体发现自己在**反复执行同一个数据操作模式**，应停下来建议主人
  把它固化成工具——这也是 _ottl/ 与 bin/ 的晋升路径（私有的一次性
  脚本 → Walnut 仓库 bin/ 的公共工具）。

## 重要细节

- `.at/whoami.json`、`.at/client/session/*.json` 含身份与凭据，
  **必须 gitignore**；`_ottl/`、`walnut/buck/`、`walnut/swap/`、
  `walnut/log/`、`walnut/conf/`、`redis/data/`（里面的 RDB 可能很大）
  同样忽略。骨架与样例手册可以提交，方便同伴复刻营地。
- Walnut 仓库根的 `.at/` 是**二级启动的数据家**（构建时自动创建
  `walnut/{buck,swap,log}`、`redis/data/`），同时也是 `.at/` 布局的
  骨架样例（`.gitkeep` 文件照常提交）；它不是智能体工作目录。

# 扩展阅读

- [自动化测试工具](30-at-tools.md) —— 五个工具的调用契约
- [Agent 测试守则](50-at-rules.md) —— 准备数据时的行为守则（自检、请示、卫生）
