# 过滤器简介

`@hsetnx` 字段不存在时才设置哈希字段（对应 Redis `HSETNX`）。

# 用法

```bash
redis @hsetnx 键 字段 值

```

# 示例

```bash
# 字段已存在则不覆盖
redis @hsetnx user:1 name xiaobai
