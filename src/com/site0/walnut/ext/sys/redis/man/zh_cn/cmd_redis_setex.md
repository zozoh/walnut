# 过滤器简介

`@setex` 设置值并同时指定过期时间，单位为**秒**（对应 Redis `SETEX`）。

等价于 `@set 键 值 -ex 秒`，返回 `"OK"`。键已存在会被覆盖。

# 用法

```bash
redis @setex 键 秒 值
```

# 示例

```bash
# 保存 5 分钟有效的验证码
redis @setex sms:code:13900001111 300 8845

# 保存带时效的缓存
redis @setex cache:frontpage 60 '<html>...</html>'
```
