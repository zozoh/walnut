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
    <header><%=hmaker.site.newsite_tt%></header>
    <section ui-gasket="form"></section>
    <footer>
        <b><i class="zmdi zmdi-plus"></i>{{hmaker.site.newsite_do}}</b>
        <span class="ing"><i class="fa fa-spinner fa-spin"></i></span>
    </footer>
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
    events : {
        'click footer b[enable]' : function(){
            var UI   = this;
            var opt  = UI.options;
            var data = UI.gasket.form.getData();
            
            var cmdText = 'hmaker newsite "'+data.siteDir+"/"+data.siteName+'" -o';
            if(data.copySite)
                cmdText += ' -copy "id:' + data.copySite + '"';
            
            // 标记正在执行
            UI.arena.attr("ing", "yes").find('>footer>b').removeAttr("enable");

            // 执行
            Wn.exec(cmdText, function(re) {
                var oNewSite = $z.fromJson(re);
                $z.invoke(opt, "done", [oNewSite], UI);
            });

        }
    },
    //...............................................................
    redraw : function(){
        var UI   = this;
        var opt  = UI.options;
        var jB   = UI.arena.find("> footer > b");
        var jMsg = UI.arena.find("> footer > .warn");

        // 显示新建站点选择器
        new FormUI({
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            parent : UI,
            gasketName : "form",
            fitparent  : false,
            uiWidth : "all",
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            on_change : function() {
                var data = this.getData();
                // 看看目录是否存在
                if(data.siteDir && data.siteName) {
                    Wn.execf('obj -noexists null {{siteDir}}/{{siteName}}', data, 
                    function(re){
                        // 判读是佛存在
                        var cmdText = "obj -noexists null '"
                                    + data.siteDir + "/" + data.siteName + "'";
                        Wn.exec(cmdText, function(re){
                            // 已存在
                            if("null" != $.trim(re)) {
                                UI.gasket.form.showPrompt("siteName", "warn");
                                UI.gasket.form.setTip("siteName", UI.msg("hmaker.site.newsite_warn_tip")); 
                                jB.removeAttr("enable");
                            }
                            // 可用
                            else {
                                // 执行创建
                                UI.gasket.form.showPrompt("siteName", "ok");
                                UI.gasket.form.setTip("siteName", UI.msg("hmaker.site.newsite_ok_tip")); 
                                jB.attr("enable", "yes");
                            }
                        });
                    });
                }
                // 数据不足，清空
                else {
                    UI.gasket.form.hidePrompt("siteName");
                    UI.gasket.form.setTip("siteName", null);
                }
            },
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            fields : [{
                key    : "copySite",
                title  : "i18n:hmaker.site.newsite_copy",
                type   : "string",
                dft    : opt.copySite ? opt.oSiteHome.id : null,
                editAs : "droplist",
                uiConf : {
                    exec  : Wn.exec,
                    escapeHtml : false,
                    items : 'obj -match \'d1:"'+opt.oSiteHome.d1+'",tp:"hmaker_site"\' -json -l',
                    icon  : '<i class="fa fa-sitemap"></i>',
                    text  : function(o){
                        return Wn.objDisplayPath(UI, o.ph, 2);
                    },
                    value : function(o) {
                        return o.id;
                    },
                    emptyItem : {
                        text  : UI.msg("hmaker.site.newsite_nocopy"),
                        value : null
                    }
                }
            }, {
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
                        var ph = Wn.getRelativePathToHome(o);
                        if("./" == ph)
                            return "~";
                        return o ? "~/" + Wn.getRelativePathToHome(o) : null;
                    }
                }
            }, {
                key    : "siteName",
                title  : UI.msg("hmaker.site.nm"),
                type   : "string",
                editAs : "input",
                uiConf : {
                    placeholder : UI.msg("hmaker.site.newsite_nm")
                }
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