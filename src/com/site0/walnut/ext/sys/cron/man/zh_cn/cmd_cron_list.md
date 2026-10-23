# 过滤器简介

`@list` 列出所有的定期任务。

非 `root` 组管理员执行时，仅会查看自己的任务。

# 用法

```bash
cron @list [ID...] [-u 用户] [-skip N] [-limit N] [-content] [-json] [-cqn] [-t ...]
  # ID      # 指定一个或多个任务的 ID
  -u        # 仅列出某个用户的任务
  -skip     # 跳过多少记录
  -limit    # 最多列出多少记录
  -content  # 同时也读取任务的命令内容
  -json     # 按 JSON 格式输出内容
  -t        # 表格输出
  -cqn      # JSON 格式化开关
```

# 示例

```bash
# 以表格列出全部任务（含命令内容）
cron @list -t -content
# | id                         | user | cron           | content
# 0 | 4frk0pojhmifqqf1edt66ccold | demo | 0 0 0/6 * * ?  | echo `date` F >> ~/t..
# ...
# total 6 items

# 按 JSON 输出
cron @list -json -cqn
```
