# 过滤器简介

`@exec` 执行 INSERT / UPDATE / DELETE 模板，或在 `-as list` 下批量执行。
结果默认写入 `result`，也可回查后放进管道上下文。

模板语法见
[编写 SQL 模板（sqlx）](@wn-doc::reference/101.dev/21-write-sqlx-tmpl.md)。

# 用法

```bash
sqlx @exec
  [sqlName]           # 模板名，如 pet.insert
  [-batch]            # 批量模式，需配合 @vars -as list
  [-test {...}]       # 上下文匹配条件，不满足则跳过执行
  [-explain]          # 执行前展开宏
  [-noresult]         # 不把本次结果写入上下文
  [-fetch_by name]    # 执行后按这个模板回查
  [-fetch_vars {...}] # 回查参数，默认用插入对象
  [-fetch_save key]   # 回查结果放入管道上下文；默认解构推入
```

- `-batch` 用第一条数据渲染 SQL 结构，后续数据按同一结构批量执行
- `-noresult` 常用于“插主表后还要插子表”，避免覆盖主表结果
- `-fetch_by` 与 `-noresult` 同时出现时，`-fetch_by` 不生效
- `-fetch_save` 只保存回查结果的第一条

# 示例

```bash
# 单条插入
sqlx @vars '{id:"P001",name:"Be"}' @exec pet.insert

# 批量插入
cat ~/pets.json | sqlx @vars -as list @exec pet.insert -batch

# 插入后回查，并放入管道上下文
sqlx @vars '{id:"P001",name:"Be"}' \
  @exec pet.insert -fetch_by pet.fetch -fetch_save pet

# 条件执行
sqlx @vars '{type:"dog"}' @exec pet.insert -test '{type:"dog"}'
```
