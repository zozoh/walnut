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
    | <button class="t0">T0</button>
      <button class="t1">T1</button>
      <button class="t2">T2</button>
    </div>
    <div class="myform" ui-gasket="myform"
    style="width:100%; height:100%; padding-top:50px; background: #FFF; margin:10px;"></div>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_test0", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "click .get_data" : function(){
            console.log(this.subUI("myform").getData());
        },
        "click .t0" : function(){
            this.do_t0();
        },
        "click .t1" : function(){
            this.do_t1();
        },
        "click .t2" : function(){
            this.do_t2();
        }
    },
    //...............................................................
    getObj : function(){
        return {
            id: this.$el.attr("obj-id"),
            x:100, y:80, 
            birthday : "1977-09-21",
            name:'I am zozoh', 
            sex:"m",
            live:true,
            //myphoto : {fid:'4thoboi83khmdqmqqvf5arogki'}
        };
    },
    //...............................................................
    fields_A : function(){
        return [{
                key   : "id",
                icon  : '<i class="fa fa-random"></i>',
                title : "i18n:ID",
                type  : "string"
            },{
                key   : "birthday",
                title : "生日",
                type  : "datetime",
                dft   : "1977-09-21",
                editAs: "datepicker"
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
                title : "i18n:X轴",
                tip   : "x 轴坐标",
                type  : "int",
                editAs : "input",
                required : true
            },{
                key   : "y",
                title : "i18n:Y轴",
                tip   : "y 轴坐标",
                type  : "int",
                editAs : "input",
                required : true
            },{
                key   : "name",
                title : "i18n:名称",
                tip   : "请输入正确的名称",
                type  : "string",
                editAs : "input"
            },{
                key   : "sex",
                icon  : '<i class="fa fa-ship"></i>',
                title : "i18n:性别",
                tip   : "请输入你的性别",
                required : true,
                type  : "string",
                editAs : "switch",
                uiConf : {items : [{text : "男", val:"m"},{text : "女", val:"f"}]}
            },{
                key   : "live",
                title : "i18n:仍旧存活",
                tip   : "量子态不算存活，算死翘翘",
                type  : "boolean",
                editAs : "switch"
            }];
    },
    //...............................................................
    fields_B : function(){
        return [{
                key   : "age",
                title : "i18n:年龄",
                type  : "int",
                editAs : "input"
            },{
                key   : "race",
                title : "i18n:种族",
                type  : "string",
                editAs : "input"
            },{
                key   : "usenm",
                title : "i18n:曾用名",
                type  : "string",
                span  : 2,
                editAs : "input"
            },{
                key   : "skill",
                title : "i18n:技能",
                type  : "string",
                editAs : "input" 
            }];
    },
    //...............................................................
    fields_C : function(){
        return [{
            key   : "poli",
            title : "i18n:政治面貌",
            tip   : "你就看着填吧",
            type  : "object",
            editAs : "droplist",
            emptyArrayAsUndefined : true,
            dft : 1,
            uiConf : {
                multi : 0,
                items : ["党员","团员","群众","敌特"]
            }
        },{
            key   : "FTF",
            title : "i18n:饮食口味",
            tip   : "你就看着填吧",
            type  : "object",
            editAs : "checklist",
            dft   : 1,
            uiConf : {
                multi : 0,
                items : [{
                    icon:'<i class="fa fa-fire"></i>',
                    text:"辣"
                },{
                    text:"酸"
                },{
                    text:"清淡再清淡"
                },{
                    text:"卤味"
                }]
            }
        },{
            key   : "myphoto",
            title : "i18n:我的图片",
            tip   : "随便选个图片",
            type  : "object",
            uiType : "ui/picker/opicker",
            uiConf : {
                clearable : false,
                parseData : function(obj){
                    if(obj)
                        return Wn.getById(obj.fid);
                },
                formatData : function(o){
                    return o ? {fid:o.id} : null;
                }
            }
        },{
            key   : "myfiles",
            title : "i18n:我的多个文件",
            tip   : "随便选个一些文件和文件夹咯",
            type  : "object",
            //dft   : [{fid:'t6c2m2r5a0htep2rto785n588f'},{fid:'3d7gf4mdtghbqqrknp2snkpo3a'}],
            uiType : "ui/picker/opicker",
            uiConf : {
                setup : {
                    checkable : true
                },
                parseData : function(objs){
                    var re = [];
                    if(objs){
                        objs.forEach(function(o, index){
                            re.push(Wn.getById(o.fid));
                        });
                    }
                    return re;
                },
                formatData : function(os){
                    var re = [];
                    os.forEach(function(o, index){
                        re.push({fid:o.id});
                    });
                    return re;
                }
            }
        },{            
            key   : "comment",
            title : "i18n:补充说明",
            tip   : "随便写点什么咯",
            type  : "string",
            editAs : "text",
            uiConf : {
                height: 120
            }
        }];
    },
    //...............................................................
    do_t0 : function(){
        var UI = this;
        new FormUI({
            parent : UI,
            gasketName : "myform",
            title : "普通测试表单",
            uiWidth : "auto",
            fields : UI.fields_A()
        }).render(function(){
            this.setData(UI.getObj());
        });
    },
    //...............................................................
    do_t1 : function(){
        var UI = this;
        new FormUI({
            parent : UI,
            gasketName : "myform",
            title : "分组表单",
            fields : [{
                icon : '<i class="fa fa-tachometer"></i>',
                title: '更多的控件',
                uiWidth : "all",
                fields : UI.fields_C()
            },{
                icon : '<i class="fa fa-rss"></i>',
                title: '各种属性',
                uiWidth : 300,
                fields : UI.fields_A()
            },{
                icon : '<i class="fa fa-rss"></i>',
                title: '四个输入框',
                fields : UI.fields_B()
            }]
        }).render(function(){
            this.setData(UI.getObj());
        });
    },
    //...............................................................
    do_t2 : function(){
        var UI = this;
        new FormUI({
            parent : UI,
            gasketName : "myform",
            title : "组为多列的表单",
            uiWidth : "all",
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
            this.setData(UI.getObj());
        });
    },
    //...............................................................
    update : function(o){
        this.$el.attr("obj-id", o.id);
        this.do_t2();
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);