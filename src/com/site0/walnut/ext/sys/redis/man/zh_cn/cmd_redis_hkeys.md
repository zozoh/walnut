# 过滤器简介

`@hkeys` 列出哈希的全部字段名（对应 Redis `HKEYS`）。

# 用法

```bash
redis @hkeys 键

```

# 示例

```bash
# 列出全部字段
redis @hkeys user:1
