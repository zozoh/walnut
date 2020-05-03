命令简介
======= 

`aliyunoss download` 用于把OSS上的文件下载到walnut
    

用法
=======

```bash
aliyunoss [$name] download
	osspath     # 目标路径, OSS上的路径 
	wnpath      # walnut路径
```

基本用法
========

```bash
# 拉取文件, OSS路径 js/jquery/jquery.js, 本地路径~/jquery_local.js
aliyunoss testoss download js/jquery/jquery.js ~/jquery_local.js
```
