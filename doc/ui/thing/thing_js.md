---
title: Thing·配置文件
author:zozoh
---

-----------------------------------------------------
# 配置文件·概述

```bash
THING_SET
   |-- thing.js    # 用户配置文件
   |-- index/      # 索引目录
   |-- data/       # 数据目录
```

- `thing.js` 是留给用户进行修改的配置文件
- 在创建 `th3_main` 实例时可以在 `options` 里面定制比该配置文件更高优先级的配置项

-----------------------------------------------------
# 配置文件·格式化说明


```js
{
    // 指定布局文件，默认为
    // ui/thing3/layout/col3_md_ma.xml
    layout : "ui/thing3/layout/col3_md_ma.xml",
    // 对于搜索部分菜单的设定
    // 如果为空，则会默认从 actions 里面找
    //  query | create | update | remove 
    // 四个命令的执行方法
    // 所有的菜单项目回调函数，this 均为 bus
    searchMenu : [{
        // @see 菜单控件配置
    }]

    // 顶级菜单项
    topMenu : [{
        // @see 菜单控件配置
    }]
    
    // 元数据字段定义，与表单控件的配置项相同
    fields : []

    // 对于搜索过滤器部分的设置
    searchFilter : {}
    
    // 对于搜索列表部分的设置
    searchList : {}
    
    // 对于搜索排序部分的设置
    searchSorter : {}
    
    // 对于搜索翻页部分的设置
    searchPager : {}
      
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
    
    //......................................
    // 唯一键约束
    // 定义了一个数组，数组中的每个元素都是一组唯一的键
    // 当 thing create/update 的时候，均不能导致这些键重复
    uniqueKeys : [{
        name : ["key1"],     // 一个元素的数组表示单个键
        required : true      // 数据不能为空，默认false,只有提交数据有值才检查
    }, {
        name : ["key1", "key2"], // 数组表示符合键
        required : false         // 如果 true，表示所有的键都不能为空
                                 // false 则表示只有所有键都为空才能通过检查
                                 // 如果一个键为空一个不为空，直接不检查就抛错    
    }],
    //......................................
    // 链接键
    // 定义了一映射表，将本字段的值映射到其他字段，甚至其他表的某几个字段
    // 如果发生了 create/update， 会自动修改生/成对应的字段
    // 如果发生了 delete 那么根据配置，会自动删除对应其他表的记录
    // 这个配置项是一个 Map，键即为自己的键
    lnKeys : {
        //----------------------------------
        // 详细说明
        "key1" : {
            // 【选】如果不指定，那么就不检查
            // 如果检查不通过，则根据下面的配置项决定是抛错，还是忍耐
            match  : '^((g2)|(g3))$',
            // 严格模式开关，如果开启严格模式，那么对于 match，必须匹配才可以
            // 当然如果本身值就是 null，那么都是无视的
            strict : true,
            // 【选】如果不指定，则使用当前数据对象
            // 这里可以指定一个过滤条件，符合这样条件的数据会被挑选出来
            // 执行后面的 set
            target : {
                // 【选】另外一个 ThingSet 的路径
                // 如果不指定，就是自己
                thingSet : '~/xxx',
                // 【选】过滤条件，如果不指定则为全部数据
                // 每个过滤项的值，遵守 set 字段值的规范
                // 上下文为当前的数据对象
                filter   : {..}
            },
            // 设置值的方法
            set : {
                // 值 "@val" 表示原始值，将直接保留原来值的类型
                "ta_key1" : "@val",
                // 如果声明了 match，那么会在上下文添加 g1-n 的变量
                // 类型都是字符串
                "ta_key2" : "@g2",
                // 你也可以指明类型:
                //  - int     : Integer : 整型
                //  - float   : Float   : 浮点
                //  - boolean : Boolean : 布尔
                //  - string  : String  : 字符串
                // 默认为 string
                // 这种语法 @val 形式也支持
                "ta_key3" : "@g3:int"
                // 如果想组合复杂一点的值，可以
                // 这种形式下，值只能被设置成字符串
                // 即，非 @xx 形式的值，会被认为是字符串模板
                "ta_key4" : "${g2}_${g4}"
            }
        },
        //----------------------------------
        // 将自己分解成多个值，分别设置到其他字段
        "key2" : {
            match : '^([A-Z]+)-([A-Z]{2})([0-9]+)-([0-9]+)$',
            set : {
                "dev_tp" : "@g1",
                "spl_nm" : "@g2",
                "spl_md" : "@g3",
                "spl_nb" : "@g4:int"
            }
        },
        //----------------------------------
        // 自己的值设置到另外的数据集的多个记录
        "key3" : {
            target : {
                thingSet : "~/xxx",
                filter : {
                    'ta_id' : "@id"
                }
            },
            set : {
                "ta_key3" : "@val"
            }
        }
    },
    //......................................
    // 导入段的设置
    dataImport : {
        enabled    : true,      // 如果此项为 true 则开启导入
        uniqueKey : "phone",    // 唯一数据键
        mapping   : "~/.sheet/测试数据_import"  // 映射数据
        accept    : ".csv, .xls"  // 可以接受的导入文件格式
        // 执行导入命令时每条记录的输出模板
        // 其中特殊占位符 `${P}` 表示进度，其他的占位符为数据字段键名
        processTmpl : "${P} ${th_nm?-未知-} : ${phone?-未设定-}",
        // 上传前设置所有数据的固定字段
        fixedForm : "~/.sheet/xxx.js",  // 一个表单form的 config JS
        // 导入一条数据后的后续处理
        // 每当创建一个数据，数据会变成 JSON 经过管道，传递给
        // 这个命令，当这个命令执行出错，后续执行将被阻断
        afterCommand : "jsc /jsbin/xxxx.js -vars"
    },
    //......................................
    // 导出段的设置
    dataExport : {
        enabled    : true,    // 如果此项为 true 则开启导出
        exportType : "xls",   // 默认导出类型。支持 csv|xls
        pageRange  : false,   // 导出页码，false 表示全部导出
        pageBegin  : 1,       // 默认起始导出页码
        pageEnd    : 10,      // 默认结束页码
        audoDownload : true,  // 导出完毕后自动下载
        // 执行导出命令时每条记录的输出模板
        // 其中特殊占位符 `${P}` 表示进度，其他的占位符为数据字段键名
        processTmpl : "${P} ${th_nm?-未知-} : ${phone?-未设定-}",
    },
    //......................................
    // 扩展的命令菜单
    extendCommand : {
        // 扩展命令的 JS 文件，是一个列表
        actions : [
            "~/xxx/action1.js",
            "~/xxx/action2.js",
        ]
    },
    //......................................
    // 事件路由表
    // 总线的事件，如果在这个映射表里，会触发对应的一个或多个事件
    eventRouter : {
        'obj:open' : ['show:comp'],
        'do:xxx'   ：'show:xxx'
    }
}
```

-----------------------------------------------------
# 菜单项说明

用户可以在下面的位置定制自己的菜单项

- `layout.xml` 的 `box` 和 `tabs` 下面
- `searchMenu` 段
- `topMenu` 段

这些地方的定义都是相同的，对于一个菜单项，基本包括下面的信息

```bash
<i..>::i18n:xxx::->doSomething  # 调用函数
<i..>::i18n:xxx::~>doSomething  # 调用异步函数
<i..>::i18n:xxx::-@do:create    # 触发消息
<i..>::i18n:xxx::~@do:create    # 触发异步消息
# 调用异步函数并切换图标和文字显示
<i..>/<i..>::i18n:xxx/i8n:xxx::~>doSomething
# 触发异步消息并切换图标和文字显示
<i..>/<i..>::i18n:xxx/i8n:xxx::~@do:create
#----------------------------------------------
--   ::i18n:xxx::->doSomething  # 只显示文字
<i..>::--      ::->doSomething  # 只显示图标
<i..>::@i18n:xx::->doSomething  # 图标上配合气泡提示
```

上述是在界面输入框内快捷输入的语法，它转化为 JSON 的表达为 

```js
[{
    // <i..>::i18n:xxx::->doSomething  # 调用函数
    text : "i18n:xxx",
    icon : '<i ...>',
    handlerName : "doSomething"
}, {
    // <i..>::i18n:xxx::~>doSomething  # 调用异步函数
    text : "i18n:xxx",
    icon : '<i ...>',
    asyncHandlerName : "doSomething"
}, {
    // <i..>::i18n:xxx::-@do:create    # 触发消息
    text : "i18n:xxx",
    icon : '<i ...>',
    fireEvent : "do:create"
}, {
    // <i..>::i18n:xxx::~@do:create    # 触发异步消息
    text : "i18n:xxx",
    icon : '<i ...>',
    asyncFireEvent : "do:create"
}, {
    // <i..>/<i..>::i18n:xxx/i8n:xxx::~>doSomething
    text : "i18n:xxx",
    icon : '<i ...>',
    asyncText : 'i18n:xxx',
    asyncIcon : '<i ..>',
    asyncHandlerName : "doSomething"
}, {
    // # 触发异步消息并切换图标和文字显示
    text : "i18n:xxx",
    icon : '<i ...>',
    asyncText : 'i18n:xxx',
    asyncIcon : '<i ..>',
    asyncFireEvent : "do:create"
}, {
    // --   ::i18n:xxx::->doSomething  # 只显示文字
    text : "i18n:xxx",
    handlerName : "doSomething"
}, {
    // <i..>::--      ::->doSomething  # 只显示图标
    icon : '<i ...>',
    handlerName : "doSomething"
}, {
    // <i..>::@i18n:xx::->doSomething  # 图标上配合气泡提示
    icon : '<i ...>',
    tip  : "i18n:xxx",
    handlerName : "doSomething"
}]
```

控件会自动转换这些命令菜单项为标准的菜单控件的

-----------------------------------------------------
# 快捷菜单项

为了方便配置，系统支持快捷菜单项，譬如

```js
searchMenu : ["@create", "@refresh", "@remove"]
```

相当于

```js
searchMenu : [{
        icon : '<i class="zmdi zmdi-flare"></i>',
        text : "i18n:th3.create",
        fireEvent : "do:create"
    }, {
        icon : '<i class="zmdi zmdi-refresh"></i>',
        tip  : "i18n:th3.refresh_tip",
        asyncIcon : '<i class="zmdi zmdi-refresh zmdi-hc-spin"></i>',
        asyncFireEvent : "list:refresh"
    }, {
        icon : '<i class="fa fa-trash"></i>',
        tip  : "i18n:th3.rm_tip",
        fireEvent : "list:remove"
    }]
```

下面我给出全部快捷菜单项的列表:

```js
{
    "@create" : {
        icon : '<i class="zmdi zmdi-flare"></i>',
        text : "i18n:th3.create",
        fireEvent : "do:create"
    },
    "@refresh" : {
        icon : '<i class="zmdi zmdi-refresh"></i>',
        tip  : "i18n:th3.refresh_tip",
        asyncIcon : '<i class="zmdi zmdi-refresh zmdi-hc-spin"></i>',
        asyncFireEvent : "list:refresh"
    }, 
    "@remove" : {
        icon : '<i class="fa fa-trash"></i>',
        tip  : "i18n:th3.rm_tip",
        fireEvent : "list:remove"
    },
    "@cleanup" : {
        icon : '<i class="fa fa-eraser"></i>',
        text : "i18n:th3.cleanup",
        fireEvent : "do:cleanup"
    },
    "@restore" : {
        icon : '<i class="zmdi zmdi-window-minimize"></i>',
        text : "i18n:th3.restore",
        fireEvent : "do:restore"
    },
    // @import 还需要配置启用开关 conf.dataImport.enabled 
    // 否则会被无视
    "@import" : {
        text : "i18n:th3.import.tt", 
        fireEvent : "do:import"
    },
    // @export 还需要配置启用开关 conf.dataExport.enabled
    // 否则会被无视
    "@export" : {
        text : "i18n:th3.export.tt", 
        fireEvent : "do:export"
    },
    "|" : {type:"separator"}
}
```


