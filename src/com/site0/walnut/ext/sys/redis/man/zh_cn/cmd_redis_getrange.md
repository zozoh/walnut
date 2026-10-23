# 过滤器简介

`@getrange` 截取键值的一段（对应 Redis `GETRANGE`），下标从 0 开始，含两端。

# 用法

```bash
redis @getrange 键 起始 结束

```

# 示例

```bash
# 截取前 4 个字符
redis @getrange mykey 0 3
