(function($z){
$z.declare([
    'zui'
], function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena com-radiolist com-butlist">
    <ul></ul>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_com_radiolist", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function(options){
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
            this.arena.find(".checked").removeClass("checked");
            $(e.currentTarget).addClass("checked");
        }
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

        var hasIcon = false;
        for(var i=0; i<items.length; i++){
            var item = items[i];
            var val  = opt.value.call(context, item, i, UI); 

            var jLi = $('<li>').appendTo(jUl)
                .attr("index", i)
                .data("@VAL", val);

            // 选择框
            $('<span it="but"><i class="fa fa-circle-thin"></i><i class="fa fa-chevron-circle-right"></i></span>')
                .appendTo(jLi);

            // 图标
            var icon = _.isString(opt.icon)
                                ? $z.tmpl(opt.icon)(item)
                                : opt.icon.call(context, item, i, UI);
            jIcon = $('<span it="icon">').appendTo(jLi);
            if(_.isString(icon)){
                jIcon.html(icon);
                hasIcon = true;
            }

            // 文字
            var text = _.isString(opt.icon)
                                ? $z.tmpl(opt.icon)(item)
                                : opt.text.call(context, item, i, UI);
            $('<b it="text">').text(UI.text(text)).appendTo(jLi);
        }

        // 没有 Icon 就全部移除
        if(!hasIcon){
            UI.arena.find("span[it='icon']").remove();
        }
    },
    //...............................................................
    getData : function(){
        return UI.arena.find("li.checked").first().data("@VAL");
    },
    //...............................................................
    setData : function(val){
        var UI = this;

        // 确保值是一个数组
        if(!_.isArray(val)){
            val = [val];
        }
        // 查找吧少年
        var jLis = UI.arena.find("li");
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
});
//===================================================================
});
})(window.NutzUtil);