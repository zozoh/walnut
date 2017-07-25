(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_ctrl'
], function(ZUI, FormMethods){
//==============================================
var html = function(){/*
<div class="ui-arena com-input">
    <div class="box">
        <input type="text" spellcheck="false">
        <span class="unit"></span>
    </div>
    <div class="ass"></div>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_com_input", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function(opt){
        var UI = FormMethods(this);

        // 默认值
        $z.setUndefined(opt, "trimData", true);

        // 监控 Esc 事件
        UI.watchKey(27, function(e){
            UI.closeAssist();
        });
    },
    //...............................................................
    events : {
        // 输入内容修改
        "change > .com-input > .box > input" : function(){
            this.__on_change();
        },
        // 打开辅助框
        "click > .com-input > .ass" : function(){
            this.openAssist();
        },
        // 关闭辅助框
        "click > .com-input > .ass-mask" : function(){
            this.closeAssist();
        }
    },
    //...............................................................
    redraw : function(){
        var UI     = this;
        var opt    = UI.options;
        var jUnit  = UI.arena.find(".unit");
        var jInput = UI.arena.find("input");

        // 声明了单位，显示一下
        UI.setUnit(opt.unit);

        // 占位符显示
        UI.setPlaceholder(opt.placeholder);

        // 助理
        UI.setAssist(opt.assist);
    },
    //...............................................................
    openAssist : function(){
        var UI  = this;
        var opt = UI.options;

        if(!UI._assist)
            return;

        // 得到数据
        var val = UI._get_data();

        // 计算助理的配置
        var uiType = UI._assist.uiType;
        var uiConf = UI._assist.uiConf || {};

        // uiType 无效
        if(!uiType)
            return;

        // 显示出辅助弹出遮罩
        $('<div class="ass-mask">').appendTo(UI.arena);
        var jAssBox = $('<div class="ass-box">').data({
            "old-val" : val||""
        }).css({
            "visibility" : "hidden",
            "position"   : "fixed",
        }).appendTo(UI.arena);
        seajs.use(uiType, function(AssUI){
            new AssUI({
                $pel : jAssBox,
                on_change : function(v) {
                    UI._set_data(v);
                }
            }).render(function(){
                // 显示
                jAssBox.css("visibility", "");

                // 设置值
                this.setData(val);

                // 移动位置
                $z.dock(UI.arena.find(">.box"), jAssBox, "H");
            });
        });
    },
    //...............................................................
    closeAssist : function() {
        var UI  = this;
        var jAssBox = UI.arena.find('> .ass-box');

        // 已经打开了
        if(jAssBox.length > 0) {
            // 得到新老值
            var oldVal = jAssBox.data("old-val");
            var newVal = UI._get_data();

            // 移除遮罩
            var uiAss = ZUI(jAssBox.children().attr("ui-id"));
            if(uiAss)
                uiAss.destroy();
            jAssBox.remove();
            UI.arena.find("> .ass-mask").remove();
            
            // 通知改动
            if(oldVal != newVal) {
                UI.__on_change();
            }
        }
    },
    /*...............................................................
    assist : {
        icon : '<..>',
        text : "i18n:xxx",
        uiType : "xxx",
        uiConf : {..}    
    }
    */
    setAssist : function(ass) {
        var UI   = this;
        var jAss = UI.arena.find(">.ass").empty();

        // 记录当前助理
        UI._assist = ass;

        // 设置属性开关
        UI.arena.attr("show-ass", ass ? "yes" : null);

        // 有助理
        if(ass) {
            // 有图标
            if(ass.icon){
                $(ass.icon).appendTo(jAss);
                if(ass.text) {
                    $('<b>').text(UI.text(ass.text)).appendTo(jAss);
                }
            }
            // 没图标，必须要有文字
            else {
                $('<b>').text(UI.text(ass.text || "i18n:com.input.assist"))
                    .appendTo(jAss);
            }
        }
    },
    //...............................................................
    setUnit : function(unit){
        var jUnit  = this.arena.find(".unit");

        // 声明了单位，显示一下
        if(unit) {
            jUnit.text(this.text(unit)).show();
        }
        // 木有单位，隐藏
        else{
            jUnit.hide();
        }
    },
    //...............................................................
    setPlaceholder : function(str) {
        this.arena.find("input").attr("placeholder", str ? this.text(str) : null);
    },
    //...............................................................
    resize : function(){
        var UI    = this;
        var opt   = UI.options;
        var jUnit = this.arena.find(".unit");
        if(opt.unit) {
            UI.arena.find(".unit").css({
                "line-height" : (jUnit.outerHeight() -1) + "px"
            });
            UI.arena.find("input").css({
                "padding-right" : jUnit.outerWidth()
            })
        }
    },
    //...............................................................
    _get_data : function(){
        var UI  = this;
        var opt = UI.options;
        var val = UI.arena.find(">.box>input").val();
        if(opt.trimData)
            val = $.trim(val);
        return val;
    },
    //...............................................................
    _set_data : function(s, jso){
        if((_.isNumber(s) && isNaN(s)) || _.isUndefined(s) || _.isNull(s))
            s = "";
        this.arena.find(">.box>input").val(s);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);