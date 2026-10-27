# 过滤器简介

`@getset` 设置新值并**返回旧值**（对应 Redis `GETSET`），整个操作是原子的。

典型用途：轮换一个值并拿到上一个值，比如定期重置计数器、切换状态。

# 用法

```bash
redis @getset 键 新值
```

# 示例

```bash
# 读出旧值并清零计数器
redis @incrby page:pv 57 @getset page:pv 0
# {"INCRBY:page:pv":57,"GETSET:page:pv":"0"}

# 切换站点状态
redis @getset site:mode maintenance
```
