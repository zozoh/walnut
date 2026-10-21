---
title: 编辑器模块
author: zozoh
---

# 编辑器模块概述

编辑器模块将用来编辑 `race==OBJ|FILE` 的对象的元数据和内容

# Backbone.Model

    {
        //............................................
        // 应用信息
        app : {
            name    : "console", // 应用的名称
            session : {..},      // 会话信息
            obj     : {..},      // 用户指定的起始对象，默认null
            requires: [          // 依赖的 JS 模块，一般为模块和UI两个
                "model/zclient", 
                "ui/console/console""
            ]    
        }
    }

模块支持如下消息:

    //----------------------------------------------
    // 这个消息一般由 Mod.set("content", content); 的方式触发
    // 模块会将字段， "content" 的内容保存到 "obj" 字段所对应的对象中去
    "change:content" ()
    //----------------------------------------------
    // 显示文本对象内容
    "show:text" (content)







    





















