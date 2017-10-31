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

# `th_manager` 控件簇配置信息

则个配置信息，格式如下： 

> 所有的回调，如果没有特殊说明，其 `this` 均为 `bus` 对象

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
    //  - 所有的函数都是异步的，接受 callback，
    //  - 函数的 this 为 bus 对象
    //  - callback 函数接受一个参数表示异步函数的返回
    //  - 所有 callback 的 {c} 为 th_search UI 
    actions : {
        /* 查询，返回的必须是带 pager 的数据集
        其中 params 的格式为:
        {
            pid   : ID         // 父目录对象 ID
            match : "{..}"     // 一个 JSON 字符串表查询条件
            sort  : "{..}"     // 一个 JSON 字符串表排序条件
            skip  : 0          // 跳过多少数据
            limit : 50         // 每页数据大小
        }
        */
        query  : {c}F(params, callback),
        
        // 创建
        create : {c}F(obj, callback),
        
        // 更新
        update : {c}F(obj, callback),
        
        // 移除
        remove : {c}(obj, callback),
    },
    
    // 应用 search 控件的  filterWidthHint
    // 即，如果菜单大于多少宽度，将会自动收起来
    // 默认 50%
    searchMenuFltWidthHint : "50%",

    // 对于搜索部分菜单的设定
    // 如果为空，则会默认从 actions 里面找
    //  query | create | update | remove 
    // 四个命令的执行方法
    // 所有的菜单项目回调函数，this 均为 bus
    searchMenu : [{
        // @see 菜单控件配置
    }]
    
    // 对于对象部分的菜单设定
    // 可以是一个函数，或者直接是一个数组，表示对象
    // 菜单部分设定
    objMenu : {c}F(obj) : [{
        // 参见 searchMenu 部分菜单设定
    }]

    // 元数据字段定义，与表单控件的配置项相同
    // th_search | th_obj | th_obj_index 需要这个字段
    fields : []

    // 对于搜索过滤器部分的设置
    searchFilter : {}
    
    // 对于搜索列表部分的设置
    searchList : {}
    
    // 对于搜索翻页部分的设置
    searchPager : {}
    
    // 对于元数据显示表单的配置
    meta : {
        // 更新元数据的方法
        //  - obj 对象完整的元数据
        //  - key 不指定的话表示全部更新，否则只更新指定的 key
        //  - callback 更新完毕后的异步回调
        update : {c}F(obj, key, callback);
    
        // 元数据表单更多的设置
        setup : {}
    }
    
    // 对于 detail 文本的配置
    detail : {
        // 如何读取数据的 detail
        read : {c}F(obj, callback),
        
        // 如何保存数据的 detail
        //  - det : {tp:"txt", brief:"xx", content:"xxx"}
        save : {c}F(obj, det, callback),
        
        // 针对内容为 markdown 的转换配置
        markdown : {
            media   : {c}F(src),   // 如何转换媒体链接
            context : ..           // 回调上下文   
        }
    }
    
    // 对于媒体的配置信息
    media : {
        // 支持多个媒体还是仅仅一个媒体, 默认 true
        multi : true
               
        // 如果已经存在，是否覆盖（如果不能覆盖，则改名）
        // 默认 true
        overwrite : true
        
        // 上传前，是否先检查一下是否可以上传
        //  - fnm  : 文件名
        //  - mime : 内容类型
        //  - size : 文件大小(字节）
        filter : {c}F(File):Boolean
    
        // 如何列出对象所有的媒体
        list  : {c}F(obj, callback),
                    
        // 移除一个媒体
        remove : {c}(obj, callback),
        
        // 上传一个媒体
        upload : {c}F({
            obj  : {..}     // 当前正在编辑的对象
            file : File     // 要上传的文件
            overwrite : true  // 与本配置项的 overwrite 相等
            progress : F(pe)  // 参数pe为一个浮点数，表示上传进度，比如  0.4321
            done : F(newObj)  // 上传成功后的回调 newObj 为新的附件 WnObj
            fail : F(re)      // 上传失败收的回调，re 为 AjaxReturn 格式的失败对象
        }),
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
        "change:meta" : {c}F(obj, key)
        
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
所有的回调的上下文(*即,this*)都为一下面的对象

```
{
    UI          : ZUI,   // 当前 UI
    manager   : ZUI,
    search    : ZUI,
    obj       : ZUI,
    data      : ZUI,
}
```

- 当然，如果你仅仅是创建了某个子控件，那么其他控件就会为空
- 只有自己和自己的子控件才会在上下文里找到

