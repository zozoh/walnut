# 过滤器简介

`@smembers` 列出集合的全部成员（对应 Redis `SMEMBERS`）。

# 用法

```bash
redis @smembers 键

```

# 示例

```bash
# 列出全部标签
redis @smembers tags
