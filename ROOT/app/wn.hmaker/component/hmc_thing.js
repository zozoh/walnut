(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker/component/hmc',
    'ui/menu/menu',
    'ui/form/form'
], function(ZUI, Wn, HMC, MenuUI, FormUI){
//==============================================
var html = function(){/*
<b> I am thing</b>
*/};
//==============================================
return ZUI.def("app.wn.hmaker_com_thing", {
    //...............................................................
    events : {
        
    },
    //...............................................................
    updateStyle : function(info){
        var UI = this;
        var ID = UI.$el.prop("id");

        // 先过滤一遍通用规则
        var styleRules = [];
        info = UI.parent.gen_rules(ID, styleRules, info);

        // 再弄一下自己的规则
        var rule = {
            selector : "#"+ID+" .hmc-main",
            items    : []
        };
        for(var key in info){
            if("ID" == key)
                continue;
            var val = info[key];
            var ru  = UI.parent.gen_rule_item(key, val);
            rule.items.push(ru);
        }
        styleRules.push(rule);

        // 应用样式规则
        UI.parent.updateComStyle(UI.$el, styleRules);
    },
    //...............................................................
    checkDom : function(){
        var UI = this;
        var jM = UI.arena.find(".hmc-main");
        jM.html($($z.getFuncBodyAsStr(html,true)));
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 确保 DOM 结构合法
        UI.checkDom();

        // 标题
        opt.$title.html(opt.titleHtml);

        // 获得属性
        var info = UI.parent.getComponentInfo(UI.$el);

        new FormUI({
            $pel   : opt.$prop,
            fields : [opt.propSetup, {
                title  : 'i18n:hmaker.cprop_special',
                fields : []
            }],
            on_change : function(key, val) {
                // console.log("detect form change: ", key, val);
                UI.setProperty(key, val);
            }
        }).render(function(){
            //console.log(this.parent.uiName);
            this.setData(info);
        });

        // 菜单
        new MenuUI({
            $pel   : opt.$menu,
            setup  : []
        }).render(function(){
            //console.log(this.parent.uiName);
        });
        
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);