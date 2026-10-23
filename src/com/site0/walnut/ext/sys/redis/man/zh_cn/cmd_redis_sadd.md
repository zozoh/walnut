# 过滤器简介

`@sadd` 向集合添加一个或多个成员（对应 Redis `SADD`）。

# 用法

```bash
redis @sadd 键 成员...

```

# 示例

```bash
# 添加标签
redis @sadd tags red green blue
