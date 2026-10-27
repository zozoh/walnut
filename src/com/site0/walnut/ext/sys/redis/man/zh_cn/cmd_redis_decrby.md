# 过滤器简介

`@decrby` 把键的整数值减去指定步长（对应 Redis `DECRBY`），等价于 `@incrby 键 -步长`，返回新值。

# 用法

```bash
redis @decrby 键 步长
```

# 示例

```bash
# 库存扣减
redis @decrby stock:sku1001 5

# 配合过期时间做每日限量
redis @setex quota:today 86400 100 @decrby quota:today 1
```
