---
title: 通用数据管理控件
author:zozoh
---

# 控件概述

本控件将提供一套 UI，方便用户迅速建立一套增删改查的数据界面。面对的数据类型包括

- 普通的 WnObj 形式的数据目录
- 标准的 Thing 数据

本控件默认认为是处理的标准的 Thing 数据，对于普通的数据目录，则需要给出更多配置信息。

控件提供如下的 UI 子控件

```
th_desgin     # 可视化设计界面
th_manager    # 顶级管理器
    + th_search               # 数据集管理界面
    + th_obj                  # 单个数据管理界面
        + th_obj_index        # 数据详情和元数据的组合界面 
            + th_obj_index_meta     #  - 元数据编辑界面
            + th_obj_index_detail   #  - 文字详情编辑界面
        + th_obj_data         # 附加数据编辑界面
            + th_obj_data_media        #  - 多媒体管理界面
            + th_obj_data_attachment   #  - 附件管理界面
```

# th_manager 控件簇配置信息

则个配置信息，格式如下： 

```
{
    // 数据模式
    //  - thing 表示处理的是一个 thingSet
    //  - obj   表示处理的是一个普通数据目录
    // 默认为 thing
    // 如果为 obj，则需要指定附件，媒体等处理办法
    // 对象的初始化函数，会根据数据模式，初始化本配置项
    // 其余字段，比如为 actions 设置默认的增删改查等
    dataMode : "thing|obj"

    // 定义了可执行的操作
    // 下面列出所有的内置操作，
    // 除非明确设置成  null ,false 等假值，
    // 控件会会根据 dataMode 设置值自行设置的
    // 所有的函数都是异步的，接受 callback，
    // 函数处理完毕后，必须主动调用  callback 并传递返回数据
    // 以便后续处理
    actions : {
        // 查询，返回的必须是带 pager 的数据集
        query  : Cmd | {c}F(params, callback),
        
        // 创建
        create : Cmd | {c}F(obj, callback),
        
        // 更新
        update : Cmd | {c}F(obj, callback),
        
        // 移除
        remove : Cmd | {c}(obj, callback),
    },

    // 对于搜索部分菜单的设定
    // 如果为空，则会默认从 actions 里面找
    //  query | create | update | remove 
    // 四个命令的执行方法
    searchMenu : [{
        icon : '<..>'        // 图标
        text : 'i18n:xxx'    // 文字
        // 动作名，这个是与 menu 控件不同
        // 如果声明了这个段 handler 段被忽略，执行的时候，是通过
        // 本字段找到相应的操作方法的
        actionName : "query"
        // 自定义操作方法，和 menu 控件的配置项意义相同
        handler : {c}F()
    }]
    
    // 对于对象部分的菜单设定
    objMenu : [{
        // 参见 searchMenu 部分菜单设定
    }]

    // 元数据字段定义，与表单控件的配置项相同
    // th_search | th_obj | th_obj_index 需要这个字段
    fields : []
    
    // 对于搜索部分的设置
    search : {}
    
    // 对于元数据显示表单的配置
    meta : {}
    
    // 对于 detail 文本的配置
    detail : {
        // 读取数据的 detail
        read : {c}F(obj, callback),
        
        // 保存数据的 detail
        save : {c}F(obj, detail, callback),
    }
    
    // 对于媒体的配置信息
    media : {
        // 支持多个媒体还是仅仅一个媒体, 默认 true
        multi : true
        
        // 上传目标: 一个文件目录的路径
        target : ObjPath
        
        // 如果已经存在，是否覆盖（如果不能覆盖，则改名）
        // 默认 true
        overwrite : true
        
        // 上传前，是否先检查一下是否可以上传
        //  - fnm  : 文件名
        //  - mime : 内容类型
        //  - size : 文件大小(字节）
        filter : {c}F(fnm, mime, size):Boolean
    
        // 如何列出对象所有的媒体
        list  : Cmd | {c}F(obj, callback),
                    
        // 移除一个媒体
        del : Cmd | {c}(obj, callback),
        
        // 上传成功后，要执行什么后续处理
        done : Cmd | {c}F(obj, oMedia, callback),
    }
    
    // 对于附件的配置信息
    attachment : {
        // 与 media 信息相同
    }
    
    // 界面还需要显示的公共文件夹
    // 界面会在本地记住最后一个路径，每次就打开这个路径
    // 不过这个路径必须在 paths 里，否则无效
    folders : {
        paths  : ["~/images","~/abc"],  // 直接指定几个路径
        ftype  : "^(jpe?g|png|gif)"     // 不声明表示不过滤
        mime   : "^image/"              // 不声明表示不过滤
        subs   : true,                  // 是否支持显示子文件夹 
    }
    
    // 这里设置了一组事件监听处理函数 
    busEvents : {
        // 元数据改变
        "change:meta" : {c}F(key, val)
        
        // 详情改变
        "change:detail" : {c}F(detail, brief)
        
        // 激活某个数据对象
        "obj:active" : {c}F(obj)
        
        // Blur 某个数据对象
        // - obj     被blur 的数据对象
        // - nextObj 下一个即将被激活的数据对象，null 表示不会有对象被激活
        "obj:blur" : {c}F(obj, nextObj)
    }
}
```

# 回调函数上下文

为了方便回调函数方法这个控件簇的任意子控件，
所有的回调的上下文*(即,this)*都为一下面的对象

```
{
    uiManager   : ZUI,
    uiSearch    : ZUI,
    uiObj       : ZUI,
    uiObjIndex  : ZUI,
    uiObjDetail : ZUI,
    uiData      : ZUI,
    uiDataMedia : ZUI,
    uiDataAttachment : ZUI
}
```

- 当然，如果你仅仅是创建了某个子控件，那么其他控件就会为空
- 只有自己和自己的子控件才会在上下文里找到

