# 过滤器简介

`@remove` 删除任务堆栈中的一条或者多条任务。

需要 `root` 组管理员权限才能执行。

# 用法

```bash
task @remove [ID...] [cron|task] [-u 用户] [-skip N] [-limit N] [-json] [-cqn]
  # ID          # 指定一个或多个任务的 ID
  # cron|task   # 指定清除的任务类型，不给则针对全部类型
  -u            # 指定任务的所属用户
  -skip         # 跳过多少记录
  -limit        # 最多处理多少记录，默认 0 表示不限制
  -json         # 按 JSON 格式输出内容
  -cqn          # JSON 格式化开关
```

# 示例

```bash
# 删除指定任务
task @remove 56y1..a8e2
```
