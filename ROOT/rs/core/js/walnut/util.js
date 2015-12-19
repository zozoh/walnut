function WN() {
    var Wn = {
//=======================================================================
// 获取当前的 app 的通用方法，不建议 UI 们直接获取 window._app
// 因为以后这个对象可能会被改名或变到别的地方
        app: function () {
            return window._app;
        },
        /*................................................................
         执行一个命令，并且在一个弹出的日志窗口显示命令的返回情况
         - cmdText  : "xxxx"        # 命令文本
         - maskConf : {..}          # 可选，是弹出的遮罩层的配置信息
         - callback : {c}F(re)      # 可选，命令执行完毕后的回调，参数为命令的返回
         或者，你可以用更精细的方法来调用
         logpanel({
         cmdText  : "xxxxx",
         maskConf : {...},
         // .. 下面的参数字段与 exec 相同
         async
         complete
         done
         fail
         context
         // msgShow,msgError,msgEnd 会被本函数覆盖，你设置了也木用
         });
         */
        logpanel: function (cmdText, maskConf, callback) {
            var options;
            if (_.isString(cmdText)) {
                options = {cmdText: cmdText};
                if (_.isFunction(maskConf)) {
                    options.maskConf = {};
                    options.complete = maskConf;
                } else {
                    options.maskConf = maskConf;
                    options.complete = callback;
                }
            } else {
                options = cmdText;
            }
            // 显示遮罩
            var MaskUI = require('ui/mask/mask');
            new MaskUI(_.extend({
                width: "60%"
            }, options.maskConf)).render(function () {
                var jPre = $('<pre class="ui-log">').appendTo(this.$main);
                Wn.exec(options.cmdText, _.extend(options, {
                    msgShow: function (str) {
                        $('<div class="ui-log-info">')
                            .text(str)
                            .appendTo(jPre)[0].scrollIntoView({
                            block: "end", behavior: "smooth"
                        });
                    },
                    msgError: function (str) {
                        $('<div class="ui-log-err">')
                            .text(str)
                            .appendTo(jPre)[0].scrollIntoView({
                            block: "end", behavior: "smooth"
                        });
                    }
                }));
            });
        },
        /*................................................................
         # 执行命令的 options 是一组回调
         {
         processData : false  // 调用结束回调的时候，是否先解析数据
         dataType : "json"    // 如果 processData 为 true 时的数据类型，默认JSON
         async    : true      // 指明同步异步，默认 true
         // 当得到返回的回调
         msgShow  : {c}F(line){..}      // 显示一行输出
         msgError : {c}F(line){..}      // 显示一行错误输出
         msgEnd   : {c}F(){..}          // 表示不会再有输出了
         // 请求完毕的回调
         complete : {c}F(content){..}   // 全部执行完
         done     : {c}F(content){..}   // 执行成功
         fail     : {c}F(Content){..}   // 执行失败
         // 所有回调的 this 对象，默认用本函数的 this
         context  : {..}
         }
         如果仅仅是一个函数，那么相当于
         {
         complate : function(content){..}
         }
         */
        exec: function (str, options) {
            var app = window._app;
            var se = app.session;
            var re = undefined;

            // 没设置回调，则默认认为是同步调用
            if (_.isUndefined(options)) {
                options = {
                    async: false,
                    complete: function (content) {
                        re = content;
                    }
                };
            }
            // 一个回调处理所有的情况
            else if (_.isFunction(options)) {
                options = {async: true, complete: options};
            }

            // 有 options 的情况，默认是异步
            $z.setUndefined(options, "async", true);

            // 固定上下文
            var context = options.context || this;

            // 没内容，那么就表执行了，直接回调吧
            str = (str || "").trim();
            if (!str) {
                $z.invoke(options, "complete", [], context);
                return;
            }

            // 处理命令
            var mos = "%%wn.meta." + $z.randomString(10) + "%%";
            //var regex = new RegExp("^(\n"+mos+":BEGIN:)(\\w+)(.+)(\n"+mos+":END\n)$","m");
            var mosHead = "\n" + mos + ":BEGIN:";
            var mosTail = "\n" + mos + ":END\n";

            // 执行命令的地址
            var url = '/a/run/' + app.name;

            // 准备发送的数据
            var sendData = "mos=" + encodeURIComponent(mos);
            sendData += "&PWD=" + encodeURIComponent(se.envs.PWD);
            sendData += "&cmd=" + encodeURIComponent(str);

            var oReq = new XMLHttpRequest();
            oReq._last = 0;
            oReq._content = "";
            oReq._moss = [];
            oReq._moss_tp = "";
            oReq._moss_str = "";

            oReq._show_msg = function () {
                var str = oReq.responseText.substring(oReq._last);
                oReq._last += str.length;
                var pos = str.indexOf(mosHead);
                var tailpos = str.indexOf(mosTail);
                // 发现完整的mos
                if (pos >= 0 && tailpos >= 0) {
                    var from = pos + mosHead.length;
                    var pl = str.indexOf("\n", from);
                    var pr = str.indexOf(mosTail, pl);
                    oReq._moss.push({
                        type: str.substring(from, pl),
                        content: str.substring(pl + 1, pr)
                    });
                    str = str.substring(0, pos);
                }
                // 发现开头
                else if (pos >= 0 && tailpos < 0) {
                    var from = pos + mosHead.length;
                    var pl = str.indexOf("\n", from);
                    oReq._moss_tp = str.substring(from, pl);
                    oReq._moss_str = str.substring(pl + 1);
                    str = str.substring(0, pos);
                }
                // 发现结尾
                else if (pos < 0 && tailpos >= 0) {
                    oReq._moss_str += str.substr(0, tailpos);
                    oReq._moss.push({
                        type: oReq._moss_tp,
                        content: oReq._moss_str
                    });
                    oReq._moss_tp = "";
                    oReq._moss_str = "";
                    str = str.substring(tailpos + mosTail.length + 1);
                }
                // 累计 Content
                oReq._content += str;
                if (str) {
                    // 正常显示
                    if (oReq.status == 200) {
                        $z.invoke(options, "msgShow", [str], context);
                    }
                    // 错误显示
                    else {
                        $z.invoke(options, "msgError", [str], context);
                    }
                }
            };

            oReq.open("POST", url, options.async);
            oReq.onreadystatechange = function () {
                //console.log("rs:" + oReq.readyState + " status:" + oReq.status + " :: \n" + oReq.responseText);
                // LOADING | DONE 只要有数据输入，显示一下信息
                if (oReq._show_msg)
                    oReq._show_msg();
                // DONE: 请求结束了，调用回调
                if (oReq.readyState == 4) {
                    // 处理请求的状态更新命令
                    for (var i = 0; i < oReq._moss.length; i++) {
                        var mosc = oReq._moss[i];
                        // 修改环境变量
                        if ("envs" == mosc.type) {
                            app.session.envs = $z.fromJson(mosc.content);
                        }
                    }
                    // 最后确保通知了显示流结束
                    $z.invoke(options, "msgEnd", [str], context);

                    var re = oReq._content;

                    // 执行回调前数据处理
                    if (options.processData) {
                        if ("json" == options.dataType) {
                            re = $z.fromJson(re);
                            // 检查是不是 session 过期了，如果过期了，直接换地址
                            $z.checkSessionNoExists(re);
                        }
                    }

                    // 调用完成后的回调
                    var funcName = oReq.status == 200 ? "done" : "fail";
                    $z.invoke(options, funcName, [re], context);
                    $z.invoke(options, "complete", [re], context);
                }
            };
            oReq.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
            // oReq.send("cmd=" + cmdText);
            //oReq.setRequestHeader("Content-length", sendData.length);
            oReq.send(sendData);
            //oReq.send("mos=haha&pwd=/home/zozoh&cmd=cd")

            // 返回返回值，如同是同步的时候，会被设置的
            return re;
        }
    }; // ~End wn

    return Wn;
}

define(function (require, exports, module) {
    var Wn = WN();
// 输出
    _.extend(exports, Wn);
    window.Wn = Wn;
//=======================================================================
});


window.Wn = WN();