# 过滤器简介

`@setbit` 设置字符串某偏移位的比特值（对应 Redis `SETBIT`），返回该位原来的值（0/1）。

偏移从 0 开始。因为操作是原子的，海量布尔状态（签到、打卡、去重）
可以压进一个字符串，这就是 Redis **Bitmap** 玩法。

# 用法

```bash
redis @setbit 键 偏移 0|1
```

# 示例

```bash
# 第 0 天签到
redis @setbit sign:u1001 0 1
redis @setbit sign:u1001 1 1
redis @setbit sign:u1001 2 0
```
