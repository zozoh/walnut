# 过滤器简介

`@hdel` 删除哈希的一个或多个字段（对应 Redis `HDEL`），返回实际删除的字段数。

字段删光后哈希键本身也会被 Redis 自动回收。

# 用法

```bash
redis @hdel 键 字段 [字段...]
```

# 示例

```bash
redis @hdel user:1 age city
```
