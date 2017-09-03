(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/c_input'
], function(ZUI, Wn, InputUI){
//==============================================
var html = function(){/*
<div class="ui-arena">
    <div ui-gasket="t_combo"></div>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_test_input", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function(){
        var UI = this;

        //...........................................................
        new InputUI({
            parent : UI,
            gasketName : "t_combo",
            width: 300,
            on_change : function(val) {
                console.log("new value::", val);
            },
            // assist : {
            //     uiType : "ui/form/c_date_range"
            // }
            assist : {
                //text : "更多",
                uiType : "ui/form/c_list",
                uiConf : {
                    drawOnSetData : true,
                    items : 'obj ~ -match \'race:"DIR", nm:"^{{val}}"\' -limit 10 -json -l -e "^(id|tp|race|nm)$"',
                    itemArgs : {val : ".+"},
                    icon : function(o){
                        return Wn.objIconHtml(o);
                    },
                    text : function(o) {
                        return Wn.objDisplayName(UI, o.nm, 0);
                    },
                    value : function(o) {
                        return o.nm;
                    }
                }
            }
        }).render(function(){
            UI.defer_report("t_combo");
        });
        //...........................................................
        return ["t_combo"];
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);