# 过滤器简介

`@hlen` 返回哈希的字段数量（对应 Redis `HLEN`）。

# 用法

```bash
redis @hlen 键
```

# 示例

```bash
redis @hlen user:1    # {"HLEN:user:1":3}
```
