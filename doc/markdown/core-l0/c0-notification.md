---
title: 队列通知机制
author: zozohtnt@gmail.com
tags:
- 概念
- 基础
---

--------------------------------------
# 动机：为什么要有通知机制

Walnut 系统通过内置模块以及[HTTP注册接口][c1-api]接受外部HTTP请求。
但是这样的请求都是即时处理返回的。有时候我们需要一个可以延迟处理的机制。

即，客户端请求系统，系统只是将这个请求做一下记录，稍后客户端会主动查询执行结果。
当然也可能不再查询，或者由系统通过某个机制触发客户端的查询。

--------------------------------------
# 计划应用场景

- 高并发场景（譬如秒杀）
- 后台运行任务
- 定时运行任务（从一个表读取定时任务，然后加入任务队列）

------------------------------------------
# 设计思路与边界

因此我们需要一个抽象的**消息队列**的概念。当然这个消息队列我们可以有不同的实现:

- Redis
- kafka
- RabbitMQ
- RocketMQ
- MQTT
- ...

反正世界上有的是消息队列的实现，或者以后高兴，自己写一个消息队列咯。

其主要的工作方式是：

- 通过 `cmd_mq` 将消息推入队列
- 通过 `WnMqConsumerSetup` 在系统启动时注册内置消息队列消费者
- 通过 `Ioc` 注册消息队列接口的实现类，以便`cmd_mq`和`WnMqConsumerSetup`取得

并且，消息队列是`Walnut`系统提供的，并没考虑到域用户会扩展。
当然，如果域用户要想让自己的域支持自定义的消息队列，他可以：

- 准备一个自己的消息队列服务
- 自己实现一个命令，譬如 `pushmsg.js` 将消息推入队列
- 自己实现一个服务，作为队列的消费者，并且将消息内容通过[HTTP注册接口][c1-api]传递给自己的域

如果这个场景比较常见，可以考虑将来增加一个`cmd_mqx` 或者
增强 `cmd_mq` 让其支持自定义的消息队列推送。
当然自定义消息队列的消费，一定是在另外的服务里做的。

--------------------------------------
# 数据结构描述

对应这个消息队列，最重要的就是定义一个消息的数据结构。各个消息队列实现，
只要能承载这个消息的结构即可。

## JSON

```bash
#
# 消息的类型声明了消息的处理方式（必须大写）
# 现在仅支持一种类型 "cmd"，同时也是默认设置
#  - cmd: 消息体是一个命令。多条用换行或者 ; 分隔
type  : "CMD"
#
# 本消息执行时采用的用户权限 nameOrPhoneOrEmail
#
user : "demo"
#
# 这是一个密钥，消息的执行（譬如IO操作）会涉及到权限
# 执行器会将当前线程切换为 user 代表的用户。
# 这必须得验证一下安全性，否则谁都能给消息队列里塞个消息，冒充任何用户执行
# 会验证 ~/.mq/secret 文件的内容，是否与给定消息相符
# 用户可以设置任意长度的密钥，当然，最常见的方法是用
# mq secret 重新生成一个随机的密钥
#
# 在 HTTPAPI　中，采用　mq send 则自然会将消息带上这个密钥
#
secret : "XXXXXXXXXXX.."
# 消息体就是一个字符串
body : "echo  'hello' > ~/abc.txt"
```

## TEXT

```bash
# 支持 '#' 开头的注释行
@type=CMD
@user=demo
@secret=xxxxx
#
# 连续两个空格以后，为 body 的内容
#
echo 'hello' > ~/abc.txt
```

--------------------------------------
# 相关知识点

- [HTTP注册接口][c1-api]

[c1-api]: core-l1/c1-regapi.md