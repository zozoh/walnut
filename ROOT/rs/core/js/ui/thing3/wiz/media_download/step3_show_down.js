(function($z){
    $z.declare([
        'zui',
        'wn/util',
        'ui/pop/pop'
    ], function(ZUI, Wn, POP){
    //==============================================
    var html = function(){/*
    <div class="ui-arena th-export-3-done" ui-fitparent="yes">
        <header>
            <i class="far fa-check-circle"></i>
            <span>{{thing.export.done}}</span>
        </header>
        <section>
            <a href="#">
                <i class="fas fa-download"></i>
                <span></span>
            </a>
        </section>
        <footer></footer>
    </div>
    */};
    //==============================================
    return ZUI.def("app.wn.thmd_3_show_done", {
        dom  : $z.getFuncBodyAsStr(html.toString()),
        //...............................................................
        events : {
            "click footer a" : function() {
                var UI = this;
                var logs = UI.parent.getData().exportLog || [];
                var logStr = logs.join("\n");
                POP.openViewTextPanel({
                    width  : 640,
                    height : 480,
                    data   : logStr
                }, UI);
            }
        },
        //...............................................................
        setData : function(data) {
            var UI = this;
            //console.log(data)
            // 显示日志
            if(data.exportLog && data.exportLog.length > 0) {
                $('<a>').text(UI.msg("thing.export.viewlog"))
                    .appendTo(UI.arena.find('footer'));
            }
            // 显示下载文件
            if(data.oTmpFile) {
                var href = '/o/read/id:' + data.oTmpFile.id;
                var jA = UI.arena.find('section a');
                jA.find('span').text(data.oTmpFile.nm);
                jA.attr('href', href);
                if(data.setup.audoDownload) {
                    $z.openUrl(href, "_self");
                }
            }
        }
    });
    //===================================================================
    });
    })(window.NutzUtil);