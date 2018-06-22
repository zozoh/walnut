(function($z){
$z.declare([
    'zui',
    'wn/util',
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena cmd-log" ui-fitparent="yes">
    <pre></pre>
</div>
*/};
//==============================================
return ZUI.def("app.wn.cmd_log", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function(){
        var UI   = this;
        var opt  = UI.options;

        // 是否执行命令
        if(opt.cmdText) {
            UI.runCommand(opt.cmdText);
        }
    },
    //...............................................................
    runCommand : function(cmdText) {
        var UI   = this;
        var opt  = UI.options;
        var jPre = UI.arena.find('>pre').empty();
        var context = opt.context || UI;

        // 预先显示信息
        if(opt.welcome) {
            $('<div>').html(opt.welcome).appendTo(jPre);
        }

        // 执行
        console.log(cmdText)
        Wn.exec(cmdText, {
            msgShow : function(str){
                $('<div class="msg-info">')
                    .text(str)
                    .appendTo(jPre)[0].scrollIntoView({
                        block: "end", behavior: "smooth"
                    });
            },
            msgError : function(str){
                $('<div class="msg-err">')
                    .text(str)
                    .appendTo(jPre)[0].scrollIntoView({
                        block: "end", behavior: "smooth"
                    });
            },
            done : function(re){
                $z.invoke(opt, "done", [re], context);
            },
            fail : function(re){
                $z.invoke(opt, "fail", [re], context);
            },
            complete : function(re){
                $z.invoke(opt, "complete", [re], context);
            }
        });
    },
    //...............................................................
    clear : function() {
        this.arena.find('>pre').empty();
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);