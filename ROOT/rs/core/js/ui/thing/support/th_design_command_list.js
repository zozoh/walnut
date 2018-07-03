(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu',
    'ui/list/list'
], function(ZUI, Wn, MenuUI, ListUI){
//==============================================
var html = function(){/*
<div class="ui-arena thdc-list" ui-fitparent="yes">
    I am thing design list
</div>
*/};
//==============================================
return ZUI.def("app.wn.thdesign_commands_list", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        
    },
    //...............................................................
    redraw : function() {
        var UI  = this;       
        

        
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