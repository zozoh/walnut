(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com',
], function(ZUI, Wn, HmComMethods, MaskUI){
//==============================================
var html = '<div class="ui-arena hmc-htmlcode"></div>';
//==============================================
return ZUI.def("app.wn.hm_com_htmlcode", {
    dom     : html,
    keepDom   : true,
    className : "!hm-com-htmlcode",
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        // 分析 html 代码，过滤 <script> 标签
        var html = (com.code || "");
        html = html.replace(/<script/g, '<script type="text/x-template"');

        // 应用 HTML
        UI.arena.html(html || '<div class="empty-content"><i class="zmdi zmdi-info-outline"></i>' + UI.msg("hmaker.com.htmlcode.blank_content") + "</div>");

        // 记录一下 Code
        UI.$el.data("@code", com.code);
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
        return {};
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/htmlcode_prop',
            uiConf : {}
        };
    }
});
//===================================================================
});
})(window.NutzUtil);