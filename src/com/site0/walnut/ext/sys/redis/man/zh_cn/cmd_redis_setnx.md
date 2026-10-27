# 过滤器简介

`@setnx` 只在键**不存在**时设置值（对应 Redis `SETNX`），返回 `1`（设置成功）或 `0`（键已存在）。

这是官方 `SET key value NX` 的替代，常用于**分布式锁**的加锁原语。

# 用法

```bash
redis @setnx 键 值
```

# 示例

```bash
# 不存在才写入
redis @setnx lock:job1 owner-A
# {"SETNX:lock:job1":1}

# 已存在则写入失败
redis @setnx lock:job1 owner-B
# {"SETNX:lock:job1":0}

# 配合过期时间做简易锁：加锁 + 续期
redis @setnx lock:job1 owner-A @expire lock:job1 30
```
