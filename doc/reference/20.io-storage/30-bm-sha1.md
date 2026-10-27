---
title: 桶管理器 - sha1
module: core.bm.bml
keywords: [桶管理器, sha1, LocalSha1BM, 内容寻址, 去重, 引用计数, 全局桶]
source:
  - core/mapping/bm/LocalSha1BMFactory.java
  - core/bm/bml/LocalSha1BM.java
  - conf/ioc/io.js
related: [10-io-mapping, 20-indexer-mongo, 33-bm-redis]
---

# 桶管理器 - sha1 概述
`sha1` 是系统的**全局默认桶管理器**——不声明 `mnt` 的对象，内容都按 SHA1 存到本地磁盘。
这是内容寻址存储：**文件的内容（SHA1）就是它的物理文件名**，天然去重。

## mnt 语法

```bash
# 桶部分省略，即用全局默认 sha1 桶（最常见）
mnt : "mongo"

# 显式声明一个命名的 sha1 桶
mnt : "://sha1(名称)"
```

## 配置参数

`sha1` 没有配置串参数，但**命名的 sha1 桶需要预先注册**：
在 `conf/ioc/io.js` 的 `localSha1BMFactory.bms` 里放入 `LocalSha1BM` 实例，
`mnt` 里的名称就是 map 的键。未注册的名字会抛 `e.io.bm.UndefinedLocalBM`。

全局默认桶的位置由 `conf/web.properties` 决定：

```properties
global-bm-bucket=~/.walnut/io/buck   # 桶目录
global-bm-swap=~/.walnut/io/swap     # 写交换区
global-bm-autocreate=true            # 目录不存在时自动创建
```

## 桶里是什么样子

```bash
~/.walnut/io/
|-- buck/            # 文件名就是内容 SHA1
|   |-- 0evq/89..g1  # 前4字符散列子目录 + 余下字符
|-- swap/            # 交换区：写句柄临时文件，flush 时才正式入桶
```

写入先落到 `swap/`，封盖时算 SHA1、移入 `buck/`——写一半崩溃不留半文件。

## 去重与引用计数

两个对象内容相同，桶里只有一份物理数据。生命周期靠引用计数（`WnReferApi`）管理：
删除对象减引用，归零才真正删桶。
（引用计数异常时可用 `refer` 命令族检查修复，见 `man refer`。）

## 注意事项

- 桶一旦封盖只读；再写会复制出新桶
- 一个对象同时只有一个写者（写句柄独占，超时回收）
- 命名 sha1 桶是运维级配置，业务用户通常直接用全局默认桶即可

## 扩展阅读

- IO 映射 — 映射机制总览
- 索引管理器 - MongoDB — 最经典的搭配：mongo 索引 + sha1 桶
