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
        // 标识输入框聚焦状态
        "focus > .com-input > .box > input" : function(e){
            $(e.currentTarget).attr("is-focus", "yes")
        },
        "blur > .com-input > .box > input" : function(e){
            $(e.currentTarget).removeAttr("is-focus");
        },
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
                        // 给自己续一秒，这样上下箭头之类的不会导致助理关闭
                        UI.__do_not_close_assist = true;
                        // 调用助理的方法
                        var methodName = UI._assist.adaptEvents[codeName];
                        var jq = $z.invoke(UI._ui_assist, methodName);
                        // 如果返回了项目，那么看看这个项目是不是需要滚动才能看见
                        if(jq) {
                            var jAssBox = UI.arena.find(".ass-box");
                            var rect = $D.rect.gen(jq);
                            var view = $D.rect.gen(jAssBox);
                            var over = $D.rect.overlap(rect,view);
                            // console.log("rect:", $D.rect.dumpValues(rect,"tlwh"),
                            //             "view:", $D.rect.dumpValues(view,"tlwh"),
                            //             "over:", $D.rect.dumpValues(over,"tlwh"));
                            if(over.height < rect.height) {
                                // 在上面
                                if(rect.top < view.top) {
                                    jAssBox[0].scrollTop -= (rect.height - over.height);
                                }
                                // 在下面
                                else {
                                    jAssBox[0].scrollTop += (rect.height - over.height);
                                }
                            }
                        }

                        // 返回吧，以防止后面逻辑被调用
                        return;
                    }
                }
            }
            // 回车
            if(13 == e.which) {
                e.preventDefault();
                UI.__on_change();
                UI.closeAssist();
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
        },
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

        // 只读
        jInput.prop('disabled', opt.readonly || false)

        // 助理
        UI.setAssist(opt.assist);
    },
    //...............................................................
    on_after_change : function(){
        var UI = this;
        // 被续了一秒
        if(UI.__do_not_close_assist) {
            UI.__do_not_close_assist = false;
            return;
        }
        // 强制关闭
        if(UI._assist && UI._assist.closeOnChange)
            UI.closeAssist();
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

        // 解析 uiType
        uiType = UI.__get_fld_quick_uiType(uiType) || uiType;

        // 显示出辅助弹出遮罩
        $('<div class="ass-mask">').appendTo(UI.arena);
        var jAssBox = $('<div class="ass-box">').css({
            "visibility" : "hidden",
            "position"   : "fixed",
            "min-width"  : UI.arena.find(">.box").width(),
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
                UI.redockAssist();
            });
        });
    },
    //...............................................................
    // 像 c_list 如果是 drawOnSetData 模式，则需要在数据加载完重新 dock
    // 否则会歪
    redockAssist : function(){
        var UI = this;
        if(UI._ui_assist) {
            var jAssBox = UI.arena.find(".ass-box");
            $z.dock(UI.arena.find(">.box"), jAssBox, "H");

            // 剪裁，确保在屏幕内
            var rect = $D.rect.gen(jAssBox);
            var win  = $D.dom.winsz();
            var over = $D.rect.overlap(rect, win);
            if($D.rect.area(rect) > $D.rect.area(over)) {
                jAssBox.css($z.pick(over, "width,height"));
            }
        }
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
        if(!jAss.html()) {
            UI.arena.removeAttr("show-ass");
            jAss.remove();
        }
    },
    //...............................................................
    mergeAssist : function(ass, isUndefined) {
        var UI = this;
        if(UI._assist) {
            if(isUndefined) {
                for(var key in ass) {
                    if(_.isUndefined(UI._assist[key])) {
                        UI._assist[key] = ass[key];
                    }
                }
                // 重新应用一下 padding
                if(UI._ui_assist) {
                    UI.arena.find(".ass-box").css({
                        "padding" : _.isUndefined(UI._assist.padding) 
                                        ? ""
                                        : UI._assist.padding
                    });
                }
            }
            // 直接替换
            else {
                _.extend(UI._assist, adaptEvents);
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
    focus : function(){
        this.arena.find(" > .box > input").focus();
    },
    //...............................................................
    isFocus : function(){
        return this.arena.find(" > .box > input").attr("is-focus")
                ? true : false;
    },
    //...............................................................
    isBlur : function(){
        return this.arena.find(" > .box > input").attr("is-focus")
                ? false : true;
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
        var jIn = UI.arena.find(">.box>input");
        var val = jIn.val();
        if(opt.trimData)
            val = $.trim(val);

        if("int" == opt.valueType) {
            var n = parseInt(val);
            if(isNaN(n)) {
                n = UI.__old_val || 0;
                jIn.val(n);
                return n;
            }
        }

        return val;
    },
    //...............................................................
    _set_data : function(s, jso){
        var UI  = this;
        var opt = UI.options;
        var jIn = UI.arena.find(">.box>input");
        if((_.isNumber(s) && isNaN(s)) || _.isUndefined(s) || _.isNull(s))
            s = "";
        if("int" == opt.valueType) {
            var n = parseInt(s);
            if(isNaN(n)) {
                n = UI.__old_val || jIn.val() || 0;
            }
            s = n;
        }
        jIn.val(s);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);