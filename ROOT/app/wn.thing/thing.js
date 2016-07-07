(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/search/search'
], function(ZUI, Wn, SearchUI){
//==============================================
var html = function(){/*
<div class="ui-arena thing" ui-fitparent="yes" ui-gasket="main"></div>
*/};
//==============================================
return ZUI.def("app.wn.thing", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/app/wn.thing/thing.css",
    i18n : "app/wn.thing/i18n/{{lang}}.js",
    //...............................................................
    init : function() {
        this.my_fields = $z.loadResource("jso:///a/load/wn.thing/form_thing.js");
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
    },
    //...............................................................
    update : function(o) {
        var UI = this;
        
        new SearchUI({
            parent : UI,
            gasketName : 'main',
            menu : ["create","edit","delete", "refresh"],
            data : "obj -match '<%=match%>' -skip {{skip}} -limit {{limit}} -l -json -pager -sort 'nm:1'",
            edtCmdTmpl : {
                "create" : "obj id:"+o.id+" -new '<%=json%>' -o",
                "delete" : "rm -rf id:{{id}}",
                "edit"   : "obj id:{{id}} -u '<%=json%>' -o"
            },
            formConf : {
                formatData : function(o){
                    o.race = 'DIR';
                    return o;
                }
            },
            maskConf : {
                width  : 500,
                height : 530
            },
            filter : {
                keyField : ["mobile:^[0-9+-]{11,}","nm:^[0-9a-zA-Z._-]{3,}","realname"]
            },
            list : {
                checkable  : true,
                fields     : UI.my_fields,
            },
            pager : {
                dft : {
                    pn   : 1,
                    pgsz : 50
                }
            }
        }).render(function(){
            this.uiFilter.setData({
                match : {pid : o.id}
            });
            this.uiPager.setData();
            this.refresh();
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);