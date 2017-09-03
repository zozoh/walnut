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
            if(!this._ui_assist) {
                this.__on_change();
            }
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
            var UI = this;
            // 是否适配事件到助理，如果适配到助理
            // 就不需要原始的打开关闭快捷功能了
            if(UI._ui_assist && UI._assist.adaptEvents) {
                for(var codeName in UI._assist.adaptEvents) {
                    var codeValue = $z.getKeyCodeValue(codeName);
                    if(codeValue == e.which) {
                        // 调用助理的方法
                        var methodName = UI._assist.adaptEvents[codeName];
                        $z.invoke(UI._ui_assist, methodName);
                        // 返回吧，以防止后面逻辑被调用
                        return;
                    }
                }
            }
            // 回车
            if(13 == e.which) {
                UI.__on_change();
            }
            // 上箭头
            else if(38 == e.which) {
                UI.closeAssist();
            }
            // 下箭头
            else if(40 == e.which) {
                UI.openAssist();
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
    on_after_change : function(){
        this.closeAssist();
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
        var jAssBox = $('<div class="ass-box">').css({
            "visibility" : "hidden",
            "position"   : "fixed",
            "padding"    : _.isUndefined(UI._assist.padding) 
                            ? "" : UI._assist.padding
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
                // 标识
                UI.arena.attr("open-ass", "yes");

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
            // 移除遮罩
            $z.invoke(UI._ui_assist, "destroy");
            jAssBox.remove();
            UI.arena.find("> .ass-mask").remove();

            // 移除标识
            UI.arena.removeAttr("open-ass");
            
            // 通知改动
            UI.__on_change();
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