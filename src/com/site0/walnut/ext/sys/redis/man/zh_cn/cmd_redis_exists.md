# 过滤器简介

`@exists` 判断一个或多个键是否存在（对应 Redis `EXISTS`），返回存在的个数。

# 用法

```bash
redis @exists 键...

```

# 示例

```bash
# 判断键是否存在
redis @exists mykey
