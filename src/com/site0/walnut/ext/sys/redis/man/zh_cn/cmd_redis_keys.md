# 过滤器简介

`@keys` 按模式匹配查找键（对应 Redis `KEYS`）。

# 用法

```bash
redis @keys 模式

```

# 示例

```bash
# 查找 user 开头的所有键
redis @keys 'user:*'
