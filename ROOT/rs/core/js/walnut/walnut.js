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

            // 调用子类自定义的 init
            $z.invoke(this, "init", [app]);
        },
        //................................................................
        // 执行命令
        on_cmd_exec: function (str, callback) {
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
            if (m) {
                console.log(m);
                if (m[6])
                    $z.openUrl("/a/open/" + m[3] + ":" + m[6]);
                else
                    $z.openUrl("/a/open/" + m[3]);
                Mod.trigger("cmd:wait", se);
                return;
            }

            // 处理命令
            var mos = "%%wn.meta." + $z.randomString(10) + "%%";
            //var regex = new RegExp("^(\n"+mos+":BEGIN:)(\\w+)(.+)(\n"+mos+":END\n)$","m");
            var mosHead = "\n" + mos + ":BEGIN:";
            var mosTail = "\n" + mos + ":END\n";

            var cmdText = encodeURIComponent(str);
            var url = '/a/run/' + app.name + "?mos=" + encodeURIComponent(mos);
            var oReq = new XMLHttpRequest();
            oReq._last = 0;
            oReq._content = "";
            oReq._moss = [];
            oReq._show_msg = function () {
                oReq.responseText = "hello world";
                var str = oReq.responseText.substring(oReq._last);
                oReq._last += str.length;
                //console.log("### str: " + str);
                // 是不是到了元数据输出的部分了
                var pos = str.indexOf(mosHead);
                //console.log("### pos:" + pos);
                if (pos >= 0) {
                    var from = pos + mosHead.length;
                    var pl = str.indexOf("\n", from);
                    var pr = str.indexOf(mosTail, pl);
                    oReq._moss.push({
                        type: str.substring(from, pl),
                        content: str.substring(pl + 1, pr)
                    });
                    str = str.substring(0, pos);
                    // for(var i=0;i<oReq._moss.length;i++){
                    //     console.log("type:[" + oReq._moss[i].type+"]");
                    //     console.log("content:\n" + oReq._moss[i].content);
                    // }
                }
                // 累计 Content
                oReq._content += str;
                if (str) {
                    // 正常显示
                    if (oReq.status == 200) {
                        Mod.trigger("show:txt", str);
                    }
                    // 错误显示
                    else {
                        Mod.trigger("show:err", str);
                    }
                }
            };
            oReq.open("POST", url, true);
            oReq.onreadystatechange = function () {
                //console.log("rs:" + oReq.readyState + " status:" + oReq.status + " :: \n" + oReq.responseText);
                // LOADING | DONE 只要有数据输入，显示一下信息
                oReq._show_msg();
                // DONE: 请求结束了，调用回调
                if (oReq.readyState == 4 && callback) {
                    // 处理请求的状态更新命令
                    for (var i = 0; i < oReq._moss.length; i++) {
                        var mosc = oReq._moss[i];
                        // 修改环境变量
                        if ("envs" == mosc.type) {
                            var app = Mod.get("app");
                            app.session.envs = $z.fromJson(mosc.content);
                        }
                    }
                    // 最后确保通知了显示流结束
                    Mod.trigger("show:end");
                    // 一个回调处理所有的情况
                    if (typeof callback == "function") {
                        callback.call(Mod, oReq._content);
                    }
                    // 对象 {done:..., fail:xxxx}
                    else {
                        // 成功
                        if (oReq.status == 200) {
                            if (typeof callback.done == "function") {
                                callback.done.call(Mod, oReq._content);
                            }
                        }
                        // 失败
                        else {
                            if (typeof callback.fail == "function") {
                                callback.fail.call(Mod, oReq._content);
                            }
                        }
                    }
                }
            };
            oReq.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
            oReq.send("cmd=" + cmdText);
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
