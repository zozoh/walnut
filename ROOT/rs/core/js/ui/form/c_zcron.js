(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_ctrl',
    'ui/zcron/zcron',
    'ui/pop/pop'
], function(ZUI, FormMethods, ZCron, POP){
//==============================================
var html = function(){/*
<div class="ui-arena com-zcron">
    <span></span>
    <a>{{edit}}</a>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_com_zcron", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/form/theme/component-{{theme}}.css",
    i18n : "ui/zcron/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt) {
        var UI = FormMethods(this);

        $z.setUndefined(opt, 'default',  "0 0 0 * * ?");
        $z.setUndefined(opt, 'onlyShowDateText', false);
    },
    //...............................................................
    events : {
        'click a' : function() {
            var UI = this;
            var opt = UI.options;

            POP.zcron(UI._get_data()||opt.default, {
                title : "编辑日期范围",
                ok : function(uiCron){
                    var ozc = uiCron.getData();
                    var str = ozc.toString();
                    // console.log(str, ozc);
                    UI._set_data(str);
                    UI.__on_change();
                }
            }, UI);
        }
    },
    //...............................................................
    redraw : function() {
        var UI = this;
        var opt = UI.options;
        
        if(opt.readonly) {
            UI.arena.find('> a').hide();
        }
    },
    //...............................................................
    _get_data : function() {
        return this.__ZCRON;
    },
    //...............................................................
    _set_data : function(str) {
        var UI = this;
        var opt = UI.options;
        var jS = UI.arena.find('>span');

        str = $.trim(str);
        this.__ZCRON = str;
        // 有值
        if(str) {
            var ozc = new ZCron(str);
            jS.text(ozc.toText(UI.msg("zcron.exp"), opt.onlyShowDateText));
        }
        // 无值
        else {
            jS.text("");
        }
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);