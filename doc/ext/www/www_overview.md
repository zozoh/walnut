---
title:www网页机制
author:zozoh
tags:
- 系统
- 扩展
- www
---

# www 发布目录

系统可以指定任意一个目录为 www 的发布目录，目录的 `www` 元数据指定浏览器访问的路径

* 标记了元数据 *www* 的目录为发布目录，它可以被浏览器直接访问
* *www* 目录可存放
    - 网页模板 : *tp=wnml* - 会被解析渲染
    - 静态网页 : *tp=html* - 直接输出
    - 各种图片等媒体资源
    - js 文件
    - css 文件
    - 其他任何文件
* 与 *httpapi* 配合，可以让网页获得更丰富的动态数据

# www 目录的映射

一个标记了 *www* 元数据的发布目录，元数据的样子是:

```
# 指明本目录映射的域名。 ROOT 表示映射任何域名
www : "ROOT"

# 指明目录下默认的主页，默认为 ["index.wnml", "index.html"]
www_entry  : ["index.wnml", "index.html"]
```

WWWModule 在处理请求的时候，采用的策略如下 

```
查看请求对象是否有属性 "www_host"，如果存在比如值为 "demo.walnutos.com"
那么则会去寻找 www='demo.walnutos.com' 的发布目录

如果没有属性 www_host 或者改发布目录不存在，则试图寻找 www="ROOT" 的发布目录

如果找到了发布目录，则会根据目录寻找 Path 对应的文件对象

没找到，就 404 咯
```

