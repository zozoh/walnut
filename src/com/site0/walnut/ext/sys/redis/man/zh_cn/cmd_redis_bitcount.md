# 过滤器简介

`@bitcount` 统计键值中被置 1 的比特位数（对应 Redis `BITCOUNT`），可只统计指定区间。

# 用法

```bash
redis @bitcount 键 [起始 结束]

```

# 示例

```bash
# 统计全部
redis @bitcount bits
# 只统计前 8 个字节
redis @bitcount bits 0 7
