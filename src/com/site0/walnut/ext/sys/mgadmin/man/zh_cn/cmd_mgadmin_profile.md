# 命令简介 

    `mgadmin profile` 用来管理性能监控器

用法
=======

```
mgadmin profile
     [enable]         # 开启记录
     [disable]        # 关闭记录
     [-quiet]         # 开启和关闭记录时，不要输出
     [list]           # 输出记录
     [-limit 10]      # list 模式下，最多输出多少条记录，默认 10
     [-cqn]           # 当 list 模式输出的时候，JSON 列表的格式化方式
     [-level 1]       # 记录的级别：0:不记录，1:仅慢操作，2:所有操作（估计你基本上用不上）
     [-slowms 100]    # 超过多少毫秒操作算慢操作
```
示例
=======

查看状态, was的值对应level

```
#> mgadmin profile
{ "was" : 0, "slowms" : 100, "ok" : 1.0 }
```

开启,默认level=1,slowms=100,仅记录超过100ms的慢操作

```
#> mgadmin profile enable -level 1 -slowms 100
{ "was" : 0, "slowms" : 100, "ok" : 1.0 }
```

关闭

```
#> mgadmin profile disable
{ "was" : 1, "slowms" : 100, "ok" : 1.0 }
```

列出记录

```
#> mgadmin profile list
```