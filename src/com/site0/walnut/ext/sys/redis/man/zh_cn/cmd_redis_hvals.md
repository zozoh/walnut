# 过滤器简介

`@hvals` 列出哈希的全部字段值（对应 Redis `HVALS`），返回数组。

# 用法

```bash
redis @hvals 键
```

# 示例

```bash
redis @hvals user:1
# {"HVALS:user:1":["xiaobai","18","hangzhou"]}
```
