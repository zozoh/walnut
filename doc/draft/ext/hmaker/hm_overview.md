---
title  : hMaker 总体设计
author : zozoh
tags:
- 扩展
- hmaker
---


# hMaker 的数据结构

```
#######################################################
# 配置
~/.hmaker                    # hMaker 的配置目录
    .cache                   # 缓存目录
         skin                # 皮肤的预编译缓存
            myskin.css       # 对于 default 皮肤的less编译缓存
                             # 会记录指纹（和ETag同理）
                             # 以便 LesscService 不重复编译
    setup
        skin
            myskin_var.less  # 皮肤的用户自定义变量
    skin                     # 皮肤目录 @see hm_skin.md
        default              # 某个指定的皮肤目录 
        ...
    template                 # 模板目录 @see hm_template.md
        _std                 # 标准模板分类目录
            th_list_article  # 某个模板目录
            ...
        abc                  # 某个扩展分类目录
        xyz                  # 另外一个扩展分类目录
    prototype                # 网站原型，提供了页面
        ... 还没想好 ...
#######################################################
# 编辑
~/sites                      # 站点
    Nutzam官网               # 某个站点目录
    天天爱踢球社区             # 每个目录都是一个站点
    ...
#######################################################
# 数据集
~/thing
    Nutz新闻                # 数据集目录结构参照 thing
    踢球路线图               # 提供统一的编辑界面
    ...
#######################################################
# 运行时
~/www
    nutzam.com              # 运行时站点的目录
    www.ttatq.com           # 通常是一个域名
    ...
```


