---
title: 数据存储与映射
module: core
keywords: [io, 存储, 映射, mnt, 索引器, 桶, 过期, 查询, 对象存储]
related: [../10.concepts/02-io-indexer-bm.md, ../10.concepts/01-std-obj.md, 01-io-model.md]
---

# 数据存储与映射

本目录讲清一件事：**数据怎么存、怎么映射到各种介质上**。
概念层的「为什么」在 [10.concepts](../10.concepts/02-io-indexer-bm.md)，这里是机制与用法的全集。

```bash
20.io-storage/
|-- README.md          # 本篇：目录导航与阅读路径
|-- 01-io-model.md        # 索引器/桶/句柄模型：读写路径、SHA1 去重、交换区
|-- 02-mount-mapping.md   # mnt 映射语法全集：索引器 × 桶管理器 的任意组合
|-- 03-dao-mapping.md     # .dao 数据源与 SQL 表映射：元数据/内容进关系库
|-- 04-xo-storage.md      # 对象存储集成（xo）：s3/cos 配置与 vofs/vxds 挂载
|-- 05-expiration.md      # 过期对象机制：expi 自动删除
```

## 阅读路径

- **先读** [io-model](01-io-model.md) — 全局默认组合（Mongo 索引 + SHA1 桶）、读写路径、去重与句柄规则，
  这是所有映射的基础
- **要用某种介质** [mount-mapping](02-mount-mapping.md) — 每种索引器/桶的 `mnt` 写法、组合示例、注意事项
- **要进关系库** [dao-mapping](03-dao-mapping.md) — 数据源、SQL 模板、内容进表
- **要上对象存储** [xo-storage](04-xo-storage.md) — 云桶配置，以及 vofs/vxds 挂载
- **辅助机制** [expiration](05-expiration.md)（到期自动删）与查询（`o` 命令的 `-match`/`@query`）在所有映射上通用

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
