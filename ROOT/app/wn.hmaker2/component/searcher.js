(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com',
    '/gu/rs/ext/hmaker/hmc_searcher.js'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = `<div class="ui-arena hmc-searcher hm-empty-save">
    <div class="kwd-input"><input></div>
    <div class="kwd-btn"><b></b></div>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_searcher", {
    dom     : html,
    keepDom   : true,
    className : "!hm-com-searcher",
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    paint : function(com) {
        var UI = this;
        
        // 标识保存时属性
        UI.arena.addClass("hm-empty-save");

        // 绘制
        UI.arena.hmc_searcher(_.extend({
            forIDE : true
        }, com));
        
    },
    //...............................................................
    getComValue : function() {
        return this.arena.hmc_searcher("value");
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
            "placeholder"  : this.msg("hmaker.com.searcher.plhd_dft"),
            "btnText"      : this.msg("search"),
            "defaultValue" : "",
            "maxLen"       : 0,
            "trimSpace"    : true,
        };
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/searcher_prop',
            uiConf : {}
        };
    }
});
//===================================================================
});
})(window.NutzUtil);