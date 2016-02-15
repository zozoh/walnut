(function($z){
$z.declare(['zui', 
    'ui/mask/mask', 
    'ui/quartz/edit_quartz'
], 
function(ZUI, MaskUI, QuartzUI){
//==============================================
return ZUI.def("ui.pop.browser", {
    css  : "theme/ui/pop/pop.css",
    //...............................................................
    init : function(options){
        var UIPOP = this;
        var mask_options = _.extend({
            dom  : "ui/pop/pop.html",
            closer: true,
            escape: true,
            width : 800,
            height: 600,
            setup : {
                uiType : "ui/quartz/edit_quartz",
                uiConf : {}
            },
            dom_events : {
                "click .pm-btn-ok" : function(){
                    console.log("OK", this);
                },
                "click .pm-btn-cancel" : function(){
                    console.log("CANCEL", this);
                }
            }
        }, options);

        // 渲染
        new MaskUI(mask_options).render(function(){
            this.body.setData(options.data);
            UIPOP.destroy();
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);