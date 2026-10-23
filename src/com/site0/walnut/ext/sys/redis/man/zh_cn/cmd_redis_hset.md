# 过滤器简介

`@hset` 设置哈希的一个字段（对应 Redis `HSET`）。

# 用法

```bash
redis @hset 键 字段 值

```

# 示例

```bash
# 设置用户名字段
redis @hset user:1 name xiaobai
