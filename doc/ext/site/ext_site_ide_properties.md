---
title:网站IDE的属性面板
author:zozoh
tags:
- 扩展
- 网站
---

# 动态属性

```html
<script type="text/x-template" class="IDE">
{
    // 项目显示文字，如果不写，则不显示项目名，支持占位符
    // 以 "i18n:" 开头的文字，从本地化语言字符集读取
    // 如果值为 "!notitle"，则会在属性面板不显示项目名称
    text  : "i18n:msg.key",
    // 属性面板上用什么控件编辑，如果没有这个属性，则会根据值的类型
    // IDE 默认分配一个控件来编辑。一般的说，都是 input
    type  : "input",     // 控件的类型，后面的章节会逐个介绍
    conf  : {}           // 控件的配置信息，各个控件均有不同，后面有描述
}
</script>
```

# 控件一览表

## 控件:input

```json
{
    type : "input",
    conf : {
        placeholder : null,   // 输入框的提示信息
        range : [20,100],     // 数字区间，两端都是闭区间
        regex : "^.*$",       // 一段正则表达式校验 
        max   : 0,            // 最大长度，0 表示不限制,
        trim  : true,         // 是否自动去掉前后空白
        history : 0           // 记录输入的历史，0 表示不记录
                              // 如果记录历史，输入的时候会自动提示
    }
}
```

## 控件:dropdown

```json
{
    type : "dropdown",
    conf : [{
        text  : "i18n:key.of.text1",
        value : 1
    },{
        text  : "i18n:key.of.text2",
        value : 2 
    }]
}
```

## 控件:radio

```json
{
    type : "radio",
    conf : .. // 参见 dropdown 控件，嗯? 找不到？向上看 ---^
}
```

## 控件:linkList

```json
{
    type : "linkList",
    conf : {

    }
}
```

## 控件:objPicker

```json
{
    type : "objPicker",
    conf : {
        base : "id:345cde..",    // 从哪个对象开始查找
        query : {                // 查询对象
            tp : "^png|jpg|jpeg$"
        }
    }
}
```

## 控件:multiObjPicker

```json
{
    type : "multiObjPicker",
    conf : {
        base : "id:345cde..",    // 从哪个对象开始查找
        query : {                // 查询对象
            tp : "^png|jpg|jpeg$"
        }
    }
}
```

## 控件:textEditor

```json
{
    type : "textEditor",
    conf : {}
}
```

## 控件:htmlEditor

```json
{
    type : "htmlEditor",
    conf : {}
}
```



