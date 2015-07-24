---
title: HTTP接口
author: zozoh
tags:
- 系统
- http
- 接口
---

# 什么是HTTP接口

系统允许任何用户注册Web接口，以便任何第三方系统通过HTTP协议访问用户提供的接口。
当然这个 HTTP 协议的请求需要遵循一定的规范:

```
# URL 格式遵循
http://$host:$port/api/$usr/$api[?name1=val1&name2=val2...]
# 例如
http://myhost/api/zozoh/util/test?f=~/workspace/abc.txt
 - $usr = zozoh
 - $api = util/test
 - http-qs-f = ~/workspace/abc.txt

又例如:
http://myhost/api/zozoh/test
 - $usr = zozoh
 - $api = test

#----------------------------------
请求的内容会被写入一个临时文件，系统会定期清除这些临时文件
临时文件如何存储 HTTP 请求的信息呢？

$usr        >> "http-usr"
$api        >> "http-api"
QueryString >> "http-qs-" 为前缀的属性，属性名大小写敏感
HEADER      >> "http-header-" 为前缀的属性，属性名全为大写
HTTP Body   >> 存放为文件的内容，如果为 GET 请求，那么文件为空

这个文件的类型为 "httpreq"，名字无后缀
```

系统的执行逻辑为:

```
系统收到了这样的请求
将会查找对应用户有没有注册相关的HTTP接口，
如果找到注册信息
    将请求的内容保存到一个临时文件
    执行用户注册的命令
    将命令的输出写回到HTTP请求的响应
否则
    系统返回 HTTP 404
```

# 如何管理注册接口

你注册的每个接口信息，将作为一个文件保存在你的用户目录下:

```
$HOME
    [.regapi]
        [tmp]              # 存放传入的请求
            45af..
        [api]              # 存放注册的 API
            [util]
                _default   # 泛匹配，响应 util/*
                test       # $api 为 util/test
            weixin
```


# 命令的执行

你的命令存放在 `api` 目录下，每个文件内容就是一个已经注册的 api 执行命令。
系统会读取文件的内容，将里面的 `${id}` 替换为存放请求的临时文件。

实际上上，占位符支持 Obj 里面的全部字段，比如 `${ph}` 将被临时文件的全路径替换

系统启动一个进程，执行命令，并将返回写回到请求的响应体里。

# 指定响应头

通常的，你的命令执行结果会被当做  `text/plain; charset=utf-8`。
但是如果你需要返回一个指定的 `Content-Type` 的时候，你可以为你的
API 注册文件添加一些元数据，用来指定 HTTP Response 的 Header:

    http-header-Content-Type : "image/png"

API 注册文件的属性列表中，任何 `http-header-` 开头的属性会被当做响应的请求头。
后面响应头的键会被转成全大写，写回响应

**注意**

> `http-header-Content-Type` 会导致添加响应的 HTTP 头 "Content-Type"。
> 但是如果你分别设置了 `http-header-Content-Type` 和 `http-header-CONTENT-TYPE` 
> 因为系统执行是大小写敏感的，这两个信息会分别添加，那么可能会导致不符合你预期的结果。
> 因此你最好统一你的 HTTP 头书写方式

# 客户端决定响应内容类型

请求如果带有特殊的参数 `resp-mime`，则表示客户端指定了响应的类型。
这个设定会比在命令执行文件里添加 `http-header-Content-Type` 还要优先





















