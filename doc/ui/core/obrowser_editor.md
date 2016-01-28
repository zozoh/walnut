---
title: 对象的编辑器机制
author:zozoh
---

# 对象的编辑器机制概述

* 每个对象都可以对应 1-n 个编辑器
* 每个编辑器都是一个 ui 控件
* 一个编辑器专门处理一种或者几种文件对象，可以是文件夹或者文件
* 默认的编辑器是:
    -  *obrowser* 控件自身用来处理 `DIR`
    -  *otext* 用来处理 MIME 类型是文本的 `FILE`
    -  *opreview* 用来处理 MIME 类型是非文本的对象，会自动针对图片视频等进行优化显示
* 编辑器一定有的编辑元素是:
    - 菜单 *menu*
    - 大纲 *outline*
    - 主区域 *arena*

# 界面配置文件

```
$HOME/.ui/
    sidebar.html         # 域的侧边栏定义
    _unknown.js          # 找不到界面定义的对象采用这个默认定义
    ftypes               # 针对每种文件类型来关联
        folder.js        # 对应文件类型 folder，DIR 对象默认就是 'folder'
        wnml.js          # 对应文件类型 "wnml"
    mimes                # 根据 mime 来关联
        text_html.js     # 对应 mime 为 text/html
        text.js          # 对应 mime 为 text/* 
    editors              # 对每个编辑器的描述
        otext.js         # 编辑器的唯一识别名称
        oview.js         # 一个 Json 文件描述了编辑器的详情
    actions              # 这个目录存放 browser 控件支持的所有动作类型
        new.js           # 文件名是这个动作的唯一标识
        delete.js        # 每个对象的内容是一个 JS 文件描述了这个动作的详情
```

根据一个对象查找到类型定义的顺序是:

```
0. o.type()     # 比如 "html"
1. o.mime()     # 比如 text/html 对应 mime_text_html
```

## 类型声明: ftypes

```
{
    /*
    动作项，通过一个字符串声明这个类型的对象可以进行什么样的操作。
    字符串的格式为
        ([@Ee]:[rwx]:动作名) | ~
    其中
        @ 表示永远显示，不折叠起来
        e 表示只有在编辑器里面显示
        E 表示在编辑器里不显示
        rwx 表示只有对对象具备相应权限才会显示
        ~ 表示分隔符
    */
    actions : [
        "@:r:new",         # 永远显示:需要读权限:new操作
        ":w:delete",       # 在菜单里显示:需要写权限:new操作
        "~",               # 分隔符
        "::properties"     # 在菜单里显示:不校验权限:属性操作
    ],
    // 可用编辑器列表，排在第一个的，为默认编辑器
    editors : ["otext", "oview"]
}
```

## 编辑器声明: editor

```
{
    key   : "otext",       // 编辑器的键，建议与文件名同名
    text  : "i18n:xxx",    // 编辑器的显示名称
    icon  : HTML,          // ICON 可选，为一段 HTML
    mime  : "^text/.+",    // 对应对象的 MIME 类型，空表示任意
    ftype : null,          // 对应的对象 type，空表示任意
    actions : [..]         // 编辑器对应的菜单，将会补充全局的菜单项
                           // 这些菜单项将不会根据读写权限进行过滤，统统要显示
    outline : true,        // 编辑器是否需要指定外部 outline 绘制区
    footer  : false,       // 编辑器是否需要指定外部 footer 绘制区
    uiType  : "/app/wn.xx/xx",  // 编辑器所采用的界面控件
    uiConf  : {..}              // 控件的配置项
}
```

* 客户端需要根据给定的 *mime* 和 *ftype* 校验给定的编辑器是否能打开对象
* 即使校验不通过，客户端界面可以允许用户强制打开这个编辑器，但是后果不可知

### 编辑器标准配置项

```
{
    menu : [..]        // 菜单项 (@see menu控件)
    editor  : {..}     // 编辑器的配置信息
    outline : $ele     // 指定编辑器的大纲视图的父元素
    footer  : $ele     // 指定编辑器状态信息条的父元素
}
```

* 上述配置项会由客户端程序动态设置
* 如果你没有在配置文件里配置 `outline:true`，则不会指定 *outline*
* 如果你没有在配置文件里配置 `footer:true`，则不会指定 *footer*

### 编辑器标准函数

除了 UI 的标准函数，编辑器还会支持下列函数

```
{
    // 更新要编辑的数据对象
    update : F(o) 
    
    // ................................. 
    // 如果支持 save_text 的话，需要支持下面的操作

    // 获取正在编辑的对象
    // 通常，这个对象是你的 update 函数记录下来的
    getCurrentEditObj : F():WnObj
    
    // 获取正在编辑的文本信息
    getCurrentTextContent : F():string
    
    // 如果想支持 save_json 的话，还需要提供:  
    getCurrentJsonContent : F():{..}
}
```


## 动作文件: actions

```
({
    key     : "xxx",        // 动作的键，建议与文件名同名
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
    handler : {context}F($ele, a);
    
    // 如果 type="group"，那么会展现出一个二级菜单
    items : ["::find","::edit"] | {UIBrowser}F($ele, a, callback)
})
```

* 基本上你可以参看 `menu` 控件，这里的设定也就是 *context* 与其略有区别
* 实际上动作的界面表示，就是由 `menu` 控件完成的
* *handler* 的 *context* 通常有两种可能，要不就是编辑器的，要不就是 *UIBrowser* 的
    - 不同的处理函数或许需要 *context* 提供更多的操作，比如获取数据等
    - 调用前，处理函数需要对 *context* 进行判断，看看自己需要的函数是否有提供

# 获取命令
    
```
# 获取当前目录的编辑器信息
appmenu

# 获取某个指定 ID 对象的编辑器信息
appmenu id:45cfae

# 获取某个指定路径的对象编辑器信息
appmenu ~/path/to/obj
```

