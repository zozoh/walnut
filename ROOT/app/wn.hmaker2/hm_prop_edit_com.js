(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
    'app/wn.hmaker2/hm__methods',
], function(ZUI, Wn, FormUI, HmMethods){
//==============================================
var html = function(){/*
<div class="ui-arena hm-prop-com">
    <div class="hmpc-info"></div>
    <div class="hmpc-form" ui-gasket="form"></div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hm_prop_edit_com", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function() {
        var UI = HmMethods(this);
    },
    //...............................................................
    update : function(com) {
        var UI = this;

        // 处理 info 区域
        var html = UI.msg("hmaker.com."+com._type+".icon");
        html += '<b>'+UI.msg("hmaker.com."+com._type+".name")+'</b>';
        html += '<em>'+com._seq+'</em>';
        UI.arena.find(".hmpc-info").html(html);

        // 处理控件扩展区域
        //UI.gasket.form.update(com);
        $z.invoke(UI.gasket.form, "update", [com]);
    },
    //...............................................................
    showBlank : function() {
        var UI = this;

        UI.arena.find(".hmpc-info").empty();

        if(UI.gasket.form)
            UI.gasket.form.destroy();
    },
    //...............................................................
    drawCom : function(uiDef, callback) {
        var UI = this;

        // 先销毁
        if(UI.gasket.form)
            UI.gasket.form.destroy();

        // 没定义，就直接回调了
        if(!uiDef) {
            $z.doCallback(callback, [], UI);
        }
        // 设置
        else {
            // 重新绑定控件
            seajs.use(uiDef.uiType, function(PropUI){
                new PropUI(_.extend({}, uiDef.uiConf||{}, {
                    parent : UI,
                    gasketName : "form"
                })).render(function(){
                    $z.doCallback(callback, [this], UI);
                });
            });
        }
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);