(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/search/search'
], function(ZUI, Wn, SearchUI){
//==============================================
var html = function(){/*
<div class="ui-arena duser" ui-fitparent="yes" ui-gasket="main"></div>
*/};
//==============================================
return ZUI.def("app.wn.duser", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/app/wn.duser/duser.css",
    i18n : "app/wn.duser/i18n/{{lang}}.js",
    //...............................................................
    init : function(){
        this.my_fields = $z.loadResource("jso:///a/load/wn.duser/form_duser.js");
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 得到主目录
        var oDusr = Wn.fetch("~/.usr");

        // 显示列表
        UI.uiSearch = new SearchUI({
            parent : UI,
            gasketName : "main",
            menu : ["create","edit","delete", "refresh"],
            data : "obj -match '<%=match%>' -skip {{skip}} -limit {{limit}} -l -json -pager -sort 'nm:1'",
            edtCmdTmpl : {
                "create" : "obj id:"+oDusr.id+" -new '<%=json%>' -o",
                "delete" : "rm -rf id:{{id}}",
                "edit"   : "obj id:{{id}} -u '<%=json%>' -o"
            },
            maskConf : {
                width  : 500,
                height : 530
            },
            filter : {
                keyField : ["mobile:^[0-9+-]{11,}","nm:^[0-9a-zA-Z._-]{3,}","realname"]
            },
            list : {
                layout : {
                    sizeHint : ["*","*","*","*"]
                },
                fields     : UI.my_fields,
                context    : UI,
            },
            pager : {
                dft : {
                    pn   : 1,
                    pgsz : 50
                }
            }
        }).render(function(){
            this.uiFilter.setData({
                match : {pid : oDusr.id}
            });
            this.uiPager.setData();
            this.refresh(function(list){
                UI.defer_report("search")
            });
        });
        
        // 返回延迟
        return ["search"];
    },
    //...............................................................
    update : function(o, callback) {
        var UI = this;

        
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);