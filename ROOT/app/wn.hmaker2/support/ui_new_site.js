(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods',
    'ui/form/form',
], function(ZUI, Wn, HmMethods, FormUI){
//==============================================
var html = `
<div class="ui-arena hm-ui-new-site">
    <header></header>
    <section ui-gasket="form"></section>
    <footer></footer>
</div>
`;
//==============================================
return ZUI.def("app.wn.hmaker_ui_new_site", {
    dom  : html,
    i18n : "app/wn.hmaker2/i18n/{{lang}}.js",
    //...............................................................
    init : function() {
        var UI = HmMethods(this);
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 显示新建站点选择器
        new FormUI({
            parent : UI,
            gasketName : "form",
            fitparent  : false,
            uiWidth : "all",
            dfields : [{
                key    : "siteDir",
                title  : "i18n:hmaker.site.new_site_dir",
                type   : "string",
                dft    : "~",
                uiType : "ui/picker/opicker",
                uiConf : {
                    showPath  : {offset:2},
                    clearable : false,
                    setup : {
                        filter    : function(o) {
                            return 'DIR' == o.race && 'hmaker_site' != o.tp;
                        },
                        //objTagName : 'SPAN',
                    },
                    parseData : function(str){
                        var m = /id:(\w+)/.exec(str);
                        if(m)
                            return Wn.getById(m[1], true);
                        if(str)
                            return Wn.fetch(str, true);
                        return null;
                    },
                    formatData : function(o){
                        return o ? "~/" + Wn.getRelativePathToHome(o) : null;
                    }
                }
            }, {
                key    : "siteName",
                title  : UI.msg("hmaker.site.nm"),
                type   : "string",
                dft    : UI.msg("hmaker.site.newsite"),
                editAs : "input"
            }]
        }).render(function(){
            this.setData({});
            UI.defer_report("form");
        });

        // 返回延迟加载
        return ["form"];
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);