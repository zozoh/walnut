# 过滤器简介

`@incr` 把键的整数值加 1（对应 Redis `INCR`），返回加完后的新值。

- 键不存在时按 0 开始
- 值不是整数会报错
- 操作原子，是计数器的首选实现（PV、点赞数、库存扣减）

# 用法

```bash
redis @incr 键
```

# 示例

```bash
# 页面访问计数
redis @incr page:index:pv @get page:index:pv

# 每天一个计数器
redis @incr pv:20261027
```
