---
title: 数据存储模型
author: zozohtnt@gmail.com
tags:
- 概念
- 基础
- IO
---

------------------------------------------
# 设计思路与边界

首先，要将数据按照`Tree`的形式组织起来

```bash
%WnRoot%
|-- home/
|   |-- xiaobai/
|   |   |-- dir1/ --> Mount To SQL
|   |   |-- dir2/ --> Mount To LocalFileSystem
|   |   |-- dir3/ --> Mount To Memory
|   |   |-- dir4/ --> Mount To Redias
|   |       4578..aq12:78a2..8912  : {..}
|   |       4578..aq12:6tq2..8422  : {..}
|   |-- zozoh/
|-- sys/
```

用 `MongoDB` 从根组织起一颗大树，在某些节点，可以映射到其他的存储介质

------------------------------------------
# 索引的存储方式

 Index     | Type    | Scene
-----------|---------|-------
MongoDB    | *nil*   | 默认场景
SQL Table  | `dao`   | 海量数据 dao(name) 
Memory     | `mem`   | 用完即抛，譬如请求对象 mem(key) 
LocalFile  | `file`  | 本地文件读写
Redis      | `redis` | 非永久保存，可多实例协同 redis(key) 
MsgQueue   | `mq`    | 为消息队列提供一个通用操作接口 mq(key) 

------------------------------------------
# 内容的存储方式

 Storage     | Position           | Scene
-------------|--------------------|-------
GlobalBM     | *nil*              | 系统默认的全局桶集(LocalBM)
LocalBM      | `lbm(BuckName)`    | 系统分配的一些本地桶集
LocalFileBM  | `filew?:///xx/xxx` | 本地文件读写
RedisBM      | `redis(BuckName)`  | Redis桶（频繁读写小文件）`redis(_)` 为全局默认 
AliyunOssBM  | `aliyunoss(xy)`    | 阿里云OSS配置名

------------------------------------------
# 关于映射

任何一个目录对象，都可以声明特殊的映射，声明的方法是：

```bash
mnt : "$IndexType[($Setting)][://$Storage]"
```

> 下面是一些例子:

 Index | Storage       | Mapping
-------|---------------|-----------------------
`dao`  | `GlobalBM`    | `dao(abc/t_news)`               
`dao`  | `LocalBM`     | `dao(abc/t_news)://lbm(Abc)`
`dao`  | `AliyunOssBM` | `dao(abc/t_news)://aliyunoss`
`dao`  | `AliyunOssBM` | `dao(abc/t_news)://aliyunoss(news-data)`
`mem`  | `GlobalBM`    | `mem`                         
`mem`  | `LocalBM`     | `mem://lbm(Abc)`               
`file` | `LocalFileBM` | `file://C:/data/demo/`
`redis`| `GlobalBM`    | `redia`
`redis`| `GlobalBM`    | `redia(tmp-files)`
`redis`| `LocalBM`     | `redia(tmp-files)://lbm(Tmp)`
`mq`   | `GlobalBM`    | `mq`
`mq`   | `GlobalBM`    | `mq(messages)`
`mq`   | `LocalBM`     | `mq(notify)://lbm(QueueData)`
`mq`   | `AliyunOssBM` | `mq(notify)://aliyunoss`

对于映射对象，它的 ID 结构必须是两段式的：

```bash
# 用 : 分隔，前面是映射根目录ID，在对应映射管理器中的ID
$HomeID:$ReferID
#-----------------------------------------
# 示例
#-----------------------------------------
# 文件映射
#  'tdprqrhenege9p11mu9rh9a07n' 表示全局索引里的一个对象
#  'path/to/file.txt' 表示在这个对象对应的索引管理器内的文件
{
  id   : "tdprqrhenege9p11mu9rh9a07n:path/to/file.txt",
  mnt  : "file:///data/somedir/",
  data : "path/to/file.txt"
  # 对于 LocalFileBM 并不关心 data 段，这个data段仅仅是作者强迫症式的对称
}
# 本地通映射
#  'tdprqrhenege9p11mu9rh9a07n' 表示全局索引里的一个对象
#  '1f76d9055mjaip6167umc2g1jt' 表示在桶管理器的数据段ID
{
  id   : "tdprqrhenege9p11mu9rh9a07n",
  mnt  : ":lbm(xyz)",
  data : "1f76d9055mjaip6167umc2g1jt"
}
```

------------------------------------------
# 索引管理器实现细节

## DaoIndexer:数据库索引管理器

```bash
~/
#-----------------------------------------
# 数据源定义
|-- .dao/
|   |-- [$DaoName].dao.json    # 每个定义就是一个数据源
#-----------------------------------------
# 索引管理器数据映射定义
|-- .domain/
    |-- entity/
        |-- [$Key].json    # 每个索引管理器的映射细节
```
> 数据源定义文件的内容格式请参看  [通用实体][c1-gde] **SQL数据源格式** 一节

索引管理器实体映射文件主要是将一个数据表的 ResultSet 对象与 `WnIoObj` 对象的字段
映射。其中 `WnIoObj` 对象，你可以认为就是一个普通的 `Map` 对象。

```js
{
  //------------------------------------------------
  // 数据源名称
  "dao": "default",    // 默认用 default
  // 这个实体是由数据源哪张表保存的
  // 如果是分表，可以用形为 t_abc_${id} 的动态表名来表示
  // 其中，占位符 ${id} 为实体本身的字段名(Java)
  "tableName": "t_abc",
  //------------------------------------------------
  // 这个实体映射为 WnIoObj 映射
  //------------------------------------------------
  // 这个映射是否可以自动创建
  // 默认为 true，如果 false，则如果不存在就抛错
  "autoCreate" : true,
  //------------------------------------------------
  //
  // 下面是字段映射
  //
  //------------------------------------------------
  // 所有的字段
  // 所有的内置字段，可以不声明，会使用默认的内置字段
  // 这些内置字段包括:
  // ```
  // 构成对象树的关键
  //  - id | pid | nm
  // 权限
  //  - c | m | g | md
  // 内容相关
  // - race | ln | tp | mime | sha1 | mnt 
  // - len  | d0 | d1 | lbls
  // 时间戳
  // - ct | lm | st | expi
  // ```
  "fields": [{
    // 本字段存放在 WnIoObj 中的名字
    "name": "id",
    // 本字段的类型
    // 默认为 String
    // 可选值为 String|Integer|Long|Float|Double|Boolean|Object|SArray|List|JSON
    // 其中 Object|SArray|JSON 在数据表中实际存储的是 VARCHAR
    //   - Object 应对到 Java 就是 NutMap
    //   - SArray 应对到 Java 就是 String[]
    //   - List   应对到 Java 就是 List
    //   - JSON   应对到 Java 就是 Object，主要看 JSON 解析出来的是什么
    // 读取回来的时候，会解析 JSON
    // 这个字段在生成 SQL 的时候，也用来决定是否要用引号包裹值等形式
    "type": "String",
    // 【选】 本字段存放在数据表中的名字，如果不声明，则与 name 相同
    "columnName": "id",
    // 【选】 生成 SQL 的时候，是否要用引号包裹名称
    // 默认的，会看如果与数据库关键字重名，则用引号包裹
    "wrapName" : false,
    //
    // 下面是关于数据字段更多的设置
    // 主要是自动建表时使用
    // 
    // 字段类型（必须大写）默认 AUTO
    // 支持 CHAR|BOOLEAN|VARCHAR|TEXT|INT|FLOAT|TIMESTAMP ...
    // @see org.nutz.dao.entity.annotation.ColType
    "columnType" : "AUTO",
    // 这里面可以声明更特殊的数据库类型，以便自动建表时使用
    // 如果声明了这个属性，在自动建表时，会覆盖 columnType 的设定
    "customDbType": null,
    // 默认值，支持 ${key} 占位符，其中 key 为更新或者插入的对象
    "defaultValue": "xxxx",
    // 本字段为版本字段，在乐观锁场景下用来恢复数据
    "isVersion" : false,
    "readonly"  : false,    // 只读字段
    "notNull"   : false,    // 非空字段
    "unsigned"  : false,    // 无符号字段
    // 自增字段：插入时，会自动忽略它
    "autoIncreasement" : false,
    // 字段大小写敏感，默认 true
    "casesensitive" : true,
    // 描述当前字段是否可插入
    "insert": true,
    // 描述当前字段是否可更新
    "update": true,
  }],
  //------------------------------------------------
  // 主键，通常就是 ["id"]，名称为 (field.name)
  "pks": ["id"],
  //------------------------------------------------
  // 声明一些关键的字段
  // 这些字段是一个　WnObj 所必须的
  // 键为： WnObj 的 key
  // 值为： 之前定义的 field.name
  // 如果没有声明，则键值同名
  // 在创建或者更新的时候，会把标准 WnObj 字段名（键）
  // 翻译成对应的映射字段名（值：field.name）
  "objKeys": {
    // 构成对象树的关键
    "id"  : "id",
    "pid" : "pid",
    "nm"  : "nm",
    // 权限
    "c"   : "c",
    "m"   : "m",
    "g"   : "g",
    "md"  : "md",
    // 内容相关
    "race": "race",
    "ln"  : "ln",
    "tp"  : "tp",
    "mime": "mime",
    "sha1": "sha1",
    "mnt" : "mnt",
    "len" : "len",
    "d0"  : "d0",
    "d1"  : "d1",
    "lbls": "lbls",
    // 时间戳
    "ct"  : "ct",
    "lm"  : "lm",
    "st"  : "st",
    "expi": "expi"
  },
  ///------------------------------------------------
  // 所有的索引
  // 会在第一次创建表的时候自动创建
  "indexes": [{
    "unique" : false,    // 唯一性索引
    "name"   : "xxx",    // 索引名称
    "fields" : ["pid","nm"]  // 索引关联字段名（field.name/Java）
  }]
}
```

------------------------------------------
# 桶管理器实现细节

## LocalIoBM:本地桶管理器

**本地数据结构**

```bash
~/
#-----------------------------------------
# 桶文件
|-- buck/           # 桶目录，桶ID就文件内容的 SHA1
|   |-- 0evq/       # 采用首4字符散列目录
|       |-- 89..g1  # 后面 28字符（可能更多）作为文件
#-----------------------------------------
# 交换文件
|-- swap/
    |-- 4tu..8q1    # 交换文件，文件名就是写句柄ID
#-----------------------------------------
# 索引管理
# Redis(SET)
io:ref:$SHA1 = ID1, ID2 ...
```

- 索引管理器由 Redis 维持以便提高效率
- 如果索引记录异常消失（访问一个对象，有SHA1，但是索引管理器没有这个键）可以通过MongoDB 的索引即时恢复

**实例结构**

```bash
<AbstractIoBM>
# 句柄管理器，与 WnIoImpl2 共享，
# 实际上所有的桶管理器都应该与 WnIoImpl2 共享这个管理器
# 因此，这个成员应该放到抽象类里
|--> handles<WnIoHandleManager>
|
|--:<LocalIoBM>
     |-- dBucket<File>      # 本地桶目录
     |-- dSwap<File>        # 本地交互区目录
     |   # 本地桶管理器为了节约空间，将根据桶的 SHA1 和引用计数
     |   # 归纳重复的桶引用，在 flush 的时候会进行 SHA1 计算
     |   # 具体逻辑下文由描述
     |-- refers<WnReferApi>
```

## LocalFileBM:本地文件桶管理器

**本地数据结构**

```bash
path/to/home/
#-----------------------------------------
# 桶文件就是下面的自然目录结构
# 读写直接针对相应的文件
|-- aaa/
|   |-- bbb/
|       |-- ccc.jpg
```

- 无交换区
- 读写直接针对文件进行

**实例结构**

```bash
<AbstractIoBM>
|--> handles<WnIoHandleManager>
|
|--:<LocalIoBM>
     |-- dHome<File>      # 本地文件桶主目录
```

## RedisBM:Redis桶管理器

> 为了能应对 Session 等小文本文件的频繁读写

**本地数据结构**

```bash
# Redis(STRING)
io:bm:$ID = 0d 0a 12 39 a8 ...
```

- 无交换区，打开句柄，会直接读取对应的字符串字节数组
- 不考虑 SHA1 重复等问题，为每个对象都开辟一个存储空间
- 同时将 SHA1 字段固定设置为 "pending"
- 即，桶ID 就是对象ID
- 因此，本桶管理器管理的对象，一律不生成 sha1

------------------------------------------
# 映射管理

一个对象的映射由两个内容构成：

1. 桶管理器
2. 索引管理器

`映射管理工厂`有一个`全局索引管理器`和`全局桶管理器`

**两段式ID**

```
$HomeID:$MyID
```

> 用来快速定位一个对象的映射根，以及自己的ID

**获取映射**

通过`全局索引管理器`和`两段式ID`，我们可以很容易定位一个对象。
但是这里有一个问题，譬如一个对象:

```json
{
  "id": "56yu..i891",
  "nm": "abcde",
  "mnt": "dao(xyz)://lbm(red)"
}
```

通过`根索引管理器`,我们得到了这个对象，但是我们到底是想得到下面那种映射呢？

1. 根映射
2. 子映射 `dao/lbm` 

如果我们想操作本体，自然需要得到根映射，如果我们想操作其内对象，则需要子映射。


------------------------------------------
# (草稿)两段式存储

采用 `LocalBM` 存储，有一个问题，就是需要计算一下 SHA1，以便去重。
如果这个本地路径映射到了远端(譬如 AliyunOSS)， 那么相当于写入到远端
然后计算 SHA1，然后发现重复，然后删掉。

这个过程有点浪费，需要考虑一下去重的策略。譬如，先写到本地文件，
看看 SHA1，如果没有，再上传。

但是多数文件不重，这又有点脱裤子放屁。

可以又下面的优化思路:

- 小文件(`<4M`): 先写到内存里，看看 SHA1，然后再决定是否真正写入
- 大文件(`<=100M`): 写到本地硬盘，然后看 SHA1，然后决定是否真正写入
- 超大文件(`>100M`): 直接写吧，没办法

**读取兼容旧版**

如果 `LocalBM` 发现一个给入的数据（无论读写）在自己的库里没有，
但是在旧版目录里有，应该将其合并，存入新版目录。

同时应该有一个扫描程序，可以全局检查一下，并转移到新版目录。
如果已经存在新版目录，则将旧版目录里的内容删除。

**或者可以按照SHA1存储**

或者桶写的时候，先写道临时区，然后计算SHA1 然后按存储 ...

## (草稿)LocalBM 的策略归总

```bash
>>> 输入文件流
#
# 小文件
#
# 它的 data 段应该是固定值 "SHA1"
#
#
|-- 写入内存
|   |-- 写入后计算 SHA1
|       |-- 按 SHA1 存储
|       |-- 分配一个UUID(data)存储
#
# 中型文件
#
# 它的 data 段应该是固定值 "SHA1"
#
#
|-- 写入临时磁盘
|   |-- 写入后计算 SHA1
|       |-- 按 SHA1 存储
|       |-- 分配一个UUID(data)存储
#
# 超大文件
#
# 它的 data 段与 sha1 不一致
#
|-- 直接写入目的地
    |-- 分配一个UUID(data)存储
        |-- 写入流
            |-- 写入后计算 SHA1
            |-- 如果重复
                |-- 删除
                |-- 依旧保留
```

------------------------------------------
# (草稿)实现接口

```bash
#-------------------------------------------------------
#
#  顶级接口 & 主实现类
#
#-------------------------------------------------------
WnIo
|-- WnIoHookedWrapper    # 带钩子的 Io 包裹类
|-- WnIoCachedWrapper    # 带缓存的 Io 包裹类
|      |--> WnIoCacheService  # 缓存服务接口
|      |    |-- WnIoObjCacheService   # 根据 ID/Path 缓存
|      |    |-- WnIoHieCacheService   # 根据 ID 缓存子 ID 列表
|      |                              # 只要有一个 ID 查不到，整个缓存无效
|      V                              
|-- WnIoImpl2         # Io 实现
|   |-- WnIoMappingFactory   # 映射工厂接口负责分析映射信息
#-------------------------------------------------------
#
#  映射管理接口
# 
#-------------------------------------------------------
|-- WnIoMappingFactory
|   |-- WnIoMappingFactoryImpl  # 一个默认实现
|
|-- WnIoMapping        # 封装映射媒体的全部行为
|   |--> WnIoIndexer   # 封装索引部分行为
|   |--> WnIoBM        # 封装桶管理器行为
#-------------------------------------------------------
#
#  索引管理器 & 实现类
# 
#-------------------------------------------------------
|-- WnIoIndexer
|   |-- WnIoDaoIndexer        # 元数据定制需要改表
|   |-- WnIoMemIndexer        # 傻查询，无持久化
|   |-- WnIoLocalFileIndexer  # 傻查询，元数据不可定制
|   |-- WnIoRedisIndexer      # 不支持查询，仅支持按 ID 获取
|   |-- WnIoMQIndexer         # 还不知道 ...
#-------------------------------------------------------
#
#  桶管理器 & 实现类
# 
#-------------------------------------------------------
|-- WnIoBM -> WnIoBucket
|   |-- WnIoLocalBM
|   |   |--> WnIoLBII                 # 对所有的桶信息进行索引的接口
|   |   |    |-- WnIoDaoSimpleLBII    # 存放在一张 SQL 表里
|   |-- WnIoLocalFileBM
|   |   |-- WnIoLocalFileWritableBM
|   |-- WnIoAliyunOssBM
# 
# 对于 WnIoLBII，可以有升级的策略，即按照桶的尺寸，存放到不同的表里
# 根据一个配置项，可以将不同尺寸，划分区间，每个对应一个表名
# 这个在运维的时候，如果划分改动，需要执行迁移程序才可以
#
#-------------------------------------------------------
#
#  锁管理器 & 实现类
#
#  句柄就是一种锁，这里的锁，有下面的信息：
#
#  - ID    : UUID    # 锁的唯一标识
#  - ct    : AMS     # 创建时间
#  - target: ID      # 锁的目标
#  - value : INT     # 锁的值，具体实现类有不同的立即，譬如乐观悲观锁
# 
#-------------------------------------------------------
|-- WnIoLockerManager
    |-- WnMemLockerManager
    |-- WnRedisLockerManager
#-------------------------------------------------------
#
#  句柄管理器 & 实现类
#
#  句柄，有下面的信息：
#
#  - ID    : UUID    # 句柄的唯一标识
#  - ct    : AMS     # 创建时间
#  - target: ID      # 句柄的目标对象ID
#  - value : INT     # 句柄的值，记录了读写位置
#                    # seek/getPosition                     
#
#  > 一个文件可以有多个读句柄，但是只能有一个写句柄
#  > 写句柄默认有效期1分钟，每次写入都需要传入句柄，有效期也会被更新
#  > 超过有效期的写句柄，如果没人创建新的写句柄，还是可以用的
# 
#-------------------------------------------------------
|-- WnIoHandleManager
    |-- WnMemIoHandleManager
    |-- WnRedisIoHandleManager
```

------------------------------------------
# (草稿)关于三个主要的类

```bash
WnIoMapping
|-- WnIoIndexer -> WnObj
|   |-- 元数据创建删除
|   |-- 元数据树形结构操作
|   |-- 元数据树查询
|-- WnIoBM <-- WnObj + Wn.S.AWRM
|   |-- open   -> 句柄
|   |-- remove <- 句柄
|   |-- write  <- 句柄
|   |-- read   <- 句柄
|   |-- seek   <- 句柄
|   |-- close  <- 句柄
```

------------------------------------------
# (草稿)关于查询

通过分析 `WnQuery` 得到 `pid`，
如果找到对应的 `WnIoMapping`，就用其查询.

否则就全局查询。

也就是说，所有的 `WnIoMapping->WnIoIndexer` 需要实现查询接口


[c1-gde]: core-l1/c1-general-data-entity.md