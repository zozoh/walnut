define(function (require, exports, module) {
//===================================================================
    var Walnut = exports;
    window.Walnut = Walnut;
    Walnut.definitions = {};
    Walnut.instances = {};
    //...............................................................
    var _normlize_form = function (form) {
        return form;
    }
    //...............................................................
    var ModPrototype = {
        initialize: function (app) {
            // 记录自身实例
            window.Walnut.instances[this.cid] = this;

            this.set("app", app);
            this.on("cmd:exec", this.on_cmd_exec);
            this.on("form:submit", this.on_form_submit);

            // 调用子类自定义的 init
            $z.invoke(this, "init", [app]);
        },
        //................................................................
        // 执行命令
        on_cmd_exec: function (str) {
            var Mod = this;
            var app = Mod.get("app");
            var se = app.session;
            str = (str || "").trim();
            if (!str) {
                Mod.trigger("cmd:wait", se);
                return;
            }

            // 记录历史记录
            var history = Mod.get("history");
            if (history)
                history.push(str);
            //L(history)

            // 退出登录
            if ("exit" == str) {
                $.get("/u/do/logout", function (re) {
                    window.location = "/";
                });
                return;
            }
            // 清屏幕
            if ("clear" == str) {
                Mod.trigger("screen:clear");
                Mod.trigger("cmd:wait", se);
                return;
            }
            // 打开
            var m = str.match(/(open)([ \t]+)([0-9a-zA-Z_.-]{1,})(([ \t]+)(.+))?/);
            if(m) {
                if(m[6])
                    $z.openUrl("/a/open/" + m[3] + "/" + m[6]);
                else
                    $z.openUrl("/a/open/" + m[3]);
                Mod.trigger("cmd:wait", se);
                return;
            }

            // 处理命令
            // Mod.trigger("show:err", str);
            // Mod.trigger("cmd:wait", se);
            var myES = new EventSource('/a/run/' + app.name + '?' + encodeURIComponent(str));
            myES.onmessage = function (e) {
                var gi;
                try {
                    $z.log(e.data);
                    gi = $z.fromJson(e.data);
                } catch (e) {
                    Mod.trigger("show:err", "e.data.notxt : " + str);
                    return;
                }
                //console.log("PID:" + gi.PID + ":" + gi.mode);
                //console.log(gi);
                // 流停止
                if (gi.mode == "STREAM_END") {
                    e.target.close();
                    Mod.trigger("cmd:wait", se);
                }
                // 组开始
                else if (gi.mode == "GROUP") {
                    // 对象组
                    if (gi.type == "obj") {
                        Mod.trigger("objs:begin", gi.id, gi.options);
                    }
                }
                // 组结束
                else if (gi.mode == "GROUP_END") {
                    Mod.trigger("objs:end", gi.groupId);
                }
                // 组内对象
                else if (gi.groupId) {
                    // 对象
                    if (gi.type == "obj") {
                        Mod.trigger("objs:add", gi.groupId, $z.fromJson(gi.content));
                    }
                    // 其他信息
                    else {
                        Mod.trigger("show:err", $z.toJson(gi));
                    }
                }
                // 显示警告
                else if (gi.type == "W") {
                    Mod.trigger("show:err", gi.content);
                }
                // 显示信息
                else if (gi.type == "I") {
                    Mod.trigger("show:info", gi.content);
                }
                // 显示文本
                else if (gi.type == "TXT") {
                    Mod.trigger("show:text", gi.content);
                }
                // 显示表单
                else if (gi.type == "form") {
                    var form = _normlize_form($z.fromJson(gi.content));
                    Mod.trigger("show:form", gi.PID, form);
                }
                // 显示上传界面
                else if (gi.type == "upload") {
                    var options = $z.fromJson(gi.content);
                    Mod.trigger("do:upload", options);
                }
                // 其他形式，用文本显示
                else {
                    Mod.trigger("show:info", gi.content);
                }
            };  // end of myES.onmessage
        },
        //................................................................
        on_form_submit: function (PID, answer) {
            var Mod = this;
            var data = (typeof answer == "string") ? answer : $z.toJson(answer);
            $.ajax({
                type: "POST",
                url: "/a/answer/" + PID,
                contentType: "application/jsonrequest",
                data: data
            }).done(function (re) {
                Mod.trigger("show:info", $z.toJson(re));
            }).fail(function (re) {
                Mod.trigger("show:err", $z.toJson(re));
            });
        }
    };
    //===================================================================
    // 定义模块
    Walnut.def = function (modName, conf) {
        var ModDef = this.definitions[modName];
        if (!ModDef) {
            conf.modName = modName;

            // 融合模块的配置信息
            _.extend(conf, ModPrototype);

            // 生成 Backbone 的模块定义
            ModDef = Backbone.Model.extend(conf);
            ModDef.modName = modName;

            this.definitions[modName] = ModDef;
        }
        // 返回
        return ModDef;
    };
    // 获取模块实例
    Walnut.checkModel = function (modId) {
        var Mod = this.instances[modId];
        if (!Mod)
            throw "! model no exists : " + modId;
        return Mod;
    };
//===================================================================
});
