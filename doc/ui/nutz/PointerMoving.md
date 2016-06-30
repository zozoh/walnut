---
title:PointerMoving - 指针移动上下文
author:zozoh
---

# 设计意图

* 希望触控设备和桌面鼠标设备采用同样的方法来处理下面这些交互行文
    - 移动
    - 拖拽
    - 改变大小
    - 其他收拾
* 本方法给出了一个底层实现的基础
* 本方法是一个 jQuery 插件

# 实现原理

![](js_PointerMoving.png)

* `trigger` : 触发移动的元素
* `viewport` : 移动的范围
    - jQuery | DOM 根据一个元素限定移动范围
    - Rect 指定一个绝对的移动范围
    - null 整个窗体为移动范围
* `FirePoint` : 触发点，记录相对于触发者的位置
* `helper` : 辅助框，用来让回调绘制附加一些移动时信息

# 触发逻辑

```
// 下面的逻辑，位置采用窗口坐标系，即 y 越大，越靠近底部
// opt 表示 PointerMoving 的配置信息
监控 mousedown (trigger){
    //.........................................
    创建上下文 pmvContext = {
        $trigger,
        $viewport,
        startInMs,
        atX,
        atY,
        beginX,
        beginY,
    }
    //.........................................
    设置一个全局遮罩层，监听 {
        mouseup(mask) {
            pmvContext += {
                endInMs,
                X,
                Y,
            }
            如果 trigger.pmv_mode_a 表示在激活模式 {
                利用 opt.position 修正 X 和 Y
                根据 opt.mode 限制 X 和 Y
                根据 opt.boundery 计算 trigger 的位置 {
                    这里要考虑 trigger 不能超过 viewport
                }
                根据 opt.autoUpdateTriggerBy 更新 trigger 位置
                opt.on_end(pvmContext);
                移除 trigger.pmv_mode_a 标识
                移除遮罩和辅助框
            }
            否则如果在 opt.clickRadius 内释放 {
                trigger.click();
            }
        }
        mousemove(mask) {
            pmvContext += {
                X,
                Y,
            }
            如果 trigger.pmv_mode_a 表示在激活模式 {
                利用 opt.position 修正 X 和 Y
                根据 opt.mode 限制 X 和 Y
                根据 opt.boundery 计算 trigger 的位置
                    这里要考虑 trigger 不能超过 viewport
                }
                根据 opt.autoUpdateTriggerBy 更新 trigger 位置
                opt.on_ing(pvmContext);
                修改辅助框位置，使其完全覆盖 trigger
                opt.on_update(pvmContext);
            }               
        }
    }
    //.........................................
    设置延迟函数(opt.delay) {
        如果没有 pvmContext.endInMs 表示要进入激活态 {
            标识 trigger.pmv_mode_a = "yes"
            创建 mask 层和辅助框
            pmvContext += {
                $mask,
                $helper,
            }
            修改辅助框位置，使其完全覆盖 trigger          
            opt.on_begin(pvmContext)
            opt.on_update(pvmContext);
        }
    }
}

```

# 回调上下文

```
{
    event     : Event,    // 事件对象
    $trigger  : jQuery,   // 触发者 DOM
    $viewport : jQuery,   // 视口 DOM
    $helper   : jQuery,   // 辅助块 DOM
    $mask     : jQuery,   // 遮罩层 DOM
    startInMs : MS,      // 开始时间
    endInMs   : MS,      // 结束时间
    atX    : Number,     // 初始点击相对于 viewport 左顶点的水平距离
    atY    : Number,     // 初始点击相对于 viewport 左顶点的垂直距离
    beginX : Number,     // 初始点击全局水平位置
    beginY : Number,     // 初始点击全局垂直位置
    X      : Number,     // 当前指针全局水平位置
    Y      : Number,     // 当前指针全局垂直位置
    // 下面是一组位置信息，控件自动实时计算，回调们就取个数就成了
    rect : {
        viewport : Rect,   // 视口
        trigger  : Rect,   // 触发者
        boundary : Rect,   // 触发者的边界
    }
}
```

# 矩形计算

zutil.js 提供下面的函数来辅助矩形计算

```
// 根据 top,left,width,height 计算剩下的信息
$z.rect_count_tlwh(rect);

// 根据 top,left,bottom,right 计算剩下的信息
$z.rect_count_tlbr(rect);

// 根据 bottom,right,width,height 计算剩下的信息
$z.rect_count_brwh(rect);

// 得到一个新 Rect 坐标系相对于 base
$z.rect_relative(rect, base);

// 相交
$z.rect_overlap(rectA, rectB);

// 相交面积
$z.rect_overlap_area(rectA, rectB);

// A 是否全部包含 B
$z.rect_contains(rectA, rectB)

// A 是否与 B 相交
$z.rect_is_overlap(rectA, rectB)

// 用 B 裁剪 A
$z.rect_cut(rectA, rectB);

// 用 B 限制 A，会保证 A 完全在 B 中，且距离原来的位置最近
$z.rect_boundary(rectA, rectB);

// 修改 A ，将其中点移动到某个位置
// 第二个参数对象只要有 x,y 就好了，因此也可以是另外一个 Rect
$z.rect_move_xy(rectA, {x,y});
```

# Rect 结构

```
// 在 zutil.js 的 rect 函数里定义了 Rect 对象
{
   width  : Number,
   height : Number,
   top    : Number,
   left   : Number,
   right  : Number,
   bottom : Number,
   x      : Number,    // 中点横坐标
   y      : Number,    // 中点纵坐标
}
```

# 遮罩层的结构

```
<div class="pmv-mask">
    <div class="pmv-helper"><!--// 来自用户的自定义--></div>
</div>
```

# 如何创建

```
$(context).PointerMoving({
    // 指明移动的视口，如果指针超过了视口，将不会再触发 on_ing
    viewport : DOM | Rect | null
    
    // 移动的方式，默认 both
    //   x : 只能横向移动
    //   y : 只能纵向移动
    //   both : 两个方向都能移动
    mode : "x|y|both"
    
    // 自动修改 trigger 的 位置属性，有效的值为
    // ["top","left"]      - 左上顶点 「默认」
    // ["top","right"]     - 右上顶点
    // ["bottom", "left"]  - 左下顶点
    // ["bottom", "right"] - 右下顶点
    // 否则表示不更新
    autoUpdateTriggerBy : ["top", "left"]
    
    // 如何判断 trigger 超出了 viewport
    // undefined : 表示不限制
    // 0 : 根据中心点
    // "100%" or Float : 为 trigger 尺寸（不包括外边距）的倍数
    // INT : 为中心点开始的一个绝对大小的正方形（半径）
    boundary : 0 | Float | undefined
    
    // 表示按住多久才表示进入激活模式（mode_a）
    // 默认 300ms
    delay : MS
    
    // 如果释放的时候，没有进入激活模式，则有可能是对 trigger 的一次点击
    // 那么在原始点半径多少以内释放表示点击呢（单位像素）
    // 默认 3px
    clickRedius : 3
    
    // 修正鼠标的指针，这里有两种方式，一种是假想视口布满了格子，移动要吸附上面
    // 第二种是一个高度定制的函数，用来直接修改鼠标指针的位置
    position : {C}F(pmvContext) | {
        gridX : 36 | .2 | "10%",       // 表示格子的宽度
        gridY : 36 | .2 | "10%",       // 表示格子的高度
        stickRadius : 36 | .2 | "10%", // 吸附半径
    }
    
    
    // 回调函数
    context : $(trigger)   // 各个回调函数的 this 参数，默认为 $(trigger)
    on_begin  : {C}F(pmvContext)  // 移动开始时
    on_ing    : {C}F(pmvContext)  // 移动时
    on_end    : {C}F(pmvContext)  // 移动结束时
    on_update : {C}F(pmvContext)  // 开始或移动结束时，主要用来更新 heper
});
``` 

# 如何销毁

```
$(context).PointerMoving("destroy");
```

* 销毁会删除在宿主上的事件监听

