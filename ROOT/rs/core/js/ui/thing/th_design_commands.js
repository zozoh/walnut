(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/c_icon',
    'ui/form/c_name',
    'ui/form/form',
    'ui/menu/menu',
    'ui/list/list',
    'ui/support/dom',
], function(ZUI, Wn, CIconUI, CNameUI, FormUI, MenuUI, ListUI, DomUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-design-commands" ui-fitparent="yes">
    <section class="thdc-actions">
        <div class="thdca-list"></div>
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
        

        // 返回延迟加载
        return ["form"];
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

        
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);