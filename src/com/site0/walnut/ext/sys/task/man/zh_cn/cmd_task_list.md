# 过滤器简介

`@list` 列出任务堆栈里的所有任务。

非 `root` 组管理员执行时，仅会查看自己的任务。

# 用法

```bash
task @list [ID...] [cron|task] [-u 用户] [-skip N] [-limit N] [-json] [-cqn]
  # ID          # 指定一个或多个任务的 ID
  # cron|task   # 指定要列出的任务类型，不给则针对全部类型
  -u            # 仅列出某个用户的任务
  -skip         # 跳过多少记录
  -limit        # 最多列出多少记录
  -json         # 按 JSON 格式输出内容
  -cqn          # JSON 格式化开关
```

# 示例

```bash
# 列出全部任务
task @list

# 按 JSON 输出
task @list -json -cqn
```
