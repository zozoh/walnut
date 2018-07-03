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
<div class="ui-arena thdc-list" ui-fitparent="yes">
    I am thing design list
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