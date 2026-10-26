---
title: 后台任务栈 /sys/tasks
module: 60.sched-async
keywords: [task, 后台任务, /sys/tasks, task add, task run, task notify]
source: [ext/sys/task/]
related: [README, 30-schedule-slot]
---

# 后台任务栈 /sys/tasks

`/sys/tasks/` 是系统统一的后台任务执行队列。定时任务被推送后落到这里，
其他模块投递的后台作业、用户手动 `task add` 补跑，最终也都是进入这个目录，
由同一个消费线程取出执行。

## 任务对象

```json5
{
  // 元数据
  "user": "zozoh"    // 执行身份
}
```

文件内容就是要执行的命令。

## 消费线程（BGT_TASK）

系统启动时 `WnSetup` 总是拉起 `BGT_TASK` 线程，它的循环逻辑是：

1. 批量弹出任务（每批上限 1000 个）；
2. 为**每个**任务 `new` 一个执行线程，以任务 `user` 身份运行命令，
   执行完再 `join` 回收；
3. 如果队列是空的，休眠 60 秒，等待新任务。

> ⚠️ 弹出即删除：任务被弹出的同时就从 `/sys/tasks/` 删除了。
> 如果执行过程中进程崩溃，这个任务不会自动重试
> （源码留有 TODO，考虑为失败任务增加重试）。请让命令幂等，
> 或容忍偶尔丢失一次。

> ⚠️ 源码中曾有一个真正的线程池实现，目前被注释掉，临时用「每任务一线程」
> 代替，注释里标记了 TODO 计划改用虚拟线程。这对使用者透明，
> 但如果你的机器任务密集，值得知道当前每个任务至少占用一个线程。

`task notify` 可以唤醒正在休眠的消费线程，让它不必等到 60 秒后再处理。

## 手动操作命令

```bash
task list                          # 查看任务栈
task add -u zozoh 'mailx default @to a@b.c @sub hi'   # 手动放入任务
task run <id...>                   # 主动执行指定任务
task remove <id...>                # 移除任务
task notify                        # 唤醒消费线程
```

## 多节点与锁

多节点部署时，弹出任务需要互斥锁（IoC 中 `sys-schedule-task-lockdu`
配置锁时长，默认 10 秒）。请使用 Redis 分布式锁，否则同一个任务
可能被多个节点取出并重复执行。
