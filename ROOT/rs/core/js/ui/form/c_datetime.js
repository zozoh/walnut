(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_ctrl',
    'ui/form/c_date',
    'ui/form/c_time'
], function(ZUI, FormMethods, DateUI, TimeUI){
//==============================================
var html = function(){/*
<div class="ui-arena com-datetime">
    <div class="cdt-date" ui-gasket="date"></div>
    <div class="cdt-time" ui-gasket="time"></div>
    <div class="cdt-btn" a="clear">{{clear}}</div>
</div>
*/};
//==============================================
return ZUI.def("ui.form_com_datetime", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/form/theme/component-{{theme}}.css",
    //...............................................................
    init : function(opt){
        FormMethods(this);

        $z.setUndefined(opt, "clearable", true);
        $z.setUndefined(opt, "dateFormat", "yyyy-mm-dd");
    },
    //...............................................................
    events : {
        'click .cdt-btn[a="clear"]' : function(){
            var UI = this;

            UI._set_data(null, true);
            UI.__on_change();
        }
    },
    //...............................................................
    redraw : function(){
        var UI   = this;
        var opt  = UI.options;
        
        // 日期
        new DateUI({
            parent : UI,
            gasketName : "date",
            clearable  : false,
            on_change : function(){
                UI.__on_change()
            }
        }).render(function(){
            UI.defer_report("date");
        });

        // 时间
        new TimeUI({
            parent : UI,
            gasketName : "time",
            clearable  : false,
            on_change : function(){
                UI.__on_change()
            }
        }).render(function(){
            UI.defer_report("time");
        });

        // 返回延迟加载
        return ['date', 'time'];
    },
    //...............................................................
    // 只接受 Date 对象或者 Date 对象的数组
    _set_data : function(d, showBlink){
        var UI = this;

        // 显示数据
        UI.gasket.date._set_data(d);
        UI.gasket.time._set_data(d);

        // 显示效果
        if(showBlink)
            $z.blinkIt(UI.arena);
    },
    //...............................................................
    _get_data : function(){
        var UI = this;

        // 得到日期
        var s_date = UI.gasket.date._get_data('yyyy-mm-dd');
        if(!s_date)
            return null;

        // 得到时间
        var s_time = UI.gasket.time._get_data('HH:mm:ss');
        if(!s_time)
            s_time = '00:00:00';

        // 拼合一下
        var str = s_date + 'T' + s_time;
        //console.log(str)

        // 返回
        return $z.parseDate(str);
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);