---
title: cmd_o 查询
module: 40.commands
keywords: [cmd_o, @query, @get, @fetch, 对象查询, 条件, 分页, 排序]
source: [ext/data/o/]
related: [10-cmd-o-overview.md, ../101.dev/11-write-sqlx-tmpl.md]
---

# cmd_o 查询概述

拿到对象有三种入口：按路径、按 ID、按条件查询。
本篇讲 `@query` / `@get` / `@fetch` / `@mime` 四个过滤器，
覆盖「我要找到这些对象」的全部场景。

## 三种入口怎么选

```bash
o ~/photos/abc.jpg        # 路径入口：确定的一个对象，不存在则报错
o @get id1 id2 id3        # ID 入口：已经知道 ID，按 ID 批量取
o ~/photos @query '{...}' # 查询入口：按条件搜一批
```

## @query：按条件查询

`@query` 的条件是一个 JSON，语法与查询条件规范一致
（区间、IN、正则、模糊、取反，完整定义见 SQL 模板篇的 AutoMatch 一节）：

```bash
# 基本条件
o ~/photos @query '{tp:"jpg"}'

# 组合条件（AND）
o ~/photos @query '{tp:"jpg", len:"[100000,)"}'

# 多条件数组（OR）
o ~/photos @query '[{tp:"jpg"},{tp:"png"}]'

# 排序 / 跳过 / 限量
o ~/photos @query '{tp:"jpg"}' -sort '{ct:-1}' -limit 10 -skip 20

# 分页：输出 {list:[...], pager:{...}}
o ~/photos @query '{tp:"jpg"}' -sort '{ct:-1}' -limit 20 -pager

# 追加而非覆盖上下文（先查 A 再补查 B）
o ~/photos @query '{tp:"jpg"}' @query -append '{tp:"png"}'
```

常用的修饰参数：`-p`（指定父节点，缺省用上下文第一个对象）、
`-mine`（限定本域本组）、`-hidden`（含隐藏对象）、
`-quiet`（父节点不存在时静默）、`-path`（补全路径属性）。

## @get / @fetch：按 ID / 路径批量取

```bash
# 按 ID 取多个
o @get id1 id2 id3 @json -cqn

# 按路径取多个
o @fetch a.txt b.txt c.txt

# -race FILE：不存在则自动创建（确保拿到对象）
o @fetch -race FILE ~/logs/today.log

# -ignore：不存在的跳过；-fallback：只取第一个存在的
o @fetch a.txt b.txt -ignore
o @fetch a.txt b.txt -fallback

# -reset：丢弃之前的上下文，只保留本次结果
o ~/photos @query @get id9 -reset
```

## @mime：查 MIME 类型

```bash
# 看全部已注册的 MIME
o @mime

# 查询某几种后缀的 MIME，一行一个
o @mime jpg png -as value
```

## 常见查询任务

```bash
# 今天创建的对象
o ~/inbox @query '{ct:"[today,today+1d)"}'

# 文件名以 report 开头的
o ~/docs @query '{nm:"^report"}'

# 按 ID 批量取详情并精简字段
o @get id1 id2 id3 @json '^(id|nm|len|mime)$' -cqn
```

# 延伸阅读

- man cmd_o_query / man cmd_o_get / man cmd_o_fetch —— 参数权威
- 101.dev/11-write-sqlx-tmpl.md —— 条件 JSON 语法（AutoMatch）的完整定义
