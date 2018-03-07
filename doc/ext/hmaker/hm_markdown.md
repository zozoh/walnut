---
title:hMaker Markdown
author:zozoh
tags:
- 扩展
- hmaker
---

# hMaker 支持的 Markdown 格式

 - 兼容 `GFM`

# 对于 code 的定制

## poster 语法

对于 <code>```poster</code> 声明的代码块，将会自动进行解析

```
# hMaker poster 语法
# 注释行用 # 开头
#----------------------------------------
# 背景
+bg:media/abc.jpg        # 表示增加一个背景图
[css]                    # 这个会描述整个海报的 css
background-color:red;    # 默认的海报的背景是 cover 且居中不重复的
[/css]                   # 结束样式段落
#----------------------------------------
# 文字
# 支持 [css] 和 [attr]
+text:广告文字             # 表示增加一个文字对象
支持换行                   # 换行就会增加一个 <br>
[css]
color:#FFF;
[/css]
#----------------------------------------
# 图片
# 支持 [css] 和 [attr]
+picture:media/abc.png   # 表示增加一个图片对象
[css]
border:1px solid #FFF;
[/css]
#----------------------------------------
# 视频
# 支持 [css] 和 [attr]
+video:media/xyz.mp4   # 表示增加一个视频对象
[attr]                 # 表示视频对象的熟悉
{controls : true}      # 内容是一个 JSON 与 <video> 标签的意义相当
[/attr]
[css]
border:1px solid #FFF;
[/css]
#----------------------------------------
# 产品说明表格
# 支持 [css] 和 [attr]
# 每行由 | 分作两列，没有 | 的列，会自动加入前行
+spec
@ 大于号开头的行，表示表头
前体类型 | α(阿尔法)前体，调味阀，果汁盖
速度    | 螺旋推进器:43转 / 旋转刷:17转
前体容量 | 500毫升
素材    | 主体:ABS
         前体:Tritan(共聚脂)
         螺旋推进器:PEI(聚醚酰亚胺)
         果汁网:聚醚酰亚胺、不锈钢
         冰淇淋网:BISEN
         安全前体盖:ABS
大小 | 179.7(W) X 223(D) X 407.4(H) mm 重量 4.8kg
电机 | A/C
电压、频率、功率 | 220V, 50Hz, 150W
电源长度 | 1.4 M
额定使用时间 | (连续使用)30分钟以下
#----------------------------------------
# 图文列表
# 支持 [css] 和 [attr]
# 如果不指明 `: xxx` 则用图片文件名作为文字
# 除非属性里，明确指定了 hideText:true 
# [itemCss] 表示每个项目（<li>）的 css
+list
- attachment/前体.png : 前体组件
- attachment/果汁杯+接渣杯.png
- attachment/推压棒.png
- attachment/旋转刷.png
- attachment/清洗刷.png
- attachment/粗孔网.png
- attachment/细孔网.png
- attachment/豆腐盒.png
- attachment/清洗刷2.png
- attachment/冰淇淋网.png
- attachment/前体置物架.png
- attachment/螺旋推进器.png
[attr]
{hideText:true}
[/attr]
[itemCss]
width:20%;
[/itemCss]
```

## poster 解析结果

```
{
    bg : "media/abc.jpg",
    cssText : "background-color:red;padding:.1rem;",
    items : [{
        type : "text",
        text : ["广告文字","一行一个"],
        cssText : "color:#FFF;"
    },{
        type    : "picture",
        picture : "media/abc.png",
        cssText : null,
    }, {
        type  : "video",
        video : "media/xyz.mp4",
        attr  : {...},
        cssText : "border:1px solid #FFF;"
    }, {
        type  : "spec",
        spec : {
                caption : "XXX",
                rows : [["前体类型", "α(阿尔法)前体，调味阀，果汁盖"],
                        ["速度"],["螺旋推进器:43转 / 旋转刷:17转"]],
            },
        attr  : {...},
        cssText : "border:1px solid #FFF;"
    },{
        type : "list",
        list : [{
                src: "attachment/前体.png", text:"前体组件"
            },{
                src: "attachment/果汁杯+接渣杯.png", text:null
            }],
        attr  : {...},
        cssText : "border:1px solid #FFF;"
        itemCss : "width:25%;"
    }]
}
```

## poster 渲染结果

```
<div class="md-code-poster">        <-- 这里应用上 +bg.cssText
    <span>文字文字文字</span>         <-- 这里应对一个 +text
    <img src="xxxx">                <-- 这里应对一个 +picture
    <video>                         <-- 这里应对一个 +video
    <section it="spec">    <-- 这里应对一个 +spec
        <table>..</table>
    <section it="list">             <-- 这里应对一个 +list
        <ul>..</ul>

</div>
```

## poster 自身的属性

```
# 这个属性仅仅作用在 +bg 项目上，是设置给整个 poster 的属性
# 指当前对象适用的背景
# 默认为适用于深色背景图，即前景为浅色
bgtype : "light"     # light 表示适用于浅色背景

# 指定了对象什么时候去掉高度的限制
hightfree : "mobile|desktop|both"
```

## poster 的项目属性

对应所有的项目（除了+bg），都可以通过 `[attr]` 设置下面的通用属性

```
# 指定了当前对象所在的区域
pos : "top|bottom|left|right|center|NW|NE|SW|SE|N|W|S|E"

# 指定了对象什么时候隐藏
ithide : "mobile|desktop|both"

# 指定了对象什么时候去掉绝对位置，采用自己的自然宽高
itfree : "mobile|desktop|both"

# 指定了对象什么时候去掉绝对位置，并用宽高撑满全部区域
itfull : "mobile|desktop|both"

# 指定对象文字的阴影
itshadow : "light|dark"
```














