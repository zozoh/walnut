---
title:微信命令
author:zozoh
tags:
- 系统
- 扩展
- 微信
---

# 微信命令概述

微信命令与 [httpapi](../core/httpapi.md) 
以及 [sendqueue](../core/sendqueue.md) 联合使用，可以:

1. 处理微信发来的消息
2. 主动调用微信的接口
3. 处理自定义菜单
4. 其他微信接口提供的功能

# 微信的权限验证

微信服务器与第三方服务器的加密信息存放在目录 `~/.weixin/` 下。

    ~/.weixin/
        rain_on_sand       # 某个微信公众号的配置存放目录
            wxconf         # 存放应用信息
            access_token   # 存放临时的 access_token
            jsapi_ticket   # 存放临时的 jsapi_ticket
            context        # 存放与用户会话的上下文
                78a5gg..   # 每个 openid 一个目录
                           # 后面的章节有详细描述
        another_公众号      # 每个微信公众号一个目录

## wxconf 的文件格式 

    {
        appID      : 'wx0d4caad29f23b326',
        appsecret  : 'd68d9507835139b0e21d28b4806c1aa7',
        token      : 'TOKEN',
        jsSdkUrl   : "http://...",     // 对于JS-SDK默认的URL
        jsApiList  : [..],             // 对于JS-SDK默认的api列表 
        // 一个数组声明了如何处理微信输入消息
        // 如果一旦匹配，就会退出循环，如果找不到匹配，会返回标准回复信息
        handlers : [
            // ...............................................
            {
                // 是否匹配处理器
                match : {
                    MsgType : "text|image|...",
                    // Content 仅当 text 有效
                    Content : "文字" | {regex:"正则表达式"}, 
                    // 下面两个仅当 event有效
                    Event   : "事件" | {regex:"正则表达式"},
                    EventKey: "事件KEY值" | {regex:"正则表达式"}
                },
                // 是否要生成上下文
                context  : false,
                // 如果匹配了，就用这个命令模板处理命令
                // 占位符就是 http 请求对象的各个字段
                command  : "echo ${id}"
            }, 
            // ................................. 下一个配置项目 ..
            {
                // 空 Map 表示无论什么事件都匹配 
                match   : {},
                context : true,
                // 特殊占位符 ${weixin_context} 表示存放命令上下文的对象
                // 仅当 genContext 为 true 的时候才起作用
                command : "echo ${weixin_context}"
            }
        ]
    }

## `access_token` 和 `jsapi_ticket`

这两个文件的内容就是票据内容，但是都有一个元数据 `expires` 指明一个绝对毫秒数。
如果当前系统时间超过了这个时间，则代表无效。

这两个文件由 `weixin` 命令维护，它会定时自己获取新的票据信息。

# 消息上下文和子命令

假设开发者想基于 `weixin` 命令开发交互式的用户体验:

    用户: 你好
    公众号: 您好，请问您怎么称呼?
    用户: 爸爸
    公众号: 爸爸您好，请问您要我做什么?
    用户: 吃屎
    公众号: 爸爸，我不能吃屎

那么怎么开发呢，上文，提到的目录:

    ~/.weixin/
        rain_on_sand       # 公众号
            context        # 上下文
                78a5gg..   # openid

你的命令如何得到上下文目录呢？ 当然你可以直接用绝对路径读取，不过有点麻烦对吧。
所以你注册命令的时候，可以

    commands: "mycmd ${id} -wxcontext ${weixin_context}"

和占位符 `${id}` 一样， 占位符 `${weixin_context}` 就是目录
`~/.weixin/rain_on_sand/context` 的 ID。 你可以把临时信息存放到这个目录的元数据里。
甚至你可以存放用户上传上来的媒体等文件。

如果你在 `${weixin_context}` 注明了元数据 `next_cmd`，那么当这个用户再次发送消息的时候，
就不会用 *wxconf* 文件里的 handler 来处理，而是将 `next_cmd` 的值作为命令模板来执行。

考虑到时效性，每个公众号的上下文如果不被访问，将只保持 *1个小时* 之后会被删除。

如果你的命令模板里有占位符 `${weixin_context}`，则上下文会被自动创建。 当然你在自己的命令里可以主动删除它。

# 与微信服务器连接

## 设置 wxconf

按照前面的约定，自行在你的公众号目录下创建 `wxconf` 文件，并填写正确的.

* appID
* appsecret
* token

当然你可以不填写 `handlers` 字段，也不会影响微信服务器的验证的。但是如果你
想根据用户的发来的消息，自动做点什么事情，这个字段就是存放你业务逻辑的地方。 

## 注册 httpapi

首先需要在 `~/.regapi/api/` 目录下建立一个注册的 api，
比如 `~/.regapi/api/demo/myweixin`。内容是:

    weixin -in id:${id}

## 配置转发 URL

之后在微信公众号后台，将 api 的地址配置入 `开发者中心>服务器配置>URL`，

    http://$youhost/api/$usr/demo/myweixin

# 自定义菜单

## 创建
    weixin -pnb 微信号 -menu ~/path/to/menu

## 删除
    weixin -pnb 微信号 -menu delete

# 主动发送消息

    weixin -pnb 微信号 -to $OpenId -out ~/path/to/send

当然，如果没有 `-out`，命令会试图从管道里读取输入。

# 输出响应消息

    weixin -out "{...一个json 数据..}" [-inmsg id:$id]

当不指明 *-pnb* 和 *-to* 的时候，*-out* 表示向标准输出写入一个微信的 XML 响应数据。
同时你可选一个选项 *-inmsg*，表明发来的消息。 本逻辑会根据这个微信消息设置 *fromUserName* 和 *toUserName*， 即使你在 *-out* 里指定了，也会被覆盖

当然用一个 JSON 描述微信的消息输出，太复杂了，如果内容较多，你可以把内容写到一个文档

    weixin -out "id:文档ID" [-inmsg id:$id]

同时也支持简明输出

    # 简单文本消息
    weixin -out "text:消息内容" [-inmsg id:$id]
    #
    # 一篇文章
    weixin -out "article:标题;;可选的描述;;可选的超链接" [-inmsg id:$id]



















