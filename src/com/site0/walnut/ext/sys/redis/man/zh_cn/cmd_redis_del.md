# 过滤器简介

`@del` 删除一个或多个键（对应 Redis `DEL`）。

# 用法

```bash
redis @del 键...

```

# 示例

```bash
# 删除单个键
redis @del mykey
# 删除多个键
redis @del key1 key2 key3
