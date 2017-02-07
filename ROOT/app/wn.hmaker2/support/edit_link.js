(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/c_droplist',
], function(ZUI, Wn, DropListUI){
//==============================================
var html = `
<div class="ui-arena edit-link">
    <header ui-gasket="drop"></header>
    <section><input></section>
    <aside></aside>
</div>`;
//==============================================
return ZUI.def("ui.edit_link", {
    dom  : html,
    css  : 'theme/app/wn.hmaker2/support/edit_link.css',
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        // 创建辅助下拉列表
        new DropListUI({
            parent : UI,
            gasketName : "drop",
            items      : opt.items,
            emptyItem  : opt.emptyItem,
            icon  : opt.icon,
            text  : opt.text,
            value : opt.value,
            on_change : function(v){
                if(v)
                    UI.setData(v);
            }
        }).render(function(){
            UI.defer_report("drop");
        });

        // 更新提示信息
        var jTip = UI.arena.find(">aside");
        if(opt.tip) {
            jTip.text(UI.text(opt.tip));
        }
        // 移除提示信息
        else {
            jTip.remove();
        }

        // 返回延迟加载
        return ["drop"];
    },
    //...............................................................
    getData : function() {
        return $.trim(this.arena.find(">section>input").val());
    },
    //...............................................................
    setData : function(str) {
        this.arena.find("section input").val(str).focus();
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);