(function($z){
$z.declare([
    'zui',
    'ui/mask/mask',
    'jquery-plugin/zcal/zcal'
], 
function(ZUI, MaskUI){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="mask">
        <div class="ui-arena picker-mask">
            <div class="ui-mask-bg"></div>
            <div class="ui-mask-main"><div class="picker-mask-main">
                <div class="pm-title"></div>
                <div class="pm-body"></div>
                <div class="pm-btns">
                    <b class="pm-btn-ok">{{ok}}</b>
                    <b class="pm-btn-cancel">{{cancel}}</b>
                </div>
            </div></div>
            <div class="ui-mask-closer"></div>
        </div>
    </div>
</div>
<div class="ui-arena picker datepicker">
    <div class="picker-box"><i class="fa fa-calendar"></i><div class="pick-box-con"></div></div>
    <div class="picker-btn picker-choose">{{choose}}</div>
    <div class="picker-btn picker-clear">{{clear}}</div>
</div>
*/};
//==============================================
return ZUI.def("ui.picker.datepicker", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : ["ui/picker/theme/picker-{{theme}}.css", 
            "jquery-plugin/zcal/theme/zcal-{{theme}}.css"],
    i18n : "ui/picker/i18n/{{lang}}.js",
    //...............................................................
    events : {
        "click .picker-choose" : function(){
            var UI    = this;
            var opt   = UI.options;
            var setup = opt.setup || {};
            // 准备遮罩的宽高
            $z.setUndefined(setup, "blockNumber",  "range"==setup.mode?2:1);
            $z.setUndefined(setup, "width",  300 * (setup.blockNumber||1));
            $z.setUndefined(setup, "height", "range"==setup.mode?408:360);

            // 弹出遮罩层
            new MaskUI(_.extend({
                dom : UI.ccode("mask").html(),
                dom_events : {
                    "click .pm-btn-ok" : function(){
                        var uiMask = ZUI(this); 
                        var jBody  = uiMask.$main.find(".pm-body");
                        var dr     = jBody.zcal("range", opt.mode);
                        UI._update(dr, true);
                        UI.__on_change();
                        uiMask.close();
                    },
                    "click .pm-btn-cancel" : function(){
                        ZUI(this).close();
                    }
                },
                on_resize  : function(){
                    var jTitle = this.$main.find(".pm-title");
                    var jBody  = this.$main.find(".pm-body");
                    var jBtns  = this.$main.find(".pm-btns");
                    // 没标题
                    if(jTitle.size() == 0)
                        jBody.css("top", 0);
                    
                    // 没按钮
                    if(jBtns.size() == 0)
                        jBody.css("bottom", 0);

                    // 重新计算日历排布
                    jBody.zcal("resize");
                }
            }, setup)).render(function(){
                var jTitle = this.$main.find(".pm-title");
                var jBody  = this.$main.find(".pm-body");
                var jBtns  = this.$main.find(".pm-btns");
                // 设定标题
                if(opt.title){
                    jTitle.html(opt.title);
                }else{
                    jTitle.remove();
                }

                // 当前日期
                var obj = UI.getData();
                var currentData = _.isArray(obj) ? obj[0] : obj;

                // 显示日历
                jBody.zcal(_.extend({
                    i18n : UI.msg("dt"),
                    fitparent : true,
                    blockHeight : "100%",
                    current : currentData,
                    showBorder : false,
                }, setup, {
                    on_cell_click : function(e, d){
                        // 单选的话，直接就确认了
                        // 多选的话，得点确定才行
                        // 判断的依据就是有木有 .pm-btns 元素
                        var jBtns = $(this).closest(".picker-mask-main").find(".pm-btns");
                        if(jBtns.size() == 0){
                            UI._update(d, true);
                            UI.__on_change();
                            ZUI(this).close();
                        }
                    }
                }));
                // 单选的话，移除按钮，激活当前日期
                if("range" != setup.mode){
                    jBtns.remove();
                    jBody.zcal("active", obj);
                }
                // 多选才需要按钮，并选中范围
                else{
                    jBody.zcal("range", obj);
                }
                // 最后调用一下回调通知
                $z.invoke(opt, "on_pop", [jBody], UI);
                
            });
        },
        "click .picker-clear" : function(){
            this._update();
            this.__on_change();
        }
    },
    //...............................................................
    init : function(options) {
        $z.setUndefined(options, "setup", {});
        $z.setUndefined(options, "clearable", true);
    },
    //...............................................................
    redraw : function(){
        var UI   = this;
        var opt  = UI.options;
        // 不用取消
        if(!opt.clearable){
            UI.arena.find(".picker-clear").remove();
        }
    },
    //...............................................................
    setData : function(obj){
        this.ui_parse_data(obj, function(o){
            this._update(o);
        });
    },
    //...............................................................
    // 只接受 Date 对象或者 Date 对象的数组
    _update : function(o, showBlink){
        var UI   = this;
        var opt  = UI.options;

        // 准备显示
        var jBox = UI.arena.find(".picker-box");
        var jBco = jBox.find(".pick-box-con").empty();

        // 记录数据
        if(o){
            UI.$el.data("@OBJ", o);
            // 准备显示函数
            var __display = _.isFunction(opt.formatDate)
                                ? opt.formatDate
                                : function(d){
                                    d = $z.parseDate(d);
                                    return d.format("yyyy-mm-dd");
                                };

            // 范围的话，则是一个数组
            if(opt.setup.mode == "range"){
                jBco.text(UI.msg("picker.range", {
                    from : __display(o[0]),
                    to   : __display(o[1]),
                }));
            }
            // 单选的话，则是一个日期
            else{
                jBco.text(__display(o));
            }
        }
        // 移除数据
        else{
            UI.$el.data("@OBJ", null);
        }

        // 显示效果
        if(showBlink)
            $z.blinkIt(jBco);
    },
    //...............................................................
    getData : function(){
        var UI = this;
        return this.ui_format_data(function(opt){
            return UI.$el.data("@OBJ");
        });
    },
    //...............................................................
    __on_change : function(){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;
        var v = UI.getData();
        $z.invoke(opt, "on_change", [v], context);
        UI.trigger("change", v);
    },
    //...............................................................
    resize : function(){
        var UI = this;
        var jBtn = UI.arena.find(".picker-btns");
        var jBox = UI.arena.find(".picker-box");
        jBox.css({
            "padding-right" : jBtn.outerWidth(true)
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);