---
title: 索引器/桶/句柄模型
module: core
keywords: [索引器, 桶管理器, 句柄, WnIo, SHA1, 去重, 读写路径]
source:
  - core/io/WnIoImpl2.java
  - core/indexer/
  - core/bm/
  - core/hdl/
related: [../10.concepts/02-std-obj.md, mount-mapping, expiration]
---

# 索引器 / 桶 / 句柄模型

WnIo 把对象拆成三部分，各自独立扩展：

```bash
WnObj（元数据）── WnIoIndexer（索引器：元数据存哪、怎么查）
       │
       └── WnIoBM（桶管理器：内容存哪、怎么读写）
              │
              └── WnIoHandle（句柄：一次读写的上下文与锁）
```

**为什么要拆开**：索引和内容的生命周期、访问模式完全不同。索引要支持灵活查询（MongoDB 擅长），内容是大字节流（本地盘/OSS 便宜）；句柄则保证「一个文件同时只有一个写者」。

## 默认组合（conf/ioc/io.js）

```bash
全局索引器 globalIndexer : MongoIndexer        # MongoDB obj 集合
全局桶管理器 globalBM      : LocalSha1BM         # 本地目录，按 SHA1 去重
句柄管理器 ioHandleManager : RedisIoHandleManager # Redis，超时回收
                           (MemoryIoHandleManager # 单机替代)
```

## 读路径

```bash
io.fetch(id/path)
  → 缓存？(WnIoCachedWrapper)
  → 索引器取元数据
  → 元数据带 mnt？→ 映射工厂取子映射(WnIoMapping)，用两段式 ID 定位
  → open(obj, "r") → 句柄管理器发读句柄 → 桶管理器读字节
```

## 写路径与去重

```bash
open(obj, "w")  → 写句柄（独占！默认 1 分钟有效，每次写续期）
                → 写入交换区(swap)
flush/close     → 桶封盖：计算 SHA1
                → LocalSha1BM：buck/前4位/SHA1 存文件（内容寻址，天然去重）
                → referApi（Redis）记录 io:ref:$SHA1 = 引用本桶的对象 ID 列表
                → 更新对象元数据 sha1/len/lm/data
```

**去重是内容级的**：两个对象内容相同，桶里只有一份物理数据，靠引用计数（`WnReferApi`）管理生命周期。删除对象减引用，归零才真正删桶。

## 句柄规则

- 一个对象**可有多个读句柄，但只能有一个写句柄**。
- 写句柄带超时（`hdl-timeout`，默认 3600 秒），超时未操作自动失效。
- 句柄记录读写位置（pos），支持 seek。

## 交换区

写操作先落到 `swap/` 目录（文件名=写句柄 ID），`flush` 时才正式入桶。这保证：
- 写一半崩溃不留半文件；
- 小文件可在内存/临时区算完 SHA1 再决定物理落点。

## 本地桶布局（global-bm）

```bash
~/.walnut/io/
|-- buck/            # 桶目录：文件名就是内容 SHA1
|   |-- 0evq/89..g1  # 前4字符散列子目录 + 余下字符
|-- swap/            # 交换区：写句柄临时文件
```

## 实战：直接编程

```java
// Java（库模式）
WnObj o = io.create(null, "/home/demo/a.txt", WnRace.FILE);
String hid = io.open(o, Wn.S.AWR);        // 申请写句柄
io.write(hid, "hello".getBytes());
io.flush(hid);
WnObj n = io.close(hid);                  // 返回带 sha1/len 的新元数据
```

```bash
# 命令行（等价操作）
echo "hello" > ~/a.txt
o ~/a.txt -cqn    # 看 sha1/len
```

## 注意事项

- 桶一旦封盖只读；再写会复制出新桶（引用计数 1 时可原地打开）。
- 引用计数异常丢失时，可用 `refer` 命令族检查修复（见 [过滤命令](../40.commands/03_cmd_filter.md)）。
- 映射子树的细节见 [mount-mapping](mount-mapping.md)。
