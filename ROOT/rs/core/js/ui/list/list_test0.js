(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/list/list'
], function(ZUI, Wn, ListUI){
//==============================================
var html = function(){/*
<div class="ui-arena" ui-fitparent="yes">
    <button class="get_data">GET DATA</button>
    | <button class="t0">T0</button>
      <button class="t1">T1</button>
      <button class="t2">T2</button>
    <div ui-gasket="testmain"
    style="width:600px; height:600px; background: #FFF; padding:10px; margin:10px;"></div>
</div>
*/};
//===================================================================
return ZUI.def("ui.list_test0", {
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
    _set_data : function(){
        var uiList = this;
        Wn.exec("obj ~/* -l -json", function(re){
            uiList.setData($z.fromJson(re));
        });
    },
    //...............................................................
    do_t0 : function(){
        var UI = this;
        new ListUI({
            parent : UI,
            gasketName : "testmain",
            arenaClass : "ui-noselect",
            nmKey : "nm",
            escapeHtml : false,
            dicon : function(o){return '<i class="oicon" otp="'+Wn.objTypeName(o)+'"></i>';},
            dtext : '<b>{{nm}}</b> : <em>{{id}}</em>',
            checkable:true,
            multi : true
        }).render(UI._set_data);
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