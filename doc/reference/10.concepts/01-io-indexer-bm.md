---
title: IO / Indexer / BM
module: api.io
keywords: [WnIo, WnIoIndexer, WnIoBM, Indexer, BM, 索引器, 桶管理器, mnt, 映射]
source:
  - api/io/WnIo.java
  - api/io/WnIoIndexer.java
  - core/WnIoBM.java
  - core/mapping/WnIoMappingFactoryImpl.java
related: [std-obj, io-model, mount-mapping, query-agg, 3x-data-source]
---

# IO / Indexer / BM

WnIO 是 Walnut 的统一数据接口：**一切数据都是树形空间里的对象，一切读写查都走 WnIO**。
它背后由两个可插拔的实现接口组成：

- **Indexer（索引器，接口 `WnIoIndexer`）**：管**元数据**——对象的树结构、字段、查询、聚合
- **BM（Bucket Manager，接口 `WnIoBM`）**：管**内容**——对象的字节流（文件内容）

两者正交：元数据放哪、内容放哪，可以独立选择、自由组合。

## 为什么分成两层

- 元数据需要查询能力（按字段查、按标签查、聚合统计），内容是大块字节流——两者的存储诉求完全不同
- 分开之后可以按需选型：小站点「Mongo + 本地文件」即可，大规模可以「SQL + Redis」
- 目录树通过 **mnt 映射**挂接：任何一个目录对象声明 `mnt` 元数据，它的子树就整体切到另一组 Indexer/BM 上

## 现有的实现

**Indexer（`core/mapping/indexer/`，ioc 键：mongo / file / filew / sql / vofs）**：

- `mongo` — MongoDB（flat 扁平集合模式）
- `sql` — SQL 数据库表（表映射定义在 `~/.io/ix/*.json`）
- `file` / `filew` — 本地文件（单文件 / 整树）
- `vofs` — 虚拟对象文件系统（对象即文件）

**BM（`core/mapping/bm/`，ioc 键：sha1 / redis / file / filew / sql / vofs / vxds）**：

- `file` / `filew` — 本地文件（普通 / 整树）
- `sha1` — 本地文件（按 sha1 去重）
- `redis` — Redis
- `sql` — SQL 大字段表
- `vofs` — 虚拟对象文件系统
- `vxds` — XO 数据集签名内容（配合 [xo](../40.commands/03_cmd_filter.md)）

## mnt 映射语法

映射写在目录对象的 `mnt` 元数据上，语法为 `索引方案(参数)://桶方案(参数)`（可省略桶部分）：

```bash
# 整个子树的元数据存 SQL 表 t_news（数据源 demo），内容存域级 SHA1 桶 news-data
mnt = "dao(demo/t_news)://lbm(news-data)"

# 元数据 Mongo，内容全局桶（最常见组合，等同不写 mnt）
mnt = "mongo"

# 系统启动时的默认根映射，由 web.properties 的 io-mounts 配置
```

> 完整方案清单（`dao` / `mongo` / `redis` / `file` / `mem` / `mq` / `lbm` / `aliyunoss` 等）
> 见 [mnt 映射语法全集](../20.io-storage/mount-mapping.md)。

相关命令：`mount` / `umount` 设置与卸载映射，`o` 命令可直接读写映射后的子树——
对使用者完全透明，不需要关心底层是 Mongo 还是 SQL。

## 常用操作示例

```bash
# 查看对象的存储映射
o ~/data/news -cqn | grep mnt

# 给目录设置映射（把 ~/data/news 切到 SQL 表）
mount ~/data/news 'dao(demo/t_news)://lbm(A)'

# 解除映射（子树回归父级映射）
umount ~/data/news
```

## 深入阅读

- [IO 模型](../20.io-storage/io-model.md)：WnIO 的完整读写流程、缓存与句柄
- [挂载与映射](../20.io-storage/mount-mapping.md)：mnt 语法全集、~/.io 配置
- [查询与聚合](../20.io-storage/query-agg.md)：Indexer 的查询能力
- [3X 数据源](06-3x-data-source.md)：更多外部数据如何接进来
