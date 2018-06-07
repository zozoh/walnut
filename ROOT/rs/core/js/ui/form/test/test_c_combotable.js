(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/c_combotable'
], function(ZUI, Wn, ComboTableUI){
//==============================================
var html = function(){/*
<div class="ui-arena form-test-con">
    <div class="tcc-btns" style="padding:10px; background:rgba(0,0,0,0.5);">
        <button>getData</button>
    </div>
    <h2>简单输入多个数据</h2>
    <div ui-gasket="com2"></div>
    <h2>一次增加多个数据</h2>
    <div ui-gasket="com0"></div>
    <h2>选择多个地址</h2>
    <div ui-gasket="com1"></div>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_test_combotable", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/form/test/test_c.css",
    //...............................................................
    events : {
        'click .tcc-btns > button' : function(e){
            var jB = $(e.currentTarget);
            var data = this.gasket.com2[jB.text()]();
            console.log(data);
            console.log($z.toJson(data, null, '  '));
        }
    },
    //...............................................................
    redraw : function(){
        var UI = this;

        //...........................................................
        new ComboTableUI({
            parent : UI,
            gasketName : "com0",
            on_change : function(val) {
                console.log("new value::", val);
            },
            fields : [{
                    title  : "ID",
                    key    : "id",
                    hide   : true,
                }, {
                    title  : "名称",
                    key    : "nm",
                    width  : "30%",
                    uiType : "@label",
                // }, {
                //     title  : "价格",
                //     key    : "price",
                //     type   : "int",
                //     dft    : 3,
                //     width  : "20%",
                //     uiType : "@input",
                // }, {
                //     title  : "数量",
                //     key    : "amont",
                //     type   : "int",
                //     dft    : 1,
                //     width  : "20%",
                //     uiType : "@input",
                }, {
                    key   : "the_date",
                    title : "一个日期",
                    hide : true,
                    type  : "datetime",
                    nativeAs : "string",
                    uiType : "@datepicker",
                    uiConf : {}
                }, {
                    key   : "the_range",
                    title : "日期范围",
                    type  : "daterange",
                    nativeAs : "string",
                    uiType : "@datepicker",
                    uiConf : {
                        setup : {
                            mode : "range"
                        }
                    }
                }],
            combo : {
                items : 'obj ~ -match \'race:"DIR", nm:"^{{val}}"\' -limit 10 -json -l -e "^(id|tp|race|nm)$"',
                itemArgs : {val : ".+"},
                icon  : function(o){
                    return Wn.objIconHtml(o);
                },
                text : function(o) {
                    return o.nm;
                },
                filter : function(o, dataList) {
                    for(var i=0; i<dataList.length; i++) {
                        if(o.id == dataList[i].id)
                            return false;
                    }
                    return true;
                }
            },
            getObj : function(val) {
                var nm = $.trim(val);
                if(!nm)
                    return null;
                //return Wn.fetch("~/" + nm, true);
                return [{
                    nm : "AAA"
                }, {
                    nm : "BBB"
                }]
            }
        }).render(function(){
            this.setData([{
                "id":"vnt8bmelr4g9mp5tsus5hpsfts",
                "nm":".hmaker",
                "the_date"  :new Date(),
                "the_range" :null
            }]);

            UI.defer_report("com0");
        });
        //...........................................................
        new ComboTableUI({
            parent : UI,
            gasketName : "com1",
            on_change : function(val) {
                console.log("new value::", val);
            },
            fields : [{
                    key    : "country",
                    title  : "国家",
                    width  : "30%",
                    uiType : "@input",
                    uiConf : {
                        // 变更后，清空后面的选项
                        on_change : function(val){
                            // 得到本行原始数据，并清空 city/zone
                            var row = this.getRowData();
                            row.city = "";
                            row.zone = "";
                            // 更新本行数据
                            this.setRowData(row)
                        },
                        assist : {
                            text   : "选择",
                            uiType : "ui/form/c_list",
                            uiConf : {
                                textAsValue : true,
                                items : ["中国","美国","英国","俄国","法国"]
                            }
                        }
                    }
                }, {
                    key   : "city",
                    title : "城市",
                    uiType : "@input",
                    uiConf : {
                        // 变更后，清空后面的选项
                        on_change : function(val){
                            // 得到本行原始数据，并清空 zone
                            var row = this.getRowData();
                            row.zone = "";
                            // 更新本行数据
                            this.setRowData(row)
                        },
                        assist : {
                            text   : "选择",
                            uiType : "ui/form/c_list",
                            uiConf : {
                                textAsValue : true,
                                items : function(val, callback) {
                                    // 得到本行原始数据
                                    var row = this.parent.getRowData();

                                    // 准备伪造数据
                                    var mockMap = {
                                        "中国" : ["北京","上海","广州","深圳"],
                                        "美国" : ["纽约","华盛顿","洛杉矶","费城"],
                                        "英国" : ["伦敦","利物浦"],
                                        "俄国" : ["莫斯科","列宁格勒"],
                                        "法国" : ["巴黎","马赛"],
                                    };
                                    var list = mockMap[row.country] || [];

                                    // 如果是异步的话，总之调用这个 callback 就好
                                    callback(list);
                                }
                            }
                        }                        
                    }
                }, {
                    key   : "zone",
                    title : "区",
                    uiType : "@input",
                    uiConf : {
                        assist : {
                            text   : "选择",
                            uiType : "ui/form/c_list",
                            uiConf : {
                                textAsValue : true,
                                items : function(val, callback) {
                                    // 得到本行原始数据
                                    var row = this.parent.getRowData();

                                    // 准备伪造数据
                                    var mockMap = {
                                        "北京" : ["西城区","东城区","宣武区","崇文区","海淀区","朝阳区","丰台区","石景山区"],
                                        "上海" : ["浦东新区","黄浦区","卢湾区","徐汇区","长宁","静安区","普陀区","闸北","虹口区","杨浦","宝山区","闵行","嘉定","金山区","松江区","青浦区","南汇","奉贤"],
                                        "广州" : ["越秀区","荔湾区","增城区","海珠区","天河区","白云区","黄埔区","番禺区","花都区","南沙区","从化市","萝岗区"],
                                        "深圳" : ["福田区","罗湖区","南山区","盐田区","宝安区","龙岗区","光明新区","坪山新区","龙华新区","大鹏新区"]
                                    };
                                    var list = mockMap[row.city] || [];

                                    // 如果是异步的话，总之调用这个 callback 就好
                                    callback(list);
                                }
                            }
                        } 
                    }
                }],
            combo : {
                drawOnSetData : false,
                items : ["中国","美国","英国","俄国","法国"],
                icon  : '<i class="fas fa-globe"></i>',
                text : function(o) {
                    return o;
                },
                filter : function(o, dataList) {
                    console.log("filter", o, dataList)
                    for(var i=0; i<dataList.length; i++) {
                        if(o == dataList[i].country)
                            return false;
                    }
                    return true;
                }
            },
            getObj : function(val) {
                var nm = $.trim(val);
                if(!nm)
                    return null;
                return {
                    country : nm
                };
            }
        }).render(function(){
            this.setData([{
                    "country" : "中国",
                    "city"    : "北京",
                    "zone"    : "海淀区",
                }]);

            UI.defer_report("com1");
        });
        //...........................................................
        new ComboTableUI({
            parent : UI,
            gasketName : "com2",
            on_change : function(val) {
                console.log("new value::", val);
            },
            fields : [{
                    title  : "名称",
                    key    : "nm",
                    width  : "30%",
                    uiType : "@input",
                }, {
                    key   : "the_range",
                    title : "日期范围",
                    type  : "daterange",
                    nativeAs : "string",
                    uiType : "@datepicker",
                    uiConf : {
                        setup : {
                            mode : "range"
                        }
                    }
                }],
            adder : {
                icon : '<i class="zmdi zmdi-plus"></i>',
                text : "新增数据",
                data : {}
            }
        }).render(function(){
            this.setData([{
                "nm":"第一期",
                "the_range" :null
            }]);
            UI.defer_report("com2");
        });
        //...........................................................
        return ["com0", "com1", "com2"];
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);