# 过滤器简介

`@hexists` 检查哈希里某字段是否存在（对应 Redis `HEXISTS`），返回 `1` / `0`。

# 用法

```bash
redis @hexists 键 字段
```

# 示例

```bash
redis @hexists user:1 name     # 1
redis @hexists user:1 email    # 0
```
