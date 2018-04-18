(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/obrowser/support/browser__methods'
], function(ZUI, Wn, BrowserMethods){
//==============================================
var html = `
<div class="ui-arena obrowser-chute ui-clr">
    <div class="chute-nav" ui-gasket="sidebar"></div>
    <div class="chute-btn">
        <b><i class="fa fa-angle-double-left"></i></b>
    </div>
</div>`;
//==============================================
return ZUI.def("ui.obrowser_chute", {
    dom  : html,
    //..............................................
    init : function(){
        BrowserMethods(this);
    },
    //..............................................
    events : {
        "click .chute-btn b" : function(e) {
            this.browser().toogleChuteMode();
        }
    },
    //..............................................
    redraw : function() {
        var UI = this;

        UI.refresh(function(){
            UI.defer_report("sidebar");
        });
        return ["sidebar"];
    },
    //..............................................
    refresh : function(callback) {
        var UI  = this;
        var opt = UI.browser().options.sidebar;

        // 显示加载中
        UI.showLoading();

        // 已经有了 UI 那么就更新
        if(UI.gasket.sidebar) {
            UI.gasket.sidebar.refresh(function(){
                UI.hideLoading();
                UI.gasket.sidebar.update(UI.__obj, UI.__asetup);
                $z.doCallback(callback, [this], UI);
            });
        }
        // 否则重新建立
        else {
            seajs.use(opt.uiType, function(SubUI){
                new SubUI(_.extend({}, opt.uiConf, {
                    parent : UI,
                    gasketName : "sidebar"
                })).render(function(){
                    UI.hideLoading();
                    this.update(UI.__obj, UI.__asetup);
                    $z.doCallback(callback, [this], UI);
                });
            });
        }
    },
    //..............................................
    update : function(o, asetup){
        this.__obj    = o;
        this.__asetup = asetup;
        this.gasket.sidebar.update(o, asetup);        
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



