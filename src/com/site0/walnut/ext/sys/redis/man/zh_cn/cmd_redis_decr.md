# 过滤器简介

`@decr` 把键的整数值减 1（对应 Redis `DECR`），等价于 `@incrby 键 -1`，返回新值。

# 用法

```bash
redis @decr 键
```

# 示例

```bash
redis @set countdown 10 @decr countdown
# {"SET:countdown":"OK","DECR:countdown":9}
```
