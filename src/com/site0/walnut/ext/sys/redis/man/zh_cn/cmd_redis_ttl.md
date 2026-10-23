# 过滤器简介

`@ttl` 查看键还有多少秒过期（对应 Redis `TTL`），-1 表示不过期，-2 表示键不存在。

# 用法

```bash
redis @ttl 键

```

# 示例

```bash
# 查看剩余时间
redis @ttl mykey
