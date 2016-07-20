(function($z){
$z.declare([
    'zui',
    'wn/util'
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="dft" class="chute-wrapper">
        <section text="i18n:favorites">
            <item><i class="fa fa-home"></i><b>i18n:home</b></item>
        </section> 
    </div>
</div>
<div class="ui-arena obrowser-chute ui-clr" ui-fitparent="yes">
    <div class="chute-scroller" doutline>
        <div class="chute-nav">{{loading}}</div>
        <div class="chute-outline outline-con">I am outline</div>
        <div class="chute-scroller-btn chute-show-outline"><i class="fa fa-caret-right"></i></div>
        <div class="chute-scroller-btn chute-show-nav"><i class="fa fa-caret-left"></i></div>
    </div>
</div>
*/};
//==============================================
return ZUI.def("ui.obrowser_chute", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    events : {
        "click section h1" : function(e){
            var jq   = $(e.currentTarget);
            var jSec = jq.parents("section");
            var orgH = jSec.attr("org-height") * 1;
            if(!orgH){
                orgH = jSec.outerHeight();
                jSec.attr("org-height", orgH);
                jSec.css("height", orgH);
            }
            // 隐藏
            if(jSec.hasClass("chute-sec-hide")){
                jSec.css("height", orgH).removeClass("chute-sec-hide");
            }
            // 显示
            else{
                jSec.css("height", jq.outerHeight(true)).addClass("chute-sec-hide");
            }
        },
        "click item" : function(e){
            var UI = this;
            var jq = UI.setActived(e.currentTarget);
            UI.browser.setData(jq.attr("ph"), jq.attr("editor"));
        },
        "click .chute-show-nav" : function(e){
            this.hideOutline();
        },
        "click .chute-show-outline" : function(e){
            this.showOutline();
        }
    },
    //..............................................
    // 根据传入的对象，自动高亮侧边栏的项目
    setActived : function(o, asetup){
        var UI = this;
        // console.log("!!!!",o.ph, asetup)
        // 找到 DOM 节点
        var jq;
        if(!_.isElement(o) && !$z.isjQuery(o)){
            // 查找侧边栏所有项目，看看哪个需要被高亮
            var jItems = UI.arena.find(".chute-nav item");
            for(var i=0;i<jItems.size();i++){
                var jItem = jItems.eq(i);
                var iPh = Wn.absPath(jItem.attr("ph") || "");
                if(iPh.length > o.ph.length)
                    continue;

                var ph1 = o.ph.substring(0, iPh.length);
                var ph2 = o.ph.substring(iPh.length);

                if(ph1 == iPh && (!ph2 || /^\//.test(ph2))) {
                    jq = jItem;
                }
            }
        }
        // 给定的就是侧边栏项目
        else {
            jq = $(o).closest("item");
        }
        // 修改显示
        if(jq){
            UI.arena.find("item").removeClass("chute-actived");
            jq.addClass("chute-actived");
        }
        return jq;
    },
    //..............................................
    showOutline : function(){
        var jS = this.arena.find(".chute-scroller").attr("outline", "yes");
        return jS.children(".chute-outline");
    },
    hideOutline : function(){
        this.arena.find(".chute-scroller").attr("outline", "no");
    },
    removeOutline : function(){
        this.arena.find(".chute-scroller").removeAttr("outline");
    },
    isShowOutline : function(){
        return this.arena.find(".chute-scroller").attr("outline") == "yes";
    },
    //..............................................
    update : function(UIBrowser, o, asetup){
        var UI = this;
        UI.browser = UIBrowser;  // 记录一下，让事件们访问能方便一下

        // 如果已经读取到侧边栏了，就仅仅高亮项目
        if(UI.arena.find("section").size()>0){
            UI.setActived(o, asetup);
            return;
        }

        // 得到侧边栏的配置信息
        var oSidebar = UIBrowser.fetch("~/.ui/sidebar.html", true);
        var jq;
        // 采用默认侧边栏
        if(null==oSidebar){
            jq = UI.ccode("dft");
        }
        // 采用指定的侧边栏
        else{
            var html = UIBrowser.read(oSidebar);
            jq = $('<div class="chute-wrapper">' + html + '</div>');
            // 去掉危险的 script 标签，以及所有元素中 on 开头的属性
            jq.find("script").remove();
            jq.find("*").each(function(){
                var jq = $(this);
                // 删掉事件相关的属性
                var attrs = this.attributes;
                for(var i=0; i<attrs.length;i++){
                    var atnm = attrs[i].name;
                    if(/^on.+/.test(atnm)){
                        jq.removeAttr(atnm);
                    }
                }
                // <b> 和 <h1-6> 需要替换 i18n
                if(/^(B|H[1-6])/.test(this.tagName)){
                    jq.text(UI.text(jq.text()));
                }
            })
            // 只有顶层才能有 "chute-wrapper" 的类选择器
            .removeClass("chute-wrapper");
        }

        // 整理每个侧边栏项目
        jq.find("item").each(function(){
            var jItem  = $(this);
            var jIcon  = jItem.children("i");
            var jText  = jItem.children("b");
            var ph     = jItem.attr("ph");
            var noicon = jItem.attr("noicon");
            var o      =  null;

            // 如果没有 ph 那么就标记一下
            if(!ph){
                jItem.addClass("chute-item-delete");
                return;
            }
            // 读取对象
            o = UIBrowser.fetch(ph, true);

            // 如果没有读到对象，标记一下
            if(!o){
                jItem.addClass("chute-item-dead");
            }

            // 如果没有 icon
            if(jIcon.size() == 0){
                jIcon = $('<i class="oicon">');
                var iconHtml;
                // 没有对象，显示一个 icon 的占位
                if(!o){
                    iconHtml = '<i class="oicon oicon_hide></i>';
                }
                // 根据对象生成 icon
                else{
                    iconHtml = Wn.objIconHtml(o);
                }
                jItem.prepend($(iconHtml));
            }

            // 如果没有文字
            if(jText.size() == 0){
                jText = $('<b>').appendTo(jItem);
            }

            // 如果没有文字内容，默认用对象的名称，否则用路径
            if(!$.trim(jText.text())){
                jText.text(o ? Wn.objDisplayName(UI, o.nm) : ph);
            }

        });

        // 移除标记删除的项目
        jq.find(".chute-item-delete").remove();

        // 加入 DOM
        var jNav = UI.arena.find(".chute-nav");
        jNav.empty().append(jq);

        // 高亮项目
        UI.setActived(o, asetup);
        
    },
    //..............................................
    resize : function(){
        var UI = this;
        var jS = UI.arena.children(".chute-scroller");
        var jbN = jS.children(".chute-show-nav");
        var jbO = jS.children(".chute-show-outline");
        var W = UI.arena.width();

        jbN.css("left", W);
        jbO.css("right", W);

    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);



