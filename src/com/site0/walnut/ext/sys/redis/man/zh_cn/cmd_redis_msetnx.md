# 过滤器简介

`@msetnx` 一次设置多个键值，只要有一个键已存在就全部不设置（对应 Redis `MSETNX`）。

# 用法

```bash
redis @msetnx -map '{键:值,...}'
  -map    # JSON 对象，键为键名，值为键值
```

# 示例

```bash
# 原子设置多个键
redis @msetnx -map '{k1:100,k2:200}'
