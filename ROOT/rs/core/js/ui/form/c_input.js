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
    css  : "ui/form/theme/component-{{theme}}.css",
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
            //console.log("!!! I am changed");
            this.__on_change();
        },
        // 下面三个时间，完美检测中文输入
        "compositionstart > .com-input > .box > input" : function(e){
            this.__compositionstart = true;
        },
        "compositionend > .com-input > .box > input" : function(e){
            this.__compositionstart = false;
            this.__do_when_input();
        },
        "input > .com-input > .box > input" : function(e){
            if(this.__compositionstart)
                return;
            this.__do_when_input();
        },
        // 特殊键盘事件
        "keydown > .com-input > .box > input" : function(e){
            // 上箭头
            if(38 == e.which) {
                this.closeAssist();
            }
            // 下箭头
            else if(40 == e.which) {
                this.openAssist();
            }
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

        // 固定了宽度
        if(opt.width) {
            UI.arena.css({
                "width" : opt.width
            });
        }

        // 声明了单位，显示一下
        UI.setUnit(opt.unit);

        // 占位符显示
        UI.setPlaceholder(opt.placeholder);

        // 助理
        UI.setAssist(opt.assist);
    },
    //...............................................................
    __do_when_input : function() {
        var UI  = this;

        // var jIn = UI.arena.find("> .box > input");
        // console.log("in>>", jIn.val());
        // 自动打开助理
        if(UI._assist && UI._assist.autoOpen && !UI._ui_assist) {
            UI.openAssist();
        }

        // 如果有助理，同步修改的值
        if(UI._ui_assist) {
            var v = UI.getData();
            UI._ui_assist.setData(v);
        }
    },
    //...............................................................
    openAssist : function(){
        var UI  = this;
        var opt = UI.options;

        // 没设置助理，或者已经打开了，就不用打开了
        if(!UI._assist || UI._ui_assist)
            return;

        // 得到数据
        var val = UI._get_data();

        // 计算助理的配置
        var uiType = $z.evalObjValue(UI._assist.uiType, [], UI);
        var uiConf = $z.evalObjValue(UI._assist.uiConf || {}, [], UI);

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

        // 加载辅助弹出控件
        seajs.use(uiType, function(AssUI){
            new AssUI(_.extend({}, uiConf, {
                $pel : jAssBox,
                on_init : function(){
                    UI._ui_assist = this;
                },
                on_change : function(v) {
                    UI._set_data(v);
                },
                on_depose : function(){
                    UI._ui_assist = undefined;
                }
            })).render(function(){
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
            $z.invoke(UI._ui_assist, "destroy");
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

        // 清除之前的助理
        if(!ass) {
            UI.closeAssist();
            UI._assist = undefined;
            return;
        }

        // 记录当前助理
        UI._assist = _.extend({}, ass);

        // 设置默认值
        $z.setUndefined(UI._assist, "autoOpen", true);

        // 设置属性开关
        UI.arena.attr("show-ass", ass ? "yes" : null);

        // 有图标
        if(ass.icon){
            $(ass.icon).appendTo(jAss);
        }
        // 有文字
        if(ass.text) {
            $('<b>').text(UI.text(ass.text)).appendTo(jAss);
        }
        // 什么都木有的话，呵呵，可能对方就不想显示吧，那就按下箭头好了
        if(!jAss.html())
            jAss.remove();
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
                "line-height" : jUnit.height() + "px"
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