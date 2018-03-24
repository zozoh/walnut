(function($z){
$z.declare([
    'zui',
    'ui/menu/menu'
], function(ZUI, MenuUI){
//==============================================
var html = function(){/*
<div class="ui-arena edit-markdown" ui-fitparent="true">
    <header class="edmd-menu" ui-gasket="menu"></header>
    <section class="edmd-edit">
        <textarea placeholder="{{markdown.empty}}" spellcheck="false"></textarea>
    </section>
    <section class="edmd-preview">
        <article></article>
    </section>
</div>
*/};
//==============================================
return ZUI.def("ui.edit-markdown", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/markdown/theme/markdown-{{theme}}.css",
    i18n : "ui/markdown/i18n/{{lang}}.js",
    //..............................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        // 设置模式
        UI.changeMode(opt.defaultMode, true);

        // 菜单配置信息
        var menuSetup = [].concat(opt.menu);
        for(var i=0; i<menuSetup.length; i++){
            if("preview" == menuSetup[i]){
                menuSetup[i] = {
                        type : "boolean",
                        on : "preview" == opt.defaultMode,
                        text_on  : 'i18n:edit',
                        icon_on  : '<i class="zmdi zmdi-edit"></i>',
                        text_off : "i18n:preview",
                        icon_off : '<i class="zmdi zmdi-eye"></i>',
                        on_change : function(isPreview) {
                            UI.changeMode(isPreview ? "preview" : "edit");
                        }
                    }
            }
        }

        // 创建菜单
        new MenuUI({
            parent : UI,
            gasketName : "menu",
            setup : menuSetup
        }).render(function(){
            UI.defer_report("menu");
        });

        return ["menu"];
    },
    //..............................................
    changeMode : function(m, quiet) {
        var UI  = this;
        var opt = UI.options;
        m = m || "edit";

        // 标识 
        UI.arena.attr("m", m);
        
        // 预览模式，更新预览内容
        if("preview" == m) {
            UI.updatePreviewHtml();
        }

        // 回调模式改变
        if(!quiet)
            $z.invoke(opt, "on_mode", [m], UI);
    },
    //..............................................
    updatePreviewHtml : function(){
        var UI  = this;
        var opt = UI.options;
        //console.log(opt.preview)
        var str  = UI.getData();
        var html = str ? $z.markdownToHtml(str, opt.preview)
                       : UI.compactHTML('<div class="edmd-empty"><%=markdown.emptyPreview%></div>');
        var jAr = UI.arena.find("> section.edmd-preview > article").html(html);

        // 解析一下海报
        jPoster = jAr.find('pre[code-type="poster"]');
        $z.explainPoster(jPoster, opt.preview);

        // 处理一下视频
        $z.wrapVideoSimplePlayCtrl(jAr.find('video'), {
            watchClick : false,
        });
    },
    //..............................................
    _set_markdown : function(str){
        var UI  = this;
        var jTx = UI.arena.find(".edmd-edit textarea");
        jTx.val(str);
        UI.updatePreviewHtml();
    },
    //..............................................
    _get_markdown : function() {
        return this.arena.find(".edmd-edit textarea").val();
    },
    //..............................................
    setData : function(str) {
        this._set_markdown(str);
    },
    //..............................................
    getData : function(){
        return this._get_markdown();
    },
    //..............................................
    resize : function(){
        var UI = this;
        var jM = UI.arena.find(">.edmd-menu");
        var jS = UI.arena.find(">section");
        jS.css({
            "height": UI.arena.height() - jM.outerHeight()
        });
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);