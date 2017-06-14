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
            this.clickItem(e.currentTarget);
        },
        "click .chute-show-nav" : function(e){
            this.hideOutline();
        },
        "click .chute-show-outline" : function(e){
            this.showOutline();
        },
        // 禁止侧边栏默认的链接点击行为
        "click a[href]" : function(e) {
            e.preventDefault();
        }
    },
    //..............................................
    redraw : function() {
        var UI = this;
        UI.refresh(function(){
            UI.defer_report("refresh");
        })
        return ["refresh"];
    },
    //..............................................
    update : function(o, asetup){
        var UI = this;

        // 记录
        UI.__obj = o;
        UI.__asetup = asetup;

        // 高亮项目
        if(o) {
            UI.highlightItem(o, asetup);
        }
        // 取消高亮
        else {
            UI.disHighlightItem();
        } 
    },
    //..............................................
    refresh : function(callback){
        var UI  = this;
        var opt = UI.options;

        // 准备命令
        var cmdText = 'app sidebar -html';
        if(opt.path){
            cmdText += ' "' + opt.path + '"';
        }

        //console.log(cmdText)

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
    // 根据传入的对象，自动高亮侧边栏的项目
    highlightItem : function(arg, arg1){
        // 得到目标项
        var jItem = this.$item(arg, arg1); 
        //console.log("jItem.size() :-> ", jItem.size())
        
        // 修改显示
        this.disHighlightItem();
        
        // 设置高亮并返回
        return jItem.addClass("chute-actived");
    },
    //..............................................
    disHighlightItem : function(){
        this.arena.find("item").removeClass("chute-actived");
    },
    //...................z...........................
    $nextItem : function(arg, arg1) {
        var jItem = this.$item(arg, arg1);
        if(jItem.length > 0) {
            if(jItem.next("item").length > 0)
                return jItem.next("item");
            if(jItem.prev("item").length > 0)
                return jItem.prev("item");
        }
        return null;
    },
    //...................z...........................
    $item : function(arg, arg1) {
        var UI = this;
        // 默认用激活项
        if(_.isUndefined(arg)){
            return UI.arena.find('item.chute-actived');
        }
        // 如果是 WnObj ... 那么查找
        if(_.isObject(arg) && arg.ph) {
            return UI.__find_item_by_obj(arg, arg1) || $([]);
        }
        // 分别指定了路径和编辑器
        if(_.isString(arg) && _.isString(arg1)) {
            return UI.arena.find('item[ph="'+arg+'"][editor="'+arg1+'"]').first();
        }
        // 本身就是 dom
        if(_.isElement(arg) || $z.isjQuery(arg)){
            return $(arg).closest("item");
        }
        // 数字
        if(_.isNumber(arg)){
            return UI.arena.find("item:eq("+arg+")");
        }
        // 在一个路径里指定了路径和编辑器
        // 如果是字符串 ...
        var m = /^([^]#)(#(.+))?$/.exec(arg);
        if(m){
            var ph     = $.trim(m[1]);
            var editor = $.trim(m[3]);
            var selector = 'item';
            if(ph) {
                selector += '[ph="'+ph+'"]';
            }
            if(selector) {
                selector += '[editor="'+editor+'"]';
            }
            return UI.arena.find(selector).first();
        }
        // 靠不晓得了
        else {
            throw "unknowns $item selector: " + arg;
        }
    },
    //...................z...........................
    clickItem : function(arg, arg1) {
        var jItem = this.highlightItem(arg, arg1);
        var iDate = this.getItemDate(jItem);
        if(iDate) {
            this.browser()
                .setData(iDate.ph, iDate.editor);
        }
    },
    //...................z...........................
    getItemDate : function(jItem, dft) {
        if(jItem) {
            return {
                ph : jItem.attr("ph"),
                editor : jItem.attr("editor"),
                icon : jItem.find('i'),
                text : jItem.find('b').text(),
            };
        }
        return dft;
    },
    //...................z...........................
    getNextItem : function(dft) {
        var jNext = this.$nextItem();
        return this.getItemDate(jNext, dft);
    },
    //...................z...........................
    __find_item_by_obj : function(o, asetup) {
        var UI = this;
        var jq = null;

        //console.log(o, asetup)

        // 那么要自动寻找匹配项 ...
        // 根据路径和编辑器给各个项目打分
        var currentWeight = 0;

        // 查找侧边栏所有项目，看看哪个需要被高亮
        var jItems = UI.arena.find("item");
        for(var i=0; i<jItems.length; i++){
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

                // 匹配编辑器再得10分
                if(asetup 
                    && asetup.editors.length>0 
                    && asetup.editors[0] == itemEditor){
                    weight += 10;
                }
            }

            // 如果具备更高的权重，计入候选
            if(weight > currentWeight){
                currentWeight = weight;
                jq = jItem;
            }
        }

        // 返回
        return jq;  
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
            var jText  = jItem.children("a");
            var oid    = jItem.attr("oid");
            var ph     = jItem.attr("ph");
            var editor = jItem.attr("editor");

            // 如果没有 ph 那么就标记一下
            if(!ph){
                jItem.addClass("chute-item-delete");
                return;
            }

            // 如果没有文字
            if(jText.length == 0){
                jText = $('<a>').appendTo(jItem);
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

            // 最后补充上 Href
            var href = "/a/open/" + window.wn_browser_appName + "?ph=" + (oid ? "id:"+oid : ph);
            if(editor)
                href += '#' + editor;
            jText.prop("href", href);

        });

        // 移除标记删除的项目
        jq.find(".chute-item-delete").remove();
    },
    //..............................................
});
//==================================================
});
})(window.NutzUtil);