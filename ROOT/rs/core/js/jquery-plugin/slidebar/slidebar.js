/*
在给定的元素上，停靠一个滑块条，可以拖动的方式改变数值
*/
(function($, $z){
//...........................................................
function $ele(ele){
    return $(ele).closest(".slidebar").data("@ELE");
}
//...........................................................
function $root(ele){
    return $(ele).closest(".slidebar");
}
//...........................................................
function _opt(ele){
    return $root(ele).data("@OPT");
}
//...........................................................
function try_move_pointer(jSb){
    var jPo  = jSb.find(".sb-pointer");
    var jEle = $ele(jSb);
    var opt  = _opt(jSb);

    // 已经无效了
    if(!jSb.attr("mouse-noup")){
        return;
    }

    // 那么做一下标识，css 会让遮罩覆盖住整个网页
    jSb.attr("pointer-moving", "yes");

    // 得到一下主体区域
    var viewport = $z.rect(jSb);

    //console.log($(jSb[0].ownerDocument.body).size());

    // 监控 body 上的释放
    $(jSb[0].ownerDocument.body).one("mouseup", function(){
        $(this).off("mousemove");
        jSb.removeAttr("pointer-moving");
    })
    // 处理移动事件
    .on("mousemove", function(e){
        //console.log(e.pageX, e.pageY);
        var pos;
        // 水平
        if("horizontal" == opt.mode){
            var x = Math.max(viewport.left, e.pageX);
            x = Math.min(viewport.right, x);
            pos = (x - viewport.left)/viewport.width;
        }
        // 垂直 
        else{
            var y = Math.max(viewport.top, e.pageY);
            y = Math.min(viewport.bottom, y);
            pos = (viewport.bottom - y) / viewport.height;
        }
        //console.log("pos", pos);
        // 调用回调
        var val = pos_to_val(jSb, pos);
        $z.invoke(opt, "change", [val, pos], opt.context||jEle);

        // 更新滑块
        update_pointer_by_pos(jSb, pos);
    });
}
//...........................................................
function update_pointer_by_val(ele, val){
    var pos = val_to_pos(ele, val);
    update_pointer_by_pos(ele, pos);
}
//...........................................................
function update_pointer_by_pos(ele, pos){
    var jSb = $root(ele);
    var opt = _opt(jSb);
    var jPo = jSb.find(".sb-pointer");
    var viewport = $z.rect(jSb);
    var rect     = $z.rect(jPo);

    // 水平
    if("horizontal" == opt.mode){
        jPo.css("left", viewport.width * pos - (rect.width/2));
    }
    // 垂直 
    else{
        jPo.css("bottom", viewport.height * pos - (rect.height/2));
    }
}
//...........................................................
function get_pos_by_pointer(ele){
    var jSb = $root(ele);
    var opt = _opt(jSb);
    var rect     = $z.rect(jSb.find(".sb-pointer"));
    var viewport = $z.rect(jSb);

    // 水平
    if("horizontal" == opt.mode){
        return (rect.x - viewport.left) / viewport.width;
    }
    // 垂直 
    return (viewport.bottom - rect.y) / viewport.height;
}
//...........................................................
function val_to_pos(ele, val){
    var jSb = $root(ele);
    var opt = _opt(jSb);
    var r0  = opt.range[0];
    var r1  = opt.range[1];

    // 如果 `range=[0, 100]`，即开始点值比较小
    if(r0 < r1){
        return Math.min(r1, val) / (r1 - r0)
    }
    // 那么就是 `range=[100, 0]`，即开始点值比较大
    return 1.0 - Math.min(r0,val) / (r0 - r1);
}
//...........................................................
function pos_to_val(ele, pos){
    var jSb = $root(ele);
    var opt = _opt(jSb);
    var r0  = opt.range[0];
    var r1  = opt.range[1];

    var val;
    // 如果 `range=[0, 100]`，即开始点值比较小
    if(r0 < r1){
        val = (r1 - r0) * pos;
    }
    // 如果 `range=[100, 0]`，即开始点值比较大
    else{
        val = r0 - (r0 - r1)*pos;
    }

    // 最后看看怎么取整
    if(/^(round|ceil|floor)$/.test(opt.valueBy)){
        return Math[opt.valueBy](val);
    }

    // 如果定义了取值函数
    if(_.isFunction(opt.valueBy)){
        return opt.valueBy.apply(opt.context||$ele(jSb), [val, pos]);
    }

    // 那就直接返回吧
    return val;
}
//...........................................................
function on_click_body(e){
    var jSb = e.data;
    var opt = _opt(jSb);
    var du  = Date.now() - jSb.attr("ct") * 1;
    // 啥也没干呢，就取消了
    if(du < 1000){
        return;
    }
    // 点击的是自己，也不取消
    if($(e.target).closest(".slidebar").size()>0){
        return;
    }
    //console.log(jSb, jSb.attr("ct"));
    var jEle = $ele(jSb);
    //console.log(jSb, jEle.data("@SLIDEBAR"));
    // 那么注销吧
    jEle.slidebar("destroy");
}
//...........................................................
var html = function(){/*
<div class="slidebar"><div class="slidebar-wrapper">
    <div class="sb-change-mask"></div>
    <div class="sb-bg-0"></div>
    <div class="sb-bg-1"></div>
    <div class="sb-ruler"></div>
    <div class="sb-pointer"></div>
</div></div>
*/};
//...........................................................
$.fn.extend({ "slidebar" : function(opt, arg){
    // 取得所在的文档
    var jBody = $(this[0].ownerDocument.body);

    var jSb = this.data("@SLIDEBAR");

    // 销毁控件
    if("destroy" == opt){
        this.removeData("@SLIDEBAR");
        jBody.off("click", on_click_body);
        if(jSb)
            jSb.remove();
        return this;
    }

    // 更新控件位置
    if("pos" == opt){
        update_pointer_by_pos(jSb, arg);
        return this;
    }

    // 用值更新控件位置
    if("val" == opt){
        update_pointer_by_val(jSb, arg);
        return this;
    }

    // 没有销毁，就不要创建了
    if(jSb && jSb.size()>0)
        return this;

    // 默认配置必须为对象
    opt = $z.extend({
        pos      : "dock",
        dockMode : "VC",
        mode     : "vertical",
        size     : 100,
        range    : [0, 100],
        valueBy  : "round",
        ruler    : 1,
        magnet   : false
    }, opt);

    // 绘制
    jSb = $($z.getFuncBodyAsStr(html, true)).attr({
        "pos"  : opt.pos,
        "mode" : opt.mode
    });

    // 记录一下关键数据
    this.data("@SLIDEBAR", jSb);
    jSb.data("@ELE", this);
    jSb.data("@OPT", opt);

    // 根据位置绘制
    if("inner" == opt.pos){
        jSb.appendTo(this);
    }else{
        jSb.appendTo(jBody);
        $z.dock(this, jSb, opt.dockMode);
    }

    // 绘制标尺
    if(opt.ruler > 1){
        var jRuler = jSb.find(".sb-ruler");
        for(var i=0; i<opt.ruler + 1; i++){
            $('<div>').appendTo(jRuler);
        }
    }

    // 如果有值，则更新滑块位置
    if(!_.isUndefined(opt.value)){
        update_pointer_by_val(opt.value);
    }

    // 记录一下当前创建的时间，以便在一定时间内，点击 body，本控件不注销
    jSb.attr("ct", Date.now());

    // 绑定事件激活拖动
    jSb.on("mousedown", function(){
        jSb.attr("mouse-noup", "yes");
        window.setTimeout(try_move_pointer, 100, jSb);
    });
    jSb.on("mouseup", function(){
        jSb.removeAttr("mouse-noup");
    });
    jSb.on("dblclick", function(){
        var pos = get_pos_by_pointer(jSb)
        console.log("pos:", pos);
    });

    // 绑定模式，会自动注销
    if("dock" == opt.pos){
        jBody.on("click", jSb, on_click_body);
    }

    // 设置物理尺寸
    jSb.css("horizontal"==opt.mode?"width":"height", opt.size);
    
    // 返回自身以便链式赋值
    return this;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

