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
return ZUI.def("ui.test_form3", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "click .get_data" : function(){
            console.log(this.subUI("myform").getData());
        },
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
            title : "测试高级控件",
            uiWidth : "all",
            fields : [{
                key   : "abc_list",
                title : "物品列表",
                tip   : "你就看着填吧",
                type  : "object",
                uiWidth : "auto",
                uiType : "@combotable",
                uiConf : {
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
                        return Wn.fetch("~/" + nm, true);
                    }
                }
            }]
        }).render(function(){
            this.setData({
                abc_list : [{
                        "id":"vnt8bmelr4g9mp5tsus5hpsfts",
                        "nm":".hmaker",
                        "price":null,
                        "amont":null
                    },{
                        "id":"7jtsk47i5ghbuqiggn9c8grgjf",
                        "nm":".thumbnail",
                        "price":null,
                        "amont":null}]
                //myphoto : {fid:'4thoboi83khmdqmqqvf5arogki'}
            });
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);