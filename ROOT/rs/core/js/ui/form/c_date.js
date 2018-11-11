(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_ctrl',
    'ui/pop/pop'
], function(ZUI, FormMethods, POP){
//==============================================
var html = function(){/*
<div class="ui-arena com-date">
    <div class="cd-box">
        <i class="fa fa-calendar"></i>
        <span></span>
    </div>
    <div class="cd-btn" a="clear">{{clear}}</div>
</div>
*/};
//==============================================
return ZUI.def("ui.form_com_date", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : ["ui/form/theme/component-{{theme}}.css", 
            "jquery-plugin/zcal/theme/zcal-{{theme}}.css"],
    //...............................................................
    init : function(opt){
        FormMethods(this);

        $z.setUndefined(opt, "setup", {});
        $z.setUndefined(opt, "clearable", true);
        $z.setUndefined(opt, "format", "yyyy-mm-dd");
    },
    //...............................................................
    events : {
        'click .cd-box' : function(){
            var UI = this;
            var d = UI.__VALUE;
            
            POP.date(d, function(newDate) {
                UI._set_data(newDate, true);
                UI.__on_change();
            });
        },
        'click .cd-btn[a="clear"]' : function(){
            var UI = this;

            UI._set_data(null, true);
            UI.__on_change();
        }
    },
    //...............................................................
    redraw : function(){
        var UI   = this;
        var opt  = UI.options;
        // 不用取消
        if(!opt.clearable){
            UI.arena.find('.cd-btn[a="clear"]').remove();
        }
    },
    //...............................................................
    // 只接受 Date 对象或者 Date 对象的数组
    _set_data : function(d, showBlink){
        var UI   = this;
        var opt  = UI.options;

        // 准备显示
        var jBox = UI.arena.find(".cd-box");
        var jBco = jBox.find(">span").empty();

        // 格式化数据并记录
        UI.__VALUE = d;

        // 显示数据
        if(d){
            // 准备显示函数
            var __display = _.isFunction(opt.formatDate)
                    ? opt.formatDate
                    : function(d){
                        d = $z.parseDate(d);
                        return d ? d.format(opt.format)
                                 : "";
                    };
            // 显示数据
            jBco.text(__display(d));
        }

        // 显示效果
        if(showBlink)
            $z.blinkIt(jBco);
    },
    //...............................................................
    _get_data : function(format){
        if(format) {
            if(this.__VALUE) {
                var d = $z.parseDate(this.__VALUE);
                if(d)
                    return d.format(format);
            }
            return '';
        }
        return this.__VALUE;
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);