---
title: 通知与提醒
module: 60.sched-async
keywords: [通知, 短信, 邮件, task notify, schedule awake, 告警]
source: [ext/sys/task/, ext/sys/schedule/, ext/net/]
related: [mq, cron-pipeline, ../70.integrations/sms-mail.md]
---

# 通知与提醒

> ⚠️ **历史变更**：旧版 Walnut 有一套基于 `/sys/noti` 的通知队列（`noti` 命令 + `noti_st` 状态机），**该机制已从源码移除**（参考文档目录里相应章节已标记删除）。当前版本的通知通过「命令直发」+「调度唤醒」实现，本文描述现状。

## 当前做法：任务里直接发通知

通知没有专门的队列层，发送者（cron 任务、API、钩子）在命令里直接调用发送命令：

```bash
# 任务失败给自己发邮件
echo 'o /data/orders -match "{st:0}" -l > ~/report/today.txt \
  || mailx default @to me@example.com @sub "订单导出失败" @content "请检查"' \
  | cron add 'T{08:30}'

# 异常短信告警
o ~/logs -match '{lv:"ERROR", lm:{$gt:昨夜时间戳}}' -l \
  | wc -l | @test '>10' && sms send -r 13800001111 系统错误数超过10条
```

可用的发送通道：

- `sms` — 短信（见 复杂命令）；
- `mailx` — 邮件（含模板邮件）；
- `weixin tmpl -send` — 微信模板消息（见 微信）；
- 任意自建命令 — 比如写 WebSocket 给前端推送（`ext/sys/websocket/`）。

## 调度侧的唤醒通知

调度系统内部有两个「通知原语」，用于线程间协作（不是业务通知）：

```bash
task notify     # 通知后台任务消费线程：有新任务了，立即处理（notifyForNewTaskComing）
schedule awake  # root：唤醒重新推入新的定期任务
```

后台消费线程在没有任务时挂起等待，`task add` 后调 `task notify` 可避免轮询延迟。

## 推荐的告警模式

```bash
# 包装一个可复用的告警脚本 ~/bin/alert.sh
mailx default @to $ALERT_TO @sub "$1" @content "$2"
sms send -r $ALERT_PHONE "$1"

# 所有 cron 任务统一调用
echo '~/bin/alert.sh "订单任务失败" "$(cat ~/logs/order.err)"' ...
```

要点：

- **谁发**：任务以 cron 对象的 `user` 身份执行，确保该用户配好 `.sms` / `.mail` 配置；
- **发给谁**：告警目标写在脚本的环境变量里，改一处全局生效；
- **防轰炸**：告警脚本里先 `lock @try alert` 限频，或记录上次告警时间戳做冷却。

## 延伸阅读

- 定时任务
- 消息队列 — 通知量大时先投 mq 再批量发
- 集成：短信与邮件
