# 过滤器简介

`@set` 设置键的值（对应 Redis `SET`），可附带过期选项。

# 用法

```bash
redis @set 键 值 [-ex 秒 | -px 毫秒 | -exAt 秒级时间戳 | -pxAt 毫秒级时间戳]
  -ex     # 多少秒后过期
  -px     # 多少毫秒后过期
  -exAt   # 在指定的秒级时间戳过期
  -pxAt   # 在指定的毫秒级时间戳过期
```

# 示例

```bash
# 设置键值
redis @set mykey 100
# 设置并指定 60 秒过期
redis @set code 8888 -ex 60
