---
title: 数据存储
author: zozoh
tags:
- 系统
- 数据存储
---

# 多样化的数据存储

1. 系统底层认为每个 *WnObj* 都可以有一个存储
2. 存储被认为是一个接口
3. 一个对象的数据应该可以被散列到不同的物理机上

# 存储句柄

```
id                # 句柄的唯一标识，一个 UUID32 字符串
ct                # 句柄创建的时间（绝对毫秒数）
lm                # 句柄最后一次被访问的时间（绝对毫秒数）
mode = R|W|RW     # 句柄打开类型

obj  -> {..}      # 对象的索引
off  = 0          # 打开针对对象原有数据何处进行操作
                  # 对于写操作，-1 表示在结尾追加

posr = 0          # 针对 swap 读操作的位置
posw = 0          # 针对 swap 写操作的位置

bucket            # 指向一个数据桶的 ID，读写操作都是针对这个桶的
                  # 如果关闭句柄，如果是写操作，自然会将这个桶的 ID 更新
                  # 到 obj.data 字段里
```

# 数据桶

```
id                # 桶的唯一标识，一个 UUID32 字符串
sealed            # 桶是否设成只读
sha1              # 桶内有效数据的 SHA1 指纹
len               # 桶的数据总长度

ct                # 桶被创建的时间（绝对毫秒数）
lm                # 桶被最后修改的时间
lread             # 桶被最后读取的时间
lsync             # 桶被最后被持久化的时间

refer_count       # 桶的引用计数
read_count        # 桶历史被读取的次数

block_size        # 桶的数据块大小
block_nb          # 桶的数据块数量
```

1. 数据桶存放尺寸大小一致的数据块
2. 当数据桶被盖上后，会生成一个 sha1 表示自身全部数据的指纹
3. 对象的数据桶如果只读，写入数据时，会新分配一个数据桶给它，旧的桶会被回收
4. `sealed && sha1` 的桶，会根据 *SHA1* 去掉重复
5. 可以根据 *SHA1* 来寻找一个数据桶

# 将桶存放在本地磁盘

```
$BUCKET_HOME
    23
        45cda3f..           # 某一个桶的目录
            bucket.json     # 桶的信息数据
            0               # 桶的第 0 个数据块
            1               # 桶的第 1 个数据块
            ...
```

* 系统启动只会检查 `$BUCKET_HOME` 是否存在
* 当调用者想读取一个桶信息，系统会首先检查内存缓存，如果有，就使用，否则就试图从磁盘加载
* 系统最多缓冲多少个桶信息，这个需要看配置项，默认不限
* 当调用修改桶的数据，桶的信息是否会立即存入磁盘，以及存入磁盘的时机由实现者掌握

# 桶接口

```
id()
isSealed()
len()
createTime()
lastModified()
lastReaded()
lastSync()
referCount()
readCount()
blockSize()
blockNumber()
#..........................................
sha1(gen)  : 获取桶的数据指纹，gen 表示如果没生成过，是否立即生成 sha1
#..........................................
# 读取桶的一部分数据
read(byte[] bs, int off, int len)
#..........................................
# 写入桶
write(byte[] bs, int off, int len)
#..........................................
# 封装桶，封装成功，返回桶数据的 SHA1
seal()    : SHA1
unseal()  : 
#..........................................
duplicate() : WnBucket     # 复制一个桶
refer() : refer_count      # 引用一个桶，返回引用后的计数
```

# 桶管理器

```
alloc(blockSize) : Bucket  # 分配一个新桶，blockSize 表示数据块的大小
free(buid)       : Bucket  # 释放一个桶，会减少桶的引用计数，如果为0，则释放桶
getById(buid)    : Bucket  # 获得一个桶
checkById(buid)  : Bucket  # 获得一个桶，不存在即抛错
getBySha1(sha1)  : Bucket  # 根据 sha1 获得一个桶
checkBySha1(sha1): Bucket  # 根据 sha1 获得一个桶，不存在即抛错
```











