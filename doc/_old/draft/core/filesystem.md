---
title: 文件系统
author: wendal
tags:
- 系统
- 文件系统
---

本文件用于规范Walnut内的文件系统挂载
===================================

下面是一个Linux标准的/etc/fstab文件

```
# <file system> <mount point>   <type>  <options>       <dump>  <pass>
UUID=af414ad8-9936-46cd-b074-528854656fcd / ext4 errors=remount-ro 0 1
/dev/xvdb1 /opt ext4 errors=remount-ro 0 1
/root/swap    none    swap    sw    0   0
```

设想的Walnut版fstab

```
<被挂载的设备> <挂载点> <文件系统类型> <文件系统参数> <dump> <pass>
~/.walnut /	mongo	props=mongo.propertes 0 0
~/workspace/git/github/walnut/ROOT/etc /etc file op=rw 0 0
none /opt/topics	sql props=/db_topics.properties 0 0
none /data/cdn	qiniu props=/qiniu_cdn.properties 0 0
none /data/redis redis props=/redis.properties 0 0
```

### 被挂载的设备

none 虚挂载设备,不对应具体的本地文件夹
~/etc 本地目录挂载点

### 挂载点

/ 根挂载点,必须存在
/etc 子挂载点,由对应的Mounter实现

### 文件系统类型

mongo 索引存mongodb,数据存本地磁盘
file  索引即时生成,数据映射到本地目录
sql   将SQL数据库映射为索引
qiniu 将七牛云存储映射为一个存储目录
redis 将redis的一个hset映射为目录

### 文件系统参数

由具体文件系统实现,不做统一要求

### dump 和 pass

当前均为0,保留值

## 挂载过程

各种文件系统的实现,均实现WnMounter. 其中,挂载点信息及参数,在init方法传入