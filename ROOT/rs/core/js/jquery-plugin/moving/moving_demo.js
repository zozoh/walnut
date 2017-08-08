(function($, $z, $D){
//.............................................
function mark_viewport_to_inner_body(){
    $('iframe').each(function(){
        var jIfm = $(this);
        var ifRe = $D.rect.gen(jIfm, {
            boxing   : "content",
            scroll_c : true,
        });
        //console.log("iframe viewport:", $z.toJson(ifRe))
        var doc = jIfm[0].contentDocument;

        // 文档所处矩形
        var vps = $z.pick(ifRe,"top,left,width,height", true).join(",");
        
        // 看看是否有 main
        var jBody = $(doc.body);
        var jMain = jBody.find("main");
        if(jMain.length > 0) {
            jMain.attr("client", vps);

            var rect = $D.rect.gen(jMain, ifRe);
            vps = $z.pick(rect,"top,left,width,height", true).join(",");
            jMain.attr("viewport", vps);
        }
        // 搞在 body 上
        else {
            jBody.attr("client", vps);
        }
    });
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
        var str = "<b>startInMs : </b>" + MVing.startInMs;
        str  += "\n<b>endInMs   : </b>" + MVing.endInMs;
 
        str  += "\n<b>cursor :</b>";
        str  += "\n<u> - win      : </u>" + $D.rect.dumpPos(MVing.cursor.win);
        str  += "\n<u> - client   : </u>" + $D.rect.dumpPos(MVing.cursor.client);
        str  += "\n<u> - viewport : </u>" + $D.rect.dumpPos(MVing.cursor.viewport);
        str  += "\n<u> - delta    : </u>" + $D.rect.dumpPos(MVing.cursor.delta);
        str  += "\n<u> - offset   : </u>" + $D.rect.dumpPos(MVing.cursor.offset);
 
        str  += "\n<b>rect :</b>";
        str  += "\n<u> - client   : </u>" + $D.rect.dumpValues(MVing.rect.client);
        str  += "\n<u> - viewport : </u>" + $D.rect.dumpValues(MVing.rect.viewport);
        str  += "\n<u> - target   : </u>" + $D.rect.dumpValues(MVing.rect.target);
        str  += "\n<u> - current  : </u>" + $D.rect.dumpValues(MVing.rect.current);
 
        str  += "\n<b>css :</b>";
        str  += "\n<u> - rect     : </u>" + $D.rect.dumpValues(MVing.css.rect);
        str  += "\n<u> - current  : </u>" + $D.rect.dumpValues(MVing.css.current);
 
        str  += "\n<b>posAt :</b>";
        str  += "\n<u> - win      : </u>" + $D.rect.dumpPos(MVing.posAt.win);
        str  += "\n<u> - client   : </u>" + $D.rect.dumpPos(MVing.posAt.client);
        str  += "\n<u> - viewport : </u>" + $D.rect.dumpPos(MVing.posAt.viewport);
        str  += "\n<u> - target   : </u>" + $D.rect.dumpPos(MVing.posAt.target);

        $('pre.sta').html(str);

        // str += "\n<b>direction :</b>";
        // str += "\n<u> - delta    : </u>" + $D.rect.dumpPos(MVing.direction.delta);
        // str += "\n<u> - offset   : </u>" + $D.rect.dumpPos(MVing.direction.offset);

        // 显示所有的感应器
        str = "<b>Sensors:</b>";
        for(var i=0; i<MVing.sensors.length; i++) {
            var sen = MVing.sensors[i];
            str += "\n<u>["+sen.index+"]</u> ";
            str += sen.visible ? "V" : "~";
            str += $z.alignLeft(" {" + $D.rect.dumpValues(sen.rect) + "}", 25);
            if(!sen.name)
            console.log(sen)
            str += $z.alignLeft("#" + (sen.name || "-nil-"), 14);
            str += (sen.text || sen.className || "-nil-");
        }
        str  += "\n<u>currentSensor            : </u>" + $z.toJson(MVing.currentSensor);
        str  += "\n<u>isTargetInClient         : </u>" + MVing.isTargetInClient;
        str  += "\n<u>isViewportInClient       : </u>" + MVing.isViewportInClient;
        str  += "\n<u>isViewportSameWithClient : </u>" + MVing.isViewportSameWithClient;
        str  += "\n<u>isCusorRelativeClient    : </u>" + MVing.isCusorRelativeClient;
        $('pre.sens').html(str);
    }
};
//.............................................
// 得到 iframe 对应的文档对象，以及其他关键变量
function getContainerList(){
    var jIfm = $('iframe');
    var doc   = $('.d-inner  > iframe')[0].contentDocument;
    var doc2  = $('.d-inner2 > iframe')[0].contentDocument;
    return [$(doc.body), $(doc2.body).find("main"), $(".d-top > .con")];
}
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
var mvCon = {
    trigger : ".mv-demo-item",
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
    scrollSensor : {x:"10%", y:30},
    sensors : function(){
        var MVing = this;
        var list  = [];
        //console.log(this.$viewport.find(".sen-drop").length)
        this.$viewport.closest("body").find(".sen-drop").each(function(){
            var jq = $(this);
            list.push({
                name : "drop",
                text : jq.text(),
                $ele : jq,
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
    "default" : function(frA, frB, jC){
        // 内联文档
        $(frA.contentDocument.body).moving(_.extend({}, mvCon, {
            clientRect : function(){
                return $D.rect.gen(frA, {
                    boxing   : "content",
                    scroll_c : true,
                });
            },
        }));

        // 内联文档：内部元素
        $(frB.contentDocument.body).moving(_.extend({}, mvCon, {
            viewport : function(){
                return this.$client.find("main");
            },
            clientRect : function(){
                return $D.rect.gen(frB, {
                    boxing   : "content",
                    scroll_c : true,
                });
            },
        }));

        // 顶级文档
        jC.moving(_.extend({}, mvCon, {
            // 呵呵，啥都不用写
        }));
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

        // 得到选区
        var cl = getContainerList();

        // 执行行为
        A[ac]($('.d-inner  iframe')[0],
              $('.d-inner2 iframe')[0],
              $('.d-top > .con'));
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