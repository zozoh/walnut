(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/support/form_c_methods',
    'app/wn.hmaker2/support/hm__methods_panel',
], 
function(ZUI, Wn, FormCtrlMethods, HmMethods){
//==============================================
var html = function(){/*
<div class="ui-arena com-edit-link" nohref="yes">{{hmaker.link.none}}</div>
*/};
//==============================================
return ZUI.def("ui.form_com_edit-link", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : 'theme/app/wn.hmaker2/support/c_edit_link.css',
    //...............................................................
    init : function(opt) {
        FormCtrlMethods(HmMethods(this));
    },
    //...............................................................
    events : {
        "click .com-edit-link" : function(){
            var UI = this;
            var href = UI._get_data();
            UI.openEditLinkPanel({
                data     : href,
                callback : function(href){
                    UI._set_data(href, true);
                }
            });
        }
    },
    //...............................................................
    setData : function(link){
        this.ui_parse_data(link, function(str){
            this._set_data(str);
        });
    },
    //...............................................................
    // 只接受 Date 对象或者 Date 对象的数组
    _set_data : function(str, showBlink){
        var UI = this;
        var jU = UI.arena.find("u");

        // 记录值
        str = $.trim(str || "");
        UI.arena
            .attr("nohref", str ? null : "yes")
                .text(str || UI.msg("hmaker.link.none"));

        // 效果
        if(showBlink){
            $z.blinkIt(UI.arena);

            // 要闪光，表示要更新，触发一下事件
            UI.__on_change();
        }
    },
    //...............................................................
    getData : function(){
        var UI = this;
        return this.ui_format_data(function(opt){
            return UI._get_data();
        });
    },
    //...............................................................
    _get_data : function(){
        return this.arena.attr("nohref") ? "" : $.trim(this.arena.text());
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);