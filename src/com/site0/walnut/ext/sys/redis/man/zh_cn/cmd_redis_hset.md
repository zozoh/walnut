# 过滤器简介

`@hset` 设置哈希的一个字段（对应 Redis `HSET`）。

- 键不存在时自动创建哈希
- 字段已存在则覆盖，返回 `0`；新字段返回 `1`

> ⚠️ 官方 Redis 4.0+ 的 `HSET` 支持一次设置多个字段，本过滤器目前只支持单字段；
> 多字段请用 `@hmset`。

# 用法

```bash
redis @hset 键 字段 值
```

# 示例

```bash
# 写一个用户档案
redis @hset user:1 name xiaobai
redis @hset user:1 age 18 @hgetall user:1
# {"HSET:user:1":0,"HGETALL:user:1":{"name":"xiaobai","age":"18"}}
```
