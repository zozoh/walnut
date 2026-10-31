---
title: cmd_o 操作上下文
module: 40.commands
keywords: [cmd_o, 上下文, @var, @filter, @push, @pop, @put, @remove, @flat]
source: [ext/data/o/]
related: [10-cmd-o-overview.md, 12-cmd-o-load.md]
---

# cmd_o 操作上下文概述

`o` 的过滤器链共享一个「上下文」：一个对象列表 + 一组变量。
本篇讲直接加工上下文的过滤器：过滤列表、读写变量、
操作对象内部的数组/文档字段、在树里游走。

## @filter：筛掉不要的对象

```bash
# 只保留 nm=abc 的
o ~/photos @query @filter 'nm:"abc"'

# 多个条件是 OR
o ~/photos @query @filter '[{tp:"jpg"},{tp:"png"}]'
```

## @var：上下文变量

变量与对象列表并存，可以被后续过滤器引用（配合 `-explain` 动态赋值）：

```bash
# 设置变量（$AGE 引用环境变量），再用于更新
o id:xxx @var a $AGE -autojava @update 'age:"=a"' -explain

# 查看变量
o @var a -view
```

## @push / @pop：操作数组字段

```bash
# 压入值（自动解析类型：1→数字，true→布尔）
o abc @push -to names A B C
o abc @push -to names 1 true A -raw        # -raw 全当字符串

# 唯一压入
o abc @push -uniq -to names A A C

# 弹出：names:2 从后弹两个；names:-1 从前弹一个；names:<all> 清空
o abc @pop names:2
o abc @pop 'names:!=xyz'      # 弹出值等于 xyz 的项
o abc @pop 'names:^a.*'       # 弹出被正则匹配的项
```

`@pop` 的 VAL 语法很丰富（下标、等值、列表、正则、空值），
完整表格见 man cmd_o_pop。

## @put / @remove：操作内部文档字段

```bash
# 向 pet 文档字段合并内容
o abc @put pet '{age:15}'

# 只补缺省值（已有 name 则不覆盖）
o abc @put pet '{name:"haha"}' -dft

# 删除 pet 里的字段
o abc @remove pet age
```

## @name：分析文件名

```bash
# 把主文件名解析到 name 键（默认）
o @fetch abc.txt @name @json '%NM'
```

## @enter / @flat：在树里进出与展平

这两个与 @tree / @children 配合（见 12-cmd-o-load.md），
职责分别是「进入树的某一层作为新上下文」和「把树展平回列表」。

## @clear：清空上下文

```bash
# 丢弃之前所有结果
o ~/accounts/index @clear
```

# 延伸阅读

- man cmd_o_filter / cmd_o_var / cmd_o_push / cmd_o_pop / cmd_o_put —— 参数权威
- 03-cmd-filter.md —— 过滤命令的上下文机制
- 12-cmd-o-load.md —— @enter / @flat 的树形场景
