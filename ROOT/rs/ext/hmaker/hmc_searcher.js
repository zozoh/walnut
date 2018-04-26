/**
Searcher 控件的运行时行为:

opt : {
    // 来自编辑器
    "placeholder"  : "请输入关键字",
    "defaultValue" : "搜索框默认值",
    "btnText"      : "立即搜索",
    "trimSpace"    : true,
    "maxLen"       : 23,

    // 下面由 IDE 指定
    forIDE : true,
    
}
*/
(function($, $z){
//...........................................................
var normalize_val_by_input = function(jInput, val) {
    var trimSpace = jInput.attr("trim-space");
    var maxLen = jInput.attr("maxlength") * 1;
    val = jInput.val();
    if(trimSpace){
        val = $.trim(val);
    }
    if(maxLen > 0 && val.length > maxLen){
        val = val.substring(0, maxLen);
    }
    return val;
};
//...........................................................
// 执行搜索
var do_search = function(opt, jq) {
    // 提交到某个页面
    if(opt.postAction) {
        var jInput = jq.find(".kwd-input input");

        var url = window.__ROOT_PATH;
        if(!url || /\/$/.test(url))
            url += opt.postAction.substring(1) + ".html";
        else
            url += opt.postAction + ".html";
        
        var target = opt.postTarget || "_blank";
        var key = opt.postParamName || "k";
        var val = jInput.val();
        if(opt.trimSpace) {
            val = $.trim(val);
        }

        //console.log(target)
        $z.openUrl(url, target, "GET", $z.obj(key, val));
    }
    // 默认刷新与自己关联的动态数据控件
    else {
        HmRT.invokeDynamicReload(jq, true);
    }
};
//...........................................................
// 显示提示
var do_show_tip = function(opt, jq, str) {
    var apiUrl = opt.API + opt.tipApi;
    // 当本机测试环境下，修改 apiUrl
    if(opt.apiDomain && /^https?:\/\/(localhost|127.0.0.1)/.test(window.location.href)){
        apiUrl = "/api/" + opt.apiDomain + opt.tipApi;
    }
    // 准备参数
    var params = _.extend({k:str||""}, opt.tipParams);
    // 发送请求
    $.get(apiUrl, params, function(re){
        // 拆分行
        var trim = $.trim(re) || "";
        var lines = trim.split(/ *\n+ */g);
        if(!trim || lines.length == 0) {
            do_hide_tip(opt, jq);
            return;
        }
        // 得到要绘制的列表DOM
        var jUl = jq.find('.search-tip').empty();
        if(jUl.length == 0) {
            jUl = $('<ul class="search-tip">').appendTo(jq);
        }
        // 停靠
        var jInput = jq.find(".kwd-input input");
        $z.dock(jInput, jUl, "H");
        jUl.css({
            'min-width': jInput.outerWidth(),
        });
        // 逐行绘制
        var str_low = str.toLowerCase();
        for(var i=0; i<lines.length; i++) {
            var line = lines[i];
            var pos  = line.toLowerCase().indexOf(str_low);
            var html = line;
            if(pos >= 0) {
                html = line.substring(0, pos) 
                       + '<em>' + line.substring(pos, pos+str.length) + '</em>' 
                       + line.substring(pos+str.length);
            }
            $('<li>').html(html).appendTo(jUl);
        }
    });
};
//...........................................................
// 隐藏提示
var do_hide_tip = function(opt, jq) {
    jq.find('.search-tip').remove();
    jq.removeAttr("old-kwd");
    jq.removeAttr("last-ms");

    // 清除监控
    var HDL = jq.data("@HDL");
    if(HDL) {
        window.clearInterval(HDL);
        jq.removeData("@HDL");
    }
};
//...........................................................
// 处理提示时间，基本策略是当用户输入关键词，冷却500ms以上才会发出查询请求
var on_show_tip = function(opt, jq) {
    // 没指定 tipApi， 无视
    if(!opt.tipApi || !opt.tipParams || !opt.tipParams.pid)
        return;

    // 看看有没有冷却
    var lastMs = parseInt(jq.attr("last-ms"));
    var nowMs  = Date.now();
    // 更新时间
    jq.attr("last-ms", nowMs);
    //console.log("on_show_tip", nowMs - lastMs);
    if(isNaN(lastMs) || (nowMs - lastMs)<300) {
        // 确保监视回调在运行
        var HDL = jq.data("@HDL");
        if(!HDL) {
            HDL = window.setInterval(on_show_tip, 300, opt, jq);
            jq.data("@HDL", HDL);
        }
        return;
    }

    // 得到搜索关键字
    var str = $.trim(jq.find('.kwd-input input').val());
    var old = jq.attr("old-kwd");
    //console.log("do tip", str, old, str != old);

    // 显示
    if(str) {
        if(str != old) {
            jq.attr("old-kwd", str);
            do_show_tip(opt, jq, str);
        }
    }
    // 隐藏
    else {
        do_hide_tip(opt, jq);
    }
};
//...........................................................
// 命令模式
var CMD = {
    value : function(val) {
        var jInput = this.find("input");
        // 获取
        if(_.isUndefined(val)){
            return normalize_val_by_input(jInput, jInput.val());
        }
        // 设置
        jInput.val(normalize_val_by_input(jInput, val));
        // 返回
        return this;
    }
}
//...........................................................
$.fn.extend({ "hmc_searcher" : function(opt){
    // 命令模式
    if(_.isString(opt)) {
        var args = $z.toArgs(arguments);
        return CMD[opt].apply(this, args.slice(1));
    }
    
    // 得到自己所在控件
    var jq = this.empty();

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 准备一下获取值
    var kwd = opt.defaultValue || "";
    var m = /^@<([^>]+)>$/.exec(kwd);
    if(m) {
        kwd = (window.__REQUEST || {})[m[1]] || "";
    }
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 开始绘制
    var jInput = $('<div class="kwd-input"><input></div>')
                    .appendTo(jq)
                        .find("input");

    jInput.attr({
        "placeholder" : opt.placeholder || null,
        "maxlength"   : opt.maxLen > 0 ? opt.maxLen : null,
        "trim-space"  : opt.trimSpace ? "yes" : null,
        "spellcheck"  : false,
    }).val(kwd);

    // 按钮文字
    var jBtnW  = $('<div class="kwd-btn"><b></b></div>').appendTo(jq);
    var jB = jBtnW.show().children("b");
    jB.html(opt.btnText || '<i class="fa fa-search"></i>');

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 监控事件
    if(!opt.forIDE) {
        // 回车表示搜索
        jq.on("keydown", ".kwd-input input", function(e){
            if(13 == e.which) {
                do_search(opt, jq);
            }
        });
        // 点击搜索按钮
        jq.on("click", ".kwd-btn", function(){
            if($.trim(jInput.val())) {
                do_search(opt, jq);
            }
        });
        // 输入提示信息(完美检测中文输入)
        jq.on("compositionstart", ".kwd-input input", function(e){
            this.__compositionstart = true;
        });
        jq.on("compositionend", ".kwd-input input", function(e){
            this.__compositionstart = false;
            on_show_tip(opt, jq);
        });
        jq.on("input", ".kwd-input input", function(e){
            if(this.__compositionstart)
                return;
            on_show_tip(opt, jq);
        });
        // jq.on("blur", ".kwd-input input", function(e){
        //     window.setTimeout(function(){
        //         do_hide_tip(opt, jq);
        //     }, 500);
        // });
        // 点击选择输入
        jq.on("click", ".search-tip li", function(e){
            jInput.val($(this).text());
            do_hide_tip(opt, jq);
        });
        // 上下方向键
        jq.on("keydown", ".kwd-input input", function(e){
            // 回车
            if(13 == e.which) {
                e.preventDefault();
                var jLi = jq.find(".search-tip li[current]");
                if(jLi.length>0) {
                    jInput.val(jLi.text());
                    do_hide_tip(opt, jq);
                    do_search(opt, jq);
                }
            }
            // 上箭头
            else if(38 == e.which) {
                var jUl = jq.find(".search-tip");
                var jLi = jUl.find("li[current]");
                if(jLi.length == 0) {
                    jLi = jq.find(".search-tip li").last();
                } else {
                    jLi = jLi.prev();
                }
                if(jLi.length > 0) {
                    jUl.find("li[current]").removeAttr("current");
                    jLi.attr("current", "yes");
                }
            }
            // 下箭头
            else if(40 == e.which) {
                var jUl = jq.find(".search-tip");
                var jLi = jUl.find("li[current]");
                if(jLi.length == 0) {
                    jLi = jq.find(".search-tip li").first();
                } else {
                    jLi = jLi.next();
                }
                if(jLi.length > 0) {
                    jUl.find("li[current]").removeAttr("current");
                    jLi.attr("current", "yes");
                }
            }
        });
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 返回自身以便链式赋值
    return jq;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

