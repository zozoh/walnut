---
title: sqlx 命令：SQL 数据库网关
module: 40.commands
keywords: [sqlx, SQL, 模板, @vars, @dvcheck, 事务, 乐观锁]
source: [ext/data/sqlx/]
related: [tmpl, command-model, ../20.io-storage/dao-mapping.md]
---

# sqlx 命令：SQL 数据库网关

`sqlx` 把外部 SQL 数据库变成 Walnut 命令：SQL 语句以**模板文件**形式存放在 `~/.sqlx/`，命令提供变量绑定、事务、乐观锁、缓存、多种输出。适合把业务数据库的查询/变更封装成可复用的命令和 API。

- 源码：`src/com/site0/walnut/ext/data/sqlx/`（`SqlxContext` + `hdl/` 处理器 + `processor/` + `expert/SqlDialect` 方言）
- 手册：`man sqlx` 及 11 个子页（`man sqlx vars`、`man sqlx trans` 等）

## 设计思路

1. **SQL 与代码分离**：语句写在 `~/.sqlx/*.sql` 模板里，改 SQL 不改代码，DBA 可直接维护；
2. **模板即接口**：模板语法与 `tmpl` 命令相同（见 [tmpl](tmpl.md)），并扩展了 `${@vars=where}` 动态条件占位——变量集合自动拼成 WHERE/SET 片段，没有值的变量自动忽略；
3. **一个文件多个语句**：用 `-- @name=xxx` 注释分段，按名引用（`pet.query`）；
4. **服务化**：`processor/`（`SqlProcessor/QueryProcessor/ExecProcessor`）与 `SqlDialect` 被 core 的 SqlIndexer、SqlBM 及 login 的 SqlSessionStore/SqlRoleStore 复用——这正是「复杂命令」的范式：命令只是壳，核心逻辑是公共库。

## 模板文件格式

```sql
-- ~/.sqlx/pet.sql
-- @name=query
-- @type=select
-- @pick=id,nm,price      # 输出白名单（可选）
-- @omit=secret           # 输出黑名单（可选）
-- @ignoreNil=true        # 变量为 null 时忽略该条件
SELECT * FROM t_pet WHERE ${@vars=where};

-- @name=update
UPDATE t_pet SET${@vars=update} WHERE id=?;
```

- `-- @name=xxx` 声明语句名；`-- @xxx=xx` 其他元数据；
- `${@vars=where}`：把命令上下文变量拼成 `AND` 连接的条件片段；
- `${@vars=update}`：拼成 `,` 连接的赋值片段；
- 普通占位 `${varName}` 直接取值，走 tmpl 语法（循环、分支、默认值都可用）。

## 适用场景

| 场景 | 说明 |
|---|---|
| 业务报表 | 复杂 SQL 封装成 `sqlx @query report.monthly -vars {...}` |
| 写接口 | `sqlx @exec` + `@dvcheck` 乐观锁，防止并发写冲突 |
| 多步事务 | `@trans` 把多条 exec 包成一个事务 |
| 数据迁移 | SQL 模板 + `sheet`/`jsonx` 管道导入导出 |

## 用法：上下文流水线

`sqlx` 的上下文：`input（stdin）→ @set/@vars 变量 → @exec/@query 执行 → 输出`。

```bash
# 查询：-vars 给模板变量
sqlx @vars '{nm:"旺财",kind:"dog"}' @query pet.query -cqn

# 防注入：-p 参数模式（变量按参数绑定而非拼接）
sqlx @vars '{nm:"'";DROP TABLE--"}' @query pet.query -p

# 结果写入管道上下文，供后续使用
sqlx @vars '{id:42}' @query pet.query -as obj -set myPet @json

# 渲染检查：不写库，看最终 SQL
sqlx @vars '{nm:"旺财"}' @view pet.query
```

### 执行与乐观锁

```bash
# UPDATE/INSERT/DELETE
sqlx @vars '{id:42, price:1999}' @exec pet.update

# @dvcheck：数据版本检查（乐观锁）
# 模板里写 UPDATE ... SET ver='${after}' WHERE id=${id} AND ver='${before}'
sqlx @vars '{id:42}' @dvcheck pet.vercheck ver 3:4
# 更新到 1 行才算通过，0 行说明被别人改过了
```

### 事务

```bash
echo '[
  {sql:"pet.update", vars:{id:42, price:1999}},
  {sql:"pet.update", vars:{id:43, price: 2999}}
]' | sqlx @trans
# 任一步失败整体回滚（细节见 man sqlx trans）
```

### 输出

```bash
sqlx @query pet.query @json -cqn        # JSON（默认）
sqlx @query pet.query @tab              # 表格
sqlx @pipe -cqn                         # 查看管线上下文（变量传递的中间态）
```

### 数据源与模板目录

- 数据源配置：`~/.dao/<daoName>.dao.json`（`{url, username, password, history}`）；
- `sqlx` 第一个位置参数是 daoName（默认 `default`），用 `-conf <路径>` 换模板目录（默认 `~/.sqlx`）；

```bash
sqlx mydb @vars '{nm:"旺财"}' @query pet.query    # 用 ~/.dao/mydb.dao.json 连接
```

### 缓存

```bash
sqlx @cache ...    # 查询结果缓存，见 man sqlx cache（适合字典表/配置表）
```

## 完整示例：订单统计接口

```sql
-- ~/.sqlx/order.sql
-- @name=summary
-- @pick=status,nb,total
SELECT status, COUNT(*) AS nb, SUM(fee) AS total
FROM t_order
WHERE 1=1 ${@vars=where}
GROUP BY status;
```

```bash
# API 命令内容：
sqlx @vars '{shop_id:"${http-qs-shop}", ct:{$gte:"${http-qs-from?2026-01-01}", $lt:"${http-qs-to?2027-01-01}"}}' \
    @query order.summary @json
```

## 注意事项

- 对外接口务必 `-p` 参数模式，字符串拼接 SQL 是注入温床；
- `@dvcheck` 要求表里有版本字段，且 `before/after` 由调用者管理（通常先查出版本号再提交）；
- 数据源连接配置在 `~/.dao/<daoName>.dao.json`（多环境用不同 daoName 文件）；
- 大结果集分页在 SQL 模板里做（`${@vars=where}` + LIMIT 占位），别一次拉全表。

## 延伸阅读

- [tmpl](tmpl.md) — 模板语法（sqlx 模板的基础）
- [o](o.md) — `@pipe` 可把 sqlx 结果转存对象
- [数据源映射](../20.io-storage/dao-mapping.md)
