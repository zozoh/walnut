# 过滤器简介

`@info` 读取 Redis 服务器信息（对应 Redis `INFO`），可按段读取。

# 用法

```bash
redis @info [段名]

```

# 示例

```bash
# 读取全部信息
redis @info
# 只读内存段
redis @info memory
