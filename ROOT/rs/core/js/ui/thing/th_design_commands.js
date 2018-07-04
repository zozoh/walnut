(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/list/list',
    'ui/thing/support/th_design_command_list'
], function(ZUI, Wn, ListUI, ThDesignCommandListUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-design-commands" ui-fitparent="yes">
    <section class="thdc-actions">
        <div class="thdca-list" ui-gasket="list"></div>
        <div class="thdca-opt">
            <ul>
                <li><a m="add">添加函数集</a></li>
                <li><a m="add">查看已加载函数</a></li>
            </ul>
        </div>
    </section>
    <section class="thdc-search" ui-gasket="search"></section>
    <section class="thdc-obj" ui-gasket="obj"></section>
</div>
*/};
//==============================================
return ZUI.def("app.wn.thdesign_commands", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        
    },
    //...............................................................
    redraw : function() {
        var UI  = this;       
        
        // 列表
        new ListUI({
            parent : UI,
            gasketName : "list",

        }).render(function(){
            UI.defer_report("list");
        });

        // 搜索命令菜单
        new ThDesignCommandListUI({
            parent : UI,
            gasketName : "search",
        }).render(function(){
            UI.defer_report("search");
        });

        // 对象命令菜单
        new ThDesignCommandListUI({
            parent : UI,
            gasketName : "obj",
        }).render(function(){
            UI.defer_report("obj");
        });
        
        // 返回延迟加载
        return ["list", "search", "obj"];
    },
    //...............................................................
    getData : function() {
        var UI = this;
        
    },
    //...............................................................
    setData : function(thConf) {
        var UI = this;

        thConf = thConf || {};

        // 确保是对象
        if(_.isString(thConf))
            thConf = $z.fromJson(thConf);

        // 设置扩展命令
        var extcmds = thConf.extendCommand || {};

        // 设置数据
        UI.gasket.list.setData(extcmds.actions);
        UI.gasket.search.setData(extcmds.search);
        UI.gasket.obj.setData(extcmds.obj);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);