---
title: 桶管理器 - sql
module: core.bm.sql
keywords: [桶管理器, sql, SqlBM, blob, 内容进表, 小文件, _content]
source:
  - core/mapping/bm/SqlBMFactory.java
  - core/bm/sql/SqlBM.java
related: [10-io-mapping, 22-indexer-sql]
---

# 桶管理器 - sql

内容存进 SQL 表的 **Blob 字段**。和 索引管理器 - sql 搭配，
元数据与内容可以落在**同一张表**——大量小文件（头像、缩略图、配置快照）的场景非常合适：
一份数据一条 SQL 事务，备份联查都方便。

## mnt 语法

```bash
sql({?daoName}:{entityName})://sql({?daoName}:{entityName})
```

```bash
# 元数据与内容同表：t_news 既存元数据，也存内容
mnt : "sql(demo:t_news)://sql(demo:t_news)"
```

- `daoName` — 数据源名，同索引侧，省略为 `default`（`~/.dao/*.dao.json`）
- `entityName` — 实体名，对应 `~/.sqlx/{entityName}.sql`（与索引侧同一个模板文件）

## 配置参数：内容指令

在 `~/.sqlx/{entityName}.sql` 里，内容操作以 `_content` 后缀声明：

- **`insert_content`** — 插入内容记录
- **`update_content`** — 存在时更新内容
- **`count_content`** — 按 id 统计
- **`fetch_content`** — 按 id 读取
- **`delete_content`** — 按 id 删除

**未定义的指令会跳过对应操作**（这一点比索引侧的「抛错」宽松）。

**同表存储时只需两个指令**：`update_content` + `delete_content`——
插入与查询由元数据的 `insert`/`select` 顺带完成。

## 参考表结构

```sql
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

## 注意事项

- 适合**小文件**；大文件请走 sha1 或 vxds
- 没有自动建表：DDL 用 `sqlx` 命令执行

## 扩展阅读

- 索引管理器 - sql — 元数据侧的模板指令（insert/select/...）
- .dao 数据源与 SQL 表映射 — 完整工作流与 sqlx 的分工
