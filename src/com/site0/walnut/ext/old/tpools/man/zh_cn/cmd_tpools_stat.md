命令简介
======= 

`tpools stat` 查看指定线程池的状态
    

用法
=======
```    
tpools stat [name] # name即线程池的名称
```

示例
=======

```
tpools stat gpsconv
```

如果线程池存在, 输出

```
{
   "active": 0,  # 激活状态的线程数量
   "max": 32,    # 线程池大小
   "completed": 2 # 已经完成的线程池任务总数
}
```

否则输出

```
{
   "msg": "no such pool"
}
```
