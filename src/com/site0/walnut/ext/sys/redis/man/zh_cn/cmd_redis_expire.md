# 过滤器简介

`@expire` 给已存在的键设置过期时间，单位**秒**（对应 Redis `EXPIRE`），返回 1（成功）或 0（键不存在）。

重新设置会**覆盖**原有的过期时间。想查剩余时间用 `@ttl`。

# 用法

```bash
redis @expire 键 秒
```

# 示例

```bash
# 会话续期 30 分钟
redis @expire session:abc 1800

# 配合 @setnx 做带超时的锁
redis @setnx lock:job1 owner-A @expire lock:job1 30
```
