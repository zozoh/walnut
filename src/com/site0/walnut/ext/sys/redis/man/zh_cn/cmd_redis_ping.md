# 过滤器简介

`@ping` 探测 Redis 是否存活（对应 Redis `PING`），正常返回 `"PONG"`。

常用于健康检查和部署后的连通性验证。

# 用法

```bash
redis @ping
```

# 示例

```bash
# 健康检查脚本里
redis @ping -cqn
# "PONG"
```
