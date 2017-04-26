(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
], function(ZUI, Wn, FormUI, MenuUI, ListUI){
//==============================================
var html = function(){/*
<div class="ui-arena thconf" ui-fitparent="yes">
    <div class="thc-info" ui-gasket="info"></div>
    <div class="thc-flds">
        <div class="thc-menu" ui-gasket="menu"></div>
        <div class="thc-btns">
            <a>{{thing.conf.cancel}}</a>
            <b>{{thing.conf.saveflds}}</b>
        </div>
        <div class="thc-fld-list" ui-gasket="list"></div>
        <div class="thc-fld-conf"></div>
    </div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.thing", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "app/wn.thing/theme/thing-{{theme}}.css",
    i18n : "app/wn.thing/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt) {
        
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        new FormUI({
            parent : UI,
            gasketName : "info",
            fitparent : false,
            fields : [{
                key   : "nm",
                title : "i18n:thing.conf.key_nm",
            }, {
                key   : "icon",
                title : "i18n:thing.conf.key_icon",
            }]
        }).render(function(){
            UI.defer_report("info");
        });

        new MenuUI({
            parent : UI,
            gasketName : "menu",
            setup : [{
                icon : '<i class="fa fa-plus"></i>',
                text : "i18n:thing.conf.addfld",
                handler : function(){
                    alert("add");
                }
            }, {
                icon : '<i class="fa fa-trash"></i>',
                text : "i18n:thing.conf.delfld",
                handler : function(){
                    alert("del");
                }
            }]
        }).render(function(){
            UI.defer_report("menu");
        });

        new ListUI({
            parent : UI,
            gasketName : "list",
        }).render(function(){
            UI.defer_report("list");
        });

        // 返回延迟加载
        return ["info", "menu", "list"];
    },
    //...............................................................
    update : function(oThSet) {
        var UI  = this;
        var opt = UI.options;
        
    },
    //...............................................................
    setData : function(oThSet) {
        this.update(oThSet);
    },
    //...............................................................
    resize : function() {
        var UI = this;
        var jInfo = UI.arena.find(">.thc-info");
        var jFlds = UI.arena.find(">.thc-flds");

        var H = UI.arena.height();
        jFlds.css("height", H - jInfo.outerHeight(true));
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);