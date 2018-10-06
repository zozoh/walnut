(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/pop/pop'
], function(ZUI, Wn, POP){
//==============================================
var html = function(){/*
<div class="ui-arena th3-wiz-done" ui-fitparent="yes">
    <header>
        <i class="far fa-check-circle"></i>
        <span>{{th3.export.done}}</span>
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
return ZUI.def("app.wn.th3_e_3_done", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "click footer a" : function() {
            var UI = this;
            var logs = UI.parent.getData().exportLog || [];
            var logStr = logs.join("\n");
            POP.openViewTextPanel({
                width  : "90%",
                height : "90%",
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
            $('<a>').text(UI.msg("th3.export.viewlog"))
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