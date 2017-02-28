(function($, $z){
//...........................................................
function do_reload(jData, opt){
    //console.log("dynamic do_reload")
    // 检查 api
    if(!opt.apiUrl){
        $('<div class="msg-error">').text("No Api!").appendTo(jData);
        return jData;
    }

    // 检查模板
    if(!opt.tmplInfo){
        $('<div class="msg-error">').text("No Template!").appendTo(jData);
        return jData;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 将参数处理成可向数据接口提交的形式
    var params;
    try{
        //console.log(opt.apiInfo.params);
        var setting = HmRT.parseSetting(opt.apiInfo.params || {}, true);
        var re  = HmRT.evalResult(opt.params, {
            context : opt.paramContext,
            setting : setting,
            request : window.__REQUEST,
            getComValue : function(comId) {
                var jTa  = $("#" + comId);
                var jqfn = jTa.attr("wn-rt-jq-fn");
                var selector = jTa.attr("wn-rt-jq-selector");
                if(jqfn) {
                    var re;
                    if(selector){
                        var jTa2 = jTa.find(selector);
                        re = jTa2[jqfn]("value");
                    }else{
                        re = jTa[jqfn]("value");
                    }
                    return re;
                }
            }
        });
        params = re.data;
    }
    // 处理参数解析的错误
    catch(errMsg){
        $('<div class="msg-error">').text(errMsg).appendTo(jData);
        throw errMsg;
    }

    // 请求
    $.get(opt.apiUrl, params||{}, function(re){
        // 清除正在加载的显示
        jData.empty();

        // api 返回错误
        if(/^e[.]/.test(re)){
            $('<div class="msg-error">').text(re).appendTo(jData);
            return jData;
        }
        // 试图解析数据
        try {
            // 记录数据
            var data = $z.fromJson(re);

            // 得到模板信息
            var tmplInfo = opt.tmplInfo;

            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // 转换数据
            //console.log("dynamic draw", data);
            var d2 = HmRT.convertDataForTmpl(data, tmplInfo.dataType);
            if(HmRT.isDataEmptyForTmpl(d2, tmplInfo.dataType)) {
                 $('<div class="msg-info">').text("No Data").appendTo(jData);
                return;
            }
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // 准备绘制模板参数
            var tmplOptions = _.extend(opt.options);
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // 确保设置模板皮肤
            if(opt.skinSelector)
                jData.prop("className", opt.skinSelector);
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // 调用模板的 jQuery 插件进行绘制
            jData[tmplInfo.name](d2, tmplOptions);

        }
        // 接口调用错误
        catch (errMsg) {
            $('<div class="msg-error">').text(errMsg).appendTo(jData);
        }
    });

    // 返回自身以便链式赋值
    return this;
}
//...........................................................
$.fn.extend({ "hmc_dynamic" : function(opt){
    // 记录自己
    var jData = this;

    // 因为要等待其他插件先加载，自己先延迟执行
    window.setTimeout(function(){
        do_reload(jData, opt);
    }, 0);
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

