# 命令简介 

`cron add` 添加一个定期任务

**本命令如果是非`root`组管理员执行，则添加自己的任务**

# 用法

```bash
cron add
  [WnCron]       # 【必】定期任务表达式
  [-f /path/to]  # 命令内容来自哪个文件，如果不指明则会从标准输入读取
  [-u xxx]       # 指定任务执行的用户，默认就是自己
  [-cqn]         # JSON 格式化参数
```

# 示例

```bash
demo> echo 'date >> ~/tmp/logs.txt' | cron add '0 0 0/3 * * ?'
{
   pid: "oil7s4uo76jkgob6dtj99i6sif",
   ph: "/sys/cron/5hguajbo0igjeofj8cchqfepn5",
   d0: "sys",
   d1: "cron",
   race: "FILE",
   tp: "cron_task",
   cron: "0 0 0/3 * * ?",
   user: "bchc",
   id: "5hguajbo0igjeofj8cchqfepn5",
   nm: "5hguajbo0igjeofj8cchqfepn5",
   ct: 1619878119771,
   lm: 1619878119776,
   mime: "application/octet-stream",
   c: "bchc",
   m: "bchc",
   g: "bchc",
   md: 493,
   sha1: "a5e753aab1e014e733e57f1e4d24fc1a4603ddea",
   len: 23
}
```