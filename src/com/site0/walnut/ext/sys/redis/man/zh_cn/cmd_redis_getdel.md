# 过滤器简介

`@getdel` 读取键的值并删除该键（对应 Redis `GETDEL`）。

# 用法

```bash
redis @getdel 键

```

# 示例

```bash
# 读完即删
redis @getdel tmp:code
