(function($, $z, $D){
//.............................................
function mark_viewport_to_inner_body(){
    var jIfm = $('.d-inner > iframe');
    var ifRe = $D.rect.gen(jIfm, {
        boxing   : "content",
        scroll_c : true,
    });
    //console.log("iframe viewport:", $z.toJson(ifRe))
    var doc = jIfm[0].contentDocument;
    $(doc.body).attr("viewport", 
        $z.pick(ifRe,"top,left,width,height", true).join(","));
}
//.............................................
function watch_window_mouse_events(win, type){
    $(win).on("mousemove", function(e){
        $('.mousetip .mu-pos').text(type + ": " + e.pageX + " / " + e.pageY);
    }).on("mouseup", function(){
        $('.mousetip .mu-st').text(type + " mouse up");
    }).on("mousedown", function(){
        $('.mousetip .mu-st').text(type + " mouse down");
    });
}
//.............................................
// 提供日志输出函数
window.__last_log_line = null;
var LOG = {
    msg : function(){
        var args = Array.from(arguments);
        var msg  = args.join(", ");
        var jPre = $('pre.log');
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
    },
    dumping : function(MVing) {
        var str = "<b>" + MVing.$target.attr("nm") + ":</b> ";

        str += "<u>start/endInMs: </u>" + MVing.startInMs + " / " + MVing.endInMs;
        str += "\n<u>currentSensor: </u>" + $z.toJson(MVing.currentSensor);

        str += "\n<b>posAt :</b>";
        str += "\n<u> - target   : </u>" + $D.rect.dumpPos(MVing.posAt.target);
        str += "\n<u> - client   : </u>" + $D.rect.dumpPos(MVing.posAt.client);
        str += "\n<u> - viewport : </u>" + $D.rect.dumpPos(MVing.posAt.viewport);

        str += "\n<b>cursor :</b>";
        str += "\n<u> - client   : </u>" + $D.rect.dumpPos(MVing.cursor.client);
        str += "\n<u> - viewport : </u>" + $D.rect.dumpPos(MVing.cursor.viewport);
        str += "\n<u> - delta    : </u>" + $D.rect.dumpPos(MVing.cursor.delta);
        str += "\n<u> - offset   : </u>" + $D.rect.dumpPos(MVing.cursor.offset);

        str += "\n<b>direction :</b>";
        str += "\n<u> - delta    : </u>" + $D.rect.dumpPos(MVing.direction.delta);
        str += "\n<u> - offset   : </u>" + $D.rect.dumpPos(MVing.direction.offset);

        str += "\n<b>rect :</b>";
        str += "\n<u> - viewport : </u>" + $D.rect.dumpValues(MVing.rect.viewport);
        str += "\n<u> - target   : </u>" + $D.rect.dumpValues(MVing.rect.target);
        str += "\n<u> - current  : </u>" + $D.rect.dumpValues(MVing.rect.current);

        str += "\n<b>css :</b>";
        str += "\n<u> - rect     : </u>" + $D.rect.dumpValues(MVing.css.rect);
        str += "\n<u> - current  : </u>" + $D.rect.dumpValues(MVing.css.current);

        $('pre.sta').html(str);
    }
};
//.............................................
// 得到 iframe 对应的文档对象，以及其他关键变量
function getContainerList(){
    var jIfm = $('.d-inner > iframe');
    var doc  = jIfm[0].contentDocument;
    return [$(doc.body), $(".d-top > .con")];
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
/*
bindMEV(jCon, ".mv-demo-item", {
    "click" : function(e){
        var jq = $(this);
        console.log("haha")

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
    //     LOG.msg("I am mouseup in Item");
    //     $(".z-mv-mask").remove();
    // }
});
*/
function bindMEV(jq, selector, funcMap){
    var MEVs = ["click",
        "mousedown",
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
            LOG.msg(
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
    // 关闭拖拽
    "none" : function(jCon){
        jCon.moving("destroy");
    },
    //.............................................
    // 默认拖拽
    "default" : function(jCon){
        var vpa = jCon.attr("viewport");
        jCon.moving({
            //viewportRect : vpa ? $D.rect.create(vpa) : null,
            viewportRect : function(){
                var vpa = this.$viewport.attr("viewport");
                return vpa ? $D.rect.create(vpa) : null;
            },
            on_ing : function(){
                this.$target.css(this.css.current);
                LOG.dumping(this);
            },
            on_end : function(){
                //this.$target.css(this.css.current);
                LOG.dumping(this);
            },
            dboundaryBy : "100%",
            dassist : {
                axis : ["right", "bottom"],
                axisFullScreen : false
            },
            scrollSensor : {
                x : "10%",
                y : 30
            },
            sensors : function(){
                var MVing = this;
                var list  = [];
                //console.log(this.$viewport.find(".sen-drop").length)
                this.$viewport.find(".sen-drop").each(function(){
                    var jq = $(this);
                    list.push({
                        name : "drop",
                        text : jq.text(),
                        rect : vpa ? MVing.getRectAtInnerDoc(jq) 
                                   :$D.rect.gen(jq),
                        inViewport : true,
                        visibility : true,
                        matchBreak : true,
                    });
                });
                return list;
            },
            sensorFunc : {
                "drop" : {
                    enter : function(sen){
                        console.log("enter", sen.index, sen.text, this);
                    },
                    leave : function(sen){
                        console.log("leave", sen.index, sen.text, this);
                    }
                }
            }
        });
    },
    //.............................................
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

        // 监控内部窗口的鼠标移动
        watch_window_mouse_events($('iframe')[0].contentDocument.defaultView, "iframe");

        // 监控外部窗口的鼠标移动
        watch_window_mouse_events(window, "window");

        // 模拟点击
        $("button").first().click();
    }, 500);
});
//.................................................
})(window.jQuery, window.NutzUtil, window.NutzDimension);