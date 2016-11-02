---
title: wup 更新服务
author:wendal
tags:
- 系统
- 通信
---

## 背景

puppet固然是长远之计,但复杂性及资源消耗,大大超过了近期的需要.

随着服务器的增长,我们需要一个更简单的,能满足近期需要的更新系统.

## 整体架构

使用 C/S 架构, 服务器与节点之间使用http协议进行通信

* Server端建立在walnut上, 通过regapi对外服务.
* Client端使用轮询方式查询更新信息

## 目录结构

```
/home/wendal/wup
	 - pkgs
	    - walnut
	 		- 20161101.tgz
	 		- 20161101-02.tgz
	 	- gbox
	 		- m1.tgz
	 		- beta1.tgz
	 	- jdk
	 		- 1.8.0_112.tgz
	 		- 1.8.0_111.tgz
	 	- mongodb
	 		- 3.2.0.tgz
	 		- 3.1.2.tgz
	 	- agent
	 		- 1.0.tgz
	 - conf
	 	- AABBCCDDEEFF.json
	 	- BBDDFFEECCEF.json
```

## pkg目录

用于存放各种版本的更新包, 包内结构

```
update      #包含更新所需要的逻辑
update.tar     #更新包的主体内容
```

# conf目录中的设备配置文件

meta数据

```json
	{
		vkey : "更新密钥", // 更新密钥
		vupdate : "2016-11-01 10:20:30.000" // 最后更新时间
	}
```

```json
{
	// 进化配置
	pkgs : [
		{name:"agent", version:"lastest"},
		{name:"mongodb", version:"lastest"},
		{name:"jdk", version:"lastest"},
		{name:"walnut", version:"lastest"}
	],
	// 环境配置
	conf : {
		"mongo-uri" : "xxx.x.x.x.x",
		"mount" : [
			{"/opt" : "/xxx/xxx"},
			{"/rs/gbox", "./ROOT/gbox"}
		]
	}
}
```
