---
title: 浏览器插件
author:zozoh
---

# 控件概述

```
{
    // 如果是缩略图，允许定制缩略图四个角上的图标，函数的返回值必须符合下面的格式
    /*
    {
        NW : HTML      // 左上角（西北）的图标HTML
        NE : HTML      // 右上角（东北）的图标HTML
        SW : HTML      // 左下角（西南）的图标HTML
        SE : HTML      // 右下角（东南）的图标HTML
    }
    */
    thumbnail : {UIBrowser}F(o)
    
    // 定制图标，通过这个，你可以输出图标的 HTML
    // 如果你的函数返回的是 null 或者空，那么就用默认的方式绘制图标
    icon : {UIBrowser}F(o)

    // 控件是否保存最后一次切换的当前路径，如果这个字段有值，则将其作为 key
    // 则会将最后一次路径的 oid 存在 localStorage 里
    // 实际上，如果两个 browser 实例的这个字段相同，那么则会共享
    lastObjId : "last-oid",
       
    // 定制菜单策略
    menu : ???
    
    // 判断一个对象是否可以被打开，默认，只有 DIR 才能打开
    canOpen : {UIBrowser}F(o):Boolean,
    
    // 是否允许多选，默认 true
    checkable : Boolean
    
    // 事件
    on_change  : F(o)       // "browser:change"  改变当前目录触发
}
```

# 本地存储模型

localStorage 存放所有获取下来的对象，以便重复利用

```
oid:431cad.. : {..}
oid:f68ae1.. : {..}
```

每个对象都会有下面的字段

```
{
    __local_cache : 1498...   // 一个绝对毫秒数表示缓存的时间
    ph : "/path/to/obj"       // 路径一定会有的，如果没有就主动拼装出来
    // 如果读取过 children，则会生成这个字段。每次刷新 children
    // 则会更新这个字段，同时添加或删除对应 localStorage 里面的数据
    children : [$id,$id..]
}
```

# 支持的操作

## setData : 设置要浏览的对象

```
// 直接设置一个 WnObj
var o = {.. WnObj ..};
UI.setData(o);

// 采用本地默认的最后存储对象，如果没有，则采用 ~
UI.setData();

// 指定一个 ID
UI.setData("id:34dacd");

// 指定一个路径
UI.setData("~/abc");

// 当然，你甚至能指定一个文件
UI.setData("~/abc.txt");
```

* 注意，因为安全的因素，浏览器控件将拒绝显示自己主目录以外的路径
* 即，给它的路径必须是 "~" 开头的，如果是以 "/" 开头的路径如果不能匹配 "/home/xxx" 将拒绝显示

## getViewMode : 获取当前浏览器的显示模式

```
var viewmode = UI.getViewMode();
```
> 关于显示模式，请参看下面的 `setViewMode`

## setViewMode : 设置浏览器的显示模式

```
UI.setViewMode("table");
```

浏览器支持下列显示模式

 Mode Name   | dblclick | 模式描述
-------------|----------|-----------------
 *table*     | open     | 表格
 *thumbnail* | open     | 缩略图
 *icons*     | open     | 图标列表
 *scroller*  | edit     | 滑动卷轴，每屏一个对象的缩略图
 *columns*   | open     | 多栏，类似 Mac 的 Finder
 *slider*    | edit     | 幻灯片放映

* 一个对象具体用什么编辑器打开，用 `appedit` 命令获取
* 一个对象具体用什么查看器查看，用 `appview` 命令获取
* 默认的，会采用 *table* 来显示
* setViewMode 会触发消息 `viewmode:change`, 参数就是 *viewmode* 本身. 

每个显示模式都对应一个具体负责显示的 UI，以后也可能不断扩张，无论如何，这个 UI 都必须支持如下的构建接口

```
{
    // 这个函数用来更新内部信息的显示，当 Browser 切换到一个新的路径
    // 就会调用这个函数
    update : F(UIBrowser, o)
    
    // 给定一个 jQuery 对象或一个 Element 对象，需要返回这个对象对应
    // 的 WnObj 对象本身
    getData : F(ele)
    
    // 判断给定的 Element 所在的 WnObj 是否是激活的
    isActived : F(ele)

    // 获取当前被激活的对象，返回 WnObj
    getActived : F()
    
    // 返回一个 WnObj 对象列表
    getChecked : F();
    
    // 显示修改当前激活对象的界面
    rename : F();
}
``` 

同时各个子 UI 都会被设置下列属性

```
{
    browser : UI        // 当前所在 UIBrowser 控件的实例
}
```

无论哪个显示模式的 UI，最后都将文件对象的显示元素标记下列特殊的类选择器:

```
.arena 
    .wnobj[oid,onm] .wnobj-hide    # 隐藏文件（以.开头）需标记.wnobj-hide
        .wnobj-thumbnail           # 缩略图
        .wnobj-nm                  # 对象显示名称
```

* 层级关系应该就是这个结构
* 中间是否有其他的元素包裹无所谓
 
## getActived : 获取当前被激活对象

```
var o = UI.getActived();
```

## getChecked : 获取当前被选中对象

```
var olist = UI.getChecked();
```

## getCurrentObj : 获取当前的对象

```
var o = UI.getCurrentObj();
```

## getPath : 获取当前路径

```
var ph = UI.getPath();
```

* 注意，这里的路径是用 "~" 开头的，即这个函数是返回面包屑道航看到的路径

## getPathObj : 获取当前路径每一个对象

```
var list = UI.getPathObj();
console.log(list);   // log  [{id:xx,nm:xx}, {id:xxx,nm:xxx}]
```

* 返回一个数组，表示面包屑道航路径包括的每一个对象，对象只有两个字段
    - *id* : 对象的 ID
    - *nm* : 对象的 nm

## get : 自动根据参数判断该如何获取对象

```
// 根据 ID 获取
var o = UI.get("id:34cade..");

// 根据路径获取
var o = UI.get("/path/to/obj");

// 根据元素对应的对象获取
var o = UI.get(jQuery||Element);

// 获取当前路径下显示的全部对象列表
var olist = UI.get();
```

## getById : 根据ID获取对象

```
// 根据 ID 获取对象，如果对象缓存过期，则从服务器获取
var o = UI.getObj("34acd5");

// 根据 ID 获取对象，强制从服务器更新数据，并更新到对应缓存
var o = UI.getObj("34acd5", true);
```

* 为了调用的便利，如果从服务器获取，本函数一定是发送同步请求的

## fetch : 根据路径获取对象

```
// 根据 Path 获取对象，如果对象缓存过期，则从服务器获取
var o = UI.fetch("/path/to/obj");

// 根据 Path 获取对象，强制从服务器更新数据，并更新到对应缓存
var o = UI.fetch("/path/to/obj", true);

// 当然你可以用 ~ 开头的路径
var o = UI.fetch("~/path/to/obj");
```

* 为了调用的便利，如果从服务器获取，本函数一定是发送同步请求的

## getChildren : 获取某指定对象的所有子对象

```
// 获取当前对象所有子
var children = UI.getChildren(o);

// 强制从服务器刷新所有的子
var children = UI.getChildren(o, true);
```

## cleanCache :  清除本地缓冲

```
// 清除全部缓存
UI.cleanCache();

// 清除指定 ID
UI.cleanCache(oid);
```

* 如果指定了对象，那么这个对象下面所有的内容也都会被一起清理
* 依靠 `children` 字段

## saveToCache : 向缓冲中保存一个对象 

```
// 如果缓冲存在，则合并
UI.saveToCache(o);

// 如果缓冲存在，则清除所有的子
UI.saveToCache(o, true{;
```

# 动态菜单

```
$HOME/.ui/actions   # 这个目录存放 browser 控件支持的所有动作类型
    new.js          # 文件名是这个动作的唯一标识
    delete.js       # 每个对象的内容是一个 JS 文件描述了这个动作的详情
    edit.js         # 动作可以自动归组，详情参见动作文件描述
```

一个动作的 JS 描述和 menu 控件输入是一致的，为了方便起见，我摘抄如下:

```
({
    key     : "xxx",        // 无论你写什么，都会被文件名替换
    text    : "i18n:xxx",   // 动作的显示名称
    icon    : HTML,         // ICON 可选，为一段 HTML
    type    : "xxx"         // 动作的类型 "button|status|group"
    // 动作显示的时候，可以执行初始化函数，修改它的一些显示效果
    init    : {UIBrowser}F($ele, a);
    
    // 如果 type=="status"，那么下面就是这个枚举的详细信息
    // 当枚举类型值被改变后，会触发事件(前提是 context 支持 trigger 函数)
    // context.trigger("status:xxx")    
    status : [{               // 如果是枚举
        text : "i18n:xxx",  // 每个枚举项目的显示文字
        icon : HTML,        // 显示的图标
        val  : "xxx"        // 对应的值
    },{..}..],


    // 如果 type="button"，那么这个按钮的执行操作就是一个函数
    handler : {UIBrowser}F($ele, a);
    
    // 如果 type="group"，那么会展现出一个二级菜单
    items : ["::find","::edit"]
})
```

实际上，它会用 *menu* 控件赖负责具体的显示。
那么对于一个具体的对象，怎么获取对应的命令菜单呢？

```
$HOME/.ui/menu
    mime_application_octet-stream.js     # 如果是文件，可以根据 mime 类型获取
    folder.js                            # 但是首先会尝试用文件类型获取
    _unknown.js                          # 所有找不到的，用这个
```

每个对象的内容都是一个 JSON:

```
{
    actions : [            # 本类型对象支持的指令
        "@:r:new",         # 永远显示:需要读权限:new操作
        ":w:delete",       # 在菜单里显示:需要写权限:new操作
        "~",               # 分隔符
        "::properties"     # 在菜单里显示:不校验权限:属性操作
    ]
}
```

* 界面则需要根据这段 JSON 显示成一个菜单



