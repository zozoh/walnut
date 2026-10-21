---
title: 表单控件
author:zozoh
---

----------------------------------
# 控件概述

表单控件将根据配置信息，创建一个对象表单。以及维护这个表单的保存行为

----------------------------------
# 如何创建实例

```js
new FormUI({
    // 默认的，控件将调用者通过 setData(o) 传入的对象 o 存储，当 getData 的时候
    // 控件将 o 与最新的编辑信息合并，返回给调用者
    // 如果希望 getData 返回一个新对象，那么将这个属性设置为 false
    // !!! 这里需要注意的是: 如果控件返回的字段值为 undefined，合并的时候会被无视
    // 字段属性 nullAsUndefined :true 则标识了该字段，如果为 null 则当做 undefined 处理
    // 字段属性 emptyAsNull : true 标识了该字段（仅对 type=string有效) 如果为空串，当做null
    // 来出来，因此对于一个 type=string 的字段，如果标识了 nullAsUndefined 则，空串会被无视
    // （因为，emptyAsNull 默认为 true）
    mergeData  : true
    
    // setData 前的预处理
    parseData  : {c}F(o):obj
    
    // getData 后的后续处理
    formatData : {c}F(o):obj
    
    // 指明对象哪个字段是 ID，默认为 "id"
    //  - ID 字段如果在 fields 里，则它不可编辑
    //  - 如果 mergeData==true，那么至少会保留源对象的 ID 字段，如果它有的话
    idKey : "id"

    // 表单可以指定一个标题，你可以随意写一段 HTML
    // 如果未定义，标题将不显示
    title : HTML,

    // 可以设置显示模式，不写是默认，现在只支持一个紧凑模式 : compact
    displayMode : "compact"
    
    // 表单的字段列表
    fields : [..]
    
    // 内置的提示
    // 默认内置下面两个
    prompts : {
        spinning : '<i class="fa fa-spinner fa-spin"></i>',
        warning  : '<i class="fa fa-warning"></i>'
    }

    // 默认一行可以容纳多少组
    cols : 1
    
    // 默认的，每个组编辑区字段应该占有的宽度
    // 如果是个浮点数，则表示百分比（相对 .ff-val)
    // 如果是整数，则表示一个绝对的像素
    // 如果值为 "auto" 则表示自动适合控件的最小宽度
    // 当然，前提是控件有最小宽度
    // 如果为 "all" 则表示撑满行
    // 默认为 "auto"
    uiWidth : 0.3
    
    //............................................................
    // 事件
    // "form:change" 字段有修改，不包括 virtual 字段
    on_change  : {c}F(key, val, fld)
    
    // "form:update" 对象改动，包括 virtual 字段
    // 参数是对象改动的部分的对象
    on_update  : {c}F(obj, fld}

}).render();
```


----------------------------------
# 表单的字段

## 普通字段

```js
{
   // 有 key 就是普通字段
   key       : "nm"          // 字段键名
   icon      : HTML          // 字段的图标
   title     : "i18n:xxx"    // 字段的标题
   tip       : "i18n:xxx"    // 字段的提示说明
   required  : Boolean       // 字段是否必须
   className : "XXXX"        // 指定特殊的类选择器
   
   // 字段键名的提示信息
   key_tip     : "i18n:xxx"
   
   // 提示信息的方向，默认是 right， 可选值是 left|right|up|down
   key_tip_pos : "right"

   // 字段是否为无效，disableField/enableField 方法可以动态控制
   disabled  : Boolean
   
   // 如果为 true 则会自动将文字区设置行高，并将 padding-top/bottom 归零
   autoLineHeight : false
   
   // 控件跨越的编辑区，如果一行只有1列，那么写多大都相当于1
   span     : 1
   
   // 字段编辑区宽度，与全局意义相当
   uiWidth  : 0.3
   
   // 字段是一个虚拟字段，它实际需要合并编辑多个字段
   // 输出的对象也是多个字段，需要合并到表单最后输出的对象里
   // 默认 false
   // 一旦指定这个字段，后面的 type 均被作为 object 看待
   // 同时 uiConf 里的 formatData 和 parseData 将收到的是
   // 完整的 form 对象的引用，而不是某个键的值
   // 注意，因为 form 的 on_change 的参数形式， virtual 类型
   // 的键将不会被通知，请用 on_update 来获取
   virtual : false,

   // 字段检查
   validate ： {
       // 针对字符串型的值，检查前是否要预先去掉左右空白
       trim : true,
       // 数字区间
       intRange : "(10,20]",
       // 日期范围的区间
       dateRange : "(2018-12-02,2018-12-31]",
       // 验证值的字符串形式，支持 "!" 开头
       regex : "^...$",
       // 确保值非 null
       notNull : true,
       // 针对字符串的值，最大长度不超过多少
       maxLength : 23,
       // 针对字符串的值，最小长度不能低于多少
       minLength : 5,
   }
   
   // 当字段对应控件被 setData 时的回调
   beforeSetData : {fld}F(o),
   afterSetData  : {fld}F(o),
   
   //.................................................
   // 字段类型: 
   //  - string : 字符串
   //  - object : JS对象，可以是空白对象或者数组
   //  - daterange : 日期范围，一个数组 [Date, Date]
   //  - datetime  : 日期时间, Date()
   //  - time      : 时间, $z.parseTime() 输出的格式
   //  - int       : 整数
   //  - float     : 浮点
   //  - boolean   : 布尔 
   type     : "string",
   
   // 如果控件的值为 null，是否当做 undefined 来处理
   // 默认为 false
   nullAsUndefined : false,
   
   // 当 type==string 时，支持属性。即如果是空字符串，会被当做 null
   emptyAsNull : true,
   
   //.................................................
   editAs   : "input"       // 快捷的编辑控件类型
   uiType   : "xxxx"        // 编辑控件类型，比 editAs 优先
   uiConf   : {..}          // 编辑控件的配置信息
}
```

## 字段分组

```js
{
   icon     : HTML          // 字段的图标
   title    : "i18n:xxx"    // 字段的标题
   tip      : "i18n:xxx"    // 字段的提示说明
   className : "XXXX"       // 指定特殊的类选择器
   
   // 对象的 ID 字段 key，默认为 "id"
   idKey : "id",
   
   // 如果为 true 则会自动将文字区设置行高，并将 padding-top/bottom 归零
   // 组内字段默认采用这个设置
   autoLineHeight : false
   
   // 本组内，一行有多少列字段，默认1
   cols     : 1
   
   // 一个数组表示每列字段的宽度，
   //  - 没有的项目当做 0
   //  - 0 表示平均分配
   //  - 0.5 小于1的浮点表示比例
   //  - 322 大于 1 的数表示绝对大小
   colSizeHint : [0]
   
   // 字段编辑区宽度，与全局意义相当
   uiWidth  : 0.3
   
   // 如果为 true，那么每个字段都有一个开关，显示表示自己在 getData 的时候是否要被忽略
   asTemplate : false,
   
   // 被标注 disabled 字段，将不是灰掉，而是隐藏
   // 默认 false
   hideDisabled : false,

   // 本组内的字段 
   fields   : [..]
}
```

* 没有 *key* 就是字段组
* 每当遇到一个字段组，就重新开始一组控件
* 字段组不能被嵌套

----------------------------------
# 控件方法

## getData

```js
// 返回控件正在编辑的表单数据
var o = uiForm.getData();

// 返回某个指定键的数据
var v = uiForm.getData("someKey");
```

form 控件获取数据的行为被下面几个配置项影响:

- form 配置项 `mergeData`，默认 true
- 字段配置项 `nullAsUndefined`，默认 false
- 字段配置项 `emptyAsNull`，默认 true

通过这几个配置项的组合，你基本让你的表单符合绝大多数使用场景。关于这几个选项的描述，请详细参见字段选项描述。这里有个例子:

```js
var obj = {txt: "Hello"};

// 对于 form, setData 后
{
     mergeData : true,
     fields : [{
         key : "txt",
         type : "string",
         emptyAsNull : true,
         nullAsUndefined : true
     }]
}

// 如果用户删掉了 input 框内容，那么 form.getData() 将返回:
{txt: "Hello"}
```


## setData

```js
// 为控件设置数据以便展示编辑界面
uiForm.setData(o);
```

## getFormDataObj

```js
var o = uiForm.getFormDataObj();
```

* 得到 form 的 merge 之前的对象数据

## getFormCtrl

```js
var ui = uiForm.getFormCtrl("name");
```

* 得到某个字段的 UI 对象

## showPrompt

在某个 Fields 上显示一个提示信息，可以是错误项，可以是 loading 等。

```js
// 显示某个内置的 prompt （参见控件定义的 prompts 段)
// 如果给定的 prompt 没有值，那么将作为一段 HTML 代码来显示
uiForm.showPrompt("name", "loading")

// 显示某个自定义的 prompt
uiForm.showPrompt("name", "<b>haha</b>");
```

* 第一个参数是字段的 key
* 函数会为字段的 DOM 添加 `form-prompt="loading"` 的属性
    * 如果是自定义的，那么属性值会是 "_customized"
* 同时会修改 `.ff-prompt` 节点的 HTML

## hidePrompt

```js
uiForm.hidePrompt("name");
```

* 第一个参数是字段的 key
* 函数会删除字段的 DOM 的 `form-prompt` 属性









