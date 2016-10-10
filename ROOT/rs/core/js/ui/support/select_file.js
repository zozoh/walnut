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
<div class="ui-arena select-file" ui-fitparent="yes">
    <header>
        <ul>
            <li tp="browse">{{support.select_file.browse}}</li>
            <li tp="upload">{{support.select_file.upload}}</li>
        </ul>
    </header>
    <section tp="browse" ui-gasket="browse"></section>
    <section tp="upload" ui-gasket="upload"></section>
    <footer><div class="sf-shelf"></div></footer>
</div>
`;
//==============================================
return ZUI.def("ui.support.select_file", {
    dom  : html,
    css  : "theme/ui/support/select_file.css",
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
        "click header li"  : function(e){
            var UI = this;
            var tp = $(e.currentTarget).attr("tp");
            UI.__swich_tab(tp);
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
            canOpen : function(o) {
                return true;
            },
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
        })).render(function(){
            UI.defer_report("uploader");
        });

        // 默认显示浏览器
        UI.__swich_tab("browse");

        // 延迟加载
        return ["browser", "uploader"];
    },
    //...............................................................
    __swich_tab : function(tp){
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
    resize : function() {
        var UI = this;
        var jH = UI.arena.children("header");
        var jS = UI.arena.children("section");
        var jF = UI.arena.children("footer");
        jS.css("top", jH.outerHeight(true));
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);