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
    events : {
        "dblclick .ui-arena" : function(e){
            // 仅针对激活控件有效
            if(!this.isActived())
                return;

            // 如果当前模式是区域选择，还需要同时高亮当前区域
            this.fire("show:prop", "com");
        }
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        // 确保 empty-save
        UI.arena.addClass("hm-empty-save");

        // 得到编辑的文本，并将文本转义成 HTML (markdown) 模式
        var code = com.code || "";
        var html;

        // 解析 Markdown
        if(code){
            // 得到站点 HOME 路径
            var oSiteHome  = UI.getHomeObj();
            var phSiteHome = oSiteHome.ph;

            // 得到当前网页路径
            var oPage  = UI.pageUI().getCurrentEditObj();
            var phPageDir = $z.getParentPath(oPage.ph);

            //console.log(phSiteHome, phPageDir);

            // 如果包括换行，则表示是 markdown 文本
            if(code.indexOf('\n') >=0 ){
                html = $z.markdownToHtml(com.code, {
                    media : function(src) {
                        // 找到对应的媒体文件
                        var phMedia;

                        // 绝对路径是相对于站点的根
                        if(/^\//.test(src)){
                            phMedia = Wn.appendPath(oSiteHome, src);
                        }
                        // 相对路径的话
                        else {
                            phMedia = Wn.appendPath(phPageDir, src);
                        }

                        // 合并路径中的 ..
                        phMedia = $z.getCanonicalPath(phMedia);

                        // 如果存在这个文件，则转换为 /o/read 的形式
                        var oMedia = Wn.fetch(phMedia, true);
                        if(oMedia){
                            return "/o/read/id:" + oMedia.id;
                        }
                        return src;
                    }
                });
                html = '<article class="md-content">' + html + '</article>';
            }
            // 否则就是纯文本
            else {
                html = $z.escapeText(com.code);
            }
            //console.log(html)
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