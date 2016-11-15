(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com',
    'ui/mask/mask',
], function(ZUI, Wn, HmComMethods, MaskUI){
//==============================================
var html = `
<div class="ui-arena hmc-text">
    <header>{{hmaker.com.text.blank_title}}</header>
    <section>{{hmaker.com.text.blank_content}}</section>
</div>
`;
//==============================================
return ZUI.def("app.wn.hm_com_text", {
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        // 编辑标题
        'click .hmc-text > header' : function(e){
            var UI = this;
            var jq = $(e.currentTarget);
            if(!UI.isInActivedBlock(jq))
                return;

            $z.editIt(jq);
        },
        // 编辑内容 
        'click .hmc-text > section' : function(e){
            var UI = this;
            var jq = $(e.currentTarget);
            if(!UI.isInActivedBlock(jq))
                return;

            // 得到文字内容
            var com = UI.getData();

            console.log(com.contentType)

            // 打开编辑器
            new MaskUI({
                width  : 900,
                height : "90%",
                dom : 'ui/pop/pop.html',
                css : 'ui/pop/pop.css',
                events : {
                    "click .pm-btn-ok" : function(){
                        var html = this.body.getHtml();
                        jq.html(html);
                        this.close();
                    },
                    "click .pm-btn-cancel" : function(){
                        this.close();
                    }
                }, 
                setup : {
                    uiType : 'ui/zeditor/zeditor',
                    uiConf : {
                        contentType : com.contentType
                    }
                }
            }).render(function(){
                this.arena.find(".pm-title").html(UI.msg('hmaker.com.text.tt_editor'));
                this.body.setHtml(jq);
            });
        }
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        // 设置初始值的 DOM 结构
        if(!UI.arena.hasClass("ui-arena")){
            UI.arena = $($z.tmpl(html)(UI._msg_map)).appendTo(UI.$el.empty());
        }
    },
    //...............................................................
    paint : function(com) {
        var UI = this;
        var jTitle   = UI.arena.children("header");
        var jContent = UI.arena.children("section");

        // 准备 CSS 的 base
        var cssBase = {
            fontSize:"",fontFamily:"",letterSpacing:"",lineHeight:"",
            margin:"",padding:"",
            color:"",background:"",textShadow:"",textAlign:""
        };

        // 更新标题显示
        if(false === com.showTitle){
            jTitle.hide();
        }
        // 显示标题
        else{
            jTitle.show();
            var css = UI.formatCss(com.title,  cssBase);
            jTitle.css(css);
        }


        // 更新内容显示
        css = UI.formatCss(com.content,  cssBase);
        jContent.css(css);
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'ui/form/form',
            uiConf : {
                uiWidth : "all",
                fields : [  // 文字区属性
                {
                    title : "i18n:hmaker.com.text.tt_content",
                    fields : [{
                        key   : "contentType",
                        title : 'i18n:hmaker.com.text.contentType',
                        type  : "string",
                        editAs : "switch",
                        uiConf : {
                            items : [{
                                text : 'i18n:hmaker.com.text.ct_text',
                                val  : "text"
                            }, {
                                text : 'i18n:hmaker.com.text.ct_html',
                                val  : "html"
                            }]
                        }
                    }, {
                        key   : "content.textAlign",
                        title : 'i18n:hmaker.com.text.textAlign',
                        type  : "string",
                        editAs : "switch",
                        uiConf : {
                            items : [{
                                icon : '<i class="fa fa-align-left">',
                                val  : 'left',
                            }, {
                                icon : '<i class="fa fa-align-center">',
                                val  : 'center',
                            }, {
                                icon : '<i class="fa fa-align-right">',
                                val  : 'right',
                            }]
                        }
                    }, {
                        key   : "content.color",
                        title : 'i18n:hmaker.com.text.color',
                        type   : "string",
                        nullAsUndefined : true,
                        editAs : "color",
                    }, {
                        key   : "upperFirst",
                        title : 'i18n:hmaker.com.text.upperFirst',
                        type  : "boolean",
                        editAs : "switch",
                    }, {
                        key   : "content.lineHeight",
                        title : 'i18n:hmaker.com.text.lineHeight',
                        type  : "string",
                        editAs : "input",
                    }, {
                        key   : "content.letterSpacing",
                        title : 'i18n:hmaker.com.text.letterSpacing',
                        type  : "string",
                        editAs : "input",
                    }, {
                        key   : "content.fontSize",
                        title : 'i18n:hmaker.com.text.fontSize',
                        type  : "string",
                        editAs : "input",
                    }, {
                        key   : "content.textShadow",
                        title : 'i18n:hmaker.com.text.textShadow',
                        type  : "string",
                        editAs : "input",
                    }]
                },
                // 标题的属性
                {
                    title : "i18n:hmaker.com.text.tt_title",
                    fields : [{
                        key   : "showTitle",
                        title : 'i18n:hmaker.com.text.showTitle',
                        type  : "boolean",
                        editAs : "switch",
                    }, {
                        key   : "title.textAlign",
                        title : 'i18n:hmaker.com.text.textAlign',
                        type  : "string",
                        editAs : "switch",
                        uiConf : {
                            items : [{
                                icon : '<i class="fa fa-align-left">',
                                val  : 'left',
                            }, {
                                icon : '<i class="fa fa-align-center">',
                                val  : 'center',
                            }, {
                                icon : '<i class="fa fa-align-right">',
                                val  : 'right',
                            }]
                        }
                    }, {
                        key   : "title.color",
                        title : 'i18n:hmaker.com.text.color',
                        type   : "string",
                        nullAsUndefined : true,
                        editAs : "color",
                    }, {
                        key   : "title.marginBottom",
                        title : 'i18n:hmaker.com.text.marginBottom',
                        type  : "string",
                        editAs : "input",
                    }, {
                        key   : "title.lineHeight",
                        title : 'i18n:hmaker.com.text.lineHeight',
                        type  : "string",
                        editAs : "input",
                    }, {
                        key   : "title.letterSpacing",
                        title : 'i18n:hmaker.com.text.letterSpacing',
                        type  : "string",
                        editAs : "input",
                    }, {
                        key   : "title.fontSize",
                        title : 'i18n:hmaker.com.text.fontSize',
                        type  : "string",
                        editAs : "input",
                    }, {
                        key   : "title.textShadow",
                        title : 'i18n:hmaker.com.text.textShadow',
                        type  : "string",
                        editAs : "input",
                    }]
                }]
                
            }
        }
    }
});
//===================================================================
});
})(window.NutzUtil);