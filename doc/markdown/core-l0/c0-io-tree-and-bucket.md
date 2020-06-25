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

 Storage     | Position        | Scene
-------------|-----------------|-------
GlobalBM     | *nil*           | 系统默认的全局桶集(LocalBM)
LocalBM      | `lbm(MyBucket)` | 系统分配的一些本地桶集
LocalFileBM  | `C:\xxx\xx`     | 本地文件读写
AliyunOssBM  | `aliyunoss(xy)` | 阿里云OSS配置名

## 两段式存储

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

## LocalBM 的策略归总

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
# 映射

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

------------------------------------------
# 实现接口

```bash
#-------------------------------------------------------
#
#  顶级接口 & 主实现类
#
#-------------------------------------------------------
WnIo
|-- WnIoCachedImpl    # 带缓存的 Io 包裹类
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
|   |-- WnIoLocalFileIndexer
|   |-- WnIoAliyunOssBM
# 
# 对于 WnIoLBII，可以有升级的策略，即按照桶的尺寸，存放到不同的表里
# 根据一个配置项，可以将不同尺寸，划分区间，每个对应一个表名
# 这个在运维的时候，如果划分改动，需要执行迁移程序才可以
#
#-------------------------------------------------------
#
#  句柄（锁）管理器 & 实现类
#
#  句柄就是一种锁，这里的锁，有下面的信息：
#
#  - ID    : UUID    # 锁的唯一标识
#  - ct    : AMS     # 创建时间
#  - target: ID      # 锁的目标
#  - value : INT     # 锁的值，具体业务具体理解
#
#  > 一个文件可以有多个读句柄，但是只能有一个写句柄
#  > 写句柄默认有效期1分钟，每次写入都需要传入句柄，有效期也会被更新
#  > 超过有效期的写句柄，如果没人创建新的写句柄，还是可以用的
# 
#-------------------------------------------------------
|-- WnIoLockerManager
    |-- WnMemLockerManager
    |-- WnRedisLockerManager
```

------------------------------------------
# 关于三个主要的类

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
# 关于查询

通过分析 `WnQuery` 得到 `pid`，
如果找到对应的 `WnIoMapping`，就用其查询.

否则就全局查询。

也就是说，所有的 `WnIoMapping->WnIoIndexer` 需要实现查询接口