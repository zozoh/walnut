命令简介
======= 

`aliyunoss lsdir` 列举oss上的文件(仿文件夹)
    

用法
=======

```bash
aliyunoss [$name] lsdir 
	osspath     # OSS上的路径 
```

基本用法
========

```bash
:> aliyunoss testoss lsdir rs/ti/
["a", "b", "c/"]
```

输出内容是一个json数组, 如果是文件夹,会以/结尾

