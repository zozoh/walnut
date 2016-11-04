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
~/wup
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

其中, ~/wup是数据根目录,执行wup命令时可以指定

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

节点数据文件

```json
{
	// 进化配置
	"pkgs" : [
		{"name":"agent", "version":"lastest"},
		{"name":"mongodb", "version":"lastest"},
		{"name":"jdk", "version":"lastest"},
		{"name":"walnut", "version":"lastest"}
	],
	// 环境配置
	"conf" : {
		"mongo-uri" : "xxx.x.x.x.x",
		"mount" : [
			{"/opt" : "/xxx/xxx"},
			{"/rs/gbox", "./ROOT/gbox"}
		]
	}
}
```

## Http Reg API

### 节点初始化

```
/api/$user/wup/node/init?macid=AABBCCDDEEFF&godkey=123456
```

参数:

* macid - 机器识别码
* godkey - 初始化密钥

返回值

```json
{
	"key" : "2dd4...stvz" // 更新密钥
}
```

### 获取节点配置信息

```
/api/$user/wup/node/get?macid=AABBCCDDEEFF&key=123456
```

参数:

* macid - 机器识别码
* key - 更新密钥

返回值就是节点数据文件本身

### 读取更新信息

```
/api/$user/wup/pkg/info?macid=AABBCCDDEEFF&key=123456&name=walnut&version=3.0
```

参数:

* macid - 机器识别码
* key - 更新密钥
* name - 更新包名称
* version - 版本号

返回值就是对应的更新文件的WnObj内容,并额外添加一个cdn属性

```json
{
	sha1 : "aabccddefff", // 文件指纹,用于校验数据正确性
	len : "",             // 更新包大小
	cdn : ""              // 可选的cdn下载地址
}
```

若更新包不存在,则返回404

### 读取更新包

```
/api/$user/wup/pkg/get?macid=AABBCCDDEEFF&key=123456&name=walnut&version=3.0
```

参数:

* macid - 机器识别码
* key - 更新密钥
* name - 更新包名称
* version - 版本号

返回值为文件数据
