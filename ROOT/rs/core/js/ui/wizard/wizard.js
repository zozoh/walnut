(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena wizard" ui-fitparent="yes">
    <div class="wz-title"></div>
    <header><ul></ul></header>
    <section ui-gasket="main"></section>
    <footer></footer>
</div>
*/};
//===================================================================
return ZUI.def("ui.wizard", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/wizard/theme/wizard-{{theme}}.css",
    //...............................................................
    init : function(opt){
        var UI = this;

        // 设置默认值
        $z.setUndefined(opt, "headMode", "single");
        $z.setUndefined(opt, "steps", {});

        // 默认选第一个界面作为起始页
        if(!opt.startPoint) {
            for(var key in opt.steps){
                opt.startPoint = key;
                break;
            }
        }

        // 准备步骤的堆栈
        UI.__steps = [];

        // 设置默认数据
        UI.setData(opt.data);

    },
    //...............................................................
    events : {
        // 点击上一步
        'click .wizard > footer > a[md="prev"]' : function(e){
            this.gotoStep(-1);
        },
        // 点击下一步
        'click .wizard > footer > a[md="next"]' : function(e){
            var UI   = this;
            var opt  = UI.options;
            var step = UI.getCurrentStep();
            var data = UI.getData();
            var context = opt.context || UI;
            var stepKey;

            // 直接跳转到下一步
            if(_.isString(step.jumpTo)){
                stepKey = step.jumpTo;
            }
            // 计算下一步
            else if(_.isFunction(step.jumpTo)){
                stepKey = step.jumpTo.call(context, data, UI);
            }

            // 报错
            if(!stepKey) {
                alert("no nextStep in step: " + $z.toJson(step));
                return;
            }

            // 保存数据
            UI.saveData();

            // 执行跳转
            UI.gotoStep(stepKey);
        },
        // 点击完成
        'click .wizard > footer > a[md="done"]' : function(e){
            var UI   = this;
            var opt  = UI.options;
            var data = UI.getData();
            var context = opt.context || UI;
            var step = UI.getCurrentStep();

            // 保存数据
            UI.saveData();
            
            // 执行回调
            $z.invoke(step, "done",    [data, UI], context);
            $z.invoke(opt,  "on_done", [data, UI], context);
        },
    },
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = this.options;
        var jUl = UI.arena.find(">header>ul");

        // 设置数据
        UI.setData(opt.data);

        // 标记模式
        UI.arena.attr("head-mode", opt.headMode);

        // 设置标题
        if(opt.title) {
            UI.arena.find(">.wz-title").html(UI.text(opt.title));
        }else{
            UI.arena.find(">.wz-title").remove();
        }

        // 预先处理所有步骤
        for(var key in opt.steps) {
            var step = opt.steps[key];

            // 为步骤设置标题
            var jLi  = $('<li>').attr("w-key", key);
            if(step.icon) {
                $('<span class="wzh-icon">').html(step.icon)
                    .appendTo(jLi);
            }
            if(step.text) {
                $('<span class="wzh-text">').text(UI.text(step.text))
                    .appendTo(jLi);
            }
            if(!step.icon && !step.text) {
                $('<span class="wzh-text">').text(key).appendTo(jLi);
            }
            jLi.appendTo(jUl);
        }
        
        // 显示起始页
        if(opt.startPoint){
            UI.gotoStep(opt.startPoint, function(){
                UI.defer_report("start");
            });
            return ["start"];
        }
    },
    //...............................................................
    // 保存当前步骤的数据
    saveData : function() {
        var UI = this;
        if(UI.gasket.main) {
            var d = $z.invoke(UI.gasket.main, "getData", []);
            if(d)
                _.extend(UI.__data, d);
        }
    },
    //...............................................................
    // stepKey 为 -1 表示弹出
    gotoStep : function(stepKey, callback) {
        var UI  = this;
        var opt = this.options;
        var jUl = UI.arena.find(">header>ul");
        var jFooter = UI.arena.find(">footer");
        var context = opt.context || UI;
        var data = UI.getData();

        // 得到操作步骤
        var step;
        // 弹出
        if(-1 == stepKey) {
            step = UI.__steps.pop();
        }
        // 压入堆栈
        else {
            step = opt.steps[stepKey];
            if(!step) {
                alert("!no step in wizard: " + stepKey);
                $z.doCallback(callback, [data, UI], context);
                return;
            }
            UI.__steps.push(step);
        }

        // 标记头部
        jUl.children().removeAttr("current")
            .filter('[w-key="'+stepKey+'"]').attr("current", "yes");

        // 准备标记按钮
        jFooter.empty();

        // 不是最后一步的话，就有上一步/下一步的按钮
        if(step.action) {
            // 是最后一步
            if(step.action.done) {
                var jBtn = $('<a m="done">').appendTo(jFooter);
                $('<span>').text(UI.text("i18n:done")).appendTo(jBtn);
            }
            // 中间步骤的话 ...
            else if(step.action.jumpTo){
                // 显示上一步的按钮
                if(UI.__last_step) {
                    $('<a m="prev">')
                        .append('<i class="zmdi zmdi-chevron-left"></i>')
                        .append($('<span>').text(UI.text("i18n:prev")))
                            .appendTo(jFooter);
                }

                // 显示下一步
                var jBtn = $('<a m="next">').appendTo(jFooter);
                if(step.action.icon) {
                    jBtn.html(step.action.icon);
                }
                var btnText  = UI.text(step.action.text || "i18n:next");
                $('<span>').text(btnText).appendTo(jBtn);
            }
        }

        // 显示界面
        seajs.use(step.uiType, function(TheUI){
            new TheUI(_.extend({}, step.uiConf, {
                parent : UI,
                gasketName : "main",
                context : context,
            })).render(function(){
                // 设置数据
                this.setData(data);

                // 确保重新调整尺寸
                window.setTimeout(function(){
                    UI.gasket.main.resize();
                }, 0);

                // 调用回调
                $z.doCallback(callback, [data, UI], context);
            });
        });
    },
    //...............................................................
    checkNextBtnStatus : function(){
        var UI = this;
    },
    //...............................................................
    getCurrentStep : function(){
        if(this.__steps.length > 0)
            return this.__steps[this.__steps.length - 1];
        return null;
    },
    //...............................................................
    getData : function() {
        return this.__data;
    },
    //...............................................................
    setData : function(data) {
        this.__data = _.extend({}, data);
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);