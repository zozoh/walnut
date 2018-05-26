(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/c_input',
    'ui/form/form',
], function(ZUI, Wn, InputUI, FormUI){
//==============================================
var html = `
<div class="ui-arena edit-link-action" ui-fitparent="yes">
    <header  ui-gasket="actions"></header>
    <section ui-gasket="params"></section>
</div>`;
//==============================================
return ZUI.def("ui.edit_link_action", {
    dom  : html,
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        // 显示列表
        new InputUI({
            parent : UI,
            gasketName  : 'actions',
            placeholder : 'i18n:hmaker.link.ac.select',
            on_change : function(val) {
                UI.__show_action_params(val);
            },
            assist : {
                icon : '<i class="zmdi zmdi-settings"></i>',
                uiType : "ui/form/c_list",
                uiConf : {
                    items : opt.actionList,
                    escapeHtml : false,
                    icon  : '<i class="zmdi zmdi-flash"></i>',
                    text  : function(ai){
                        return '<span>' + UI.text(ai.text) + '</span>'
                                + '<em>' + ai.name + '</em>';
                    },
                    value : function(ai) {
                        return ai.name;
                    }
                }
            }
        }).render(function(){
            UI.defer_report('main');
        });

        // 返回延迟加载
        return ["main"];
    },
    //...............................................................
    getActionInfo : function(actionName) {
        var UI  = this;
        var opt = UI.options;

        actionName = $.trim(actionName);

        if(!actionName)
            return null;

        if(_.isArray(opt.actionList) && opt.actionList.length > 0) {
            for(var i=0; i<opt.actionList.length; i++) {
                var ai = opt.actionList[i];
                if(ai.name == actionName)
                    return ai;
            }
        }

        return null;
    },
    //...............................................................
    __show_action_params : function(actionName, args) {
        var UI = this;
        var ai = UI.getActionInfo(actionName);
        var jPa = UI.arena.find('>section');

        // 首先取消表单
        if(UI.gasket.params)
            UI.gasket.params.destroy();
        jPa.empty();

        // 显示对应的表单
        if(ai) {
            if(_.isArray(ai.params) && ai.params.length > 0) {
                // 根据参数数组获取参数对象
                var params = {};
                if(_.isArray(args) && args.length>0) {
                    var len = Math.min(ai.params.length, args.length);
                    for(var i=0; i<len; i++) {
                        var pa = ai.params[i];
                        params[pa.key] = args[i];
                    }
                }
                // 初始化表单，并设置值
                new FormUI({
                    parent : UI,
                    gasketName : "params",
                    title : 'i18n:hmaker.link.ac.pa_title',
                    displayMode : "compact",
                    mergeData : false,
                    uiWidth   : "all",
                    fields : ai.params
                }).render(function(){
                    this.setData(params);
                });
            }
            // 动作不需要参数
            else {
                $('<div class="ai-tip">').text(UI.msg('hmaker.link.ac.noparams'))
                    .appendTo(jPa);    
            }
        }
        // 自定义的未知动作
        else if(actionName) {
            $('<div class="ai-tip">').text(UI.msg('hmaker.link.ac.unknownact'))
                .appendTo(jPa);
        }
        // 没有动作
        else {
            $('<div class="ai-tip">').text(UI.msg('hmaker.link.ac.noact'))
                .appendTo(jPa);
        }
    },
    //...............................................................
    getData : function() {
        var UI = this;

        // 得到动作名
        var actionName = $.trim(UI.gasket.actions.getData());
        if(!actionName)
            return null;

        // 准备调用参数
        var args = [];

        // 得到动作参数
        if(UI.gasket.params){
            var params = UI.gasket.params.getData();

            // 得到动作配置信息
            var ai = UI.getActionInfo(actionName);

            // 根据配置获取参数数组
            if(params && ai && _.isArray(ai.params) && ai.params.length>0) {
                for(var i=0; i<ai.params.length; i++) {
                    var pa  = ai.params[i];
                    var val = params[pa.key];
                    args.push($z.toJson(val));
                }
            }
        }

        // 拼装成调用函数并返回
        var re = "javascript:" + actionName;
        if(actionName.indexOf('(') < 0)
            re += '(' + args.join(',') + ')';
        console.log(re);

        // 返回
        return re;
    },
    //...............................................................
    /*
     - data 格式为:
    {
        invoke : "$z.xxx",
        args   : [..],
    }
    */
    setData : function(data) {
        var UI = this;
        // 有数据
        if(data && data.invoke) {
            var ai = UI.getActionInfo(data.invoke);
            // 有标准动作
            if(ai) {
                UI.gasket.actions.setData(data.invoke);
                UI.__show_action_params(data.invoke, data.args);
            }
            // 自定义动作
            else {
                var call = data.invoke + '(' + (data.args||[]).join(',') + ')';
                UI.gasket.actions.setData(call);
                UI.__show_action_params(null);   
            }
        }
        // 清空数据
        else {
            UI.gasket.actions.setData("");
            UI.__show_action_params(null);
        }
    },
    //...............................................................
    resize : function() {
        var UI = this;
        var jHe = UI.arena.find('>header');
        var jPa = UI.arena.find('>section');
        jPa.css({
            'height' : UI.arena.height() - jHe.outerHeight()
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);