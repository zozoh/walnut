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
    
    /*
     * 本皮肤是否启用了 JS，默认 false
     * 如果启用，那么皮肤目录必须有一个 skin.js
     * 它必须符合 CMD 的编写规范,即用 define 函数输出一个
     * 对象，它的大概样子应该是:

     define(function (require, exports, module) {
        on : function(){
            // 当皮肤被启用时调用
        },
        off : function(){
            // 当皮肤被注销时调用
        },
        on_resize : function(doc, jRoot, $){
            // 当窗口改变尺寸时调用
        }
     });
     
     其中每个函数的 this 都是这样的上下文对象
     
    {
        // 标示皮肤运行环境
        // "IDE" 表示编辑器内，其他值就全当在真正的环境下好了
        // ! 大小写敏感
        mode   : "IDE",

        win    : window,           // 皮肤需要应用的 window 对象 
        doc    : document,         // 皮肤需要应用的文档对象
        root   : documentElement,  // 文档对象根节点
        jQuery : window.jQuery     // jQuery 库对象
    }

     * 在运行时，就是页面加载的时候，在编辑器里，就是每次皮肤被应用的时候
     * ! 注: skin 里面的 JS 应该自行判断当前的环境是否为编辑环境
     * ! 判断的依据为  this.mode == "IDE" 
     * ! 在运行时，这个 JS 可以使用 jQuery|zuitl|underscore 库，其他的库
     * ! 必须在这个数组里声明，比如 "@alloy_finger" 手势库或者 "@vue" 等
     */
    js : ["@jquery", "@alloy_finger"],
    
    // 针对各个控件的可选样式，用户可以在界面上为控件附加上皮肤的类选择器
    // 因为各个控件的 DOM 结构已知，所以皮肤的 css 可以针对各个层级产生作用
    // 选择器推荐也用 skin 开头, 以避免冲突
    com : {
        "image" : [{
            selector : "AAAA",          // 类选择器，不加 "."
            text     : "i18n:xxxx",     // 显示文字
            // 对于组件来说，skin 可以定制 Block 面板的属性
            // 如果没有用控件默认的
            // 内置 CSS 可以支持:
            // margin|padding|border|borderRadius|color|background
            // boxShadow|overflow 
            blockFields : [
                "padding",          // 字符串是内置 CSS
                "@att1(yes/no)",    // 显示 yes/no 的属性开关
                "@att2(yes)",       // 显示 yes 或者不存在的属性开关
                "@att3[text:value,text:value]" // 显示枚举型属性开关
            ]
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


