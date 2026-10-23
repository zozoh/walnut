# 过滤器简介

`@getset` 设置键的新值并返回旧值（对应 Redis `GETSET`）。

# 用法

```bash
redis @getset 键 新值

```

# 示例

```bash
# 改值并拿到旧值
redis @getset mykey 200
