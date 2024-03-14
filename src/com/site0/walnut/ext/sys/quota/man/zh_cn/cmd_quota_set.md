# 命令简介

`quota set` 设置用户配额

```
quota set (-t (disk|network)) <-u username> 1024000
```

* t 类型, disk磁盘, network网络流量
* u 用户名称
* 必须带配额信息,单位字节

必须具有管理员权限的用户才允许设置配额, root用户永无配额限制

# 用法

```
# 设置用户的磁盘配额
>quota set -t disk 1024000000

# 设置用户的流量配额
>quota set -t network 1024000000
```

