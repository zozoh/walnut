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
            data : "obj ~/abc -match '<%=match%>' -skip {{skip}} -limit {{limit}} -l -json -pager -sort '<%=sort%>'",
            menu : ["create", "refresh", "delete", "edit"],
            edtCmdTmpl : {
                "create"  : "obj ~/abc -new '<%=json%>' -o",
                "delete"  : "rm -rf id:{{id}}",
                "edit"    : "obj id:{{id}} -u '<%=json%>' -o"
            },
            filter : {
                keyField : ["nm", "alias"],
                keyFieldIsOr : true,
                assist : {
                    width : 600,
                    autoOpen : false,
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
                            dft : null,
                            uiType : "@number_range",
                            uiConf : {}
                        }]
                    }
                },
                tabs : [{
                    icon : '<i class="zmdi zmdi-file"></i>',
                    text : "文件",
                    // color : "#000",
                    // background : "#F0F",
                    value : {
                        race : "FILE"
                    }
                }, {
                    dchecked : true,
                    icon : '<i class="zmdi zmdi-folder"></i>',
                    text : "目录",
                    // color : "#000",
                    // background : "#FF0",
                    value : {
                        race : "DIR"
                    }
                }, {
                    icon : '<i class="zmdi zmdi-apps"></i>',
                    text : "APP",
                    value : {
                        nm : "app"
                    }
                // }, {
                //     text : "很长很长很长很长的文字",
                //     value : {}
                // }, {
                //     text : "很长很长很长很长的文字",
                //     value : {}
                // }, {
                //     text : "很长很长很长很长的文字",
                //     value : {}
                // }, {
                //     text : "很长很长很长很长的文字",
                //     value : {}
                }],
                tabsPosition : "drop",
                dtabsMulti    : true,
                dtabsKeepChecked : true,
                tabsStatusKey : "test_pet_search_tab",
                dhideInputBox : true,
            }, 
            list : {
                fields : [{
                    key : "nm",
                    title : "名称",
                }, {
                    key : "alias",
                    title : "别名"
                }, {
                    key : "g",
                    title : "所在组",
                    uiType : '@label',
                }, {
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
                    title : "最后修改时间",
                    uiType : '@label',
                    display : function(o) {
                        return $z.parseDate(o.lm).format("yyyy-mm-dd HH:MM:ss");
                    }
                }]
            }
        }).render(function(){
            this.uiFilter.setData({
                "d1" : Wn.app().session.grp
            });
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