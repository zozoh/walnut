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
SQL Table  | `dao`   | 海量数据
Memory     | `mem`   | 用完即抛，譬如请求对象
LocalFile  | `file`  | 本地文件读写
Redis      | `redis` | 非永久保存，可多实例协同
MsgQueue   | `mq`    | 为消息队列提供一个通用操作接口

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
# 桶管理器实现细节

## LocalIoBM:本地桶管理器

**本地数据结构**

```bash
path/to/home/
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

## RedisIoBM:Redis桶管理器

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