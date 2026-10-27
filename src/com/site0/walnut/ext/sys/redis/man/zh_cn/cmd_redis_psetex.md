# 过滤器简介

`@setex` 的毫秒版（对应 Redis `PSETEX`），过期时间单位为**毫秒**。

适合亚秒级过期的场景，如防抖标记、短窗口限流。

# 用法

```bash
redis @psetex 键 毫秒 值
```

# 示例

```bash
# 500 毫秒后自动消失的互斥标记
redis @psetex lock:api:call 500 busy
```
