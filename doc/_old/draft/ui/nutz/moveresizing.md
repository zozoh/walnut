---
title:$().moveresizing - 移动和修改尺寸
author:zozoh
---

# 设计意图

* 基于 `$().pmoving()` 
* 对于选区内指定对象进行移动和修改大小的操作

# 前提条件

我们假想你的 DOM 结构是

```
$viewport         <- 随便什么块元素都能做视口, position:relative
    ...           <- 嵌套多少层都无所谓
        $trigger  <- 这个是触发对象，会对其移动 position:absolute
                     同时为了能够标识它，控件会为其增加一个自定义属性
                     [mvrz-block="yes"]
            $wrapper   <- 这里面是个包裹，确保有如下 CSS
                          width:100%; height:100%; position:relative
                          你可以自行设置内边距等属性
                $ass   <- 辅助框,用来放置修改大小的手柄，里面的 HTML 会被控件修改
                $con   <- 这里放你随便什么内容，自行 CSS 撑满 .wrapper
                ...    <- 后面你还要放什么我不管
```

其中，`$ass` 的 HTML 为

```
<div class="mvrz-ass">
    <div class="mvrza-grp" md="L">
        <div class="mvrza-hdl" hd="NW"></div>
        <div class="mvrza-hdl" hd="W"></div>
        <div class="mvrza-hdl" hd="SW"></div>
    </div>
    <div class="mvrza-grp" md="C">
        <div class="mvrza-hdl" hd="N"></div>
        <div class="mvrza-hdl" hd="S"></div>
    </div>
    <div class="mvrza-grp" md="R">
        <div class="mvrza-hdl" hd="NE"></div>
        <div class="mvrza-hdl" hd="E"></div>
        <div class="mvrza-hdl" hd="SE"></div>
    </div>
</div>
```

控件会提供一个 CSS 文件， 定义这些手柄的位置，你可以用 CSS 开关某些手柄的显示和样式

```
.mvrz-ass {
   position:absolute; top:0; left: 0; right: 0; bottom: 0;
   @flex(); flex-flow:row nowrap; justify-content:space-between;
   .mvrza-grp {
       flex:0 0 8px;  
       @flex(); flex-flow:column nowrap; justify-content:space-between;
       .mvrza-hdl {
            flex:0 0 8px; 
            border:1px solid #CCC; background: rgba(255,255,255,0.6);
        }
       .mvrza-hdl[hd="NW"], .mvrza-hdl[hd="SE"]{cursor: nwse-resize;}
       .mvrza-hdl[hd="NE"], .mvrza-hdl[hd="SW"]{cursor: nesw-resize;}
       .mvrza-hdl[hd="N"],  .mvrza-hdl[hd="S"] {cursor: ns-resize;}
       .mvrza-hdl[hd="W"],  .mvrza-hdl[hd="E"] {cursor: ew-resize;}
   }
}
```    

# 如何创建

```
$(viewport).moveresizing({
    // 在 viewport 之内，的选择器
    trigger : "selector",
        
    // 支持 pmoving 除了 
    //  - findTriggerElement
    //  - autoUpdateTriggerBy
    //  - helperPosition
    //  - on_ing
    // 之外全部选项
    
    // 采用哪个顶点作为锚点，修改 CSS
    // "top,left"      - 左上顶点 「默认」
    // "top,right"     - 右上顶点
    // "bottom,left"   - 左下顶点
    // "bottom,right"  - 右下顶点
    // 否则表示不自动更新
    anchorVertex : "top,left"
    
    // 如何修改目标块的位置，默认为 "top,left,width,height"
    // null 表示不自动修改
    // 给定函数则表示你自行修改
    updateBlockBy : "top,left,width,height" | {context}F(rect)
    
    // 为了保证修改大小的手柄可以显示，需要调整其 zIndex
    // 默认为0，表示采用 css 来设定
    hdlZIndex : 0
        
    // 回调函数，参数是 trigger 的相对于 viewport 的矩形
    // 你也可以用 this.rect.blockInView 来获取
    on_begin  : {context}F()       // 开始时
    on_end    : {context}F()       // 结束时
    on_move   : {context}F(rect)   // 被移动时触发
    on_resize : {context}F(rect)   // 修改大小时触发
    on_change : {context}F(rect)   // 移动或修改大小均触发
});
``` 

* `pmoving.helperPosition` 这里一定是 *hover*
* `pmoving.autoUpdateTriggerBy` 这里一定是 *null*


控件会维护一个上下文，实际上就是 [pmoving 的运行时上下文](pmoving.html#运行时上下文)
同时，它会增加几个项目

```
extend pmvoing.context {
    // 如果移动的是尺寸手柄，那么会在这里标识是哪个手柄
    // 否则，则表示，移动整个 trigger
    hdlMode : "NW|W|SW|N|S|NE|E|SE",
    
    // 由于 trigger 可能是手柄，这里提供一个新的矩形对象，表示当前块
    $block : jQuery    // 指向当前块对象
    $wrapper : jQuery  // 块内的包裹元素
    $ass   : jQuery    // 辅助控制柄块
    $con   : jQuery    // 块的内容区域
    rect   : {
        ...
        block          // 块对象的尺寸
        blockInView    // 块对象相对于 viewport 的尺寸(CSS描述)
    }
}
```

# 如何销毁

```
$(context).moveresizing("destroy");
```



