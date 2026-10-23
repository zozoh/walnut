# 过滤器简介

`@hgetall` 读取哈希的全部字段和值（对应 Redis `HGETALL`）。

# 用法

```bash
redis @hgetall 键

```

# 示例

```bash
# 读出整个用户哈希
redis @hgetall user:1
