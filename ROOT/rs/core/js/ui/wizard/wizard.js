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

        // 预先处理所有步骤
        UI.__steps = [];
        var i = 0;
        for(var key in opt.steps) {
            var step = opt.steps[key];

            // 记录本身的 Key
            step.key   = key;
            step.index = i++;

            // 格式三个按钮
            UI.__normalize_step_btn(step, "prev", true);
            UI.__normalize_step_btn(step, "next");
            UI.__normalize_step_btn(step, "done");

            // 加入列表
            UI.__steps.push(step);
        }

        // 标记当前步骤 
        UI.__index = -1;

        // 设置默认数据
        UI.setData(opt.data);

    },
    //...............................................................
    __normalize_step_btn : function(step, btnKey, dft) {
        var btn = step[btnKey];
        if(_.isUndefined(btn))
            btn = dft;
        if(!_.isUndefined(btn)) {
            if(!btn.action){
                step[btnKey] = $z.obj("action", btn);
            }
            // console.log(btnKey, dft, step)
        }
    },
    //...............................................................
    events : {
        // 点击底部按钮
        'click .wizard > footer > b[enabled]' : function(e){
            var UI   = this;
            var opt  = UI.options;
            var jB   = $(e.currentTarget);
            var step = UI.getCurrentStep();
            var data = UI.getData();
            var context = opt.context || UI;
            var btnMode = jB.attr("m");

            // 下一步
            if("next" == btnMode) {
                // 保存数据
                UI.saveData();

                // 寻找下一步跳转的目标
                var stepKey = UI.__get_target_stepKey(step.next, 1);

                // 执行跳转
                UI.gotoStep(stepKey);
            }
            // 上一步
            else if("prev" == btnMode) {
                // 寻找下一步跳转的目标
                var stepKey = UI.__get_target_stepKey(step.prev, -1);

                // 执行跳转
                UI.gotoStep(stepKey);
            }
            // 完成
            else if("done" == btnMode) {
                // 保存数据
                UI.saveData();
                // 执行回调
                $z.invoke(step.done, "action", [data, UI], context);
                $z.invoke(opt,  "on_done", [data, UI], context);
            }
        },
    },
    //...............................................................
    __get_target_stepKey : function(stepBtn, dftStepKey){
        var UI  = this;
        var opt = UI.options;
        var data = UI.getData();
        var context = opt.context || UI;

        if(_.isString(stepBtn.action)){
            return stepBtn.action;
        }
        // 计算下一步
        if(_.isFunction(stepBtn.action)){
            return stepBtn.action.call(context, data, UI);
        }
        // 直接跳转的步数
        if(_.isNumber(stepBtn.action)){
            return stepBtn.action;
        }
        // 返回默认跳转
        return dftStepKey;
    },
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = this.options;
        var jUl = UI.arena.find(">header>ul").empty();

        // 标记模式
        UI.arena.attr("head-mode", opt.headMode);

        // 设置标题
        if(opt.title) {
            UI.arena.find(">.wz-title").html(UI.text(opt.title));
        }else{
            UI.arena.find(">.wz-title").remove();
        }

        // 预先处理所有步骤
        for(var i=0; i<UI.__steps.length; i++) {
            var step = UI.__steps[i];

            // 为步骤设置标题
            var jLi  = $('<li>').attr("w-key", step.key);
            
            // 步骤图标
            $('<span class="wzh-icon">').html(step.icon || (step.index+1))
                .appendTo(jLi);
            
            // 步骤文字
            $('<span class="wzh-text">').text(UI.text(step.text || step.key))
                .appendTo(jLi);
            
            // 加入列表
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
    // stepKey : 1 表示下一步，-1 表示上一步，字符串表示跳转到某步骤的键值
    // 
    gotoStep : function(stepKey, callback) {
        var UI  = this;
        var opt = this.options;
        var jUl = UI.arena.find(">header>ul");
        var jFooter = UI.arena.find(">footer");
        var context = opt.context || UI;
        var data = UI.getData();

        //console.log("goto", stepKey, data);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``
        // 切换当前步骤下标
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``
        // 非零数字表示偏移量
        if(stepKey && _.isNumber(stepKey)) {
            UI.__index += stepKey;
        }
        // 字符串表示直接跳转的步骤
        else if(_.isString(stepKey)){
            UI.__index = -1;
            for(var i=0; i<UI.__steps.length; i++) {
                var sp = UI.__steps[i];
                if(sp.key == stepKey){
                    UI.__index = sp.index;
                    break;
                }
            }
        }
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``
        // 得到当前操作步骤对象
        var step = UI.getCurrentStep();

        //console.log("get step", step);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``
        // 标记头部
        var jLis = jUl.children().removeAttr("md");
        for(var i=0; i<UI.__steps.length; i++) {
            var jLi = jLis.eq(i);
            if(i < UI.__index)
                jLi.attr("md", "done");
            else if(i == UI.__index)
                jLi.attr("md", "current");
            else
                break;
        }
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``
        // 准备标记按钮
        jFooter.empty();
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``
        // 弄一下按钮
        var noDoneBtn = true;
        var noPrevBtn = true;
        var noNextBtn = true;
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``
        // 是最后一步
        if(step.done) {
            noDoneBtn = false;
            var jBtn = $('<b m="done" enabled="yes">').appendTo(jFooter);
            $('<span>').text(UI.text("i18n:done")).appendTo(jBtn);
        }
        // 中间步骤的话 ...
        else {
            // 显示上一步的按钮
            //console.log(step)
            if(step.index > 0 && step.prev.action) {
                noPrevBtn = false;
                var jBtn = $('<b m="prev" enabled="yes">').appendTo(jFooter);
                // 指示符
                $('<span class="btn-di">')
                    .html('<i class="zmdi zmdi-chevron-left"></i>')
                        .appendTo(jBtn);
                // 图标
                if(step.prev.icon) {
                    $('<span class="btn-icon">')
                        .html(step.prev.icon)
                            .appendTo(jBtn);
                }
                // 文字
                var btnText  = UI.text(step.next.text || "i18n:prev");
                $('<span class="btn-text">')
                    .text(btnText)
                        .appendTo(jBtn);
            }
            // 显示下一步的按钮
            if(step.next.action){
                noNextBtn = false;
                var jBtn = $('<b m="next">').appendTo(jFooter);
                // 图标
                if(step.next.icon) {
                    $('<span class="btn-icon">')
                        .html(step.next.icon)
                            .appendTo(jBtn);
                }
                // 文字
                var btnText  = UI.text(step.next.text || "i18n:next");
                $('<span class="btn-text">')
                    .text(btnText)
                        .appendTo(jBtn);
                // 指示符
                $('<span class="btn-di">')
                    .html('<i class="zmdi zmdi-chevron-right"></i>')
                        .appendTo(jBtn);
            }
        }
        // 如果没有按钮，隐藏底栏
        if(noPrevBtn && noNextBtn && noDoneBtn){
            jFooter.hide();
        }else{
            jFooter.show();
        }
        // 设置完毕以后重新改变一下尺寸
        UI.resize();
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``
        // 显示界面
        seajs.use(step.uiType, function(TheUI){
            new TheUI(_.extend({}, step.uiConf, {
                parent : UI,
                gasketName : "main",
                context : context,
            })).render(function(){
                // 设置数据
                $z.invoke(this, "setData", [data]);

                // 确保重新调整尺寸
                window.setTimeout(function(){
                    UI.gasket.main.resize();
                }, 0);

                // 确保下一步按钮状态被更新
                UI.checkNextBtnStatus();

                // 调用回调
                $z.doCallback(callback, [data, UI], context);
            });
        });
    },
    //...............................................................
    checkNextBtnStatus : function(){
        var UI = this;
        var jB = UI.arena.find('> footer > b[m="next"]');

        // 看看按钮状态
        if(jB.length > 0 
           && UI.gasket.main 
           && $z.invoke(UI.gasket.main, "isDataReady",[])) {
            jB.attr("enabled", "yes");
        }
        // 按钮灰掉
        else {
            jB.removeAttr("enabled");
        }
    },
    //...............................................................
    getCurrentStep : function(){
        if(this.__index < 0) {
            alert("!no step in wizard: " + this.__index);
            return;
        }
        return this.getStep(this.__index);
    },
    //...............................................................
    // index 下标。 -1 表示最后一个，-2 表示倒数第二个，以此类推。
    //       大于等于 0 的，直接就是下标
    getStep : function(index){
        if(index < 0)
            index += this.__steps.length;
        if(index >= 0){
            return index<this.__steps.length ? this.__steps[index] : null;
        }
        return null;
    },
    //...............................................................
    getStepByOffset : function(off){
        return this.getStep(this.__index + off);
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
    resize : function() {
        var UI = this;
        var jH = UI.arena.find('>header');
        var jS = UI.arena.find('>section');
        var jF = UI.arena.find('>footer');

        if(UI.isFitParent()) {
            var H  = UI.arena.height();
            var hh = jH.outerHeight(true);
            var hf = jF.outerHeight(true);
            jS.css('height', H-hh-hf);
        }
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);