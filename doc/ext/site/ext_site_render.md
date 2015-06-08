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

1. 生成渲染网页
2. 执行渲染网页(主要是 onload 后 JS 的那段逻辑)
3. 将渲染后DOM整理一下，之后输出成最终结果

## 后端生成渲染网页

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


## 执行渲染网页

1. 用 `HtmlUnit` 来执行这个本地临时文件





















