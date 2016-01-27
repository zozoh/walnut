---
title:苦瓜的数据结构
author:zozoh
---


# 苦瓜应用的数据结构

```
$HOME
    #---------------------------------------------
    # 控件的配置目录
    .bp
        component            # 所有的组件目录
            base             # 控件集合，元数据 `title` 表示了集合名称
                text.js
                image.js
            shopx            
                car.js       # 每个 js 就是一个遵守控件规范的模块
        

    #---------------------------------------------
    # 工程目录可以是任何目录
    ..随便什么目录..
        site.json        # 站点配置信息
        theme            # 采用的主题
            current      # 当前主题，通常是个链接目录
            theme.json   # 主题的配置信息
        page
            xxx.bpml     # 一个文件一个页面，元数据 title 表示页面中文名
    
```

# 控件与主题

1. 控件负责产生 HTML
2. 每个控件产生的 HTML 都有且只有一个顶级元素
2. 控件的 HTML 标签有自己的特殊 className 同时也有通用的 className
3. 主题的规范，将详细规范了这些 className 的意义




