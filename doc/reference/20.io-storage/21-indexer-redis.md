---
title: 索引管理器 - Redis
module: core.indexer.redis
keywords: [索引管理器, redis, 规划中, 键值, 按ID取]
source:
  - conf/ioc/io.js
related: [10-io-mapping, 20-indexer-mongo, 33-bm-redis]
---

# 索引管理器 - Redis

> **状态：规划中，暂未实现。** `conf/ioc/io.js` 的 `indexers` 里预留了 `redis` 键，
> 当前值为 `null`，源码中没有对应的索引器实现类。

## 设计意图

Redis 是键值存储，查询能力弱（基本只能按 key 取），但读写极快。
作为索引管理器，它适合这样的子树：

- 对象**只按 ID 访问**，不需要按字段查询、排序、聚合
- 元数据极小、生命周期短（配合 `expi` 过期机制正好）

典型的想象空间：会话索引、临时令牌、高频计数器的元数据侧。
如果元数据需要被 `match` 查询，请用 MongoDB 或 SQL。

## 规划中的语法

```bash
mnt : "redis"
mnt : "redis(配置名)"
```

参数将指向 Redis 连接配置（预期与 Redis 桶管理器 的 `~/.io/bm/*.json`
同一套 `WedisConfig` 格式）。在实现落地前，以上语法仅供参考，不要用于生产配置。

## 扩展阅读

- 桶管理器 - redis — Redis 作为桶管理器**已经实现**，小文件内容的高频读写可用它
- IO 映射 — 映射机制总览
