---
title:任何东西都是 Thing
author:zozoh
tags:
- 系统
- 扩展
- thing
---

# 什么是 thing

Walnut 的 `WnObj` 是对所有的数据进行的最高级的抽象。 因为抽象层次较高，所有无法围绕它进行更多的组件和命令编写。 

`thing` 就是任何东西。 它通过一组 *WnObj* 的组合，描述了以下但不限于:

* 论坛版块
* 论坛贴士
* 商品
* 订单
* 货品
* 等等

你可以认为任何一个目录下面，都存放了一组（可能是海量）的 *thing*，通常它的目录结构为:

```
# 带有 thing 元数据的目录被认为是一个 ThingSet
# 元数据所指向的 ID 为这个 ThingSet 下面每个对象的一份 JSON 定义文件
@DIR {thing:"PATH"}
    $ThingID           # 每个子目录就是一个 thing
        ...            # 每个子目录的内容，由定义文件规定
        comments       # 每个 thing 都可能有评论信息，信息可以是 md,html,txt
            45cdae.md  # 文件的元数据包括 
                       #   th_etp : "comment",  // 标识是评论
                       #   th_set : ID,         // 对应 ThingSet 的 ID
                       #   th_id  : ID          // 对应的 ThingID
                       #  .. 更多元数据参见后面的章节
``` 

# ThingSet

```
thing   : "PATH"     // 指向一个 JSON 定义文件
th_init : "PATH"     // 指向了一个app-init的配置文件，
                    // `thing init` 命令会根据当前的这个设定初始化 ThingSet
```

* 标识了元数据 `thing` 的集合就是 *ThingSet*

# ThingSet 定义文件

```
{
     name : "xxx",         // 东东的类型名称
     text : "i18n:xxx".    // 东东的多国语言显示
     icon : "<...>",       // 东东的图标 HTML
     
     // 物品的字段，这个遵守 ui/form/form 控件的 field 字段定义规范
     // 并在此基础上有下列形式的扩展
     fields : [{
        .. 子文件作为照片
     }, {
        .. 子文件作为详情
     }, {
        .. 子目录作为照片集
     }, {
        .. 子目录作为附件集
     }]
}
```

# Thing

```
nm    : ID       // 与自己的id字段相同，无意义，你可以随意修改，只不过 ls 的时候好看
tp    : "thing"  // 目录类型表示是个 Thing
pid   : ID       // 说明自己属于哪个 ThingSet
lbls  : ["xx"]   // 标签
ct    : MS       // 创建时间
lm    : MS       // 最后修改时间
thumb : ID       // 缩略图ID  @see 缩略图机制
//...........................................
th_ow_id  : "xxx"   // 所属者（Wn账号）
th_ow_nm  : “xxx"   // 「冗余」所属者显示名
th_name   : "xxx"   // 东西的名称
th_breif  : "xxx"   // 东西的简单文本介绍，用作详细列表显示
//...........................................
th_c_cmt   : 45      // 评论数
th_c_view  : 1980    // 浏览次数
th_c_agree : 86      // 赞同次数
//...........................................
tha_xxx : ??     // 其他以 tha_ 开头的属性是根据定义文件里面的字段来声明的
..
```

# ThingComments

```
nm    : ID       // 与自己的id字段相同，无意义，你可以随意修改，只不过 ls 的时候好看
tp    : "md"     // 文件类型，表示这个评论文件的内容
mime  : "text/plain" // 文件的 MIME 类型
ct    : MS       // 创建时间
lm    : MS       // 最后修改时间
//...........................................
th_etp : "comment"   // 标识是评论
th_set : ID   // 对应 ThingSet 的 ID
th_id  : ID   // 对应的 ThingID
th_rep : ID   // 「选」表示本评论是针对某个评论的评论
//...........................................
th_ow_id  : "xxx"   // 所属者（Wn账号）
th_ow_nm  : “xxx"   // 「冗余」所属者显示名
th_name   : "xxx"   // 东西的名称
th_breif  : "xxx"   // 东西的简单文本介绍，用作详细列表显示
//...........................................
th_c_agree  : 86      // 赞同次数
```

* 评论发出，不能删除，只能修改

