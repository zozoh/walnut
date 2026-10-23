# 过滤器简介

`@setbit` 设置键值在指定偏移处的比特位（对应 Redis `SETBIT`），值为 0 或 1。

# 用法

```bash
redis @setbit 键 偏移 0或1

```

# 示例

```bash
# 把偏移 7 置 1
redis @setbit bits 7 1
