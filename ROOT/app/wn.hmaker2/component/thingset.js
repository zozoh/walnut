(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = function(){/*
<div class="ui-arena hmc-thingset">
    I am thingset
</div>
*/};
//==============================================
return ZUI.def("app.wn.hm_com_thingset", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        console.log("I am com.thingset redraw")
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    setupProp : function(){
        return {
            uiType : 'ui/form/form',
            uiConf : {
                uiWidth : "all",
                fields : [{
                    title : 'i18n:hmaker.com.thingset.tt_ds',
                    fields : [{
                        key : "dsId",
                        title : "i18n:hmaker.com.thingset.ds_id",
                        type : "string",
                        editAs : "input",
                    },{
                        key : "dsQuery",
                        title : "i18n:hmaker.com.thingset.ds_query",
                        type : "object",
                        editAs : "text",
                    },{
                        key : "display",
                        title : "i18n:hmaker.com.thingset.display",
                        type : "object",
                        editAs : "text",
                    }]
                }, {
                    title : 'i18n:hmaker.com.thingset.tt_flt',
                    fields : [{
                        key : "fltEnabled",
                        title : "i18n:hmaker.com.thingset.flt_enabled",
                        type : "boolean",
                        editAs : "switch",
                    },{
                        key : "fltCnd",
                        title : "i18n:hmaker.com.thingset.flt_cnd",
                        type : "object",
                        editAs : "text",
                    },{
                        key : "fltSort",
                        title : "i18n:hmaker.com.thingset.flt_sort",
                        type : "object",
                        editAs : "text",
                    }]
                }, {
                    title : 'i18n:hmaker.com.thingset.tt_pager',
                    fields : [{
                        key : "pgEnabled",
                        title : "i18n:hmaker.com.thingset.pg_enabled",
                        type : "boolean",
                        editAs : "switch",
                    },{
                        key : "pgSize",
                        title : "i18n:hmaker.com.thingset.pg_size",
                        type : "int",
                        editAs : "input"
                    }]
                }]
            }
        };
    },
    //...............................................................
    getProp : function() {

    },
    //...............................................................
    paint : function(com) {

    }
});
//===================================================================
});
})(window.NutzUtil);