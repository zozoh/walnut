# 命令简介

`redis` 直接操作 Redis 数据源

配置来自 `~/.redis/<名称>.json`（`-conf` 指定，缺省取默认配置）。
每个 `@过滤器` 对应一条 Redis 命令，最后一个过滤器的结果作为输出。
结果键的渲染方式：位置参数 `I`/`K`/`A` 前缀（下标/键/数组）+ 分隔符。

# 用法

```bash
redis [-conf 名称] [渲染键] @过滤器... [-cqn]
```

支持的过滤器（与 Redis 命令一一对应）：

```bash
@get @set @setnx @setex @psetex @getset @getdel @getex @del @exists
@expire @ttl @type @keys @scan @incr @decr @incrby @decrby
@mset @msetnx @append @strlen @getrange @setrange
@setbit @getbit @bitcount @sadd @smembers
@hset @hsetnx @hmset @hget @hmget @hgetall @hdel @hexists @hkeys @hvals @hlen @hstrlen
@ping @info
```

# 示例

```bash
# 设置并读取键
redis @set mykey 100 @get mykey

# 哈希操作
redis @hset user:1 name xiaobai @hgetall user:1

# 指定数据源
redis -conf cache @get session:abc

# 键过期
redis @setex code 60 8888 @get code

# 自增计数
redis @incr visit:count @get visit:count
```
