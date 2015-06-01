---
title:网站的DOM规范
author:zozoh
tags:
- 系统
- 扩展
- 网站
---


# 主题的DOM约定

看完了上面一节酣畅淋漓的介绍，我们再说说 DOM。 诚然，DOM 就是一坨 HTML 代码而已，
我们依然会将其分作下面几类:

1. 模板DOM: `$THEME/template/$mytmpl` 目录下
    * 一个页面只会用到一个模板 DOM。(*当然我知道这是废话，但是有些人的智商... 你懂的*)
    * 生成的页面不会被我在包裹上其他乱七八糟的代码，`<body>` 下面就是模板的 HTML
    * 明白了吗？
2. 组件DOM: `$THEME/lib/$mylib` 目录下
    * 组件的 DOM 会被插入模板的各个扩展点
    * 什么是扩展点？ 就是声明了 `gasket` 属性的元素
    * 组件只要有扩展点，就可以再嵌套组件了
3. 这两种 DOM [语法][dom]基本一致，只是[约定](#一点点额外要求)有一点点不同

# 一点点额外要求

1. 模板的 DOM 可以有多个顶层节点，其中每个顶层节点:
    * 必须有自定义属性 `layout-type` 确定布局类型
    * 布局就有限的几种，一旦确定，必须按照要求建立DOM, 你可以看[文档][layout]了解更多
    * 属性 `theme-name` 用来选择符合[CSS规范][css]的样式，
2. 组件的 DOM 只能有一个顶层节点，该顶层节点:
    * 属性 `theme-name` 用来选择符合[CSS规范][css]的样式
    * 组件的编写是个技术活，请参照[文档][lib]

# 举个栗子

## 模板文件

假设我们有个模板 `template/home`

```html
<html>
<head><title></title></head>
<body template="blank">
<div layout="block" theme="navbar" gasket="导航条">
    <div extend="导航条" apply="简单导航条">
        <template>{height : "90px"}</template>
        <div extend="logo" apply="logo">
            <template>{src:'site:/imgs/mylogo.png'}</template>
        </div>
        <div extend="menu" apply="menu-simple">
            <template>
            {
                mode  : "horizontal",
                links : [
                    {text:"首页", href:"site:/page/index.hml"},
                    {text:"下载", href:"site:/page/download.hml"},
                    {text:"文档", href:"site:/page/documents.hml"},
                    {text:"资源", href:"site:/page/resource.md"},
                    {text:"关于", href:"site:/page/about.md"}
                ]
            }
            </template>
        </div>
    </div>
</div>
<div layout="block" theme="banner" gasket="形象图"></div>
<div layout="row" theme="arena" nm="主区域">
    <div gasket="main" nm="内容区"><div>
    <div gasket="side" nm="侧边栏"><div>
</div>
</body>
</html>
```

## HTML 文件

```html
<div template="home">
    <div extend="形象图" apply="轮换图">
        <template>
        {
            width  : "100%",
            height : "300px",
            interval : 1000,
            images : [
                "site:/imgs/banner/00.jpg",
                "site:/imgs/banner/01.jpg",
                "site:/imgs/banner/02.jpg",
            ]
        }
        </template>
    </div>
    <div extend="内容区" apply="HTML片段">
        <template>
            <h1>哈哈哈</h1><p>随便什么内容
        </template>
    </div>
    <div extend="侧边栏" apply="挂件栏">
        <template>{padding:20}</template>
        <div extend="内容区" apply="日期时间"></div>
        <div extend="内容区" apply="标签列表"></div>
        <div extend="内容区" apply="留言摘要"></div>
        <div extend="内容区" apply="友情链接"></div>
    </div>
</div> 
```

## 组件:轮换图

```html
<div theme="slider">
    <template class="lib-setting">
    <!--
    这里面是控件关于动态属性的声明
    动态属性的详细的内容，请参见 《网站IDE的属性面板》
    -->
    </template>
    <template class="lib-dom">
        <div class="slider slider-light">
            <div class="wrapper">
                <ul class="slider-items">
                <ul> 
            </div>
        </div>
    </template>
</div>
```

## 组件:HTML片段

```html
<div theme="fragment">
    <template class="lib-setting" content="html">
    </template>
    <template class="lib-dom">
        <div class="fragment">i18n:fragment.placeholder</div>
    </template>
</div>
```

1. `.lib-setting` 的 *content* 属性有下列的值
    + **json** : 声明动态属性面板，如果不声明 content，默认就是这个
    + **html** : 属性面板就显示一个 HTML 输入框，允许用户书写 HTML
    + **markdown** : 属性面板显示一个 markdown 编辑器
2. 通过 *content="html"* 实际上允许用户在属性面板里书写任意的HTML代码
3. 无论是 *html* 还是 *markdown*，属性面板用户编写的内容，将完全替代 *.lib-dom* 下内容

## 组件:日期时间

```html
<div theme="lib-datetime">
    <template class="lib-setting">
    <!--
    这里面是控件关于动态属性的声明
    动态属性的详细的内容，请参见 《网站IDE的属性面板》
    -->
    </template>
    <template class="lib-dom">
        <div class="date"><!--// 日期的DOM--></div> 
        <div class="time"><!--// 时间的DOM--></div> 
    </template>
</div>
```


[theme]:  ext_site_theme.md      "网站的主题"
[dom]:    ext_site_rule_dom.md   "网站的DOM规范"
[js]:     ext_site_rule_js.md    "网站的JS规范"
[css]:    ext_site_rule_css.md   "网站的CSS规范"
[i18n]:   ext_site_rule_i18n.md  "网站的本地化规范"
[layout]: ext_site_layout.md     "网站的布局"
[lib]:    ext_site_lib.md        "网站的组件编写规范"

