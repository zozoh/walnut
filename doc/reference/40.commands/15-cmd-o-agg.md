---
title: cmd_o 聚集统计
module: 40.commands
keywords: [cmd_o, @agg, @count, @size, 聚合, 统计, 分组, TOP]
source: [ext/data/o/]
related: [10-cmd-o-overview.md, 16-cmd-o-output.md, 11-cmd-o-query.md]
---

# cmd_o 聚集统计概述

不用写 SQL，直接对对象做聚合统计：分组、计数、求和、TOP N。
本篇讲 `@agg` / `@count` / `@size` 三个过滤器。

## @agg：分组聚合

`@agg` 的聚合参数是「位置自由」的声明式写法，过滤器按参数形式判断意图：

```bash
o ~/tmp/fakedoc @agg value=COUNT:id name=TIMESTAMP_TO_DATE:ct type=tp TOP10 -tab
```

- `输出字段=[函数:]键名` —— 聚合/分组声明：
  - `value=COUNT:id` —— 聚合计算，函数支持 `COUNT|MAX|MIN|SUM|AVG`
  - `name=TIMESTAMP_TO_DATE:ct` —— 分组键，默认按原始值分组（RAW）；
    键是 `ct|lm|expi` 时自动按天分组
- `TOP10` / `DATA100` —— 最多输出多少条 / 最多取多少条记录
- `name:DESC` —— 按输出字段排序
- `-tab` —— 表格输出（`-bish` 控制边框/行号/汇总/表头）

### 时间转换函数

```bash
# 按天统计
name=AMS_TO_DATE:ct
# 按小时统计
name=AMS_TO_AHOU:ct
```

## @count：条件计数

比「查询后数长度」更省——直接在索引层完成计数：

```bash
# 直接输出一个数字
o ~/mydir @count '{ct:"[today,today+1d)"}'

# 计数结果放进指定键（配合 @summary 输出）
o ~/mydir @count -as today '{ct:"[today,today+1d)"}' @summary -cqn
# {"today":4}

# 限定范围
o ~/orders @count '{status:"paid"}' -skip 0 -limit 1000
```

## @size：上下文对象数量

```bash
# 查询结果有多少个
o ~/photos @query @size
# 34

# 放进指定键输出
o ~/photos @query @size n -cqn
# {"n":34}
```

`@size` 数的是**当前上下文**里的对象数；`@count` 是按条件**重新统计**。
前者是管道里的「数一下」，后者是索引层的「查一下」。

## 常见统计任务

```bash
# 每天新增多少文件（按天分组计数）
o ~/inbox @agg value=COUNT:id day=AMS_TO_DATE:ct -tab

# 本周最活跃的 10 个用户
o ~/logs @query '{ct:"[today-7d,)"}' \
  @agg value=COUNT:id user=uid TOP10 user:DESC -tab

# 图片总大小
o ~/photos @agg value=SUM:len
```

# 延伸阅读

- man cmd_o_agg / cmd_o_count / cmd_o_size —— 参数权威
- 11-cmd-o-query.md —— 条件 JSON 语法
