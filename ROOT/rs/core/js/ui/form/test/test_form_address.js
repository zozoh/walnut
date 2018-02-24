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
return ZUI.def("ui.test_form_address", {
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
                // 如果城市变动，清空区
                if('city' == key) {
                    this.update("zone", "");
                }
            },
            fields : [{
                key   : "city",
                title : "城市",
                type  : "string",
                editAs : "input",
                uiConf : {
                    assist : {
                        text   : "选择",
                        uiType : "ui/form/c_list",
                        uiConf : {
                            textAsValue : true,
                            items : ["北京","上海","广州","深圳"]
                        }
                    }
                }
            }, {
                key   : "zone",
                title : "区",
                type  : "string",
                editAs : "input",
                uiConf : {
                    assist : {
                        text   : "选择",
                        uiType : "ui/form/c_list",
                        uiConf : {
                            textAsValue : true,
                            items : function(val, callback) {
                                // 得到所在的城市
                                var city = this.parent.parent.getData("city");

                                // 准备伪造数据
                                var mockMap = {
                                    "北京" : ["西城区","东城区","宣武区","崇文区","海淀区","朝阳区","丰台区","石景山区"],
                                    "上海" : ["浦东新区","黄浦区","卢湾区","徐汇区","长宁","静安区","普陀区","闸北","虹口区","杨浦","宝山区","闵行","嘉定","金山区","松江区","青浦区","南汇","奉贤"],
                                    "广州" : ["越秀区","荔湾区","增城区","海珠区","天河区","白云区","黄埔区","番禺区","花都区","南沙区","从化市","萝岗区"],
                                    "深圳" : ["福田区","罗湖区","南山区","盐田区","宝安区","龙岗区","光明新区","坪山新区","龙华新区","大鹏新区"]
                                };
                                var list = mockMap[city] || [];

                                // 如果是异步的话，总之调用这个 callback 就好
                                callback(list);
                            }
                        }
                    }
                }
            }]
        }).render(function(){
            
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);