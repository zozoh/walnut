(function($z){
$z.declare([
    'zui', 
    'wn/util',
    'ui/obrowser/support/browser__methods',
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
    __do_notify : function(){
        var UI  = this;
        var opt = UI.opt();
        var context = opt.context || UI;

        // 获得当前选中/激活的对象
        var aObj  = UI.getActived();
        var cObjs = UI.getChecked();

        // 通知脚注
        UI.browser().trigger("browser:info", UI.msg("obrowser.selectNobj", {n:cObjs.length}));

        // 通知选中
        $z.invoke(opt, "on_select", [aObj, cObjs], context);
        UI.browser().trigger("browser:select", aObj, cObjs);
    },
    //..............................................
    update : function(o, callback){
        var UI  = this;
        var opt = UI.opt();
        var uiList = UI.gasket.list;

        // 显示正在加载
        uiList.showLoading();

        // 得到当前所有的子节点
        Wn.getChildren(o, opt.filter, function(objs){
            uiList.hideLoading();
            
            // 显示列表
            uiList.setData(objs);

            // 调用回调
            $z.doCallback(callback, [objs]);
        }, true);
    },
    //..............................................
    getData : function(arg){
        return this.browser.getById($(arg).closest(".wnobj").attr("oid"));
    },
    //..............................................
    // 修改激活项目的名称
    rename : function(){
        this.gasket.list.rename();
    },
    //..............................................
    isActived : function(ele){
        return this.gasket.list.isActived(ele);
    },
    //..............................................
    getActived : function(){
        return this.gasket.list.getActived();
    },
    setActived : function(arg){
        return this.gasket.list.setActived(arg);
    },
    //..............................................
    getChecked : function(){
        return this.gasket.list.getChecked();
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);