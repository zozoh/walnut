# 过滤器简介

`@setnx` 键不存在时才设置值（对应 Redis `SETNX`）。

# 用法

```bash
redis @setnx 键 值

```

# 示例

```bash
# 只有键不存在时写入
redis @setnx lock:1 100
