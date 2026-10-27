# 过滤器简介

`@hmset` 一次性设置哈希的多个字段（对应 Redis `HMSET`），返回 `"OK"`。

键值对通过 `-map` 传 JSON 对象。官方 Redis 4.0 起 `HMSET` 已被多字段 `HSET` 取代，
但在本过滤器里它仍是设置多字段的正确方式。

# 用法

```bash
redis @hmset 键 -map '{字段1:值1, 字段2:值2, ...}'
```

# 示例

```bash
redis @hmset user:1 -map '{name:"xiaobai", age:18, city:"hangzhou"}' @hgetall user:1
```
