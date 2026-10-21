---
title: 第三方登录
author:wendal
tags:
- 系统
- 用户
---

## 设计动机

为了满足第三方登录的需求

## 目录结构

```
- /home
	- wendal
		 - .oauth
		 	- conf.properties
		 	- tmp
		 		- xxx...yyy
		 		- aaa...bbb
```

其中

* conf.properties是oauth的配置文件
* tmp目录内,以session id为文件名,存放oauth所需要的SocialAuthManager序列化数据(JDK序列化), 过期时间为5分钟

## 调用过程

```
C->S: oauth send github https://nutz.cn/yvr/u/oauth/github/callback
S->C: https://github.com/login/oauth/authorize?client_id=30100863592971748671&response_type=code&redirect_uri=https%3A%2F%2Fnutz.cn%2Fyvr%2Fu%2Foauth%2Fgithub%2Fcallback&scope=user:email
C->Oauth: 重定向到oauth send返回的URL,进行第三方登录操作
Oauth->C: 第三方系统登录完成,返回到return URL
C->S: cat '{code:${http-qs-code}}' | oauth callback github
S->C: {provider:"github",profileId:"589819",aa:"wendal"}
```