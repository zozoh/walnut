# 过滤器简介

`@info` 返回 Redis 服务器的统计与配置信息（对应 Redis `INFO`）。

不传参数返回全部段落（内存、客户端、持久化、统计……，文本很长）；
传段落名可只取一部分，如 `server` / `clients` / `memory` / `stats` / `keyspace`。

# 用法

```bash
redis @info [段落名]
```

# 示例

```bash
# 全部信息（长文本）
redis @info

# 只看内存
redis @info memory

# 查看 keyspace 了解各库键数量
redis @info keyspace
```
