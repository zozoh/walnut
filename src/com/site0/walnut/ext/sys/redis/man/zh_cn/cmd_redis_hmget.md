# 过滤器简介

`@hmget` 一次读取哈希的多个字段（对应 Redis `HMGET`）。

# 用法

```bash
redis @hmget 键 字段...

```

# 示例

```bash
# 一次读两个字段
redis @hmget user:1 name age
