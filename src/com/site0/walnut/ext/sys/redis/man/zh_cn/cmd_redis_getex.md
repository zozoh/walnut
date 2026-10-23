# 过滤器简介

`@getex` 读取键的值并设置过期时间（对应 Redis `GETEX`），必须给出一个过期选项。

# 用法

```bash
redis @getex 键 [-ex 秒 | -px 毫秒 | -exAt 秒级时间戳 | -pxAt 毫秒级时间戳]
  -ex     # 多少秒后过期
  -px     # 多少毫秒后过期
  -exAt   # 在指定的秒级时间戳过期
  -pxAt   # 在指定的毫秒级时间戳过期
```

# 示例

```bash
# 读取并指定 10 秒后过期
redis @getex session:abc -ex 10
