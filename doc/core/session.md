---
title:会话
author:zozoh
tags:
- 系统
- 会话
---

# 会话对象

每当用户登录成功，系统会为其建立一个专有的会话对象

    /session/e35d..a23c

这个会话对象是个文本文件，会记录一些用户的 IP，客户端信息等数据。以备随时进行安全验证。
文件名则就是这个 session 的 ID。同时这个对象会有下面这些元数据:

    me   : "xiaobai"    # 会话的用户
    expi : 36000000     # 会话多长时间会过期(ms), 0 表示已经过期了
    lm   : Date()       # 最后修改时间则变成用户最后一次访问时间(ms)

我们通过 `obj` 的索引表，可以很容易查到特殊用户 `xiaobai` 正打开哪几个会话。

# 会话对象内容

同时，会话对象的内容就是一个 JSON 文本，以便持久化用户保存在 Session 里面的信息:

    {
        // 这些是 Session Obj 包括的信息
        id           : "ea12..",   // 服务器的 SessionId
        me           : "zozoh",    // 记录当前操作的用户
        expired      : 36000000, // 有效期 
        lastModified : Date(),   // 即 Session Obj 的 `lm`
        //................................................
        // 环境变量，更多详情，请参见 《环境变量》一节
        // 基本上这些值都是可以被用户在界面上修改的
        envs: {
            HOME : "/home/zozoh",
            PATH : ["/bin","/sbin","~/bin"],
            PWD  : "/home/zozoh/workspace/git/nutz"
        }
    }

# 多重登录

就像 *Linux/Unix* 一样，客户端在已经登录的会话上执行 `su - zozoh` 就会切换账号。如果用户
执行 `exit` 就会退回上一个会话。为了做到这一点，服务器采用树形结构记录会话

    /session/eda2..                  # signup  xiaobai
    /session/eda2../7f3c..           # su - zozoh
    /session/eda2../7f3c../9ba6..    # su - root

* 当执行 `exit` 的时候，会返回上一层会话，如果过没有上一层了则会提示用户登录
* 会话被退出前，会首先更新父会话的 *lm* 以便保证父会话不会过期















