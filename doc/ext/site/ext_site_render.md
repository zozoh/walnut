---
title:网站的渲染
author:zozoh
tags:
- 扩展
- 网站
---

# 怎么渲染？

1. 无论是后端还是前端，归根结底，是通过生成一个网页，用 JS 来渲染的
2. 后端，将渲染后的 DOM 树输出成 html 文件
3. 前端，主要是动态获得组件，渲染然后替换对应的扩展点
4. 总之，渲染是以组件为单位的

# 后端渲染策略

渲染的关键步骤是:

1. 展开渲染网页
2. 执行网页渲染(主要是 onload 后 JS 的那段逻辑)
3. 整理渲染后DOM

## 展开渲染网页

1. 以模板为基础，首先填充 DOM 上的各个扩展点，然后加入一段渲染脚本
2. 输出这个网页到一个本地临时文件 

```html
<html>
<head>
    <title></title>
    <!--
    这里链接上渲染脚本需要的库文件，考虑到网页的运行环境问题，链接的方式采用内联。
    以便这个网页在任何地方都能直接执行
    -->
    <script>输出内容 /gu/core/js/jquery/jquery-2.1.3/jquery.js </script>
    <script>输出内容 /a/site/page_render.js </script>
    <!--
    之后加上一段 script 作为渲染的入口，这个函数执行的时候，所有的 DOM 应该准备完毕了
    -->
    <script>                                             
    $(function(){
        site.renderDocument({
            library : $(".library")
        });
    });
    </script>
    <!--
    等网页执行完毕，<head> 内的 script 会统统被移除，换上用户在网站上指定的
    CSS 和 JS，这时候用的就是链接的方式了
    -->
</head>
<body>
    <!--
    这里依次输出网页用到的组件
    -->
    <section class="library">
        <section name="mymenu">...</section>
        <section name="topshadow">...</section>
    </section>
    ...
    <!--
    依次扩展每个扩展点    
    -->
    <div .. gasket="menu">
        <section extend="menu" apply="mymenu">
        ..
        </section>
        <section extend="menu" apply="topshadow">
        ..
        </section>
    </div>
    ...
</body>
</html>
```
这里有一个具体的展开逻辑
```
//...........................................................
展开扩展点(
    tmpl   - 模板 DOM
    ref    - 参考 DOM
    gasket - 展开后插入的目标, null 表示插入到扩展点元素下面
){
    在 tmpl 遍历所有的扩展点 gEle
        从 ref || tmpl 获得扩展方式 exts
        展开组件(gEle, exts, gasket || gEle)
}
//...........................................................
展开组件(gEle, exts, gasket) {
    清除 gEle 下面所有的扩展点
    处理 exts 的每个 ext
        找到组件 libDom // <section name="xxx">
        得到 libTmpl
        展开扩展点(libTmpl, ext, ext);
        附加 ext 到 gasket 的子节点
}
//...........................................................
```



## 执行网页渲染

1. 用 `HtmlUnit` 来执行这个本地临时文件
2. 即，用 JS 将所有的组件的 DOM 展开，即 `<section extend...>` 节点会被组件的*DOM*替代
3. 同时 *theme* 属性也会被 *class* 替代
3. 因此保证这个网页包括渲染时需要的 JS,DOM 等所有必要内容
4. 渲染的结果保留在内存中以备后续步骤使用

## 整理渲染后DOM

1. 删除 `<head>` 内所有的 `<script>`
2. 删除 `<section class="library">`
3. 删除所有的 *gasket* 属性
4. 删除所有的 *theme* 属性

# 前端渲染策略

每个页面标签都保存这样一个数据结构

```
{
    library : <section class="library">     // 存放每个加载后的组件 DOM
    template: <section class="template">    // 存放模板，模板定义了网页<body>的内容
    content : xxxxx                         // 存放网页或者Markdown 等内容正文
    canvas  : $(..)                         // 指向模板的显示区域
}   
```

1. 编辑模板的时候，template 就是父模板，content 就是自己
2. 修改组件属性的时候，会影响到 content 部分的属性定义
3. 添加删除组件的时候，也会影响 content 部分的属性定义
4. 每当 content 部分改动的时候会发消息 `page:change($content的组件节点)` 
5. 之后会利用组件的渲染函数渲染 convas 对应的 DOM



















