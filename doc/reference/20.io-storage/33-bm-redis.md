---
title: 桶管理器 - redis
module: core.bm.redis
keywords: [桶管理器, redis, RedisBM, 小文件, 会话, WedisConfig, io/bm]
source:
  - core/mapping/bm/RedisBMFactory.java
  - core/bm/redis/
related: [10-io-mapping, 21-indexer-redis, 30-bm-sha1]
---

# 桶管理器 - redis 概述
内容存进 Redis。内存级的读写速度，适合**小文件的高频读写**——会话对象、
临时缓存、验证码正文这类「几十 KB 以内、读写极频繁、丢了也能重建」的内容。

## mnt 语法

```bash
mnt : "://redis({配置名})"
```

```bash
# 索引用全局 Mongo，内容进 Redis
mnt : "://redis(sess)"
```

## 配置参数：Redis 连接定义

`redis` 桶的配置不在 `mnt` 字符串里，而是一份 JSON：

- **root 组成员**：可直接使用 `conf/ioc/io.js` 里 `redisBMFactory.bms`
  预定义的连接（默认注册了 `_`）
- **普通用户**：在自己主目录下放 `~/.io/bm/{配置名}.json`，
  `mnt` 里的配置名就是文件名（去 `.json`）

文件格式为 `WedisConfig`：

```js
{
  "name" : "sess",           // 配置名（可省略）
  "host" : "127.0.0.1",
  "port" : 6379,
  "ssl"  : false,
  "password" : null,
  "database" : 5,
  "connectionTimeout" : 2000,
  "soTimeout" : 2000,
  "maxTotal" : 50,
  "maxIdle" : 10,
  "minIdle" : 1,
  "setup" : { "prefix" : "io:bm:sess:" }   // 键前缀，防止串库
}
```

找不到配置时抛 `e.io.bm.UndefinedRedisBM`。

## 注意事项

- Redis 有容量上限且内存昂贵：**大文件不要进 Redis**（经验值：1M 以内的小内容）
- 非持久化的 Redis 实例重启后内容即失——存进去的东西必须是「丢了也能重建」的
- 高频筛选的对象元数据不要指望 Redis（它没有查询能力），元数据侧请用
  MongoDB 或 SQL

## 扩展阅读

- IO 映射 — 映射机制总览
- 索引管理器 - Redis — Redis 做索引器（规划中），与做桶是两种用法
