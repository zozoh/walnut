# 命令简介 

`cron list` 列出所有的定期任务

**本命令如果是非`root`组管理员执行，则仅会查看自己任务**

# 用法

```bash
cron list
  [ID...]            # 指定一个或者多个任务的 ID
  [-u xxx]           # 仅列出某个用户的任务
  [-skip 0]          # 跳过多少记录
  [-limit 0]         # 最多列出多少记录
  [-content]         # 同时也读取任务的命令内容
  [-json]            # 指定按照 JSON 格式输入出内容
  [-cqn]             # JSON 格式化参数
```

# 示例

```bash
demo$ cron list -t -content
# | id                         | user | cron           | content
--+----------------------------+------+----------------+-----------------------
0 | 4frk0pojhmifqqf1edt66ccold | demo | 0 0 0/6 * * ?  | echo `date` F >> ~/t..
1 | t509rlhv8kjvtqd1rmdm31o9sb | demo | 0 0 0/5 * * ?  | echo `date` E >> ~/t..
2 | 1ijl1jqv6ki99qe1o5b1mip4q9 | demo | 0 0 0/4 * * ?  | echo `date` D >> ~/t..
3 | 6eo89bu8i2hlop9l6ki2mgip19 | demo | 0 0 0/3 * * ?  | echo `date` C >> ~/t..
4 | t3763h8ceoh59p8kcie4pr8ni3 | demo | 0 0 0/2 * * ?  | echo `date` B >> ~/t..
5 | 4leuorl9icjgvqut1h3q0634ah | demo | 0 0 0/1 * * ?; | echo `date` A >> ~/t..
--+----------------------------+------+----------------+-----------------------
total 6 items
```