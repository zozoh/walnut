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
<div class="ui-arena com-edit-link hm-link-box">
    <span class="cel-icon"></span>
    <span class="cel-href"></span>
    <span class="cel-del" balloon="down:clear">
        <i class="zmdi zmdi-close"></i>
    </span>
</div>
*/};
//==============================================
return ZUI.def("ui.form_com_edit-link", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : 'app/wn.hmaker2/support/theme/hmaker_support-{{theme}}.css',
    //...............................................................
    init : function(opt) {
        FormCtrlMethods(HmMethods(this));
    },
    //...............................................................
    events : {
        // 编辑链接
        "click .com-edit-link .cel-href" : function(){
            var UI   = this;
            var opt  = UI.options;
            var href = UI._get_data();
            UI.openEditLinkPanel({
                emptyItem : opt.emptyItem,
                fixItems  : opt.fixItems,
                href      : href,
                callback  : function(href){
                    UI._set_data(href, true);
                }
            });
        },
        // 清除链接
        "click .com-edit-link .cel-del" : function(){
            this._set_data("", true);
        }
    },
    //...............................................................
    redraw : function() {
        this.balloon();
    },
    //...............................................................
    // 只接受 Date 对象或者 Date 对象的数组
    _set_data : function(str, showBlink){
        var UI = this;
        
        // 设置
        UI.setLinkToBox(UI.arena, str);

        // 效果
        if(showBlink){
            $z.blinkIt(UI.arena);

            // 要闪光，表示要更新，触发一下事件
            UI.__on_change();
        }
    },
    //...............................................................
    _get_data : function(){
        return this.getLinkFromBox(this.arena);
    },
    //...............................................................
    setData : function(link){
        this.ui_parse_data(link, function(str){
            this._set_data(str);
        });
    },
    //...............................................................
    getData : function(){
        var UI = this;
        return this.ui_format_data(function(opt){
            return UI._get_data();
        });
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);