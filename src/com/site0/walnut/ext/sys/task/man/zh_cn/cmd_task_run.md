# 过滤器简介

`@run` 主动执行任务堆栈里的一个或者多个任务。

需要 `root` 组管理员权限才能执行。

# 用法

```bash
task @run [ID] [-u 用户] [-skip N] [-limit N] [-json] [-cqn]
  # ID      # 指定一个任务的 ID
  -u        # 指定任务的所属用户
  -skip     # 跳过多少记录
  -limit    # 最多处理多少记录，默认 0 表示不限制
  -json     # 按 JSON 格式输出内容
  -cqn      # JSON 格式化开关
```

# 示例

```bash
# 主动执行全部后台任务
task @run

# 执行指定任务
task @run 56y1..a8e2
```
