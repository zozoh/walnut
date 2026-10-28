---
title: cmd_o 输出
module: 40.commands
keywords: [cmd_o, @json, @ajax, @tmpl, @tab, @value, @summary, @zip, 输出格式]
source: [ext/data/o/]
related: [10-cmd-o-overview.md, 15-cmd-o-agg.md]
---

# cmd_o 输出概述

过滤器链的末尾是输出。本篇讲控制输出的八个过滤器：
JSON / AjaxResult / 模板 / 表格 / 取值 / 汇总 / 打包 / 静默。

## 默认输出与 @quiet

不加输出过滤器时，`o` 默认把上下文对象按 JSON 输出（受 `-cqn` 控制）。
`@quiet` 完全禁止输出——写操作的标准收尾：

```bash
o ~/photos @query @update 'scanned:true' @quiet
```

## @json / @ajax：结构化输出

```bash
# 全部字段
o @get id1 id2 @json -cqn

# 只输出指定键（AutoMatch，^ 开头是正则）
o @get id1 id2 @json '^(id|nm|len)$' -cqn

# 快捷名：过滤掉所有标准字段（只留业务字段）
o ~/data @query @json '%EXT' -cqn

# 快捷名变体：%EXT-NM / %EXT-TP / %EXT-TC / %EXT-THC
# （分别保留 nm / nm+tp / nm+tp+时间 / nm+tp+时间+thing 字段）

# AjaxResult 包裹（regapi 响应直接可用）
o @get id1 @ajax -cqn
# {"ok":true,"data":{...}}
```

两者都支持 `-path`（补全路径）、`-mapping`（BeanMapping 转换）、
`-explain`（再加工），见 man。

## @tmpl / @tab：格式化输出

```bash
# 行模板：每个对象渲染一行
o ~/abc @query '{tp:"txt"}' @tmpl '${id} : ${nm}'

# 自定义分隔符
o ~/abc @query @tmpl '${id}' -sep ','

# 表格
o ~/abc @query @tab -bish id nm title
```

## @value：取单个字段的值

```bash
# 取出所有 id，一行一个
o ~/abc @query '{tp:"txt"}' @value id

# 逗号连接
o ~/abc @query @value id -sep ','

# JSON 数组
o ~/abc @query @value id -as json -cqn
```

## @summary：输出汇总字段

与 @count / @inc 等会产生「汇总键」的过滤器配合：

```bash
o ~/mydir @count -as today '{ct:"[today,today+1d)"}' @summary -cqn
# {"today":4}
```

## @zip：打包下载

```bash
# 查询结果打包，输出压缩包对象
o ~/mydir @query @zip

# 指定输出路径
o ~/mydir @query @zip ~/backup/mydir.zip -quiet

# 指定父节点与文件名模板
o ~/orders @query '{ct:"[today,today+1d)"}' \
  @zip ~/out/orders.zip -base ~/orders -fnm '${nm}-${id}'
```

# 延伸阅读

- man cmd_o_json / cmd_o_tmpl / cmd_o_tab / cmd_o_zip —— 参数权威
- 50.regapi —— @ajax 输出与 regapi 响应的衔接
