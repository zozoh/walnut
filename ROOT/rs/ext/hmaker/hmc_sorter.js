/**
Searcher 控件的运行时行为:

opt : {
    "fields": [{
            "text": "价格",
            "name": "price",
            "modify": false,
            "order": 1,
            "enabled": true
            "items": [{"name": "vvv", "order": 1}],
        },{...}]

    // 下面是调用者自由增加的属性
    emptyHtml : '<i class="zmdi zmdi-alert-circle-o"></i> Empty',

    // 下面由 IDE 指定
    ignoreShowHideEvent : true,
}
*/
(function($, $z){
//...........................................................
// 绘制项目
function __draw_fld(fld, jUl) {
    // 得到排序值的 key
    var orKey = fld.order == 1 ? "asc" : "desc";

    var jLi = $('<li>').attr({
        "key"    : fld.name,
        "modify" : fld.modify ? "yes" : null,
        "or-val" : fld.order,
        "or-nm"  : orKey,
        "enabled" : fld.enabled ? "yes" : null,
    });
    
    // 绘制字段标题
    $('<em>').text(fld.text).appendTo(jLi);

    // 绘制排序图标
    $('<span>').attr("or-icon", orKey).appendTo(jLi);

    // 设置固定排序项目
    if(_.isArray(fld.items) && fld.items.length > 0) {
        var orFixed = [];
        for(var i=0; i<fld.items.length; i++) {
            var it = fld.items[i];
            orFixed.push(it.name + ":" + it.order);
        }
        jLi.attr("or-fixed", orFixed.join(","));
    }


    // 加入 DOM
    jUl.append(jLi);
}
//...........................................................
function __redraw(jq, opt) {
    // 空数据
    if(!_.isArray(opt.fields) || opt.fields.length == 0) {
        $('<div class="empty">')
            .html(opt.emptyHtml || "No Setting")
                .appendTo(jq);
        return jq;
    }

    // 开始绘制
    var jUl = $('<ul>').appendTo(jq);
    for(var i=0; i<opt.fields.length; i++) {
        __draw_fld(opt.fields[i], jUl);
    }
}
//...........................................................
function getValue(jLi) {
    console.log(jLi)
    var re = jLi.attr("key") + ":" + (jLi.attr("or-val")||1); 
    var or_fixed = jLi.attr("or-fixed");
    re += (or_fixed ? "," + or_fixed : "");
    return re.replace(/[ +]/g, "");
}
//...........................................................
// 命令模式
var CMD = {
    active : function(index) {
        var jUl = this.children("ul");
        var jLi = $z.jq(jUl, index);
        jUl.children().removeAttr("enabled");
        jLi.attr("enabled", "yes");
        return this;
    },
    value : function(str) {
        // 获取
        if(_.isUndefined(str)){
            var jLi = this.find('li[enabled]');
            if(jLi.length > 0)
                return getValue(jLi);
            return null;
        }
        // 设置某个高亮段
        str = str.replace(/[ +]/g, "");
        this.find("li").each(function(){
            var jLi = $(this);
            var val = getValue(jLi);
            jLi.attr("enabled", str == val ? "yes" : null);
        });
        // 返回
        return this;
    }
}
//...........................................................
$.fn.extend({ "hmc_sorter" : function(opt){
    // 命令模式
    if(_.isString(opt)) {
        var args = $z.toArgs(arguments);
        return CMD[opt].apply(this, args.slice(1));
    }

    // 得到自己所在控件
    var jq = this.empty();
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 开始绘制
    __redraw(jq, opt);

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 监控事件
    if(!opt.forIDE) {
        jq.on("click", "li", function(){
            var jLi = $(this);
            // 如果是当前选项，且是可修改的，那么切换一下值
            if(jLi.attr("enabled")) {
                // 直接返回以便跳过重新加载的语句
                if(!jLi.attr("modify"))
                    return;
                // 切换值
                if("asc" == jLi.attr("or-nm")) {
                    jLi.attr({
                        "or-nm"  : "desc",
                        "or-val" : -1
                    }).find("span[or-icon]")
                        .attr("or-icon","desc");
                }else{
                    jLi.attr({
                        "or-nm"  : "asc",
                        "or-val" : 1
                    }).find("span[or-icon]")
                        .attr("or-icon","asc");
                }
            }
            // 激活当前项目
            else {
                CMD.active.apply(jq, [this]);
            }

            // 重新加载
            HmRT.invokeDynamicReload(jq, true);
        });
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 返回自身以便链式赋值
    return jq;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

