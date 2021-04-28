# 命令简介

`schedule` 管理系统的分钟计划表

详细请参见文档: `c1-cron-schedule.md` 

# 用法

```
schedule [list|load|clean] [options...]
```

它支持的子命令有：

```bash
schedule list        # 列出所有的分钟计划任务（非root组成员仅能看自己的任务）
schedule load        # 将定期任务表的任务加载分钟计划表（需指定一个日期）
schedule clean       # 主动清除过期的分钟计划
```