(function($z){
$z.declare([
    'zui',
    'ui/form/support/bullet_list',
], function(ZUI, BulletListSupport){
//==============================================
var html = function(){/*
<div class="ui-arena com-checklist com-butlist">
    <div class="cc-quick">
        <span mode="all">{{com.checklist.all}}</span>
        <span mode="reverse">{{com.checklist.reverse}}</span>
        <span mode="none">{{com.checklist.none}}</span>
    </div>
    <ul></ul>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_com_checklist", BulletListSupport({
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/form/theme/component-{{theme}}.css",
    i18n : "ui/form/i18n/{{lang}}.js",
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
            var UI  = this;
            var opt = UI.options;
            var jq  = $(e.currentTarget);
            // 有限多选的话 ...
            if(opt.multi > 1){
                if(jq.hasClass("checked")){
                    jq.removeClass("checked");
                }
                // 没超过了限制，才能再选
                else if(UI.arena.find("li.checked").size() < opt.multi){
                    jq.addClass("checked");
                }
                // 否则警告
                else{
                    alert(UI.msg("com.multilimit", {n:opt.multi}));
                }
            }
            // 随便多选的话 ...
            else{
                jq.toggleClass("checked");
            }
            UI.__on_change();
        },
        "click .cc-quick span" : function(e) {
            var UI  = this;
            var opt = UI.options;
            var md  = $(e.currentTarget).attr("mode");
            var jLi = UI.arena.find("ul li");
            // 全
            if("all" == md){
                // 有限多选的话 ...
                if(opt.multi > 1 && jLi.size()>opt.multi){
                    alert(UI.msg("com.multilimit", {n:opt.multi}));
                    return;
                }
                // 全部标记
                jLi.addClass("checked");
            }
            // 反
            else if("reverse" == md){
                // 有限多选的话 ...
                if(opt.multi > 1 && jLi.not(".checked").size()>opt.multi){
                    alert(UI.msg("com.multilimit", {n:opt.multi}));
                    return;
                }
                jLi.toggleClass("checked");
            }
            // 无
            else {
                jLi.removeClass("checked");
            }
            UI.__on_change();
        }
    },
    //...............................................................
    _list_item_icon : '<i class="fa fa-square-o"></i><i class="fa fa-check-square"></i>',
    //...............................................................
    _before_load : function() {
        // 去掉快速按钮
        if(this.options.quickButton === false){
            this.arena.find(".cc-quick").remove();
        }
    },
    //...............................................................
    _get_data : function(){
        var re = [];
        this.arena.find("li.checked").each(function(){
            re.push($(this).data("@VAL"));
        });
        return re;
    },
    //...............................................................
    _set_data : function(val){
        var UI = this;

        // 确保值是一个数组
        if(!_.isArray(val)){
            val = [val];
        }
        // 查找吧少年
        UI.arena.find("li").removeClass("checked").each(function(){
            var jLi = $(this);
            var v0  = jLi.data("@VAL");
            if(val.indexOf(v0)>=0){
                jLi.addClass("checked");
            }
        });
        
    }
    //...............................................................
}));
//===================================================================
});
})(window.NutzUtil);