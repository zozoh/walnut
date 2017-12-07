---
title:皮肤机制
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
        skin.info.json      # 一个JSON文件，以便编辑器理解皮肤
        skin.css            # 皮肤的样式文件「输出结果」
        skin.less           # 皮肤的样式文件
        _skin_var.less      # 皮肤样式的变量文件
        skin.js             # 「选」皮肤的 JS 文件
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
                "@att1(文字){yes/no}",    // 显示 yes/no 的属性开关
                "@att2(文字){yes}",       // 显示 yes 或者不存在的属性开关
                "@att3(文字}[text=value,text=value]", // 显示枚举型属性开关
                "#C> li[open-sub] > a(高亮项前景)=",   // 颜色自定义选择器
                "#B> li[open-sub](高亮项背景)=#000",   // 背景自定义选择器 
                "#L> li[open-sub](边框)=#000",        // 边框颜色自定义选择器 
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
    //!!!!!!!!!!!!!!! 这个作废
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
# 自动映射模板

对于 `dynamic` 控件，根据皮肤自动选出模板会有更好的用户体验。而且皮肤如果不支持的模板，则选了也没啥意义。因此我们给出下面的约定:

```
...
com : {
    "dynamic" : [{
        // 根据类选择器，属性面板需要知道对应到什么模板
        // 譬如 _std-th_list_article 就应该对应到 _std/th_list_article
        // 这里的 `-` 作为路径分隔符，因此模板名称不能带有 `-` 否则会出事
        selector : "_std-th_list_article",
        text     : "i18n:xxxx",             // 显示文字
    }]
},
...
```

# skin.less

暂时允许随便写吧 ^_^!，以后应该加一个约定，以便根据 skin.info.json 生成，也能根据 skin.less 生成 skin.info.json

# _skin_var.less

格式如下，以便编辑器能正确解析格式

```
//#全局设置
@f_0         : .18rem;     // 默认文字大小
@lnk_c       : #08F;       // 链接颜色
@lnk_ho_c    : #F80;       // 链接悬浮颜色
@h_f_c       : #000;       // 高亮区前景色
@h_b_c       : #f9c320;    // 高亮区背景色

//#页眉设定
@sky_title_c  : #444;       // 网站标题颜色
```

# skin.css

皮肤的主体就是一个 CSS 文件。这个 CSS 文件通常是通过 `skin.less` 生成的，在皮肤目录下：

```
_skin_var.less
    |
    | 引入
    v
skin.less
    |
    | 生成
    v
skin.css
```

在一个域，用户在的配置文件是

```
~/hmaker/skin
    myskin
        _skin_var.less
        skin.less
        skin.css
```

## 编辑时 skin.css 的生成

编辑时，会要求能动态反映出 `skin.less` 的修改结果，因此编辑器会请求 `/api/youdomein/hmaker/load/45ac..89ae/skin.css`，在 REGAPI 里面会设置路径参数适配接口 

```
~/.regapi/api/hmaker/load/_ANY/_ANY/_action
  _ANY[0] {"api-param-name" : "siteId"}
  _ANY[1] {"api-param-name" : "rsPath"}
%COPY:
hmaker read id:${http-params.siteId?none} '${http-params.rsPath}' \
            -force ${http-qs-f?false}
%END%
```

当 `hmaker read` 发现 *rsPath* 是 *skin.css* 的时候，就会自动寻找站点的配置信息，找到皮肤目录的 `skin.less` 将其编译。 当然 `skin.less` 里面 *import* 的 `_skin_var.less` 是默认规约，如果不引入这个文件，编辑器提供的界面是无法自定义 `_skin_var.less` 里面的内容的。

本机制不限制你在 `skin.less` 引入更多的 less 文件。

注意，用户自定义的皮肤变量存放在 `~/.hmaker/setup/skin/myskin_var.less`，这个文件如果存在，将会替换 `_skin_var.less` 里面的内容。即，`_skin_var.less` 里面的是变量的默认值。

对于 `hmaker read` 命令，如果不指定 `-force true`，那么它会利用 `~/.hmaker/cache/skin/myskin.css` 作为缓存，这个文件有个元数据 `finger`，对应 skin 目录所有 `less/css` 文件的变动进行了签名，每次真正输出 css 都会更新这个文件并重新记录签名。以防止重复执行*less*的编译操作。

签名算法为：

```
(伪代码）
准备签名字符串 str
遍历 skin 目录下所有的 less/css {
    str += Wn.getEtag(obj)
}
签名为 MD5(str)
```

## 发布时的 skin.css 生成

`hmaker publish` 命令发布整个站点的时候，会

- 强制更新 `~/.hmaker/cache/skin/myskin.css`，并更新 `finger`
- 将这个 `skin.css` copy到目标*www*目录下
- 给页面直接引入 `skin.css`


# skin.js

皮肤如果有比较绚丽的交互效果，可能得用点 JS，不过这个不是必须的。
编辑器会无视这个，发布的时候，如果有，会在 `<head>` 里插入这个文件


