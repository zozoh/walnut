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
<head>
    <title>${i18n:siteTitle}</title>
</head>
<body>
<h1>${i18n:siteTitle}</h1>
<div layout-type="block">
    <div class="layout-block-outer"></div>
        <div class="layout-block-inner" gasket="slogan">
            <!--// gasket 声明的扩展点, 可以插入任意组件 -->
        </div>
    </div>
</div>
<div theme-name="banner" class="ide-editable">
    <!--
    声明了 .ide-editable 的 DOM，下面必须有 .ide-conf 的
    模板元素，内容是一段 JSON，描述了 IDE 怎么用属性面板编辑本节点极其子节点
     - 当然，考虑到嵌套，声明了 .ide-editable 子节点会被忽略
     - 就是说在 IDE，点击任何一个节点，属性面板编辑的就是最近一级的 .ide-editable
     - 如果子节点没有 .ide-conf，全当是 "{}"
    -->
    <template class="ide-conf">
    <!-- 这里是配置信息，详细，请看 《网站IDE的属性面板》 一节-->
    </template>
    <!-- 这个是固定的 DOM 节点 -->
    <div>
        <ul class="banner-images"></ul>
        <ul class="banner-dotte"></ul>
    </div> 
</div>
<div layout-type="row">
    <div class="layout-row-outer">
        <div class="layout-row-inner">
            <div class="layout-cell" gasket="main"><div>
            <div class="layout-cell" gasket="side"><div>
        </div>
    </div>
</div>
</body>
</html>
```

* 这个模板很明显，我们有三个扩展点:
    * slogan
    * main
    * side
* 同时这个模板也允许IDE编辑自己的 *banner* 区域
* IDE 现在支持 *.html* 和 *.md* 文件
* *.html* 文件我们可以允许你编辑这三个扩展点，编辑的内容会存在 *html* 文件内
* *.md* 文件 IDE 不允许你编辑这三个扩展点
    - 因为编辑的信息不知道怎么存放比较好
    - 而且一般你也用不着
    - 渲染时 *.md* 文件本身会被转成 HTML 添入某一个扩展点
        + 首先会看看有没有叫 `main` 的扩展点
        + 没有的话，就找第一个扩展点填充
    - 因此如果你打开了 *.md* 文件，属性面板什么都不会显示
* 因为 *.md* 比较简单，我们就来说说 *.html* 是怎么扩展这三个扩展点的吧

## HTML 文件

```html
<div template="home">
    <div extend="slogan">
    </div>
</div> 
```


# 占位符

模板和组件都支持占位符

1. 占位符的形式为 `${xxxxx}` 比如 `${myName}`，占位符名就是 *myName*
2. 大小写敏感
3. `${i18n:xxxx}` 形式的占位符表示多国语言输出
    + 如果没有找到对应的值，我就会输出键
    + 如果不想输出键，可以用 `!` 开头，比如 `${!i18n:xxx}`
4. 占位符其他的值，在 `site.conf` 里定
5. 占位符输出，不会逃逸 HTML 标签，即你可以直接在多国语言里写点 HTML 标签
6. 如果想逃逸 HTML 标签，占位符用 `^` 开头
    + 比如 `${^i18n:xxxx}`
    + 或者 `${^copyright}`


# 我的一点把戏

1. 你的组件 DOM 顶层节点在渲染的时候，我会插入 `class="路径"` 作为运行时标识
    *  





[theme]:  ext_site_theme.md      "网站的主题"
[dom]:    ext_site_rule_dom.md   "网站的DOM规范"
[js]:     ext_site_rule_js.md    "网站的JS规范"
[css]:    ext_site_rule_css.md   "网站的CSS规范"
[i18n]:   ext_site_rule_i18n.md  "网站的本地化规范"
[layout]: ext_site_layout.md     "网站的布局"
[lib]:    ext_site_lib.md        "网站的组件编写规范"

