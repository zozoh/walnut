---
title: .dao 数据源与 SQL 表映射
module: core.indexer.sql
keywords: [dao, 数据源, SQL映射, 表映射, sqlx, 模板, SqlIndexer, SqlBM]
source:
  - ext/sys/sql/WnDaos.java
  - core/mapping/indexer/SqlIndexerFactory.java
  - core/indexer/sql/SqlIndexer.java
  - core/mapping/bm/SqlBMFactory.java
  - core/mapping/support/SqlIoArgs.java
  - core/mapping/support/SqlIoOptions.java
related: [mount-mapping, sqlx, io-model, query-agg]
---

# .dao 数据源与 SQL 表映射

把目录映射到一张 SQL 表：元数据进关系库（支持事务/联查/海量），内容可继续走全局桶，
也可以落进同一张表的 Blob 字段。需要 **一个数据源配置 + 一份 SQL 模板 + 一个 `mnt`**。
SQL 模板复用 sqlx 体系（`~/.sqlx/*.sql`），数据源就是 sqlx 的 `.dao` 配置。

## 第一步：定义数据源 `~/.dao/{name}.dao.json`

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

`mnt` 里不指定数据源名时，默认使用 `default`。

## 第二步：编写 SQL 模板 `~/.sqlx/{entityName}.sql`

`sql` 映射**没有字段映射配置**：表长什么样、怎么读写，完全由你写的 SQL 模板决定。
实体名就是模板文件名（去掉 `.sql`）。元数据操作需要这些模板指令：

- **`insert`** — 插入记录
- **`update_by`** — 按条件 `filter` 更新记录的 `meta`
- **`count`** — 统计符合条件的记录数（结果集第一列即返回值）
- **`select`** — 按条件查询记录的 `columns` 字段，支持 `sorter` 排序和 `limit`/`skip` 翻页
- **`remove`** — 按 id 删除记录
- **`inc`** — 按条件将指定字段增加 `val`

**没有定义的指令，对应的 IO 操作会抛错**——模板按需编写，用不到的操作可以不实现。
模板语法与变量体系与 sqlx 完全一致，见 [sqlx](../40.commands/sqlx.md)。

内容也进 SQL 表时（见下文「内容也进表」一节），在同一文件里追加 `_content` 后缀的指令：
`insert_content` / `update_content` / `count_content` / `fetch_content` / `delete_content`。

## 第三步：挂映射

```bash
mount ~/biz/orders 'sql(demo:t_news)'
# 之后 ~/biz/orders 下的对象全部落在 t_news 表
```

## `sql(...)` 参数详解

```bash
sql({?daoName}:{entityName}:{?options}:{?filter})
```

- **`daoName`** — 数据源名（`~/.dao/{daoName}.dao.json`），省略为 `default`
- **`entityName`** — 实体名，必选，对应 `~/.sqlx/{entityName}.sql`
- **`options`** — 空格分隔的列声明开关，告诉 SqlIndexer 你的表有哪些内置字段列：

  - `AMS` — `ct`、`lm`、`expi`、`synt` 四个时间字段以 long 毫秒存储（默认会转成时间字符串，适配 `DateTime` 列）
  - `d0d1` — 表含 `d0`、`d1` 内部字段列
  - `cmg` — 表含 `c`、`m`、`g` 内部字段列（可单独写 `c`、`cm`、`g` 等任意组合）
  - `md` — 表含 `md`（mode 权限）列
  - `pid` — 表含 `pid`（父对象 ID）列

- **`filter`** — JSON 过滤条件：查询时固定并入条件，插入记录时也固定带入，
  一张表可以按此切出多个映射（如 `{species:"dog"}` 只映射狗的数据）

```bash
# 完整例子：demo 数据源 + t_news 实体 + 时间用 long 存储 + 只映射 dog 记录
mnt : "sql(demo:t_news:AMS:{species:\"dog\"})"
```

## 查询怎么用

映射目录照常支持 `WnQuery`（相等、区间、正则、排序、翻页），由 SqlIndexer 翻译成 SQL，
语义见 [query-agg](query-agg.md)：

```bash
o ~/biz/orders -match 'status:"NEW",ct:[1720000000000,)' -sort 'ct:-1' -limit 20 -cqn
```

## 内容也进表：`sql` 桶

桶侧写 `sql({daoName}:{entityName})`，内容存同一张表的 Blob 列：

```bash
mnt : "sql(demo:t_news)://sql(demo:t_news)"
```

模板文件里需要内容指令（未定义的指令会跳过对应操作）：

- **`insert_content`** — 插入内容记录
- **`update_content`** — 存在时更新内容
- **`count_content`** — 按 id 统计
- **`fetch_content`** — 按 id 读取
- **`delete_content`** — 按 id 删除

**元数据与内容同表时**，只需定义 `update_content` / `delete_content` 两个指令——
插入与查询由元数据指令顺带完成，非常适合大量小文件的场景。
参考表结构：

```sql
-- id 即对象 ID；content 为字节内容
id VARCHAR(26) PRIMARY KEY,  -- 对象 ID
nm VARCHAR(128),             -- 对象名称
tp VARCHAR(32),              -- 类型后缀
mime VARCHAR(64),            -- 内容 MIME
sha1 VARCHAR(40),            -- 内容 SHA1
len INT,                     -- 内容大小
ct DATETIME(3),              -- 记录创建时间
lm DATETIME(3),              -- 最后修改
content BLOB                 -- 字节内容
```

## 与 sqlx 的关系

- **形态** — sql 映射：目录=表，对象=行，**标准 WnObj 操作** — sqlx：`~/.sqlx/*.sql` 模板，**自由 SQL**
- **适合** — sql 映射：对象生命周期管理（增删改查带元数据） — sqlx：报表、联查、批量写、复杂 SQL
- **身份** — sql 映射：任何命令/脚本透明使用 — sqlx：`sqlx` 命令显式调用
- **共用** — 同一个 `~/.dao/` 数据源、同一套 `~/.sqlx/` 模板

sqlx 命令详见 [sqlx](../40.commands/sqlx.md)。

## 注意事项

- **没有自动建表**：表结构由你掌控，建表/迁移用 `sqlx` 命令执行 DDL。
- 高频筛选的字段请建真实表列并在 `select` 模板里输出；塞在 meta JSON 里的字段不走 SQL 索引。
- `options` 声明与真实表结构必须一致，否则查询/写入会因缺列报错。
- 内容进表适合小文件；大文件走全局 SHA1 桶或 [vxds 对象存储](mount-mapping.md)。
