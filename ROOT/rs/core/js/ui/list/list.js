(function($z){
$z.declare([
    'zui',
    'ui/jtypes',
    'ui/support/list_methods'
], function(ZUI, JsType, ListMethods){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="checker" class="lst-item-checker">
        <i tp="checkbox" class="fa fa-square-o current"></i>
        <i tp="checkbox" class="fa fa-check-square"></i>
    </div>
</div>
<div class="ui-arena lst" ui-fitparent="true">
</div>
*/};
//==============================================
return ZUI.def("ui.list", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/list/theme/list-{{theme}}.css",
    //..............................................
    init : function(opt){
        var UI  = ListMethods(this);
        // 默认不是多选
        $z.setUndefined(opt, "checkable", false);

        // 父类
        UI.__setup_options(opt);

        // 其他
        $z.setUndefined(opt, "escapeHtml", true);

        // key 字段专门用来显示
        $z.setUndefined(opt, "key", opt.nmKey || opt.idKey);

        // 预先编译每个项目的显示方式
        $z.evalFldDisplay(opt);
    },
    //..............................................
    events : {
        "click .lst-item" : function(e){
            this._do_click_list_item(e, false);
        },
        "click .lst-item-checker" : function(e){
            e.stopPropagation();
            this.toggle(e.currentTarget);
        },
        "click .ui-arena" : function(e){
            var jq = $(e.target);
            var jRow = jq.closest(".lst-item");
            if(this.options.blurable && !jRow.hasClass("lst-item-actived"))
                this.setAllBlur();
        },
        "click .tbl-checker" : function(e){
            e.stopPropagation();
            var UI = this;
            var jChecker = $(e.currentTarget);
            var tp = jChecker.attr("tp");
            // 全选
            if("none" == tp){
                UI.check();
            }
            // 全取消
            else{
                UI.uncheck();
            }
        }
    },
    //...............................................................
    __before_draw_data : function(){
        var UI  = this;
        var opt = UI.options;

        // 如果需要显示选择框 ...
        if(opt.checkable){
            UI.arena.addClass("lst-show-checkbox");
        }
    },
    //..............................................
    __after_actived : function(o, jItem) {
        this.arena.find(".lst-item-checked")
            .removeClass("lst-item-actived lst-item-checked");
        jItem.addClass("lst-item-actived lst-item-checked");
    },
    //..............................................
    __after_checked : function(jItems) {
        jItems.addClass("lst-item-checked");
    },
    //..............................................
    __after_blur : function(jItems) {
        jItems.removeClass("lst-item-actived lst-item-checked");
    },
    //..............................................
    __after_toggle : function(jItems) {
        var UI = this;
        jItems.each(function(){
            var jItem = $(this);
            if(UI.isActived(jItem))
                jItem.addClass("lst-item-actived");
            else
                jItem.removeClass("lst-item-actived");

            if(UI.isChecked(jItem))
                jItem.addClass("lst-item-checked");
            else
                jItem.removeClass("lst-item-checked");
        });
    },
    //..............................................
    $listBody : function(){
        return this.arena;
    },
    //..............................................
    $createItem : function(){
        return $('<div class="lst-item">');
    },
    //..............................................
    _draw_item : function(jItem, obj) {
        var UI  = this;
        var opt = UI.options;

        // 获取显示值
        var s  = opt.__dis_obj.call(UI, obj, opt);

        // 国际化
        s = UI.text(s);

        // 逃逸 HTML
        if(opt.escapeHtml === true)
            jItem.text(s || '');
        else
            jItem.html(s || '');

        // 如果需要显示选择框
        if(opt.checkable){
            jItem.prepend(UI.ccode("checker"));
        }
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);