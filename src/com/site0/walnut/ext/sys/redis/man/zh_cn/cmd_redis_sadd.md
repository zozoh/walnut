# 过滤器简介

`@sadd` 往集合添加一个或多个成员（对应 Redis `SADD`），返回**实际新增**的成员数
（已存在的成员不计入）。

集合内成员唯一、无序，天然适合**去重**（已发用户名单、IP 黑名单）。

# 用法

```bash
redis @sadd 键 成员 [成员...]
```

# 示例

```bash
# 记录已发放奖励的用户
redis @sadd reward:sent u1001 u1002 u1003

# 批量添加，重复的自动忽略
redis @sadd reward:sent u1001 u1004
# {"SADD:reward:sent":1}
```
