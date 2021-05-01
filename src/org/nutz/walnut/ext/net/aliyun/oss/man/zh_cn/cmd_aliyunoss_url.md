命令简介
======= 

`aliyunoss genurl` 获取临时访问URL
    

用法
=======

```bash
aliyunoss [$name] genurl 
	osspath     #OSS上的路径 
	-t $timeout # URL有效期,默认是10分钟有效
```

基本用法
========

创建一个临时url,用于访问私有bucket上的文件

```bash
:> aliyunoss testoss genurl test/abc.txt
http://testabc.oss-cn-beijing.aliyuncs.com/test/abc.txt?Expires=1588514819&OSSAccessKeyId=LTAI4GCKxgprwNsKvpobvyXD&Signature=cXU7jnCTaZKevyqUpXcnFTUONrg%3D

:> aliyunoss testoss genurl test/abc.txt | curl
abc # 生成url后通过curl访问得到abc.txt的内容"abc"
```
