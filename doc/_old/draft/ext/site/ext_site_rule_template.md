---
title:网站的模板规范
author:zozoh
tags:
- 扩展
- 网站
---

# 定义一个模板

```html
<html>
<head><title></title></head>
<body>
<div layout="block" theme="navbar" gasket="nav">
    <section extend="nav" apply="简单导航条">
        <template name="height">90px</template>
        <section extend="logo" apply="logo">
            <template name="src">/imgs/zozoh.jpg</template>
        </section>
        <section extend="menu" apply="menu-simple">
            <template name="links">
            [
                {text:"首页", href:"site:/page/index.hml"},
                {text:"下载", href:"site:/page/download.hml"},
                {text:"文档", href:"site:/page/documents.hml"},
                {text:"资源", href:"site:/page/resource.md"},
                {text:"关于", href:"site:/page/about.md"}
            ]
            </template>
        </section>
    </section>
</div>
<div layout="block" theme="banner" gasket="banner"></div>
<div layout="row" theme="arena">
    <div gasket="main"></div>
    <div gasket="side"></div>
</div>
</body>
</html>
```