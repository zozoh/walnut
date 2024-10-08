# 命令简介 

`schedule load` 将定期任务表的任务加载分钟计划表（需指定一个日期）

**!!!本命令需要`root`组管理员权限才能执行**

# 用法

```bash
schedule load
  [ID...]            # 【选】指定一个或者多个定期任务的 ID
                     # 如果不指定，就加载系统中所有的定期任务
  [-today today]     # 要加载哪天的计划，默认是当天
  [-slot now]        # 要加载哪个时间槽，可以是下标，也可以是一个时间
                     #  0-1399 : 时间槽下标
                     #  14:00  : 直接指定一个时间对应的分钟槽
                     #  now+1h : 从现在开始 1 小时以后的那个分钟槽
                     #  now+1m : 从现在开始 1 分钟以后的那个分钟槽
                     #  now+1s : 从现在开始 1 秒钟以后的那个分钟槽
                     # 默认为 now 表示当前时间槽
  [-amount 3]        # 从开始时间槽开始，加载多少个时间槽，默认 3 个
  [-u xxx]           # 仅针对某个用户的任务
  [-skip 0]          # 跳过多少记录
  [-limit 0]         # 最多列出多少记录
  [-force]           # 是否强制加载。否则，如果已经加载过了就不加载了
  [-json]            # 指定按照 JSON 格式输入出内容
  [-cqn]             # JSON 格式化参数
```

# 示例

```bash
```