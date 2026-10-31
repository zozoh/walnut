---
title: cmd_o 修改删除与生成序号
module: 40.commands
keywords: [cmd_o, @create, @update, @delete, @inc, 批量更新, 序号]
source: [ext/data/o/]
related: [10-cmd-o-overview.md, 11-cmd-o-query.md]
---

# cmd_o 修改删除概述

对象的创建、更新、删除、编号，都在本篇四个过滤器里。
它们共同的模式是：**对上下文里的每个对象执行操作**，
所以「先 @query 查出目标，再操作」是标准玩法。

## @update：更新字段

```bash
# 更新一个对象
o ~/accounts/index @update 'x:100,y:99'

# 从管道读取更新内容
echo 'x:100,y:99' | o ~/accounts/index @update

# 先查再批量更新：把今天的 jpg 都打上已扫描标记
o ~/photos @query '{ct:"[today,today+1d)"}' @update 'scanned:true' @quiet
```

`-explain` 支持动态赋值（配合 @var 做变量替换），见 man cmd_o_update。

## @create：创建对象

```bash
# 当前目录下创建文件
o @create abc.txt

# 创建目录
o @create dir/abc -race DIR

# 指定元数据创建
o @create '{nm:"xiaobai", age:12}'

# 一次多个
o @create a.txt b.txt c.txt

# 指定父目录（-auto 会自动补建父目录）
o @create -p ~/my/dir -auto x.txt y.txt

# 已存在则覆盖（按 nm 判断）
o @create report.csv -upsert

# 从 JSON 管道批量创建
cat objs.json | o @create
```

`-keep` 表示创建后不清空已有上下文（默认新对象替换上下文）。

## @delete：删除

```bash
# 删除文件
o ~/path/to/a.txt @delete

# 递归删除目录
o ~/path/to/dir @delete -r

# 先查再删（输出被删对象，便于确认删了什么）
o ~/tmp @query '{ct:"[today-90d,)"}' @delete @json -cqn
```

> 注意：不加 `-r` 时，非空目录删不掉（会报错）。

## @inc：生成序号

`@inc` 利用对象字段做计数器，生成连续序号——业务单号场景专用：

```bash
# 基于 ~/tmp/seq1 对象的 value 字段，生成 4 位对齐序号
o @fetch -race FILE ~/tmp/seq1 @inc -pad 4
# 0001
# 再执行一次 → 0002

# 静默自增（只要副作用不要输出）
o @fetch -race FILE ~/tmp/seq1 @inc -quiet

# 按 JSON 输出，序号放进指定键
o @fetch -race FILE ~/tmp/seq1 @inc -json -key no -l -cqn
```

每次执行，对象的计数键（默认 `value`）原子加 N（默认 1），
并把当前值渲染输出。与 `val @gen seq:` 的区别：`@inc` 就地累加
一个对象字段，适合「每天一个计数器」这类简单场景；
`val @gen seq:` 支持前缀、日期滚动对象等复杂规则（见 write-jsbin 篇）。

# 延伸阅读

- man cmd_o_create / cmd_o_update / cmd_o_delete / cmd_o_inc —— 参数权威
- 11-cmd-o-query.md —— 「先查再操作」的查询侧
