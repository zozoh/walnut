# 过滤器简介

`@setex` 设置键值并指定多少秒后过期（对应 Redis `SETEX`）。

# 用法

```bash
redis @setex 键 秒 值

```

# 示例

```bash
# 60 秒后过期的验证码
redis @setex code 60 8888
