(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form'
], function(ZUI, Wn, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena" ui-fitparent="yes" ui-gasket="form"></div>
*/};
//===================================================================
return ZUI.def("ui.test_form_textarea_obj", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
    },
    //...............................................................
    update : function(o){
        var UI = this;
        new FormUI({
            parent : UI,
            gasketName : "form",
            title : "联动选择省市的 combo 组件",
            on_change: function(key, val) {
                console.log(key, val);
                console.log(this.getData());
            },
            fields : [{
                key   : "the_list",
                title : "测试对象列表",
                type  : "object",
                uiWidth : "all",
                editAs : "text",
                uiConf : {
                    asJson : true,
                    height : 300
                }
            }]
        }).render(function(){
            this.setData({
                the_list : [{x:100,y:99}, {x:88,y:45}]
            });
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);