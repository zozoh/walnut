---
title: 索引管理器 - MongoDB
module: core.indexer.mongo
keywords: [索引管理器, mongo, MongoDB, mongoFlat, 集合, 默认索引器]
source:
  - core/mapping/indexer/MongoFlatIndexerFactory.java
  - core/mapping/indexer/MongoIndexerFactory.java
  - core/indexer/mongo/
related: [10-io-mapping, 20-indexer-mongo, 30-bm-sha1]
---

# 索引管理器 - MongoDB 概述
`mongo` 是系统的**全局默认索引管理器**——不声明 `mnt` 的对象，元数据都进 MongoDB。

## mnt 语法

```bash
mnt : "mongo"
mnt : "mongo(集合名)"
```

- 不带参数：使用全局默认集合（`obj`）
- 带参数：元数据存入指定的集合，譬如 `mongo(app_log)` 把子树的元数据存进 `app_log` 集合

## 配置参数

`mongo` 本身没有复杂的配置串，它只有「集合名」这一个参数。
但有两点值得知道：

- **集合模式**：ioc 默认注册的是 `MongoFlatIndexerFactory`（扁平集合模式），
  整棵子树的元数据平铺在一个集合里，靠 `pid` 组织树结构
- **命名实例**：如果你需要完全不同的 MongoDB 连接（另一个库、另一套账号），
  可以在 `conf/ioc/io.js` 里用 `MongoIndexerFactory` 预注册命名的索引器实例，
  然后 `mongo(实例名)` 引用它——这和全局默认索引器用的是同一个 `mongoDB` bean 不同

MongoDB 的连接信息不在 `mnt` 里，而在 `conf/ioc/mongo.js`（Ioc  Bean `mongoDB`），
系统级配置，一个 Walnut 实例一组。

## 适合什么场景

- 默认选择：复杂查询（按字段、区间、正则、标签）、海量字段的对象，MongoDB 都扛得住
- 对象的任意自定义元数据直接进 Mongo 文档，不用建表——这是 Walnut 快的原因

## 注意事项

- `mongo(集合名)` 的子树，查询照常走 `WnQuery`，聚合走 `agg`，语义与全局集合完全一致
- 换集合不是迁移数据：改 `mnt` 只影响新写入，老元数据还在原集合里

## 扩展阅读

- IO 映射 — 映射机制总览
- 桶管理器 - sha1 — 全局默认桶，最常与 mongo 搭配
