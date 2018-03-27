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

        UI.listenBus("update:obj", UI.updatePathBar);
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
    update : function(obj) {
        var UI = this;

        // 记录数据
        UI.__my_obj = obj;

        // 通知其他部分激活某个对象的属性
        UI.fire("active:other", obj);

        // 显示对象路径
        UI.updatePathBar(obj);

        // 更新显示对象 
        UI.gasket.content.update(obj);
    },
    //...............................................................
    updatePathBar : function(obj) {
        var UI = this;
        var jH = UI.arena.children("header").empty();
        if(!obj) {
            jH.text("empty!");
        }
        // 显示对象路径
        else {
            //var appName = window.wn_browser_appName || "wn.hmaker2";
            var aph = UI.getRelativePath(obj);
            jH.append($(UI.getObjIcon(obj, true)))
              .append($('<span>').text(aph));
                // .append($('<a target="_blank" href="/a/open/'
                //             + appName + '?ph=id:'+obj.id+'">' + aph + '</a>'));
        }
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