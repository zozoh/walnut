(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form'
], function(ZUI, Wn, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena" ui-fitparent="yes">
    <div style="position:absolute; padding:6px;">
    <button class="get_data">GET DATA</button>
    </div>
    <div class="myform" ui-gasket="myform" style="width:100%; height:100%; "></div>
</div>
*/};
//===================================================================
return ZUI.def("ui.test_form2", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "click .get_data" : function(){
            console.log(this.subUI("myform").getData());
        },
    },
    //...............................................................
    fields_A : function(){
        return [{
                key   : "id",
                icon  : '<i class="fa fa-random"></i>',
                title : "ID",
                type  : "string"
            },{
                key   : "birthday",
                title : "生日",
                type  : "datetime",
                dft   : new Date(),
                editAs: "datepicker"
            },{
                key :"exp_start",
                title :"发货时间",
                uiWidth : 200,
                type :"datetime",
                dft : new Date(),
                nativeAs : "string",
                format : "yyyy-mm-dd",
                editAs :"datepicker"
            }, {
                key :"exp_id",
                title :"快递单号",
                dft : null,
                type :"string",
                editAs :"input"
            },{
                key   : "drange",
                title : "选一个日期范围",
                type  : "daterange",
                dft   : ["2016-02-02", new Date()],
                nativeAs : "string",
                format : "yyyy-mm-dd",
                editAs: "datepicker",
                uiConf: {setup : {mode : "range"}} 
            },{
                key   : "x",
                title : "X轴",
                tip   : "x 轴坐标",
                type  : "int",
                editAs : "input",
                required : true
            },{
                key   : "y",
                title : "Y轴",
                tip   : "y 轴坐标",
                type  : "int",
                editAs : "input",
                required : true
            },{
                key   : "favColor",
                title : "最喜爱的颜色",
                tip   : "代表了你的性格哟",
                type  : "object",
                editAs : "color"
            },{
                key   : "background",
                title : "背景颜色",
                tip   : "代表了你的性格哟",
                type  : "string",
                editAs : "background"
            },{
                key   : "name",
                title : "名称",
                tip   : "请输入正确的名称",
                type  : "string",
                editAs : "input"
            },{
                key   : "sex",
                icon  : '<i class="fa fa-ship"></i>',
                title : "性别",
                tip   : "请输入你的性别",
                required : true,
                type  : "string",
                editAs : "switch",
                uiConf : {items : [{text : "男", val:"m"},{text : "女", val:"f"}]}
            },{
                key   : "live",
                title : "仍旧存活",
                tip   : "量子态不算存活，算死翘翘",
                type  : "boolean",
                editAs : "switch"
            }];
    },
    //...............................................................
    fields_B : function(){
        return [{
                key   : "age",
                title : "年龄",
                type  : "int",
                editAs : "input"
            },{
                key   : "race",
                title : "种族",
                type  : "string",
                editAs : "input"
            },{
                key   : "usenm",
                title : "曾用名",
                type  : "string",
                span  : 2,
                editAs : "input"
            },{
                key   : "skill",
                title : "技能",
                type  : "string",
                editAs : "input" 
            }];
    },
    //...............................................................
    update : function(o){
        var UI = this;
        new FormUI({
            parent : UI,
            gasketName : "myform",
            on_change : function(key, val){
                console.log("form change:", key, val);
            },
            title : "普通测试表单",
            uiWidth : "auto",
            fields : [{
                icon : '<i class="fa fa-rss"></i>',
                text : '四个输入框',
                cols : 3,
                fields : UI.fields_B()
            },{
                icon : '<i class="fa fa-rss"></i>',
                text : '各种属性',
                cols : 2,
                fields : UI.fields_A()
            }]
        }).render(function(){
            this.setData({
                id: o.id,
                x:100, y:80, 
                birthday : "1977-09-21",
                name:'I am zozoh', 
                favColor : null,
                sex:"m",
                live:true,
                //myphoto : {fid:'4thoboi83khmdqmqqvf5arogki'}
            });
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);