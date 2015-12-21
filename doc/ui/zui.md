---
title:ZUI 框架
author:zozoh
---

# 一个UI对象的样子

这里声明一个 UI 模块，这个模块就是一个工厂类，用来构建各个 UI 的实例
每一个 UI 的实例都是一个 Backbone.View 它的数据结构

```
{
//---------------------------------------------------
// 下面这些属性都由调用者在 new 的时候提供
//  * 其中 `$pel` 和 `parent/gasketName` 互斥，优先级低
//  * model 是可选的，但大多数消息驱动的 UI 都需要它
//---------------------------------------------------
uiName     : "ui.name"  // UI 类型名
uiKey      : "myUi"     // 用户指定的 UI 助记ID，可以用 UI.get/checkByKey 接获取实例
depth      : 0          // UI 的深度，顶层 UI 为 0，它的子UI为 1，依次类推
cid        : "view1"    // Backbone 生成的实例ID （只能读取）
tagName    : "div"      // 本UI的顶级DOM节点是什么标签，默认DIV
className  : ".."       // 顶级DOM节点的类选择器，默认采用uiName
$pel       : $<..>      // 本 UI 附着在哪个 DOM 节点上
$el        : $<..>      // 本 UI 的顶级 DOM 节点是什么
parent     : UI(..)     // 父 UI 的实例
gasketName : ".."       // UI 所在的父元素的扩展点名称
model      : {..}       // 本 UI 对应的 Backbone Model 是什么
lang       : "zh-cn"    // 本 UI 采用的语言，默认 zh-cn
//---------------------------------------------------
// 记录了自己的扩展点的名称，以及对应的 DOM
// 这个属性由 parseDom 函数来生成，即 ui 的实例的 $ui.init 函数
// 需要主动调用 parseDom 来生成下面两个属性
//---------------------------------------------------
gasket    : {
    "chute" : {
        ui    : []      // 插入点的 UI 对象，必须是一个数组，以便容纳多个
        multi : false   // 默认false，新 UI 会导致之前的 UI 被销毁
        jq    : $<..>   // 插入点的 DOM 元素的 jQuery 包裹
    }
    "arena" : {
        ...
    }
},
//---------------------------------------------------
// 存放多国语言字符串
//---------------------------------------------------
_msg_map : {
...
}
//---------------------------------------------------
// 下面这些属性都由 ZUI 的工厂函数(ZUI.def)提供
// UI 的实现者不可私自修改 !!!
//---------------------------------------------------
initialize  : F(options)   // Backbone.View 的初始化函数
destroy     : F(..)        // UI 的释放资源函数
watchKey    : F(..)        // 监听快捷键
unwatchKey  : F(..)        // 取消监听快捷键
watchMouse  : F(..)        // 监听全局鼠标事件，防冒泡的听不到
unwatchMouse: F(..)        // 取消鼠标事件监听    
listenModel : F(..)        // 监听模块消息
listenParent: F(..)        // 监听父UI的消息
listenUI    : F(..)        // 监听指定UI的消息
msg         : F(..)        // 得到本地化字符串
resize      : F(..)        // 将自身设置为符合父选区的大小
//---------------------------------------------------
// 视图的渲染方法，实际上这是视图的主要渲染逻辑
// 它最终会调用 $ui.redraw 方法完成视图的最终显示
// 在这之前，它会根据视图的自身属性，进行代码模板等资源的加载
// 这些加载(尤其是本地化字符串的加载)是异步的
// 因此，调用完 render 并不能马上得到渲染的结果
//---------------------------------------------------
render      : F(..)
//---------------------------------------------------
// Backbone.View 的事件监听
// 所有的方法的 this 都是 ui 实例本身
events      : {...}
//---------------------------------------------------
// 下面这些属性在 ZUI.def 的时候，由UI实现者提供
// 这些构成了 UI 的主要业务逻辑，所有的 UI 实例，这个段都是相同的 
// 所有的方法的 this 都是 ui 实例本身
$ui         : {
    css     : "/path/to/css" | ["path1", "path2"],
    // DOM 字段可以是一个 URI，这样 ZUI 会用类加载器去向服务器请求这个资源
    // 由于 seajs 用的是 xhr，那么就会有跨域的问题，为此，这个字段
    // 也可以是一个 HTML 代码本身，只要开头和结尾都有块注释
    // 即， /* 和 * / 包裹，那么中间的内容会被 ZUI 当做你的代码模板，
    // 它就不会去加载，而是直接使用你的这段 HTML
    dom     : "/path/to/html",
    // 指定了本 UI 所用的多国语言字符串
    // 同时，如果值为 ".." 则表示采用父 UI 的多国语言字符串集合
    i18n    : "app/abc/i18n/{{lang}}.json",
    // 初始化函数，UI 可以在其中加载自己需要的资源
    //  @ 会被 initialize 在实例构造时调用
    init    : F(..),
    // UI 的主要绘制逻辑
    redraw  : F(..),
    // 释放 UI 的资源
    //  @ 会被 destroy 在实例销毁的时候调用
    depose  : F(..),
    // 修改自身大小以适应选区
    //  @ 会被 resize 在适当的时候调用
    resize  : F(..)
}

// UI 实例的 options 段是每个实例都不同的
options : {
    $pel       : <..>
    parent     : UI(..)
    gasketName : ".."
    model      : {..}
    // 这些可以重载 ZUI 的定义
    i18n
    dom
    css
    redraw
    // 主题，默认为 w0
    theme      : "w0" 
    // 下面四个事件是 UI 四种行为时的标准回调，你可以在生成实例的参数里做自定义 
    // 每个回调的 context 都是 UI 实例本身
    on_init   : F(..),   // 也会触发 "ui:init" 事件
    on_redraw : F(..),   // 也会触发 "ui:redraw" 事件
    on_depose : F(..),   // 也会触发 "ui:depose" 事件
    on_resize : F(..)    // 也会触发 "ui:resize" 事件
}
```

# 声明一个UI

任何一个 UI 都可以用如下方法定义，第二个参数就是 UI 的 conf

```
var UI = ZUI.def("ui.name", {
    // Backbone.View 需要的事件映射
    events : {...},
    // ZUI 需要的特殊属性
    css     : "/path/to/css" | ["path1", "path2"],
    dom     : "/path/to/html",
    i18n : "app/abc/i18n/{{lang}}.json",
    init    : F(..)
    redraw  : F(..)
    depose  : F(..)
    resize  : F(..)
    // 随便你添加更多的方法和属性了
    on_xxxx : func(){..}
    x : 2.16,
    y : 9.21
});
```

# 创建一个 UI 的实例

并用下面的方法生成实例

```
var ui = new UI({
    $pel       : <..>
    parent     : UI(..)
    gasketName : ".."
    model      : {..}
    // 样式
    theme      : ".."
    // 监听其他的 UI 事件
    do_ui_listen : {
        "uiKey event:name" : F(..) | "methodName"
    },
    // 四个标准事件的回调
    on_init   : F(..)
    on_redraw : F(..)
    on_depose : F(..)
    on_resize : F(..)
    ... UI 的特殊配置 ...
});
```


# 全局事件监听

一个 UI 控件可以向父类注册全局事件监听，ZUI 负责控制这个监听的生命周期，如果UI被销毁，相应的监听函数也会被注销掉。现在支持两种事件监听，*键盘事件* 和 *全局点击*

> ! 注意，如果事件在触发的地方防止了冒泡，是不会被全局监听到的。全局监听就意味着，只监听那些冒泡到 document 级别的事件。

## 键盘事件

框架会捕捉整个文档范围的键盘事件，你可以通过下述方法注册

```
// 建立键盘事件监听
UI.watchKey(41, ["alt","shift"], function(e){..});
UI.watchKey(41, "ctrl", function(e){..});
UI.watchKey(41, function(e){..});

// 取消监听
UI.unwatchKey(41, ["alt", "shift"]);

// 取消所有的键盘监听
UI.unwatchKey();
```

框架会在下述结构下保存你的监听

```
ZUI.键盘监听映射 = {
    "监听的键" : {
        "UI的cid" : [函数1, 函数2]
    }
}
比如:
ZUI.keymap = {
   "alt+shift+28" : {
       "c2" : [F(e), F(e)..]
   }
};
```
* 组合键的名称必须是 *"alt"*, *"ctrl"*, *"shift"*, *"meta"* 这四种
* 你用一个数组传入多个组合键，比如 `UI.watchKey(41, ["ctrl","alt"], ..` 表示键 `alt+ctrl+41`， 生成键的时候会做排序，因此你给数组的内容不重要
* 第一个参数 `41` 表示键的码值
* 监听函数的上下文(*this*)就是 UI 实例本身，控件会根据 *cid* 得到 UI 的实例

## 全局鼠标事件

有些时候控件需要在点击到 document 的时候，做点事情，比如隐藏弹出的菜单等

```
// 建立鼠标事件监听
UI.watchMouse("click", F(e){..});

// 取消鼠标事件监听
UI.unwatchMouse("click");

// 取消全部鼠标事件监听
UI.unwatchMouse();
```
框架会在下述结构下保存你的监听

```
ZUI.鼠标监听映射 = {
    "click" : {
        "UI的cid" : [函数1, 函数2]
    }
}
比如:
ZUI.mousemap = {
   "click: {
       "c2" : [F(e), F(e)..]
   }
};
```

* 监听函数的上下文(*this*)就是 UI 实例本身，控件会根据 *cid* 得到 UI 的实例
* 目前，监听类型仅仅支持下面几种
    * mousedown
    * mouseup
    * mousemove
    * click
    * dblclick
    * mouseenter
    * mouseleave
    * contextmenu


