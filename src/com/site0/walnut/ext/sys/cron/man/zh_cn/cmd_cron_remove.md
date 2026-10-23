# 过滤器简介

`@remove` 删除定期任务列表的一条或者多条记录。

非 `root` 组管理员执行时，只能删除自己的任务。

# 用法

```bash
cron @remove [ID...] [-u 用户] [-skip N] [-limit N] [-json] [-cqn] [-t ...]
  # ID      # 指定一个或多个任务的 ID
  -u        # 仅删除某个用户的任务
  -skip     # 跳过多少记录
  -limit    # 最多删除多少记录
  -json     # 按 JSON 格式输出被删除的内容
  -t        # 表格输出
  -cqn      # JSON 格式化开关
```

# 示例

```bash
# 移除自己全部的定时任务
cron @remove -t
# | id                         | user | cron           | content
# 0 | 4frk0pojhmifqqf1edt66ccold | demo | 0 0 0/6 * * ?  |
# ...
# total 6 items

# 移除指定的定时任务
cron @remove rbnjs3ngf2hn7qe277vn08i1lk 714ic6a91cin2q740pke84ivvk -t
```
