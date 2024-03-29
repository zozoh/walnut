---
title: 对象过期机制
author: zozohtnt@gmail.com
key: c0-expi
---

--------------------------------------
# 动机：为什么要有对象过期机制

某些时候，我们需要一些延迟删除的对象，或者虽然持久存储，但是希望
有一定时限的对象。譬如会话对象，或者订单对象等。

如果在每个业务里，都需要启动一个后台线程，定期清理，
会对业务逻辑的维护者造成不小的心智负担。

我们希望，在创建一个对象时，如果你指定了这个对象的有效期，然后就不用管了，
系统到期会自动删除这些对象。

--------------------------------------
# 计划应用场景

- 会话对象
- 商品订单（未支付到期自动删除）
- HTTP请求对象，等临时对象

--------------------------------------
# 设计思路与边界

首先，我们为对象建立一个标准字段 `expi` 值为一个绝对毫秒数。
表示这个对象的有效期至哪个绝对毫秒。

在 `Io` 层面每当建立这个对象，或者修改一个对象的元数据，如果发现了过期时间设定，
则会将这个对象的 ID 以及过期时间记录在一个表里，默认的，我们认为 Mongo 的某个集合
是个不错的地方，尤其是 Mongo 支持 `upset` 这个特性，尤其适合更新或者插入二选一的操作。

在 `Setup` 时，启动系统后台线程定期扫描这个表。
当然，这个扫描逻辑需要一些复杂一点的逻辑，以便应对多个节点竞争问题

--------------------------------------
# 数据结构描述

记录的过期时间逻辑表结构：

```bash
id   : ID      # 对象 ID（主键去重）
expi : AMS     # 对象过期时间
hold : AMS     # 被某清理线程占用的过期时间，超过这个时间，其他清理线程才可操作对象
ow   : "xxx"   # 被哪个清理线程占用
```

--------------------------------------
# 一些细节

首先，一个 Walnut 节点只会启动一个清理线程。
当然考虑到多个节点竞争的问题，清理线程启动时会申请一个锁服务，
并且只会取得一个固定数量上限的过期对象（数量可配置）

它会在记录里标识一下自己的信息，以及自己预期的操作时间（通常为30s）
在这段期间内这个记录就不会被其他节点关注了

当清理完成后，清理线程会清理记录

--------------------------------------
# 相关知识点

- [数据存储模型][c0-tab]
- [通用锁服务][c0-lock]

[c0-iob]: ../core-l0/c0-io-tree-and-bucket.md
[c0-lock]: ../core-l0/c0-lock-service.md