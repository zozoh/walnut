(function($z){
$z.declare([
    'zui',
    'jquery-plugin/pmoving/pmoving',
], function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="timeline.obj" class="tmln-obj"><div class="tmln-objw">
        <header><dt></dt></header>
        <section></section>
        <footer><span>==</span></footer>
    </div></div>
</div>
<div class="ui-arena tmln" ui-fitparent="yes"><div class="tmln-con">
    <div class="tmln-bg"></div>
    <div class="tmln-ruler"></div>
    <!-- 这里是一个个的层 -->
</div></div>
*/};
//==============================================
return ZUI.def("ui.timeline", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/ui/timeline/timeline.css",
    //..............................................
    events : {
        "click .tmln-hour" : function(e) {
            var UI  = this;
            var opt = UI.options;
            var context = opt.context || UI;

            var jHour = $(e.currentTarget);
            var tFrom = UI.__get_timeObj(jHour);
            var tTo   = UI.__get_timeObj(jHour, true);

            console.log("create on:", tFrom.key)

            // 调用回调，以便创建新块
            $z.invoke(opt, "on_create", [tFrom,tTo, function(tlo){
                if(tlo) {
                    if(tlo.layer && tlo.from && tlo.to) {
                        UI.__add_block(tlo);
                    }
                    // 格式错误 
                    else {
                        throw "invalid param for timline.on_create->callback" + tlo;
                    }
                }
            }], context);
        }
    },
    //..............................................
    __add_block : function(tlo) {
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;

        // 找到对应的层
        var jLayer = UI.arena.find('.tmln-layer[layer-name="'+tlo.layer+'"]');
        if(jLayer.size() == 0){
            throw "invalid layerName for timline.__add_block : " + tlo.layer;
        }
        var ly = jLayer.data("@LAYER");


        var tFrom = $z.parseTime(tlo.from);
        var tTo   = $z.parseTime(tlo.to);

        var time_text = $z.timeText(tFrom, "12H") + " - " + $z.timeText(tTo, "12H");
        var jBlock = UI.ccode("timeline.obj")
                        .appendTo(jLayer);
        jBlock.find("> .tmln-objw > header > dt").text(time_text);
        jBlock.css(ly.css);

        // 保存对象
        jBlock.data("@TLO", tlo);
        
        // 修改区块的大小
        UI.__update_obj_display(jBlock);

        // 调用回调进一步绘制
        $z.invoke(ly, "on_draw_block", [jBlock, tlo], context);
        
    },
    //..............................................
    __update_obj_display : function(jBlock) {
        var UI  = this;
        var opt = UI.options;

        var tlo   = jBlock.data("@TLO");
        var tFrom = $z.parseTime(tlo.from);
        var tTo   = $z.parseTime(tlo.to);

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
                .appendTo(jCon);
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
                console.log(jq.html())
                this.rect.tmlnObj = $z.rect(this.$tmlnObj);

                // 标识遮罩层
                if(jq.closest("footer").size() > 0){
                    this.aMode = "obj-resize";
                    this.$mask.addClass("tmln-obj-resize");
                    console.log("resize")
                }
                // 否则就是移动
                else {
                    this.aMode = "obj-move";
                    this.$mask.addClass("tmln-obj-move");
                    console.log("move")
                }
            },
            autoUpdateTriggerBy : null,
            boundary : "100%",
            position : {
                gridX  : "100%",
                gridY  : "4.1.66667%",
                stickX : 0,
                stickY : "4%"
            },
            on_ing : function() {
                var css = {}
                // 模式: move
                if("obj-move" == this.aMode) {
                    css.top = this.rect.inview.top;
                }
                // 模式: resize
                else {
                    css.height = Math.max(10, this.rect.trigger.bottom - this.rect.tmlnObj.top);
                }
                // 修改
                this.$tmlnObj.css(css);
            }
        });


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