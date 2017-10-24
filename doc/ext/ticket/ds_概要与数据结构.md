# 工单概要

工单系统的重要节点包含：

* 输入
* 响应
* 处理
* 反馈
* 收集总结

## 输入

主要是用户进行，可以通过多种渠道提交工单

1. 网页表格
2. 邮件
2. 电话
3. APP
4. 公众号
5. 即时通讯工具


客户提交的内容包含文字，语音，图片，视频等，最终将这些内容作为工单的内容保存到服务器中。

## 响应

在工单提交后，应第一时间向有关人员（客服，开发）等推送消息，同时开始记录工单的响应时长。

工单推送系统应根据事先安排好的工单流转策略，将不同类型的工单转发到处理人手中。

在工单推送后一段时间内无响应，应及时通知相关人员，由其他人员处理该工单

## 处理

工单的处理包括回复工单提出的内容，重点记录需求，Bug等内容。

一个工单可能会有与客户多次沟通记录的过程，等问题确实解决后（需求，bug开Issue），可记录该工单为完成状态。

## 反馈

工单处理结束后，记录用户的满意度与处理过程中发现的问题。

## 收集总结

定期回顾与整理工单内容，对于问题型的工单，可筛选出常见问题作为Wiki，后续补充完善到帮助中心。

定期输出统计信息，包括数量，类型分类，响应时长，解决率，满意度等


# 流转图

![工单流转](media/%E5%B7%A5%E5%8D%95%E6%B5%81%E8%BD%AC.png)



# 数据结构

基于walnut系统的工单，因为涉及到跨域的问题，对外采用regapi的方式来进行数据交互。

比如stie0项目，单独开一个域进行工单管理，其他域（用户）只要注册到该域即可提交工单。


## 目录结构


```
~/.ticket
    ticket.json         // 配置文件
    /user               // 用户目录
        wn_${usrid}             // 用户1
        u002                    // 用户2
        ...
    /cservice           // 客服目录
        wn_${usrid}             // 客服1
        cs002                   // 客服2
        ...
    /record             // 工单记录
        /${usrNm}_${time}       // 工单1，目录形式 里面可存放附件
        /t1111111111112         // 工单2
        .....
```

## ticket.json

```js
{
    nm: "零站工单服务",
    welcome: "欢迎你注册使用零站工单服务"    
}
```

## 用户

```js
{
    usrId:          $id,                // walnut用户user.id
    usrNm:          "xxx",              // walnut用户user.nm
    usrDmn:         "xxx",              // /home/xxx 这个xxx，方便知道是哪个域
    notiWalnut:     true,               // walnut系统通知，比如登陆网页，app等
    notiEmail:      true,               // 邮件通知，需用户配置过邮箱
    notiSms:        true,               // 短信通知，需用户配置过短信，该功能可收费
    notiWeixin:     true,               // 微信通知，关注过公众号，绑定了walnut用户    
}

```


## 客服

```js
{
    usrId:          $id,                // walnut用户user.id
    usrNm:          "xxx",              // walnut用户user.nm
    usrDmn:         "xxx",              // /home/xxx 这个xxx，方便知道是哪个域
    usrAlias:       "xxx",              // 客服昵称，
    serviceTp:      "CS" | "DT"         // 客服类型，不同类型具有一些额外操作或流程
    notiWalnut:     true,               // walnut系统通知，比如登陆网页，app等
    notiEmail:      true,               // 邮件通知，需用户配置过邮箱
    notiSms:        true,               // 短信通知，需用户配置过短信
    notiWeixin:     true,               // 微信通知，关注过公众号，绑定了walnut用户    
}

```


客服类型：

* CS 普通客服 负责回答一些常见问题，比如操作上的或功能上的
* DT 研发客服 还有需求分析的任务，开Issue等

## 工单对象

TODO 考虑使用thing来管理工单


```js
{
    // 基本信息
    usrId:              $id,           // walnut用户user.id
    tickerStart:   14...333,           // 时间戳 开始时间 即提交时间
    ticketEnd:           -1,           // 时间戳 结束时间 -1表示未结束
    ticketStatus:     "xxx",           // 工单状态 
    ticketTp:       "xxxxx",           // 工单类型 确定大概问题方向, 客服可根据内容进行调整
    
    title:          "xxxxx",            // 工单标题
    text:           "xxxxx",            // 第一次提交内容
    // 用户提交记录,  列表方式记录
    request:  [{                       
        text:       "xxxxxxxxxx",       // 文本形式的内容
        attachments:  [$id, $id],       // 附件的id，可以是图片，语言，视频等
        time:           14...333,       // 时间戳 响应时间
    }, {                   
        text:       "xxxxxxxxxx",       
        attachments:  [$id, $id],       
        time:           14...333,       
    }, {
        ......
    }],

    // 当前客服，最后处理的人
    csId:            $id,                // walnut用户user.id
    csAlias:         "xxx",              // 客服昵称
    // 转移记录
    csTrans:         [$id, $id]          // 哪些客服依次处理过该问题
    csTransTime:     [14.., 14..]        // 记录转移时间，也就是分配时间

    // 响应记录，因为响应客服可以进行转移，且存在多次响应情况，所以采用列表方式记录
    response: [{
        csId:                $id,        // walnut用户user.id
        csAlias:         "xxx",              // 客服昵称
        text:       "xxxxxxxxxx",        // 文本形式的内容
        attachments:   [$id, $id]        // 附件的id，可以是图片，语言，视频等
        time:           14...333,        // 时间戳 响应时间
    }, {
        ......
    }],

    // 标签
    ticketTag: ["xxx", "yyyy"],             // 方便过滤与查询

    // 需求、Bug
    ticketIssue: ["xxx", "xxxx"]            // 对应的Issue列表

    // 满意度
    satisfaction:    1                   // 满意度，用户确定问题解决时，需提交满意度调查
}

```

ticketStatus列表


| 名称 | 含义 |
| --- | :-- |
| new | 新工单待分派  |
| assign | 工单已分派 |
| reassign | 工单重新分派 |
| creply | 待您反馈  |
| ureply | 待客服继续处理 |
| done | 工单处理完毕 |
| close | 工单已关闭 |

ticketTp 列表


| 名称 | 含义 |
| --- | --- |
| issue | bug或新需求 |
| question | 使用问题 |




请求与响应列表合并，并按照时间戳排序既是整个工单的处理过程。

在请求与响应列表发生变化时，应将内容发到通知服务进程，根据用户、客户的推送配置进行消息推送。



