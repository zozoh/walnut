# 过滤器简介

`@decrby` 键的值减少指定的整数（对应 Redis `DECRBY`）。

# 用法

```bash
redis @decrby 键 减量

```

# 示例

```bash
# 一次减 3
redis @decrby store:apple 3
