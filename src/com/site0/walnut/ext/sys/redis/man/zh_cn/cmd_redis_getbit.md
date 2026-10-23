# 过滤器简介

`@getbit` 读取键值在指定偏移处的比特位（对应 Redis `GETBIT`）。

# 用法

```bash
redis @getbit 键 偏移

```

# 示例

```bash
# 读取偏移 7 的位
redis @getbit bits 7
