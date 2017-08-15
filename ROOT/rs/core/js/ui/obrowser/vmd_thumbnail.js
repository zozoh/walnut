(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/obrowser/support/browser__methods_list',
    'ui/otiles/otiles'
], function(ZUI, Wn, BrowserMethods, OTilesUI){
//==============================================
var html = function(){/*
<div class="ui-arena obrowser-vmd-thumbnail" ui-fitparent="yes" ui-gasket="list"></div>
*/};
//==============================================
return ZUI.def("ui.obrowser_vmd_thumbnail", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    init : function(){
        BrowserMethods(this);
    },
    //..............................................
    redraw : function(){
        var UI  = this;
        var opt = UI.opt();

        new OTilesUI({
            parent : UI,
            gasketName : "list",
            renameable : opt.renameable,
            evalThumb: opt.thumbnail,
            multi : opt.multi,
            objTagName : opt.objTagName,
            on_open : function(o) {
                if(opt.singleClickOpen){
                    return;
                }
                UI.browser().setData("id:"+o.id);
            },
            on_before_actived : function(o) {
                // 如果支持单击就打开 ...
                if(opt.singleClickOpen){
                    var o = this.getData(e.currentTarget);
                    if(opt.canOpen(o)){
                        UI.browser.setData("id:"+o.id);
                        return false;
                    }
                }
            },
            on_actived : function() {
                UI.__do_notify();
            },
            on_blur : function(jItems, nextObj, nextItem) {
                if(!nextObj)
                    UI.__do_notify();
            },
            on_checked : function() {
                UI.__do_notify();
            },
        }).render(function(){
            UI.defer_report("list");
        });

        return ["list"];
    },
    //..............................................
});
//==================================================
});
})(window.NutzUtil);