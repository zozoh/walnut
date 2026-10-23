# 过滤器简介

`@hdel` 删除哈希的一个或多个字段（对应 Redis `HDEL`）。

# 用法

```bash
redis @hdel 键 字段...

```

# 示例

```bash
# 删除字段
redis @hdel user:1 age
