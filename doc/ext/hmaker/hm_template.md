---
title:站点模板
author:zozoh
tags:
- 扩展
- hmaker
---

# 什么是站点模板

任何一个站点都可以选用一套模板，来为 `dynamic` 控件数据提供 DOM 渲染逻辑. 模板存放在域中 `.hmaker` 目录下以便多个站点共享

```
~/.hmaker/template
    templateA           # 模板所有的资源存放目录
        templagte.info.json  # 一个JSON文件，以便编辑器理解皮肤
        jquery.fn.js         # 模板的 jQuery 插件
```

# templagte.info.json

```
{
    // 模板的 jQuery 插件名
    "name"     : "wn_plst_th_video",
    
    // 模板显示名
    "title"    : "默认视频列表",
    
    // 可以匹配什么样的数据接口返回类型
    // 可选值为: list | obj
    "dataType" : "list",
    
    // 模板选项表，格式参见 《动态设置》
    "options"  : { ... }
    
    // 指定了服务器端渲染模板
    "dom" : {
        "fileName" : "dom.wnml",  // 模板文件名，默认 dom.wnml
        "varName"  : "mo"         // 模板主变量名，默认为控件 ID
        "dataKey"  : "pager"      // 生成 jQuery 调用时，是否传入数据
                                  // 如果为 "@" 则表示整个对象
                                  // 空表示不传递
    }
}
```

> [《动态设置》](hm_setting.md)

# jquery.fn.js

就是一个标准的 jQuery 插件。 发布程序会生成下面的调用代码:

```
$(function(){
    $('#dynamic_1 > .hmc-dynamic').hmc_dynamic({
        // 控件的配置项，其中 options 段表示模板实例配置选项
    }, {
        // 数据，由 dom.dataKey 声明
    });
});
```

# dom.wnml

为了考虑到减少请求次数，以及搜索引擎优化，模板支持在服务器端渲染。
即，`hmaker publish` 会根据动态控件的具体配置信息，生成一段 wnml 代码，
这样请求动态控件，拿到的 dom 就已经是在服务器端渲染好的。

这就要求，模板还需要同时提供一个 wnml 的代码模板。 当然，如果你不提供， `hmaker publish` 会生成一个默认的模板，即裸数据，还是能有效的减少请求次数，只不过不利于搜索引擎优化而已。

默认的，你需要在模板目录下提供 `dom.wnml` 作为模板，如果也可以不是这个文件名，但是需要在 `template.info.json` 里面的 `dom.fileName` 段指定。

这个模板文件遵守基本的 `Nutz Tmpl` 语法，即支持 `${xxx}` 形式的占位符，只不过对占位符有特殊约定，
我们先举个例子:

```html
<each var="${@@}" items="${comId}.list">
    <div class="wn-li-media">
        <div class="wlm-thumb" style="background-image:url(${API}/thumb?${@@thumb})">
            <em>${@displayText.info_em}</em>
            <u>${@displayText.info_u}</u>
            <b>${@displayText.info_b}</b>
            <a class="wlm-ta"><span></span></a>
        </div>
        <ul class="wlm-info">
            <li class="wlm-ih">
                <a href="${href?}?${paramName}=${@@=objKey}">${@displayText.title}</a>
            </li>
            <li class="wlm-is">${@displayText.brief}</li>
        </ul>
    </div>
</each>
```

`hmaker publish` 将利用这段代码，生成一个 `wnml` 的代码片段，插入到控件对应的 *arena* 部分。渲染这段代码之前，程序会准备替换上下文，上下文中将包括:

- `comId` : 控件的 ID

对于模板每个占位符，发布程序是这么理解的

- `@@` : 模板主变量名，可以在 `template.info.json` 里面的 `dom.varName` 段指定。默认为控件 ID
- `@@=xx` : 形式的占位符（开头有两个@加一个=），需要查找控件的选项的 `options` 段，
    + 如果值为 `str` 则，会生成 *wnml* 支持的占位符格式 `${var.str?}`
    + 这里的 *var* 为模板主变量
    + 结尾的 `?` 是占位符语法，表示没有就输出空
- `@@xx` : 形式的占位符（开头有两个@），表示直接输出主变量的键值
    + 如果值为 `str` 则，会生成 *wnml* 支持的占位符格式 `${var.xx?}`
    + 这里的 *var* 为模板主变量
    + 结尾的 `?` 是占位符语法，表示没有就输出空
- `@xxx` : 形式的占位符（开头有一个@），也需要查找控件的选项的 `options` 段，
    + `@a.b` 表示在 options 的键 *a* 下的 *b* 的值
    + `@xyz` 表示在 options 的键 *xyz* 的值
    + 如果值为 `=str` 则，会生成 *wnml* 支持的占位符格式 `${var.str?}`
    + 这里的 *var* 为模板主变量
    + 结尾的 `?` 是占位符语法，表示没有就输出空
    + 其他形式的值，则表示一个静态值

上述代码，在控件选项如下的时候：

```
    ...
    options: {
        href: "index",
        paramName: "id",
        objKey: "id",
        displayText: {
            title: "=th_nm",
            brief: "=brief",
            info_em: "A",
            info_u: "B",
            info_b: "C"
        },
        API: "/api"
    },
    ...
```

而模板配置信息为:

```
    ...
    "dom" : {
        "fileName" : "dom.wnml",
        "varName"  : "mo"
    }
    ...
```

将生成:

```html
<each var="mo" items="dynamic_1.list"> 
    <div class="wn-li-media"> 
        <div class="wlm-thumb" style="background-image:url(/api/thumb?${mo.thumb?})"> 
            <em>A</em> 
            <u>B</u> 
            <b>C</b> 
            <a class="wlm-ta"><span></span></a> 
        </div> 
        <ul class="wlm-info"> 
            <li class="wlm-ih">
                <a href="index.html?id=${mo.id?}">${mo.th_nm?}</a>
            </li> 
            <li class="wlm-is">${mo.brief?}</li> 
        </ul> 
    </div> 
</each>
```


