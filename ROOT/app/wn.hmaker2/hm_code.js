(function($z){
$z.declare([
    'zui',
    'app/wn.hmaker2/support/hm__methods',
    'ui/o_edit_text/o_edit_text'
], function(ZUI, HmMethods, EditTextUI){
//==============================================
var html = `
<div class="ui-arena hm-code" ui-fitparent="yes">
    <header></header>
    <section ui-gasket="content"></section>
</div>
`;
//==============================================
return ZUI.def("app.wn.hmaker_text", {
    dom : html,
    //...............................................................
    init : function() {
        var UI = HmMethods(this);
    },
    //...............................................................
    redraw : function(){
        var UI = this;

        // 显示对象浏览器
        new EditTextUI({
            parent : UI,
            gasketName : "content",
            showMeta : false,
        }).render(function(){
            UI.defer_report("content");
        });

        return ["content"];
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
            .append($('<span>').text(aph));
            //.append($('<a target="_blank" href="/a/open/'+window.wn_browser_appName+'?ph=id:'+o.id+'">' + aph + '</a>'));

        // 更新显示对象 
        UI.gasket.content.update(o);
    },
    //...............................................................
    getCurrentEditObj : function(){
        return this.__my_obj;
    },
    //...............................................................
    getCurrentTextContent : function(){
        return this.gasket.content.getCurrentTextContent();
    },
    //...............................................................
    getActions : function(){
        return ["@::save_text",
                "::hmaker/hm_create", 
                "::hmaker/hm_duplicate", 
                "::hmaker/hm_delete",
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