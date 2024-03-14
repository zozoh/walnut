命令简介
======= 

`tpools` 线程池操作
    

用法
=======

```
tpools stat   # 查看线程池状态
tpools task   # 添加线程池任务
tpools close  # 关掉指定线程池
tpools list   # 列出已有线程池的名称
```

示例
=======

除list外,所有命令均需要提供线程池名称

```
# 线程池名称gpsconv, 执行的命令为 jsc ~/.jsbin/gps_conv.js id:abcdefge
tpools task gpsconv "jsc ~/.jsbin/gps_conv.js id:abcdefge"
```
