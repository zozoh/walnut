---
title: HTTP 注册接口
author: zozohtnt@gmail.com
---

--------------------------------------
# 动机：为什么要有HTTP注册接口

让任何用户都可以轻松的搭建自己的动态`HTTP`服务。

--------------------------------------
# 计划应用场景

- 论坛，商城等动态网站
- 游戏/应用的动态数据获取
- 向第三方开放数据接口

--------------------------------------
# 设计思路与边界

在一个域里，通过简单的配置，就能让一个 http 请求路由到一个命令里。
命令的输出就是 http 的响应。

为了能获取 HTTP 请求的内容，每次请求都会为其建立一个文件对象。
具体请参看 [请求对象](#请求对象) 一节。

> 当然，为了让请求速度更快，可以将 `~/.regapi/tmp/` 映射为内存模式

--------------------------------------
# 数据结构描述

## 域数据结构

```bash
~/.regapi/
#------------------------------
# 存放所有的API对象
|-- api/
|   |-- test       # 对应 /api/{MyDomain}/test
|   |-- weixin/    # 按目录存放路径
|   |   |-- in     # 对应 /api/{MyDomain}/weixin/in
#------------------------------
# 路径通配符
# `_ANY` 表示通配符
# 元数据 `api-param-name:"id"` 表示这个通配符的形参名
# 更多请参看 [路径参数] 一节
|   |-- _ANY/      # 路径通配符 ?
|   |   |-- _ANY   # 文件的话，相当于 *
#------------------------------
# 存放所有的临时请求对象
|-- tmp/
    |-- ofb5luva5agl2pnmlj2376vuku   # 每个请一个对象
    |-- piub8fs0dgg95qvpgr0vo8rm2m   # 请求对象会缓存10分钟
```

## API对象

API 对象是一个文件，REGAPI模块通过一个`http url`路由到它，然后通过它的文件内容（命令模板）来处理请求。它通过一些元数据支持如下的定制行为:

```bash
#--------------------------------------------------
#               指定响应头
#--------------------------------------------------
# 指定了 http-header- 前缀的元数据会固定写入到响应头里，
# 当然，元数据名称是大小写不敏感的，实际上写入响应头时全部会被转成大写
http-header-Content-Type : "text/plain; charset=utf-8"
# or
http-header-Content-Type : "image/png"
#--------------------------------------------------
#                 重定向
#--------------------------------------------------
# 开启这个选项，你的命令输出将会作为重定向的内容
http-resp-code : 302
#--------------------------------------------------
#               动态响应头
#--------------------------------------------------
# 有时候你需要用命令来动态决定响应头
# 开启这个选项即可。详情请参看 [动态决定响应头] 一节
#--------------------------------------------------
http-dynamic-header : true
#--------------------------------------------------
#                 网站登陆
#--------------------------------------------------
# 根据票据自动取得网站用户的账户信息
# @see [获取网站会话] 一节获取更多信息
http-www-home   : "~/www"  # 站点目录
http-www-ticket : "http-qs-ticket"  # 从哪里获取票据，默认http-qs-ticket
# 如果为 true，则必须登陆，否则返回 403
# 默认会看看是否设置了 http-www-home
http-www-auth   : true
#--------------------------------------------------
#                 请求对象
#--------------------------------------------------
# 请求对象被创建后一段时间会被自动删除
# 默认的时间为 1 分钟
http-tmp-duraion : 60000
#--------------------------------------------------
# 你可以指定将请求的某几个 Cookie 值 Copy 到请求执行的上下文里
# 不过通常你不需要设置这个属性
#--------------------------------------------------
copy-cookie : ["SEID", "ABC"]
```

文件的内容是一个命令模板，模板的占位符上下文为请求对象本身。
因此你可以使用任何请求对象的元数据，也可以用 `cat id:${id}` 的方式读取请求对象的内容。

## 请求对象

```bash
#--------------------------------------------------
#
#                 域信息
#
#--------------------------------------------------
http-usr : "demo"        # API的处理域主账户名
http-grp : "demo"        # API的处理域主账户的主组（通常就是域的名称）
http-usr : "/home/demo/" # API的处理域主账户名的HOME目录
http-api : "test"        # API 文件名称
#--------------------------------------------------
#
#                 系统会话信息
#
#--------------------------------------------------
# 如果你已经登录了一个 Walnut 系统会话，这个会显示系统会话信息
# 当然，如果你没登陆（同时是没登陆的）这些元数据都是没有的
http-se-id      : "67r..23q"   # 会话 ID
http-se-ticket  : "8um..32q"   # 会话票据
http-se-me-name : "demo"       # 会话用户名
http-se-me-group: "demo"       # 会话用户主组
http-se-vars : {..}            # 会话内的环境变量
#--------------------------------------------------
#
#                 站点会话信息
#
#--------------------------------------------------
# 如果你已经在域里建立一个有用户系统的站点
# 当前会话信息会自动增加在这里
http-www-se-id     : "y6..91"     # 站点会话ID 
http-www-se-ticket : "8um..32q"   # 站点会话票据
http-www-me-id     : "5e..g1"     # 会话用户ID
http-www-me-nm     : "xiaobai"    # 会话用户登陆名
http-www-me-phone  : "139..."     # 会话用户手机号
http-www-me-email  : "x@xx.com"   # 会话用户邮箱
http-www-me-role   : "user"       # 会话用户业务角色名
http-www-me-nickname : "小白"      # 会话用户昵称
#--------------------------------------------------
#
#                 请求信息
#
#--------------------------------------------------
http-url : "http://localhost:8080/api/demo/test"  # 请求的完整路径
http-uri : "/api/demo/test"  # 请求的全路径
http-method   : "GET"        # 请求方法
http-protocol : "HTTP/1.1"   # 协议

# 路径参数： @see “路径参数”一节
args : ["test"]
params : {
  "nm" : "test"
}

# Query String 
# 请求参数用 http-qs- 作为前缀，参数名全小写
http-qs   : "a=find&x=99"
http-qs-a : "find"
http-qs-x : "99"

# 所有的 Cookie 用 http-cookie- 作为前缀
# 键名全大写
http-cookie-SEID: "ujsmsd0564juorkvek6v3hgg7o"

# 所有的请求头参数用 http-header- 作为前缀
# 键名全大写
http-header-ACCEPT: "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
http-header-ACCEPT-ENCODING : "gzip, deflate, br",
http-header-ACCEPT-LANGUAGE : "zh-CN,zh;q=0.9",
http-header-CONNECTION : "keep-alive",
http-header-COOKIE : "SEID=ujsmsd0564juorkvek6v3hgg7o",
http-header-HOST : "localhost:8080",
http-header-SEC-FETCH-DEST : "document",
http-header-SEC-FETCH-MODE : "navigate",
http-header-SEC-FETCH-SITE : "none",
http-header-SEC-FETCH-USER : "?1",
http-header-UPGRADE-INSECURE-REQUESTS : "1",
http-header-USER-AGENT : "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36",
```

请求对象的请求体就是 HTTP 请求的流内容。普通的GET请求，那么它就是空。
如果是`POST`请求，那么就是：

```
a=xxx&b=xxx
```

如果是HTML5的文件上传流，那么就是文件内容:

```
7b0a 2020 2278 223a 2031 3030 2c0a 2020
227a 223a 2033 322c 0a20 2022 7922 3a20
...
```

因此，你要做一个文件上传，就非常简单了，直接 `cp id:${id} ~/path/to/target` 即可。


---------------------------------------------
# 动态决定响应头

有时候，你的希望你的 api 命令来指定 HTTP 响应的头部内容，你可以为你的 api 文件添加元数据 `http-dynamic-header=true`。这样系统执行你的命令时，会先将你命令所有的输出，输出到一个字符串缓冲里，然后进行分析。 如果你的输出格式类似:

```
HTTP/1.1 200 OK
Content-Type: text/html;charset=UTF-8
SET-COOKIE:MYID=xxxxxxxx; Path=/;
Server: Walnut HTTPAPI

<html>
...
```

那么它会解析输出内容的头部，将其作为标准的 HTTP 头写回到响应里。

这个机制非常适合读取多媒体信息等场景，因为你可以在命令里判断是否可以输出 `HTTP302`。
Walnut 也为你内置了一个命令：

```bash
# API(thing): 缩略图
@FILE .regapi/api/thumb
{
  "http-dynamic-header": true
}
%COPY:
httpout -body ${http-qs} \
  -etag  '${http-header-IF-NONE-MATCH?none}' \
  -range '${http-header-RANGE?}'
%END%
```

--------------------------------------
# 路径参数

考虑到兼容世界上大多数 REST API，必须支持路径参数啊。

- `.regapi/api` 目录下的 名字为 `_ANY` 文件或者目录表示 `?`
- 如果 `_ANY` 是目录，则相当于 `?` 其内的 `_action` 文件表示具体的操作
- 如果 `_ANY` 是文件，则相当于 `*`
- `_ANY` 目录/文件支持元数据: `api-param-name:"id"`，以便为路径参数指定形参名称

## 匹配 `?`

```bash
API:  ~/.regapi/api/post/_ANY/_action
        其中 _ANY {"api-param-name" : "id"}
URL: http://host/api/yourdomain/post/9827
请求对象会多出元数据: 
{
    ...
    args : ["9827"]
    params : {       // 只有声明了 "api-param-name" 才会有
        "id" : "9827"
    }
    ...
}
#---------------------------------------------
API:  ~/.regapi/api/post/_ANY/_action
如果URL: http://host/api/yourdomain/post/9827/xyz
那么就会 404
#---------------------------------------------
API:  ~/.regapi/api/post/_ANY/get
URL: http://host/api/yourdomain/post/9827/get
请求对象会多出元数据: 
{
    ...
    args : ["9827"]
    ...
}
#---------------------------------------------
如果URL: http://host/api/yourdomain/post/9827
那么就会 404
```

## 匹配 `*`

```bash
API:  ~/.regapi/api/post/_ANY
        其中 _ANY {"api-param-name" : "ph"}
URL: http://host/api/yourdomain/post/cat/409681
请求对象会多出元数据: 
{
    ...
    args : ["cat/409681"]
    params : {       // 只有声明了 "api-param-name" 才会有
        "ph" : "cat/409681"
    }
    ...
}
#---------------------------------------------
如果URL: http://host/api/yourdomain/xyz/9827
那么就会 404
```

## 组合两种通配符


```bash
API:  ~/.regapi/api/post/_ANY/_ANY/_action
        其中
            _ANY[0] {"api-param-name" : "type"}
            _ANY[1] {"api-param-name" : "id"}
URL: http://host/api/yourdomain/post/pet/cat/409681
请求对象会多出元数据: 
{
    ...
    args : ["pet", "cat/40981"]
    params : {       // 只有声明了 "api-param-name" 才会有
        "type" : "cat",
        "id" : "cat/409681"
    }
    ...
}
```