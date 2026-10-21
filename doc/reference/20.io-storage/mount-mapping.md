---
title: mnt 映射语法全集
module: core.mapping
keywords: [mnt, 映射, dao, mongo, redis, file, mem, mq, 桶管理器, lbm]
source:
  - core/mapping/
  - core/indexer/
  - core/bm/
related: [io-model, dao-mapping, metadata-conventions]
---

# mnt 映射语法全集

任何目录声明 `mnt` 元数据，就把它的**子树委托**给指定的索引器 + 桶管理器组合。这是 Walnut「一套 API 操作所有存储」的实现机制。

## 语法

```bash
mnt : "$IndexType[($Setting)][://$Storage]"
# 例子
dao(demo/t_news)://aliyunoss(news-data)
└─┬─┘└───┬───┘  └──┬──┘ └───┬─────┘
索引器  设置     分隔   桶管理器(可选)
```

两段式 ID `$HomeID:$ReferID` 定位映射根：`HomeID` 是全局索引里的映射根对象，`ReferID` 是子索引器内的 ID。

## 索引器（内容左边的部分）

| 索引器 | 写法 | 元数据存于 | 适用场景 |
|--------|------|-----------|----------|
| `mongo` | `mongo` / `mongo(name)` | MongoDB 集合 | 默认；复杂查询、海量字段对象 |
| `dao` | `dao(源/表名)` | SQL 表 | 海量结构化数据，见 [dao-mapping](dao-mapping.md) |
| `mem` | `mem` / `mem(key)` | 内存 | 用完即抛（请求对象等） |
| `file` | `file` | 本地文件系统即时生成 | 只读目录镜像（索引不可定制） |
| `redis` | `redis` / `redis(key)` | Redis | 仅按 ID 取、不支持复杂查询 |
| `mq` | `mq` / `mq(name)` | 消息队列 | 队列抽象操作 |

## 桶管理器（`://` 右边的部分，可选）

| 桶管理器 | 写法 | 内容存于 |
|----------|------|----------|
| 全局桶 | 省略 | `globalBM`（LocalSha1BM，本地去重） |
| 本地桶 | `lbm(名)` | 域目录 `~/buck`（域级 SHA1 桶） |
| 本地文件 | `file:///绝对路径/` | 自然目录结构（如挂载静态站点目录） |
| Redis | `redis(名)` | Redis 字符串（小文件高频读写，如会话） |
| 阿里云 OSS | `aliyunoss(配置名)` | OSS（配置在 `~/.aliyun/oss/`） |

## 组合示例

```bash
# MongoDB 索引 + 全局桶（最常见，等于不写 mnt）
mnt : "mongo"

# SQL 表索引 + 全局桶
mnt : "dao(demo/t_news)"

# SQL 表索引 + 域级桶
mnt : "dao(demo/t_news)://lbm(news-data)"

# 本地目录镜像（静态资源）
mnt : "file:///var/www/static/"

# Redis 小文件（会话对象）
mnt : "redis"

# 内存请求对象（regapi 请求加速）
mnt : "mem"

# 消息队列
mnt : "mq(notify)"
```

## 定义自己的映射资源

```bash
# Redis 桶：定义数据源
~/.io/bm/my_redis_bm.json     # {host,port,password,database,setup:{prefix}}
mnt : "redis(my_redis_bm)"

# SQL 索引器：数据源 + 表映射
~/.dao/demo.dao.json          # {url,username,password,...}
~/.io/ix/my_ix.json           # {dao:"demo",tableName:"t_x",fields:[...],indexes:[...]}
mnt : "dao(my_ix)"
```

文件格式详见 [dao-mapping](dao-mapping.md)。

## 操作命令

```bash
mount ~/data 'dao(demo/t_news)'     # 设置映射
umount ~/data                        # 移除映射
o ~/data -cqn                      # 映射目录照常使用
mount -l                             # 列出（具体子命令见 man mount）
```

## 注意事项

- 映射子树的对象 **ID 是两段式**（`HomeID:ReferID`），`obj` 命令可直接用。
- 换映射介质不是迁移数据——换 `mnt` 只影响新写入；老数据仍在旧桶里。
- `file://` 映射的目录**只读**（写入请用 `LocalFileWIndexer` 工厂，见 conf/ioc/io.js 的 `localFileWIndexerFactory`）。
- 映射声明在目录元数据上，改映射 = 改元数据（`o -u 'mnt:"..."'`）。
