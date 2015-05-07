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
            if(m) {
                if(m[6])
                    $z.openUrl("/a/open/" + m[3] + "/" + m[6]);
                else
                    $z.openUrl("/a/open/" + m[3]);
                Mod.trigger("cmd:wait", se);
                return;
            }

            // 处理命令
            var url = '/a/run/' + app.name + '?' + encodeURIComponent(str);
            // $.get(url).done(function(re){
            //     Mod.trigger("show:txt", re);
            //     if(typeof callback == "function")
            //         callback.call(Mod, re);
            // }).fail(function(re){
            //     Mod.trigger("show:err", re.responseText);
            //     if(typeof callback == "function")
            //         callback.call(Mod, re.responseText);
            // });
            var oReq = new XMLHttpRequest();
            oReq._last = 0;
            oReq._show_msg = function(){
                var str = oReq.responseText.substring(oReq._last);
                oReq._last += str.length;
                // 正常显示
                if(oReq.status==200){
                    Mod.trigger("show:txt", str);
                }
                // 错误显示
                else {
                    Mod.trigger("show:err", str);   
                }
            };
            oReq.open("GET",url,true);
            oReq.onreadystatechange = function(){
                //console.log("rs:" + oReq.readyState + " status:" + oReq.status + " :: \n" + oReq.responseText);
                // LOADING | DONE 只要有数据输入，显示一下信息
                oReq._show_msg();
                // DONE: 请求结束了，调用回调
                if(oReq.readyState == 4){
                    if(typeof callback == "function")
                        callback.call(Mod, oReq.responseText);
                }
            };
            oReq.send();
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
