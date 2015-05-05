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
            // this.listenModel("login:none"   ,this.on_login_none);
            // this.listenModel("login:ok"     ,this.on_login_ok);
            // this.listenModel("login:fail"   ,this.on_login_fail);
            this.listenModel("screen:clear", this.clearScreen);
            this.listenModel("cmd:wait", this.on_cmd_wait);
            this.listenModel("show:info", this.info);
            this.listenModel("show:err", this.err);
            this.listenModel("show:text", this.info);
            this.listenModel("show:form", this.form);
            this.listenModel("objs:begin", this.on_objs_begin);
            this.listenModel("objs:add", this.on_objs_add);
            this.listenModel("objs:end", this.on_objs_end);
            this.listenModel("do:upload", this.on_do_upload);
            /*
             var jq = this.ccode("input");`
             jq.children('.ui-console-ps').html("I am console");
             this.arena.append(jq);
             */
            // this.$pel.on("click",function(){
            //     $(this).find(".ui-arena .ui-console-inbox").focus();
            //     var ele = $(this).find(".ui-arena .ui-console-inbox")[0];
            //     ele.setSelectionRange(ele.value.length,ele.value.length)
            // });
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
        info: function (s) {
            if (s[s.length - 1] == '\n')
                s = s.substring(0, s.length - 1);
            this.arena.append(this.ccode("block").text(this.msg(s)));
        },
        err: function (s) {
            if (s[s.length - 1] == '\n')
                s = s.substring(0, s.length - 1);
            this.arena.append(this.ccode("error").text(this.msg(s)));
        },
        form: function (PID, form) {
            var answer = {};
            var keys = [];
            var UI = this;
            var Mod = this.model;
            for (var key in form) {
                keys.push(key);
            }
            // 有表单项
            if (keys.length > 0) {
                var _do_form = function (keys, index) {
                    var key = keys[index];
                    var af = form[key];
                    UI.prompt(af.prompt, function (str, jBlock) {
                        answer[key] = str;
                        if (++index < keys.length) {
                            _do_form(keys, index);
                        }
                        // 填充完毕，准备提交答案
                        else {
                            console.log(answer);
                            Mod.trigger("form:submit", PID, answer);
                        }
                    });
                };
                // 开始填充
                _do_form(keys, 0);
            }
            // 没有表单
            else {
                this.err("e.console.form.empty");
            }
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
        on_objs_begin: function (blockId, options) {
            var jq = this.ccode("objs").attr("id", blockId).appendTo(this.arena);
            jq.attr("objs-mode", options.mode || "short");
        },
        _gen_obj_nm: function (obj) {
            var jq = $('<span att="nm"><span>' + obj.nm + '</span></span>');
            var span = jq.children();
            jq.addClass("ui-console-obj-" + obj.race);
            return jq;
        },
        on_objs_add: function (blockId, obj) {
            var jObjs = $("#" + blockId);
            var mode = jObjs.attr("objs-mode");
            var jq = this.ccode("objs.obj." + mode);
            if ("long" == mode) {
                jq.append($('<span att="race">' + obj.race.substring(0, 1) + '</span>'));
                jq.append($('<span att="ow">' + obj.ow + '</span>'));
                jq.append($('<span att="len">' + (obj.len ? obj.len : '--') + '</span>'));
                jq.append($('<span att="lm">' + obj.lm + '</span>'));
                jq.append(this._gen_obj_nm(obj));
            }
            else if ("short" == mode) {
                jq.append(this._gen_obj_nm(obj));
            }
            else {
                throw "Kao!!!!!";
            }
            $("#" + blockId).append(jq);
        },
        on_objs_end: function (blockId) {
        },
        //...................................................................
        on_cmd_wait: function (se) {
            this._watch_usr_input(se);
        },
        //...................................................................
        _watch_usr_input: function (se) {
            var UI = this;
            var ps = se.me + "@" + UI.$pel.attr("appnm") + "$ ";
            UI.prompt(ps, function (str, jBlock) {
                // 显示旧的输入行
                var jq = UI.ccode("prompt.read");
                jq.find('.ui-console-ps').text(ps);
                jq.find('.ui-console-text').text(str);
                UI.arena.append(jq);
                jBlock.remove();

                // 进行判断 ...
                UI.model.trigger("cmd:exec", str);

                // 准备新的输入行
                //UI._watch_usr_input(se);      
            });
        }
        //...................................................................
        // _show_login_form : function(){
        //     var UI = this;
        //     UI.prompt(UI.msg("login.usr")+":", function(loginName){
        //         UI.prompt(UI.msg("login.pwd")+":", function(loginPwd){
        //             UI.model.trigger("login:do", loginName, loginPwd);
        //         }, true);
        //     });
        // }
        //...................................................................
    });
//=======================================================================
});