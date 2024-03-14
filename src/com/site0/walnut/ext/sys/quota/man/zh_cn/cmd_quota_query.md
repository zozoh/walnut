# 命令简介

`quota query` 查询用户配额及使用情况

```
quota query (-t (disk|network)) <-u username> <-r (true|false)>
```

* t 类型, disk磁盘, network网络流量
* u 用户名称
* r 是否查询实时数据,默认是false

磁盘空间使用情况,周期性更新

# 用法

```
# 查询当前用户的磁盘配额
>quota query -t disk

{
   used: 61522375,
   quota: 104857600,
   type: "disk",
   realtime: false
}

# 查询当前用户的实时磁盘配额
>quota query -t disk -r true

{
   used: 61522375,
   quota: 104857600,
   type: "disk",
   realtime: true
}


# 查询当前用户的网络配额
>quota query -t disk -r true

{
   used: 61522375,
   quota: 104857600,
   type: "network",
   realtime: false
}
```

