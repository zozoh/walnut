(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/search2/search',
], function(ZUI, Wn, SearchUI){
//==============================================
var html = function(){/*
<div class="ui-arena test-for-search" ui-fitparent="true" ui-gasket="main"></div>
*/};
//==============================================
return ZUI.def("ui.test_for_search", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    redraw : function() {
        var UI = this;

        // 加载对象编辑器
        new SearchUI({
            parent : UI,
            gasketName : "main",
            data : "obj -match '<%=match%>' -skip {{skip}} -limit {{limit}} -l -json -pager -sort '<%=sort%>'",
            menu : ["create", "refresh", "delete", "edit"],
            filter : {
                form : {
                    fields : [{
                        key : "race",
                        title : "种类",
                        uiType : "@switch",
                        uiConf : {
                            items : [{
                                value : "FILE", text : "文件"
                            }, {
                                value : "DIR",  text : "目录"
                            }]
                        }
                    }, {
                        key : "lm",
                        title : "最后修改日期",
                        uiType : "@input",
                        uiConf : {
                            assist : {
                                icon   : '<i class="zmdi zmdi-calendar-note"></i>',
                                text   : "设置日期范围",
                                uiType : "ui/form/c_date_range"
                            }
                        }
                    }, {
                        key : "number",
                        title : "某个数字范围",
                        uiType : "@input",
                        uiConf : {
                            assist : {uiType : "ui/form/c_number_range"}
                        }
                    }]
                }
            }
        }).render(function(){
            UI.defer_report("main");
        });

        // 返回延迟加载
        return ["main"];
    },
    //..............................................
    update : function(o) {
        this.gasket.main.refresh();
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);