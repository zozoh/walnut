# 过滤器简介

`@incrby` 键的值增加指定的整数（对应 Redis `INCRBY`）。

# 用法

```bash
redis @incrby 键 增量

```

# 示例

```bash
# 一次加 5
redis @incrby visit:count 5
