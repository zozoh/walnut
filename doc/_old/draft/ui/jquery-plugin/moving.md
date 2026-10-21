
# 浏览器兼容性

```
Safari:
 - 内部 window 的 "mousemove" 事件，如果窗口滚动下去，就截获不到了
 - Safari 的确傻逼
```

# 几个概念

*moving* 插件实际上是：在指定`选区(selection)`监控`触发器(trigger)`的*mousedown*事件。
一旦触发，寻找移动的`目标(target)`，并监视其再`视口(viewport)` 里的移动。

移动的时候，插件会在顶级窗口准备`遮罩(mask)`元素，并在其内监控`感应器(sensor)`对应的行为，并根据配置，动态的绘制辅助线。

- `选区(selection)`就是插件的 *this*
- `窗体(win)`整个窗体
- `可视区域(client)`鼠标捕获区域，这个区域可能有 scrollBar
- `视口(viewport)`得到*目标*后，查找自己的视口，默认为*选区*
- `触发器(trigger)`声明了*选区*内可以被触发的元素选择器，默认为 `> *`
    - 插件会用`$(selection).on("mousedown", trigger, F())`来触发
- `目标(target)`得到*触发器*后，会在触发器里查找真正的拖拽目标，默认为*触发器*本身
- `感应器(sensor)`会在鼠标经过后触发指定操作。默认感应器有
    - `scroll-N` :  向上滚动(优先)
    - `scroll-S` :  向下滚动(优先)
    - `scroll-W` :  向左滚动
    - `scroll-E` :  向右滚动
- 调用者可以自行添加更多的感应器，比如添加*drop*的目标等
- `遮罩(mask)`为拖拽时动态绘制的元素的父元素，拥有比较高的*z-index*，它覆盖整个*可视区域(client)*

# 遮罩层的DOM结构

```
window
    iframe
        document.body
            某拖拽目标
    DIV.z-moving-mask
        DIV.z-mvm-sensors         <!--// 全局感应器绘制层-->
            DIV.z-mvm-sit         <!--// 在视口外的可见感应器 -->
               SECTION            <!--// 感应器的可见部分包裹容器 -->
               ASIDE@md="x"       <!--// 辅助元素X -->
               ASIDE@md="y"       <!--// 辅助元素Y -->
               SPAN               <!--// 如果感应器有 text，写在这里-->
        DIV.z-mvm-client                <!--// 感应器副本-->
            DIV.z-mvm-client-con        <!--// 感应器副本包裹-->
                ASIDE.cl-placeholder         <!--// 占位块，用来让视口内部大小与原本一致-->
                DIV.z-mvm-viewport           <!--// 视口副本-->
                    DIV.z-mvm-viewport-con    <!--// 视口副本包裹-->
                        DIV.z-mvm-sit         <!--// 视口内感应器 -->
                            ...               <!--// @see 上面的感应器 DOM 结构 -->
                DIV.z-mvm-sit         <!--// 在视口内的可见感应器 -->
                    ...               <!--// @see 上面的感应器 DOM 结构 -->
        DIV.z-mvm-target          <!--// 移动目标的显示副本-->
        SVG                       <!--// 辅助线绘制层-->
            LINE                  <!--// 水平辅助线-->
            LINE                  <!--// 垂直辅助线-->
```

- `DIV.z-mvm-sit` 表示可视的感应器的帮助元素，有属性 `se-actived="yes"` 表示激活的感应器

# 运行时上下文

在插件被触发会创建如下的`上下文对象(MVing)`:

```
Event   : Event,       // 事件对象
options : {..},        // 插件的配置信息
win     : Window,      // 选区所在的窗口
doc     : Element,     // 选区所在文档对象
body    : Element,     // 选区所在文档对象的 Body 元素
$body   : jQuery,      // 选区所在文档对象的 Body 元素 jQuery 包裹
$selection : jQuery,   // 选区的 jQuery 对象
$trigger   : jQuery,   // 触发器的 jQuery 对象
data : ..              // 来着 options.data
//..................................................
$client    : jQuery,   // 鼠标捕捉区
$viewport  : jQuery,   // 视口的 jQuery 对象
$target    : jQuery,   // 移动目标的 jQuery 对象
viewportIsClient : true, // 视口和鼠标捕捉区域是否重合
//..................................................
// 表示计算 target 初始矩形时，需要对 client 矩形转换坐标系
// 通常，是在 <iframe> 嵌套的内联文档对象里，要启用这个选项
isTargetInClient : false,
//..................................................
// 记录视口初始的滚动
viewportScroll : {
    x: 0,
    y: 0;
},
//..................................................
// 初始点击位置信息
posAt : {
    win      : {x,y},      // 相对于窗体
    client   : {x,y},      // 相对于鼠标捕捉区
    viewport : {x,y},      // 相对于视口
    target   : {x,y},      // 相对于目标
},
//..................................................
startInMs : MS,       // 开始时间
//--------------------------------------------------
// 以下为真正触发移动后创建的属性
//--------------------------------------------------
endInMs   : MS,       // 结束时间（有了结束时间表示移动结束了）
//..................................................
// 指针位置
cursor : {
    win      : {x,y}   // 相对于窗体
    client   : {x,y}   // 相对于鼠标捕捉区
    viewport : {x,y}   // 相对于视口
    delta    : {x,y}   // 相对于上次位置的位移
    offset   : {x,y}   // 相对于初始点击位置
                       // 即 cursor.client - posAt.client
},
//..................................................
// 移动方向
direction : {
    // 相对于上次位置的方向
    delta : {
        x : "left" | "right" | null,
        y : "up"   | "down"  | null
    },
    // 相对于初始点击位置的方向
    offset : {
        x : "left" | "right" | null,
        y : "up"   | "down"  | null
    },
}
//..................................................
// 矩形信息
// 如果参数给定了 viewportRect，那么所有的矩形队友对其进行转换
// 即，如果是一个 iframe 内的网页，且 mask 默认是附加在顶级窗口对象上的
// 则需要制定 viewportRect，那么这组矩形信息都是相对于顶级窗口的
rect : {
    client   : Rect,      // 鼠标捕捉区，null 表示整个窗体
    viewport : Rect,      // 视口
    target   : Rect,      // 原始目标
    current  : Rect,      // 目标当前跟随移动所应该的设置的矩形
},
//..................................................
// 是否忽略视口的滚动信息
// 默认为 false。如果为 true，则表示计算 css.rect 的时候
// 不考虑 client 的 scrollTop/scrollLeft
// 在设置 <div> 内嵌的 absolute 块时尤其需要标记为 true
// 这个属性一般是在移动初始化的几个函数里的某个一个设置，譬如
//  - clientRect
//  - viewport
//  - viewportRect
//  - init
//  - target
//  - on_begin
ignoreViewportScroll :  false,
//..................................................
// 根据目标矩形，计算出一个矩形，并用这个矩形来限制目标边界
// 返回 null 或者没有这个函数定义都表示不限制
boundaryBy : {c}F(targetRect):Rect
//..................................................
// 计算当前目标和视口的关系
css : {
    rect    : Rect,     // 目标当前相对于视口的位置信息
                        // 补偿了视口的 scrollTop/Left
    current : {..},     // 给出 css 描述，默认 top,left
                        // 配置项 cssBy 指定了要获取哪些属性
},
//..................................................
// 遮罩层的其他绘制层
$mask      : jQuery,   // 遮罩的 jQuery 对象
mask : {
    $client    : jQuery,   // 鼠标捕捉区部分
    $clientCon : jQuery,   // 鼠标捕捉区容器
    $clientPlaceHolder : jQuery, // 来跟随视口 scroll 的
    $viewport  : jQuery,   // 视口副本，如果与捕捉区重叠，则与mask.$client相等
    $viewportCon : jQuery, // 视口容器，如果与捕捉区重叠，则与mask.$clientCon相等
    $sensors  : jQuery,    // 视口外可见感应器绘制层
    $target   : jQuery,    // 移动目标副本
    assist : {             // 辅助绘制对象集合
        $root  : jQuery    // 对应 <svg>
        $lineX : jQuery    // 对应水平的 <line>
        $lineY : jQuery    // 对应垂直的 <line>
    }
},
//..................................................
// 感应区，数组按顺序匹配。遇到匹配的感应区则停止
// 因此靠前的感应区优先级比较高
sensors : [{
    index   : 0,         // 下标，0 base，作为唯一标识「这个会自动生成，你设置也没用」
    name    : "xxx",     // 感应器名称，插件会在 sensorFunc 里执行对应的匹配方法
    text    : "xxx",     // 可见感应器，显示文字
    $ele    : jQuery,    // 感应区对应的元素。如果为 null 则必须指定 scope
    rect    : Rect|10,   // 感应区范围，如果不指定，则默认根据 $ele 计算
                         // 如果是一个数字，则表示根据 $ele 计算是增加的 padding 
    $helper : jQuery,    // 可见感应器对应的显示元素。即 DIV.z-mvm-sit
                         // 不可见的感应器，此项为 null
                         // 「这个会自动生成，你设置也没用」
    className : "xxx",   // 如果声明，则为 $helper 增加特殊的类选择器名称
    scope     : true,    // 范围，win|client|viewport，
                         // 如果指定了 $ele 则自动计算
                         // 默认为 win
    visible    : true,   // 是否要在绘制层显示感应区
    matchBreak : true,   // 如果匹配上了，是否继续匹配后续感应器，默认 true
    disabled   : false,  // 如果设置，这个感应器将永远不被激活，只用来显示
                         // 当然，如果都没有 visible，这个感应器相当于没用
    actived    : false   // 感应器状态，true 表示激活的感应器
                         // 「这个会自动生成，你设置也没用」
}],
// 感应器如果匹配上了，根据名称，执行集合中的函数
// 如果没有对应的函数，则会抛错
// 一个感应器函数必须有两个方法 
//   "enter" 表示进入感应器 
//   "leave" 表示离开感应器
// 每个方法都接受下列相同的参数
//  - {c}    : 为上下文对象本身
//  - sensor : 为感应器对象本身
//  - index  : 为感应器下标
sensorFunc : {
    "xxx" : {
        "enter" : {c}F(sensor),   // 进入触发一次
        "hover" : {c}F(sensor),   // 鼠标移动就触发
        "leave" : {c}F(sensor),   // 离开触发一次
    }
    ...
},
// 记录了当前的感应器下标，会根据这个来判断 enter/leave
currentSensor : [0,3]
```

> 这个上下文对象存放在 `window.__nutz_moving`，当移动完成后会被自动销毁

# 如何创建

```
$(ele).moving({
    //..................................................
    // 表示鼠标移动多少像素才会真正触发移动
    // 默认 3 像素
    fireRedius : 3
    //..................................................
    // 在选区之内哪些元素会触发插件
    // $(selection).on("mousedown", trigger, F())
    // 默认，会是 ">*"
    trigger  : "selector",
    
    // 查找视口，如果返回 null 或者一个空 jQuery 集合，那么表示禁止触发
    // 如果返回的是 window/document/body 任何一个元素，都表示整个文档窗口为
    // 视口，如果是内联文档，通常会指定 iframe 为视口
    // 如果未定义，则将当前选区作为视口
    viewport : jQuery | {c}F():jViewport,
    
    // 指定视口的矩形，如果有视口该选项生效
    // 默认的，插件会自动根据 viewport 来寻找视口
    // 在创建时指定了 "viewportRect"，则运行时字段 `targetIsRelative` 为 true
    // 表示计算 target 初始矩形时，需要对 viewport 矩形转换坐标系
    // 通常，是在 <iframe> 嵌套的内联文档对象里，要启用这个选项
    // !!! 注意，如果你给的是一个函数，那么每次初始化(mousedown时)都会被计算
    // 如果计算的值为 null, 则默认用 viewport 的元素进行计算
    viewportRect : Rect | {c}F():Rect
    
    // 指定鼠标捕捉区矩形，如果指定，鼠标事件的 clientX/Y 会根据这个区域换算成窗体坐标
    // 如果拖拽嵌套进 iframe 的网页元素，需要指定本选项
    // !!! 注意，如果你给的是一个函数，那么每次初始化(mousedown时)都会被计算
    // 如果计算的值为 null, 那么，如果指定了 viewportRect 选项，则用其值来作为鼠标捕捉区
    // 这就意味着，如果你的视口是 iframe 的 body，只需要指定 viewportRect 就好
    // 否则，你还需要指定 clientRect
    clientRect : Rect | {c}F():Rect
    
    // 有可能是 trigger 选择器内部某个元素（比如修改大小的手柄）被作为触发对象
    // 可以通过一个自定义函数，返回你确定要移动的元素。
    // 默认的，会认为整个 trigger 元素就是要移动的对象
    // 如果函数返回 null 或者一个空 jQuery 集合，那么表示禁止触发
    target : jQuery | {c}F():jTarget
    //..................................................
    // 在上下文中记录一个你自定义的对象
    // 在任何回调函数中（由于this都是 你可以直接从 MVing.data 获取你设置的值)
    // 这个值可以是一个函数，在插件真正进入移动时前，会调用，以便获取真正的数据对象 
    data : null | {c}F():Object,
    //..................................................
    // 建立的遮罩层 z-index，默认为 999999
    maskZIndex : 999999,
    
    // 为 $mask 附加类选择器，默认 null 不加
    maskClass : null,
    //..................................................
    // 滚动感应器
    // 它会创建一组最优先的感应器
    // null 表示不设置感应器
    // 默认为 {x:30, y:30}
    scrollSensor : {
        x : "10%",
        y : 30
    },
    
    // 滚动的步长，默认为 10
    // 这个值会被取绝对值，如果为0 则会被设置成默认值，即 0
    scrollStep : 10,
    
    // 滚动的时间间隔（毫秒），默认 50
    scrollInterval : 50,
    
    // 自定义感应器，函数返回一个感应器对象数组
    // 感应器对象参见 MVing 一节关于 sensors 的描述
    sensors : Sensor[] | {c}F():Sensor[..],
    
    // 感应器所使用的函数，参见 MVing 一节关于 sensorFunc 的描述
    sensorFunc : {
        "xxx" : {
            "enter" : {c}F(),
            "leave" : {c}F(),
        }
    },
    //..................................................
    // 辅助线层配置
    // 默认为 null 表示不显示辅助线
    assist : {
        // 根据当前目标矩形（rect.current）显示坐标轴，
        // 坐标轴原点为矩形的哪个顶点
        // 值必须为一个数组 [X, Y]，比如
        //  - 左上顶点 : ["left","top"]
        //  - 右上顶点 : ["right","top"]
        //  - 左下顶点 : ["left","bottom"]
        //  - 右下顶点 : ["right","bottom"]
        // 默认为 null 表示不显示坐标轴
        axis : ["left", "top"],
        
        // 坐标轴是否满屏显示
        // 默认为 false，表示只显示到视口
        axisFullScreen : false,
    },
    //..................................................
    // 自动修改 trigger 的位置时，采用哪个顶点
    // "top,left"      - 左上顶点
    // "top,right"     - 右上顶点
    // "bottom,left"   - 左下顶点
    // "bottom,right"  - 右下顶点
    //  null 表示不自动更新
    // 可以是数组模式，即 "top,left" 与 ["top","left"] 等价
    // 如果是拖拽模式，默认为 null
    // 否则默认为 "top,left"
    cssBy : "top,left"
    //..................................................
    // 移动的方式
    //   x : 只能横向移动
    //   y : 只能纵向移动
    //   默认为否， 即两个方向都能移动
    mode : "x" | "y" | undefined
    //..................................................
    // 如何判断 target 超出了 viewport
    // undefined : 表示不限制
    // 0 : 根据中心点
    // "100%" or Float : 为 trigger 尺寸（不包括外边距）的倍数
    // INT : 在当前宽高基础上，修改尺寸
    // 也可以是一个自定义函数，返回 target 用来限制边界的矩形
    // 如果返回`否`则表示不限制
    boundaryBy : 0 | Float | undefined | {c}F():Rect
    
    //..................................................
    // 全局回调函数
    init      : {c}F()   // 鼠标按下时，上下文对象刚刚生成，有了$trigger
    on_begin  : {c}F()   // 确定这是一个拖拽之后，还未设置传感器和计算目标尺寸之前
    on_reday  : {c}F()   // 设置好了拖拽上下文之后
    on_ing    : {c}F()   // 移动时
    on_end    : {c}F()   // 移动结束时
});
```

