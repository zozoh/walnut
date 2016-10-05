(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/obrowser/support/browser__methods'
], function(ZUI, Wn, BrowserMethods){
//==============================================
var html = `
<div class="ui-arena obrowser-chute-sidebar" ui-fitparent="yes">
    
</div>`;
//==============================================
return ZUI.def("ui.obrowser_chute_sidebar", {
    dom  : html,
    //..............................................
    init : function(){
        BrowserMethods(this);
    },
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
            var jq = UI.highlightItem(e.currentTarget);
            UI.browser().setData(jq.attr("ph"), jq.attr("editor"));
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
    highlightItem : function(o, asetup){
        var UI = this;
        var jq;
        // 嗯是个对象，那么要自动寻找匹配项 ...
        if(!_.isElement(o) && !$z.isjQuery(o)){
            // console.log("!!!!",o, asetup)
            // 根据路径和编辑器给各个项目打分
            var currentWeight = 0;

            // 查找侧边栏所有项目，看看哪个需要被高亮
            var jItems = UI.arena.find("item");
            for(var i=0;i<jItems.length;i++){
                var jItem = jItems.eq(i);
                var iPh = Wn.absPath(jItem.attr("ph") || "");
                if(iPh.length > o.ph.length)
                    continue;

                var ph1 = o.ph.substring(0, iPh.length);
                var ph2 = o.ph.substring(iPh.length);

                var itemEditor = jItem.attr("editor");

                // 起始分值为 0
                var weight = 0;

                // 如果激活对象没有编辑器设定，但是 item 有，那么无视
                if((!asetup || !asetup.editors || asetup.editors.length == 0) && itemEditor){
                    continue;
                }

                // 项目路径完全包含给定路径，得一分
                if(ph1 == iPh && (!ph2 || /^\//.test(ph2))) {
                    // 完全匹配得10分
                    if(o.ph == iPh) {
                        weight += 10;
                    }
                    // 否则没有编辑器的话，得 1 分
                    else if(!itemEditor){
                        weight += 1;
                    }
                }

                // 匹配编辑器再得10分
                if(asetup && asetup.editors.length>0 && asetup.editors[0] == itemEditor){
                    weight += 10;
                }

                // 如果具备更高的权重，计入候选
                if(weight > currentWeight){
                    currentWeight = weight;
                    jq = jItem;
                }
            }     
        }
        // 给定的就是侧边栏项目，甭找了，直接用吧
        else {
            jq = $(o).closest("item");
        }
        // 修改显示
        UI.arena.find("item").removeClass("chute-actived");
        if(jq){
            jq.addClass("chute-actived");
        }
        return jq;
    },
    //..............................................
    redraw : function() {
        var UI = this;
        UI.refresh(function(){
            UI.defer_report("reload");
        })
        return ["reload"];
    },
    //..............................................
    refresh : function(callback){
        var UI  = this;
        var opt = UI.options;

        // 准备命令
        var cmdText = 'app-sidebar -html';
        if(opt.path){
            cmdText += ' "' + opt.path + '"';
        }

        console.log(cmdText)

        // 读取侧边栏
        Wn.exec(cmdText, function(sideHtml){
            // 处理侧边栏 HTML
            sideHtml = UI.compactHTML(sideHtml || `<div>
                empty sidebar
            </div>`);

            // 预先处理侧边栏的 HTML
            var jq = $('<div class="chute-wrapper">' + sideHtml + '</div>');
            
            // 格式化侧边栏的 DOM
            UI.__format_sideHTML(jq);
            
            // 计入 DOM
            UI.arena.empty().append(jq);

            // 高亮侧边栏
            if(UI.__obj) {
                UI.highlightItem(UI.__obj, UI.__asetup);
            }

            // 调用回调
            $z.doCallback(callback, [], UI);
        });

    },
    //..............................................
    __format_sideHTML : function(jq) {
        var UI = this;

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

        // 整理每个侧边栏项目
        jq.find("item").each(function(){
            var jItem  = $(this);
            var jIcon  = jItem.children("i");
            var jText  = jItem.children("b");
            var ph     = jItem.attr("ph");

            // 如果没有 ph 那么就标记一下
            if(!ph){
                jItem.addClass("chute-item-delete");
                return;
            }

            // 如果没有文字
            if(jText.length == 0){
                jText = $('<b>').appendTo(jItem);
            }

            // 如果没有文字内容，默认用对象的名称，否则用路径
            var text = $.trim(jText.text());
            if(!text){
                jText.text(ph || "no text");
            }
            // 否则国际化
            else {
                jText.text(Wn.objDisplayName(UI, text));
            }


        });

        // 移除标记删除的项目
        jq.find(".chute-item-delete").remove();
    },
    //..............................................
    update : function(o, asetup){
        var UI = this;

        // 记录
        UI.__obj = o;
        UI.__asetup = asetup;

        // 高亮项目
        UI.highlightItem(o, asetup);
        
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);