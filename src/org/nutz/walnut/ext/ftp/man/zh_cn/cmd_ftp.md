# 命令简介 

    `ftp` 用来处理与FTP服务器的交互
    
    本命令需要主动向FTP服务器发送请求，则需要读取 `~/.ftp/配置名称` 目录下的 `ftpconf` 文件。
    该文件的格式为:
    
    {
    	host       : 'site0.cn',
    	port       : 2121,
    	username   : 'wendal',
    	token      : 'token', // 通过ftpd passwd命令可生成或查看
    	pathPrefix : '/home/wendal/ftp' // 可限制ftp操作的根路径,可以是null
    }

用法
=======

```
ftp ls           # 列出指定目录
ftp upload       # 将文件上传到ftp服务器
ftp download     # 将ftp服务器上的文件下载到本地
ftp mv           # 将ftp服务器上的文件改名
ftp rm           # 删除ftp服务器上的某个文件或目录
```