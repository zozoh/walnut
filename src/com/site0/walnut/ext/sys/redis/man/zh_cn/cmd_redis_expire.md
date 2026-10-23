# 过滤器简介

`@expire` 给键设置多少秒后过期（对应 Redis `EXPIRE`）。

# 用法

```bash
redis @expire 键 秒

```

# 示例

```bash
# 让键 1 小时后过期
redis @expire mykey 3600
