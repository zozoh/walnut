---
title: 定时任务调度流水线
module: 60.sched-async
keywords: [cron_task, schedule_task, task, 分钟计划表, /sys/cron, /sys/schedule, /sys/tasks, 后台任务]
source: [ext/sys/cron/, ext/sys/schedule/, ext/sys/task/]
related: [cron-syntax, mq, hooks, notify]
---

# 定时任务调度流水线

定时任务采用**三张表 + 两条锁 + 一条消费线程**的流水线，源码在 `ext/sys/cron|schedule|task`。调度精度为 **1 分钟**。

## 三张表

```text
/sys/cron/       cron_task    定期任务定义（内容=命令模板）
/sys/schedule/   schedule_task 分钟计划表（按分钟槽 0-1439 组织）
/sys/tasks/      task          后台任务栈（待执行队列）
```

### 1. /sys/cron —— 任务定义

每个对象是一个定期任务（`tp:cron_task`），关键元数据：

- `cron`：WnCron 表达式（见 表达式）；
- `user`：执行身份；
- **文件内容**就是要执行的命令模板。

### 2. /sys/schedule —— 分钟计划表

把 `cron` 表达式在某天展开成分钟槽：

- `schedule load <日期>` 把当天所有 cron_task 按表达式展开，写入分钟槽；
- 索引文件：`index/<yyyymmdd>.schedule.index.txt`（某天的分钟槽索引）；
- `schedule push <分钟槽>` 把该槽任务推入任务栈；
- `schedule clean`（root）清理过期计划；`schedule awake`（root）唤醒重推。

### 3. /sys/tasks —— 后台任务栈

待执行队列，由后台消费线程（`BGT_*`）逐个取出执行：

- `task add` 手动添加（指定 user + 命令）；
- `task run` 主动执行栈中任务；
- `task remove` / `task list`；
- `task notify` 通知消费线程立即处理。

## 命令速查

```bash
# cron：管理定期任务
cron list                              # 列任务（非 root 只见自己的）
cron add '0 0 0/3 * * ?'               # 从 stdin 读命令，添加任务
echo 'o ~/data -l | wc -l >> ~/stats.txt' | cron add '0 0 0/3 * * ?'
cron add 'D[20261101,20261112) T{08:00}' -f ~/jobs/daily.sh   # 命令来自文件
cron remove <id>
cron preview <id>                      # 预览该任务未来哪些分钟会跑
cron explain '0 0/20 8-18 * * ?'       # 解释表达式

# schedule：分钟计划表
schedule load 20261021                 # 加载某天的计划
schedule list                          # 列出计划
schedule push 600                      # 推 10:00 槽的任务入栈
schedule clean                         # root：清过期计划
schedule awake                         # root：唤醒重推

# task：任务栈
task list
task add -u zozoh 'mailx default @to a@b.c @sub hi'
task run <id...>
task remove <id...>
task notify
```

## 执行时序

```text
T-1 分钟      schedule load <今天>     展开 cron → 分钟槽
每整分        schedule push <当前槽>   槽内任务 → /sys/tasks
持续          BGT_ 消费线程            逐条取出执行（以任务 user 身份）
```

用 `cron preview` 可以离线验证「这个任务什么时候跑」，不必等真跑。

## 两条锁

为防止多节点/多线程重复推进：

- **分钟计划锁**：同一分钟只允许一个节点做 `load/push`；
- **任务栈锁**：取任务执行时互斥。

锁服务由 IoC 提供（单机内存锁或 Redis 分布式锁，见 部署），多节点部署时必须用 Redis 锁，否则任务会被重复执行。

## 典型场景

- **日报推送**：`cron add 'T{08:30}'`，命令里查数据 + `mailx @tmpl`；
- **数据清理**：`cron add 'T{03:00}'`，命令里 `o -del` 过期对象；
- **一次性未来任务**：cron 表达式限定日期区间，到期自动失效；
- **手动补跑**：`task add` 直接入栈。

## 注意事项

- 精度 1 分钟，秒级需求请用 消息队列 或自己轮询；
- 任务以 `cron` 对象上的 `user` 身份执行，注意该用户的目录与权限；
- 命令模板里避免长时间阻塞的外部调用，给 `httpc @timeout` 之类设好超时；
- 非 root 用户只能管理自己的任务（`cron list` 自动过滤）。

## 延伸阅读

- WnCron 表达式
- 钩子 — 任务命令触发的钩子联动
- 通知 — 任务失败的提醒通道
