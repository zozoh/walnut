(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/obrowser/obrowser',
    'ui/upload/upload',
], 
function(ZUI, Wn, BrowserUI, UploadUI){
//==============================================
var html = `
<div class="ui-code-template">
    <div code-id="ssItem" class="ss-item">
        <div class="ssi-thumb"></div>
        <div class="ssi-name"><a target="_blank"></a></div>
        <div class="ssi-remove"><i class="zmdi zmdi-close"></i></div>
    </div>
</div>
<div class="ui-arena select-file" ui-fitparent="yes">
    <header>
        <ul>
            <li tp="browse">{{support.select_file.browse}}</li>
            <li tp="upload">{{support.select_file.upload}}</li>
        </ul>
    </header>
    <section tp="browse" ui-gasket="browse"></section>
    <section tp="upload" ui-gasket="upload"></section>
    <footer><div class="sf-shelf">{{support.select_file.shelf_msg_empty}}</div></footer>
    <div class="sf-selected-con"><div>
        <h5>{{support.select_file.selected_tt}}</h5>
        <div class="sf-selected-close">{{close}}</div>
        <div class="sf-selected"><div class="empty">{{support.select_file.shelf_msg_empty}}</div></div>
    </div></div>
</div>
`;
//==============================================
return ZUI.def("ui.support.select_file", {
    dom  : html,
    css  : "ui/support/theme/select_file-{{theme}}.css",
    i18n : "ui/support/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt) {
        $z.setUndefined(opt, "multi", true);
        $z.setUndefined(opt, "base", "~");
        $z.setUndefined(opt, "browser",  {});
        $z.setUndefined(opt, "uploader", {});
    },
    //...............................................................
    events : {
        "click header li" : function(e){
            var UI = this;
            var tp = $(e.currentTarget).attr("tp");
            UI.__switch_tab(tp);
        },
        "click .sf-shelf[has-items]" : function(){
            this.arena.find(".sf-selected-con").attr("show", "yes");
        },
        "click .sf-selected-close" : function(){
            this.arena.find(".sf-selected-con").removeAttr("show");
        },
        "click .ssi-remove" : function(e){
            var UI = this;
            var jItem = $(e.currentTarget).closest(".ss-item");
            var index = jItem.prevAll().length;

            // 更新数据
            UI.__objList = $z.removeItemAt(UI.__objList, index);

            // 移除
            $z.removeIt(jItem);

            // 更新 shelf 显示
            UI.__update_shelf();
        }
    },
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        // 浏览器控件
        new BrowserUI(_.extend({
            lastObjId  : "com_select_file",
            sidebar    : false,
            footbar    : false,
            history    : false,
            multi      : opt.multi ? true : false,
            objTagName : 'SPAN',
            canOpen : function(o) {
                return 'DIR' == o.race;
            },
            on_select : function(aObj, cObjs){
                UI.__set_shelf(cObjs);
            }
        }, opt.browser, {
            parent     : UI,
            gasketName : "browse",
        })).render(function(){
            this.setData(function(){
                UI.defer_report("browser");
            });
        });

        // 上传控件
        new UploadUI(_.extend({
            multi      : opt.multi ? true : false,
        }, opt.uploader, {
            parent     : UI,
            gasketName : "upload",
            finish : function(objList) {
                if(!_.isArray(objList))
                    objList = [objList];
                UI.__set_shelf(objList);
            }
        })).render(function(){
            UI.defer_report("uploader");
        });

        // 默认显示浏览器
        //UI.__switch_tab("browse");
        UI.__switch_tab("upload");

        // 延迟加载
        return ["browser", "uploader"];
    },
    //...............................................................
    __set_shelf : function(objList) {
        var UI  = this;
        var opt = UI.options;

        // 记录数据，仅仅记录文件
        UI.__objList = [];
        for(var o of objList)
            if('DIR' != o.race)
                UI.__objList.push(o);

        // 更新脚注
        UI.__update_shelf();

        // 重新绘制选中项目详情区
        var jSS    = UI.arena.find(".sf-selected").empty();

        // 有选中的项目
        if(UI.__objList.length > 0) {
            for(var o of UI.__objList) {
                var jItem = UI.ccode("ssItem");
                jItem.children(".ssi-thumb").css("background-image", 'url(/o/thumbnail/id:'+o.id+'?sh=32)').attr({
                    "jpg" : o.thumb ? "yes" : null
                });
                jItem.find(">.ssi-name>a").text(Wn.objDisplayName(UI, o.nm,0)).attr({
                    "href" : "/a/open/"+(window.wn_browser_appName||"wn.browser")+"?ph=id:"+o.id
                });
                jSS.append(jItem);
            }
        }
        // 没有的不显示链接
        else{
            jSS.html($('<div class="empty">').text(UI.msg("support.select_file.shelf_msg_empty")));
        }
        
        // 最后重新计算尺寸
        UI.resize();
    },
    //...............................................................
    __update_shelf : function() {
        var UI  = this;

        // 重新绘制
        var jShelf = UI.arena.find("> footer > .sf-shelf");

        // 有选中的项目
        if(UI.__objList.length > 0) {
            jShelf.text(UI.msg("support.select_file.shelf_msg", {
                n : UI.__objList.length
            })).attr("has-items", "yes");
        }
        // 没有的不显示链接
        else{
            jShelf.text(UI.msg("support.select_file.shelf_msg_empty")).removeAttr("has-items");
        }
    },
    //...............................................................
    __switch_tab : function(tp){
        var UI  = this;
        var opt = UI.options;

        // 切换标签显示
        var jUl = UI.arena.find("header>ul");
        jUl.find('.highlight').removeClass("highlight");
        jUl.find('[tp="'+tp+'"]').addClass("highlight");

        // 更换主体区域
        UI.arena.children('section').hide()
            .filter('[tp="'+tp+'"]').show();

        // 重新刷新布局
        UI.resize(true);
    },
    //...............................................................
    getData : function(){
        var UI  = this;
        var opt = UI.options;

        if(opt.multi) {
            return UI.__objList || [];
        }

        return UI.__objList && UI.__objList.length > 0 
                    ? UI.__objList[0]
                    : null;
    },
    //...............................................................
    resize : function() {
        var UI = this;
        var jH = UI.arena.children("header");
        var jS = UI.arena.children("section");
        var jF = UI.arena.children("footer");
        jS.css("top", jH.outerHeight(true));
        UI.arena.find(".sf-selected").css({
            "max-height": jS.outerHeight(),
            "max-width" : jS.width()
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);