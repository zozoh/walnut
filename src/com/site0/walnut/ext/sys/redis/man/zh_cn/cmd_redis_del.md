# 过滤器简介

`@del` 删除一个或多个键（对应 Redis `DEL`），返回实际删除的个数。

对任何类型的键都有效；键不存在时自动跳过。

# 用法

```bash
redis @del 键 [键...]
```

# 示例

```bash
# 删除一个键
redis @del tmp:cache

# 批量删除
redis @del session:a session:b session:c

# 配合 @keys 清掉一类键（生产慎用，见 @keys 说明）
redis @del `redis @keys 'tmp:*' -cqn`
```
