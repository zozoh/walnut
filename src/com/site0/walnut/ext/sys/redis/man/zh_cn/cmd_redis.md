# 命令简介

`redis` 直接操作 Redis 数据源。它的每个 `@过滤器` 与 Redis 原生命令**一一对应**，
用法和语义与官方 Redis 命令一致（参考 redis.io 命令手册），
只是参数改写成 Walnut 过滤器的风格。

- 连接配置来自 `~/.redis/<名称>.json`（`-conf` 指定名称，缺省 `default`）
- 一条 `redis` 命令里可以串联任意多个过滤器，它们按顺序执行在同一个连接上
- 最后一个过滤器（其实每个过滤器）的结果汇总成一个 JSON Map 输出

# 用法

```bash
redis [-conf 名称] [结果键格式] @过滤器... [-cqn]
```

## 结果键格式

每个过滤器执行完，结果存进结果 Map，键的格式由位置参数控制：

- 由 `I`（过滤器下标）、`A`（动作名）、`K`（键名） 三个字母组合而成，控制键里包含哪些段以及顺序
- 默认 `AK`，即 `动作名:键名`，如 `SET:mykey`
- 分隔符默认 `:`，跟在字母后面，如 `redis AK::` 用 `::` 分隔

```bash
# 输出形如 {"GET:mykey":"100"}
redis @set mykey 100 @get mykey

# 输出形如 {"mykey:GET":"100"}（键名在前）
redis KA: @set mykey 100 @get mykey
```

## 连接配置

```json5
// ~/.redis/default.json
{
  "host": "127.0.0.1",
  "port": 6379,
  "password": "xxxxxx",     // 可选
  "database": 0,            // 可选，默认 0
  "ssl": false,             // 可选
  "connectionTimeout": 2000,
  "soTimeout": 2000
}
```

多数据源就放多个文件：`~/.redis/cache.json`、`~/.redis/session.json`，
用 `-conf cache` 切换。

## 支持的过滤器（按数据类型分组）

字符串：

```bash
@get @set @setnx @setex @psetex @getset @getdel @getex
@append @strlen @getrange @setrange
@setbit @getbit @bitcount
@mset @msetnx
@incr @decr @incrby @decrby
```

键：

```bash
@del @exists @expire @ttl @type @keys @scan
```

哈希：

```bash
@hset @hsetnx @hmset @hget @hmget @hgetall
@hdel @hexists @hkeys @hvals @hlen @hstrlen
```

集合：

```bash
@sadd @smembers
```

服务器：

```bash
@ping @info
```

每个过滤器的详细说明与示例见同目录的 `cmd_redis_<过滤器名>.md`。

# 示例

```bash
# 设置并读取（注意输出是 JSON Map）
redis @set mykey 100 @get mykey
# {"SET:mykey":"OK","GET:mykey":"100"}

# 带过期时间写入：-ex 秒，-px 毫秒，-exAt/-pxAt 绝对时间戳
redis @set code 8888 -ex 60 @ttl code

# 哈希操作
redis @hset user:1 name xiaobai @hgetall user:1

# 批量写：-map 接受 JSON 对象
redis @mset -map '{a:1,b:2,c:3}' @get a

# 自增计数
redis @incr visit:count @get visit:count

# 游标遍历（生产环境替代 KEYS）
redis @scan 0 -match 'user:*' -count 100

# 指定数据源 + 只看某个过滤器的结果
redis -conf cache @get session:abc -cqn

# 管道配合：查出对象后把值写进 Redis
o ~/cfg/theme.json -cqn | redis @set site:theme
```

# 注意事项

- `@set` 目前只支持过期参数（`-ex/-px/-exAt/-pxAt`），**不支持** `NX/XX`
  （不存在才写 / 存在才写）语义，请用 `@setnx` 表达「不存在才写」
- `@getex` 必须带 `-ex/-px/-exAt/-pxAt` 之一，否则不会执行任何操作
- `@keys` 会一次性扫描全库，键多时会阻塞 Redis，遍历请用 `@scan`
- 复杂场景（多字段 HSET、NX/XX、Lua 脚本等）暂不支持，可用 `jsc` 命令
  写 JS 脚本通过 `sys.exec` 或直接操作
