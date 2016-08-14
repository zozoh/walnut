(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
    'app/wn.hmaker2/hm__methods',
], function(ZUI, Wn, FormUI, HmMethods){
//==============================================
var html = function(){/*
<div class="ui-arena hm-prop-ele" ui-fitparent="yes">
    <div class="hmpe-info"></div>
    <div class="hmpe-form" ui-gasket="form"></div>
    <div class="hmpe-hide"></div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hm_prop_edit_ele", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function() {
        var UI = HmMethods(this);
    },
    //...............................................................
    events : {
        "click .hmpe-hide" : function() {
            this.fire("hide:com:ele");
        }
    },
    //...............................................................
    update : function(comEle) {
        var UI = this;

        // 处理 info 区域
        if(comEle._info)
            UI.arena.find(".hmpe-info").html(comEle._info);

        // 处理控件扩展区域
        $z.invoke(UI.gasket.form, "update", [comEle]);
    },
    //...............................................................
    showBlank : function() {
        var UI = this;

        UI.arena.find(".hmpc-info").empty();

        if(UI.gasket.form)
            UI.gasket.form.destroy();
    },
    //...............................................................
    draw : function(uiDef, callback) {
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