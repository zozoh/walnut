---
title: 索引管理器 - SQL
module: core.indexer.sql
keywords: [索引管理器, sql, SqlIndexer, sqlx, dao, 数据源, 模板, AMS, d0d1, cmg, md, pid, filter]
source:
  - core/mapping/indexer/SqlIndexerFactory.java
  - core/indexer/sql/SqlIndexer.java
  - core/mapping/support/SqlIoArgs.java
  - core/mapping/support/SqlIoOptions.java
  - ext/sys/sql/WnDaos.java
related: [10-io-mapping, 32-bm-sql, sqlx]
---

# 索引管理器 - SQL

把子树的元数据存进一张 SQL 表。适合订单、流水这类**海量、结构化、需要事务与联查**的数据。
表长什么样完全由你决定——`sql` 索引器没有字段映射配置，读写规则全写在一套 SQL 模板里。

## mnt 语法

```bash
sql({?daoName}:{entityName}:{?options}:{?filter})
```

```bash
# 默认数据源 + t_news 实体 + 时间字段用 long 存储 + 只映射 dog 记录
mnt : "sql(:t_news:AMS:{species:\"dog\"})"
```

## 配置参数

参数按冒号分四段，只有 `entityName` 是必须的：

- **`daoName`** — 数据源名，对应 `~/.dao/{daoName}.dao.json`（连接串、账号、连接池）。
  省略为空串时（`sql(:t_news)`）使用 `default` 数据源
- **`entityName`** — 实体名，必选。对应 `~/.sqlx/{entityName}.sql` 模板文件，
  实体名就是模板文件名（去 `.sql`）
- **`options`** — 空格分隔的开关，告诉索引器你的表有哪些内置字段列：

  - `AMS` — `ct`、`lm`、`expi`、`synt` 四个时间字段以 long 毫秒存储
    （默认会转成时间字符串，适配 `DateTime` 列）
  - `d0d1` — 表含 `d0`、`d1` 内部字段列
  - `cmg` — 表含 `c`、`m`、`g` 内部字段列（可写任意组合，如 `cm`、`g`）
  - `md` — 表含 `md`（mode 权限）列
  - `pid` — 表含 `pid`（父对象 ID）列

- **`filter`** — JSON 条件：查询时固定并入条件，插入记录时也固定带入。
  一张物理表可以按它切出多个映射（如 `{species:"dog"}` 只映射狗的数据）

## SQL 模板：`~/.sqlx/{entityName}.sql`

模板复用 sqlx 体系。元数据操作需要这些指令：

- **`insert`** — 插入记录
- **`update_by`** — 按条件更新记录的 `meta`
- **`count`** — 统计符合条件的记录数（结果集第一列即返回值）
- **`select`** — 按条件查询记录的 `columns` 字段，支持 `sorter` 排序和 `limit`/`skip` 翻页
- **`remove`** — 按 id 删除记录
- **`inc`** — 按条件将指定字段增加 `val`

**没有定义的指令，对应的 IO 操作会抛错**——用不到的操作可以不实现。
模板语法与变量体系见 sqlx。

## 数据源：`~/.dao/{daoName}.dao.json`

```js
{
  "url" : "jdbc:mysql://127.0.0.1:3306/walnut",
  "username" : "root",
  "password" : "root",
  "maxActive": 50,
  "maxWait" : 15000,
  "testWhileIdle" : true
}
```

## 完整例子

```bash
# 数据源 ~/.dao/demo.dao.json
# 模板   ~/.sqlx/t_news.sql
mount ~/biz/news 'sql(demo:t_news:AMS)'
o ~/biz/news -match 'status:"NEW"' -sort 'ct:-1' -limit 20 -cqn
```

## 注意事项

- **没有自动建表**：表结构由你掌控，建表/迁移用 `sqlx` 命令执行 DDL
- `options` 声明必须与真实表结构一致，否则查询/写入会因缺列报错
- 高频筛选字段请建真实表列并在 `select` 模板里输出；塞进 meta JSON 的字段不走 SQL 索引
- 想让**内容也进同一张表**？见 桶管理器 - sql

## 扩展阅读

- sqlx — SQL 模板语法
