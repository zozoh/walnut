---
title: 锁服务（lock）
module: 70.functionate
keywords: [lock, 锁, 分布式锁, redis, 并发控制, privateKey]
source: [api/lock/, impl/lock/, ext/sys/lock/]
related: [README, ../../../src/com/site0/walnut/ext/sys/lock/man/zh_cn/cmd_lock.md]
---

# 锁服务（lock）概述

一个业务系统常常有「同一时刻只允许一个执行者做某件事」的需求：
定时任务防重入、批量任务分片、防止并发写同一份数据……

`Walnut` 为此内置了一个锁服务。它对外只有一个接口
`com.site0.walnut.api.lock.WnLockApi`，系统里的任何模块
（命令、Java 代码、定时任务）都通过它申请和释放锁。
你日常则是通过 `lock` 命令来操作它；在 [jsc 执行的
jsbin 脚本](../101.dev/20-write-jsbin.md)里，也是通过 `lock`
命令来使用锁服务。

## 锁对象与 privateKey

一把锁的核心信息（`WnLock` 接口）：

- **name**：锁名，全局唯一，定位一把锁
- **owner**：持有者名称（通常是用户名）
- **hint**：一段文字线索，说明为什么加这把锁，死锁排查时非常有用
- **hold / expi**：持有时间 / 过期时间（绝对毫秒），锁是**自动过期**的，
  即使持有者崩溃没有释放，锁到期后也会被抢走，不会产生永久死锁
- **privateKey**：每次加锁成功随机生成的密钥

`privateKey` 是这个设计里最关键的一环：

- 加锁成功时服务端返回一次 `privateKey`，**只给持有者**
- 释放锁必须提供匹配的 `privateKey`
- 别人即便 `get` 到锁信息（`list` / `@get` 返回的锁对象中
  `privateKey` 一律被抹掉），也无法释放你的锁

也就是说，「能看」不等于「能放」，谁加锁谁释放。

## 两种实现

### Redis 实现：系统的默认锁服务

`com.site0.walnut.impl.lock.redis.QuickRedisLockApi`

基于 `SET NX EX` 原子语义加锁——锁名即 Redis 键，锁值就是
序列化后的锁对象（`privateKey;name=...,owner=...,...`），
过期时间即锁的有效期。释放锁用一段 Lua 脚本把
「比对 privateKey + 删除」合并成一个原子操作，避免
「比对通过但锁被抢走再误删」的竞态。

加锁失败后的重试由两层参数控制（实现内部的短重试，
配置键 `setup` 里的 `retry-interval` / `retry-times`），
更长周期的等待重试由命令层的 `@try -block` 负责。

它是系统默认实现，由 ioc 装配（`conf/ioc/service.js` 的
`lockApi`），Redis 连接来自 `conf/ioc/redis.js` 的
`redisConfForLockApi`：

- 连接参数：`redis-host` / `redis-port` / `redis-ssl` /
  `redis-password` / `redis-db-lock`（锁专用 db，默认 2）
- `setup` 配置（conf 键 → 默认值）：
  - `prefix` ← `lock-redis-prefix`，锁键前缀，默认 `lock:`
  - `retry-interval` ← `lock-ask-retry-interval`，加锁失败后的
    重试间隔（毫秒），默认 100
  - `retry-times` ← `lock-ask-retry-times`，加锁失败后的重试
    次数，默认 5

### 内存实现：单机测试用

`com.site0.walnut.impl.lock.memory.MemoryLockApi`

全部锁存放在一个进程内的 `HashMap` 里。加锁 / 释放都是
`synchronized` 方法，天然原子；过期判断由调用时惰性完成
（锁已过期视同不存在，直接覆盖）。

它**不能跨节点**，重启即全部丢失，因此只适用于单机部署或测试，
ioc 中以 `lockApiByMemory` 备用，要替换默认锁服务时改
`service.js` 里 `lockApi` 的引用即可。

## 域模式与全局模式

`lock` 命令默认是**域模式**：你操作的锁名会自动加上
`<你的组名>:` 前缀，譬如锁名 `abc` 实际操作的是
`myDomain:abc`。这样不同域（用户组）各用各的锁，互不干扰。

加 `-G` 进入**全局模式**，锁名原样使用，全系统只有一把同名锁
（典型场景：全局只有一个的后台任务防重入）。

## 通过命令使用

子命令一览：

- `lock @try <锁名>`：尝试加锁，输出锁对象（含 privateKey）
- `lock @get <锁名...>`：查看锁信息（拿不到 privateKey）
- `lock @free <锁名> <privateKey>`：释放锁
- `lock @list`：列出全部锁
- `lock @info`：打印当前锁服务实现信息

## 示例

```bash
# 申请一把 30 秒的锁，防重入一个定时任务（域模式）
lock @try cron_sync_data -du 30 -hint "定时同步任务"

# 输出形如（privateKey 是释放锁的凭证，请妥善保管）
# {
#    name: "myDomain:cron_sync_data",
#    hold: "2026-10-29 22:00:00.000",
#    expi: "2026-10-29 22:00:30.000",
#    privateKey: "8q4qf5pycb",
#    owner: "demo",
#    hint: "定时同步任务"
# }

# 锁被别人占着？阻塞等待，最多重试 5 次，每次间隔 2 秒
lock @try cron_sync_data -du 30 -block -retry 5 -interval 2

# 加锁失败时输出一个自定义的失败对象，方便脚本判断
lock @try cron_sync_data -du 30 -fail '{name:"FAIL",hint:"busy"}'

# 看看这把锁被谁占着
lock @get cron_sync_data

# 看看全局所有的锁（全局模式）
lock -G @list

# 用完了释放（需要当时返回的 privateKey）
lock @free cron_sync_data 8q4qf5pycb
```

在 [jsc 执行的 jsbin 脚本](../101.dev/20-write-jsbin.md)里，`sys` 对象是
脚本上下文封装（`JvmJsExecContext`），并不直接暴露 Java 服务接口，
通过 `sys.exec2` 调用 `lock` 命令即可拿到锁服务对象：

```js
// 拿到锁对象（JSON），privateKey 留着释放用
var lock = sys.exec2map("lock @try job_x -du 30 -hint 'JS 任务'");
if (lock && lock.privateKey) {
    try {
        // ... 做需要互斥的事
    } finally {
        sys.exec2("lock @free job_x " + lock.privateKey);
    }
}
```

# 延伸阅读

- [lock 命令 man 文档](../../../src/com/site0/walnut/ext/sys/lock/man/zh_cn/cmd_lock.md)
- 锁服务接口：`src/com/site0/walnut/api/lock/WnLockApi.java`
- Redis 实现：`src/com/site0/walnut/impl/lock/redis/QuickRedisLockApi.java`
- 内存实现：`src/com/site0/walnut/impl/lock/memory/MemoryLockApi.java`
