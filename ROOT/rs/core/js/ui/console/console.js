define(function (require, exports, module) {
    // View 的监听函数，this 为 View 本身
    var on_keypress_at_inbox = function (e) {
        var me = $(e.currentTarget);
        var isPassword = me.attr("type") == "password";
        // 如果是回车
        if (e.which == 13) {
            var func = me.data("on_key_enter");
            if (func) {
                me.removeData("on_key_enter");
                var str = me.val() || me.text();
                func.apply(me[0], [str, me.parents('.ui-console-block')]);
                // 密码输入框，处理完毕后清除内容，并只读
                if (isPassword) {
                    me.val("").attr("readonly", true);
                }
                // 普通文本将自身只读
                else {
                    me.attr("readonly", true);
                }
            }
        }
    };

    // View 的监听函数，this 为 View 本身
    var on_keydown_at_inbox = function (e) {
        var ele = e.currentTarget;
        var me = $(ele);
        var isPassword = me.attr("type") == "password";
        var Mod = this.model;
        // 如果是 tab
        if (e.which == 9) {
            e.preventDefault();
        }
        // 回车，清除历史
        else if (e.which == 13) {
            Mod.resetCommandIndex();
        }
        // 上箭头
        else if (e.which == 38) {
            e.preventDefault();
            var s = Mod.prevCommand();
            me.val(s);//[0].setSelectionRange(s.length, s.length);
            //ele.value = s;
            //ele.setSelectionRange(ele.value.length,ele.value.length)
            //L(s.length + " : " + s)
            //alert("haha")
            //me.focus();
            //ele.setSelectionRange(2, 2);
        }
        // 下箭头 
        else if (e.which == 40) {
            e.preventDefault();
            var s = Mod.nextCommand();
            me.val(s);
            //me.val(s);//[0].setSelectionRange(s.length, s.length);
        }
    };

    // 点击 .ui-arena 应该聚焦到输入框
    var on_click_arena = function () {
        this.arena.find(".ui-console-inbox").focus();
    };

    //=======================================================================
    var ZUI = require("zui");
    module.exports = ZUI.def("ui.console", {
        dom: "ui/console/console.html",
        css: "ui/console/console.css",
        init: function (options) {
            this.listenModel("screen:clear", this.clearScreen);
            this.listenModel("cmd:wait", this.on_cmd_wait);
            this.listenModel("show:err", this.on_show_err);
            this.listenModel("show:txt", this.on_show_txt);
            this.listenModel("do:upload", this.on_do_upload);
        },
        //..............................................................
        // 模块启动的主函数
        redraw: function () {
            var app = this.model.get("app");
            this.model.trigger("cmd:wait", app.session);
        },
        //...................................................................
        resize: function () {
            //console.log("I am console resize");
        },
        //...................................................................
        events: {
            "keypress .ui-console-inbox": on_keypress_at_inbox,
            "keydown  .ui-console-inbox": on_keydown_at_inbox,
            "click    .ui-arena": on_click_arena
        },
        //...................................................................
        clearScreen: function () {
            this.arena.empty();
        },
        _font : {
            reset : function(){
                this.font    = 0;      // [1,8]
                this.color   = -1;     // [30,37]
                this.bgcolor = -1;     // [40,47]
            },
            wrap : function(str){
                var bFont    = this.font>=1 && this.font<=8;
                var bColor   = this.color>=30 && this.color<=37;
                var bBgColor = this.bgcolor>=40 && this.bgcolor<=47;
                var html = '<span';
                if(bFont || bColor || bBgColor){
                    html += ' class="';
                    if(bFont)    html += ' ui-console-f-font'   +this.font;
                    if(bColor)   html += ' ui-console-f-color'  +this.color;
                    if(bBgColor) html += ' ui-console-f-bgcolor'+this.bgcolor;
                    html += '"';
                }
                html += '>' + str + '</span>';
                return html;
            },
            parse : function(fs) {
                var ss = fs.split(";");
                if(ss.length>0){
                    var f = parseInt(ss[0]);
                    if(f == 0)
                        this.reset();
                    this.font = f;
                }
                if(ss.length>1){
                    this.color = parseInt(ss[1]);
                }
                if(ss.length>2){
                    this.bgcolor = parseInt(ss[2]);
                }
            }
        },
        on_show_txt: function (s) {
            var jq = this.ccode("block");
            // 试图对颜色码进行分析
            var l = 0;
            var r = 0;
            for(;r<s.length;r++){
                var b = s.charCodeAt(r);
                // 遇到颜色
                if(b == 0x1b){
                    // 有字符串，先消费
                    if(r > l){
                        jq.append($(this._font.wrap(s.substring(l,r))));
                    }
                    // 试图读取控制码
                    r+=2; l = r;
                    for(;r<s.length;r++) {
                        var c = s.charAt(r);
                        if(c == "m")
                            break;
                    }
                    this._font.parse(s.substring(l,r));
                    l = r+1;
                }
            }
            // 最后输出剩余的
            if(r > l){
                jq.append($(this._font.wrap(s.substring(l,r))));
            }
            // 显示
            this.arena.append(jq);
        },
        on_show_err: function (s) {
            if (s[s.length - 1] == '\n')
                s = s.substring(0, s.length - 1);
            this.arena.append(this.ccode("error").text(this.msg(s)));
        },
        //...................................................................
        prompt: function (ps, callback, isPassword) {
            var UI = this;
            var jq = UI.ccode("prompt");
            UI.arena.append(jq);
            var left = jq.find(".ui-console-ps").text(ps).width();
            var jInbox = jq.find(".ui-console-inbox");
            if (isPassword)
                jInbox.attr("type", "password");
            jInbox
                .css("padding-left", left + 9)
                .data("on_key_enter", callback)
                .focus();
        },
        //...................................................................
        on_cmd_wait: function (se) {
            this._watch_usr_input(se);
        },
        //...................................................................
        _watch_usr_input: function (se) {
            var UI = this;
            var Mod = UI.model;
            var ps = se.me + "@" + UI.$pel.attr("appnm") + "$ ";
            UI.prompt(ps, function (str, jBlock) {
                // 显示旧的输入行
                var jq = UI.ccode("prompt.read");
                jq.find('.ui-console-ps').text(ps);
                jq.find('.ui-console-text').text(str);
                UI.arena.append(jq);
                jBlock.remove();

                // 进行判断 ...
                Mod.trigger("cmd:exec", str, function(){
                    Mod.trigger("cmd:wait", se);
                });

                // 准备新的输入行
                //UI._watch_usr_input(se);      
            });
        }
        //...................................................................
    });
//=======================================================================
});