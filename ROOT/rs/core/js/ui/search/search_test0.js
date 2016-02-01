(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/search/search'
], function(ZUI, Wn, SearchUI){
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
return ZUI.def("ui.form_main", {
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
    getFields : function(){
        return [{
                key   : "nm",
                text  : "名称",
                type  : "string",
                escapeHtml : false,
                display : function(o){
                    var html = '<i class="oicon" otp="'+Wn.objTypeName(o)+'"></i>';
                    html += '<b>'+Wn.objDisplayName(o)+'</b>';
                    return html;
                }
            },{
                key   : "id",
                icon  : '<i class="fa fa-random"></i>',
                text  : "ID",
                type  : "string",
                editAs: "label"
            },{
                key   : "live",
                text  : "有效性",
                type  : "string",
                dft   : "no",
                editAs: "switch",
                uiConf: {
                    items : [{
                        text : "i18n:yes", val:"yes"
                    },{
                        text : "i18n:no", val:"no"
                    }]
                }
            },{
                key   : "lm",
                text  : "最后修改时间",
                type  : "datetime",
                editAs : "label"
            }];
    },
    //...............................................................
    do_t0 : function(o){
        var UI = this;
        new SearchUI({
            parent : UI,
            gasketName : "testmain",
            menu : ["create","edit","delete", "refresh"],
            edtCmdTmpl : {
                "create" : "obj id:"+o.pid+" -new '<%=json%>' -o",
                "delete" : "rm -rf id:{{id}}",
                "edit"   : "obj id:{{id}} -u '<%=json%>' -o"
            },
            list : {
                fields : UI.getFields()
            },
            pager : {
                dft : {
                    pn   : 1,
                    pgsz : 3
                }
            }
        }).render(function(){
            this.uiFilter.setData({
                match : {pid : o.pid}
            });
            this.uiPager.setData();
            this.refresh();
        });
    },
    //...............................................................
    update : function(o){
        this.$el.attr("obj-id", o.id);
        this.do_t0(o);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);