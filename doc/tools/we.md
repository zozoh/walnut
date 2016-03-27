---
title: 包管理
author: wendal
---

场景
===========================

类似于maven或docker的中央仓库, 提供软件包下载,更新,上传,分享服务

版本号
==========================

版本号由2部分组成, 主版本号 和 时间戳(yyyyMMddHHmmss) 中间用英文横杠相连.

服务结构
===========================


```
v1 -- 用户名 -- 软件包 -- 版本号 -- we.json文件+项目文件
```

示例结构

```
v1 
  -- zozoh
       -- walnut
             -- lastest // 最新版本,文本文件
             -- 1.0-201603251950
                    -- we.json
                    -- walnut-1.0-201603251950.zip
  -- wendal
       -- nutzbook
             -- lastest // 最新版本,文本文件
             -- 2.7-201603252000
                    -- we.json
                    -- nutzbook-2.7-201603252000.war
      
```

下载
===========================

```
we get --version=lastest zozoh/walnut
we pull  zozoh/walnut
```

we -- 命令名
pull -- 拉取最新版命令
get -- 获取指定版本号的软件包
zozoh/walnut -- 用户名及软件包

先检查对应项目的lastest文件,然后与本地版本进行对比后下载

上传
===========================

```
we push --version=2.0 zozoh/walnut xxx.war we.json
```

we -- 命令名
pull -- 拉取命令
zozoh/walnut -- 用户名及软件包
xxx.war we.json -- 需要上传的文件,如果不包含we.json,则自动生成一个
若version未提供,则使用1.0,若版本号不包含横杠,则自动添加横杠及时间戳


检索
=============================

用于查找匹配指定条件的项目

```
we search xxx
```

运行
=============================

运行方式当前分2种, nutzweb和war, 通过web.json指定

nutzweb -- 软件包应该是一个压缩包,会解压到指定目录,然后通过web.properties进行启动
war -- 软件包的主为war, 通过tomcat/jetty启动,或deploy到指定tomcat/jetty实例

运行环境
============================

运行目录 -- 主要指nutzweb方式下的解压目录
运行时配置 -- 额外的classpath路径等,一般用于存在于本地环境相关的配置文件