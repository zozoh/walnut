---
title: 阿里云整合
author: zozoh
key: f0-aliyun
---

# 域配置信息

```bash
~/.aliyun/        # 阿里云的配置目录
#---------------------------------------
|-- oss/          # OSS 配置目录
|   |-- default.json   # 默认配置
|   |-- abc.json       # 指定名称的配置信息
#---------------------------------------
# 视频点播配置目录
|-- vod/
|   |-- default.json   # 默认配置
|   |-- abc.json       # 指定名称的配置信息
#---------------------------------------
# 更多的阿里云服务配置
| ...
```

# default.json

每个服务下默认的配置文件即为 `default.json` 这个文件通常会存有服务的访问票据

```js
{
  id : "xxx",      // accessKeyId
  secret : "xxx"   // accessKeySecret
}
```

# 命令示意

```bash
# 视频点播服务：获取视频信息
aliyunvod play_info 0083f3e137774a748e2d89a506cd1ef5
```

现在支持的整合命令包括：

 Command    | Description
------------|------------------
`aliyunoss` | OSS 服务
`aliyunvod` | 视频点播服务