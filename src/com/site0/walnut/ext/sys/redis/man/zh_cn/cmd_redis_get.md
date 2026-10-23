# 过滤器简介

`@get` 读取键的值（对应 Redis `GET`）。

# 用法

```bash
redis @get 键

```

# 示例

```bash
# 读取键的值
redis @get mykey
# 管道中使用：先写后读
redis @set mykey 100 @get mykey
