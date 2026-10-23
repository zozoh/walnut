# 过滤器简介

`@preview` 预览指定定期任务的计划表。

非 `root` 组管理员执行时，只能预览自己的任务。

# 用法

```bash
cron @preview [ID...] [-u 用户] [-skip N] [-limit N] [-today 日期] [-empty] [-slot N] [-cqn]
  # ID      # 指定一个或多个任务的 ID
  -u        # 仅预览某个用户的任务
  -skip     # 跳过多少记录
  -limit    # 最多预览多少记录
  -today    # 指定预览日期，默认是当天（now）
  -empty    # 空时间槽也要显示
  -slot     # 时间槽的数量，默认 24 表示 24 小时；精度到分钟应为 1440
  -cqn      # JSON 格式化开关
```

# 示例

```bash
# 预览今天的任务时间分布
cron @preview

# 空槽也显示，精度到分钟
cron @preview -empty -slot 1440
```
