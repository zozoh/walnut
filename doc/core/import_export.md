---
title:数据导出导入
author:wendal
tags:
- 系统
- 运维
---

# 设计动机

厚朴项目的数据需要"备份"功能, 同理, strato, woodpeax服务器也需要

按wendal最初的想法,是打包bucket数据目录及mongodb导出dump文件,也是当前厚朴服务器的做法.

然而,这种"硬"备份有很多问题:

* 不支持增量备份,虽然bucket目录可以做增量,但mongodb的数据不能.
* 不支持局部备份,即单独备份walnut内的指定目录树
* 存在bucket数据与mongodb dump数据不匹配的可能性
* 不能工作在集群环境下

为了解决上述的问题, 需要设计一套基于WnIo接口的导入导出机制

# 导出

数据分成3部分, 

* objs.txt 目录树文件
* objs 元数据的文件夹
* bucket 裸数据的文件夹

objs.txt 目录树文件, 一行一条记录

```
// 格式
$id:$path:$obj_sha1:$data_sha1
// 示例
vvabc..dbad:/home/wendal/logo.png:df23er...dfdf:ss32..f34
```

元数据的文件夹和裸数据数据文件夹,均以sha1命名存储

```
objs
	- vv 
		- abcd...efg
bucket
	- vv
		- 3hb5m0t8ionrai3di3eh98s2
```

上述3个文件和文件夹夹,打包压缩之后,就是一个数据导出包(或者叫备份包)

### 增量备份

在第一次完整导出后(基线备份包/完整备份包), 后续导出使用增量方式(增量备份包)

与基线更新包的异同:

* objs.txt 是完整的
* objs 元数据文件夹,只包含新增/修改过的数据
* bucket 裸文件数据文件夹, 只包含新增/修改过的数据

### 增量备份的生成过程

以基线备份包及后续的N个增量备份包为蓝本, 生成当前系统的增量备份包

* 第一轮, 生成完整的objs.txt
* 第二轮, 根据当前的objs.txt与前一次备份的objs.txt,差分出objs目录
* 第三轮, 根据当前的objs.txt与前一次备份的objs.txt,差分出bucket目录

### 增量备份的合并执行过程

以基线备份包及后续的N个增量备份包为蓝本, 合成一个新的基线备份包(完整备份包)

因为objs.txt总是包含完整的目录树,所以仅需要递归查找该目录下的文件所对应的objs文件

* 第一轮, 递归查找objs下的原数据文件
* 第二轮, 根据objs.txt, 递归查找bucket下数据文件
* 第三轮, 将上述objs和bucket文件夹,连同objs.txt,压缩生成新的基线备份包

## 导入

导入的过程, 与"增量备份的合并执行过程"的类似,只是数据最终落地到walnut的目录树中

### id是否需要保持原本的id

若保持原id, 必然会出现id重复的可能性:

```
touch /home/wendal/logo.png
doExport /home/wendal # 输出备份
mv /home/wendal/logo.png /home/zozoh/logo.png
doImport dump_xxx.tgz /home/wendal
```

所以, 导入功能应具备下列配置项:

* 保留id与否
* 遇到id冲突时, 是自动创建新id还是报错
