---
title: 桶管理器 - mem
module: core.bm.mem
keywords: [桶管理器, mem, 内存, 规划中, 临时内容]
source:
  - conf/ioc/io.js
related: [10-io-mapping, 33-bm-redis, 30-bm-sha1]
---

# 桶管理器 - mem 概述
> **状态：规划中，暂未实现。** 源码中没有 `mem` 桶管理器的实现类，
> `conf/ioc/io.js` 的 `bms` 里也没有注册 `mem` 键。

## 设计意图

内容直接放内存，速度最快、重启即失。比 Redis 桶 更极致的临时性：

- 内容极小（1K 左右）、读写极其频繁
- 完全不需要持久化，甚至不跨节点共享（纯本机 JVM 内存）

典型的想象空间： regapi 请求对象的临时缓冲、计算过程的中间产物。

## 规划中的语法

```bash
mnt : "://mem"
mnt : "://mem(名称)"
```

在实现落地前，以上语法仅供参考，不要用于生产配置。
现在就要类似的效果，可以用 Redis 桶 加短 `expi` 代替。

## 扩展阅读

- IO 映射 — 映射机制总览
- 桶管理器 - redis — 已实现的内存级桶
