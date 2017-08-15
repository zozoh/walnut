(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena tabs" ui-fitparent="yes">
<header><ul class="tabs-list"></ul></header>
<section><div ui-gasket="main"></div></section>
</div>
*/};
//==============================================
return ZUI.def("ui.tabs", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/tabs/theme/tabs-{{theme}}.css",
    //..............................................
    events : {
        'click ul.tabs-list > li.tabs-li' : function(e){
            var UI  = this;
            var jLi = $(e.currentTarget);
            UI.changeUI(jLi.attr("key"));
        }
    },
    //..............................................
    redraw : function() {
        var UI = this;
        var opt = UI.options;
        var jUl = UI.arena.find(">header>ul").empty();

        // 设置显示模式
        UI.setMode();

        // 绘制标签
        var firstKey;
        for(var key in opt.setup) {
            // 记录第一个 Key
            if(!firstKey)
                firstKey = key;
            // 准备 <li>
            var tli = opt.setup[key];
            var jLi = $('<li class="tabs-li">').attr("key", key)
                        .appendTo(jUl);

            // 图标
            if(tli.icon) {
                $('<span class="tabs-li-icon">')
                    .html(tli.icon).appendTo(jLi);
            }

            // 文字
            $('<span class="tabs-li-text">')
                .html(tli.text || key).appendTo(jLi);
        }

        // 寻找第一个显示的标签
        var showKey = opt.defaultKey || firstKey;
        if(_.isFunction(showKey)){
            showKey = showKey.call(UI);
        }
        // 默认用第一个 Key
        UI.changeUI(showKey, function(){
            UI.defer_report("show");
        });

        // 返回延迟加载
        return ["show"];
    },
    //..............................................
    getCurrentUI : function(){
        return this.gasket.main;
    },
    //..............................................
    getCurrentKey : function(){
        return this.arena.find('>header .tabs-li[current]').attr("key");
    },
    //..............................................
    isCurrent : function(key) {
        return (key||"") == this.getCurrentKey();
    },
    //..............................................
    hasUIKey : function(key) {
        return this.options.setup[key] ? true : false;
    },
    //..............................................
    changeUI : function(key, callback, quiet) {
        // 支持 changeUI(key, true) 这样的形式
        if(_.isBoolean(callback)) {
            quiet = callback;
            callback = null;
        }
        var UI  = this;
        var opt = UI.options;
        var tli = opt.setup[key];
        var jUl = UI.arena.find(">header>ul");
        if(!tli)
            throw "tabs key [" + key + "] wihtout defined!!!";
        var jLi = UI.arena.find('>header .tabs-li[key="'+key+'"]');
        var prevUI = UI.getCurrentUI();

        // 如果已经是当前的，无视
        if(jLi.attr("current")){
            $z.doCallback(callback, [prevUI, prevUI], UI);
            return;
        }

        // 执行切换
        jUl.children('[current]').removeAttr("current");
        jLi.attr("current", "yes");

        // 加载子UI
        var uiType = $z.evalObjValue(tli.uiType, [key], UI);
        var uiConf = $z.evalObjValue(tli.uiConf, [key], UI) || {};
        seajs.use(uiType, function(SubUI){
            new SubUI(_.extend({}, uiConf, {
                parent : UI,
                gasketName : "main"
            })).render(function(){
                // 回调事件
                if(!quiet)
                    $z.invoke(opt, "on_changeUI", [key, this, prevUI], UI);
                // 调用回调
                $z.doCallback(callback, [this, prevUI]);
            });
        });
    },
    //..............................................
    setMode : function(mode) {
        var UI = this;
        var opt = UI.options;
        UI.arena.attr("mode", mode || opt.mode || "top");
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);