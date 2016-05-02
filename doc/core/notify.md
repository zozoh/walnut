---
title: 通知机制
author: zozoh
tags:
- 系统
- sendqueue
---

# 通知机制是什么

专门想系统以外第三方平台发送消息的机制，包括，微信，短信等

# 通知队列

```
/sys/notify
    $id         # 每条通知都是一个文件
```

元数据

```
{
    tp          : "notify",
    
    // 消息的创建者
    noti_c : "xiaobai",
    
    // 消息的状态
    // 0  : 新建
    // 1  : 正在处理，会配合 lm，如果 timeout 则会增加 retry
    // 10 : 完成，完成后，会设置 expi ，以便系统统一删除 
    noti_st     : 0,
    
    // 消息发送的 timeout，单位秒，默认 10
    noti_timeout : 10,
    
    // 消息的发送方式
    // 可以是 "weixin", "sms" 等
    noti_by     : "weixin",
    noti_retry  : 0      // 重试次数
    noti_retry_max : 3   // 最大重试次数，默认 3
    
    // 微信模板消息专有参数 
    noti_weixin_tmpl : {
        // 消息模板的变量数据
    }
}
```

# 添加通知

```
noti add weixin '{json}'
```

# 处理通知

```
noti do [消息ID] [-u 用户] [-limit 1]
```

* 在一个 Job（也可能是几个）执行命令，挑选一个消息（cmd_obj），并调用 `noti do`
* 只能处理自己创建的消息
* 不输入 *-limit* 表示一次性处理所有的消息

# 清理已经完成的通知

```
noti clean 
```

* 清除所有 `noti_st=10` 的消息
* 只有 root 组的用户可以执行这个命令

# 删除通知

```
noti del [消息ID] [-u 用户] 
```

* 只有 root 组用户才能删除其他用户的消息
* 自己可以删除自己创建的消息


