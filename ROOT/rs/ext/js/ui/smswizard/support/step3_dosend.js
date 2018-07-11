(function($z){
$z.declare([
    'zui',
    'wn/util'
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena smsw-step3-dosend" ui-fitparent="yes">
    <section>
        <div class="ssd-begin">{{smswizard.send_begin}}</div>
    </section>
    <footer st="sending">
        <a m="pause">{{smswizard.send_pause}}</a>
        <a m="goon">{{smswizard.send_go_on}}</a>
    </footer>
</div>
*/};
//==============================================
return ZUI.def("ui.ext.smsw_step3_dosend", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        'click footer a[m="pause"]' : function() {
            this.doPause();
        },
        'click footer a[m="goon"]' : function() {
            this.doSend();
        }
    },
    //...............................................................
    doPause : function() {
        var UI = this;
        if(UI.__HDL) {
            window.clearInterval(UI.__HDL);
        }
        UI.arena.find('footer').attr('st', 'pause');
    },
    //...............................................................
    doSend : function() {
        var UI = this;
        var data = UI.parent.getData();

        var opt = UI.options;
        var jSe = UI.arena.find('>section');

        if(!_.isArray(data.targets)){
            return;
        }

        // 确保清除
        if(UI.__HDL) {
            window.clearInterval(UI.__HDL);
        }

        // 标识状态
        UI.arena.find('footer').attr('st', 'sending');
        // console.log(data)
        //------------------------------------------------
        // 开始
        UI.arena.find('div.ssd-begin').text(UI.msg('smswizard.send_begin', {
            nb : data.targets.length
        }));
        //------------------------------------------------
        // 逐条发送，为了稍微限制一下访问速度，每条短信一秒一发
        var msBegin = Date.now();
        UI.__HDL = window.setInterval(function(){
            //------------------------------------------------
            // 到头了
            if(UI.__SEND_INDEX >= data.targets.length) {
                var du = Date.now() - msBegin;
                var ti = $z.parseTimeInfo(du, "ms");
                var msg = UI.msg('smswizard.send_done', {
                    nb : data.targets.length,
                    du : ti.toString()
                    // 计入消息
                });
                $('<div class="ssd-end">').text(msg).appendTo(jSe);
                // 确保滚动到底
                jSe[0].scrollTop = jSe[0].scrollHeight;
                // 停止并退出
                window.clearInterval(UI.__HDL);
                return;
            }
            //------------------------------------------------
            var ta = data.targets[UI.__SEND_INDEX++];
            var phone = ta[opt.phoneKey];
            var name  = ta[opt.nameKey];

            // 准备日志
            var jLine = $('<div class="ssd-send">').appendTo(jSe);
            var jCon  = $('<div class="ssds-con">').appendTo(jLine);
            $('<span k="status">')
                .html('<i class="zmdi zmdi-refresh zmdi-hc-spin"></i>')
                    .appendTo(jCon);
            $('<span k="nb">')
                .text( UI.__SEND_INDEX + "/" +  data.targets.length)
                    .appendTo(jCon);
            $('<span k="phone">').text(phone).appendTo(jCon);
            $('<span k="name">').text(name).appendTo(jCon);

            // 准备命令
            var cmdText = $z.tmpl('sms send -r {{phone}} -t i18n:{{tmplName}} \'<%=params%>\'')({
                phone : phone,
                tmplName : data.tmplName,
                params : $z.toJson(data.params || {})
            });
            $('<pre>').text(cmdText).appendTo(jLine);

            // 执行命令
            // window.setTimeout(function(jLine, index){
            //     var jSt = jLine.find('span[k="status"]');
            //     // 成功
            //     if(index %2 == 0) {
            //         jSt.attr('st','OK').html('<i class="zmdi zmdi-check"></i>');
            //     }
            //     // 失败
            //     else {
            //         jSt.attr('st','FAIL').html('<i class="zmdi zmdi-alert-triangle"></i>');
            //     }

            // }, 2000, jLine, UI.__SEND_INDEX);
            Wn.exec(cmdText, {
                context : {
                    jSt : jLine.find('span[k="status"]'),
                    jPr : jLine.find('pre'),
                    phone : phone,
                },
                done : function(re) {
                    // 失败的执行
                    if(/^e./.test(re)) {
                        this.jSt.attr('st','FAIL')
                            .html('<i class="zmdi zmdi-alert-triangle"></i>');
                        return;
                    }
                    // 解析一下
                    var reo = $z.fromJson(re);
                    var map = reo[this.phone] || {};
                    // 失败
                    if('OK' != map.msg) {
                        this.jSt.attr('st','FAIL')
                            .html('<i class="zmdi zmdi-alert-polygon"></i>');
                    }
                    // 那就是成功咯
                    else {
                        this.jSt.attr('st','OK').html('<i class="zmdi zmdi-check"></i>');
                    }
                },
                fail : function(re) {
                    this.jSt.attr('st','FAIL')
                        .html('<i class="zmdi zmdi-alert-triangle"></i>');
                },
                complete : function(re) {
                    $('<div class="re">').text(re).appendTo(this.jPr);
                }
            });

            // 确保滚动到底
            jSe[0].scrollTop = jSe[0].scrollHeight;
        }, 1000);  // End of  window.setInterval
    },
    //...............................................................
    setData : function(data) {
        this.__SEND_INDEX = 0;
        this.doSend();
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);