(function($z){
$z.declare([
    'zui',
    'ui/form/support/bullet_list',
], function(ZUI, BulletListSupport){
//==============================================
var html = function(){/*
<div class="ui-arena com-radiolist com-butlist">
    <ul></ul>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_com_radiolist", BulletListSupport({
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/ui/form/component.css",
    i18n : "ui/form/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt){
        this.__setup_dft_display_func(opt);
    },
    //...............................................................
    events : {
        "click li" : function(e){
            this.arena.find(".checked").removeClass("checked");
            $(e.currentTarget).addClass("checked");
            this.__on_change();
        }
    },
    //...............................................................
    _list_item_icon : '<i class="fa fa-circle-thin"></i><i class="fa fa-chevron-circle-right"></i>',
    //...............................................................
    getData : function(){
        var UI  = this;
        var opt = UI.options;
        var jLi = this.arena.find("li.checked").first();
        var val = jLi.data("@VAL");

        if(opt.fullData) {
            return {
                text  : jLi.find('b[it]').text(),
                value : val
            }
        }

        return val;
    },
    //...............................................................
    setData : function(val){
        var UI = this;

        // 确保值是一个数组
        if(!_.isArray(val)){
            val = [val];
        }
        // 查找吧少年
        var jLis = UI.arena.find("li").removeClass("checked");
        for(var i=0;i<jLis.length;i++){
            var jLi = jLis.eq(i);
            var v0  = jLi.data("@VAL");
            if(v0 == val){
                jLi.addClass("checked");
                break;
            }
        }
        
    }
    //...............................................................
}));
//===================================================================
});
})(window.NutzUtil);