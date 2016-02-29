(function($z){
$z.declare(['zui', 
    'ui/mask/mask', 
    'ui/quartz/edit_quartz'
], 
function(ZUI, MaskUI, QuartzUI){
//==============================================
return ZUI.def("ui.pop.quartz", {
    css  : "theme/ui/pop/pop.css",
    //...............................................................
    redraw : function(){
        var UIPOP = this;
        var opt   = this.options;
        var mask_options = _.extend({
            dom  : "ui/pop/pop.html",
            i18n : "ui/quartz/i18n/{{lang}}.js",
            closer: true,
            escape: true,
            width : 430,
            height: 500,
            setup : {
                uiType : "ui/quartz/edit_quartz",
                uiConf : {}
            },
            dom_events : {
                "click .pm-btn-ok" : function(){
                    var UI = ZUI(this);
                    var qz = UI.body.getData();
                    var context = UI.options.context || UI.body;
                    $z.invoke(UI.options, "on_ok", [qz], context);
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
            this.arena.find(".pm-title").html(this.text(opt.title || "i18n:quartz.title"));
            this.body.setData(opt.data);
            UIPOP.destroy();
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);