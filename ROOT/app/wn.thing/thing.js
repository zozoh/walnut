(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/search/search',
    'ui/form/form'
], function(ZUI, Wn, SearchUI, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena thing" ui-fitparent="yes">
    <section class="th-search"><div class="th-con" ui-gasket="search"></div></section>
    <section class="th-form"><div class="th-con" ui-gasket="form"></div></section>
</div>
*/};
//==============================================
return ZUI.def("app.wn.thing", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/app/wn.thing/thing.css",
    i18n : "app/wn.thing/i18n/{{lang}}.js",
    //...............................................................
    update : function(o) {
        var UI = this;

        // 创建搜索条
        new SearchUI({
            parent : UI,
            gasketName : "search",
            menu : ["delete", "refresh"],
            data : "thing "+o.id+" query '<%=match%>' -skip {{skip}} -limit {{limit}} -json -pager -sort 'lm:1'",
            
        }).render(function(){
            this.refresh();
        });
        
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);