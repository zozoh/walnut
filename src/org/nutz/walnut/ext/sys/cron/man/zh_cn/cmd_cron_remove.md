# 命令简介 

`cron remove` 删除定期任务列表的一条或者多条记录

**本命令如果是非`root`组管理员执行，则删除自己的任务**

# 用法

```bash
task remove      
  [ID...]            # 指定一个或者多个任务的 ID
  [-u xxx]           # 仅列出某个用户的任务
  [-skip 0]          # 跳过多少记录
  [-limit 0]         # 最多列出多少记录
  [-json]            # 指定按照 JSON 格式输入出内容
  [-cqn]             # JSON 格式化参数
```

# 示例

```bash
#
# 移除自己全部的定时任务
#
demo$ cron remove -t
# | id                         | user | cron           | content
--+----------------------------+------+----------------+--------
0 | 4frk0pojhmifqqf1edt66ccold | demo | 0 0 0/6 * * ?  | 
1 | t509rlhv8kjvtqd1rmdm31o9sb | demo | 0 0 0/5 * * ?  | 
2 | 1ijl1jqv6ki99qe1o5b1mip4q9 | demo | 0 0 0/4 * * ?  | 
3 | 6eo89bu8i2hlop9l6ki2mgip19 | demo | 0 0 0/3 * * ?  | 
4 | t3763h8ceoh59p8kcie4pr8ni3 | demo | 0 0 0/2 * * ?  | 
5 | 4leuorl9icjgvqut1h3q0634ah | demo | 0 0 0/1 * * ?; | 
--+----------------------------+------+----------------+--------
total 6 items

#
# 指定移除指定的定时任务
#
demo$ cron remove rbnjs3ngf2hn7qe277vn08i1lk 714ic6a91cin2q740pke84ivvk -t
# | id                         | user | cron          | content
--+----------------------------+------+---------------+--------
0 | rbnjs3ngf2hn7qe277vn08i1lk | demo | 0 0 0/5 * * ? | 
1 | 714ic6a91cin2q740pke84ivvk | demo | 0 0 0/2 * * ? | 
--+----------------------------+------+---------------+--------
total 2 items
```