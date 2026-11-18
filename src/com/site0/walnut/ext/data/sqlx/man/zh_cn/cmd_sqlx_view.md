# 过滤器简介

`@view` 只渲染 SQL 模板，不连库执行。用于确认模板最终长什么样、参数顺序对不对。

模板语法见
[编写 SQL 模板（sqlx）](@wn-doc::reference/101.dev/21-write-sqlx-tmpl.md)。

# 用法

```bash
sqlx @view
  [sqlName]          # 模板名，如 pet.select
  [-p]               # 参数模式：输出 ? 和参数列表
  [-explain]         # 渲染前展开时间等宏
  [-i]               # 列表模式：每条上下文前打印序号
  [-start 1]         # -i 的起始序号
  [-cqn]             # -p 模式输出参数时使用的 JSON 格式
```

- 不写 `-p`：文本模式，值直接拼成 SQL，适合肉眼看结构
- 写 `-p`：预编译模式，SQL 里是 `?`，下面列出参数名和值
- 模板文件改过但输出没变，先执行 `sqlx @cache -clear`

# 示例

```bash
# 看文本 SQL
sqlx @vars '{filter:{name:"Be"}}' @view pet.select

# 看参数化 SQL + 参数
sqlx @vars '{filter:{name:"Be"}}' @view pet.select -p

# 批量上下文逐条预览
cat ~/pets.json | sqlx @vars -as list @view pet.insert -i
```
