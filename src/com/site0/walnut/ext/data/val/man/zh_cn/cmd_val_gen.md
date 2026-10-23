# 过滤器简介

`@gen` 按规则生成值，是 `val` 命令的核心过滤器。

规则语法由 `ValueMakers` 支持，例如 `int:1,100`（区间整数）、日期、字符串等；
生成的多个值会去重并排序后输出。

# 用法

```bash
val @gen 规则 [-n 数量] [-json]
  -n      # 生成个数，默认 1
  -json   # 以 JSON 数组输出；生成多于 1 个时默认就是 JSON
```

# 示例

```bash
# 生成一个 1 到 100 的随机整数
val @gen 'int:1,100'

# 生成 10 个（自动以 JSON 数组输出）
val @gen 'int:1,100' -n 10

# 强制 JSON 输出
val @gen 'int:1,100' -n 3 -json

# 结合上下文变量
val @vars '{prefix:"ID"}' @gen '${prefix}-${int:1,999}'
```
