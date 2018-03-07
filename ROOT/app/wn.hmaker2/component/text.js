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

        // 得到站点 HOME 路径
        var oSiteHome  = UI.getHomeObj();
        var phSiteHome = oSiteHome.ph;

        // 得到当前网页路径
        var oPage  = UI.pageUI().getCurrentEditObj();
        var phPageDir = $z.getParentPath(oPage.ph);

        // 得到内容类型
        var ttp = com.contentType || "auto";

        // 解析 Markdown
        if(code){
            //console.log(phSiteHome, phPageDir);

            // 如果包括换行，则表示是 markdown 文本
            if("markdown" == ttp || ("auto" == ttp && code.indexOf('\n') >=0 )){
                html = $z.markdownToHtml(com.code, {
                    media : function(src) {
                        return UI.__tidy_src(src, oSiteHome, phPageDir);
                    }
                });
                html = '<article class="md-content">' + html + '</article>';
            }
            // 否则就是纯文本
            else {
                html = $z.escapeText(com.code);
                html = html.replace(/\r?\n/g, '<br>');
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

        // 删掉空的 <p>
        UI.arena.find("p").each(function(){
            if(!$(this).html())
                $(this).remove();
        });

        // 清理 <div>
        UI.arena.find("div").each(function(){
            var jDiv = $(this);
            var br;
            // 删掉 DIV 开头和结尾的 BR
            var children = jDiv.children();
            if (children.size() > 0) {
                br = children.first();
                if ("BR" == br[0].tagName) {
                    br.remove();
                }
            }
            if (children.size() > 1) {
                br = children.last();
                if ("BR" == br[0].tagName) {
                    br.remove();
                }
            }
            // 删掉 DIV 前的 BR
            br = jDiv.prev();
            if (br.size() > 0 && "BR" == br[0].tagName) {
                br.remove();
            }
            // 删掉 DIV 后的 BR
            br = jDiv.next();
            if (br.size() > 0 && "BR" == br[0].tagName) {
                br.remove();
            }
        });

        // 修改图片和多媒体的源
        UI.arena.find('img').each(function(){
            var jImg = $(this); 
            var src  = jImg.attr("src");
            var src2 = UI.__tidy_src(src, oSiteHome, phPageDir);
            if(src!=src2) {
                jImg.attr("src", src2);
            }
        });

        // 标识标题
        UI.arena.find("h1,h2,h3,h4,h5,h6").addClass("md-header");
    },
    //...............................................................
    __tidy_src : function(src, oSiteHome, phPageDir) {
        // 已经被整理过了
        if(/^\/o\/read\/id:/.test(src))
            return src;

        // 找到对应的媒体文件
        var phMedia;

        // 绝对路径是相对于站点的根
        if(/^\//.test(src)){
            phMedia = Wn.appendPath(oSiteHome.ph, src);
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
            uiType : 'app/wn.hmaker2/com_prop/text_prop',
            uiConf : {}
        };
    }
});
//===================================================================
});
})(window.NutzUtil);