# 过滤器简介

`@mset` 一次设置多个键值（对应 Redis `MSET`），键值以 `-map` 传入。

# 用法

```bash
redis @mset -map '{键:值,...}'
  -map    # JSON 对象，键为键名，值为键值
```

# 示例

```bash
# 一次设置两个键
redis @mset -map '{k1:100,k2:200}'
