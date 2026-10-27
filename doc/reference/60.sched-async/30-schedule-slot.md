---
title: 分钟计划表 /sys/schedule
module: 60.sched-async
keywords: [schedule, schedule_task, 分钟槽, /sys/schedule, schedule load, schedule push]
source: [ext/sys/schedule/]
related: [README, 10-cron-task, 40-task-queue]
---

# 分钟计划表 /sys/schedule 概述
`/sys/schedule/` 是 cron 表达式和任务执行之间的「展开结果」缓存。
预加载线程把 `/sys/cron/` 里的任务按表达式展开成一个个**分钟槽**，
写入这里；推送线程每分钟把当前槽的任务推入 `/sys/tasks/` 并删除该槽。

展开 cron 表达式是昂贵的操作，而「取出当前分钟的任务」应该非常廉价——
这就是把计划表独立出来的原因。

## 分钟槽对象

一天被切成 1440 个分钟槽（`slot` 取值 0–1439），每个槽是一个对象：

```json5
{
  // 元数据
  "slot": 600,              // 分钟槽：10:00
  "date": 1760992800000,    // 槽对应的日期时间（毫秒）
  "expi": 1761079200000     // 过期时间 = 槽时间 + 1 天，供自动清理
}
```

槽对象的**内容**就是该分钟要执行的命令副本（连同执行身份一起从任务对象复制过来）。

另外还有一份按天组织的索引文件 `index/<yyyyMMdd>.schedule.index.txt`，
记录某天有哪些分钟槽，`expi` 为 2 天，用于快速判断「这一天有没有计划、
有多少计划」而不用遍历目录。

## 后台线程

- **预加载（`BGT_CRON`）**：每轮取出系统全部 cron 任务（上限 1000 个），
  从当前槽开始向前展开 `sys-cron-preload-amount` 个槽（默认 5），写入计划表；
  然后睡到已加载的最后一个槽开始时醒来，进入下一轮。
- **推送（`BGT_SCHEDULE`）**：每分钟醒一次，取当前分钟槽，
  把槽里的任务推入 `/sys/tasks/`（推完即删除槽对象，
  并带一点半槽内的随机抖动，避免多节点同时推送）。

两个线程都受 `sys-schedule-enabled` 配置控制（默认开启）。

## 手动操作命令

正常情况下这两个线程全自动工作，不需要人工介入。系统也提供了
`schedule` 命令供排查和干预（多数需要 root）：

```bash
schedule load 20261021      # 手动展开某天的计划（调试表达式用）
schedule list               # 查看计划表内容
schedule push 600           # 手动把 10:00 槽的任务推入任务栈
schedule clean              # 清理过期计划（root）
schedule awake              # 唤醒推送线程立即工作（root）
```

## 多节点与锁

多节点部署时，同一分钟只允许一个节点做 `load/push`。
锁服务由 IoC 提供（`sys-schedule-lockdu` 配置锁时长，默认 60 秒），
必须确保锁服务是 Redis 分布式锁，否则多个节点会把同一个任务重复推入任务栈。
