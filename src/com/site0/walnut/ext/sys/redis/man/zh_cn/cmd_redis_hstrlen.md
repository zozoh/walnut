# 过滤器简介

`@hstrlen` 查看哈希某字段值的长度（对应 Redis `HSTRLEN`）。

# 用法

```bash
redis @hstrlen 键 字段

```

# 示例

```bash
# 字段值长度
redis @hstrlen user:1 name
