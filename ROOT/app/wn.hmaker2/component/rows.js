(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com_layout'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = '<div class="ui-arena hmc-rows"></div>';
//==============================================
return ZUI.def("app.wn.hm_com_rows", HmComMethods({
    keepDom : true,
    dom     : html,
    className : "!hm-layout hm-com-rows",
    //...............................................................
    init : function() {
        this.__area_id_prefix = this.msg("hmaker.com._area.row");
    },
    //...............................................................
    applyBlockCss : function(cssCom, cssArena){
        this.$el.css(cssCom);
        this.arena.css(cssArena);
        this._is_defined_size_max_value = !$D.dom.isUnset(cssCom.height);
        this.makeFullIfOnlyOneArea();
    },
    //...............................................................
    _apply_area_size : function(jArea, asize) {
        jArea = this.getArea(jArea);
        asize = asize || jArea.attr("area-size");
        jArea.css("height", asize || "");
        jArea.find(">.hm-area-con").css("height", asize ? "100%" : "");
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/rows_prop.js',
            uiConf : {}
        };
    }
}));
//===================================================================
});
})(window.NutzUtil);