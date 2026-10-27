# 过滤器简介

`@hsetnx` 只在字段**不存在**时写入（对应 Redis `HSETNX`），返回 `1`（写入）或 `0`（已存在）。

适合「只初始化一次」的字段，比如用户注册时间。

# 用法

```bash
redis @hsetnx 键 字段 值
```

# 示例

```bash
# 首次访问记录注册时间，重复执行不覆盖
redis @hsetnx user:1 created_at '2026-10-27 12:00'
```
