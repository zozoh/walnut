---
title:网站IDE的属性面板
author:zozoh
tags:
- 系统
- 扩展
- 网站
---

# 网站IDE的属性面板介绍

```
    布局类型 : _________________
    ---------------------------
    ...
    DOM 动态属性
    ...
    ---------------------------
    主题样式 : 
```

# 动态属性

当用户选定了 *.ide-editable* 的某个节点，面板会根据这个节点的 *.ide-conf* 显示动态属性。
动态属性的配置信息如下:

```html
<template>
{
    "src" : {
        // 项目显示文字，如果不写，则不显示项目名，支持占位符
        text      : "i18n:img.link",
        // 选择器，指明要处理哪个元素
        selector  : "img",
        //...................................................
        // 怎么从 DOM 节点里取得值
        // 里面的 this 为选择器对应的元素
        getter : function(){
            return this.src;
        },
        //...................................................
        // 得到值了，怎么设值，这个是一个函数体
        // 里面的 this 为选择器对应的元素
        // 值 val 为函数参数，是从属性面板上获取来的用户输入
        setter : function(val){
            // 如果值有错误，抛错，IDE 会显示一个对应的错误提示。
            if(!val){
                throw "${i18n:e.site.nosrc}";
            }
            this.src = val;
        },
        //...................................................
        // 值的类型
        //  - str   : [默认]字符串
        //  - int   : 整数
        //  - float : 浮点数
        //  - bool  : 布尔
        //  - list  : 列表
        //  - obj   : 复杂对象
        type  : "str|int|..",
        //...................................................
        // 属性面板上用什么控件编辑
        // 关于这个详细的内容请参看 《网站IDE的属性面板》
        uiType: "input|dropdown|.." ,  // UI 控件的类型
        uiConf: ..                     // UI 控件的配置信息
    }
}
</template>
```

## 控件:input

```json
{
    uiType : "input",
    uiConf : {
        placeholder : null,   // 输入框的提示信息
        max  : 0,             // 最大长度，0 表示不限制,
        trim : true,          // 是否自动去掉前后空白
        history : 0           // 记录输入的历史，0 表示不记录
                              // 如果记录历史，输入的时候会自动提示
    }
}
```

## 控件:dropdown,radio

```json
{
    uiType : "dropdown|radio",
    uiConf : [{
        text  : "i18n:key.of.text1",
        value : 1
    },{
        text  : "i18n:key.of.text2",
        value : 2 
    }]
}
```

## 控件:links

```json
{
    uiType : "links",
    uiConf : {

    }
}
```

## 控件:pickobj

```json
{
    uiType : "pickobj",
    uiConf : {
        base : "id:345cde..",    // 从哪个对象开始查找
        query : {                // 查询对象
            tp : "^png|jpg|jpeg$"
        }
    }
}
```

## 控件:pickobjs

```json
{
    uiType : "pickobj",
    uiConf : {
        base : "id:345cde..",    // 从哪个对象开始查找
        query : {                // 查询对象
            tp : "^png|jpg|jpeg$"
        }
    }
}
```

## 控件:html

```json
{
    uiType : "pickobj",
    uiConf : {
        base : "id:345cde..",    // 从哪个对象开始查找
        query : {                // 查询对象
            tp : "^png|jpg|jpeg$"
        }
    }
}
```





