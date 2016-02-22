(function($z){
$z.declare(['zui', 
    'wn/util', 
    'ui/mask/mask', 
    'ui/obrowser/obrowser'
], 
function(ZUI, Wn, MaskUI, BrowserUI){
//==============================================
return ZUI.def("ui.pop.browser", {
    css  : "theme/ui/pop/pop.css",
    //...............................................................
    redraw : function(){
        var UIPOP = this;
        var opt   = this.options;
        var mask_options = _.extend({
            dom  : "ui/pop/pop.html",
            closer: true,
            escape: true,
            width : 800,
            height: 600,
            exec  : Wn.exec,
            app   : Wn.app(),
            setup : {
                uiType : "ui/obrowser/obrowser",
                uiConf : _.extend({
                    gasketName: "body",
                    sidebar : false
                }, opt)
            },
            dom_events : {
                "click .pm-btn-ok" : function(){
                    var UI   = ZUI(this);
                    var objs = UI.body.getChecked();
                    var context = UI.options.context || UI.body;
                    $z.invoke(UI.options, "on_ok", [objs], context);
                    UI.close();
                },
                "click .pm-btn-cancel" : function(){
                    var UI = ZUI(this);
                    var context = UI.options.context || UI.body;
                    $z.invoke(UI.options, "on_cancel", [], context);
                    UI.close();
                }
            }
        }, opt);

        // 渲染
        new MaskUI(mask_options).render(function(){
            if(opt.title)
                this.arena.find(".pm-title").html(this.text(opt.title));
            else
                this.arena.find(".pm-title").remove();
            this.body.setData(opt.base);
            UIPOP.destroy();
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);