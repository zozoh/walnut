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

        // 得到内容类型
        var ttp = com.contentType || "auto";

        // 解析 Markdown
        if(code){
            //console.log(phSiteHome, phPageDir);

            // 如果包括换行，则表示是 markdown 文本
            if("markdown" == ttp || ("auto" == ttp && code.indexOf('\n') >=0 )){
                html = $z.markdownToHtml(com.code, {
                    media : function(src) {
                        return UI.tidy_src(src, oSiteHome, oPage);
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

        // 处理一下文章
        var jAr = UI.arena.find("article.md-content");
        if(jAr.size() > 0) {
            // 修改图片和多媒体的源
            jAr.find('img').each(function(){
                var jImg = $(this); 
                var src  = jImg.attr("src");
                var src2 = UI.tidy_src(src, oSiteHome, oPage);
                if(src!=src2) {
                    jImg.attr("src", src2);
                }
            });

            // 标识标题
            jAr.find("h1,h2,h3,h4,h5,h6").addClass("md-header");

            // 解析一下海报
            jPoster = jAr.find('pre[code-type="poster"]');
            $z.explainPoster(jPoster, {
                media : function(src) {
                    return UI.tidy_src(src, oSiteHome, oPage);
                }
            });

            // 处理一下视频
            $z.wrapVideoSimplePlayCtrl(jAr.find('video'), {
                watchClick : false
            });
        }

    },
    //...............................................................
    getMyAnchors : function() {
        var UI = this;
        var re = [];
        UI.arena.find('article.md-content a[name]').each(function(){
            var an = $.trim($(this).attr("name"));
            if(an)
                re.push(an);
        });
        return re;
    },
    //...............................................................
    getBlockPropFields : function(block) {
        return [block.mode == 'inflow' ? "margin" : null,
                "padding","border","borderRadius", "textAlign",
                "fontFamily","fontSize",
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