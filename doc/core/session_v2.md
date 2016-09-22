---
title:会话
author:zozoh
tags:
- 系统
- 会话
---

# 会话的数据结构

每当用户登录成功，系统会为其建立一个专有的会话对象

    /session/e35d..a23c

这个会话对象是个文本文件，会记录一些用户的 IP，客户端信息等数据。以备随时进行安全验证。
文件名则就是这个 session 的 ID。同时这个对象会有下面这些元数据:

    id   : "xxx"        # 会话的 ID
    nm   : "xxx"        # 也是会话的 ID
    me   : "xiaobai"    # 会话的用户
    grp  : "xiaobai"    # 会话所属的组（域）
    du   : MS           # 会话持续时间，毫秒
    expi : AMS          # 绝对毫秒数，会话的过期时间
    p_se_id : null      # 父会话

* 通过 `id+me+grp` 来校验会话
* 通过 `p_se_id` 来管理多重登录，即，当一个会话被 touch，所有的父会话也将被 touch

# 会话的环境变量

会话对象内容部就是一个 JSON 的数据结构，存放所有用户的环境变量

    {
        HOME : "/home/zozoh",
        PATH : ["/bin","/sbin","~/bin"],
        PWD  : "/home/zozoh/workspace/git/nutz"
    }

* 当调用会话接口的 var 相关函数的时候，才会读取环境变量

#  如何实现多重 Session 登录

## UsrModule

在 `do_logout` 和 `do_logout_ajax` 函数里，会执行 `WnSessionService.logout`
如果返回的不是 null 表示退出到父会话，那么写下去的响应，应该修改 cookie 的设定

## 命令行实现

实现 `cmd_login` 命令，用法

```
login superman
```

* 检查当前账号是否是 `op` 组管理员，或者 `root` 组成员
    - @see `sys.usrService.isAdminOfGroup(sys.me, "op")`
    - @see `sys.usrService.isMemberOfGroup(sys.me, "root")` 
    - 参考命令 `cmd_renameUser`
* 如果检查通过会根据当前会话创建父会话（注意，这里需要用 root 权限执行
    - `Wn.WC().su` 函数可以用来切换用户

* 命令执行的输出就是新的 Session 对象
    - 新会话的过期时间是10秒
    - 同时命令会向线程上下文放置这个新会话的 ID
* `AppModule.run` 会在请求结束的时候，添加一个新的宏，告诉客户端切换 SessionID
* `Wn.exec` 应用执行这个宏
    - 将请求 `/u/do/chse/ajax?id=NewSeId` 切换会话的 cookie
    - 这个请求将会是同步请求

## 界面实现

* 界面的 browser 控件用户信息部分将提供界面，让用户输入新的域的 name
    - 登录的时候，相当于也执行 `login` 命令 
* 退出连接会访问 `/u/do/logout`
    - `UsrModule.do_logout` 函数如果发现有父会话，将会设置父会话的 ID 到响应的 cookie 里

# 会话接口: WnSession

管理会话的读写

`@see org.nutz.walnut.api.usr.WnSession`

# 会话服务接口: WnSessionService

管理会话的生命周期，即创建，删除，获取等

`@see org.nutz.walnut.api.usr.WnSessionService`











