---
title:hMaker Markdown
author:zozoh
tags:
- 扩展
- hmaker
---

# hMaker 支持的 Markdown 格式

 - 兼容 `GFM`

# 链接打开新窗口

```bash
# 利用 + 表示新开一个窗口
[+官网](http://nutzam.com)  

#如果就是想加一个 + 前面加个空格
[ +官网](http://nutzam.com)  
```

# 指定图片/视频宽高

语法 `宽[/|]高`  分隔线，可以是 `|` 也可以是 `/` 都一样

```bash
# 指定宽高
![800/600](logo.png)    # 相当于 800px/600px
![8rem/6rem](logo.png)
![50%/300px](logo.png)

# 仅仅指定宽度
![8rem](logo.png)
![60%](logo.png)

# 仅仅指定高度
![/6rem](logo.png)
![|30%](logo.png)
```

# 对于 code 的定制

## poster 语法

对于 <code>```poster</code> 声明的代码块，将会自动进行解析

```bash
# hMaker poster 语法
# 注释行用 # 开头
#----------------------------------------
# 全局设定
@layout:t1               # 布局名称，默认没有布局
@bg:media/abc.jpg        # 表示整个海报块的背景图，比css里面的优先
@bgcolor:#FFF            # 背景颜色，比 css 里面的优先
@color:#000              # 前景颜色，比 css 里面的优先
@height:1rem/?           # 海报块的高度: PC/手机
#----------------------------------------
# 文字
+text:广告文字             # 表示增加一个文字对象
支持换行                   # 换行就会增加一个 <br>
#----------------------------------------
# 图片
+picture:media/abc.png   # 表示增加一个图片对象
#----------------------------------------
# 视频
+video{controls:true}:media/xyz.mp4   # 表示增加一个视频对象
#----------------------------------------
# 产品说明表格
# 每行由 | 分作两列，没有 | 的列，会自动加入前行
+spec
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
#----------------------------------------
# 组
-group
  +text:xxxx
  +picture:xxxx
  +list
    - attachment/xxx
    - attachment/xxx
  -group
#----------------------------------------
# 自定义属性
# 任何元素都支持自定义属性，写法是在类型后面来个大括弧
# 表示一个属性集的 JSON 对象（必须在同一行）
+text{align:"right"}
+video{controls:true}
#----------------------------------------
# 自定义CSS
# 任何元素都支持自定义CSS属性，写法是在类型后面来个中括号
# 如果与自定义属性联用，必须放在自定义属性后面
+text[color:red;font-size:12px;]
+video{controls:true}[width:100%;]
```

总之 poster 的元素声明格式为

```
[+-]元素类型.selector{Attributes}[CSS Style]
```

## poster 解析结果

```
{
    bg : "media/abc.jpg",        //「选」背景图片
    bgcolor : "#FFF",            //「选」背景颜色
    color   : "#FFF",            //「选」前景颜色
    height  : "2rem/?",          //「选」海报块的高度: PC/手机
    layout  : "t1",              //「选」布局名称
    attr    : {..}               //「选」属性
    cssText : "background-color:red;padding:.1rem;",
    items : [{
        type     : "text",            //「通用」类型为 text
        depth    : 0,                 //「通用」 缩进级别，每两个空格算一个缩进
        selector : "xxx",             //「通用」表示一个特殊的类选择器
        cssText : "color:#FFF;",      //「通用」元素的 style
        text : ["广告文字", "一行一个"]  // 文字内容
    }, {
        type    : "picture",
        cssText : null,
        picture : "media/abc.png",
    }, {
        type  : "video",
        attr  : {...},
        cssText : "border:1px solid #FFF;"
        video : "media/xyz.mp4",
    }, {
        type  : "spec",
        attr  : {...},
        cssText : "border:1px solid #FFF;"
        spec : {
                rows : [["前体类型", "α(阿尔法)前体，调味阀，果汁盖"],
                        ["速度"],["螺旋推进器:43转 / 旋转刷:17转"]],
            },
    },{
        type    : "list",
        attr    : {...},
        cssText : "border:1px solid #FFF;"
        list : [{
                src: "attachment/前体.png", text:"前体组件"
            },{
                src: "attachment/果汁杯+接渣杯.png", text:null
            }],
    }, {
        type  : "group",
        depth : 0,
        selector : "xxx",
        attr : {...},
        items : [
            // 这里是对象，可以再嵌套组
        ]
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














