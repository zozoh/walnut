命令简介
======= 

`pools task` 添加线程池任务
    

用法
=======

```
pools task [name]          #线程池名称, 如果线程池不存在,自动新建
           ["jsc xx xxx"]  # 命令字符串
           ["-wait 15000"] # 等待时长,单位毫秒,默认不等待
```

示例
=======

```
# 线程池名称gpsconv, 执行的命令为 jsc ~/.jsbin/gps_conv.js id:abcdefge
pools task gpsconv "jsc ~/.jsbin/gps_conv.js id:abcdefge"
```

无输出.
