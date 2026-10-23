# 过滤器简介

`@hget` 读取哈希的一个字段（对应 Redis `HGET`）。

# 用法

```bash
redis @hget 键 字段

```

# 示例

```bash
# 读取用户名
redis @hget user:1 name
