# 过滤器简介

`@exists` 检查一个或多个键是否存在（对应 Redis `EXISTS`），返回存在的个数
（不是每个键一个结果，是所有键合计）。

# 用法

```bash
redis @exists 键 [键...]
```

# 示例

```bash
redis @exists mykey                 # 1 或 0
redis @exists a b c                 # 3 个里存在几个

# 判断后再决定初始化
redis @exists cfg:init @setnx cfg:init done
```
