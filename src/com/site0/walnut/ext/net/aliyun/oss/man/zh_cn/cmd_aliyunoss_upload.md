命令简介
======= 

`aliyunoss upload` 用于把文件上传到OSS
    

用法
=======

```bash
aliyunoss [$name] upload 
	osspath     # 目标路径, OSS上的路径 
	wnpath      # walnut路径
```

基本用法
========

```bash
# 推送文件, OSS路径 js/jquery/jquery.js, 本地路径~/jquery_local.js
aliyunoss testoss upload js/jquery/jquery.js ~/jquery_local.js
```
