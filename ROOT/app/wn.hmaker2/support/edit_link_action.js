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
    __show_action_params : function(actionName) {
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
                new FormUI({
                    parent : UI,
                    gasketName : "params",
                    title : 'i18n:hmaker.link.ac.pa_title',
                    displayMode : "compact",
                    mergeData : false,
                    uiWidth   : "all",
                    fields : ai.params
                }).render(function(){
                    this.setData({});  // 确保设置了默认值
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
        
    },
    //...............................................................
    setData : function(href) {
        var UI = this;
        UI.__show_action_params(null);
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