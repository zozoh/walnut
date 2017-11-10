(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/c_combotable'
], function(ZUI, Wn, ComboTableUI){
//==============================================
var html = function(){/*
<div class="ui-arena">
    <div class="tcc-btns" style="padding:10px; background:rgba(0,0,0,0.5);">
        <button>getData</button>
    </div>
    <div ui-gasket="com0" style="width:300px;"></div>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_test_combotable", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        'click .tcc-btns > button' : function(e){
            var jB = $(e.currentTarget);
            var data = this.gasket.com0[jB.text()]();
            console.log($z.toJson(data));
            //this.alert($z.toJson(data));
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
                    width  : "60%",
                    uiType : "@label",
                }, {
                    title  : "价格",
                    key    : "price",
                    type   : "int",
                    dft    : 3,
                    width  : "20%",
                    uiType : "@input",
                }, {
                    title  : "数量",
                    key    : "amont",
                    type   : "int",
                    dft    : 1,
                    width  : "20%",
                    uiType : "@input",
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
                "price":null,
                "amont":null
            },{
                "id":"7jtsk47i5ghbuqiggn9c8grgjf",
                "nm":".thumbnail",
                "price":null,
                "amont":null}]);

            UI.defer_report("com0");
        });
        //...........................................................
        return ["com0"];
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);