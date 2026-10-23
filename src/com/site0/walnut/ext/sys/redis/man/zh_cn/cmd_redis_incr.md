# 过滤器简介

`@incr` 键的值自增 1（对应 Redis `INCR`）。

# 用法

```bash
redis @incr 键

```

# 示例

```bash
# 访问计数加 1 并读出
redis @incr visit:count @get visit:count
