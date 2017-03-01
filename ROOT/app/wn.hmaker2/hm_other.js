(function($z){
$z.declare([
    'zui',
    'app/wn.hmaker2/support/hm__methods',
    'ui/o_view_obj/o_view_obj',
], function(ZUI, HmMethods, ViewObjUI){
//==============================================
var html = `
<div class="ui-arena hm-other" ui-fitparent="yes">
    <header></header>
    <section ui-gasket="objview"></section>
</div>
`;
//==============================================
return ZUI.def("app.wn.hmaker_other", {
    dom : html,
    //...............................................................
    init : function() {
        var UI = HmMethods(this);
    },
    //...............................................................
    redraw : function(){
        var UI = this;

        // 显示对象浏览器
        new ViewObjUI({
            parent : UI,
            gasketName : "objview",
            showMeta : false,
        }).render(function(){
            UI.defer_report("objview");
        });

        return ["objview"];
    },
    //...............................................................
    update : function(o) {
        var UI = this;

        // 记录数据
        UI.__my_obj = o;

        // 通知其他部分激活某个对象的属性
        UI.fire("active:other", o);

        // 显示对象路径
        var aph = UI.getRelativePath(o);
        UI.arena.children("header")
            .append($(UI.getObjIcon(o)))
            .append($('<a target="_blank" href="/a/open/browser?ph=id:'+o.id+'">' + aph + '</a>'));

        // 更新显示对象 
        UI.gasket.objview.update(o);
    },
    //...............................................................
    getCurrentEditObj : function(){
        return this.__my_obj;
    },
    //...............................................................
    getActions : function(){
        return ["::hmaker/hm_create", 
                "@::hmaker/hm_delete",
                "@::download",
                "~",
                "::hmaker/hm_site_new",
                "::hmaker/hm_site_dup",
                "::hmaker/hm_site_del",
                "~",
                "@::hmaker/pub_site",
                "~",
                "@::hmaker/hm_site_conf",
                "~",
                "::zui_debug",
                "::open_console"];
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);