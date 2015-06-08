---
title:网站的组件规范
author:zozoh
tags:
- 扩展
- 网站
---

# 定义一个组件

```html
<!-- 
组件必须由一个 section 包裹
-->
<section>
<!-- 
一个组件可以由多个 <template class="lib-prop"> 来规定它有多少动态属性。
其中 name 属性表示该动态属性的名字， type 为值的类型。
一个组件的属性值可能有下面这些类型:
 - str   : [默认]字符串
 - int   : 整数
 - float : 浮点数
 - link  : 链接
 - bool  : 布尔
 - list  : 列表
 - obj   : 复杂对象
 - markdown : Markdown 文本
 - html  : HTML 文本
 -->
<template class="lib-prop" name="links" type="obj">
    <!-- 
    IDE 的属性面板信息，更多信息请参看 《网站IDE的属性面板》
    -->
    <script type="text/x-template" class="IDE">
    {
        text  : "i18n:menu-simple.links",
        type  : "linkList",
        conf  : {}
    }
    </script>
    <!-- 
    如何设置值, 这个是一个函数体，内容为一个匿名函数
    系统会通过 eval 将这个函数解析并保存。以便之后随时调用。
    
     - selector 属性如果未定义，则函数的 this 为顶层DOM
     - val 参数为传入的值，函数的任务就是根据这个值正确的修改自己的 DOM 树
     - 属性面板逻辑会将用户的输入进行翻译，
     - 比如 site:xx 会被翻译成一个可以直接使用的 IMG 链接
     
    -->
    <script class="SETTER" selector="ul">
    function(val){
        var jq = $(this).empty();
        for(var i=0; i<val.length; i++){
            var v = val[i];
            var li = $('<li><a href='+v.href+'>'+v.text+'</a></li>');
            li.appendTo(jq);
        }
    }
    </script>
</template>
<!--
组件的 DOM 结构 
 -->
<template class="lib-dom">
    <div class="menu-simple">
        <ul></ul>
    </div>
</template>
<!-- // 结束 -->
</section>
```


[theme]:  ext_site_theme.md      "网站的主题"
[dom]:    ext_site_rule_dom.md   "网站的DOM规范"
[js]:     ext_site_rule_js.md    "网站的JS规范"
[css]:    ext_site_rule_css.md   "网站的CSS规范"
[i18n]:   ext_site_rule_i18n.md  "网站的本地化规范"
[layout]: ext_site_layout.md     "网站的布局"
[lib]:    ext_site_lib.md        "网站的组件编写规范"

