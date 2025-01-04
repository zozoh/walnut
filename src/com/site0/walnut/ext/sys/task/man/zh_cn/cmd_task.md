# 命令简介

`task` 用来管理处理系统后台任务堆栈.

详细请参见文档: `c1-cron-task.md`
    

# 用法

```
task [list|run|remove] [options...]
```

它支持的子命令有：

```bash
task list            # 列出任务堆栈里的所有任务
task run             # 主动执行任务堆栈里的一个或者多个任务
task add             # 向堆栈中添加一个任务
task remove          # 删除任务堆栈中的一条或者多条任务
task notify          # 通知后台任务消费线程立即处理后台任务队列
```