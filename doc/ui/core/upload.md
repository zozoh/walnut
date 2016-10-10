---
title:上传控件
author:zozoh
---

# 控件概述

在 $pel 内创建一组 DOM，用来提供用户的文件上传界面


# 如何创建实例

```
/*
 new UploadUI({
     //.......................................
     // 设置标题，如果 false 将隐藏标题栏
     // 自定义标题支持字符串模板，占位符为 target 的所有字段
     title : false|true|"自定义",
     
     //.......................................
     // 指定多文件上传，默认为 false
     multi : true | false,
     
     //.......................................
     // 一个 WnObj 的实例表示上传目标
     // 如果是个目录就是多文件上传，如果是文件就是单文件上传
     target  : {
         id : "34acde..",     // 如果指定了 ID 则必须是已经存在的目标
         ph : "/path/to/ta",  // 否则采用 Path, 不存在就创建
         race : "DIR"         // 特指 ph 对应对象的类型
     },
     
     // 仅对单文件上传有效，没有声明，则不显示文件预览
     // 如果给的是一个 url，则位对象默认预览图
     // 如果 target.id 存在，则用 target.id 预览
     preview : true | "/url/to/target",
     
     //.......................................
     // 如果是多文件上传，重名文件是否自动替换
     // 默认 false
     replaceable : false | true,
     
     //.......................................
     // 声明校验函数，或者一个正则表达式验证文件名
     validate : function(file){
     throw "如果错误抛出错误消息"
     } || "^.+[.]png$" || /^.+[.]png$/
     
     //.......................................
     // 声明某个文件上传后的处理
     // this 为 Upload 的 UI 本身
     // 当 status == "done" 时 re 为 WnObj 的 JSON
     // 当 status == "fail" 时 re 为 AjaxReturn 的 JSOn
     // status 为 "done" 或 "fail"
     done : function(re){..}
     fail : function(re){..}
     complete : function(re, status) {..}
     
     //.......................................
     // 上传不再工作时调用，批量上传，最后一个文件处理完毕后调用
     // 它和你声明的 complete 不冲突，会在它之后调用
     // this 为 Upload 的 UI 本身
     // objs 为本次上传一共成功的文件对象列表。单选的话就是一个对象
     finish : function(objs){..}
     
     // 多文件上传，当最后一个文件处理完了才会调用
 }).render();
```

