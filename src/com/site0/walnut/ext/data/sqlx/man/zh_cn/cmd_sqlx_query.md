# 过滤器简介

`@query` 执行 SELECT 模板，结果默认作为 `result` 输出；也可以用 `-set` 放进管道上下文。

模板语法见
[编写 SQL 模板（sqlx）](@wn-doc::reference/101.dev/21-write-sqlx-tmpl.md)。

# 用法

```bash
sqlx @query
  [sqlName]           # 模板名，如 pet.select
  [-p]                # 参数模式，避免 SQL 注入；生产查询建议始终加
  [-by {...}]         # 对每条结果做 Explain 形状转换
  [-set key]          # 结果写入管道上下文
  [-val {...}]        # -set 时对结果做 Explain 转换
  [-as obj|list]      # -set 模式：obj 只取第一条，默认 list
```

- 不加 `-set`：结果由 `@json` / `@tab` 等后续过滤器输出
- `-set ...`：解构推入管道上下文；`-set pet` 则放到 `pet` 键
- `-as obj` 只对 `-set` 生效

# 示例

```bash
# 参数化查询并输出
sqlx @vars '{filter:{st:1}}' @query pet.select -p

# 查询结果放入管道上下文
sqlx @vars '{id:"xxx"}' @query pet.fetch -p -set pet -as obj

# 直接转换结果形状
sqlx @vars '{filter:{st:1}}' @query pet.select -p -by '{id:id,name:name}'
```
