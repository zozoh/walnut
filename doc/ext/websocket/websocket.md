---
title: WebSocket
author:wendal
tags:
- 系统
- 通信
---

## 应用场景

长连接,异步通信,远程执行及响应

## 回调存储

由于websocket属于异步通信, 下发数据后, 等待回应后, 通常需要执行下一步操作.

```
/sys/ws

	-- aabbccddeeffgg
	-- xxyyzzaabbccdd
```

每个文件含元数据

```js
{
		ws_ct_usr  : "root",  // Job的创建者
		ws_usr   : "root",    // 运行时用户名
		ws_grp   : "wendal",  // 运行时组
		expi     : 1241241234 // 过期时间
}
```

ws的回调文件默认10分钟过期

## 链接建立

客户端发起连接 

```
ws://$ip/websocket
wss://$ip/websocket
```

服务器端下发初始化信息,并将websocket登记到本地map,原因是WebSocketSession无法序列化.

```js
{
	event: 'hi',
	wsid : session.getId()
}
```

客户端收到event:hi之后,上传关注信息

```js
{
	method: "watch",
	user  : "wendal",
	match : {
		box_macid : "AABBCCDDEEFF"
	}
}
```

服务器根据客户端的信息,将符合条件的一个WnObj赋值websocket_watch元数据.

```
{
	websocket_watch: "asdfgfewrtegsdfgd"
}
```

## 通信流程

服务器根据websocket_watch存储的websocket session id, 下发数据, 可以是文本数据/Event数据/二进制数据

```js
{
	event : "reboot",
	id    : "aabbccddeeffgg"
}
```

如果需要回调,则存储 /sys/ws/aabbccddeeffgg

客户端收到,并执行后,反馈

```js
{
	method: "resp",
	id: "aabbccddeeffgg",
	ok: true,
	args: ["xxx", "yyy"]
}
```