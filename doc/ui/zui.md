---
title:ZUI 框架
author:zozoh
---

# 概述

                      ZUI    
                       ^
                  提供命令菜单支持
                    ZActionUI
                       ^
                       |
       +---------------+----------------+
       |                                |
    ZLayoutUI                        ZStreamUI
    提供布局支持                       流式布局+浮动区域
       ^
    wedit | wshelf ...    # 各种具体应用


# 布局

`layout.js` 提供了布局相关的功能，它将在 `init` 的时候分析 *_ui.html* 模板，根据模板给出的代码结构分析出一个布局层级树:

    比如根据如下的 DOM 结构
    .ui-arena  layout-mode="vertical"   //  水平或者垂直布局
        DIV layout-name="chute" layout-val="*"       // * 表示尽可能多的分配
        DIV layout-name="vbar"  layout-val="20%"     // 支持百分比
        DIV layout-name="icons" layout-val="132"     // 支持绝对像素值
        DIV layout-val="*" layout-mode="horizontal"  // 进入下一层
            DIV layout-name="main" layout-val="*"
            DIV layout-name="side" layout-val="200"
    
    我们可以得到一个树形对象，以及一组快捷的访问方式
        layout : {
            // 记录了从 .arena 开始的布局
            _root_ : {
                $ele : $(.arena),
                mode : "vertical",
                children : [
                    {$ele:$(DIV), name:"chute", val:"*"},
                    {$ele:$(DIV), name:"vbar",  val:0.2},
                    {$ele:$(DIV), name:"icons", val:132},
                    {$ele:$(DIV), val:"*", children: [
                        {$ele:$(DIV), name:"main", val:"*"},
                        {$ele:$(DIV), name:"side", val:200},
                    ]},
                ]
            }
            // 快捷的DOM访问名，可以用 this.layout.chute 来访问到
            chute : $(DIV)
            vbar  : $(DIV)
            icons : $(DIV)
            main  : $(DIV)
            side  : $(DIV)
        }

所有被设置了 layout-mode



# 用户交互命令菜单

界面提供用户交互命令菜单，菜单项由命令 `actions $appName $obj` 来获取，
其返回是一个 JSON 对象:

    [
        {
            pin    : true|false,  // 是否固定显示
            text   : "action.txt.key",
            action : {
                type : "cmd|mod_msg|ui_msg",
                data : "cat -id ${ID}"
            }
        },{
            newgroup : true
        },{
            // 下一个菜单项
        }
    ]


    
