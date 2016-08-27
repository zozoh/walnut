(function($z){
$z.declare([
    'zui',
    'ui/form/support/enum_list',
], function(ZUI, EnumListSupport){
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
    <div class="com-drop"><div>
        <ul></ul>
        <div class="com-multi-apply">{{ok}}</div>
    </div></div>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_com_droplist", EnumListSupport({
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    init : function(opt){
        var UI = this;

        // 设置默认取值方法
        UI.__setup_dft_display_func(opt);

        // 注册全局关闭
        UI.watchMouse("click", do_close_all);
        UI.watchKey(27, do_close_all);
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
                
                // 最后自动确定尺寸
                UI.resize();
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
    _before_load : function() {
        // 标记单/多选形态
        this.arena.attr("multi", this.isMulti() ? "yes" : "no");
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
    _draw_items : function(items){
        var UI  = this;
        var opt = UI.options;
        var jUl = UI.arena.find("ul").empty();
        var context = opt.context || UI.parent;

        if(!_.isArray(items))
            return;

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
                $('<span it="check"><i class="fa fa-caret-right"></i></span>')
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
            var text = val;
            if(_.isString(opt.text))
                text = $z.tmpl(opt.text)(item);
            else if(_.isFunction(opt.text))
                text = opt.text.call(context, item, i, UI);

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
        //UI.__on_change();
    },
    //...............................................................
    resize : function() {
        var UI = this;
        var opt = UI.options;
        // 展开了下拉框，那么需要自动调整大小和位置
        if(UI.arena.attr("show")) {
            var jBox  = UI.arena.find(".com-box");
            var jDrop = UI.arena.find(".com-drop").removeAttr("freeze");
            jDrop.css(_.extend(jBox.offset(), {
                width  : "",
                height : ""
            }));

            // 下面不要让下拉框超出窗口
            var rect = $z.rect(jDrop);
            var viewport = $z.winsz();
            var rect2 = $z.rect_clip_boundary(rect, viewport);
            jDrop.css($z.rectObj(rect2, "top,left,width,height"));

            // 冻结后， CSS 可以修改内容的显示
            jDrop.attr("freeze", "yes");
        }
    }
    //...............................................................
}));
//===================================================================
});
})(window.NutzUtil);