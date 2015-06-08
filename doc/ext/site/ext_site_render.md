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

以模板为基础，首先填充 DOM 上的各个扩展点，然后加入一段渲染脚本

```html
<html>
<head>
    <title></title>
</head>
<body>
    ...
    <div .. gasket="menu">
        <section extend="menu" apply="mymenu">
        ..
        </section>
    </div>
    ...
</body>
</html>
```
























