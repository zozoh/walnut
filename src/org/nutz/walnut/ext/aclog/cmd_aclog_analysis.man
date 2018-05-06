命令简介
======= 

`aclog analysis` 分析访问日志,输出报表
    

用法
=======

```
aclog analysis [-du (10m|1h|12h|1d|3d|7d)] [-group (ua,ip,rc)] [-from XXX] [-to XXX] [-format (md|json)]
```

### 参数

- du 区间长度,支持 分(m) 时(h) 日(d) 等
- group 分组字段,默认是 ua,host,rc
- from 开始时间,优先于du参数
- to 结束时间,默认是当前时间
- ip 远程ip前缀
- host 指定host
- ua 指定ua前缀
- format 输出格式,默认是md(markdown), 还可以是json

参数均可选