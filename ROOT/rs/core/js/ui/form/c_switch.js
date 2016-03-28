(function($z){
$z.declare([
    'zui'
], function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena com-switch">
    <ul></ul>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_com_switch", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function(options){
        $z.setUndefined(options, "items", [{
            val   : false,
            text  : "i18n:no"
        },{
            val   : true,
            text  : "i18n:yes"
        }]);
        $z.setUndefined(options, "icon", function(o){
            if(_.isObject(o)) 
                return o.icon;
        });
        $z.setUndefined(options, "text", function(o){
            if(_.isString(o))
                return o;
            return o.text;
        });
        $z.setUndefined(options, "value", function(o, index){
            if(_.isString(o))
                return index;
            return _.isUndefined(o.val) ? index : o.val;
        });
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
                if(!jq.hasClass("checked")){
                    UI.arena.find(".checked").removeClass("checked");
                    jq.addClass("checked");
                    UI.__on_change();
                }
            }
        }
    },
    //...............................................................
    __on_change : function(){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;
        var v = UI.getData();
        $z.invoke(opt, "on_change", [v], context);
        UI.trigger("change", v);
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var re = ["loading"];
        $z.evalData(UI.options.items, null, function(items){
            UI._draw_items(items);
            re.pop();
            UI.defer_report(0, "loading");
        });
        return re;
    },
    //...............................................................
    _draw_items : function(items){
        var UI  = this;
        var opt = UI.options;
        var jUl = UI.arena.find("ul");
        var context = opt.context || UI;

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

            // 下拉项目
            var text = opt.text.call(context, item, i, UI);
            $('<b it="text">').text(UI.text(text)).appendTo(jLi);
        }
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
});
//===================================================================
});
})(window.NutzUtil);