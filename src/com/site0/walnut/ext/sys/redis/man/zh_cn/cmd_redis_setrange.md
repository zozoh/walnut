# 过滤器简介

`@setrange` 从指定下标开始覆写键值的一部分（对应 Redis `SETRANGE`）。

# 用法

```bash
redis @setrange 键 下标 内容

```

# 示例

```bash
# 从下标 5 开始覆写
redis @setrange mykey 5 'WORLD'
