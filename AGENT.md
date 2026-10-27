# AGENT.md — 快速了解 Walnut 项目

> 给 AI Agent 的入口文件。读完本篇 + 按顺序点开 2~3 个链接，即可建立对全项目的认知，不必瞎翻。

## 这个项目是什么

Walnut 是一个**业务系统开发基座**（Java + 内嵌 Nutz 框架），核心思想一句话：

**把一切数据源统一成一棵对象树，用命令操作它，用 JS 写业务。**

- 一切数据都是对象（文件/目录/会话/支付单……），对象 = 元数据 + 内容桶，
  元数据与内容可存在不同介质上（Mongo/SQL/Redis/文件/对象存储，可任意组合）
- 操作全部命令化（157 个命令：73 内置 + 84 扩展），支持管道与重定向，高仿 Linux
- 每个命令有 man 文档，随源码存放（`man/zh_cn/cmd_*.md`）
- 业务逻辑主要用 `jsc` 命令 + JS 脚本动态扩展，无需重新部署

## 阅读顺序（最少 token 路线）

1. **`doc/reference/INDEX.md`** — 全量文档索引（每篇一行：标题/模块/关键词）。
   需要了解什么主题，按关键词搜这里，直达对应文档。
2. **`doc/reference/10.concepts/README.md`** — 五大核心概念
   （标准对象、IO/Indexer/BM、Domain/Home、会话、命令模型），是理解一切的地基。
3. **`doc/reference/SOURCEMAP.md`** — 源码包 ↔ 文档的双向映射。
   已经拿到一个类/包名？查这张表找到对应文档，反之亦然。
4. 写代码前再看 **`doc/reference/101.dev/`**（扩展开发）和对应命令的 man 文档。

## 文档体系（doc/）

```text
doc/
|-- reference/    # 正式参考手册（分模块：00 快速开始 / 10 概念 / 20 存储 / 30 认证 /
|                 #   40 命令 / 50 HTTP API / 60 定时任务 / 70 功能场景 / 80 运维 / 90 开发）
|-- skill/        # 给 AI Agent 的技能文档（write-doc.skill.md = 写文档的规范与叙事风格）
|-- note/         # 历史工作笔记，非正式，仅供参考，可能过时
|-- _old/         # 旧文档存档，内容已与源码脱节，不要引用
```

- 知识文档之间的互跳链接尚未接通（冻结期），按 INDEX.md 的目录导航即可。
- 命令的权威文档在源码旁的 `man/zh_cn/`，格式：简单命令一篇，复杂/过滤命令主文档 + 每子命令一篇。
- 文档与源码冲突时，**以源码为准**；可顺手修正文档（遵守 `doc/skill/write-doc.skill.md`）。

## 源码布局（src/com/site0/walnut/）

```text
api/     # 核心接口（io/auth/hook/session……）—— 先读接口，语义都在 Javadoc
core/    # api 的主要实现（io 映射/索引器/桶管理器）
impl/    # 服务实现；impl/box/cmd/ = 73 个内置命令，每个一个文件
ext/     # 84 个扩展命令，按逻辑分类：data / geo / media / net / sys / util / xo
web/     # Jetty/HTTP 适配，regapi（动态 API）在这里
cron/    # WnCron 表达式引擎（Quartz 兼容 + 区间扩展）
ooml/    # OOML 文档格式（类 Markdown， Walnut 富文本）
login/   # 账户与会话；util/ alg/ val/ = 工具层
```

构建运行：Maven 项目（`pom.xml`），配置走 Nutz Ioc（`conf/ioc/`）。
上手步骤见 `doc/reference/00.start-here/quick-start.md`。

## 重要：这些已弃用，不要读不要改

- **`thing` 全系列**（概念与源码都将删除）
- **`600.site-app/` 相关**（`cmd_www` 已删）
- **`cmd_obj` 命令** → 用 **`cmd_o`** 代替（文档示例均已改用 `o`）
- **`cmd_crontab`**（旧单文件定时器，标记 @deprecated，用 `cron` 三件套代替）
- `bin/ client/ gradle/ build.py jdbc/` 基本弃用；`.gitignore` 忽略的文件都不用看

## 上手建议

1. 先跑通 `quick-start.md`，在控制台玩 `o` 命令（一切皆对象的最佳入口：`man cmd_o`）
2. 重点命令（设计最完整、最值得当范例读）：
   `cmd_o`（过滤）、`cmd_sqlx`（复杂）、`cmd_tmpl`（过滤）、`cmd_xapi`（复杂）
3. 每个命令源码头部注释 + 同目录 man 文档就是它的全部契约；改命令必改 man 文档
