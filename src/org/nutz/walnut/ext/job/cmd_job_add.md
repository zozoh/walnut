# 命令简介 

    `job add` 添加任务

用法
=======

```
job add [-Q] [-base64 true] [-cron $cron] [-name name] cmds
```

示例
============

添加一个马上执行的任务, 返回值是job的id

```
>: job add 'touch /root/abc'
7cq04f9lf6i6aof1a8p3vhgq57
```

添加一个计划任务, 每天00:30分发个邮件, "秒,分,时,日,月,年,星期"

```
>: job add -name '定时发邮件' -cron '0 30 * * * * ?' 'email send xxxxx'
7cq04f9lf6i6aof1a8p3vhgq57
```

