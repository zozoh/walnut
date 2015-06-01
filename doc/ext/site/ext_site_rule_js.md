---
title:网站的JS规范
author:zozoh
tags:
- 系统
- 扩展
- 网站
---

# 三种JS

一个主题通过 JS 来提供交互效果和与服务器交互的能力。主题的 JS 存在于:

1. 主题JS: `$THEME/js` 目录下，这些 JS 所有主题页面都会使用
2. 模板JS: `$THEME/template/$mytmpl` 目录下，这些JS只有在使用对应模板的页面才会用到
3. 组件JS: `$THEME/lib/$mylib` 目录下，这些JS只有在使用对应组件的页面才会用到

当生成渲染页面的时候，这些 JS 会统统被 `<script>` 链接到页面。

# 一点点额外要求

1. 为了防止 JS 函数定义的覆盖，你的JS定制的函数，必须包裹在自己的闭包里。
    * 当然你不写在闭包里，我也拿你没辙
    * 对于名称冲突，那你就自行承担后果咯
    * 有些人就是贱
2. 必须暴露出一个函数, 函数名为 `路径`，以便页面加载后调用。这个函数的 *this*
    * 主题JS 和 模板JS 为 document.body
    * 组件JS 为组件顶级 DOM 的 Element
3. 如果你的 JS 里没有这个方法呢？
    * 不怎么样，不会被调用的
    * 因为会预先判断一下有这个函数才调用

# 举个栗子

比如你有一个模板 `template/light`

```
template/light/js/myinit.js       # 要初始化一些业务数据
template/light/js/mybehavior.js   # 初始化一些组件的行文
template/light/js/mylib.js        # 提供一些组件的帮助方法
```

那么我们来看看这三个 JS 大概应该是什么样子:

```
myinit.js
-------------------- 以下是文件内容的分隔线 -----------------------
(function(){
    // 在这个闭包里，尽情的定义自己的方法吧
    // !!! 注意，这个闭包执行的时候，DOM 可能还没加载，因为js可能在 <head> 里引入
})();
/*
    因为路径是 template/light/js/myinit.js， 
    因此初始化函数名必须是  "template_light_js_myinit"
    函数的 this 就是 document.body
        当然，组件初始化的函数，this 则是组件的顶级 Element
    渲染页面的时候，页面会加入一个代码片段来调用这个函数
    <script>
    $(function(){
        if(typeof window.template_light_js_myinit == 'function'){
            template_light_js_myinit.call(document.body);
        }
    });
    </script>
    看到没，实际上你不提供这个定义式，你在闭包里用
    window.template_light_js_myinit = function(){...}
    的形式来声明你的初始化函数也是没有问题的。
*/
function template_light_js_myinit(){
    // 当你 template DOM 加载完了，会调用这个方法
    // !!! 注意，这时候组件的 DOM 也一定加载完了，但是组件的 JS 初始化方法 
    //     还没有被调用
}
```

下一个:

    mybehavior.js
    -------------------- 以下是文件内容的分隔线 -----------------------
    (function(){
        window.template_light_js_mybehavior = function(){
            // 绑定一些 JS 监听
        };
    })();

再下一个:

    mylib.js
    -------------------- 以下是文件内容的分隔线 -----------------------
    (function(){
        // 声明一些帮助方法
    })();

[theme]:  ext_site_theme.md      "网站的主题"
[dom]:    ext_site_rule_dom.md   "网站的DOM规范"
[js]:     ext_site_rule_js.md    "网站的JS规范"
[css]:    ext_site_rule_css.md   "网站的CSS规范"
[i18n]:   ext_site_rule_i18n.md  "网站的本地化规范"
[layout]: ext_site_layout.md     "网站的布局"
[lib]:    ext_site_lib.md        "网站的组件编写规范"