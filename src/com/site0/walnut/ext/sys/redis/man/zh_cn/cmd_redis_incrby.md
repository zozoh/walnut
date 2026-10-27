# 过滤器简介

`@incrby` 把键的整数值加上指定步长（对应 Redis `INCRBY`），返回新值。

步长可以是负数（等价于减法）。原子操作。

# 用法

```bash
redis @incrby 键 步长
```

# 示例

```bash
# 库存扣减 3 件
redis @incrby stock:sku1001 -3

# 积分增加
redis @incrby user:1001:score 50
```
