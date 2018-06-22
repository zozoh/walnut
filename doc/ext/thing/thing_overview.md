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
# 带有 thing_set 类型的目录被认为是一个 ThingSet
@DIR {tp:"thing_set"} # 特殊类型目录
    thing.js          # 每个 Thing 的定义
    tmp/              # 临时文件夹，存放所有上传下载的临时文件
    index/            # 索引表，存放 thing 的所有元数据
        $ThingID      # 每个都是文件，nm==id
                      # tp 为 'th_index'
    comment/          # 存放所有的评论信息
        $CommentID    # 每个都是一个文件
                      # tp 为 'th_cmt'
    data/             # 存放每个 thing 的负数数据 
        $ThingID/          # 每个子目录对应一个 thing, nm与索引对象同名
                           # tp 为 'th_data'
            thumb.jpg      # 对象的缩略图
            media          # 目录，存放相关的一组媒体
            attachment     # 目录，存放相关的一组附件
            resource       # 目录，存放评论信息相关的附件
``` 

我们认为:

- Thing 没有子
- 基于约定，通过文件和目录，可以描述世间万物

# ThingSet

```
tp : "thing_set" // 目录类型是 thing_set
//...........................................
icon  : HTML     // 小图标
thumb : ID       // 缩略图ID  @see 缩略图机制
//...........................................
th_icon  : HTML  // Thing 的默认图标
th_thumb : ID    // Thing 的默认缩略图
//...........................................
```

* 标识了元数据 `thing` 的目录就是 *ThingSet*

# thing.js

```
{
  // 布局显示
  searchMenuFltWidthHint : "50%", // 搜素面板菜单全部缩起的宽度阀值
  meta: true,          // 是否显示元数据
  detail: false,       // 是否显示详情编辑
  media: false,        // 是否显示媒体面板
  attachment: false,   // 是否显示附件面板
  //......................................
  /* 导入导出设定
  导入导出命令模板接受下面的上下文
  {
    f : {..}        // 临时文件对象，一个 WnObj
    tsId  : ID      // 数据集的 ID
    query : ".."    // 导出时给定的过滤参数，要拼在 thing query 后面
  }
  命令模板的占位符，{{xxx}} 表示替换，但是会逃逸 HTML 字符
  如果不逃逸，一般用 <%=xxx%>
  */ 
  cmd_import : "xxx",     // 导入命令模板
  cmd_export : "xxx",     // 导出命令模板
  //......................................
  // 过滤器设定 @see ui/search/search_filter.md
  searchFilter : {}
  //......................................
  // 列表批量对象操作菜单
  searchMenu : [..]
  //......................................
  // 列表设定 @see ui/search/search_list.md
  searchList : {}
  //......................................
  // 排序器设定 @see ui/search/search_sorter.md
  searchSorter : {}
  //......................................
  // 翻页条设定 @see ui/search/search_pager.md
  searchPager : {}
  //......................................
  // 单个对象操作菜单
  objMenu : [..]
  //......................................
  // 物品的字段，这个遵守 ui/form/form 控件的 field 字段定义规范
  fields : [..]
}
```

# Thing索引

```
nm    : ID       // 唯一标识
lbls  : ["xx"]   // 标签
ct    : MS       // 创建时间
lm    : MS       // 最后修改时间
//...........................................
tp    : 'html'       // 类型
mime  : 'text/html'  // 内容类型 text|html|markdown 等
//...........................................
len     : 0            // 内容长度 
brief   : "xxx"        // 摘要自动生成的话，截取内容前面 256 个字符
//...........................................
icon  : HTML     // 小图标，如果没有，默认用 ThingSet.th_icon
thumb : ID       // 缩略图ID  @see 缩略图机制，如果没有，默认用 ThingSet.th_thumb
//...........................................
th_ow     : "xxx"   // 所属者，通常表示 dusr 指定的账号系统，默认null
th_nm     : "xxx"   // 东西的名称，如果是文章，那么就是标题
th_live   : 1       // 1 表示有效， -1 表示删除了
//...........................................
th_set    : "xxx"   // ThingSet 的 ID
th_cate   : "xxx"   // 分类ID（如果有分类）
//...........................................
th_c_cmt    : 45      // 评论数
th_c_view   : 1980    // 浏览次数
th_c_agree  : 86      // 赞同次数
th_media_nb : 0       // 具有的媒体计数
th_att_nb   : 0       // 具有的附件计数
//...........................................
xxx : ??            // 其他属性是根据定义文件里面的字段来声明的
..
```

- 详情信息就是文件的内容

# ThingComments

```
nm    : TS       // 创建时时间戳，格式类似 20150721132134321
ct    : MS       // 创建时间
lm    : MS       // 最后修改时间
//...........................................
tp    : 'th_cmt'     // 类型
mime  : 'text/html'  // 内容类型 text|html|markdown 等
//...........................................
// 如果内容不足 256 个字符，直接存入 content，len为 0
// 如果内容超过 256 个字符，则content为null，细节存入内容，并计入 len
len     : 0            // 内容长度 
content : "xxx"        // 短内容
brief   : "xxx"        // 摘要
//...........................................
th_id  : ID          // 对应的 ThingID
th_set : "xxx"       // ThingSet 的 ID
th_rep : ID          // 「选」表示本评论是针对某个评论的评论
//...........................................
th_c_agree  : 86      // 赞同次数
//...........................................
th_ow     : "xxx"     // 所属者，通常表示 dusr 指定的账号系统，默认null
``

* 评论发出，不能删除，只能修改?

# 对于 thing 的操作

- `man thing` 是对于数据操作全部的命令
- `org.nutz.walnut.ext.thing.WnThingService` 提供了操作类




