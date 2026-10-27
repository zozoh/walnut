# 过滤器简介

`@hstrlen` 返回哈希某字段值的字符串长度（对应 Redis `HSTRLEN`），字段不存在返回 0。

# 用法

```bash
redis @hstrlen 键 字段
```

# 示例

```bash
redis @hstrlen user:1 name    # {"HSTRLEN:user:1.name":7}
```
