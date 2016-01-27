(function($z){
$z.declare(['zui', 
    'wn/util', 
    'ui/mask/mask', 
    'ui/support/dom',
    'ui/obrowser/obrowser'
], 
function(ZUI, Wn, MaskUI, DomUI, BrowserUI){
//==============================================
var html = function(){/*
<div class="ui-arena pop" ui-fitparent="yes">
    <div class="pop-title"></div>
    <div class="pop-body" ui-gasket="body"></div>
    <div class="pop-actions">
        <div class="pop-btn" key="ok">{{ok}}</div>
        <div class="pop-btn" key="cancel">{{cancel}}</div>
    </div>
</div>
*/};
//==============================================
return ZUI.def("ui.pop.browser", {
    css  : "theme/ui/pop/pop.css",
    //...............................................................
    init : function(options){
        var UIPOP = this;
        var mask_options = _.extend({
            closer: true,
            escape: true,
            width : 800,
            height: 600,
            exec  : Wn.exec,
            app   : Wn.app(),
            setup : {
                uiType : "ui/support/dom",
                uiConf : {
                    dom  : $z.getFuncBodyAsStr(html.toString()),
                    on_redraw : function(){
                        var UI = this;
                        var jTitle = UI.arena.find(".pop-title");
                        if(!options.title){
                            jTitle.remove();
                        }else{
                            jTitle.text(UI.text(options.title));
                        }
                    },
                    events : {
                        "click .pop-btn" :function(e){
                            var UI   = this;
                            var jBtn = $(e.currentTarget);
                            var key  = jBtn.attr("key");
                            var uiBrowser = UI.subUI("body");
                            // 取消
                            if("cancel" == key){
                                UI.parent.close();
                                $z.invoke(options, "on_cancel", [], options.context || uiBrowser);
                            }
                            // 确认
                            else if("ok" == key){
                                // 获得所有选中的对象
                                var objs = uiBrowser.getChecked();
                                $z.invoke(options, "on_ok", [objs], options.context || uiBrowser);
                                // 关闭
                                UI.parent.close();
                            }
                        }
                    },
                    setup : [{
                        uiType : "ui/obrowser/obrowser",
                        uiConf : _.extend({
                            gasketName: "body",
                            sidebar : true
                        }, options)
                    }],
                    on_resize : function(){
                        var UI = this;
                        var H  = UI.arena.height();
                        var hT = UI.arena.find(".pop-title").outerHeight(true);
                        var hA = UI.arena.find(".pop-actions").outerHeight(true);
                        UI.arena.find(".pop-body").css({
                            "height" : H - hT - hA
                        });
                    }
                }
            }
        }, options);

        // 渲染
        new MaskUI(mask_options).render(function(){
            this.subUI("main/body").setData(options.base);
            UIPOP.destroy();
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);