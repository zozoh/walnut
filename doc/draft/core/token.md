---
title: Token机制
author: wendal
tags:
- 系统
- API
---



# 什么是token

token是一个随机字符串, 调用双方同时持有, 通过一定的规则来验证请求的合法性

对于一些需要确定用户身份,但传送账号密码又不方便/不安全的环境下,需要一个机制来确认请求的用户及请求本身的合法性

# 如何验证合法性

先看看请求方,在请求时,它所知道的数据, 或者说是它可以控制的数据有:

* App识别码 appid
* 密钥 token **不发送**
* 时间戳 timestamp
* 随机字符串 nonce
* 签名 sign
* 其他请求参数

服务器端将收到:

* App识别码 appid
* 时间戳 timestamp
* 随机字符串 nonce
* 签名 sign
* 其他请求参数

注意, token是**不会**发送到服务器端的, 而sign是根据一个算法得到的.

那么, 服务器端如何知道token的值呢? 根据appid. 因为appid与token,在服务器端成对存放. 知道appid,就能查到token.

综上所述, 客户端和服务器端都知道了全部参数, 接下来就是验证请求的合法性了, 这一点通过sign的算法来验证.

## sign的算法

sign=sha256(nonce, timestamp, token, 其他请求参数)

因为客户端和服务器端均知道上述算法的全部变量值,只要其中任意一个变量不对,上述得出的sign肯定对不上.

鉴于sha1已经出现撞库, 所以开始使用sha256做hash算法.

## 为什么需要nonce和timestamp

按照sign的算法, 不加nonce和timestamp也能验证请求的合法性, 对吗? 不完全对.

需要考虑中间人攻击, 即假设上述请求被他人拦截, 然后原样重发一次, 因为sign算法中的变量均不变(没有nonce和timestamp),所以sign依然合法, 从而放行.

那nonce为啥能防止? 因为我们要求nonce只能用一次,第二次就失效, 换句话说, 把原请求重发一次, 不允许放行了

那,那为啥还有timestamp啊!!! 因为nonce是一次性,服务器需要记住用过的nonce,但不可能无限期存放下去,那怎么办?!! 不存嘛, 有人拿一天前用过的nonce来验证,怎么办?!!!

所以, 我们加个timestamp, 且要求客户端时间与服务器时间的差异不能超过10分钟, 然后nonce存20分钟, 就解决了, 为啥呢?

例如 T+0时发起请求, 用了nonce=A, 然后在20分钟内,再使用同一个nonce=A就直接reject, 然后20分钟后, 服务器端的nonce=A已经删了,但timestamp也相差了20分钟, 一样reject掉.

## 还有什么要补充的吗?

有!!! token的传送过程,需要是安全可靠的, 若需要走http协议发出去,起码走https!!! 你看微信/支付宝,都是要你手敲到自己的系统去,杜绝直接传给你...