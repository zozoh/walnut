define(function (require, exports, module) {
//=======================================================================
// 获取当前的 app 的通用方法，不建议 UI 们直接获取 window._app
// 因为以后这个对象可能会被改名或变到别的地方
exports.app = function(){
    return window._app;
};
/*................................................................
# 执行命令的 options 是一组回调
{
    // 当得到返回的回调
    msgShow  : function(line){..}    // 显示一行输出
    msgError : function(line){..}    // 显示一行错误输出
    msgEnd   : function(){..}        // 表示不会再有输出了
    // 请求完毕的回调
    complete : function(content){..}     // 全部执行完
    done     : function(content){..}     // 执行成功
    fail     : function(Content){..}     // 执行失败
    // 所有回调的 this 对象，默认用本函数的 this
    context  : {..}
}
如果仅仅是一个函数，那么相当于
{
    complate : function(content){..}
}
*/
exports.exec = function (str, options) {
    var app = window._app;
    var se = app.session;
    
    // 一个回调处理所有的情况
    if (typeof options == "function") {
        options = {complete: options};
    }

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

    var cmdText = encodeURIComponent(str);
    var url = '/a/run/' + app.name + "?mos=" + encodeURIComponent(mos);
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
    oReq.open("POST", url, true);
    oReq.onreadystatechange = function () {
        //console.log("rs:" + oReq.readyState + " status:" + oReq.status + " :: \n" + oReq.responseText);
        // LOADING | DONE 只要有数据输入，显示一下信息
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
            
            // 调用完成后的回调
            var funcName = oReq.status == 200 ? "done" : "fail";
            $z.invoke(options, funcName,   [oReq._content], context);
            $z.invoke(options, "complete", [oReq._content], context);
        }
    };
    // oReq.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    // oReq.send("cmd=" + cmdText);
    oReq.setRequestHeader("Content-type", "text/html");
    oReq.send(cmdText);
};
//=======================================================================
});