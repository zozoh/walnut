# 过滤器简介

`@scan` 游标方式遍历键（对应 Redis `SCAN`），避免 `KEYS` 阻塞。

# 用法

```bash
redis @scan [游标] [-match 模式] [-count 每批数量]
  -match    # 只返回匹配模式的键
  -count    # 每批返回的数量，默认 100
```

# 示例

```bash
# 从头遍历一批键
redis @scan 0 -match 'user:*' -count 100
