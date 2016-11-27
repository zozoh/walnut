---
title:站点皮肤
author:zozoh
tags:
- 扩展
- hmaker
---

# 什么是站点皮肤

任何一个站点都可以选用一套皮肤，来保证自己各个控件统一的外观. 皮肤存放在域中 `.hmaker` 目录下以便多个站点共享

```
~/.hmaker/skin
    skin_nameA                # 皮肤所有的资源存放目录
        skin.info.json  # 一个JSON文件，以便编辑器理解皮肤
        skin.css        # 皮肤的样式文件
        skin.js         # 「选」皮肤的 JS 文件
```

# skin.info.json

```
{
    // 将会在 body 上标识属性 body[skin="XXX"] 
    // 必须为英文或者数字下划线或中划线
    name : "skin-name",
    
    // 针对各个控件的可选样式，用户可以在界面上为控件附加上皮肤的类选择器
    // 因为各个控件的 DOM 结构已知，所以皮肤的 css 可以针对各个层级产生作用
    // 选择器推荐也用 skin 开头, 以避免冲突
    com : {
        "image" : [{
            selector : "AAAA",          // 类选择器，不加 "."
            text     : "i18n:xxxx",     // 显示文字
        },{
            ..
        }],
        "menu" : [{
            ..
        }]
    },
    // 布局区域的皮肤样式，针对所有布局控件的 .hm-area 可以选择的类选择器
    // 不管是什么布局，总是分成一个个的区域，这里认为区域都是平等的，可以附加
    // 特殊的类选择器。这个选择器一定是被加载 .hm-area 上的
    // 并且通常，它会假想有 `.hm-area > .hm-area-con` 这样的结构
    area : [{
        selector : "AAAA",          // 类选择器，不加 "."
        text     : "i18n:xxxx",     // 说明文字
    }, {
        selector : "AAAA",          // 类选择器，不加 "."
        text     : "i18n:xxxx",     // 说明文字    
    }],
    // 针对各个显示模板的特殊样式
    // 用户通过显示模板可以定制动态数据的 DOM 结构(无论是服务器端渲染还是JS端渲染)
    // 但是说到 CSS 必须由皮肤提供支持。
    // 如果一个显示模板在下面的映射里找到了选择器，那么就必须在自己输出的顶级元素里添加它
    // 这个逻辑是由 objlist/objshow 控件内置提供的，
    // 当然发布站点的时候也要遵从这一逻辑
    template : {
        "template_name_A" : ".skin_template_name_A",
        ...
    }
}
```

# skin.css

没啥好说的，就是 css，是 skin 的主要逻辑体现

# skin.js

皮肤如果有比较绚丽的交互效果，可能得用点 JS，不过这个不是必须的。
编辑器会无视这个，发布的时候，如果有，会在 `<head>` 里插入这个文件


