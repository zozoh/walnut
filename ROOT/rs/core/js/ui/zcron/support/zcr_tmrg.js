(function($z){
$z.declare([
    'zui',
    'ui/zcron/support/zcr_methods',
], function(ZUI, ZCronMethods, SwitchUI){
//==============================================
var html = function(){/*
<div class="ui-arena zcr-tmrg">
    <table>
        <tr key="from">
            <td>从</td>
            <td><input placeholder="00:00:00" spellcheck="false"></td>
        </tr>
        <tr key="to">
            <td>至</td>
            <td><input placeholder="23:59:59" spellcheck="false"></td>
        </tr>
        <tr key="step">
            <td>每隔</td>
            <td>
                <input placeholder="30" spellcheck="false">
                <span><u v="h">小时</u><u v="m">分钟</u><u v="s">秒</u></span>
            </td>
        </tr>
    </table>
</div>
*/};
//==============================================
return ZUI.def("ui.zcron_tmrg", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    // 监听父控件的消息
    init : function(opt) {
        var UI = ZCronMethods(this);
        UI.listenUI(this.parent.parent, "data:change", this.update);
    },
    //...............................................................
    events : {
        'click [key="step"] u' : function(e){
            var jU = $(e.currentTarget);
            this.setStepUnit(jU.attr("v"));
            this.notifyChange();
        },
        'change input' : function(){
            this.notifyChange();
        }
    },
    //...............................................................
    setStepUnit : function(u) {
        this.arena.find('[key="step"] u').removeAttr("current");
        this.arena.find('[key="step"] u[v="'+u+'"]').attr("current", "yes");
    },
    //...............................................................
    notifyChange : function(){
        var str = this.getData();
        this.cronUI().setPart(0, str).setPart(1, "0 0 0");
    },
    //...............................................................
    update : function(ozc) {
        var UI = this;

        // 范围
        if(ozc.rgTime){
            var sFrom  = ozc.rgTime.left();
            var sTo    = ozc.rgTime.right();
            var tiFrom = $z.parseTimeInfo(sFrom);
            var tiTo   = $z.parseTimeInfo(sTo);
            UI.arena.find('[key="from"] input').val(tiFrom.toString(true));
            UI.arena.find('[key="to"] input').val(tiTo.toString(true));
        }
        // 没有的话，清空
        else {
            UI.arena.find('[key="from"] input').val("");
            UI.arena.find('[key="to"] input').val("");
        }

        // 重复
        var tr = ozc.timeRepeater;
        if(tr) {
            UI.arena.find('[key="step"] input').val(tr.stepValue);
            UI.setStepUnit(tr.stepUnit || "m");
        }
        // 没有的话，清空
        else {
            UI.arena.find('[key="step"] input').val("");
            UI.setStepUnit(null);
        }
    },
    //...............................................................
    getData : function(){
        var UI = this;

        // 得到数据
        var from = UI.arena.find('[key="from"] input').val() || "00:00";
        var to   = UI.arena.find('[key="to"]   input').val() || "23:59:59";
        var step = parseInt(UI.arena.find('[key="step"] input').val());
        if(isNaN(step))
            step = 30;
        var unit = UI.arena.find('[key="step"] u[current]').attr("v") || "m";

        // 拼接字符串
        return "T["+from+","+to+"]{0/"+step+unit+"}";
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);