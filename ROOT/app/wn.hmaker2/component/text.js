(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com',
    'ui/mask/mask',
], function(ZUI, Wn, HmComMethods, MaskUI){
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

            // 打开编辑器
            new MaskUI({
                width  : 900,
                height : "90%",
                dom : 'ui/pop/pop.html',
                css : 'ui/pop/pop.css',
                events : {
                    "click .pm-btn-ok" : function(){
                        var html = this.body.getHtml();
                        UI.arena.html(html);
                        this.close();
                    },
                    "click .pm-btn-cancel" : function(){
                        this.close();
                    }
                }, 
                setup : {
                    uiType : 'ui/zeditor/zeditor',
                    uiConf : {
                        contentType : "text"
                    }
                }
            }).render(function(){
                this.arena.find(".pm-title").html(UI.msg('hmaker.com.text.tt_editor'));
                this.body.setHtml(UI.arena);                
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