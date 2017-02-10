(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/pop/pop',
    'ui/mask/mask',
    'app/wn.hmaker2/support/hm__methods_com',
], function(ZUI, Wn, POP, MaskUI, HmComMethods){
//==============================================
var html = '<div class="ui-arena hmc-text">{{hmaker.com.text.blank_content}}</div>';
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
    events : {
        // 编辑内容 
        'click > *' : function(e){
            var UI = this;

            if(!UI.isActived())
                return;

            // 得到文本内容
            var html = UI.arena.html();
            var text = html.replace(/<\/p>/g, '')
                    .replace(/<p>/g, '\n\n')
                    .replace(/<br>/g, '\n');
            var jDiv = $("<div>");
            text = jDiv.text(text).text();
            jDiv.remove();

            // 打开编辑器
            POP.openEditTextPanel({
                i18n        : UI._msg_map,
                title       : "i18n:hmaker.com.text.tt_editor",
                contentType : "text",
                data : text,
                callback : function(data){
                    var html = data.replace(/</g, "&lt;")
                                    .replace(/>/g, "&gt;")
                                    .replace(/(\r?\n){2,}/g, "<p>")
                                    .replace(/\r?\n/g, '<br>'); 
                    UI.arena.html(html);
                }
            });
        }
    },
    //...............................................................
    _redraw_com : function() {
        var UI = this;
        var jW = UI.$el.children(".hm-com-W");

        // 确保有辅助节点
        var jAA = jW.children(".hmc-text-tipicon");
        if(jAA.length == 0) {
            $('<div class="hmc-text-tipicon hm-del-save">')
                .html('<i class="zmdi zmdi-edit"></i>')
                    .append($('<em>').text(UI.msg("hmaker.com.text.edit_tip")))
                        .appendTo(jW);
        }
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        // console.log("paint text:", com)
        
        // 准备 CSS 的 base
        var cssBase = {
            fontSize:"",fontFamily:"",letterSpacing:"",lineHeight:"",
            color:"",background:"",textShadow:"",textAlign:""
        };

        var css = UI.formatCss(com, cssBase);
        UI.arena.css(com);
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
            // "lineHeight" : ".24rem",
            // "fontSize"   : ".14rem",
        };
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/text_prop',
            uiConf : {}
        };
    }
});
//===================================================================
});
})(window.NutzUtil);