(function($z){
$z.declare([
    'zui'
], function(ZUI){
//==============================================
var do_close_all = function(ignore_cids){
    if(!ignore_cids){
        ignore_cids = [];
    }
    if(!_.isArray(ignore_cids)){
        ignore_cids = [ignore_cids];
    }
    $('.ui-form_com_droplist').each(function(){
        ZUI(this).arena.removeAttr("show");
    });
};
//==============================================
var html = function(){/*
<div class="ui-arena com-droplist">
    <div class="com-box">
        <div class="com-box-show">&nbsp;</div>
        <div class="com-box-drop"><i class="fa fa-caret-down"></i></div>
    </div>
    <div class="com-mask"></div>
    <div class="com-drop">
        <ul></ul>
        <div class="com-multi-apply">{{ok}}</div>
    </div>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_com_droplist", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
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

        // 注册全局关闭
        this.watchMouse("click", do_close_all);
        this.watchKey(27, do_close_all);
    },
    //...............................................................
    events : {
        "click .com-box" : function(e){
            e.stopPropagation();
            var UI = this;
            // 隐藏
            if(UI.arena.attr("show")){
                UI.arena.removeAttr("show");
            }
            // 显示
            else{
                // 多选的话，同步状态
                if(UI.isMulti()){
                    var vals = UI.getData();
                    UI.arena.find("ul li").each(function(){
                        var jLi = $(this);
                        var v0 = jLi.data("@VAL");
                        if(vals.indexOf(v0)>=0){
                            jLi.addClass("checked");
                        }else{
                            jLi.removeClass("checked");
                        }
                    });
                }
                // 单选同步状态
                else{
                    var val = UI.getData();
                    UI.arena.find("ul li").each(function(){
                        var jLi = $(this);
                        var v0 = jLi.data("@VAL");
                        if(v0 == val){
                            jLi.addClass("checked");
                        }else{
                            jLi.removeClass("checked");
                        }
                    });
                }
                // 标记显示
                UI.arena.attr("show", "yes");
            }
        },
        "click li" : function(e){
            var UI  = this;
            var jq = $(e.currentTarget);

            // 有限多选的话 ...
            if(UI.isMulti(true)){
                e.stopPropagation();

                // 判断判断
                if(jq.hasClass("checked")){
                    jq.removeClass("checked");
                }
                // 没超过了限制，才能再选
                else if(UI.arena.find("li.checked").size() < UI.options.multi){
                    jq.addClass("checked");
                }
                // 否则警告
                else{
                    alert(UI.msg("com.multilimit", {n:UI.options.multi}));
                }
            }
            // 随便多选的话 ...
            else if(UI.isMulti()){
                e.stopPropagation();
                jq.toggleClass("checked");
            }
            // 单选，就简单了
            else {
                UI.arena.find(".com-box-show").empty();
                UI._append_val(jq);
                // 触发事件
                UI.__on_change();
            }
        },
        "click .com-multi-apply" : function(){
            var UI = this;
            UI.arena.find(".com-box-show").empty();
            UI._append_val(UI.arena.find(".com-drop li.checked"));
            // 触发事件
            UI.__on_change();
        }
    },
    //...............................................................
    _append_val : function(jLis) {
        var UI    = this;
        var jShow = UI.arena.find(".com-box-show");
        jLis.each(function(){
            var jLi = $(this);
            var val   = jLi.data("@VAL");
            // 把列表项里的东东 copy 到框框里
            var jq = $('<div class="cbsi">').appendTo(jShow)
                .html(jLi.html())
                .data("@VAL", val);
            // 木有图标的就移除啦
            var jIcon = jq.find("span[it=icon]");
            if(jIcon.children().size() == 0)
                jIcon.remove();
        });
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;

        // 标记单/多选形态
        UI.arena.attr("multi", UI.isMulti() ? "yes" : "no");

        // 读取数据
        var re = ["loading"];
        $z.evalData(UI.options.items, null, function(items){
            UI._draw_items(items);
            re.pop();
            UI.defer_report(0, "loading");
        }, context);

        // 返回，以便异步的时候延迟加载
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

            // 是否是多选，数值大于 1
            if(UI.isMulti()){
                $('<span it="check"><i class="fa fa-square-o"></i><i class="fa fa-check-square"></i></span>')
                    .appendTo(jLi);
            }else{
                $('<span it="check"><i class="fa fa-check"></i></span>')
                    .appendTo(jLi);
            }

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
        var jShow = UI.arena.find(".com-box-show");

        // 多选返回的是数组
        if(UI.isMulti()){
            var re = [];
            jShow.find(".cbsi").each(function(){
                re.push($(this).data("@VAL"));
            });
            return re;
        }

        // 单选就返回值
        return jShow.find(".cbsi").first().data("@VAL");
    },
    //...............................................................
    setData : function(val){
        var UI = this;
        UI.arena.find(".com-box-show").empty();

        // 所有的备选项
        var jLis = UI.arena.find("ul li");

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
                    UI._append_val(jLi);
                }
            });
        }
        // 单选只加一个
        else{
            for(var i=0;i<jLis.length;i++){
                var jLi = jLis.eq(i);
                var v0  = jLi.data("@VAL");
                if(v0 == val){
                    UI._append_val(jLi);
                    break;
                }
            }
        }
        // 触发事件
        UI.__on_change();
    },
    //...............................................................
    __on_change : function(){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;
        var v = UI.getData();
        $z.invoke(opt, "on_change", [v], context);
        UI.trigger("change", v);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);