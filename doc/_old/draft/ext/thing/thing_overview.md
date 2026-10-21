---
title:任何东西都是 Thing
author:zozoh
tags:
- 系统
- 扩展
- thing
---

----------------------------------
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

```bash
# 带有 thing_set 类型的目录被认为是一个 ThingSet
@DIR {tp:"thing_set"} # 特殊类型目录
    thing.js          # Thing 字段的特殊定义，主要描述了 thing 的行为
    tmp/              # 临时文件夹，存放所有上传下载的临时文件
    index/            # 索引表，存放 thing 的所有元数据
        $ThingID      # 每个都是文件，nm==id
    data/             # 存放每个 thing 的负数数据 
        $ThingID/          # 每个子目录对应一个 thing, nm与索引对象同名
            thumb.jpg      # 对象的缩略图
            media/         # 目录，存放相关的一组媒体
            attachment/    # 目录，存放相关的一组附件
            resource/      # 目录，存放评论信息相关的附件
            comment/       # 放置 thing  所有的评论
              $CommentID   # 每个都是一个文件，元数据会关联 resource 里面的媒体
                           # 以便删除的使用
``` 

我们认为:

- Thing 没有子
- 基于约定，通过文件和目录，可以描述世间万物

----------------------------------
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

----------------------------------
# thing.js

```js
{
  // 布局显示
  searchMenuFltWidthHint : "50%", // 搜素面板菜单全部缩起的宽度阀值

  // 表示四个部分是否显示，如果都不显示，那么就会只显示一个列表
  meta: true,          // 是否显示元数据
  detail: false,       // 是否显示详情编辑
  media: false,        // 是否显示媒体面板
  attachment: false,   // 是否显示附件面板
  //......................................
  // 唯一键的约束
  // 定义了一个数组，数组中的每个元素都是一组唯一的键
  // 当 thing create/update 的时候，均不能导致这些键重复
  uniqueKeys : [{
      name : ["key1"],     // 一个元素的数组表示单个键
      required : true      // 数据不能为空，默认false,只有提交数据有值才检查
    }, {
      name : ["key1", "key2"], // 数组表示符合键
      required : false     // 如果 true，表示所有的键都不能为空
                           // false 则表示只有所有键都为空才能通过检查
                           // 如果一个键为空一个不为空，直接不检查就抛错    
    }
  ],
  //......................................
  // 导入段的设置
  dataImport : {
    enabled    : true,      // 如果此项为 true 则开启导入
    uniqueKey : "phone",    // 唯一数据键
    mapping   : "~/.sheet/测试数据_import"  // 映射数据
    accept    : ".csv, .xls"  // 可以接受的导入文件格式
    // 执行导入命令时每条记录的输出模板
    // 其中特殊占位符 `${P}` 表示进度，其他的占位符为数据字段键名
    processTmpl : "${P} ${th_nm?-未知-} : ${phone?-未设定-}",
    // 上传前设置所有数据的固定字段
    fixedForm : "~/.sheet/xxx.js",  // 一个表单form的 config JS
    // 导入一条数据后的后续处理
    // 每当创建一个数据，数据会变成 JSON 经过管道，传递给
    // 这个命令，当这个命令执行出错，后续执行将被阻断
    afterCommand : "jsc /jsbin/xxxx.js -vars"
  },
  //......................................
  // 导出段的设置
  dataExport : {
    enabled    : true,    // 如果此项为 true 则开启导出
    exportType : "xls",   // 默认导出类型。支持 csv|xls
    pageRange  : false,   // 导出页码，false 表示全部导出
    pageBegin  : 1,       // 默认起始导出页码
    pageEnd    : 10,      // 默认结束页码
    audoDownload : true,  // 导出完毕后自动下载
    // 执行导出命令时每条记录的输出模板
    // 其中特殊占位符 `${P}` 表示进度，其他的占位符为数据字段键名
    processTmpl : "${P} ${th_nm?-未知-} : ${phone?-未设定-}",
  },
  //......................................
  // 扩展的命令菜单
  extendCommand : {
    // 扩展命令的 JS 文件，是一个列表
    actions : [
      "~/xxx/action1.js",
      "~/xxx/action2.js",
    ],
    // 搜索列表的扩展命令菜单
    search : [{
      text : "按钮名称",
      icon : '<i ...>',
      cmdText : '命令模板',      // 指定要执行的命令
      handlerName : "doSomething"   // 调用的函数
    }, {
      type : "seperator"   // 表示分隔符
    }],
    // 搜索列表的扩展命令菜单
    obj : [..]
  }
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

----------------------------------
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

----------------------------------
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

----------------------------------
# 对于 thing 的操作

- `man thing` 是对于数据操作全部的命令
- `org.nutz.walnut.ext.thing.WnThingService` 提供了操作类




