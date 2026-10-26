---
title: 文件系统映射
module: core.indexer.localfile
keywords: [文件系统映射, file, filew, 本地目录, 只读, 静态站点, 挂载]
source:
  - core/mapping/indexer/LocalFileIndexerFactory.java
  - core/mapping/indexer/LocalFileWIndexerFactory.java
  - core/mapping/bm/LocalFileBMFactory.java
  - core/indexer/localfile/
  - core/bm/localfile/
related: [10-io-mapping, 20-indexer-mongo, 30-bm-sha1]
---

# 文件系统映射

把服务器磁盘上的某个目录，挂成 Walnut 对象树里的一棵子树。
这是 `file` / `filew` 两个索引管理器的活——它们是**索引与内容合并映射**的简写：
元数据按目录树即时生成，内容就放在文件本来的位置，不复制、不搬迁。

## mnt 语法

```json5
{
    // ... 其他元数据
    "mnt": "file:///opt/mnt/project/demo"    // 只读
}
```

```json5
{
    // ... 其他元数据
    "mnt": "filew://D:/tmp/path/to/dir"      // 可写
}
```

- `file://` — **只读**镜像：在 Walnut 里浏览、搜索、读取磁盘目录（静态站点、资源目录）
- `filew://` — **可写**： Walnut 里的写入会真正落到磁盘文件上

注意路径是裸路径（`file:///opt/...` 三个斜杠是因为路径本身以 `/` 开头；
Windows 盘符写 `filew://D:/...`）。

## 它是怎么工作的

- **元数据即时生成**：磁盘上的每个文件/目录，访问时才变成 WnObj——
  Walnut 的全局索引里**没有**这些条目，不占 Mongo 空间
- **查询是列目录**：`match` 查询能力有限（本质是按目录列举过滤），
  别指望它像 Mongo 那样按任意字段检索
- **文件系统就是真相**：文件在磁盘上改名、删除，映射里跟着变

## 典型场景

```bash
# 把静态站点挂出来，配合 regapi 直接对外服务
mount ~/site 'file:///var/www/static/'

# 把项目的 DDL 目录挂进 Walnut 管理
mount ~/ddl 'filew:///mnt/project/bunya/init/ddl/'
```

## 注意事项

- 换映射不是迁移：摘掉 `mnt` 后，磁盘上的文件还在原地
- `file://` 下的写入操作会抛错（只读），别选错
- 合并简写也兼容老式 `file://C:/` 的写法（MountInfo 自动归一）

## 扩展阅读

- IO 映射 — 映射机制总览
- 对象存储映射 — 同样的合并简写思路，介质换成对象存储
