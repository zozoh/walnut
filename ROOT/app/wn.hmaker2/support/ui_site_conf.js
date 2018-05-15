(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods',
    'ui/form/form',
], function(ZUI, Wn, HmMethods, FormUI){
//==============================================
var html = `
<div class="ui-arena hm-ui-site-conf" ui-fitparent="yes" ui-gasket="form"></div>
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
            uiWidth : "all",
            fields  : [{
                key : "id",
                title : "ID",
                editAs : "label"
            }, {
                key : "nm",
                title : UI.msg("hmaker.site.nm"),
                type : "string",
                editAs : "input"
            }, {
                key   : "hm_target_release",
                title : "i18n:hmaker.site.hm_target_release",
                icon  : UI.msg("hmaker.icon.ta_release"),
                type  : "string",
                uiWidth : "auto",
                editAs : "droplist",
                uiConf : {
                    emptyItem : {},
                    items : "www query -ava",
                    text  : function(o){
                        return o.nm;
                    },
                    value : function(o){
                        return "~/" + Wn.getRelativePathToHome(o);
                    },
                }
            }, {
                key   : "hm_site_skin",
                title : UI.msg("hmaker.site.skin"),
                icon  : UI.msg("hmaker.icon.skin"),
                type  : "string",
                uiWidth : "auto",
                editAs : "droplist",
                uiConf : {
                    items : "obj ~/.hmaker/skin/* -json -l",
                    icon  : UI.msg("hmaker.icon.skin"),
                    text  : null,
                    value : function(o){
                        return o.nm;
                    },
                    emptyItem : {}
                }
            }, {
                key   : "www_entry",
                title : "i18n:hmaker.site.www_entry",
                icon  : '<i class="zmdi zmdi-globe"></i>',
                tip   : "i18n:hmaker.site.www_entry_tip",
                type  : "string",
                uiWidth : "all",
            }, {
                key   : "hm_account_set",
                title : UI.msg("hmaker.site.account_set"),
                icon  : '<i class="zmdi zmdi-account"></i>',
                type  : "string",
                uiWidth : "auto",
                editAs : "droplist",
                uiConf : {
                    items : "obj ~/thing -match 'tp:\"thing_set\"' -sort 'th_nm:1' -l",
                    icon  : function(o){
                        return o.icon || '<i class="fa fa-cube"></i>';
                    },
                    text  : function(o){
                        return o.nm;
                    },
                    value : function(o){
                        return o.id;
                    },
                    emptyItem : {}
                }
            }, {
                key   : "hm_role_set",
                title : "i18n:hmaker.site.role_set",
                icon  : '<i class="zmdi zmdi-account-box-o"></i>',
                type  : "string",
                uiWidth : "auto",
                editAs : "droplist",
                uiConf : {
                    items : "obj ~/thing -match 'tp:\"thing_set\"' -sort 'th_nm:1' -l",
                    icon  : function(o){
                        return o.icon || '<i class="fa fa-cube"></i>';
                    },
                    text  : function(o){
                        return o.nm;
                    },
                    value : function(o){
                        return o.id;
                    },
                    emptyItem : {}
                }
            }, {
                key   : "hm_login_entry",
                title : "i18n:hmaker.site.login_entry",
                icon  : '<i class="zmdi zmdi-globe-lock"></i>',
                tip   : "i18n:hmaker.site.login_entry_tip",
                type  : "string",
                uiWidth : "all",
            }, {
                key   : "hm_wxmp",
                title : "i18n:hmaker.site.hm_wxmp",
                icon  : '<i class="fab fa-weixin"></i>',
                type  : "string",
                uiWidth : "auto",
                editAs : "droplist",
                uiConf : {
                    emptyItem : {},
                    items : "obj ~/.weixin -match 'race:\"DIR\"' -json -l",
                    text  : function(o){
                        return o.title || o.nm;
                    },
                    value : function(o){
                        return o.nm;
                    },
                }
            }],
            on_change : function(key, val) {
                var uiForm = this;
                var obj = $z.obj(key, val);
                var oHomeId = this.getData("id");

                // 显示执行中
                uiForm.showPrompt(key, "spinning");

                // 更新配置信息
                Wn.exec("obj id:"+oHomeId+" -u -o", $z.toJson(obj), function(re){
                    // 隐藏提示信息
                    uiForm.hidePrompt();
                    //console.log(re)

                    // 出错
                    if(/^e./.test(re)) {
                        UI.alert(re);
                        return;
                    }

                    // 保存站点对象
                    var obj  = $z.fromJson(re);
                    Wn.saveToCache(obj);

                    // 存储回表单
                    uiForm.setData(obj);

                    // 调用回调
                    $z.invoke(opt, "on_update", [obj], UI);
                });
            }
        }).render(function(){
            UI.defer_report("form");
        });

        // 返回延迟加载
        return ["form"];
    },
    //...............................................................
    setData : function(o) {
        this.gasket.form.setData(o);
    },
    getData : function(o) {
        return this.gasket.form.getData();
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);