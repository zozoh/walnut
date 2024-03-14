命令简介
======= 

`aliyunoss` 用于访问阿里云OSS服务
    

用法
=======

```bash
aliyunoss upload    # 把文件推送到OSS
aliyunoss download  # 从OSS拉取文件
aliyunoss rm        # 删除指定的文件
aliyunoss meta      # 文件的元数据管理
aliyunoss lsdir     # 枚举文件
aliyunoss mkdir     # 创建文件
```

配置文件  ~/.aliyun/oss/$name.json

```js
{
  endpoint : "oss-cn-qingdao.aliyuncs.com", // 本地调试写公网,服务器上写内存
  id : "ABC", // 通过阿里云RAM机制创建 
  secret : "ABCDEFG", // 通过阿里云RAM机制创建 
  bucketName : "testoss" // OSS的bucket名称
}
```

基本用法
========

```bash
# 推送文件, OSS路径 js/jquery/jquery.js, 本地路径~/jquery_local.js
aliyunoss testoss upload js/jquery/jquery.js ~/jquery_local.js
# 拉取文件, OSS路径 js/vue/vue.js, 本地路径~/vue_local.js
aliyunoss testoss download js/vue/vue.js ~/vue_local.js
# 删除文件
aliyunoss testoss rm js/vue2/vue2.js
# 查询文件
aliyunoss testoss lsdir js/
# 建文件夹(非必须)
aliyunoss testoss mkdir js/abc/
```
