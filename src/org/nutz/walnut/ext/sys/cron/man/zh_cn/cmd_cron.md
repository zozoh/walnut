# 命令简介

`cron` 用来管理处理系统定期任务列表.

详细请参见文档: `c1-cron-task.md`
    

# 用法

```
cron [list|add|remove|preview] [options...]
```

它支持的子命令有：

```bash
cron list        # 列出所有的定期任务（非root组成员仅能看自己的任务）
cron add         # 添加一个定期任务
cron remove      # 删除一个定期任务
cron preview     # 预览指定定期任务的计划表
```