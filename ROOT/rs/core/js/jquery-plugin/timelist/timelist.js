(function($, $z){
//...........................................................
var NM_OPT     = "timelist_OPTION";
var NM_CHECKED = "timelist_CHECKED";
function _tmpl(str){
    return _.template(str, {
        escape : /\{\{([\s\S]+?)\}\}/
    });
}
function options(ele, opt) {
    // 保存配置
    if(opt){
        return $root(ele).data(NM_OPT, opt);
    }
    // 获取配置
    return $root(ele).data(NM_OPT);
}
function $root(ele) {
    var jq; 
    if (ele instanceof jQuery) {
        jq = ele;
    } else {
        jq = $(ele);
    }
    if(jq.hasClass("timelist"))
        return jq;
    var re = jq.parents(".timelist");
    if(re.size()>0)
        return re;
    re = jq.children(".timelist");
    if(re.size()>0)
        return re;
    throw "Can not found $root by : " + ele;
}
// 根据配置，来决定给定时间点与原始的时间点怎么融合
// 会返回一个新数组
// 参数 referSec 如果定义，则表示指明这个秒数所在的点一定是要保留的
// 如果 referSec === false 明确的表示不需要进行如何计算，就排个序就得了
function normalize_tps(opt, tps, referSec){
    // 空数组
    if(!tps || tps.length == 0)
        return [];

    // 首先将数组中所有的非绝对秒数的值都变成绝对秒数 
    for(var i=0;i<tps.length;i++){
        var tp = tps[i];
        if(!_.isNumber(tp)){
            tps[i] = $z.parseTime(tp).sec;
        }
    }
    // 排序 && 去重
    tps = _.uniq(tps.sort(function(a, b){
        return a - b;
    }), true);

    // 如果是多选模式，就无所谓了，
    if(!opt.multi && tps.length>1 && referSec!==false){
        // 范围模式只允许一个范围，没给定 referSec 则用最后一个
        if("range" == opt.mode){
            // 开始查找吧少年
            var found_refer_sec = false;
            var group   = [];
            var last    = -1;
            var sUnit   = opt.timeUnit * 60;
            var sUnitx2 = sUnit * 2;
            for(var i=0; i<tps.length; i++){
                var sec = tps[i];

                // 这个秒数是连续的压入
                if(last<0 || (last+sUnitx2)>sec){
                    group.push(sec);
                }
                // 不是连续的，如果之前是参考块，就返回
                else if(found_refer_sec){
                    return group;
                }
                // 否则就建立一个新块
                else{
                    group = [];
                    group.push(sec);
                }

                // 这个秒是否是命中呢？
                if(!found_refer_sec){
                    found_refer_sec = (sec == referSec);
                }

                // 记录 last
                last = sec;
            }
            // 最后无论如何，返回组
            return group;
        }
        // 单选模式，如果没有给定 referSec 则也用最后一个
        else {
            if(_.isUndefined(referSec) || _.indexOf(tps, referSec, true)<0){
                return tps[tps.length-1];
            }else{
                return [referSec];
            }
        }
    }

    // 去重 && 返回
    return tps;
}
//...........................................................
var commands = {
    get : function(mode){
        var re = [];
        this.find(".timelist-item-checked").each(function(){
            var jItem = $(this);
            var _t = $z.parseTime(jItem.attr("sec")*1);
            // 对象
            if("obj" === mode){
                re.push(_t);
            }
            // 秒
            else if("sec" == mode){
                re.push(_t.sec);
            }
            // 字符串
            else{
                re.push(_t.key_min);
            }
        });
        return re;
    },
    set : function(tps){
        var jRoot  = $root(this);
        var opt    = options(jRoot);
        var tps2   = normalize_tps(opt, tps);
        update(jRoot, opt, tps2, true);
        return this;
    },
    clear : function(){
        this.find(".timelist-item")
            .removeClass("timelist-item-checked")
            .removeClass("timelist-item-actived");
        return this;
    },
    add : function(tps){
        var jRoot  = $root(this);
        var opt    = options(jRoot);
        var tps2   = normalize_tps(opt, tps);
        update(jRoot, opt, tps2, false);
        return this;
    },
    disable : function(tps, resetAll){
        var jRoot  = $root(this);
        var opt    = options(jRoot);
        var tps2   = normalize_tps(opt, tps, false);
        set_disable(jRoot, opt, tps2, resetAll);
        return this;
    }
};
//...........................................................
var _DOM = function(){/*
<div class="timelist">
    <div class="timelist-wrapper"></div>
</div>
*/};
//...........................................................
function bindEvents(jRoot, opt){
    jRoot.on("click", ".timelist-item", on_click_item);
}
//...........................................................
function on_click_item(e){
    // 标记单元格
    var jItem     = $(this);

    // disable 的项目，无视
    if(jItem.hasClass("timelist-item-disabled")){
        return;
    }

    var jRoot     = $root(jItem);
    var jLast     = jRoot.find(".timelist-item-actived");
    var jItemList = jRoot.find(".timelist-item");

    jItem.toggleClass("timelist-item-checked");

    // 要标记一个范围了
    if(jLast.size()>0 && e.shiftKey){
        var s0 = jLast.attr("sec") * 1;
        var s1 = jItem.attr("sec") * 1;
        var sBegin = Math.min(s0, s1);
        var sEnd   = Math.max(s0, s1);
        jItemList.each(function(){
            var jq  = $(this);
            var sec = jq.attr("sec") * 1;
            if(sec >= sBegin && sec<sEnd){
                jq.addClass("timelist-item-checked");
            }
        });
    }

    // 记录自身为活动
    if(jItem.hasClass("timelist-item-actived")){
        jItem.removeClass("timelist-item-actived");
    }
    // 标记活动
    else{
        jLast.removeClass("timelist-item-actived");
        jItem.addClass("timelist-item-actived");
    }

    // 开始计算
    var opt = options(jRoot);
    var _t_sec = jItem.attr("sec") * 1;

    // 首先取得全部时间点
    var tps = commands.get.call(jRoot, "sec");

    // 根据配置项分析计算
    var tps2 = normalize_tps(opt, tps, _t_sec);

    // 更新
    update(jRoot, opt, tps2, true);
}
//...........................................................
// 接受的输入 tps 必须是一个绝对秒数
function update(jRoot, opt, tps, resetAll){
    // 考虑是否重置所有的选择
    var jItems = jRoot.find(".timelist-item");
    if(resetAll){
        jItems.removeClass("timelist-item-checked");
    }
    
    // 处理每个时间点项目
    do_each_item(jRoot, jItems, opt, tps, function(){
        this.addClass("timelist-item-checked");
    });

    // 回调
    $z.invoke(opt, "on_change", [tps], jRoot);
}
//...........................................................
// 接受输入标记一组时间点是不可选的
function set_disable(jRoot, opt, tps, resetAll){
    // 考虑是否重置所有的选择
    var jItems = jRoot.find(".timelist-item");
    if(resetAll){
        jItems.removeClass("timelist-item-disabled");
    }
    
    // 处理每个时间点项目
    do_each_item(jRoot, jItems, opt, tps, function(){
        this.addClass("timelist-item-disabled");
    });
}
//...........................................................
// 接受的输入 tps 必须是一个绝对秒数
function do_each_item(jRoot, jItems, opt, tps, callback){
    jItems = jItems || jRoot.find(".timelist-item");
    if(tps.length>0 && _.isFunction(callback)){
        var _tps = [].concat(tps);
        var sUnit   = opt.timeUnit * 60;
        jItems.each(function(){
            var jItem = $(this);
            var sec = jItem.attr("sec") * 1;
            var sto = sec + sUnit;
            // 在给定时间点里找，有木有能匹配的，匹配的就置 -1 表示用过了
            for(var i=0;i<_tps.length;i++){
                var tp = _tps[i];
                if(tp>=0 && tp>=sec && tp<sto){
                    _tps[i] = -1;
                    callback.apply(jItem, [tps, i, jRoot, opt]);
                    break;
                }
            }
        });
    }
}
//...........................................................
function redraw($ele, opt){
    // 得到 HTML 结构 
    var html = $z.getFuncBodyAsStr(_DOM.toString());
    html = html.substring(2, html.length - 2)

    // 创建基础 DOM 结构
    var jRoot = $(html).appendTo($ele);
    if(opt.className)
        jRoot.addClass(opt.className);
    if(opt.display)
        jRoot.addClass("timelist-" + opt.display);
    
    // 根据配置绘制
    var jWrapper = jRoot.children(".timelist-wrapper");
    var sUnit  = opt.timeUnit * 60;
    for(var i=0;i<opt.scopes.length;i++){
        var scope  = opt.scopes[i];
        var jScope = $('<div class="timelist-scope">').appendTo(jWrapper);
        var jScopeInner = $('<div class="timelist-scope-inner">').appendTo(jScope);
        var sBegin = $z.parseTime(scope[0]).sec;
        var sEnd   = $z.parseTime(scope[1]).sec;
        //console.log(scope, sBegin, sEnd)
        var n      = 0;
        var jGroup = $('<div class="timelist-group">').appendTo(jScopeInner);
        while(sBegin < sEnd){
            var _t = $z.parseTime(sBegin);
            //console.log(_t.key)
            if(n>0 && (n % opt.groupUnit) == 0){
                jGroup = $('<div class="timelist-group">').appendTo(jScopeInner);
            }
            // 创建项目
            var jItem = $('<div class="timelist-item">').appendTo(jGroup);
            jItem.attr("sec",   _t.sec)
                .attr("tkey",   _t.key)
                .attr("hour",   _t.H)
                .attr("minute", _t.m)
                .attr("second", _t.s)
                .text(_t.key_min);

            // 下一个时间点
            sBegin += sUnit;
            n++;
        }
    }

    // 保存配置
    options(jRoot, opt);

    if(opt.className)
        jRoot.addClass(opt.className);

    return jRoot;
}
//...........................................................
$.fn.extend({ "timelist" : function(opt, arg0, arg1){
    // 命令
    if(_.isString(opt)){
        return commands[opt].call($root(this), arg0, arg1);
    }
    // 默认配置必须为对象
    opt = $z.extend({}, {
        width     : "auto",          
        height    : "auto",
        className : "skin-light",
        mode      : "default",
        multi     : true,
        display   :  "horizontal",
        timeUnit  : 60,
        groupUnit : 1,
        scopes    : [["00:00", "24:00"]],
    }, opt);

    // 重绘基础 dom 结构，同时这会保存配置信息
    var jRoot = redraw(this, opt);

    // 绘制 dom
    bindEvents(jRoot, opt);

    // 更新
    if(opt.date)
        update(jRoot, opt.date);

    // 如果有必要，标记一些不可用的项目
    if(opt.disabled)
        set_disable(jRoot, opt, opt.disabled);

    // 返回自身以便链式赋值
    return this;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);




