---
title: 对象列表
author:zozoh
---

# 控件概述

olist 假想输入是一组 `WnObj` 对象，它会将对象显示成一个列表。
同时它也提供对这个列表的数据操作方法

# 如何创建实例

```
new UIOList({
    $pel  : $(document.body),
    
    /*
    获取数据的方法，它的值可能性比较多:
    - 数组为静态数据，每个数据都必须是你希望的对象，那么这个数据会被直接使用
        [..]
    - 异步获取数据: 函数
      那么你的函数必须接收一个回调，当你处理完数据，调用这个回调，把你获得数组传回来
        function(callback){
            // TODO 不管怎样，获得一个对象数组
            // 假设你的对象数组是 objList，那么你必须这样调用回调
            callback(objList);
        }
    - 异步获取数据: ajaxReturn
      假设你给的 URL 返回的是 NutzWeb 定义的 AjaxReturn 形式的对象
        {
            ajax   : "/path/to/url",  // 请求的地址
            params : {..},            // 请求的参数
            method : "GET"            // 请求方法，默认为 GET
        }
    - 异步获取数据: json 数组
      假设你给的 URL 返回的是普通 JSON 数组
        {
            json   : "/path/to/url",  // 请求的地址
            params : {..},            // 请求的参数
            method : "GET"            // 请求方法，默认为 GET
        }
    - 异步执行命令: 命令输出必须是JSON数组
        {cmd : "obj * -json"}
    */
    data : ..

    // 指定了数据对象，哪个键是表示 id，默认为 id
    idKey : "id"

    // 控件是否可以多选，如果可以多选，那么选择框两种状态的类名
    // 默认采用 Font-Awesome 的两个类，即，如果你声明
    // checkable : true，那么就相当:
    checkable : {
        checked : "fa fa-check-square-o",
        normal  : "fa fa-square-o"
    }

    activable : Boolean   // 是否可以选中激活
    blurable  : Boolean   // 是否可以取消激活，点击列表空白可取消激活
    
    // 每个项目的图标，可以设置统一图标，或者设置成 html 代码模板
    // 甚至一个函数。如果是代码模板的话，那么会用 WnObj 的值来替换占位符
    // 如果是函数，这个函数不能是异步的，如果返回 null 则表示没有icon
    icon : '<i class="o-icon-{{tp}}"></i>' | F(o):HTML,

    /* 
    如果觉得用函数定义一个 icon 的 HTML 片段太复杂，而直接用占位符太简陋，这里有一个
    折中的方案，可以声明一个专门生成 icon 的类选择器的配置段
    你可以给出一个配置表或者一个函数，来根据 obj 获取对象的图标
    当然，如果你没有声明 "icon" 段，这个配置会被忽略。
    如果声明了 F(o) 没啥好说的，这个函数被认为是同步函数，返回无效则等效与空串
    如果声明了配置表，它的格式需要是:
        {
            key : "nm",    // 表示采用对象的哪个 key 来获取 icon
            map : {
                "a" : "fa fa-plus",
                "b" : "fa fa-map"
            }
            dft : "others"  // 如果在映射里找不到值，就用这个默认值
                            // 当然如果没声明的话，默认认为是空串
        }
    无论怎样，控件都会在对象根据你给定的值，生成一个 o['_icon_class'] 属性，你在 icon
    模板里可以用 '{{_icon_class}}' 来直接使用。如果这个值为否，那么整个 icon 都不会被输出
    */
    iconClass : {..} | F(o)

    // 对于列表项显示的内容也是一样，可以用 html 代码模板，或者一个函数
    text : Tmpl(HTML) | F(o):HTML

    // 列表还有一个显示文字的问题。通过 display 段的定义，可以为每个字段定制显示方式
    // 比如，定义了 "nm"，那么会在对象里增加一个 "_display.nm" 的字段。
    // 值根据配置会显示相应的值，如果没有，则采用原值。
    // 在 text 模板中，可以用 "{{_display.nm}}" 取得到这个值
    display : {
        nm : {
            "abc" : "i18n:key.to.abc",
            "xyz" : "随便一个名字"
        }
    }

    // 定义一个过滤器，在绘制数据之前，每个数据都要经过过滤函数
    // 函数 return 一个经过过滤的绘制对象。 如果返回空或未定义，那么将不绘制
    filter : F(o)

    // 定义一个自定义的比较方法，这样，输出列表的时候，就会默认依照这个排序
    comparer : F(o1,o2)

    // 下面是一组回调，当响应行为发生的时候会触发对应的回调
    // 同时作为 Backbone 的插件，它也会触发一个消息，请参看对应的注释
    // 所有的回调函数的 this 都为 UI 对象本身
    // 参数如果出现了 o 表示最相关的数据， jq 表示数据相关的 $(DOM)
    on_change  : F([o..])    // "olist:change"  首次加载一定会被触发
    on_checked : F([o..])    // "olist:checked" 参数为本次被选的对象
    on_uncheck : F([o..])    // "olist:uncheck" 参数为本次被选的对象
    on_actived : F(o,jq)     // "olist:actived" 参数为本次激活的对象
    on_blur    : F(o,jq)     // "olist:blur"    参数为列表中已经激活的项目

}).render();
```

# 控件方法

## getActived

```
// 获取当前被激活的对象
var o = olist.getActived();
```

## active

```
// 将某个对象变成激活状态，会触发对应的回调和消息
olist.setActived(Element | jQuery | Number | "ID" | {..})
```

* 对于 Element 和 jQuery，则表示 dom 节点，那么这个节点如果是 '.list-item' 则会被激活
* 如果是数字，那么会选择列表内的项目激活，当然超出列表的则无视
* 负数则表示从后面开始计算，-1 表示列表最后一个元素
* 如果是普通对象，代表查找，则依次匹配列表的数据，发现完全匹配的，就激活
* 如果是字符串，则表示数据的 ID

## blur

```
// 如果列表有激活的项目，取消它，会触发对应的回调和消息
olist.blur()
```

## getChecked

```
// 获取当前被选中的对象，当然如果多选开关没开或者没有项目被选中，永远返回空数组
var objList = olist.getChecked();
```

## check

```
// 将给定的东东，全部选中，并触发相应的消息
// 如果没有参数，则表示全选
olist.check(Element | jQuery | Number | "selector" | {..})
```

## uncheck

```
// 将给定的东东，全部选中，并触发相应的消息
// 如果没有参数，则表示全部取消
olist.uncheck(Element | jQuery | Number | "selector" | {..})
```

## toggle

```
// 将给定的东东，全部反选，并触发相应的消息
// 如果没有参数，则表示全部反选
olist.toggle(Element | jQuery | Number | "selector" | {..})
```

## getData

```
// 获取当前正在显示的 WnObj 的数组
var objList = olist.getData();
```

## setData

```
// 让控件根据给定数据进行显示刷新
// d - 数据，与 options.data 意义相同
// permanent - 为 true 则修改 options.data 的设定，即下次 refresh 还是用这个数据
olist.setData(d, permanent)
```

## addLast

```
```


## refresh

```
// 重新从 options.data 处获取数据，并刷新显示
```







