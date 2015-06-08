---
title:网站的DOM规范
author:zozoh
tags:
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


[theme]:  ext_site_theme.md      "网站的主题"
[dom]:    ext_site_rule_dom.md   "网站的DOM规范"
[js]:     ext_site_rule_js.md    "网站的JS规范"
[css]:    ext_site_rule_css.md   "网站的CSS规范"
[i18n]:   ext_site_rule_i18n.md  "网站的本地化规范"
[layout]: ext_site_layout.md     "网站的布局"
[lib]:    ext_site_lib.md        "网站的组件编写规范"

