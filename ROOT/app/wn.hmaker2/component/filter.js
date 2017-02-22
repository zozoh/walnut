(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com',
    '/gu/rs/ext/hmaker/hmc_filter.js'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = '<div class="ui-arena hmc-filter hmc-cnd"></div>';
//==============================================
return ZUI.def("app.wn.hm_com_filter", {
    dom     : html,
    keepDom   : true,
    className : "!hm-com-filter",
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        "click .hmcf-exts b" : function(){
            var UI = this;

            // 在激活的组件内容才生效
            if(!UI.isActived())
                return;

            // 切换折叠状态
            UI.__is_folder_show = !UI.__is_folder_show;
            UI.__sync_folder();
        }
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        // 标识保存时属性
        UI.arena.addClass("hm-empty-save");

        // 绘制
        UI.arena.hmc_filter(_.extend({ignoreShowHideEvent:true}, com));

        // 同步折叠项的状态
        UI.__sync_folder();
        
    },
    //...............................................................
    __sync_folder : function(){
        var UI = this;
        // 显示折叠项
        if(UI.__is_folder_show) {
            UI.arena.hmc_filter("showFolder");
        }
        // 隐藏折叠项
        else{
            UI.arena.hmc_filter("hideFolder");
        }
    },
    //...............................................................
    getBlockPropFields : function(block) {
        return [block.mode == 'inflow' ? "margin" : null,
                "padding","border","borderRadius",
                "color", "background",
                "boxShadow","overflow"];
    },
    //...............................................................
    getDefaultData : function(){
        return {
            fields : [],
            btnExtTextShow : UI.msg("com.hmaker.filter.btnExtTextShow"),
            btnExtTextHide : UI.msg("com.hmaker.filter.btnExtTextHide"),
            btnMultiText   : UI.msg("hmaker.com.filter.multi"),
        };
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/filter_prop',
            uiConf : {}
        };
    }
});
//===================================================================
});
})(window.NutzUtil);