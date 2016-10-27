(function($, $z){
//...........................................................
$.fn.extend({ "objshow" : function(opt){

    // 记录自己
    var jq = this;

    // 检查 api
    if(!opt.apiUrl){
        $('<div class="api-none">').text("No Api!").appendTo(jq);
        return jq;
    }

    // 检查模板
    if(!opt.tmplInfo){
        $('<div class="template-none">').text("No Template!").appendTo(jq);
        return jq;
    }

    var tmplOptions = opt.options || {};

    // TODO 处理参数

    // 请求
    $.post(opt.apiUrl, opt.params||{}, function(re){

        // api 返回错误
        if(/^e[.]/.test(re)){
            $('<div class="api-error">').text(re).appendTo(jq);
            return jq;
        }
        // 试图解析数据
        try {
            // 记录数据
            var obj = $z.fromJson(re);

            // 没数据
            if(!obj) {
                $('<div class="api-empty">').text("没有数据").appendTo(jq);
                return jq;
            }

            // 循环绘制
            var ele  = document.createElement(opt.tmplInfo.tagName || 'DIV');
            var jDiv = $(ele).appendTo(jq)[opt.tmplInfo.name](obj, tmplOptions);
            if(opt.skinSelector)
                jDiv.addClass(opt.skinSelector);
        }
        // 接口调用错误
        catch (errMsg) {
            $('<div class="api-error">').text(errMsg).appendTo(jq);
        }
    });

    // 返回自身以便链式赋值
    return this;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

