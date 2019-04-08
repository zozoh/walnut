# 命令简介 

    `ftp upload` 用来把本地文件上传到ftp服务器

用法
=======

```
ftp [confName] upload [source] [target]
```

其中source是walnut内的路径, target是ftp服务器上的路径

示例
=======

```
#> ftp local upload  ~/what.zip /home/wendal/www/backup.zip
```