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
        <div class="com-multi-action">
            <b a="ok">{{ok}}</b>
            <b a="cancel">{{cancel}}</b>
        </div>
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

        // 设置默认空项目的文字
        if(opt.emptyItem) {
            $z.setUndefined(opt.emptyItem, "text", "i18n:none");
            $z.setUndefined(opt.emptyItem, "value", "");
        }

        // 其他设置
        $z.setUndefined(opt, "escapeHtml", true);

        // 注册全局关闭
        UI.watchMouse("click", do_close_all);
        UI.watchKey(27, do_close_all);
    },
    //...............................................................
    events : {
        // 显示下拉框
        "click .com-box" : function(e){
            e.stopPropagation();
            var UI = this;
            // 隐藏: TODO 额，为啥有这个逻辑？
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
        // 点击下拉框项目
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
                // 记旧值
                var old_val = UI._get_data();
                UI.arena.find(".com-box-show").empty();
                UI._append_val(jq);

                // 得到新值
                var new_val = UI._get_data();

                // 触发事件
                if(new_val != old_val)
                    UI.__on_change();
            }
        },
        // 点击 mask 隐藏
        "click .com-mask" : function(e) {
            this.__do_apply_multi();
        },
        "click .com-multi-action b" : function(e){
            var UI = this;
            var jB = $(e.currentTarget);
            if("ok" == jB.attr("a")) {
                UI.__do_apply_multi();
            }
            // 直接关闭
            else{
                do_close_all();
            }
        }
    },
    //...............................................................
    _before_load : function() {
        // 标记单/多选形态
        this.arena.attr("multi", this.isMulti() ? "yes" : "no");
    },
    //...............................................................
    __do_apply_multi : function(){
        var UI = this;
        if(UI.isMulti()){
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
    _draw_items : function(items){
        var UI  = this;
        var opt = UI.options;
        var jUl = UI.arena.find("ul").empty();
        var context = opt.context || UI.parent;
        var multi   = UI.isMulti();

        // 是否绘制空对象
        var offset = 0;
        if(!multi && opt.emptyItem) {
            UI.__append_item(offset++, opt.emptyItem, multi, jUl);
        }

        // 默认添加一些固定的 items
        if(_.isArray(opt.fixItems)) {
            items = [].concat(opt.fixItems, items);
        }

        if(!_.isArray(items))
            return;

        // 逐个绘制
        var hasIcon = false;
        for(var i=0; i<items.length; i++){
            var item = items[i];

            // 准备绘制项目
            var it = {
                value : opt.value.call(context, item, i, multi, UI)
            };

            // 图标
            it.icon = _.isString(opt.icon)
                        ? $z.tmpl(opt.icon)(item)
                        : opt.icon.call(context, item, i, UI);
            hasIcon = _.isString(it.icon);

            // 文字
            it.text = it.value;
            // 指定了文字模板
            if(_.isString(opt.text)){
                it.text = $z.tmpl(opt.text)(item);
            }
            // 指定了文字回调函数
            else if(_.isFunction(opt.text)){
                it.text = opt.text.call(context, item, i, UI);
            }

            // 追加项目
            UI.__append_item(offset + i, it, multi, jUl);
        }

        // 没有 Icon 就全部移除
        if(!hasIcon){
            UI.arena.find("span[it='icon']").remove();
        }
    },
    //...............................................................
    // it - 接受标准的 {icon,text,value} 格式对象，绘制一个项目
    __append_item : function(index, it, multi, jUl) {
        var UI  = this;
        var opt = UI.options;

        jUl = jUl || UI.arena.find("ul");

        var jLi = $('<li>').appendTo(jUl)
                .attr("index", index)
                .data("@VAL", it.value);

        // 多选的话显示选择框
        if(UI.isMulti()){
            $('<span it="check"><i class="fa fa-square-o"></i><i class="fa fa-check-square"></i></span>')
                .appendTo(jLi);
        }
        // 否则显示一个选中指示器
        else{
            $('<span it="check"><i class="fa fa-check"></i></span>')
                .appendTo(jLi);
        }

        // 图标
        if(it.icon){
            $('<span it="icon">').html(it.icon).appendTo(jLi);
        }

        // 文字
        if(it.text) {
            var jText = $('<b it="text">').appendTo(jLi);
            if(opt.escapeHtml) {
                jText.text(UI.text(it.text));
            }else {
                jText.html(UI.text(it.text));
            }
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
    setMulti : function(limit) {
        this.options.multi = limit;
    },
    //...............................................................
    _get_data : function(){
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
    _set_data : function(val){
        var UI  = this;
        var opt = UI.options;
        console.log(val)

        // 清空显示框
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
                    return;
                }
            }
            // 没有找到，看看有没有默认值
            if(opt.emptyItem) {
                UI._append_val(jLis.first());
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
                width  : Math.max(jBox.outerWidth(), jDrop.width()),
                height : ""
            }));

            // 下面不要让下拉框超出窗口
            var rect = $D.rect.gen(jDrop);
            //console.log(rect)
            var viewport = $z.winsz();
            var rect2 = $D.rect.boundaryIn(rect, viewport, true);
            jDrop.css($z.pick(rect2, "top,left,width,height"));

            // 冻结后， CSS 可以修改内容的显示
            jDrop.attr("freeze", "yes");
        }
    }
    //...............................................................
}));
//===================================================================
});
})(window.NutzUtil);