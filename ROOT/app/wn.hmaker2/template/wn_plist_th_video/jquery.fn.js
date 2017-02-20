(function($, $z){
//..........................................................
$.fn.extend({ "wn_plst_th_video" : function(obj, opt){
    var jq = this;

    // 得到列表
    var list = (obj.list && obj.pager) ? obj.list : obj;
    
    // 检查数据结构
    if(!_.isArray(list)){
        $('<div class="dynamic-msg" m="api-error">')
            .html('<i class="zmdi zmdi-alert-triangle"></i> Data should be Array!')
                .appendTo(jq);
        return;
    }
    
    // 空数组
    if(list.length == 0){
        $('<div class="dynamic-msg" m="api-error">')
            .html('<i class="zmdi zmdi-alert-circle-o"></i> No data')
                .appendTo(jq);
        return;
    }
    
    // 循环数据
    for(var i=0; i<list.length; i++) {
        var obj  = list[i];
        var jDiv = $('<div><a><span><b></b><em></em></span></a></div>')
                    .appendTo(jq);
        // 根据值修改
        jDiv.find('a').css({
            "background-image" : "url(" + opt.API + "/gbox/thumb?"+obj.thumb+")"
        }).attr("href", $z.tmpl("{{href}}.wnml?{{key}}={{val}}")({
            href : opt.href,
            key  : opt.paramName  || "id",
            val  : obj[opt.objKey || "id"],
        }));
        jDiv.find("b").text(obj.th_nm);
        jDiv.find("em").text("分享者 : " + (obj.th_ow || "咸蛋超人"));
    }

    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);