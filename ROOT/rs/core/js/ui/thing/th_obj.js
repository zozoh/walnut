(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/thing/support/th_methods',
    'ui/thing/th_obj_index',
    'ui/thing/th_obj_data',
], function(ZUI, Wn, ThMethods, ThObjIndexUI, ThObjDataUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-obj" ui-fitparent="true">
    <div class="th-obj-index-con" ui-gasket="index"></div>
    <div class="th-obj-data-con"  ui-gasket="data"></div>
</div>
*/};
//==============================================
return ZUI.def("ui.th_obj", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    i18n : "ui/thing/i18n/{{lang}}.js",
    //..............................................
    init : function(opt){
        ThMethods(this);
    },
    //..............................................
    redraw : function(){
        var UI  = this;
        var bus = UI.bus();
        var conf = UI.getBusConf();

        // 准备返回延迟加载
        var re = [];
        
        // 加载索引界面
        if(conf.meta || conf.detail) {
            re.push("index");
            new ThObjIndexUI({
                parent : UI,
                gasketName : "index",
                bus : bus
            }).render(function(){
                UI.defer_report("index");
            });
        }
        // 移除包裹
        else {
            UI.arena.find(">.th-obj-index-con").remove();
        }

        // 加载多媒体和附件界面
        if(conf.media || conf.attachment) {
            re.push("data");
            new ThObjDataUI({
                parent : UI,
                gasketName : "data",
                bus : bus
            }).render(function(){
                UI.defer_report("data");
            });
        }
        // 移除包裹
        else {
            UI.arena.find(">.th-obj-data-con").remove();
        }

        // 返回延迟加载
        return re;
    },
    //..............................................
    _fill_context : function(uiSet) {
        var UI = this;
        uiSet.obj = this;
        if(UI.gasket.index)
            UI.gasket.index._fill_context(uiSet);
        if(UI.gasket.data)
            UI.gasket.data._fill_context(uiSet);
    },
    //..............................................
    update : function(o, callback, force) {
        var UI  = this;
        //console.log("update", o);

        if(!force && o && UI.__OBJ && UI.__OBJ.id == o.id) {
            $z.doCallback(callback, [o], UI);
            return;
        }

        // 记录
        UI.__OBJ = o;

        // 准备延迟加载项目
        var keys = [];
        if(UI.gasket.index)
            keys.push("update_index");
        if(UI.gasket.data)
            keys.push("update_data");

        // 注册延迟加载函数
        UI.defer(keys, function(){
            $z.doCallback(callback, [o], UI);
        });
        
        // 更新索引界面
        if(UI.gasket.index)
            UI.gasket.index.update(o, function(){
                UI.defer_report("update_index");
            });

        // 更新数据界面
        if(UI.gasket.data)
            UI.gasket.data.update(o, function(){
                UI.defer_report("update_data");
            });
    },
    
    //..............................................
});
//==================================================
});
})(window.NutzUtil);