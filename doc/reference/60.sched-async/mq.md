---
title: 消息队列
module: 60.sched-async
keywords: [mq, 消息队列, CMD 消息, secret, 消费者, WnMqApi]
source: [ext/sys/mq/]
related: [cron-pipeline, hooks, notify]
---

# 消息队列

`mq` 是 Walnut 的系统消息队列，用于异步执行命令（fire-and-forget）。源码在 `src/com/site0/walnut/ext/sys/mq/`（`WnMqApi`、`WnMqMessage`、`cmd_mq`，以及可选的 rocketmq 后端）。

## 消息格式

`mq send` 接受三种形式：JSON、TEXT、FILE（`<-` 开头），不给参数时从管道读：

```bash
# JSON 形式：type 默认 CMD，还有 user / secret / body / data
mq send '{type:"CMD", body:"echo xx > ~/xx.txt"}'

# TEXT 形式：@key=val 头若干行，空行后是正文
mq send '@user=demo\n\necho xx > ~/xx.txt'

# FILE 形式：消息内容来自文件对象
mq send '<-id:xxx' '<-~/path/to/msg.txt'

# 指定主题（默认 sys）
mq send -t mytopic '{body:"o ~/data -del"}'
```

`WnMqMsgType` 目前的核心类型是 `CMD`——消费方取出 `body` 作为命令执行。

## 密钥：mq secret

向队列发消息需要密钥（防止伪造）：

```bash
mq secret    # 为当前用户生成密钥，存 ~/.mq/secret
```

HTTPAPI 场景下系统会自动携带密钥；JS/Java 里通过 `WnMqApi` 发送时也要带对应用户的 secret。

## 消费侧

消费由 `WnMqConsumerSetup`（IoC 装配）注册的消费者处理。部署方可以：

1. 用内置消费者执行 CMD 消息；
2. 自建命令消费，配合 cron 轮询或自定义线程；
3. 用 RocketMQ 后端做集群消费（`ext/sys/mq/rocketmq/`）。

## 典型场景

- **API 异步化**：接口收到请求后立即返回，重活 `mq send` 丢给后台；
- **批量通知**：任务脚本里循环 `mq send` 投递通知命令；
- **削峰**：大促时前端动作只入队，消费者按自己速度执行。

```bash
# regapi 里异步处理示例
echo '{"ok":true}'
mq send '{body:"o /data/orders -match \"{st:0}\" -l | sheet -tpo xls -out ~/report/today.xls"}'
```

## 注意事项

- 消息体里的命令以**入队用户的身份**执行，注意该用户权限；
- `secret` 文件丢失会导致发消息失败，可重新 `mq secret` 生成；
- mq 只管投递不保证顺序；需要顺序的场景用 [任务栈](cron-pipeline.md) 或锁。

## 延伸阅读

- [定时任务流水线](cron-pipeline.md)
- [钩子](hooks.md)
- [复杂命令](../40.commands/02_cmd_complex.md) — `mq` 命令索引
