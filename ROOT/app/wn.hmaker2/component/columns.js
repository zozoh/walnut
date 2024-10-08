(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com_layout'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = '<div class="ui-arena hmc-columns"></div>';
//==============================================
return ZUI.def("app.wn.hm_com_columns", HmComMethods({
    keepDom : true,
    dom     : html,
    className : "!hm-layout hm-com-columns",
    //...............................................................
    init : function() {
        this.__area_id_prefix = this.msg("hmaker.com._area.col");
    },
    //...............................................................
    applyBlockCss : function(cssCom, cssArena){
        this.$el.css(cssCom);
        this.arena.css(cssArena);
        this._is_defined_size_max_value = !$D.dom.isUnset(cssCom.width);
        this.makeFullIfOnlyOneArea();
    },
    //...............................................................
    _apply_area_size : function(jArea, asize) {
        jArea = this.getArea(jArea);
        asize = asize || jArea.attr("area-size");
        // 设置数字
        if(/^([\d.]+)(px|rem|%)?$/.test(asize)) {
            jArea.css({
                "width" : asize,
                "flex"  : "0 0 auto"
            });
        }
        // 仅仅是紧凑模式
        else if("compact" == asize) {
            jArea.css({
                "width" : "",
                "flex"  : "0 0 auto"
            });   
        }
        // 否则全部去掉
        else {
            jArea.css({
                "width" : "",
                "flex"  : ""
            });   
        }
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/columns_prop.js',
            uiConf : {}
        };
    }
}));
//===================================================================
});
})(window.NutzUtil);