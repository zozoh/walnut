# 命令简介

`val` 值生成器：按规则生成测试数据（数字、字符串、日期等）

通过 `@gen` 指定生成规则，`@vars` 提供上下文变量，`@macro` 做宏展开。
常用于构造假数据、生成默认值。

# 用法

```bash
val @gen 规则 [-n 数量] [-json] [@vars 名值...] [@macro 文本]
```

支持的过滤器：

```bash
@gen     # 按规则生成值（规则语法见 ValueMakers）
@vars    # 设置上下文变量（JSON；=键 表示从管道整体读入）
@macro   # 宏展开文本（-n 结尾不换行）
```

# 示例

```bash
# 生成一个随机整数
val @gen 'int:1,100'

# 生成 10 个并存成 JSON 数组
val @gen 'int:1,100' -n 10

# 带上下文变量
val @vars '{prefix:"ID"}' @gen '${prefix}-${int:1,999}'

# 宏展开
val @macro '今天是 $date:now'
```
