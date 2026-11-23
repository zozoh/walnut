# AGENTS.md — Walnut

业务系统开发基座（Java + 内嵌 Nutz）：**一切数据源是一棵对象树，用命令操作它，用 JS 写业务。**

> 维护：本文件由智能体自己维护；改之前先读 [如何编写AGENTS.md](../dev-tools/doc/write-AGENTS.md)。

## 阅读顺序（最少 token）

文档在私有库 `../wn-doc/`（`{WN_DOC_HOME}`）：

1. `{WN_DOC_HOME}/public/reference/INDEX.md` — 全量索引，按关键词直达；
2. `.../10.concepts/README.md` — 五大核心概念（对象 / IO-Indexer-BM / Domain / 会话 / 命令）；
3. `.../SOURCEMAP.md` — 源码包 ↔ 文档映射；
4. 写代码前看 `.../101.dev/` + 对应 `man/zh_cn/cmd_*.md`。

文档与源码冲突时**以源码为准**；命令契约 = 源码头注释 + 同目录 man 文档，改命令必改 man。

## 跑测试

- 优先用工作区 MCP 的 `mcp__wn__junit`（`project=walnut`，可加 `class_name` / `method`）；
- 没有 MCP 才 `mvn -o test -Dtest=<类名>`；全量已知有红（Mongo/本地路径），先跑基线，别把既存红当自己改坏；
- 判绿看**用例计数**，`junit` 的 `exit` 恒为 0；
- `test/` 下 `Abstract*Test` 必须声明 `abstract`，否则被实例化出一堆 NPE 假红；
- 集成/验收用例在 `atest/`（`python atest/run.py --only CASE-xxx`）。

## 源码布局

`src/com/site0/walnut/`：`api`（接口，先读 Javadoc）、`core`（io/索引器/桶管理）、`impl`（服务 + `box/cmd` 内置命令）、`ext`（扩展命令）、`web2`（Jetty12 栈）、`cron`、`ooml`、`login`。
构建：Maven（`pom.xml`）；装配 `web2/service`；配置 `conf/web.properties`。

## 弃用（不要读、不要改）

`thing` 全系列、`600.site-app/`、`cmd_obj`（用 `cmd_o`）、`cmd_crontab`（用 `cron`）、`client/ gradle/ build.py jdbc/`。
例外：`bin/` 是智能体测试公共工具集（wn-ping / wn-cli / at-*）。

## 纪律

- 临时文件只放 `.tmp/`（已 gitignore），任务结束前清理；不能删的要在汇报里说明原因；
- 上手先跑 `.../00.start-here/quick-start.md`，玩 `o` 命令；重点范本：`cmd_o`、`cmd_sqlx`、`cmd_tmpl`、`cmd_xapi`。
