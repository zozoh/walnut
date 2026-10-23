---
title: mnt 映射语法全集
module: core.mapping
keywords: [mnt, 映射, sql, mongo, vofs, vxds, file, filew, redis, sha1, 桶管理器, 索引器]
source:
  - core/mapping/support/MountInfo.java
  - core/mapping/WnIoMappingFactoryImpl.java
  - core/mapping/indexer/
  - core/mapping/bm/
  - conf/ioc/io.js
related: [io-model, dao-mapping, xo-storage, ../10.concepts/02-io-indexer-bm.md]
---

# mnt 映射语法全集

任何 **目录** 声明 `mnt` 元数据，就把它的**子树委托**给指定的索引器 + 桶管理器组合。
这是 Walnut「一套 API 操作所有存储」的实现机制：元数据与内容分开保存，
各自可以指向不同的介质，按接口实现任意组合。

## 语法

```bash
mnt : "$IndexType[($Setting)][://$BMType[($Setting)]]"
# 兼容双冒号写法
mnt : "$IndexType[($Setting)]::$BMType[($Setting)]"

# 例子
sql(demo:t_news)://sha1
└─┬─┘└─┬─┘    └──┬─┘
索引器 设置      桶管理器(可选)
```

- 只写索引器：桶用全局默认（`globalBM`，本地 SHA1 去重桶）
- 只写桶（以 `://` 开头）：索引用全局默认（`globalIndexer`，MongoDB）
- 映射声明在 `DIR` 上才有意义——只有目录有子节点；在 `FILE` 上声明不影响子孙（它没有子孙）
- 读取对象内容时，系统从对象自身开始**向上逐级**寻找桶映射设定

两段式 ID `$HomeID:$ReferID` 定位映射根：`HomeID` 是全局索引里的映射根对象 ID，
`ReferID` 是子索引器内部的 ID（譬如 vofs 映射里它是 base64 编码的对象存储路径）。

## 索引器（`mnt` 冒号左边的部分）

 conf/ioc/io.js 的 `ioMappingFactory.indexers` 注册了这些类型：

- **`mongo`** — 写法：`mongo` — 元数据存于 MongoDB `obj` 集合 — 全局默认索引器；复杂查询、海量字段对象
- **`sql`** — 写法：`sql({daoName}:{entityName}:{options}:{filter})` — 元数据存于 SQL 表 — 海量结构化数据，模板与数据源定义见 [dao-mapping](dao-mapping.md)
- **`file`** — 写法：`file:///{绝对路径}` — 元数据由本地文件系统即时生成 — 只读目录镜像
- **`filew`** — 写法：`filew:///{绝对路径}` — 同上，可写版本
- **`vofs`** — 写法：`vofs://{s3|cos|oss|obs|kodo}:{域主目录}#{配置名}` — 元数据存于对象存储桶 — 海量非结构化内容（视频、图片库等）

> `mem` / `redis` / `mq` 在 ioc 配置中为 `null`（预留，暂无实现类）。

## 桶管理器（`://` 右边的部分，可省略）

 conf/ioc/io.js 的 `ioMappingFactory.bms` 注册了这些类型：

- **（省略）** — 内容存于 `globalBM`：本地 SHA1 去重桶，见 [io-model](io-model.md)
- **`sha1`** — 写法：`sha1` — 同全局桶，显式声明
- **`redis`** — 写法：`redis({配置名})` — 内容存于 Redis 字符串 — 小文件高频读写（如会话对象）；root 组成员可用 ioc 预定义配置，其他用户读 `~/.io/bm/{配置名}.json`（WedisConfig 格式）
- **`file` / `filew`** — 写法：`file:///{绝对路径}` — 内容按自然目录结构存放（如挂载静态站点目录）
- **`sql`** — 写法：`sql({daoName}:{entityName})` — 内容存于 SQL 表的 Blob 字段 — 与索引共用一张表，适合小文件，见 [dao-mapping](dao-mapping.md)
- **`vofs`** — 写法：`vofs({s3|cos|oss|obs|kodo}:{域主目录}#{配置名})` — 内容直接存对象存储
- **`vxds`** — 写法：`vxds({s3|cos|oss|obs|kodo}:sha1:{段数}:{域主目录}#{配置名}[@redis])` — 内容按内容签名（SHA1）存对象存储，带引用计数，见下文

### `file://` 的合并声明规则

`file` / `filew` / `vofs` 是**索引与内容一起映射**的简写——元数据按目录树组织，
内容桶也由同一介质管理：

```bash
# 只读挂载静态站点目录
mnt : "file:///var/www/static/"

# 读写挂载
mnt : "filew:///opt/mnt/project/demo"

# 对象存储文件系统（元数据与内容都在桶里）
mnt : "vofs://s3:/home/demo#test"
```

### `vxds`: 内容签名对象存储

`vxds(s3:sha1:22:/home/demo#test@redis)` 各段含义：

- `s3` — 对象存储类型：`s3 | cos | oss | obs | kodo`（当前实现：s3、cos）
- `sha1` — 签名算法：对象键取内容的 SHA1（去重，天然内容寻址）
- `22` — 键的前 `N` 个字符每两位一段作为路径层级，如 `ea890abcd...` → `ea/89/0abcd...`
- `/home/demo` — 配置所在域的主目录
- `test` — 对象存储连接配置名，存放在 `~/.io/{type}/{name}.json5`，见 [xo-storage](xo-storage.md)
- `@redis` — 【可选】引用计数服务，目前仅支持 `redis`，配置文件为 `{域主目录}/.redis/{配置名}.io.refer.json`

**引用计数安全**：内容按 SHA1 去重后，桶内物理文件靠引用计数管理生命周期（归零才删除）。
为了防止普通用户借引用计数搞乱全局 Redis，连接系统内置 Redis 时需在 web 配置中显式授权：

```properties
# 允许所有域使用系统内置 Redis
redis-host-allow-domain=*
# 或只允许指定域
redis-host-allow-domain=demo,admin
# 限允许连接的数据库（不限制则省略）
redis-database-allow=5,6
```

用户自己的 Redis 连接（与系统 `redis-host` 不一致）不受限制。

## 组合示例

```bash
# MongoDB 索引 + 全局桶（最常见，等于不写 mnt）
mnt : "mongo"

# SQL 表索引 + 全局桶
mnt : "sql(demo:t_news)"

# SQL 表索引 + Redis 桶
mnt : "sql(demo:t_news)://redis(sess)"

# 只换桶：索引用全局 Mongo，内容进本地目录
mnt : "://file:///var/www/static/"

# 会话对象：Redis 桶（索引仍走全局 Mongo）
mnt : "://redis(sess)"
```

## 定义自己的映射资源

```bash
# Redis 桶：定义连接配置
~/.io/bm/my_redis_bm.json        # WedisConfig: {host,port,password,database,setup:{prefix}}
mnt : "://redis(my_redis_bm)"

# SQL 索引器：数据源 + SQL 模板
~/.dao/demo.dao.json             # {url,username,password,maxActive,...}
~/.sqlx/t_news.sql               # insert/update_by/count/select/remove/inc 模板
mnt : "sql(demo:t_news)"
```

文件格式详见 [dao-mapping](dao-mapping.md) 与 [xo-storage](xo-storage.md)。

## 操作命令

```bash
mount ~/data 'sql(demo:t_news)'     # 设置映射
umount ~/data                        # 移除映射
o ~/data -cqn                      # 映射目录照常使用
mount -l                             # 列出（具体子命令见 man mount）
```

## 注意事项

- 映射子树的对象 **ID 是两段式**（`HomeID:ReferID`），`o` 命令可直接用。
- 换映射介质不是迁移数据——改 `mnt` 只影响新写入；老数据仍在旧桶里。
- `file://` 映射的目录**只读**；写入请用 `filew://`。
- 映射声明在目录元数据上，改映射 = 改元数据（`o -u 'mnt:"..."'`）。
- `vofs` / `vxds` 的对象存储配置（`~/.io/{type}/{name}.json5`）由 `xo` 命令体系管理，见 [xo-storage](xo-storage.md)。
