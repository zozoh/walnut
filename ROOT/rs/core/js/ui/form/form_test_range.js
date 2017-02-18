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
return ZUI.def("ui.form_test0", {
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
            title : "测试范围选择控件",
            fields : [{
                key   : "date_range_input",
                title : "日期范围输入框",
                type  : "string",
                editAs : "input",
                uiConf : {
                    assist : {
                        icon   : '<i class="zmdi zmdi-calendar-note"></i>',
                        text   : "设置日期范围",
                        uiType : "ui/form/c_date_range"
                    }
                }
            }, {
                key   : "number_range_input",
                title : "数字范围输入框",
                type  : "string",
                editAs : "input",
                uiConf : {
                    unit : "RMB",
                    assist : {uiType : "ui/form/c_number_range"}
                }
            }, {
                key   : "number_range",
                title : "数字范围",
                type  : "string",
                editAs : "number_range",
                uiConf : {}
            }, {
                key   : "date_range",
                title : "日期范围",
                type  : "string",
                editAs : "date_range",
                uiConf : {}
            }]
        }).render(function(){
            this.setData({
                number_range_input : "  ( 1  , 23  ] ",
                number_range       : "  ( 1  , 23  ] ",
                date_range         : " (  2016-01-9 ,  2017-02-21 ]",
            });
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);