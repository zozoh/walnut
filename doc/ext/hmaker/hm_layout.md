---
title:hMaker 字段布局语法
author:zozoh
tags:
- 扩展
- hmaker
---

# 什么是字段布局

在动态显示控件，如果需要显示一个对象的字段，总需要按顺序将这些字段渲染成 DOM，并指定样式。
字段布局就是一个这样的文本描述。

默认的（实际上我写这篇文档的时候，只有默认的），字段布局是希望对象显示遵循下面的规则：

1. 每个字段指定宽度，高度自动向下延展
2. 字段可以分组，组也可以指定宽度
3. 所有的字段组，都是 `float:left`
4. 字段组不能嵌套，但是字段可以不在组里
5. 不管是字段组还是字段，都是 `inline-block` 的
6. 我并不假设你会将字段叠在另外的字段之上

# 字段布局语法

```bash
# .开头表示某个字段名，:100 表示宽度 100% 这个会设在 css 里
# 字段名后面的 [-] 表示，控件如果发现用户设置了链接，就插入到这里
# 也可以用 [+] 同样表示插入链接，但是是新窗口打开
.th_nm[-]:100

# @表示开始一组，直到新组，所有的字段都会归纳在组内
# @50/100 表示 PC 的时候，组宽为 50，移动设备组宽 100
# 对应尺寸，我还支持下面的写法
#  @50/-   PC时 50%，手机时隐藏
#  @50/?   PC时 50%，手机时不设宽度
#  @-/100  PC时隐藏，手机时宽度 100%
#  @-/-    PC/手机都隐藏，不过这样有什么意义我还没看出来，反正我是支持的
#  @?      PC/手机都不设置宽度
#  @       相当于 @?
# 注意，在字段，也支持同样的语法，譬如 .th_nm:50/?
@50/100
# 字段语法 ={..} 表示对字段值的映射，即如果是"A"就显示猫，支持HTML
# 这里没有指定宽度，因此表示显示多宽就多宽
.th_cate={A:"猫",B:"狗"}
# 字段语法 =date("xxx") 将用来格式化日期或者毫秒数为一个具体的时间
.th_birthday=date(yyyy-mm-dd)
# 字段语法 =size(2) 则会显示友好的大小，譬如 1.5G, 37.2M 等，
# `2` 表示小数点后面几位
.len:100/?=size(2)
# 字段语法 =ul 会将字段值强制转换为数组
# 每个数组元素会依次生成 <li>
.lbls:100=ul
# 字段语法 =markdown 将会将字段内容转换为markdown显示
.content=markdown
# 字段语法 =thumb 字段内容应该为 /api/thumb 可接受的 QueryString
.thumb=thumbnail
# 如果一定要结束一个组，后面的字段则不会加入任何组
# 可以用三个波浪线强制表示组的结束
~~~
```

下面我给一个布局语法的例子:

```
.th_nm:100

@50/100
 .th_cate={A:"猫",B:"狗"}
 .th_birthday=date(yyyy-mm-dd)
 .len:100/?=size(2)
 .lbls:100=ul
```


# 字段布局解析结果

```json
{
    "data" : [{
            "type" : "group",        // 表示字段组
            "w_desktop" : 50,        // 表示 50% 宽度
            "w_mobile"  : undefined, // undefined 表示未设置宽度
            "items" : [{           // 下面是字段组包含的内容
                    "type"    : "field",    // 表示是一个字段，不写也成
                    "key"     : "th_nm",    // 字段键值是什么
                    "display" : "string",   // 字段值如何显示，默认 string
                    "config"  : undefined,  // 显示配置参数
                    "link"    : "_self",    // 表示可以插入链接，_blank 表示新窗口
                    "w_desktop" : 100,      // 不解释
                    "w_mobile"  : "hidden", // hidden 表示隐藏
                }, {
                    "key"     : "th_cate",
                    "display" : "mapping",
                    "config"  : {A:"猫",B:"狗"},
                    ...  // 我就懒得写 w_desktop | w_mobile 了
                }, {
                    "key"     : "th_birthday",
                    "display" : "date",
                    "config"  : "yyyy-MM-dd",
                    ...  // 我就懒得写 w_desktop | w_mobile 了
                }, {
                    "key"     : "len",
                    "display" : "size",
                    "config"  : 2,
                    ...  // 我就懒得写 w_desktop | w_mobile 了
                }, {
                    "key"     : "lbls",
                    "display" : "ul",
                    ...  // 我就懒得写 w_desktop | w_mobile 了
                }]
        }]
}
```

# 字段布局的渲染

- 字段组会生成 `<div>`
- 字段会生成 `<span>`
- 有链接的字段，会生成 `<a>`

