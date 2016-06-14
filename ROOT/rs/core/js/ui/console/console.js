(function($z){
$z.declare(['zui', 'wn/util'], function(ZUI, Wn){
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
    var UI = ZUI.checkInstance(ele);
    var me = $(ele);
    var isPassword = me.attr("type") == "password";
    
    // 如果是 tab
    if (e.which == 9) {
        e.preventDefault();
    }
    // 回车，添加历史记录，并清除索引
    else if (e.which == 13) {
        var str = $.trim(me.val());
        if(!/^(exit|clear)$/g.test(str))
            UI.history(str);
    }
    // 上箭头
    else if (e.which == 38) {
        e.preventDefault();
        var s = UI.history(-1);
        if(s)
            me.val(s);
    }
    // 下箭头 
    else if (e.which == 40) {
        e.preventDefault();
        var s = UI.history(1);
        if(s)
            me.val(s);
    }
    // Ctrl+K 清除历史
    else if(e.which == 75 && e.metaKey){
        this.clearScreen();
    }
};

// 点击 .ui-arena 应该聚焦到输入框
var on_click_arena = function () {
    // 没有选择文本，才聚焦
    if(!$z.getSelectedTexts()){
        this.arena.find(".ui-console-inbox").focus();
    }
};
//=======================================================================
return ZUI.def("ui.console", {
    dom: "ui/console/console.html",
    css: "ui/console/console.css",
    i18n : "ui/console/i18n/{{lang}}.js",
    init: function (options) {
        var UI  = this;
        var app = Wn.app();
        UI.app = app;

        // 消费原本的 PWD
        var oldPWD = UI.local("PWD");
        if(oldPWD){
            //console.log("oldPWD", oldPWD)
            UI.local("PWD", null);
            app.session.envs.PWD = oldPWD;
        }
        else{
            //console.log("oldPWD nil")
        }

        UI.listenModel("screen:clear", UI.clearScreen);
        UI.listenModel("cmd:wait", UI.on_cmd_wait);
        UI.listenModel("show:err", UI.on_show_err);
        UI.listenModel("show:txt", UI.on_show_txt);
        UI.listenModel("show:end", UI.on_show_end);

        // 如果页面重新加载
        // window.addEventListener("beforeunload", function (event) {
        //     var PWD = UI.app.session.envs.PWD;
        //     event.returnValue = UI.msg("console.unload") + " : \n" + PWD;
        //     UI.local("PWD", PWD);
        //     return event.returnValue;
        // });
    },
    //..............................................................
    // 模块启动的主函数
    redraw: function () {
        // 需要控件调用者在 render 里面调用 on_cmd_wait()
        // 这样第一次的时候光标不对的问题就解决了
        // 原因是，redraw 的时候，DOM 对象还没显示出来
        // this.on_cmd_wait();
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
        this.on_cmd_wait();
    },
    _font: {
        reset: function () {
            this.font = 0;      // [1,8]
            this.color = -1;     // [30,37]
            this.bgcolor = -1;     // [40,47]
        },
        wrap: function (str) {
            var bFont = this.font >= 1 && this.font <= 8;
            var bColor = this.color >= 30 && this.color <= 37;
            var bBgColor = this.bgcolor >= 40 && this.bgcolor <= 47;
            var html = '<span';
            if (bFont || bColor || bBgColor) {
                html += ' class="';
                if (bFont)    html += ' ui-console-f-font' + this.font;
                if (bColor)   html += ' ui-console-f-color' + this.color;
                if (bBgColor) html += ' ui-console-f-bgcolor' + this.bgcolor;
                html += '"';
            }
            html += '></span>';
            return $(html).text(str);
        },
        parse: function (fs) {
            var ss = fs.split(";");
            if (ss.length > 0) {
                var f = parseInt(ss[0]);
                if (f == 0)
                    this.reset();
                this.font = f;
            }
            if (ss.length > 1) {
                this.color = parseInt(ss[1]);
            }
            if (ss.length > 2) {
                this.bgcolor = parseInt(ss[2]);
            }
        }
    },
    on_show_end: function () {
        if (this._old_s) {
            this.__print_txt(this._old_s);
            this._old_s = "";
        }
    },
    on_show_txt: function (s) {
        var old = this._old_s || "";
        s = old + s;
        // 寻找最后换行，之前的输出
        var i = s.length - 1;
        for (; i >= 0; i--) {
            var b = s.charCodeAt(i);
            if (b == 0x0a) {
                i++;
                var str = s.substring(0, i);
                this.__print_txt(str);
                break;
            }
        }
        // 记录还没显示的数据
        this._old_s = s.substring(i);
    },
    __print_txt: function (s) {
        var jq = this.ccode("block");
        this.__join_txt(jq, s);
        // 显示
        this.arena.append(jq);
        //this.arena[0].scrollIntoView({block: "end", behavior: "smooth"});
        //this.arena[0].scrollIntoView({block: "end", behavior: "smooth"});
        jq[0].scrollIntoView({block: "end", behavior: "smooth"});
    },
    __join_txt : function(jq, s){
        // 试图对颜色码进行分析
        var l = 0;
        var r = 0;
        for (; r < s.length; r++) {
            var b = s.charCodeAt(r);
            // 遇到颜色
            if (b == 0x1b) {
                // 有字符串，先消费
                if (r > l) {
                    jq.append($(this._font.wrap(s.substring(l, r))));
                }
                // 试图读取控制码
                r += 2;
                l = r;
                for (; r < s.length; r++) {
                    var c = s.charAt(r);
                    if (c == "m")
                        break;
                }
                this._font.parse(s.substring(l, r));
                l = r + 1;
            }
        }
        // 最后输出剩余的
        if (r > l) {
            jq.append($(this._font.wrap(s.substring(l, r))));
        }
    },
    on_show_err: function (s) {
        if (s[s.length - 1] == '\n')
            s = s.substring(0, s.length - 1);
        this.arena.append(this.ccode("error").text(this.msg(s)));
    },
    //...................................................................
    _prompt: function (ps, callback, isPassword) {
        var UI = this;
        var jq = UI.ccode("prompt");
        UI.arena.append(jq);
        var left = jq.find(".ui-console-ps").text(ps).outerWidth();
        var jInbox = jq.find(".ui-console-inbox");
        if (isPassword)
            jInbox.attr("type", "password");
        jInbox
            .css("padding-left", left + 9)
            .data("on_key_enter", callback)
            .focus().click();
        UI.arena.click();
    },
    //...................................................................
    on_cmd_wait: function () {
        var UI = this;
        var app = Wn.app();
        var se = app.session;
        this._watch_usr_input(se);
    },
    //...................................................................
    _render_ps1: function(se) {
        var envs = se.envs;
        var ps1  = envs["PS1"] || "\\u:\\W\\$ ";
        var re   = "";
        for(var i=0;i<ps1.length;i++){
            var c = ps1.charAt(i);
            // 转义
            if("\\"==c){
                c = ps1.charAt(++i);
                switch(c){
                case "u":
                    re += envs["MY_NM"];
                    break;
                case "w":
                    var pwd = envs["PWD"];
                    re += pwd==envs["HOME"] ? "~" : envs["PWD"];
                    break;
                case "W":
                    var pwd = envs["PWD"];
                    if(pwd == envs["HOME"]){
                        re += "~";
                    }
                    else if(pwd=="/"){
                        re += "/";
                    }
                    else {
                        re += pwd.substring(pwd.lastIndexOf("/")+1);
                    }
                    break;
                case "$":
                    re += envs["MY_NM"]=="root" ? "#" : "$";
                    break;
                default:
                    re += c;
                }
            }
            // 其他字符输出
            else{
                re += c;
            }
        }
        return re;
    },
    //...................................................................
    _watch_usr_input: function (se) {
        var UI = this;
        //var ps = "[" + se.me + "@" + _app.name + " " + Mod.cph + "]" + (se.me == "root" ? "#" : "$");
        //var ps = se.me + "@" + _app.name + "$ ";
        var ps = this._render_ps1(se);
        UI._prompt(ps, function (str, jBlock) {
            str = $.trim(str);
            var cmdText = str;

            // 显示旧的输入行
            var jq = UI.ccode("prompt.read");
            jq.find('.ui-console-ps').text(ps);
            jq.find('.ui-console-text').text(str);
            UI.arena.append(jq);
            jBlock.remove();

            // 退出登录
            if ("exit" == cmdText) {
                $.get("/u/do/logout", function (re) {
                    window.location = "/";
                });
                return;
            }
            // 清屏幕
            if ("clear" == cmdText) {
                UI.clearScreen();
                return;
            }
            // 打开
            var m = cmdText.match(/(open)([ \t]+)([0-9a-zA-Z_.-]{1,})(([ \t]+)(.+))?/);
            if (m) {
                var path = m[6];
                var params = undefined;
                // 有打开路径 ...
                if(path){
                    // 非绝对路径，补上前缀
                    if(!/^[~\/].*$/.test(path))
                        path = UI.app.session.envs.PWD + "/" + path;
                    params = {ph : path};
                }
                $z.openUrl("/a/open/" + m[3], "_blank", "GET", params);
                UI.on_cmd_wait();
                return;
            }

            // 处理命令
            Wn.exec(cmdText, {
                context  : UI,
                msgShow  : UI.on_show_txt,
                msgError : UI.on_show_err,
                msgEnd   : UI.on_show_end,
                complete : function () {
                    // 本地保存一下当前路径
                    UI.local("PWD", UI.app.session.envs.PWD);
                    // 继续准备接受用户下一个指令
                    UI.on_cmd_wait();
                }
            });
            // 准备新的输入行
            //UI._watch_usr_input(se);      
        });
    },
    //...................................................................
    // 添加或者获取历史记录
    history : function(obj){
        // 获得自己本地的命令历史记录
        var UI = this;
        var history = [];
        var session = Wn.app().session;
        var his_key = "console_" + session.me;
        if (localStorage) {
            history = $z.fromJson(localStorage.getItem(his_key) || "[]");
        }
        // 添加历史记录
        if(typeof obj == "string"){
            var s = $.trim(obj);
            if(s){
                history.push(obj);
                // 最多记录  1000 个历史
                var n = 1000;
                if(history.length>n){
                    history = history.slice(history.length - n, history.length);
                }
                localStorage.setItem(his_key, $z.toJson(history));
                //localStorage.setItem(his_key, "[]");
            }
            UI.$el.removeAttr("console-his-index");
            return history.length;
        }

        // 如果没有历史记录，返回空
        if(history.length == 0)
            return null;

        // 那么必然就是获取历史信息咯, 向前是 -1 向后是  1
        var off = obj>0 ? 1 : -1;
        // 获取当前的索引信息
        var indexAttr = UI.$el.attr("console-his-index");
        var index = off + ( indexAttr ? indexAttr*1 : history.length);

        // 取不到历史记录
        if(index<0 || index >= history.length)
            return null;
        // 记录索引并返回内容
        UI.$el.attr("console-his-index", index);
        return history[index];
    }
    //...................................................................
});
//=======================================================================
});
})(window.NutzUtil);