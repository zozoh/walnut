# 命令简介 

    `ftp download` 用来下载ftp服务器上的文件到本地

用法
=======

```
ftp [confName] downlaod [source] [target]
```

其中source是ftp服务器上的路径, target是walnut内的路径

示例
=======

```
#> ftp hopeweb download /home/wendal/www/backup.zip ~/what.zip
```