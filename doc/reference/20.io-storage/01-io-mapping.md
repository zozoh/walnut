---
title: IO 映射
module: core.mapping
keywords: [mnt, 映射, IO映射, 索引管理器, 桶管理器, 全局映射, 两段式ID]
source:
  - core/mapping/support/MountInfo.java
  - core/mapping/WnIoMappingFactoryImpl.java
  - core/mapping/indexer/
  - core/mapping/bm/
  - conf/ioc/io.js
related: [README, dao-mapping, xo-storage, ../10.concepts/01-std-obj.md]
---

# 什么是 IO 映射

在 [一切皆对象](../10.concepts/01-std-obj.md) 里我们知道，一个对象 = 元数据 + 内容。
所谓 **IO 映射**，就是说明一个对象的这两部分信息**分别由谁来管理**。

管理元数据的，我们称为**索引管理器**（Indexer）：树结构、字段、查询、聚合，都归它管。
管理内容的，我们称为**桶管理器**（BM，Bucket Manager）：字节流的读写归它管。

一个对象总归是由一个索引管理器 + 一个桶管理器共同管理的。
目录对象没有内容，所以目录只有索引管理器，没有桶管理器。

# 全局映射

系统启动时有一个**全局索引管理器**和一个**全局桶管理器**。
不做任何声明的对象，元数据交给全局索引管理器，内容交给全局桶管理器——
这就是默认形态，很多对象一辈子都活在全局映射里，你完全不用关心它们的存在。

# 对象的自定义映射

但 Walnut 的数据要存到各种各样的介质上：SQL 表、Redis、对象存储、本地目录……
所以我给每个**目录对象**一个 `mnt` 元数据，它可以为自己的整棵子树指定另外一组
索引管理器和桶管理器。

举两个例子感受一下：

```bash
# 子树的元数据存进 SQL 表（订单、新闻这类海量结构化数据）
mount ~/biz/orders 'sql(:t_order:cmg)'

# 子树的内容按 SHA1 存进对象存储桶（视频、图片这类海量文件）
mount ~/videos '://vxds(s3:sha1:22:/home/demo#backup)'
```

声明之后一切照旧——`o`、`cat`、查询、授权，对使用者完全透明。
改主意了就 `umount` 把映射卸掉，子树回归全局映射。

有两个规则顺带知道就好：映射**不能嵌套**（子树里的对象不能再声明 `mnt`，
一个对象只属于一个映射）；读取对象内容时，系统从对象自身开始**向上逐级**
寻找桶映射设定。

# 现有映射方式

映射写在一个字符串里，语法是「索引管理器(配置) + 分隔符 + 桶管理器(配置)」：

```bash
sql(:attachment:cmg)::vxds(s3:sha1:22:/home/demo#attachment)
```

其中：

- `sql` — 索引管理器名称，`:attachment:cmg` 是它的配置信息
- `::` — 分隔符（也支持 `://`），分隔符及其右边整体可以省略（桶管理器用全局默认）
- `vxds` — 桶管理器名称，`s3:sha1:22:/home/demo#attachment` 是它的配置信息

具体每个管理器的配置信息格式，参见后续的专题文档，这里先给你一张全名单。

**索引管理器**（管元数据）：

- `mongo` — MongoDB
- `redis` — Redis
- `sql` — `RDS` 数据库
- `file` — 文件系统，只读
- `filew` — 文件系统，可写入
- `vofs` — 对象存储的虚拟文件系统

**桶管理器**（管内容）：

- `sha1` — 按照内容 `SHA1` 存储到本地
- `vxds` — 对象存储
- `sql` — `RDS` 数据库的 `blob` 字段
- `redis` — Redis
- `mem` — 内存
- `multi` — 条件映射（规划中）

# 特殊映射：文件系统

有一种映射你在语法名单里看不到 `file` 桶管理器——因为 `file` / `filew` 是
**索引与内容一起映射**的简写：元数据按目录树组织，内容就放在文件本来的位置。

```json5
{
    // ... 其他元数据
    "mnt": "filew://D:/tmp/path/to/dir"
}
```

声明之后，这个目录就是你磁盘上那个目录的影子，`cat`、`o` 直接读写文件。
`file://` 是只读版本，`filew://` 可写。

# 特殊映射：对象存储

`vofs` 也是同样的合并简写——元数据与内容都在对象存储桶里，
桶本身就是一棵「目录树」：

```json5
{
    // ... 其他元数据
    "mnt": "vofs://s3:/home/demo#backup"
}
```

对象存储的连接配置（`~/.io/{s3,cos}/*.json5`）由 `xo` 命令体系管理，
详见 [对象存储集成](xo-storage.md)。

# 两段式 ID

被映射的子树，其对象的 `ID` 会变成两段式 `$HomeID:$ReferID`：

- `HomeID` — 全局索引里映射根对象的 ID
- `ReferID` — 子索引器内部的 ID（譬如 vofs 映射里它是 base64 编码的对象存储路径）

这样全局索引只需记录映射根这一个对象，子树里再多对象也不占全局索引的条目，访问更快。

# 总结

读到这里，我想你已经基本对 `Walnut` 如何存储对象的策略有了基本了解。

你还想了解这些存储方式的细节，可以进一步阅读：

- [索引管理器 - MongoDB](20-indexer-mongo.md)
- [索引管理器 - Redis](21-indexer-redis.md)
- [索引管理器 - SQL](22-indexer-sql.md)
- [桶管理器 - sha1](30-bm-sha1.md)
- [桶管理器 - vxds](31-bm-vxds.md)
- [桶管理器 - sql](32-bm-sql.md)
- [桶管理器 - redis](33-bm-redis.md)
- [桶管理器 - mem](34-bm-mem.md)
- [桶管理器 - multi](35-bm-multi.md)
- [文件系统映射](40-indexer-file.md)
- [对象存储映射](41-indexer-vofs.md)
