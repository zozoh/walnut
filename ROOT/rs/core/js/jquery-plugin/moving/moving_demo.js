(function($, $z, $D){
//.............................................
function mark_viewport_to_inner_body(){
    var jIfm = $('section.d-inner > iframe');
    var ifRe = $D.rect.gen(jIfm, {
        boxing   : "content",
        scroll_c : true,
    });
    console.log($z.toJson(ifRe))
    var doc = jIfm[0].contentDocument;
    $(doc.body).attr("viewport", 
        $z.pick(ifRe,"top,left,width,height", true).join(","));
}
//.............................................
// 重载 console.log
window.__last_log_line = null;
window.console = {
    log : function(){
        var args = Array.from(arguments);
        var msg  = args.join(", ");
        var jPre = $('pre');
        if(window.__last_log_line == msg){
            var jLL = jPre.find("div:last-child");
            var jEm = jLL.find("em");
            if(jEm.length == 0) {
                $('<em>').text(2).prependTo(jLL);
            }else{
                jEm.text(jEm.text() * 1 + 1);
            }
        } else {
            window.__last_log_line = msg;
            $('<div>').appendTo(jPre).text(msg);
            var elePre = jPre[0];
            elePre.scrollTop = elePre.scrollHeight;
        }
    }
};
//.............................................
// 得到 iframe 对应的文档对象，以及其他关键变量
function getContainerList(){
    var jIfm = $('section.d-inner > iframe');
    var doc  = jIfm[0].contentDocument;
    return [$(doc.body), $("section.d-top")];
}
//.............................................
// 对容器执行操作
function do_con(callback) {
    var conList = getContainerList();
    for(var i=0; i < conList.length; i++){
        callback(conList[i]);
    }
}
//.............................................
function bindMEV(jq, selector, funcMap){
    var MEVs = ["mousedown",
        "mouseenter",
        "mouseleave",
        "mousemove",
        "mouseout",
        "mouseover",
        "mouseup"];
    funcMap = funcMap || {};
    for(var i=0;i<MEVs.length;i++){
        var MEV  = MEVs[i];
        var func = funcMap[MEV] || function(e){
            console.log(
                jq[0].tagName,
                e.type, 
                e.currentTarget.tagName, 
                e.currentTarget.className);
        };
        if(selector) {
            jq.on(MEV, selector, func);
        }else{
            jq.on(MEV, func);
        }
    }
}
//.................................................
// 定义行为
var A = {
    //.............................................
    // 默认拖拽
    "default" : function(jCon){
        bindMEV(jCon, ".mv-demo-item", {
            "mouseenter" : function(e){
                var jq = $(this);

                // 得到自己的viewport
                var opt = {
                    viewport : $D.rect.create(jCon.attr("viewport")),
                    scroll_c : true
                };

                // 得到自己的矩形坐标
                var rect = $D.rect.gen(jq, opt);

                // 转换成 css
                var css = $z.pick(rect, "top,left,width,height");

                // 在父窗口创建  Mask
                var jMask = $('<div class="z-mv-mask">').css(_.extend(css,{
                    "background" : "rgba(0,0,0,0.5)",
                    "position"   : "fixed",
                })).appendTo(document.body);

                jMask.one("click", function(){
                    $(this).remove();
                });
            },
            // "mouseleave" : function(e){
            //     console.log("I am mouseup in Item");
            //     $(".z-mv-mask").remove();
            // }
        });
    },
};
//.................................................
// 监控文档加载
$(function(){
    // 清空日志
    $('pre').dblclick(function(){
        $(this).empty();
    });
    // 动作事件
    $(document.body).on("click", "footer>button", function(e){
        var jB = $(this);
        var ac = jB.attr("a");

        // 标识主区域状态
        $(document.body).find("section").attr({
            "mv-mode": ac=="none" ? null : ac
        });

        // 执行行为
        do_con(function(jCon){
            A[ac](jCon);
        });
    });

    // 等内文档加载完毕
    window.setTimeout(function(){
        // 标识视口
        $(window).resize(mark_viewport_to_inner_body);
        mark_viewport_to_inner_body();

        // 模拟点击
        $("button").first().click();
    }, 200);
});
//.................................................
})(window.jQuery, window.NutzUtil, window.NutzDimension);