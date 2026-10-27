# 过滤器简介

`@hkeys` 列出哈希的全部字段名（对应 Redis `HKEYS`），返回数组。

# 用法

```bash
redis @hkeys 键
```

# 示例

```bash
redis @hkeys user:1
# {"HKEYS:user:1":["name","age","city"]}
```
