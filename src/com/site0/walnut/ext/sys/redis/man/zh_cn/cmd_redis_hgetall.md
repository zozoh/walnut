# 过滤器简介

`@hgetall` 读取哈希的**全部**字段和值（对应 Redis `HGETALL`），返回 `{字段:值, ...}`。

字段少的哈希直接用它最省事；字段很多且只取部分时，请用 `@hkeys` + `@hmget` 控制流量。

# 用法

```bash
redis @hgetall 键
```

# 示例

```bash
redis @hgetall user:1
# {"HGETALL:user:1":{"name":"xiaobai","age":"18","city":"hangzhou"}}
```
