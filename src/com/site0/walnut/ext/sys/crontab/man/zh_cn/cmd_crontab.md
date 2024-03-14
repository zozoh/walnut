# 命令简介 

    `crontab` 用来控制系统的WnCrontabService
    
    默认读取系统的/etc/crontab 文件
    
```
0 * * * * * <root> doback
3 1 * * * * <muting> do any js xx
```

## crontab 文件

与linux的crontab类似,增加了zcron特有的属性

分3部分

zcron表达式 <执行用户> 命令

其中执行用户用尖括弧包起来, 以分隔配置的三部分
   