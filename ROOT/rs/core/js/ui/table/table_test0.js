(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/table/table'
], function(ZUI, Wn, TableUI){
//==============================================
var html = function(){/*
<div class="ui-arena" ui-fitparent="yes">
    <button class="get_data">GET DATA</button>
    | <button class="t0">T0</button>
      <button class="t1">T1</button>
      <button class="t2">T2</button>
    <div ui-gasket="testmain"
    style="width:600px; height:600px; padding:10px; margin:10px; border:10px solid #CCC;"></div>
</div>
*/};
//===================================================================
return ZUI.def("ui.table_test0", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "click .get_data" : function(){
            alert($z.toJson(this.subUI("testmain").getData()));
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
            name:'I am zozoh', 
            sex:"m",
            live:true,
            myphoto : {fid:'4thoboi83khmdqmqqvf5arogki'}
        };
    },
    //...............................................................
    getFields : function(){
        return [{
                key   : "nm",
                title : "名称",
                type  : "string"
            },{
                key   : "id",
                icon  : '<i class="fa fa-random"></i>',
                title : "ID",
                type  : "string"
            },{
                key   : "live",
                title : "有效性",
                type  : "string",
                dft   : "no",
                uiConf: {
                    items : [{
                        text : "i18n:yes", val:"yes"
                    },{
                        text : "i18n:no", val:"no"
                    }]
                }
            },{
                key   : "lm",
                title : "最后修改时间",
                type  : "datetime",
                format : "yy-mm-dd HH:MM:ss"
            }];
    },
    //...............................................................
    do_t0 : function(){
        var UI = this;
        new TableUI({
            parent : UI,
            arenaClass : "ui-noselect",
            gasketName : "testmain",
            fields : UI.getFields(),
            checkable:true
        }).render(function(){
            var uiTable = this;
            Wn.exec("obj ~/* -l -json", function(re){
                uiTable.setData($z.fromJson(re));
            });
        });
    },
    
    //...............................................................
    update : function(o){
        this.$el.attr("obj-id", o.id);
        this.do_t0();
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);