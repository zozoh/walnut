(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/pop/pop',
    'ui/mask/mask',
    'app/wn.hmaker2/support/hm__methods_com',
], function(ZUI, Wn, POP, MaskUI, HmComMethods){
//==============================================
var html = '<div class="ui-arena hmc-text hm-empty-save"></div>';
//==============================================
return ZUI.def("app.wn.hm_com_text", {
    dom     : html,
    keepDom   : true,
    className : "!hm-com-text",
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        // 得到编辑的文本，并将文本转义成 HTML (markdown) 模式
        var code = com.code || "";
        var html;

        // 解析 Markdown
        if(code){
            html = $z.markdownToHtml(com.code);
        }
        // 显示空文本
        else {
            html = UI.compactHTML(`<div class="empty-content">
                    <i class="zmdi zmdi-info-outline"></i>
                    {{hmaker.com.text.blank_content}}
                 </div>`);
        }

        // 更新 HTML
        UI.arena.html(html);
    },
    //...............................................................
    getBlockPropFields : function(block) {
        return [block.mode == 'inflow' ? "margin" : null,
                "padding","border","borderRadius", "textAlign",
                "fontFamily","_font","fontSize",
                "lineHeight","letterSpacing","textShadow",
                "color", "background",
                "boxShadow","overflow"];
    },
    //...............................................................
    getDefaultData : function(){
        return {
            // "lineHeight" : ".24rem",
            // "fontSize"   : ".14rem",
        };
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/htmlcode_prop',
            uiConf : {
                contentType : "text",
                title       : "i18n:hmaker.com.text.tt",
                openText    : "i18n:hmaker.com.text.open",
                editorTitle : "i18n:hmaker.com.text.edit_tt",
            }
        };
    }
});
//===================================================================
});
})(window.NutzUtil);