(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com_dynamic'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = `<div class="hmc-dynamic ui-arena hm-del-save">
    <section></section>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_objlist", {
    dom     : html,
    keepDom : false,
    //...............................................................
    init : function(){
        var UI = HmComMethods(this);
    },
    //...............................................................
    events : {
        
    },
    //...............................................................
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/dynamic_prop',
            uiConf : {}
        };
    },
    //...............................................................
    paint : function(com) {
        var UI = this;
        var jW = UI.$el.find(".hm-com-W")

        // 得到数据
        com = com || UI.getData();

        // 检查显示模式
        if(!UI.__check_mode(com)){
            return ;
        }

        var jList = UI.arena.find(">section").show();

        jList.text("will draw ...");

    },
    //...............................................................
    __check_mode : function(com) {
        var UI = this;
        var jMsg = UI.arena.find(">section").empty();

        // 确保有数据接口
        if(!com.api) {
            jMsg.html(UI.msg("hmaker.com.dynamic.noapi"));
            UI.arena.attr("display-mode", "warn");
            return false;
        }

        // 确保有显示模板
        if(!com.template) {
            jMsg.html(UI.msg("hmaker.com.dynamic.notemplate"));
            UI.arena.attr("display-mode", "warn");
            return false;
        }
        
        // 通过检查
        UI.arena.attr("display-mode", "list");
        return true;
        
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);