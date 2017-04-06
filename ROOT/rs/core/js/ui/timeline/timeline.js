(function($z){
$z.declare([
    'zui',
    'jquery-plugin/pmoving/pmoving',
], function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="timeline.obj" class="tmln-obj"><div class="tmln-objw">
        <header class="ui-clr"><dt></dt><em></em></header>
        <section></section>
        <footer><span>==</span></footer>
        <div class="tmln-obj-del"><i class="zmdi zmdi-close"></i></div>
    </div></div>
</div>
<div class="ui-arena tmln" ui-fitparent="yes"><div class="tmln-con">
    <div class="tmln-bg"></div>
    <div class="tmln-ruler"></div>
    <!-- 这里是一个个的层 -->
    <div class="tmln-layer-con"></div>
</div></div>
*/};
//==============================================
return ZUI.def("ui.timeline", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/timeline/theme/timeline-{{theme}}.css",
    //..............................................
    events : {
        "click .tmln-hour" : function(e) {
            var UI  = this;
            var opt = UI.options;

            var jHour = $(e.currentTarget);
            var tFrom = UI.__get_timeObj(jHour);
            var tTo   = UI.__get_timeObj(jHour, true);

            // 调用回调，以便创建新块
            $z.invoke(opt, "on_create", [tFrom,tTo, function(tlo, layer){
                if(tlo) {
                    // 各种检查
                    if(!tlo.from)
                        throw "timline.on_create->callback: no tlo.from";
                    if(!tlo.to)
                        throw "timline.on_create->callback: no tlo.to";
                    
                    // 执行添加
                    UI.__add_block(tlo, layer);
                }
            }], UI);
        },
        "click .tmln-obj-del" : function(e) {
            var UI  = this;
            var opt = UI.options;
            var jBlock = $(e.currentTarget).closest(".tmln-obj");
            var jLayer = jBlock.closest(".tmln-layer");
            jBlock.remove();

            UI.do_on_change(jLayer);
        }
    },
    //..............................................
    do_on_change : function(jBlock) {
        var UI   = this;
        var opt  = UI.options;

        var data = UI.getData();
        var layerName = jBlock.closest(".tmln-layer").attr("layer-name");
        
        var context = {
            currentObj : jBlock.data("@TLO"),
            layerName  : layerName,
            layerData  : data[layerName],
            data       : data,
        };

        // 调用层回调
        var optLayer = (opt.layers || {})[layerName];
        $z.invoke(optLayer, "on_layer_change", [context.layerData], context);

        // 调用全局回调
        $z.invoke(opt, "on_change", [data], context);
    },
    //..............................................
    $layer : function(layer) {
        var UI = this;
        // 指定了添加的层名称
        if(_.isString(layer)) {
            return UI.arena.find('.tmln-layer[layer-name="'+layer+'"]');
        }
        // 指定了添加层的序号
        if(_.isNumber(layer)) {
             return UI.arena.find('.tmln-layer:eq('+layer+')');
        }
        // 本身就是 jQuery 或者 DOM
        if($z.isjQuery(layer) || _.isElement(layer)){
            return $(layer);
        }
        // 如果没指定，那么默认选择最后一个层（即最高层）
        return UI.arena.find('.tmln-layer:last-child');
    },
    //..............................................
    __add_block : function(tlo, layer) {
        var UI  = this;
        var opt = UI.options;

        // 增加块
        var jBlock = UI.__append_block_dom(tlo, layer);

        // 最后发出绘制通知回调
        UI.do_on_change(jBlock);
    },
    //..............................................
    __append_block_dom : function(tlo, layer) {
        var UI  = this;

        // 找到对应的层
        var jLayer = UI.$layer(layer);

        if(jLayer.size() == 0){
            throw "invalid layerName for timline.__add_block : " + layer;
        }
        var ly = jLayer.data("@LAYER");

        var tFrom = $z.parseTime(tlo.from);
        var tTo   = $z.parseTime(tlo.to);

        var jBlock = UI.ccode("timeline.obj")
                        .appendTo(jLayer)
                        .css(ly.css);

        // 保存对象
        jBlock.data("@TLO", tlo);
        
        // 修改区块的大小
        UI.__update_obj_display(jBlock);

        // 调用回调进一步绘制
        var context = UI.__context(jBlock);
        $z.invoke(ly, "on_draw_block", [tlo], context);

        // 返回
        return jBlock;
    },
    //..............................................
    // 返回一个绘制块的上下文
    __context : function(jBlock) {
        return {
            $block : jBlock,
            $info  : jBlock.find(">.tmln-objw>header>em"),
            $main  : jBlock.find(">.tmln-objw>section"),
            ui     : this
        };
    },
    //..............................................
    __update_obj_display : function(jBlock) {
        var UI  = this;
        var opt = UI.options;

        var tlo   = jBlock.data("@TLO");
        var tFrom = $z.parseTime(tlo.from);
        var tTo   = $z.parseTime(tlo.to);

        // 修改显示值
        var time_text = $z.timeText(tFrom, "12H") + " - " + $z.timeText(tTo, "12H");
        jBlock.find("> .tmln-objw > header > dt").text(time_text);

        // 得到 top 和 height 的比例
        var scaleTop    = tFrom.sec / 86400;
        var scaleHeight = (tTo.sec - tFrom.sec) / 86400;

        // 得到视口的矩形
        var H = UI.arena.find(".tmln-con").height();

        jBlock.css({
            top    : H * scaleTop,
            height : H * scaleHeight
        });

    },
    //..............................................
    __get_timeObj : function(jHour, end) {
        var sec = jHour.attr("sec") * 1;
        if(end)
            sec += jHour.attr("duration") * 1;
        return $z.parseTime(sec);
    },
    //..............................................
    init : function(opt) {
        $z.setUndefined(opt, "layers", {});
    },
    //..............................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;
        var jCon = UI.arena.find(".tmln-con");
        var jBg  = jCon.find(".tmln-bg");
        var jRu  = jCon.find(".tmln-ruler");
        var jLyCon = jCon.find(">.tmln-layer-con");

        // 输出 24 个小时的时间槽和标尺
        for(var i=0;i<24;i++){
            var sec = i * 3600;
            var key = (i>9?"":"0")+i+":00";
            // 时间槽
            $('<div class="tmln-hour">')
                .attr("sec", sec)
                .attr("key", key)
                .attr("duration", 3600)
                .appendTo(jBg);
            // 标尺
            $('<div class="tmln-rui">')
                .attr("sec", sec)
                .attr("key", key)
                .text(key)
                .appendTo(jRu);
        }

        // 输出层
        for(var layerName in opt.layers) {
            var layer = opt.layers[layerName];
            $('<div class="tmln-layer">')
                .attr("layer-name", layerName)
                .data("@LAYER", layer)
                .appendTo(jLyCon);
        }

        // 响应各层的鼠标拖拽事件
        jCon.pmoving({
            trigger   : ".tmln-obj",
            mode      : "y",
            maskClass : "tmln-mask",
            on_begin  : function(){
                var jq = $(this.Event.target);
                // 得到 tmln-obj
                this.$tmlnObj = jq.closest(".tmln-obj");
                this.rect.tmlnObj = $z.rect(this.$tmlnObj);

                // 确定最小高度
                this.minHeight = this.$tmlnObj.find(">.tmln-objw>header").outerHeight(true);

                // 标识遮罩层
                if(jq.closest("footer").size() > 0){
                    this.aMode = "obj-resize";
                    this.$mask.addClass("tmln-obj-resize");
                }
                // 否则就是移动
                else {
                    this.aMode = "obj-move";
                    this.$mask.addClass("tmln-obj-move");
                }
            },
            autoUpdateTriggerBy : null,
            boundary : "100%",
            on_ing : function() {
                var tlo  = this.$tmlnObj.data("@TLO");
                var tFrom = $z.parseTime(tlo.from);
                var tTo   = $z.parseTime(tlo.to);

                var css = {}
                // 模式: move
                if("obj-move" == this.aMode) {
                    css.top = this.rect.inview.top;
                    // 改变 from 和 to
                    var du = tTo.sec - tFrom.sec;
                    var fromSec = Math.round((css.top / this.rect.viewport.height) * 86400 / 1800) * 1800;
                    tlo.from = $z.parseTime(fromSec).key;
                    tlo.to   = $z.parseTime(fromSec + du).key;
                }
                // 模式: resize
                else {
                    css.height = Math.max(this.minHeight, this.rect.trigger.bottom - this.rect.tmlnObj.top);
                    // 仅仅改变 to
                    var toSec = tFrom.sec + Math.round((css.height / this.rect.viewport.height) * 86400 / 1800) * 1800;
                    tlo.to = $z.parseTime(toSec).key;
                }
                // 修改
                this.$tmlnObj.css(css);
                
                // 修改区块的大小
                UI.__update_obj_display(this.$tmlnObj);
            },
            on_end : function() {
                UI.do_on_change(this.$tmlnObj);                
            }
        });
    },
    //..............................................
    getData : function(layer) {
        var UI = this;

        // 返回固定某层数据
        if(layer) {
            return UI.getLayerData(layer);
        }

        // 逐层返回
        var re = {};
        UI.arena.find(".tmln-layer").each(function(){
            var jLayer = $(this);
            var lynm = jLayer.attr("layer-name");
            re[lynm] = UI.getLayerData(jLayer);
        });
        return re;
    },
    //..............................................
    getLayerData : function(layer) {
        var jLayer = this.$layer(layer);
        var re = [];
        jLayer.children(".tmln-obj").each(function(){
            re.push($(this).data("@TLO"));
        });
        return re;
    },
    //..............................................
    setData : function(data, layer) {
        var UI = this;

        // 返回固定某层数据
        if(layer) {
            return UI.setLayerData(layer, data[layer]);
        }

        // 逐层添加
        UI.arena.find(".tmln-layer").each(function(){
            var jLayer = $(this);
            var layerName = jLayer.attr("layer-name");
            UI.setLayerData(layerName, data[layerName]);
        });
    },
    //..............................................
    setLayerData : function(layer, layerData) {
        var UI = this;

        var jLayer = UI.$layer(layer).empty();

        if(_.isArray(layerData)) {
            for(var i=0; i<layerData.length; i++) {
                var tlo = layerData[i];
                UI.__append_block_dom(tlo, jLayer);
            }
        }
    },
    //..............................................
    resize : function() {
        var UI = this;
        UI.arena.find(".tmln-layer>.tmln-obj").each(function(){
            UI.__update_obj_display($(this));
        });
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);