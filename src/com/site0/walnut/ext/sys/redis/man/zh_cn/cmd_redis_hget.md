# 过滤器简介

`@hget` 读取哈希的一个字段（对应 Redis `HGET`），字段不存在返回 `null`。

# 用法

```bash
redis @hget 键 字段
```

# 示例

```bash
redis @hget user:1 name
# {"HGET:user:1":"xiaobai"}
```
