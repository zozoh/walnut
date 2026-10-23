# 过滤器简介

`@psetex` 设置键值并指定多少毫秒后过期（对应 Redis `PSETEX`）。

# 用法

```bash
redis @psetex 键 毫秒 值

```

# 示例

```bash
# 5000 毫秒后过期
redis @psetex code 5000 8888
