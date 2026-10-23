# 过滤器简介

`@hmset` 一次设置哈希的多个字段（对应 Redis `HMSET`），字段以 `-map` 传入。

# 用法

```bash
redis @hmset 键 -map '{字段:值,...}'
  -map    # JSON 对象，键为字段名，值为字段值
```

# 示例

```bash
# 一次设置两个字段
redis @hmset user:1 -map '{name:xiaobai,age:18}'
