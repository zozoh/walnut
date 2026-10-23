# 过滤器简介

`@add` 添加一个定期任务。

非 `root` 组管理员执行时，添加的是自己的任务。

# 用法

```bash
cron @add 任务表达式 [-f 路径] [-u 用户] [-cqn]
       # 任务表达式    # 定期任务表达式（如 '0 0 0/3 * * ?'）
  -f        # 命令内容来自哪个文件；不指明则从标准输入读取
  -u        # 指定任务执行的用户，默认是自己
  -cqn      # JSON 格式化开关
```

# 示例

```bash
# 从管道读命令内容，添加定时任务
echo 'date >> ~/tmp/logs.txt' | cron @add '0 0 0/3 * * ?'
# {
#    pid: "oil7s4uo76jkgob6dtj99i6sif",
#    ph: "/sys/cron/5hguajbo0igjeofj8cchqfepn5",
#    tp: "cron_task",
#    cron: "0 0 0/3 * * ?",
#    user: "bchc",
#    ...
# }
```
