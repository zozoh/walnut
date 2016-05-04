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
    tp          : "wn_noti",
    
    // 消息的创建者
    noti_c : "xiaobai",
    
    // 消息的状态
    // 0  : 新建
    // 1  : 正在处理，会配合 lm，如果 timeout 则会增加 retry
    // 10 : 完成，完成后，会设置 expi ，以便系统统一删除 
    // 14 : 失败
    noti_st     : 0,
    
    // 消息执行发送后的状态的具体描述
    // 如果状态是 10 则一定是 "ok"，否则就是错误详情
    noti_errmsg : "xxxx"
    
    // 消息发送的 timeout，单位秒，默认 10
    noti_timeout : 10,
    
    // 消息的发送方式
    // 可以是 "weixin", "sms" 等
    noti_by     : "weixin",
    noti_retry  : 0      // 重试次数
    noti_retry_max : 3   // 最大重试次数，默认 3
    
    // 消息发送目标，不同发送处理器，对这个目标的理解不同
    // 比如 sms 就人为是手机号, weixin 就认为是 openID 等
    noti_target : "xxxx"

    //...........................................
    // 短信消息专有参数
    //...........................................
    // 短信内容
    noti_sms_text : "xxxxxx"
    
    // 短信服务供应商
    noti_sms_provider : "xxxx"
    
    //...........................................
    // 微信模板消息专有参数 
    //...........................................
    // 模板 ID
    noti_wx_tmpl_id : "xxxxx"
    
    // 消息的 URL
    noti_wx_tmpl_url : "http://xxxx"
    
    // 消息内容
    noti_weixin_tmpl_content : {
        // 消息模板的变量数据
    }
    
    // 微信消息发送后，服务器返回的 msgid
    noti_wx_tmpl_msgid : 200228332

}
```

# 添加通知

```
# 添加模板通知
noti add weixin -tmpl "ngqIp.."
                -to "OPENID"
                -url "xxxxx"
                -content "{..}"

# 添加短信通知
noti add sms -text "xxxx" -to "139.." -provider xxx
```


# 执行发送通知

```
noti send [消息ID] [-u 用户] [-limit 1]
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


