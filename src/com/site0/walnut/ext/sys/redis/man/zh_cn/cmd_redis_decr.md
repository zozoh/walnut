# 过滤器简介

`@decr` 键的值自减 1（对应 Redis `DECR`）。

# 用法

```bash
redis @decr 键

```

# 示例

```bash
# 库存减 1
redis @decr store:apple
