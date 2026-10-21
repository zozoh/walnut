---
title  : Quota 总体设计
author : wendal
tags:
- 扩展
- 配额
---


# 什么是Quota(配额)

A quota is the limited number or quantity of something which is officially allowed

因为资源是有限的,所以需要限制

因为资源需要限制,所以付费可以得到更多资源

为了支撑上述逻辑,需要一个通用的配额管理机制.

### 名称

* quota 配额
* usage 已使用量

# 数据结构

## 目录结构

使用一个单独的/sys/quota存储配额数据

```
- sys
	- quota
		- wendal
		- hashi
		- feeder
		- ...
```

## 元数据

```
{
	quota_disk : 1073741824,    // 1Gb存储空间
	quota_network : 1073741824, // 1Gb流量
	quota_regapi : 10000,       // regapi调用次数
	...
	usage_network: 92274688,    // 已用流量88Mb,
	usage_regapi : 7863         // 已使用 regapi调用次数
}
```

## 元数据命名规则

* quota_$type 代表某种资源的配额数,Long类型, 具体单位由类型决定
* usage_$type 代表某种资源的已用量,Long类型, 具体单位由类型决定

## 瞬时用量和非瞬时用量

* 瞬时用量, 例如磁盘空间是不需要保存usage的,因为它总能根据系统内的数据重新计算
* 非瞬时用量,或者叫累加用量, 例如网络流量和regapi, 需要将他们持久化, 也就是需要usage_$type

## 周期性重置

例如网络流量,是按月计量的,需要定期重置usage