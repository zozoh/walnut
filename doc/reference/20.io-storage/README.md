---
title: 数据存储与映射
module: core
keywords: [io, 存储, 映射, mnt, 索引器, 桶, WnIo, WnIoIndexer, WnIoBM, 过期, 对象存储]
source:
  - api/io/WnIo.java
  - api/io/WnObj.java
  - api/io/WnIoIndexer.java
  - core/WnIoBM.java
  - core/mapping/WnIoMappingFactoryImpl.java
related: [../10.concepts/01-std-obj.md, 01-io-model.md, 02-mount-mapping.md]
---

# 数据存储与映射

本目录讲清一件事：**数据怎么存、怎么映射到各种介质上**。
先看下面的核心模型，再按阅读路径深入各篇。

## 核心模型：IO / Indexer / BM

数据访问是任何系统的核心。Walnut 用四个接口把这件事讲清楚：

- **IO — 统一数据读写接口**（`interface WnIo`，`api/io/WnIo.java`）
  对外的唯一数据入口：**一切数据都是树形空间里的对象，一切读写查都走 WnIO**。
  使用者（命令、regapi、服务类）只面对它，不需要知道底层存储是什么。

- **对象 — 一条数据的基本单位**（`interface WnObj`，`api/io/WnObj.java`）
  文件、目录、会话、支付单……任何一条数据都是一个对象。一个对象有**两部分信息**：
  **元数据**（id、名称、权限、标签、时间等业务字段）和**内容**（字节流，即文件内容）。
  对象模型详见 [标准对象](../10.concepts/01-std-obj.md)。

- **Indexer — 对象元数据索引管理器**（`interface WnIoIndexer`，`api/io/WnIoIndexer.java`）
  管对象的**元数据**：树结构、字段、查询、聚合。

- **BM — 对象内容桶管理器**（`interface WnIoBM`，`core/WnIoBM.java`）
  管对象的**内容**：字节流的读写（BM = Bucket Manager，桶管理器）。

### 关键设计：元数据与内容分开保存

**一个对象的元数据与内容桶是分开保存的，可以存在不同的介质上**——
Indexer 和 BM 是两个独立的可插拔接口，按接口的实现可以**任意组合**：

```bash
# 元数据在 MongoDB，内容在本地文件 —— 最常见组合
Indexer = mongo    BM = file

# 元数据在 SQL 表（海量结构化数据可聚合），内容在 Redis
Indexer = sql      BM = redis

# 一个目录一个组合：子树整体切到另一组 Indexer/BM（mnt 映射）
mnt = "sql(demo:t_news)://sha1"
```

分开的理由：

- 元数据需要查询能力（按字段查、按标签查、聚合统计），内容是大块字节流——存储诉求完全不同
- 按需选型：小站点「Mongo + 本地文件」即可，大规模可以「SQL + Redis」
- 目录树通过 **mnt 映射**挂接：任何一个目录对象声明 `mnt` 元数据，它的子树就整体切到另一组 Indexer/BM 上

### 现有的实现

**Indexer（`core/mapping/indexer/`，ioc 键：mongo / file / filew / sql / vofs）**：

- `mongo` — MongoDB（flat 扁平集合模式）
- `sql` — SQL 数据库表（SQL 模板定义在 `~/.sqlx/*.sql`，数据源在 `~/.dao/*.dao.json`）
- `file` / `filew` — 本地文件（单文件 / 整树）
- `vofs` — 虚拟对象文件系统（对象即文件）

**BM（`core/mapping/bm/`，ioc 键：sha1 / redis / file / filew / sql / vofs / vxds）**：

- `file` / `filew` — 本地文件（普通 / 整树）
- `sha1` — 本地文件（按 sha1 去重）
- `redis` — Redis
- `sql` — SQL 大字段表
- `vofs` — 虚拟对象文件系统
- `vxds` — 对象存储内容签名存储（配合 [xo](04-xo-storage.md)）

### mnt 映射语法

映射写在目录对象的 `mnt` 元数据上，语法为 `索引方案(参数)://桶方案(参数)`（可省略桶部分）：

```bash
# 整个子树的元数据存 SQL 表（数据源 demo，实体模板 ~/.sqlx/t_news.sql），内容存全局 SHA1 桶
mnt = "sql(demo:t_news)"

# 元数据 Mongo，内容全局桶（最常见组合，等同不写 mnt）
mnt = "mongo"
```

> 完整方案清单（`mongo` / `sql` / `file` / `filew` / `vofs` / `sha1` / `redis` / `vxds` 等）
> 见 [mnt 映射语法全集](02-mount-mapping.md)。

相关命令：`mount` / `umount` 设置与卸载映射，`o` 命令可直接读写映射后的子树——
对使用者完全透明，不需要关心底层是 Mongo 还是 SQL。

```bash
# 查看对象的存储映射
o ~/data/news -cqn | grep mnt

# 给目录设置映射（把 ~/data/news 切到 SQL 表）
mount ~/data/news 'sql(demo:t_news)'

# 解除映射（子树回归父级映射）
umount ~/data/news
```

```bash
20.io-storage/
|-- README.md               # 本篇：核心模型 + 目录导航
|-- 01-io-model.md          # 索引器/桶/句柄模型：读写路径、SHA1 去重、交换区
|-- 02-mount-mapping.md     # mnt 映射语法全集：索引器 × 桶管理器 的任意组合
|-- 03-dao-mapping.md       # .dao 数据源与 SQL 表映射：元数据/内容进关系库
|-- 04-xo-storage.md        # 对象存储集成（xo）：s3/cos 配置与 vofs/vxds 挂载
|-- 05-expiration.md        # 过期对象机制：expi 自动删除
```

## 阅读路径

- **要懂读写流程** [01-io-model](01-io-model.md) — 全局默认组合（Mongo 索引 + SHA1 桶）、读写路径、去重与句柄规则
- **要用某种介质** [02-mount-mapping](02-mount-mapping.md) — 每种索引器/桶的 `mnt` 写法、组合示例、注意事项
- **要进关系库** [03-dao-mapping](03-dao-mapping.md) — 数据源、SQL 模板、内容进表
- **要上对象存储** [04-xo-storage](04-xo-storage.md) — 云桶配置，以及 vofs/vxds 挂载
- **辅助机制** [05-expiration](05-expiration.md)（到期自动删）与查询（`o` 命令的 `-match`/`@query`）在所有映射上通用

## 一页速查

```bash
# 默认：Mongo 索引 + 本地 SHA1 去重桶，不用任何配置
echo "hello" > ~/a.txt

# 目录=SQL 表（数据源 ~/.dao/demo.dao.json + 模板 ~/.sqlx/t_news.sql）
mount ~/biz/orders 'sql(demo:t_news)'

# 目录挂成本地静态站点（只读）
mount ~/static 'file:///var/www/static/'

# 内容进对象存储（配置 ~/.io/s3/backup.json5）
mount ~/videos 'vofs://s3:/home/demo#backup'

# 到期自动删除
o ~/tmp/session.json -u 'expi:%ms:now+1h'
```
