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
                assist : {
                    width : 600,
                    form : {
                        fields : [{
                            key : "race",
                            title : "种类",
                            dft : null,
                            uiType : "@switch",
                            uiConf : {
                                singleKeepOne : false,
                                items : [{
                                    value : "FILE", text : "文件"
                                }, {
                                    value : "DIR",  text : "目录"
                                }]
                            }
                        }, {
                            key : "lm",
                            title : "最后修改日期",
                            dft : null,
                            uiWidth : 300,
                            uiType : "@date_range",
                            uiConf : {
                                formatData : function(str){
                                    if(/^[mM][sS]/.test(str))
                                        return str;
                                    return str ? "MS"+str : null;
                                },
                                parseData : function(str){
                                    if(/^[mM][sS]/.test(str))
                                        return str.substring(2);
                                    return str;
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