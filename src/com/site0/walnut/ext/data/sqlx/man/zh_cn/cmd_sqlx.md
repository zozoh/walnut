# 命令简介

`sqlx` 执行用户目录 `~/.sqlx` 下的 SQL 模板，完成查询、增删改、事务和历史记录。

模板文件、`${@vars}` 语法、条件值和 `EXISTS` 配方统一见
[编写 SQL 模板（sqlx）](@wn-doc::reference/101.dev/21-write-sqlx-tmpl.md)；
本篇只讲命令怎么调用。

一个 `.sql` 文件可以包含多条实体：

```sql
-- @name = pet.select
-- @type = SELECT
SELECT id, name FROM t_pet WHERE 1=1
${@vars=where; prefix=AND}

-- @name = pet.update
-- @type = UPDATE
UPDATE t_pet SET ${@vars=update; omit=id} WHERE id=${@vars=param; name=id}
```

调用名是“文件名.实体名”，例如 `pet.select`。

# 用法

```bash
sqlx [daoName] [@filter ...]
```

- `daoName`：数据源名，缺省 `default`
- `-conf <dir>`：模板目录，缺省 `~/.sqlx`
- 过滤器按出现顺序执行；前一个过滤器的 `result` / 管道上下文可给后一个使用
- 模板写错时先用 `@view ... -p` 看渲染结果

# 过滤器列表

```bash
@vars       # 设置本次执行的变量集合
@set        # 设置上下文变量（ID、时间、序列等）
@pipe       # 设置/查看管道上下文
@dvcheck    # 数据版本检查
@trans      # 设置本命令周期的事务级别
@exec       # 执行 INSERT / UPDATE / DELETE
@query      # 执行 SELECT
@view       # 只渲染 SQL，不执行
@cache      # 查看或清理模板缓存
@json       # 控制结果 JSON 输出
@tab        # 把结果输出为表格
@hislog     # 控制本次执行的历史记录
```

# 子命令文档

- [`@vars`](cmd_sqlx_vars.md) —— 输入变量集合；模板语法见 21 篇，不在本命令里
- [`@set`](cmd_sqlx_set.md) —— 生成/设置变量
- [`@pipe`](cmd_sqlx_pipe.md) —— 管道上下文
- [`@dvcheck`](cmd_sqlx_dvcheck.md) —— 乐观锁式版本检查
- [`@trans`](cmd_sqlx_trans.md) —— 事务
- [`@exec`](cmd_sqlx_exec.md) —— 非查询执行
- [`@query`](cmd_sqlx_query.md) —— 查询
- [`@view`](cmd_sqlx_view.md) —— 预览渲染
- [`@cache`](cmd_sqlx_cache.md) —— 模板缓存
- [`@json`](cmd_sqlx_json.md) —— JSON 输出
- [`@tab`](cmd_sqlx_tab.md) —— 表格输出
- [`@hislog`](cmd_sqlx_hislog.md) —— 历史记录
