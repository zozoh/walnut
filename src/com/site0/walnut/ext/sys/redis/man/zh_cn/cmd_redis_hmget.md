# 过滤器简介

`@hmget` 一次读取哈希的多个字段（对应 Redis `HMGET`），按参数顺序返回数组，
不存在的字段对应位置为 `null`。

# 用法

```bash
redis @hmget 键 字段1 字段2 [...]
```

# 示例

```bash
redis @hmget user:1 name age no_such_field
# {"HMGET:user:1":["xiaobai","18",null]}
```
