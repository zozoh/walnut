(function($z){
$z.declare([
    'zui',
    'ui/zcron/support/zcr_methods',
], function(ZUI, ZCronMethods, SwitchUI){
//==============================================
var html = function(){/*
<div class="ui-arena zcr-tmrg" ui-fitparent="yes">
    <table>
        <thead><tr>
            <th>&nbsp;</th>
            <th>从</th>
            <th>至</th>
            <th>每隔</th>
        </tr></thead>
        <tbody></tbody>
        <tfoot>
            <tr><td colspan="4">
                <b>增加新的时间范围</b>
            </td></tr>
        </tfoot>
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
            this.setStepUnit(jU.closest("tr"), jU.attr("v"));
            this.notifyChange();
        },
        'change input' : function(){
            this.notifyChange();
        },
        'click .tr-item [a="del"]' : function(e){
            $(e.currentTarget).closest("tr").remove();
            this.notifyChange();
        },
        'click tfoot b' : function(){
            this.arena.find(".tr-empty").remove();
            this.__append_tr({
                region : $z.region("[0:00,23:59]")
            });
            this.notifyChange();
        }
    },
    //...............................................................
    setStepUnit : function(jTr, u) {
        jTr.find('[key="step"] u').removeAttr("current");
        jTr.find('[key="step"] u[v="'+u+'"]').attr("current", "yes");
    },
    //...............................................................
    notifyChange : function(){
        var str = this.getData();
        this.cronUI().setPart(0, str).setPart(1, "0 0 0");
    },
    //...............................................................
    update : function(ozc) {
        var UI = this;
        var jT = UI.arena.find('table tbody').empty();

        // 范围
        if(ozc.timeRepeaters.length > 0){
            for(var i=0; i<ozc.timeRepeaters.length; i++) {
                UI.__append_tr(ozc.timeRepeaters[i], jT);
            }
        }
        // 没有的话，清空
        else {
            jT.html('<tr class="tr-empty"><td colspan="4">没有数据</td></tr>');
        }

    },
    //...............................................................
    __append_tr : function(tr, jT) {
        var UI = this;
        jT = jT || UI.arena.find('table tbody');

        // 准备 HTML
        var html = UI.compactHTML(`
        <tr class="tr-item">
            <td a="del">
                <span data-balloon="删除" data-balloon-pos="left">
                    <i class="zmdi zmdi-close"></i>
                </span>
            </td>
            <td key="from"><input placeholder="00:00:00" spellcheck="false"></td>
            <td key="to"><input placeholder="23:59:59" spellcheck="false"></td>
            <td key="step">
                <input placeholder="30" spellcheck="false">
                <span><u v="h">小时</u><u v="m">分钟</u><u v="s">秒</u></span>
            </td>
        </tr>`);
        var jTr = $(html).appendTo(jT);

        // 填充字段
        var sFrom  = tr.region.left();
        var sTo    = tr.region.right();
        var tiFrom = $z.parseTimeInfo(sFrom);
        var tiTo   = $z.parseTimeInfo(sTo);
        jTr.find('[key="from"] input').val(tiFrom.toString(true));
        jTr.find('[key="to"] input').val(tiTo.toString(true));
        jTr.find('[key="step"] input').val(tr.stepValue || "");
        UI.setStepUnit(jTr, tr.stepUnit || "m");
    },
    //...............................................................
    getData : function(){
        var UI = this;
        var jT = UI.arena.find('table tbody');

        // 准备返回数据
        var trStrs = [];

        // 得到数据
        jT.find('tr.tr-item').each(function(){
            var jTr = $(this);
            var from = jTr.find('[key="from"] input').val() || "00:00";
            var to   = jTr.find('[key="to"]   input').val() || "23:59:59";
            var step = parseInt(jTr.find('[key="step"] input').val());
            if(isNaN(step))
                step = 30;
            var unit = jTr.find('[key="step"] u[current]').attr("v") || "m";
            // 拼接字符串
            trStrs.push("T["+from+","+to+"]{0/"+step+unit+"}");
        });

        // 返回
        return trStrs.join(" ") || null;
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);