# 过滤器简介

`@hexists` 判断哈希的字段是否存在（对应 Redis `HEXISTS`）。

# 用法

```bash
redis @hexists 键 字段

```

# 示例

```bash
# 字段是否存在
redis @hexists user:1 name
