(function($z){
$z.declare([
    'zui',
    'ui/form/support/enum_list',
], function(ZUI, EnumListSupport){
//==============================================
var html = function(){/*
<div class="ui-arena com-switch">
    <ul></ul>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_com_switch", EnumListSupport({
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function(opt){
        
        // 设置默认取值方法
        this.__setup_dft_display_func(opt);

        // 默认是布尔选项
        $z.setUndefined(opt, "items", [{
            val   : false,
            text  : "i18n:no"
        },{
            val   : true,
            text  : "i18n:yes"
        }]);
    },
    //...............................................................
    events : {
        "click li" : function(e){
            var UI = this;
            var jq = $(e.currentTarget);
            // 有限多选的话 ...
            if(UI.isMulti(true)){
                if(jq.hasClass("checked")){
                    jq.removeClass("checked");
                    UI.__on_change();
                }
                // 没超过了限制，才能再选
                else if(UI.arena.find("li.checked").size() < UI.options.multi){
                    jq.addClass("checked");
                    UI.__on_change();
                }
                // 否则警告
                else{
                    alert(UI.msg("com.multilimit", {n:UI.options.multi}));
                }
            }
            // 随便多选的话 ...
            else if(UI.isMulti()){
                jq.toggleClass("checked");
                UI.__on_change();
            }
            // 单选，就简单了
            else {
                // 如果选中那么就取消选中
                if(jq.hasClass("checked")){
                    jq.removeClass("checked");
                }
                // 选中
                else {
                    UI.arena.find(".checked").removeClass("checked");
                    jq.addClass("checked");
                }
                // 通知
                UI.__on_change();
            }
        }
    },
    //...............................................................
    _draw_items : function(items){
        var UI  = this;
        var opt = UI.options;
        var jUl = UI.arena.find("ul");
        var context = opt.context || UI.parent;

        for(var i=0; i<items.length; i++){
            var item = items[i];
            var val  = opt.value.call(context, item, i, UI); 

            var jLi = $('<li>').appendTo(jUl)
                .attr("index", i)
                .data("@VAL", val);

            // 图标
            var icon = opt.icon.call(context, item, i, UI);
            jIcon = $('<span it="icon">').appendTo(jLi);
            if(_.isString(icon)){
                jIcon.html(icon);
                hasIcon = true;
            }

            // 文字
            var text = val;
            if(_.isString(opt.text))
                text = $z.tmpl(opt.text)(item);
            else if(_.isFunction(opt.text))
                text = opt.text.call(context, item, i, UI);

            if(text)
                $('<b it="text">').text(UI.text(text)).appendTo(jLi);

            // 提示文字
            if(item.tip) {
                var tip = UI.text(item.tip);
                if(!/^(up|down|left|right):/.test(tip))
                    tip = "up:" + tip;
                jLi.attr("balloon", tip);
            }
        }

        // 打开提示文字
        UI.balloon();
    },
    //...............................................................
    // limit 参数表示是否为限定数量的多选
    isMulti : function(limit){
        if(limit){
            return this.options.multi > 1;
        }
        return this.options.multi ? true : false;
    },
    //...............................................................
    getData : function(){
        var UI = this;
        // 多选返回的是数组
        if(UI.isMulti()){
            var re = [];
            UI.arena.find("li.checked").each(function(){
                re.push($(this).data("@VAL"));
            });
            return re;
        }

        // 单选就返回值
        return UI.arena.find("li.checked").first().data("@VAL");
    },
    //...............................................................
    setData : function(val){
        var UI = this;

        // 所有的备选项
        var jLis = UI.arena.find("li").removeClass("checked");

        // 多选就加多个
        if(UI.isMulti()){
            // 确保值是一个数组
            if(!_.isArray(val)){
                val = [val];
            }
            // 查找吧少年
            jLis.each(function(){
                var jLi = $(this);
                var v0  = jLi.data("@VAL");
                if(val.indexOf(v0)>=0){
                    jLi.addClass("checked");
                }
            });
        }
        // 单选只加一个
        else{
            for(var i=0;i<jLis.length;i++){
                var jLi = jLis.eq(i);
                var v0  = jLi.data("@VAL");
                if(v0 == val){
                    jLi.addClass("checked");
                    break;
                }
            }
        }
    }
    //...............................................................
}));
//===================================================================
});
})(window.NutzUtil);