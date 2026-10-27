---
title: cron 命令与任务定义
module: 60.sched-async
keywords: [cron, cron_task, 定期任务, /sys/cron, cron add, cron preview]
source: [ext/sys/cron/]
related: [README, 20-cron-syntax, 30-schedule-slot, 40-task-queue]
---

# cron 命令与任务定义概述

`cron` 是定时任务的入口命令，负责管理 `/sys/cron/` 目录下的定期任务对象。
日常使用中，你只需要掌握这一个命令，后面的调度、推送、执行都由系统自动完成。

## 一个任务对象长什么样

每个定期任务就是一个普通对象（`tp:"cron_task"`），关键信息有两处：

```json5
{
  // 元数据
  "tp": "cron_task",
  "cron": "0 30 8 * * ?",   // WnCron 表达式，描述「什么时候跑」
  "user": "zozoh"           // 以哪个用户的身份执行
}
```

文件的内容就是要执行的命令模板，例如：

```bash
jsc ~/jobs/daily_report.js
```

## 常用操作

```bash
# 列出任务（非 root 用户只能看到自己的）
cron list

# 添加任务：从 stdin 读命令
echo 'o ~/data -l | wc -l >> ~/stats.txt' | cron add '0 30 8 * * ?'

# 添加任务：命令来自文件
cron add 'D[20261101,20261112) T{08:00}' -f ~/jobs/daily.sh

# 预览任务未来会在哪些分钟执行（离线验证，不用等真跑）
cron preview <id>

# 解释一个表达式（不用创建任务也能看懂表达式的含义）
cron explain '0 0/20 8-18 * * ?'

# 删除任务
cron remove <id>
```

`-f` 参数让命令模板来自文件，适合比较长的脚本；不加 `-f` 时命令从标准输入读取。

## 权限

`cron list` 等查询操作会自动按当前登录用户过滤：你只能看到自己名下的任务，
`root` 可以看到并管理全部任务。

## 执行身份

任务执行时以对象上的 `user` 字段作为身份。这个字段在添加任务时确定，
写命令模板时要想清楚：这个用户的 HOME 是什么、有没有目标目录的写权限。

## 注意事项

- 表达式语法参见 20-cron-syntax.md，这里不展开。
- `cron preview` 是调试的好帮手：写完表达式先预览，确认命中了你想的那些分钟。
- 表达式可以限定日期区间（如 `D[20261101,20261112)`），到期后任务自然不再被调度，
  适合「只跑一段时间」的一次性需求。
