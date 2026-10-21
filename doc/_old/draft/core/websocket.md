---
title: WebSocket
author:wendal
tags:
- 系统
- 通信
---

## 设计动机

为了满足Android Xplay的"即时"更新要求.

即发布完成后, Xplay客户端内的peanut需要马上知道更新,然后开始下载. 

在可靠网络下, websocket是最佳选择

* 基于Http协议, 但超越Http协议, 浏览器也直接支持, 其他协议,例如mqtt就很难在浏览器中使用
* 准实时通知

## 协议地址 

websocket建立连接,就必须有一个地址, walnut固定为/websocket

* http网页下: ws://$host_name/websocket 
* https网页下: wss://$host_name/websocket 

注意一下, https下不可以使用ws://,必然报错

供参考的js实现

```js
		var WS_URL = window.location.host+"/websocket"
		if (location.protocol == 'http:') {
			ws = new WebSocket("ws://"+WS_URL);
		} else {
			ws = new WebSocket("wss://"+WS_URL);
		}
```

## 建立连接的过程

名称/代称:
* 浏览器 B
* 服务器 S
* 连接识别码 wsid

```
B-->S 客户端发起连接, 使用ws://$host/websocket
S-->B 服务器端接受连接,下发文本欢迎信息 {event:"hi",wsid:'$wsid'}
B-->S 客户端发送监视请求, 类型为文本信息 {method:'watch', user:'$user', match:{box_macid:'AABBCCDDEEFF'}}
S-->S 根据match信息,查找到特定WnObj,标注元数据websocket_watch:$wsid
```

提示:  客户短(浏览器/其他websocket客户端)应定期发送ping信息,保持连接

## 通信文本的格式

服务器欢迎客户端的信息:
```
{
	event : "hi",
	wsid  : "ojvnos2mjkhp1onk7ah8ra107t" 
}
```

监视请求:
```
{
	method:'watch', 
	user:'$user', // 用于限定用户,存在安全性风险
	match:{box_macid:'AABBCCDDEEFF'} // 需要匹配的文件,仅匹配一条记录哦
}
```

其他通信文本,同样推荐使用json格式

## 服务器主动推送信息到客户端

服务器需要推送信息,那么必须先知道websocket的wsid,而wsid是作为文件的元数据的(websocket_watch)

所以, 必须约束一套机制,使得客户端和服务器端都能找到同一个文件

方案A: 
* 服务器预先创建一个文件,并将其id写入到html/js中,
* 客户端发起watch命令,通过match中的条件,精确匹配到这个文件上
* 因为服务器自行创建了改文件,所以它直接取这个文件的websocket_watch属性,就能得到wsid

方案B:
* 服务器端和客户端约定一个match条件, 该match条件总能匹配到同一个文件, 例如某个特殊的键值
* 服务器端只需要按约定的规律去找,总能找到客户端正在watch的那个文件,从而得到wsid