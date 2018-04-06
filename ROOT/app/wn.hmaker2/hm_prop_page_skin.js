(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu',
    'ui/form/form',
    'app/wn.hmaker2/support/hm__methods_panel',
], function(ZUI, Wn, MenuUI, FormUI, HmMethods){
//==============================================
var html = `<div class="ui-arena hm-prop-page-skin" ui-fitparent="yes">
    <header ui-gasket="menu"></header>
    <section ui-gasket="form"></section>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_prop_page_skin", {
    dom  : html,
    //...............................................................
    init : function() {
        var UI = HmMethods(this);
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        new MenuUI({
            parent : UI,
            gasketName : "menu",
            setup : [{
                icon : '<i class="fas fa-save"></i>',
                text : 'i18n:hmaker.page.skin_apply',
                handler : function(){
                    UI.__do_apply_skin_var();
                }
            }, {
                type : "separator"
            }, {
                icon : '<i class="zmdi zmdi-plus-circle-o"></i>',
                text : 'i18n:hmaker.page.skin_spread',
                handler : function(){
                    UI.gasket.form.spreadGroup()
                }
            }, {
                icon : '<i class="zmdi zmdi-minus-circle-outline"></i>',
                text : 'i18n:hmaker.page.skin_collapse',
                handler : function(){
                    UI.gasket.form.collapseGroup()
                }
            }, {
                type : "separator"
            }, {
                text : 'i18n:hmaker.page.skin_reset',
                handler : function(){
                    UI.__do_reset_skin_var();
                }
            }]
        }).render(function(){
            UI.defer_report("menu");
        });

        // 返回延迟加载
        return ["menu"];
    },
    //...............................................................
    __do_apply_skin_var : function() {
        var UI = this;

        // 首先呢，显示一下正在处理中
        UI.hmaker().showLoading("hmaker.page.skin_apply_ing");

        // 首先将数据拿出来
        var data = UI.gasket.form.getData();

        // 生成一个最终的 _skin_var.less
        var str = UI.renderSkinVar({
            form : UI._skin_var.form,
            data : data,
        });
        //console.log(str);

        // 保存
        Wn.execf('str > id:{{siteId}}/.skin/_skin_var.less', str, {
            siteId : UI.getHomeObjId()
        }, function(){
            UI.__preload_skin_css(function(){
                UI.fire("change:site:skin");
                UI.hmaker().hideLoading();
            });
        });

        // 通知页面皮肤的修改
    },
    //...............................................................
    __preload_skin_css : function(callback) {
        var UI = this;
        var oHome = UI.getHomeObj();

        // 确保加载皮肤
        var sHref = $z.tmpl('/api/{{d1}}/hmaker/load/{{siteId}}/skin.css')({
            d1     : oHome.d1, 
            siteId : oHome.id,
        });
        $.get(sHref, function(){
            $z.doCallback(callback);
        });
    },
    //...............................................................
    __do_reset_skin_var : function() {
        var UI = this;

        // 确认一下先
        UI.confirm("hmaker.page.skin_reset_tip", function(){
            // 首先呢，显示一下正在处理中
            UI.hmaker().showLoading("hmaker.page.skin_reset_ing");

            // 移除掉当前站点的皮肤设定
            Wn.execf('rm id:{{homeId}}/.skin/_skin_var.less', {
                homeId : UI.getHomeObjId()
            }, function() {
                // 刷新一下，自然会重 copy 回来默认的皮肤变量设定
                UI.refresh(function(){
                    UI.__preload_skin_css(function(){
                        UI.fire("change:site:skin");
                        UI.hmaker().hideLoading();
                    });
                });
            });
        });
    },
    //...............................................................
    __draw_skin_var : function(skinVar, callback) {
        var UI = this;

        // 记录一下
        UI._skin_var = skinVar;

        new FormUI({
            parent : UI,
            gasketName : "form",
            uiWidth : "all",
            fields : skinVar.form.fields,
        }).render(function(){
            this.setData(skinVar.data);
            $z.doCallback(callback);
        });
    },
    //...............................................................
    refresh : function(callback){
        var UI = this;
        console.log("I am page skin refresh");

        UI.showLoading();
        UI.reloadSkinVarSet(function(str){
            var skinVar = UI.parseSkinVar(str);
            console.log(skinVar);
            UI.__draw_skin_var(skinVar, function(){
                UI.hideLoading();
                console.log("all done");
                $z.doCallback(callback);
            });
        });
    },
    //...............................................................
    resize : function(){
        var UI = this;
        var jH = UI.arena.find(">header");
        var jS = UI.arena.find(">section");

        var H = UI.arena.height();
        jS.css("height", H - jH.outerHeight());
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);